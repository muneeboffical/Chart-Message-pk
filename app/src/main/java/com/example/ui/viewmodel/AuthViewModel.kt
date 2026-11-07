package com.example.ui.viewmodel

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.R
import com.example.data.model.UserProfile
import com.example.data.repository.ChatRepository
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import kotlin.random.Random

data class AuthUiState(
  val isLoading: Boolean = false,
  val isAuthenticated: Boolean = false,
  val userProfile: UserProfile? = null,
  val isProfileCompleted: Boolean = false,
  val errorMessage: String? = null,
  val statusMessage: String? = null
)

class AuthViewModel(private val repository: ChatRepository) : ViewModel() {

  private val auth: FirebaseAuth = Firebase.auth
  private val _uiState = MutableStateFlow(AuthUiState())
  val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

  init {
    checkCurrentAuth()
  }

  fun checkCurrentAuth() {
    val currentUser = auth.currentUser
    if (currentUser != null) {
      _uiState.value = _uiState.value.copy(isAuthenticated = true, isLoading = true)
      viewModelScope.launch {
        repository.observeUserProfile(currentUser.uid).collect { profile ->
          if (profile != null && profile.username.isNotBlank()) {
            _uiState.value = _uiState.value.copy(
              isLoading = false,
              userProfile = profile,
              isProfileCompleted = true
            )
            repository.setOnlinePresence(true)
          } else {
            // Need profile registration details (Username, User Code, Display Name)
            _uiState.value = _uiState.value.copy(
              isLoading = false,
              userProfile = profile ?: UserProfile(
                userId = currentUser.uid,
                email = currentUser.email ?: "",
                displayName = currentUser.displayName ?: "User"
              ),
              isProfileCompleted = false
            )
          }
        }
      }
    } else {
      _uiState.value = AuthUiState(isAuthenticated = false)
    }
  }

  fun signInWithGoogle(context: Context) {
    val activity = context as? Activity ?: run {
      _uiState.value = _uiState.value.copy(errorMessage = "Activity context is required for sign-in")
      return
    }

    val clientId = try {
      context.getString(R.string.default_web_client_id)
    } catch (e: Exception) {
      _uiState.value = _uiState.value.copy(
        errorMessage = "Google Sign-In configuration missing: default_web_client_id not found"
      )
      return
    }

    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
    val credentialManager = CredentialManager.create(context)
    val signInOption = GetSignInWithGoogleOption.Builder(serverClientId = clientId).build()
    val request = GetCredentialRequest.Builder().addCredentialOption(signInOption).build()

    viewModelScope.launch {
      try {
        val result = credentialManager.getCredential(activity, request)
        val credential = result.credential
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
          val googleIdToken = GoogleIdTokenCredential.createFrom(credential.data).idToken
          val authCredential = GoogleAuthProvider.getCredential(googleIdToken, null)
          auth.signInWithCredential(authCredential).await()
          checkCurrentAuth()
        } else {
          _uiState.value = _uiState.value.copy(isLoading = false, errorMessage = "Unexpected credential format")
        }
      } catch (e: GetCredentialCancellationException) {
        Log.w("Auth", "Google Sign-In flow cancelled: ${e.message}", e)
        _uiState.value = _uiState.value.copy(isLoading = false)
      } catch (e: Exception) {
        Log.e("Auth", "Google Sign-In failed", e)
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          errorMessage = e.localizedMessage ?: "Sign in failed"
        )
      }
    }
  }

  fun generateRandomUserCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    val code = (1..6)
      .map { chars[Random.nextInt(chars.length)] }
      .joinToString("")
    return "PK$code"
  }

  fun completeProfile(
    displayName: String,
    username: String,
    userCode: String,
    about: String,
    avatarUrl: String? = null
  ) {
    val currentUser = auth.currentUser ?: return
    if (displayName.isBlank() || username.isBlank() || userCode.isBlank()) {
      _uiState.value = _uiState.value.copy(errorMessage = "Please enter all required fields")
      return
    }

    _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
    viewModelScope.launch {
      val result = repository.createOrUpdateUserProfile(
        email = currentUser.email ?: "user@chatsmessagepk.com",
        displayName = displayName.trim(),
        username = username.trim().lowercase(),
        userCode = userCode.trim().uppercase(),
        avatarUrl = avatarUrl,
        about = if (about.isBlank()) "Hey there! I am using Chats Message Pk." else about.trim()
      )
      if (result.isSuccess) {
        val created = result.getOrThrow()
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          userProfile = created,
          isProfileCompleted = true,
          statusMessage = "Profile setup successfully!"
        )
      } else {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to save profile"
        )
      }
    }
  }

  fun updateAbout(newAbout: String) {
    val current = _uiState.value.userProfile ?: return
    viewModelScope.launch {
      repository.createOrUpdateUserProfile(
        email = current.email,
        displayName = current.displayName,
        username = current.username,
        userCode = current.userCode,
        avatarUrl = current.avatarUrl,
        about = newAbout
      )
    }
  }

  fun signOut(context: Context) {
    val credentialManager = CredentialManager.create(context)
    viewModelScope.launch {
      withContext(NonCancellable) {
        try {
          repository.setOnlinePresence(false)
          credentialManager.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
          Log.w("Auth", "Failed to clear credential state: ${e.message}")
        } finally {
          auth.signOut()
          _uiState.value = AuthUiState(isAuthenticated = false)
        }
      }
    }
  }

  fun deleteAccount(context: Context, onComplete: () -> Unit) {
    _uiState.value = _uiState.value.copy(isLoading = true)
    viewModelScope.launch {
      val result = repository.deleteUserAccount()
      if (result.isSuccess) {
        signOut(context)
        onComplete()
      } else {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          errorMessage = result.exceptionOrNull()?.localizedMessage ?: "Failed to delete account"
        )
      }
    }
  }

  fun clearError() {
    _uiState.value = _uiState.value.copy(errorMessage = null, statusMessage = null)
  }
}
