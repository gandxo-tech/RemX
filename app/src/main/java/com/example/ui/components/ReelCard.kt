package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import com.example.data.Reel

@Composable
fun UnifiedReelCard(
    reel: Reel,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isMini: Boolean = false
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isMini) MaterialTheme.colorScheme.surfaceContainerHigh else MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isMini) 3.dp else 4.dp),
        modifier = modifier
            .then(if (isMini) Modifier.width(140.dp) else Modifier.fillMaxWidth())
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = if (isMini) Modifier else Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(9f / 16f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (reel.thumbnailUrl.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = reel.thumbnailUrl,
                        contentDescription = "Miniature vidéo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier
                                    .align(Alignment.Center)
                                    .size(20.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                strokeWidth = 2.dp
                            )
                        },
                        error = {
                            Icon(
                                imageVector = Icons.Filled.Error,
                                contentDescription = "Erreur",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(if (isMini) 24.dp else 32.dp)
                            )
                        }
                    )
                } else {
                    Icon(
                        imageVector = if (reel.status == "error") Icons.Filled.Error else Icons.Filled.HourglassEmpty,
                        contentDescription = "En attente",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(if (isMini) 24.dp else 32.dp)
                    )
                }

                if (isMini) {
                    Surface(
                        shape = CircleShape,
                        color = Color.Black.copy(alpha = 0.5f),
                        modifier = Modifier.size(32.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(if (isMini) 8.dp else 12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (reel.author.isNotEmpty()) {
                    Text(
                        text = "@${reel.author}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (!isMini) {
                    if (reel.status != "ready" && reel.status != "done") {
                        ReelAnalysisProgressIndicator(
                            status = reel.status,
                            isCompact = true
                        )
                    }
                }

                Text(
                    text = reel.caption.takeIf { it.isNotEmpty() } ?: if (reel.status == "ready" || reel.status == "done") "Sans description" else "Analyse en cours...",
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = if (isMini) FontWeight.Normal else FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (!isMini && reel.themes.isNotEmpty()) {
                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        reel.themes.take(2).forEach { theme ->
                            SuggestionChip(
                                onClick = { },
                                label = { Text(theme, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp) },
                                modifier = Modifier.height(24.dp),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
