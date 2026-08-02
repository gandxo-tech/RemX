package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val uid: String) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val auth = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        if (auth == null) {
            _authState.value = AuthState.Error("Firebase non configuré. Veuillez ajouter votre configuration Firebase dans le panel Secrets (AI Studio).")
        } else {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                _authState.value = AuthState.Success(currentUser.uid)
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        viewModelScope.launch {
            if (auth == null) {
                _authState.value = AuthState.Error("Firebase non configuré.")
                return@launch
            }
            _authState.value = AuthState.Loading
            
            val credentialManager = CredentialManager.create(context)
            
            val webClientId = BuildConfig.WEB_CLIENT_ID
            if (webClientId.isEmpty()) {
                _authState.value = AuthState.Error("WEB_CLIENT_ID manquant. Configurez-le dans le panel Secrets (AI Studio) ou dans .env.")
                return@launch
            }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(webClientId)
                .setAutoSelectEnabled(true)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            try {
                val result = credentialManager.getCredential(
                    request = request,
                    context = context,
                )
                val credential = result.credential

                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    
                    auth?.signInWithCredential(firebaseCredential)?.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            _authState.value = AuthState.Success(user?.uid ?: "")
                        } else {
                            _authState.value = AuthState.Error(task.exception?.message ?: "Erreur d'authentification Firebase")
                        }
                    }
                } else {
                    _authState.value = AuthState.Error("Type de credential invalide")
                }
            } catch (e: GetCredentialException) {
                Log.e("Auth", "GetCredentialException", e)
                _authState.value = AuthState.Error(e.message ?: "Erreur lors de l'obtention des credentials")
            } catch (e: Exception) {
                Log.e("Auth", "Exception", e)
                _authState.value = AuthState.Error(e.message ?: "Une erreur inconnue est survenue")
            }
        }
    }
    
    fun signOut() {
        auth?.signOut()
        _authState.value = AuthState.Idle
    }
}
