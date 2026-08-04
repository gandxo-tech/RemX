package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun MinimalActivityChart(
    data: List<Int>,
    modifier: Modifier = Modifier
) {
    var isLoaded by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        delay(100)
        isLoaded = true
    }

    val maxVal = (data.maxOrNull() ?: 1).coerceAtLeast(1)
    val colorPrimary = MaterialTheme.colorScheme.primary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .padding(16.dp)
    ) {
        Text(
            text = "Activité des sauvegardes",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom
        ) {
            data.forEach { value ->
                val targetHeight = if (isLoaded) (value.toFloat() / maxVal) else 0f
                val animatedHeight by animateFloatAsState(
                    targetValue = targetHeight,
                    animationSpec = tween(durationMillis = 800),
                    label = "barHeight"
                )
                
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .fillMaxHeight(animatedHeight.coerceAtLeast(0.05f))
                    ) {
                        drawRoundRect(
                            color = colorPrimary,
                            size = Size(size.width, size.height),
                            cornerRadius = CornerRadius(4.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
