package com.aifilmstudio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF101322)
private val Panel = Color(0xFF1B2033)
private val Violet = Color(0xFF8B75FF)
private val Muted = Color(0xFFB8BED0)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Ink
                ) {
                    FilmStudioHome()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmStudioHome() {
    Scaffold(
        containerColor = Ink,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("AI FILM STUDIO", fontWeight = FontWeight.Black, letterSpacing = 1.4.sp)
                        Text("Hayalindeki filmi oluştur", color = Muted, fontSize = 12.sp)
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
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(26.dp),
                    colors = CardDefaults.cardColors(containerColor = Panel)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF352A67), Color(0xFF202B49), Panel)
                                )
                            )
                            .padding(22.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Violet)
                                Spacer(Modifier.width(8.dp))
                                Text("YARATICI STÜDYO", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                            Spacer(Modifier.height(18.dp))
                            Text(
                                "Bir fikrin mi var?",
                                color = Color.White,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                "Senaryonu yaz, karakterlerini tasarla ve kendi filmini üretmeye başla.",
                                color = Muted,
                                fontSize = 14.sp,
                                lineHeight = 21.sp,
                                modifier = Modifier.padding(top = 8.dp, bottom = 18.dp)
                            )
                            Button(
                                onClick = { },
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

            item {
                Text("ÜRETİM ARAÇLARI", color = Muted, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
            }
            item {
                ToolCard(
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Violet) },
                    title = "Senaryo oluştur",
                    description = "Fikrini sahnelere ve diyaloglara dönüştür."
                )
            }
            item {
                ToolCard(
                    icon = { Icon(Icons.Default.Palette, contentDescription = null, tint = Violet) },
                    title = "Karakter ve sahne tasarla",
                    description = "Filminin dünyasını ve karakterlerini planla."
                )
            }
            item {
                ToolCard(
                    icon = { Icon(Icons.Default.RecordVoiceOver, contentDescription = null, tint = Violet) },
                    title = "Seslendirme",
                    description = "Sahnelerin için anlatım ve diyalog hazırla."
                )
            }
            item {
                ToolCard(
                    icon = { Icon(Icons.Default.VideoLibrary, contentDescription = null, tint = Violet) },
                    title = "Video stüdyosu",
                    description = "Sahnelerini düzenle ve MP4 filmine dönüştür."
                )
            }
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF29243E)),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(Modifier.padding(18.dp)) {
                        Text("İlk sürüm", color = Color.White, fontWeight = FontWeight.Bold)
                        Text(
                            "Bu APK projenin başlangıç arayüzüdür. Yapay zekâ üretimi, ses ve video işleme servisleri sonraki geliştirme adımlarında bağlanacaktır.",
                            color = Muted,
                            fontSize = 13.sp,
                            lineHeight = 19.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolCard(
    icon: @Composable () -> Unit,
    title: String,
    description: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Panel),
        shape = RoundedCornerShape(18.dp)
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .background(Color(0xFF302B50), RoundedCornerShape(14.dp))
                    .padding(12.dp)
            ) { icon() }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(description, color = Muted, fontSize = 13.sp, lineHeight = 18.sp)
            }
        }
    }
}
