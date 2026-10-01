package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.ui.theme.CosmicBlack

@Composable
fun CosmicBackgroundBox(
    modifier: Modifier = Modifier,
    nebulaAlpha: Float = 0.42f,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CosmicBlack)
    ) {
        // Deep Space Nebula Backdrop (Cosmic nebula photo matching attached image)
        Image(
            painter = painterResource(id = R.drawable.cosmic_nebula_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(nebulaAlpha)
        )

        // Cosmic Black scrim gradient overlays for crystal-clear readability of white text
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.65f),
                            Color.Black.copy(alpha = 0.25f),
                            Color.Black.copy(alpha = 0.55f),
                            Color.Black.copy(alpha = 0.90f)
                        )
                    )
                )
        )

        // Screen Foreground Content
        content()
    }
}
