package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    onComplete: (String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var showWarning by remember { mutableStateOf(false) }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bienvenue sur RemX",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Ton assistant personnel de marque-pages intelligent",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Comment on t'appelle ? (Prénom ou Pseudo)") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp),
                singleLine = true
            )

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Warning, 
                        contentDescription = "Attention",
                        tint = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Tes données sont stockées uniquement sur ce téléphone. Si tu désinstalles l'application, tes sauvegardes seront perdues. Un système d'exportation est disponible dans les paramètres.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onComplete(name.trim())
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                enabled = name.isNotBlank()
            ) {
                Text("Commencer")
            }
        }
    }
}
