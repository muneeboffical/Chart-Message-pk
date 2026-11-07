package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CallRed
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun SettingsHomeScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onNavigate: (String) -> Unit
) {
  val context = LocalContext.current
  val profile = uiState.userProfile

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // User Profile Header Card
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth().testTag("profile_header_card")
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            modifier = Modifier.size(60.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = profile?.displayName?.take(1)?.uppercase() ?: "U",
                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )
            }
          }

          Spacer(modifier = Modifier.width(16.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = profile?.displayName ?: "User",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "@${profile?.username ?: ""}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // User Code Pill with Copy Action
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .padding(top = 4.dp)
                .clickable {
                  val code = profile?.userCode ?: ""
                  val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                  clipboard.setPrimaryClip(ClipData.newPlainText("User Code", code))
                  Toast.makeText(context, "Copied User Code: $code", Toast.LENGTH_SHORT).show()
                }
            ) {
              Surface(
                shape = RoundedCornerShape(6.dp),
                color = EmeraldPrimary.copy(alpha = 0.15f)
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Icon(Icons.Default.VpnKey, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(14.dp))
                  Text(
                    text = "Code: ${profile?.userCode ?: ""}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldPrimary
                  )
                  Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = EmeraldPrimary, modifier = Modifier.size(12.dp))
                }
              }
            }
          }
        }
      }
    }

    // Settings Menu Items
    item {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column {
          SettingsNavRow(
            icon = Icons.Default.CloudUpload,
            title = "Cloud Backup",
            subtitle = "Backup and restore chats associated with your email",
            onClick = { onNavigate("backup") }
          )
          HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)

          SettingsNavRow(
            icon = Icons.Default.Security,
            title = "Privacy",
            subtitle = "Last seen, read receipts, blocked contacts",
            onClick = { onNavigate("privacy") }
          )
          HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)

          SettingsNavRow(
            icon = Icons.Default.ManageAccounts,
            title = "Account",
            subtitle = "Security notifications, delete account",
            onClick = { onNavigate("account") }
          )
          HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)

          SettingsNavRow(
            icon = Icons.Default.Palette,
            title = "Chats & Wallpaper",
            subtitle = "Theme, chat history, export",
            onClick = { Toast.makeText(context, "Chats wallpaper & settings active", Toast.LENGTH_SHORT).show() }
          )
          HorizontalDivider(modifier = Modifier.padding(start = 56.dp), thickness = 0.5.dp)

          SettingsNavRow(
            icon = Icons.Default.Info,
            title = "About Chats Message Pk",
            subtitle = "Version 1.0 • Designed for secure messaging",
            onClick = {
              Toast.makeText(context, "Chats Message Pk • Modern Email-based Messaging", Toast.LENGTH_LONG).show()
            }
          )
        }
      }
    }
  }
}

@Composable
fun SettingsNavRow(
  icon: ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 14.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(icon, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(24.dp))
    Spacer(modifier = Modifier.width(16.dp))
    Column(modifier = Modifier.weight(1f)) {
      Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold))
      Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
  }
}

// --- CLOUD BACKUP SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudBackupScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  var autoBackupFrequency by remember { mutableStateOf("Daily") }
  var isEncryptedBackup by remember { mutableStateOf(true) }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        title = { Text("Cloud Backup") },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // Info Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(Icons.Default.CloudDone, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(36.dp))
            Column {
              Text(
                text = "Last Cloud Backup",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              val backupTimeText = if (uiState.latestBackup?.createdAt != null) {
                SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
                  .format(uiState.latestBackup.createdAt.toDate())
              } else "Never backed up"

              Text(
                text = backupTimeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          if (uiState.latestBackup != null) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Size: ${(uiState.latestBackup.backupSize / 1024.0).toInt()} KB", style = MaterialTheme.typography.bodyMedium)
              Text(text = "Chats: ${uiState.latestBackup.chatCount}", style = MaterialTheme.typography.bodyMedium)
              Text(text = "End-to-End Encrypted", color = EmeraldPrimary, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
            }
          }

          Button(
            onClick = { viewModel.triggerCloudBackup(isEncryptedBackup) },
            modifier = Modifier
              .fillMaxWidth()
              .height(50.dp)
              .testTag("backup_now_button"),
            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
            shape = RoundedCornerShape(25.dp),
            enabled = !uiState.isLoading
          ) {
            if (uiState.isLoading) {
              CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
            } else {
              Icon(Icons.Default.Backup, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Back Up Now", style = MaterialTheme.typography.titleMedium)
            }
          }
        }
      }

      // Settings
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
          Text(text = "Backup Settings", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("End-to-End Encrypted Backup", style = MaterialTheme.typography.bodyLarge)
              Text("Protect cloud backup with encryption", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = isEncryptedBackup,
              onCheckedChange = { isEncryptedBackup = it }
            )
          }

          HorizontalDivider(thickness = 0.5.dp)

          Column {
            Text("Auto-Backup Frequency", style = MaterialTheme.typography.bodyLarge)
            Row(
              modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              listOf("Daily", "Weekly", "Monthly", "Manual").forEach { freq ->
                FilterChip(
                  selected = autoBackupFrequency == freq,
                  onClick = { autoBackupFrequency = freq },
                  label = { Text(freq) }
                )
              }
            }
          }

          HorizontalDivider(thickness = 0.5.dp)

          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Column {
              Text("Associated Email Account", style = MaterialTheme.typography.bodyLarge)
              Text(uiState.userProfile?.email ?: "", style = MaterialTheme.typography.bodySmall, color = EmeraldPrimary)
            }
          }
        }
      }
    }
  }
}

// --- PRIVACY SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  var readReceipts by remember { mutableStateOf(true) }
  var lastSeenPrivacy by remember { mutableStateOf("Everyone") }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        title = { Text("Privacy & Security") },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { padding ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Text("Who can see my personal info", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

            Column {
              Text("Last Seen & Online", style = MaterialTheme.typography.bodyLarge)
              Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                listOf("Everyone", "My Contacts", "Nobody").forEach { opt ->
                  FilterChip(
                    selected = lastSeenPrivacy == opt,
                    onClick = { lastSeenPrivacy = opt },
                    label = { Text(opt) }
                  )
                }
              }
            }

            HorizontalDivider(thickness = 0.5.dp)

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                Text("Read Receipts", style = MaterialTheme.typography.bodyLarge)
                Text("Show blue checkmarks when messages are read", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Switch(checked = readReceipts, onCheckedChange = { readReceipts = it })
            }

            HorizontalDivider(thickness = 0.5.dp)

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column {
                val blockedCount = uiState.contacts.count { it.isBlocked }
                Text("Blocked Contacts ($blockedCount)", style = MaterialTheme.typography.bodyLarge)
                Text("Manage blocked contacts", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
          }
        }
      }
    }
  }
}

// --- ACCOUNT SCREEN ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onNavigateBack: () -> Unit,
  onSignOut: () -> Unit
) {
  BackHandler { onNavigateBack() }
  val context = LocalContext.current
  var showDeleteDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        title = { Text("Account Management") },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Account Details", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Registered Email:", style = MaterialTheme.typography.bodyMedium)
            Text(uiState.userProfile?.email ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Unique User Code:", style = MaterialTheme.typography.bodyMedium)
            Text(uiState.userProfile?.userCode ?: "", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = EmeraldPrimary)
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Username:", style = MaterialTheme.typography.bodyMedium)
            Text("@${uiState.userProfile?.username ?: ""}", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold))
          }
        }
      }

      Button(
        onClick = onSignOut,
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurfaceVariant),
        shape = RoundedCornerShape(24.dp)
      ) {
        Icon(Icons.Default.ExitToApp, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Log Out")
      }

      OutlinedButton(
        onClick = { showDeleteDialog = true },
        modifier = Modifier.fillMaxWidth().height(48.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = CallRed),
        shape = RoundedCornerShape(24.dp)
      ) {
        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = CallRed)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Delete My Account", color = CallRed)
      }
    }
  }

  if (showDeleteDialog) {
    AlertDialog(
      onDismissRequest = { showDeleteDialog = false },
      title = { Text("Delete Account permanently?") },
      text = {
        Text("This will permanently delete your Chats Message Pk account, profile, contact list, and cloud backup data.")
      },
      confirmButton = {
        Button(
          onClick = {
            showDeleteDialog = false
            onSignOut()
          },
          colors = ButtonDefaults.buttonColors(containerColor = CallRed)
        ) {
          Text("Delete Permanently")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteDialog = false }) { Text("Cancel") }
      }
    )
  }
}
