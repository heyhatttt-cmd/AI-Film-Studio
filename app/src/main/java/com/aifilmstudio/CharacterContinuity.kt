package com.aifilmstudio

import android.content.Context
import org.json.JSONObject

/**
 * Offline-first character memory and scene continuity checks.
 *
 * This module stores descriptive production metadata locally. It does not claim
 * to perform biometric face matching or generate video frames.
 */
data class CharacterProfile(
    val id: String,
    val name: String,
    val faceDescription: String,
    val hair: String,
    val wardrobe: String,
    val accessories: String,
    val voiceDescription: String
)

data class SceneContinuity(
    val sceneId: String,
    val characterId: String,
    val location: String,
    val lighting: String,
    val props: String,
    val previousEndState: String,
    val nextStartState: String
)

data class ContinuityReport(
    val passed: Boolean,
    val issues: List<String>
)

class CharacterMemoryStore(context: Context) {
    private val preferences = context.getSharedPreferences(
        "ai_film_character_memory",
        Context.MODE_PRIVATE
    )

    fun saveCharacter(profile: CharacterProfile) {
        val json = JSONObject()
            .put("id", profile.id)
            .put("name", profile.name)
            .put("faceDescription", profile.faceDescription)
            .put("hair", profile.hair)
            .put("wardrobe", profile.wardrobe)
            .put("accessories", profile.accessories)
            .put("voiceDescription", profile.voiceDescription)
        preferences.edit().putString("character_${profile.id}", json.toString()).apply()
    }

    fun getCharacter(id: String): CharacterProfile? {
        val raw = preferences.getString("character_$id", null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            CharacterProfile(
                id = json.getString("id"),
                name = json.optString("name"),
                faceDescription = json.optString("faceDescription"),
                hair = json.optString("hair"),
                wardrobe = json.optString("wardrobe"),
                accessories = json.optString("accessories"),
                voiceDescription = json.optString("voiceDescription")
            )
        }.getOrNull()
    }

    fun saveScene(scene: SceneContinuity) {
        val key = "scene_${scene.sceneId}"
        val json = JSONObject()
            .put("sceneId", scene.sceneId)
            .put("characterId", scene.characterId)
            .put("location", scene.location)
            .put("lighting", scene.lighting)
            .put("props", scene.props)
            .put("previousEndState", scene.previousEndState)
            .put("nextStartState", scene.nextStartState)
        preferences.edit().putString(key, json.toString()).apply()
    }

    fun getScene(sceneId: String): SceneContinuity? {
        val raw = preferences.getString("scene_$sceneId", null) ?: return null
        return runCatching {
            val json = JSONObject(raw)
            SceneContinuity(
                sceneId = json.getString("sceneId"),
                characterId = json.getString("characterId"),
                location = json.optString("location"),
                lighting = json.optString("lighting"),
                props = json.optString("props"),
                previousEndState = json.optString("previousEndState"),
                nextStartState = json.optString("nextStartState")
            )
        }.getOrNull()
    }

    fun characterIds(): Set<String> = preferences.all.keys
        .filter { it.startsWith("character_") }
        .map { it.removePrefix("character_") }
        .toSet()

    fun clearCharacter(id: String) {
        preferences.edit().remove("character_$id").apply()
    }
}

object SceneContinuityChecker {
    /**
     * Compares adjacent scene descriptions and the saved character profile.
     * Empty fields are treated as unknown, not as a mismatch.
     */
    fun check(
        character: CharacterProfile?,
        previous: SceneContinuity,
        next: SceneContinuity
    ): ContinuityReport {
        val issues = mutableListOf<String>()

        if (previous.characterId != next.characterId) {
            issues += "Sahnelerdeki karakter kimliği farklı."
        }
        if (character != null && character.id != next.characterId) {
            issues += "Yeni sahne kayıtlı karakter profiliyle eşleşmiyor."
        }
        compare("Mekân", previous.location, next.location, issues)
        compare("Işık", previous.lighting, next.lighting, issues)
        compare("Nesne/aksesuar konumu", previous.props, next.props, issues)

        if (previous.previousEndState.isNotBlank() &&
            next.nextStartState.isNotBlank() &&
            normalize(previous.previousEndState) != normalize(next.nextStartState)
        ) {
            issues += "Hareket devamlılığı açıklamaları birbiriyle uyuşmuyor."
        }

        if (character != null) {
            if (character.wardrobe.isBlank()) issues += "Karakter kıyafeti hafızada tanımlanmamış."
            if (character.hair.isBlank()) issues += "Karakter saç görünümü hafızada tanımlanmamış."
        }

        return ContinuityReport(passed = issues.isEmpty(), issues = issues)
    }

    private fun compare(label: String, first: String, second: String, issues: MutableList<String>) {
        if (first.isNotBlank() && second.isNotBlank() &&
            normalize(first) != normalize(second)
        ) {
            issues += "$label bilgisi sahneler arasında değişiyor."
        }
    }

    private fun normalize(value: String): String =
        value.trim().lowercase().replace(Regex("\\s+"), " ")
}
