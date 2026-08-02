package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.auth.AuthState
import com.example.auth.AuthViewModel

@Composable
fun AuthScreen(
    onSignInSuccess: () -> Unit,
    authViewModel: AuthViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (val state = authState) {
            is AuthState.Idle -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Bienvenue sur RemX",
                        style = MaterialTheme.typography.headlineLarge
                    )
                    Text(
                        text = "Retrouvez facilement les Reels Instagram utiles que vous avez sauvegardés.",
                        style = MaterialTheme.typography.bodyLarge,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { authViewModel.signInWithGoogle(context) }) {
                        Text("Continuer avec Google")
                    }
                }
            }
            is AuthState.Loading -> {
                CircularProgressIndicator()
            }
            is AuthState.Success -> {
                onSignInSuccess()
            }
            is AuthState.Error -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.padding(32.dp)
                ) {
                    Text(
                        text = "Erreur : ${state.message}",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Button(onClick = { authViewModel.signInWithGoogle(context) }) {
                        Text("Réessayer avec Google")
                    }
                }
            }
        }
    }
}
