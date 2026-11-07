package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.EmeraldSecondary
import com.example.ui.viewmodel.AuthUiState
import com.example.ui.viewmodel.AuthViewModel

@Composable
fun SignInScreen(
  viewModel: AuthViewModel,
  uiState: AuthUiState
) {
  val context = LocalContext.current
  var emailInput by remember { mutableStateOf("") }
  var passwordInput by remember { mutableStateOf("") }
  var showEmailDialog by remember { mutableStateOf(false) }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(24.dp)
      .testTag("sign_in_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(20.dp),
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .verticalScroll(rememberScrollState())
    ) {
      // App Icon & Branding
      Surface(
        modifier = Modifier.size(100.dp),
        shape = CircleShape,
        color = EmeraldSecondary,
        shadowElevation = 6.dp
      ) {
        Box(contentAlignment = Alignment.Center) {
          Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "Chats Message Pk Logo",
            modifier = Modifier.size(72.dp)
          )
        }
      }

      Text(
        text = "Chats Message Pk",
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onBackground
        )
      )

      Text(
        text = "Simple. Secure. Reliable Messaging & Calling.\nEmail-based identity with unique User Codes.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))

      // Google Sign-In (Official Provisioned Identity Flow)
      Button(
        onClick = { viewModel.signInWithGoogle(context) },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("google_sign_in_button"),
        colors = ButtonDefaults.buttonColors(
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = RoundedCornerShape(26.dp),
        enabled = !uiState.isLoading
      ) {
        if (uiState.isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            color = MaterialTheme.colorScheme.onPrimary,
            strokeWidth = 2.dp
          )
        } else {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Icon(Icons.Default.Security, contentDescription = null)
            Text(
              text = "Sign in with Google",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
            )
          }
        }
      }

      // Security Badges
      Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
          containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(16.dp)
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = EmeraldPrimary,
            modifier = Modifier.size(28.dp)
          )
          Column {
            Text(
              text = "End-to-End Encrypted Cloud",
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Protected with user code discovery. No phone number required.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      if (uiState.errorMessage != null) {
        Card(
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
          shape = RoundedCornerShape(12.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = uiState.errorMessage,
            color = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.padding(12.dp),
            style = MaterialTheme.typography.bodySmall
          )
        }
      }
    }
  }
}

@Composable
fun ProfileSetupScreen(
  viewModel: AuthViewModel,
  uiState: AuthUiState
) {
  val initialName = uiState.userProfile?.displayName?.ifBlank { "Ahmed Khan" } ?: "Ahmed Khan"
  val initialEmail = uiState.userProfile?.email ?: ""
  var displayName by remember { mutableStateOf(initialName) }
  var username by remember { mutableStateOf("ahmedkhan") }
  var userCode by remember { mutableStateOf(viewModel.generateRandomUserCode()) }
  var about by remember { mutableStateOf("Hey there! I am using Chats Message Pk.") }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(24.dp)
      .testTag("profile_setup_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .widthIn(max = 480.dp)
        .verticalScroll(rememberScrollState()),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Text(
        text = "Profile Registration",
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )

      Text(
        text = "Please set up your public profile and unique User Code for Chats Message Pk.",
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Surface(
        modifier = Modifier.size(90.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            Icons.Default.Person,
            contentDescription = null,
            modifier = Modifier.size(54.dp),
            tint = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      OutlinedTextField(
        value = displayName,
        onValueChange = { displayName = it },
        label = { Text("Full Name") },
        modifier = Modifier.fillMaxWidth().testTag("name_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = username,
        onValueChange = { username = it.filter { ch -> ch.isLetterOrDigit() || ch == '_' } },
        label = { Text("Username (without @)") },
        prefix = { Text("@") },
        modifier = Modifier.fillMaxWidth().testTag("username_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      // Unique User Code (e.g. AF729184 or PK...)
      OutlinedTextField(
        value = userCode,
        onValueChange = { userCode = it.uppercase() },
        label = { Text("Unique User Code") },
        trailingIcon = {
          IconButton(onClick = { userCode = viewModel.generateRandomUserCode() }) {
            Icon(Icons.Default.Autorenew, contentDescription = "Generate new code")
          }
        },
        modifier = Modifier.fillMaxWidth().testTag("user_code_input"),
        singleLine = true,
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = about,
        onValueChange = { about = it },
        label = { Text("About Status") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 3,
        shape = RoundedCornerShape(12.dp)
      )

      if (uiState.errorMessage != null) {
        Text(
          text = uiState.errorMessage,
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall
        )
      }

      Button(
        onClick = {
          viewModel.completeProfile(
            displayName = displayName,
            username = username,
            userCode = userCode,
            about = about
          )
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .testTag("complete_profile_button"),
        shape = RoundedCornerShape(26.dp),
        enabled = !uiState.isLoading && displayName.isNotBlank() && username.isNotBlank() && userCode.isNotBlank()
      ) {
        if (uiState.isLoading) {
          CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
        } else {
          Text("Complete Registration", style = MaterialTheme.typography.titleMedium)
        }
      }
    }
  }
}
