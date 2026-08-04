package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class ReelAnalysisStage(val step: Int, val totalSteps: Int, val label: String, val description: String) {
    UPLOADED(1, 4, "Reçu", "Reel téléversé et enregistré en mémoire"),
    PROCESSING(2, 4, "Traitement", "Analyse visuelle et des métadonnées en cours..."),
    TRANSCRIBING(3, 4, "Transcription", "Extraction de l'audio et paroles en arrière-plan..."),
    COMPLETED(4, 4, "Terminé", "Analyse terminée et indexée pour la recherche RAG"),
    ERROR(-1, 4, "Erreur", "Une erreur s'est produite lors de l'analyse");

    companion object {
        fun fromStatus(status: String): ReelAnalysisStage {
            return when (status.lowercase()) {
                "uploaded", "pending" -> UPLOADED
                "processing", "analyzing", "in_progress" -> PROCESSING
                "transcribing", "audio_processing", "transcription" -> TRANSCRIBING
                "ready", "done", "completed" -> COMPLETED
                "error", "failed" -> ERROR
                else -> UPLOADED
            }
        }
    }
}

@Composable
fun ReelAnalysisProgressIndicator(
    status: String,
    modifier: Modifier = Modifier,
    isCompact: Boolean = false
) {
    val currentStage = ReelAnalysisStage.fromStatus(status)

    if (isCompact) {
        CompactProgressBadge(currentStage = currentStage, modifier = modifier)
    } else {
        DetailedProgressCard(currentStage = currentStage, modifier = modifier)
    }
}

@Composable
fun CompactProgressBadge(
    currentStage: ReelAnalysisStage,
    modifier: Modifier = Modifier
) {
    val chipColor = when (currentStage) {
        ReelAnalysisStage.UPLOADED -> MaterialTheme.colorScheme.tertiaryContainer
        ReelAnalysisStage.PROCESSING -> MaterialTheme.colorScheme.secondaryContainer
        ReelAnalysisStage.TRANSCRIBING -> MaterialTheme.colorScheme.secondaryContainer
        ReelAnalysisStage.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
        ReelAnalysisStage.ERROR -> MaterialTheme.colorScheme.errorContainer
    }

    val textColor = when (currentStage) {
        ReelAnalysisStage.UPLOADED -> MaterialTheme.colorScheme.onTertiaryContainer
        ReelAnalysisStage.PROCESSING -> MaterialTheme.colorScheme.onSecondaryContainer
        ReelAnalysisStage.TRANSCRIBING -> MaterialTheme.colorScheme.onSecondaryContainer
        ReelAnalysisStage.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
        ReelAnalysisStage.ERROR -> MaterialTheme.colorScheme.onErrorContainer
    }

    Surface(
        color = chipColor,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (currentStage == ReelAnalysisStage.PROCESSING || currentStage == ReelAnalysisStage.UPLOADED || currentStage == ReelAnalysisStage.TRANSCRIBING) {
                CircularProgressIndicator(
                    modifier = Modifier.size(12.dp),
                    strokeWidth = 2.dp,
                    color = textColor
                )
            } else if (currentStage == ReelAnalysisStage.COMPLETED) {
                Icon(
                    imageVector = Icons.Filled.TaskAlt,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Error,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(12.dp)
                )
            }

            Text(
                text = "${currentStage.label} (${currentStage.step}/4)",
                style = MaterialTheme.typography.labelSmall,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
        }
    }
}

@Composable
fun DetailedProgressCard(
    currentStage: ReelAnalysisStage,
    modifier: Modifier = Modifier
) {
    val progressAnim by animateFloatAsState(
        targetValue = when (currentStage) {
            ReelAnalysisStage.UPLOADED -> 0.25f
            ReelAnalysisStage.PROCESSING -> 0.50f
            ReelAnalysisStage.TRANSCRIBING -> 0.75f
            ReelAnalysisStage.COMPLETED -> 1.0f
            ReelAnalysisStage.ERROR -> 0.0f
        },
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "progress"
    )

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Statut de l'analyse",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (currentStage) {
                                ReelAnalysisStage.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
                                ReelAnalysisStage.ERROR -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.secondaryContainer
                            }
                        ) {
                            Text(
                                text = "${currentStage.label} • ${currentStage.step}/4",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when (currentStage) {
                                    ReelAnalysisStage.COMPLETED -> MaterialTheme.colorScheme.onPrimaryContainer
                                    ReelAnalysisStage.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                                    else -> MaterialTheme.colorScheme.onSecondaryContainer
                                },
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 10.sp
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = currentStage.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (currentStage == ReelAnalysisStage.PROCESSING || currentStage == ReelAnalysisStage.UPLOADED || currentStage == ReelAnalysisStage.TRANSCRIBING) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.5.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            LinearProgressIndicator(
                progress = { progressAnim },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = if (currentStage == ReelAnalysisStage.ERROR) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            // 4-stage Step Indicators
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StageStepItem(
                    stepNumber = 1,
                    title = "Reçu",
                    icon = Icons.Filled.CloudDone,
                    isCompleted = currentStage.step > 1,
                    isActive = currentStage.step == 1
                )

                DividerLine(isCompleted = currentStage.step > 1, modifier = Modifier.weight(1f))

                StageStepItem(
                    stepNumber = 2,
                    title = "Traitement",
                    icon = Icons.Filled.Psychology,
                    isCompleted = currentStage.step > 2,
                    isActive = currentStage.step == 2
                )

                DividerLine(isCompleted = currentStage.step > 2, modifier = Modifier.weight(1f))

                StageStepItem(
                    stepNumber = 3,
                    title = "Transcription",
                    icon = Icons.Filled.HourglassEmpty,
                    isCompleted = currentStage.step > 3,
                    isActive = currentStage.step == 3
                )

                DividerLine(isCompleted = currentStage.step > 3, modifier = Modifier.weight(1f))

                StageStepItem(
                    stepNumber = 4,
                    title = "Terminé",
                    icon = Icons.Filled.TaskAlt,
                    isCompleted = currentStage.step >= 4,
                    isActive = currentStage.step == 4
                )
            }
        }
    }
}

@Composable
private fun StageStepItem(
    stepNumber: Int,
    title: String,
    icon: ImageVector,
    isCompleted: Boolean,
    isActive: Boolean
) {
    val backgroundColor by animateColorAsState(
        targetValue = when {
            isCompleted -> MaterialTheme.colorScheme.primary
            isActive -> MaterialTheme.colorScheme.primaryContainer
            else -> MaterialTheme.colorScheme.surfaceVariant
        },
        label = "stepBg"
    )

    val contentColor by animateColorAsState(
        targetValue = when {
            isCompleted -> Color.White
            isActive -> MaterialTheme.colorScheme.onPrimaryContainer
            else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        },
        label = "stepContent"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(backgroundColor)
                .then(
                    if (isActive) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isActive || isCompleted) FontWeight.Bold else FontWeight.Normal,
            fontSize = 11.sp,
            color = if (isActive || isCompleted) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun DividerLine(
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    val lineColor by animateColorAsState(
        targetValue = if (isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        label = "lineColor"
    )

    Box(
        modifier = modifier
            .padding(start = 4.dp, end = 4.dp, bottom = 18.dp)
            .height(2.dp)
            .background(lineColor)
    )
}
