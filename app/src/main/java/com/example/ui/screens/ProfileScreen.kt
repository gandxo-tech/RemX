package com.example.ui.screens

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.MainViewModel
import com.example.data.UserPreferences
import com.example.ui.components.GradientButton
import com.example.ui.components.LegalInfoModal
import com.example.ui.components.LegalModalType
import com.example.ui.components.RemXHeaderDivider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = viewModel(),
    mainViewModel: MainViewModel = viewModel()
) {
    val context = LocalContext.current
    val userPreferences = remember { UserPreferences.getInstance(context) }
    val currentThemeMode by userPreferences.themeMode.collectAsState()

    val user by viewModel.user.collectAsState(initial = null)
    val username = user?.name ?: "Utilisateur RemX"
    val phoneNumber = user?.phoneNumber?.ifBlank { "N° non renseigné" } ?: "N° non renseigné"

    var activeLegalModal by remember { mutableStateOf<LegalModalType?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            viewModel.exportData(it) { success ->
                val msg = if (success) "Mémoire sauvegardée avec succès !" else "Erreur lors de la sauvegarde"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.importData(it) { success ->
                val msg = if (success) "Mémoire restaurée avec succès !" else "Erreur lors de l'importation"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = {
                        Text(
                            text = "RemX • Espace Profil & Sécurité",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                )
                RemXHeaderDivider()
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // User Header Card (WhatsApp Style)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(56.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = username.take(1).uppercase(),
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = username,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Lock,
                                contentDescription = null,
                                tint = Color(0xFF25D366),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Compte local protégé par mot de passe",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Data Safety Warning Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = "Protection des données",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Compte Room DB sécurisé. Tes identifiants et tes souvenirs restent exclusivement enregistrés sur ce téléphone.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            // Display Theme Settings
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Thème d'affichage",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (currentThemeMode == 1) "Thème Clair" else "Thème Sombre (Par défaut)",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (currentThemeMode == 1) "Mode clair actif" else "Mode sombre actif",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = currentThemeMode != 1,
                                onClick = { userPreferences.setThemeMode(0) },
                                label = { Text("Sombre") },
                                shape = RoundedCornerShape(12.dp)
                            )
                            FilterChip(
                                selected = currentThemeMode == 1,
                                onClick = { userPreferences.setThemeMode(1) },
                                label = { Text("Clair") },
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }
                }
            }

            // Backup & Restore
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Sauvegarde & Importation",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                GradientButton(
                    onClick = { exportLauncher.launch("remx_memoire_backup.json") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Download, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Sauvegarder ma mémoire (Export JSON)", color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { importLauncher.launch(arrayOf("application/json")) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Upload, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Restaurer une mémoire (Import JSON)")
                }
            }

            // Legal & About Pages Section
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Informations Légales & À Propos",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column {
                        ListItem(
                            headlineContent = { Text("Mentions Légales", fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { activeLegalModal = LegalModalType.MentionsLegales }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ListItem(
                            headlineContent = { Text("Politique de Confidentialité", fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { activeLegalModal = LegalModalType.PolitiqueConfidentialite }
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                        ListItem(
                            headlineContent = { Text("À Propos de RemX", fontWeight = FontWeight.SemiBold) },
                            leadingContent = { Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { activeLegalModal = LegalModalType.APropos }
                        )
                    }
                }
            }

            // Logout Button
            OutlinedButton(
                onClick = { mainViewModel.logout() },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Icon(Icons.Filled.Logout, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Se déconnecter", fontWeight = FontWeight.Bold)
            }

            // Developer Credit
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Application développée avec passion par GBAGUIDI Exaucé alias Gandxo ✨",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        activeLegalModal?.let { modalType ->
            LegalInfoModal(
                type = modalType,
                onDismiss = { activeLegalModal = null }
            )
        }
    }
}
