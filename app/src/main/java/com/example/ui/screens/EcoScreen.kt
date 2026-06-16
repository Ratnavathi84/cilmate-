package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.BuildConfig
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.TextOnDark
import com.example.ui.components.GlassCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

// -- Gemini Data Classes Setup --
@JsonClass(generateAdapter = true) data class GenerateContentRequest(val contents: List<Content>, val systemInstruction: Content? = null)
@JsonClass(generateAdapter = true) data class Content(val parts: List<Part>)
@JsonClass(generateAdapter = true) data class Part(val text: String? = null)
@JsonClass(generateAdapter = true) data class GenerateContentResponse(val candidates: List<Candidate>)
@JsonClass(generateAdapter = true) data class Candidate(val content: Content)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object RetrofitClientGemini {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    val service: GeminiApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(OkHttpClient.Builder().connectTimeout(60, java.util.concurrent.TimeUnit.SECONDS).build())
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(GeminiApiService::class.java)
    }
}

@Composable
fun EcoScreen() {
    val coroutineScope = rememberCoroutineScope()
    var aiInsight by remember { mutableStateOf<String?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    val tips = listOf(
        "Plant a tree in your community to help absorb CO2.",
        "Use a reusable water bottle to reduce plastic waste.",
        "Switch to LED bulbs to save energy and forest resources.",
        "Conserve water by taking shorter showers.",
        "Buy local and organic to support eco-friendly farming."
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(id = com.example.R.drawable.nature_night_background_1781618169463),
            contentDescription = "Nature Background",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Nature & UI",
                style = MaterialTheme.typography.headlineMedium,
                color = TextOnDark,
            )
            
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Carbon Stewardship",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextOnDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Your current monthly estimated carbon footprint is 120 kg CO2. Lower than last month by 10%!",
                        color = TextOnDark.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { 0.4f },
                        modifier = Modifier.fillMaxWidth().height(8.dp),
                        color = LeafGreen,
                        trackColor = TextOnDark.copy(alpha = 0.1f),
                    )
                }
            }
            
            // Environmental Section
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text(
                        "Eco-friendly Tips",
                        style = MaterialTheme.typography.titleLarge,
                        color = TextOnDark
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    tips.forEach { tip ->
                        Row(modifier = Modifier.padding(vertical = 4.dp)) {
                            Icon(Icons.Default.Eco, contentDescription = null, tint = SoftGreen)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = tip, color = TextOnDark.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // AI Insights
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = "AI Context", tint = SoftGreen)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "AI Climate Insights",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextOnDark
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    if (isGenerating) {
                        CircularProgressIndicator(color = SoftGreen, modifier = Modifier.align(Alignment.CenterHorizontally))
                    } else if (aiInsight != null) {
                        Text(
                            text = aiInsight!!,
                            color = TextOnDark.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    } else {
                        Text(
                            "Tap to generate AI insights related to your local weather pattern and get personalized recommendations for energy usage.",
                            color = TextOnDark.copy(alpha = 0.8f),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                isGenerating = true
                                coroutineScope.launch {
                                    aiInsight = withContext(Dispatchers.IO) {
                                        try {
                                            val req = GenerateContentRequest(
                                                contents = listOf(Content(listOf(Part("Generate 2 sentences about how a sunny day is great for solar energy efficiency and saving carbon emissions.")))),
                                                systemInstruction = Content(listOf(Part("You are an eco-friendly AI assistant.")))
                                            )
                                            val res = RetrofitClientGemini.service.generateContent(BuildConfig.GEMINI_API_KEY, req)
                                            res.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: "No insight available."
                                        } catch (e: Exception) {
                                            "Could not generate insight. Please check your API key."
                                        }
                                    }
                                    isGenerating = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = LeafGreen)
                        ) {
                            Text("Get Daily Insight", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
