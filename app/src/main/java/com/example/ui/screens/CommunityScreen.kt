package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.ForestGreen
import com.example.ui.theme.LeafGreen
import com.example.ui.theme.TextOnDark

import com.example.ui.components.GlassCard

@Composable
fun CommunityScreen() {
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                "Nature Circles",
                style = MaterialTheme.typography.headlineMedium,
                color = TextOnDark,
            )
            
            CommunityCard(title = "Eco Wisdom Quiz", subtitle = "Test your knowledge on renewable energy.")
            CommunityCard(title = "Local Green Currents", subtitle = "Environmental updates from your city...")
            CommunityCard(title = "Wild Glances", subtitle = "View beautiful weather phenomenons.")
        }
    }
}

@Composable
fun CommunityCard(title: String, subtitle: String) {
    GlassCard(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, color = TextOnDark)
            Spacer(modifier = Modifier.height(8.dp))
            Text(subtitle, color = TextOnDark.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
        }
    }
}
