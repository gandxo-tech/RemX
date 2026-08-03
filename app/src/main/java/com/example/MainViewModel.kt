package com.example

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class AppState {
    object Loading : AppState()
    object NeedsOnboarding : AppState()
    data class Ready(val user: User) : AppState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val userDao = AppDatabase.getDatabase(application).userDao()
    
    private val _appState = MutableStateFlow<AppState>(AppState.Loading)
    val appState: StateFlow<AppState> = _appState.asStateFlow()
    
    init {
        viewModelScope.launch {
            userDao.getUser().collect { user ->
                if (user == null || !user.isLoggedIn) {
                    _appState.value = AppState.NeedsOnboarding
                } else {
                    _appState.value = AppState.Ready(user)
                }
            }
        }
    }
    
    fun registerUser(name: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val existingUser = userDao.getUserSync()
            if (existingUser != null && existingUser.name.isNotBlank()) {
                if (existingUser.name.equals(name.trim(), ignoreCase = true)) {
                    onResult(false, "Ce pseudo existe déjà. Veuillez vous connecter ou choisir un autre pseudo.")
                } else {
                    userDao.insertUser(
                        User(
                            id = 1,
                            name = name.trim(),
                            passwordHash = password,
                            isLoggedIn = true
                        )
                    )
                    onResult(true, null)
                }
            } else {
                userDao.insertUser(
                    User(
                        id = 1,
                        name = name.trim(),
                        passwordHash = password,
                        isLoggedIn = true
                    )
                )
                onResult(true, null)
            }
        }
    }

    fun loginUser(name: String, password: String, onResult: (Boolean, String?) -> Unit) {
        viewModelScope.launch {
            val existingUser = userDao.getUserSync()
            if (existingUser != null) {
                if (existingUser.name.equals(name.trim(), ignoreCase = true) && existingUser.passwordHash == password) {
                    userDao.insertUser(existingUser.copy(isLoggedIn = true))
                    onResult(true, null)
                } else if (!existingUser.name.equals(name.trim(), ignoreCase = true)) {
                    onResult(false, "Ce pseudo ne correspond à aucun compte existant.")
                } else {
                    onResult(false, "Mot de passe incorrect.")
                }
            } else {
                onResult(false, "Aucun compte trouvé. Veuillez créer un compte.")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            userDao.logoutUser()
        }
    }
}

