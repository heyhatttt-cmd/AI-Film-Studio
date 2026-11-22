package com.aifilmstudio

import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import androidx.compose.runtime.DisposableEffect
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF101322)
private val Panel = Color(0xFF1B2033)
private val Violet = Color(0xFF8B75FF)
private val Muted = Color(0xFFB8BED0)

private object StudioScreen {
    const val HOME = "home"
    const val PROJECT = "project"
    const val SCRIPT = "script"
    const val CHARACTER = "character"
    const val VOICE = "voice"
    const val VIDEO = "video"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = Ink) {
                    FilmStudioHome()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmStudioHome() {
    val context = LocalContext.current
    val preferences = remember { context.getSharedPreferences("ai_film_project", 0) }
    var screen by remember { mutableStateOf(StudioScreen.HOME) }
    var projectName by remember { mutableStateOf(preferences.getString("name", "") ?: "") }
    var projectIdea by remember { mutableStateOf(preferences.getString("idea", "") ?: "") }
    var script by remember { mutableStateOf(preferences.getString("script", "") ?: "") }
    var characterName by remember { mutableStateOf("") }
    var face by remember { mutableStateOf("") }
    var hair by remember { mutableStateOf("") }
    var wardrobe by remember { mutableStateOf("") }
    var accessories by remember { mutableStateOf("") }
    var voiceDescription by remember { mutableStateOf("") }
    var voiceText by remember { mutableStateOf("") }
    var sceneLocation by remember { mutableStateOf("") }
    var sceneLighting by remember { mutableStateOf("") }
    var sceneProps by remember { mutableStateOf("") }
    var previousEnd by remember { mutableStateOf("") }
    var nextStart by remember { mutableStateOf("") }
    var feedback by remember { mutableStateOf("") }
    var geminiApiKey by remember { mutableStateOf("") }
    var aiBusy by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var ttsReady by remember { mutableStateOf(false) }
    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }

    DisposableEffect(context) {
        val engine = TextToSpeech(context) { status ->
            ttsReady = status == TextToSpeech.SUCCESS &&
                (ttsEngine?.isLanguageAvailable(Locale("tr", "TR")) ?: TextToSpeech.LANG_NOT_SUPPORTED) >= TextToSpeech.LANG_AVAILABLE
            if (status == TextToSpeech.SUCCESS) {
                ttsEngine?.language = Locale("tr", "TR")
            }
        }
        ttsEngine = engine
        onDispose {
            engine.stop()
            engine.shutdown()
            ttsEngine = null
            ttsReady = false
        }
    }

    Scaffold(
        containerColor = Ink,
        topBar = {
            TopAppBar(
                navigationIcon = {
                    if (screen != StudioScreen.HOME) {
                        TextButton(onClick = { screen = StudioScreen.HOME }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Geri", tint = Color.White)
                        }
                    }
                },
                title = {
                    Column {
                        Text("AI FILM STUDIO", fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                        Text(screenTitle(screen), color = Muted, fontSize = 12.sp)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Ink,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (screen) {
                StudioScreen.HOME -> {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(26.dp),
                            colors = CardDefaults.cardColors(containerColor = Panel)
                        ) {
                            Box(
                                modifier = Modifier.fillMaxWidth().background(
                                    Brush.linearGradient(listOf(Color(0xFF352A67), Color(0xFF202B49), Panel))
                                ).padding(22.dp)
                            ) {
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Violet)
                                        Spacer(Modifier.width(8.dp))
                                        Text("YARATICI STÜDYO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                    Spacer(Modifier.height(18.dp))
                                    Text("Bir fikrin mi var?", color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)
                                    Text(
                                        "Proje bilgilerini gir, senaryonu ve karakterlerini hazırla.",
                                        color = Muted, fontSize = 14.sp,
                                        modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
                                    )
                                    Button(
                                        onClick = { screen = StudioScreen.PROJECT; feedback = "" },
                                        colors = ButtonDefaults.buttonColors(containerColor = Violet),
                                        shape = RoundedCornerShape(14.dp)
                                    ) {
                                        Icon(Icons.Default.Movie, contentDescription = null)
                                        Spacer(Modifier.width(8.dp))
                                        Text("Yeni film projesi")
                                    }
                                }
                            }
                        }
                    }
                    item { SectionLabel("ÜRETİM ARAÇLARI") }
                    item {
                        ToolCard({ Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Violet) },
                            "Senaryo oluştur", "Fikrini sahnelere ve diyaloglara dönüştür.") { screen = StudioScreen.SCRIPT }
                    }
                    item {
                        ToolCard({ Icon(Icons.Default.Palette, contentDescription = null, tint = Violet) },
                            "Karakter ve sahne tasarla", "Karakter bilgilerini kaydet ve sahne devamlılığını kontrol et.") { screen = StudioScreen.CHARACTER }
                    }
                    item {
                        ToolCard({ Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Violet) },
                            "Seslendirme", "Diyalog metnini hazırla ve seslendirme metni olarak sakla.") { screen = StudioScreen.VOICE }
                    }
                    item {
                        ToolCard({ Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Violet) },
                            "Video stüdyosu", "Sahne bilgilerini düzenle ve devamlılık denetimi yap.") { screen = StudioScreen.VIDEO }
                    }
                    item {
                        InfoCard("Bağlı servisler", "Gemini senaryo üretimi API anahtarı girildiğinde çalışır. Türkçe metin-ses telefonundaki Android TTS motoruyla çalışır. Video üretimi için ücretsiz ve güvenilir bir kota henüz doğrulanmadı.")
                    }
                }
                StudioScreen.PROJECT -> {
                    item { SectionLabel("YENİ FİLM PROJESİ") }
                    item { StudioField("Proje adı", projectName, { projectName = it }, "Örn. Kayıp Şehir") }
                    item { StudioField("Film fikri", projectIdea, { projectIdea = it }, "Filminin konusunu birkaç cümleyle anlat.", 4) }
                    item {
                        PrimaryAction("Projeyi kaydet") {
                            preferences.edit().putString("name", projectName).putString("idea", projectIdea).apply()
                            feedback = "Proje taslağı bu cihaza kaydedildi."
                        }
                    }
                    item { FeedbackText(feedback) }
                }
                StudioScreen.SCRIPT -> {
                    item { SectionLabel("SENARYO ATÖLYESİ") }
                    item { StudioField("Hikâye fikri", projectIdea, { projectIdea = it }, "Hikâyenin ana fikrini yaz.", 4) }
                    item { StudioField("Senaryo / sahne metni", script, { script = it }, "Sahne başlığı, açıklama ve diyaloglarını buraya yaz.", 7) }
                    item { StudioField("Gemini API anahtarı (bu oturumda kullanılır)", geminiApiKey, { geminiApiKey = it }, "Google AI Studio anahtarını buraya yapıştır.") }
                    item {
                        TextButton(onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://aistudio.google.com/apikey")))
                        }) { Text("Ücretsiz Gemini API anahtarı al") }
                    }
                    item {
                        PrimaryAction(if (aiBusy) "Senaryo hazırlanıyor..." else "Gemini ile senaryo oluştur") {
                            if (geminiApiKey.isBlank() || projectIdea.isBlank()) {
                                feedback = "Önce hikâye fikrini ve Gemini API anahtarını gir."
                            } else {
                                aiBusy = true
                                feedback = "Gemini'ye bağlanılıyor..."
                                scope.launch {
                                    val result = runCatching {
                                        GeminiTextService.generateScript(geminiApiKey.trim(), projectIdea)
                                    }
                                    aiBusy = false
                                    result.onSuccess {
                                        script = it
                                        preferences.edit().putString("idea", projectIdea).putString("script", it).apply()
                                        feedback = "Gemini senaryoyu oluşturdu ve taslağa kaydetti."
                                    }.onFailure {
                                        feedback = "Bağlantı hatası: ${it.message ?: "İstek tamamlanamadı"}"
                                    }
                                }
                            }
                        }
                    }
                    item {
                        PrimaryAction("Senaryoyu kaydet") {
                            preferences.edit().putString("idea", projectIdea).putString("script", script).apply()
                            feedback = "Senaryo taslağı kaydedildi."
                        }
                    }
                    item { FeedbackText(feedback) }
                    item { InfoCard("Ücretsiz kullanım notu", "Gemini ücretsiz kotası ve erişilebilir model durumuna bağlıdır. Bu uygulama ücretli servise otomatik geçmez. Google hesabında faturalandırma açıksa kota aşımında ücret doğabilir. API anahtarı bu ekranda tutulur, uygulama tarafından kalıcı kaydedilmez. Ücretsiz katmanda gönderdiğin içerikler Google tarafından ürün geliştirmede kullanılabilir.") }
                }
                StudioScreen.CHARACTER -> {
                    item { SectionLabel("KARAKTER HAFIZASI") }
                    item { StudioField("Karakter adı", characterName, { characterName = it }, "Karakterin adı") }
                    item { StudioField("Yüz / fiziksel görünüm", face, { face = it }, "Yüz şekli, yaş görünümü ve belirgin özellikler", 3) }
                    item { StudioField("Saç", hair, { hair = it }, "Saç rengi ve modeli") }
                    item { StudioField("Kıyafet", wardrobe, { wardrobe = it }, "Sabit kalması gereken kıyafet") }
                    item { StudioField("Aksesuarlar", accessories, { accessories = it }, "Gözlük, kolye, çanta vb.") }
                    item { StudioField("Ses ve konuşma tarzı", voiceDescription, { voiceDescription = it }, "Örn. sakin, kalın tonlu, yavaş konuşur", 2) }
                    item {
                        PrimaryAction("Karakter hafızasına kaydet") {
                            if (characterName.isBlank()) {
                                feedback = "Kaydetmek için karakter adı gir."
                            } else {
                                val id = "main_character"
                                CharacterMemoryStore(context).saveCharacter(
                                    CharacterProfile(id, characterName, face, hair, wardrobe, accessories, voiceDescription)
                                )
                                feedback = "Karakter profili cihaz hafızasına kaydedildi."
                            }
                        }
                    }
                    item { FeedbackText(feedback) }
                    item { InfoCard("Devamlılık", "Kaydedilen açıklamalar sonraki sahnelerde referans olarak kullanılabilir. Bu kayıt yüzü biyometrik olarak eşleştirmez.") }
                }
                StudioScreen.VOICE -> {
                    item { SectionLabel("SES METNİ HAZIRLAMA") }
                    item { StudioField("Seslendirme / diyalog metni", voiceText, { voiceText = it }, "Karakterin söyleyeceği metni yaz.", 6) }
                    item { StudioField("Karakterin ses tarifi", voiceDescription, { voiceDescription = it }, "Ses tonu ve konuşma biçimi", 2) }
                    item {
                        PrimaryAction("Metni Türkçe seslendir") {
                            val engine = ttsEngine
                            if (voiceText.isBlank()) {
                                feedback = "Önce seslendirilecek metni yaz."
                            } else if (!ttsReady || engine == null) {
                                feedback = "Türkçe ses motoru hazır değil. Telefon ayarlarından Türkçe TTS ses verisini yükle."
                            } else {
                                val result = engine.speak(voiceText, TextToSpeech.QUEUE_FLUSH, null, "aifilm_voice")
                                feedback = if (result == TextToSpeech.SUCCESS) "Telefonun Türkçe ses motoru konuşmayı başlattı." else "Seslendirme başlatılamadı."
                            }
                        }
                    }
                    item {
                        PrimaryAction("Ses metnini kaydet") {
                            preferences.edit().putString("voice_text", voiceText).putString("voice_description", voiceDescription).apply()
                            feedback = "Seslendirme metni kaydedildi."
                        }
                    }
                    item { FeedbackText(feedback) }
                    item { InfoCard("Servis durumu", if (ttsReady) "Android Türkçe metin-ses motoru hazır. Seslendirme cihazındaki yüklü sese bağlıdır." else "Türkçe TTS motoru bulunamadı veya henüz hazır değil. Telefon ayarlarından Metin okuma / Text-to-speech bölümünde Türkçe ses verisi yükle.") }
                    item { InfoCard("Sınır", "Bu özellik metni telefona okutur; MP3/WAV dosyası dışa aktarmaz ve dudak senkronizasyonu üretmez.") }
                }
                StudioScreen.VIDEO -> {
                    item { SectionLabel("SAHNE VE DEVAMLILIK") }
                    item { StudioField("Mekân", sceneLocation, { sceneLocation = it }, "Örn. yağmurlu bir sokak") }
                    item { StudioField("Işık", sceneLighting, { sceneLighting = it }, "Örn. gece, soğuk mavi ışık") }
                    item { StudioField("Nesneler / aksesuar konumu", sceneProps, { sceneProps = it }, "Sahnede görünen önemli nesneler", 2) }
                    item { StudioField("Önceki sahnenin bitiş hareketi", previousEnd, { previousEnd = it }, "Örn. karakter kapıya doğru yürümeyi bitirir", 2) }
                    item { StudioField("Bu sahnenin başlangıç hareketi", nextStart, { nextStart = it }, "Örn. karakter kapının önünde durur", 2) }
                    item {
                        PrimaryAction("Devamlılığı denetle") {
                            val profile = CharacterMemoryStore(context).getCharacter("main_character")
                            val previous = SceneContinuity("previous", "main_character", sceneLocation, sceneLighting, sceneProps, previousEnd, "")
                            val next = SceneContinuity("next", "main_character", sceneLocation, sceneLighting, sceneProps, "", nextStart)
                            val report = SceneContinuityChecker.check(profile, previous, next)
                            feedback = if (report.passed) {
                                "Metin tabanlı kontrol: belirgin bir uyumsuzluk bulunmadı."
                            } else {
                                "Kontrol uyarıları: " + report.issues.joinToString(" ")
                            }
                        }
                    }
                    item { FeedbackText(feedback) }
                    item { InfoCard("Video üretimi", "Bu sürüm sahne notlarını ve metin tabanlı devamlılığı yönetir. Gerçek video oluşturma, sahne birleştirme ve MP4 dışa aktarma henüz bağlı değildir.") }
                }
            }
        }
    }
}

private fun screenTitle(screen: String): String = when (screen) {
    StudioScreen.PROJECT -> "Yeni film projesi"
    StudioScreen.SCRIPT -> "Senaryo oluştur"
    StudioScreen.CHARACTER -> "Karakter ve sahne tasarımı"
    StudioScreen.VOICE -> "Seslendirme"
    StudioScreen.VIDEO -> "Video stüdyosu"
    else -> "Hayalindeki filmi oluştur"
}

@Composable
private fun SectionLabel(value: String) {
    Text(value, color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
}

@Composable
private fun StudioField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 1
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Muted) },
            minLines = minLines,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
private fun PrimaryAction(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(containerColor = Violet),
        shape = RoundedCornerShape(14.dp)
    ) { Text(label, color = Color.White, fontWeight = FontWeight.Bold) }
}

@Composable
private fun FeedbackText(value: String) {
    if (value.isNotBlank()) {
        Text(value, color = Color(0xFFC8F0D0), fontSize = 13.sp)
    }
}

@Composable
private fun InfoCard(title: String, message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF29243E)), shape = RoundedCornerShape(18.dp)) {
        Column(Modifier.padding(18.dp)) {
            Text(title, color = Color.White, fontWeight = FontWeight.Bold)
            Text(message, color = Muted, fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

@Composable
private fun ToolCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.background(Color(0xFF302B50), RoundedCornerShape(14.dp)).padding(12.dp)) { icon() }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, color = Muted, fontSize = 13.sp)
            }
            Text("›", color = Violet, fontSize = 28.sp)
        }
    }
}
