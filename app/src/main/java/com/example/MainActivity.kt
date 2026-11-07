package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.repository.ChatRepository
import com.example.ui.screens.*
import com.example.ui.theme.ChatsMessagePkTheme
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel
import com.example.ui.viewmodel.MainChatViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    setContent {
      ChatsMessagePkTheme {
        AppContent()
      }
    }
  }
}

@Composable
fun AppContent() {
  val context = LocalContext.current
  val repository = remember { ChatRepository(context) }
  val authViewModel = remember { AuthViewModel(repository) }
  val authUiState by authViewModel.uiState.collectAsStateWithLifecycle()

  var firebaseUser by remember { mutableStateOf(Firebase.auth.currentUser) }

  DisposableEffect(Unit) {
    val listener = FirebaseAuth.AuthStateListener { auth ->
      firebaseUser = auth.currentUser
      authViewModel.checkCurrentAuth()
    }
    Firebase.auth.addAuthStateListener(listener)
    onDispose {
      Firebase.auth.removeAuthStateListener(listener)
    }
  }

  if (firebaseUser == null || !authUiState.isAuthenticated) {
    SignInScreen(
      viewModel = authViewModel,
      uiState = authUiState
    )
  } else if (!authUiState.isProfileCompleted) {
    ProfileSetupScreen(
      viewModel = authViewModel,
      uiState = authUiState
    )
  } else {
    val uid = firebaseUser!!.uid
    key(uid) {
      MainApp(
        repository = repository,
        currentUserId = uid,
        authViewModel = authViewModel,
        authUiState = authUiState
      )
    }
  }
}

@Composable
fun MainApp(
  repository: ChatRepository,
  currentUserId: String,
  authViewModel: AuthViewModel,
  authUiState: AuthUiState
) {
  val context = LocalContext.current
  val chatViewModel = remember(currentUserId) { MainChatViewModel(repository, currentUserId) }
  val chatUiState by chatViewModel.uiState.collectAsStateWithLifecycle()
  val snackbarHostState = remember { SnackbarHostState() }

  LaunchedEffect(chatUiState.infoBanner) {
    chatUiState.infoBanner?.let { message ->
      snackbarHostState.showSnackbar(message)
      chatViewModel.clearInfoBanner()
    }
  }

  Scaffold(
    snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      // Sub-screen routing
      when (chatUiState.activeScreen) {
        "chat" -> {
          IndividualChatScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onNavigateBack = { chatViewModel.navigateBack() }
          )
        }
        "call" -> {
          CallActiveScreen(
            viewModel = chatViewModel,
            uiState = chatUiState
          )
        }
        "new_contact" -> {
          NewContactScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onNavigateBack = { chatViewModel.navigateBack() }
          )
        }
        "contact_requests" -> {
          ContactRequestsScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onNavigateBack = { chatViewModel.navigateBack() }
          )
        }
        "new_group" -> {
          GroupCreationScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onNavigateBack = { chatViewModel.navigateBack() }
          )
        }
        "new_community" -> {
          CommunityCreationScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onNavigateBack = { chatViewModel.navigateBack() }
          )
        }
        "backup" -> {
          CloudBackupScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onNavigateBack = { chatViewModel.navigateBack() }
          )
        }
        "privacy" -> {
          PrivacyScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onNavigateBack = { chatViewModel.navigateBack() }
          )
        }
        "account" -> {
          AccountScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onNavigateBack = { chatViewModel.navigateBack() },
            onSignOut = { authViewModel.signOut(context) }
          )
        }
        else -> {
          DashboardScreen(
            viewModel = chatViewModel,
            uiState = chatUiState,
            onOpenSettings = { chatViewModel.setTab(4) },
            onOpenNewContact = { chatViewModel.navigateTo("new_contact") },
            onOpenRequests = { chatViewModel.navigateTo("contact_requests") },
            onOpenNewGroup = { chatViewModel.navigateTo("new_group") },
            onOpenNewCommunity = { chatViewModel.navigateTo("new_community") }
          )
        }
      }
    }
  }
}
