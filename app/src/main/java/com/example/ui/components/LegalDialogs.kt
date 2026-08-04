package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

sealed class LegalModalType {
    object MentionsLegales : LegalModalType()
    object PolitiqueConfidentialite : LegalModalType()
    object APropos : LegalModalType()
}

@Composable
fun LegalInfoModal(
    type: LegalModalType,
    onDismiss: () -> Unit
) {
    val title = when (type) {
        LegalModalType.MentionsLegales -> "Mentions Légales"
        LegalModalType.PolitiqueConfidentialite -> "Politique de Confidentialité"
        LegalModalType.APropos -> "À Propos de RemX"
    }

    val icon: ImageVector = when (type) {
        LegalModalType.MentionsLegales -> Icons.Default.Shield
        LegalModalType.PolitiqueConfidentialite -> Icons.Default.Lock
        LegalModalType.APropos -> Icons.Default.Info
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Icon(
                                icon,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Fermer")
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Scrollable Body
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    when (type) {
                        LegalModalType.MentionsLegales -> MentionsLegalesContent()
                        LegalModalType.PolitiqueConfidentialite -> PolitiqueConfidentialiteContent()
                        LegalModalType.APropos -> AProposContent()
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

                // Footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("J'ai compris")
                    }
                }
            }
        }
    }
}

@Composable
private fun MentionsLegalesContent() {
    LegalSectionHeader("1. Éditeur & Conception")
    LegalText("RemX est une application de gestion de mémoire personnelle conçue et développée par GBAGUIDI Exaucé alias Gandxo. Développée pour simplifier la conservation et l'organisation des contenus Instagram Reels.")

    LegalSectionHeader("2. Hébergement & Données")
    LegalText("L'ensemble des données (souvenirs, favoris, historique de recherche et identifiants de compte) est exclusivement hébergé en local sur votre appareil Android grâce à une base de données SQLite sécurisée par le framework Room.")

    LegalSectionHeader("3. Propriété Intellectuelle")
    LegalText("L'ensemble des logos, interfaces, graphismes et algorithmes de l'application RemX sont protégés par le droit d'auteur. Les contenus partagés (créateurs Instagram) restent la propriété exclusive de leurs auteurs respectifs.")

    LegalSectionHeader("4. Services IA Intégrés")
    LegalText("RemX utilise l'API Gemini développée par Google pour générer des métadonnées enrichies (résumés, catégories, légendes). Aucun contenu personnel n'est conservé à des fins d'entraînement.")
}

@Composable
private fun PolitiqueConfidentialiteContent() {
    LegalSectionHeader("1. Respect de la vie privée")
    LegalText("RemX garantit la confidentialité absolue de vos données. Votre compte, vos préférences et votre historique restent scellés sur votre appareil Android.")

    LegalSectionHeader("2. Stockage dans Room DB")
    LegalText("Vos identifiants (nom, téléphone/email et mot de passe/PIN) sont chiffrés et enregistrés localement dans la table Room. Aucune transmission à un serveur tierce n'est effectuée.")

    LegalSectionHeader("3. Exportation et contrôle")
    LegalText("Vous gardez le contrôle total sur votre mémoire : vous pouvez exporter votre base de données au format JSON à tout moment ou réinitialiser votre compte en un clic.")

    LegalSectionHeader("4. Permissions requises")
    LegalText("• Accès Internet : Nécessaire exclusivement pour interroger l'API Gemini et télécharger les métadonnées de Reels.\n• Notifications : Permet de vous alerter dès qu'une analyse de Reel est finalisée.")
}

@Composable
private fun AProposContent() {
    LegalSectionHeader("RemX — Votre Second Cerveau Visuel")
    LegalText("Version 1.2.0 • Conçue par GBAGUIDI Exaucé (Gandxo) • Propulsé par Jetpack Compose & Google Gemini AI.")

    LegalText("Combien de fois avez-vous sauvegardé un Reel sur Instagram pour une recette, une astuce de voyage ou un conseil de productivité, sans jamais le retrouver au bon moment ?")

    LegalSectionHeader("Notre Mission")
    LegalText("RemX transforme votre flux de Reels en une bibliothèque intelligente, consultable instantanément et toujours disponible dans votre poche.")

    LegalSectionHeader("Fonctionnalités Clés")
    LegalText("• Partage direct depuis l'application Instagram\n• Analyse automatique des résumés par l'IA Gemini\n• Recherche textuelle et conversationnelle dans vos souvenirs\n• Mode hors-ligne et compte utilisateur sécurisé en Room")
}

@Composable
private fun LegalSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun LegalText(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
