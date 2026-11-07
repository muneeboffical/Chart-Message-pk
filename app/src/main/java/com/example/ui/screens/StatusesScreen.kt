package com.example.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.StatusUpdate
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatusesScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState
) {
  var showAddStatusDialog by remember { mutableStateOf(false) }
  val myUid = uiState.userProfile?.userId ?: ""

  val myStatus = uiState.statuses.firstOrNull { it.userId == myUid }
  val otherStatuses = uiState.statuses.filter { it.userId != myUid }

  val recentStatuses = otherStatuses.filter { !it.viewerIds.contains(myUid) }
  val viewedStatuses = otherStatuses.filter { it.viewerIds.contains(myUid) }

  Scaffold(
    floatingActionButton = {
      Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SmallFloatingActionButton(
          onClick = { showAddStatusDialog = true },
          containerColor = MaterialTheme.colorScheme.surfaceVariant,
          contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ) {
          Icon(Icons.Default.Edit, contentDescription = "Text Status")
        }
        FloatingActionButton(
          onClick = {
            viewModel.postStatus("image", "", "Photo update from Chats Message Pk", "#0B3C2D", "https://picsum.photos/400/600")
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary,
          modifier = Modifier.testTag("add_status_fab")
        ) {
          Icon(Icons.Default.CameraAlt, contentDescription = "Photo Status")
        }
      }
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
      // My Status Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clickable {
                if (myStatus != null) viewModel.viewStatus(myStatus) else showAddStatusDialog = true
              }
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box {
              Surface(
                modifier = Modifier
                  .size(54.dp)
                  .border(2.dp, if (myStatus != null) EmeraldPrimary else Color.Transparent, CircleShape),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = uiState.userProfile?.displayName?.take(1)?.uppercase() ?: "M",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                  )
                }
              }
              if (myStatus == null) {
                Surface(
                  color = EmeraldPrimary,
                  shape = CircleShape,
                  modifier = Modifier
                    .size(20.dp)
                    .align(Alignment.BottomEnd)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                }
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "My Status",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = if (myStatus != null) {
                  "Tap to view your active status (${myStatus.viewerIds.size} views)"
                } else {
                  "Tap to add status update"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Recent Updates Section
      if (recentStatuses.isNotEmpty()) {
        item {
          Text(
            text = "Recent updates",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        items(recentStatuses, key = { it.id }) { status ->
          StatusItemRow(status = status, isViewed = false, onClick = { viewModel.viewStatus(status) })
        }
      }

      // Viewed Updates Section
      if (viewedStatuses.isNotEmpty()) {
        item {
          Text(
            text = "Viewed updates",
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        items(viewedStatuses, key = { it.id }) { status ->
          StatusItemRow(status = status, isViewed = true, onClick = { viewModel.viewStatus(status) })
        }
      }

      if (otherStatuses.isEmpty()) {
        item {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 32.dp),
            contentAlignment = Alignment.Center
          ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Icon(Icons.Default.Update, contentDescription = null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
              Text("No contact updates right now", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              Text("Statuses disappear automatically after 24 hours.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
            }
          }
        }
      }
    }
  }

  // Add Text Status Dialog
  if (showAddStatusDialog) {
    var statusText by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#0B3C2D") }

    AlertDialog(
      onDismissRequest = { showAddStatusDialog = false },
      title = { Text("New Text Status") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          OutlinedTextField(
            value = statusText,
            onValueChange = { statusText = it },
            placeholder = { Text("Type a status update...") },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
          )

          // Background Color Pickers
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            listOf("#0B3C2D", "#1E3A8A", "#6D28D9", "#BE123C", "#B45309").forEach { hex ->
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(android.graphics.Color.parseColor(hex)))
                  .clickable { selectedColor = hex }
                  .border(
                    width = if (selectedColor == hex) 3.dp else 0.dp,
                    color = Color.White,
                    shape = CircleShape
                  )
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (statusText.isNotBlank()) {
              viewModel.postStatus(
                type = "text",
                text = statusText.trim(),
                caption = "",
                bgColor = selectedColor
              )
              showAddStatusDialog = false
            }
          },
          enabled = statusText.isNotBlank()
        ) {
          Text("Post")
        }
      },
      dismissButton = {
        TextButton(onClick = { showAddStatusDialog = false }) { Text("Cancel") }
      }
    )
  }

  // Full-Screen Story Viewer
  if (uiState.viewingStatus != null) {
    val status = uiState.viewingStatus
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(status.id) {
      progress = 0f
      for (i in 1..100) {
        delay(50L) // 5 seconds story display
        progress = i / 100f
      }
      viewModel.closeStatusViewer()
    }

    Dialog(
      onDismissRequest = { viewModel.closeStatusViewer() },
      properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
      val bgColor = try {
        Color(android.graphics.Color.parseColor(status.bgColor))
      } catch (e: Exception) {
        EmeraldPrimary
      }

      Box(
        modifier = Modifier
          .fillMaxSize()
          .background(bgColor)
          .clickable { viewModel.closeStatusViewer() }
          .padding(16.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.SpaceBetween
        ) {
          // Progress bar and User Header
          Column(modifier = Modifier.padding(top = 24.dp)) {
            LinearProgressIndicator(
              progress = { progress },
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
              color = Color.White,
              trackColor = Color.White.copy(alpha = 0.3f)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                Surface(
                  modifier = Modifier.size(40.dp),
                  shape = CircleShape,
                  color = Color.White.copy(alpha = 0.25f)
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = status.userName.take(1).uppercase(),
                      color = Color.White,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
                Column {
                  Text(text = status.userName, color = Color.White, fontWeight = FontWeight.Bold)
                  Text(text = "Chats Message Pk Status", color = Color.White.copy(alpha = 0.8f), fontSize = 11.sp)
                }
              }

              IconButton(onClick = { viewModel.closeStatusViewer() }) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
              }
            }
          }

          // Center Text / Caption
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = if (status.type == "text") status.text else status.caption.ifBlank { "Photo Status" },
              style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
              ),
              modifier = Modifier.padding(24.dp)
            )
          }

          // Footer info (Viewers count)
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Visibility, contentDescription = null, tint = Color.White.copy(alpha = 0.8f), modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = "${status.viewerIds.size} views",
              color = Color.White.copy(alpha = 0.8f),
              style = MaterialTheme.typography.bodySmall
            )
          }
        }
      }
    }
  }
}

@Composable
fun StatusItemRow(
  status: StatusUpdate,
  isViewed: Boolean,
  onClick: () -> Unit
) {
  val ringColor = if (isViewed) MaterialTheme.colorScheme.outline else EmeraldPrimary

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(vertical = 6.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Surface(
      modifier = Modifier
        .size(52.dp)
        .border(2.dp, ringColor, CircleShape),
      shape = CircleShape,
      color = MaterialTheme.colorScheme.secondaryContainer
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = status.userName.take(1).uppercase(),
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSecondaryContainer
        )
      }
    }

    Spacer(modifier = Modifier.width(14.dp))

    Column {
      Text(
        text = status.userName,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
      )
      val timeText = remember(status.createdAt) {
        if (status.createdAt != null) {
          SimpleDateFormat("hh:mm a", Locale.getDefault()).format(status.createdAt.toDate())
        } else "Recent"
      }
      Text(
        text = "Today, $timeText",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
