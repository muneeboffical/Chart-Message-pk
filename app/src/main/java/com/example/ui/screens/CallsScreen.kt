package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CallSession
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CallsScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onStartNewCall: () -> Unit
) {
  val myUid = uiState.userProfile?.userId ?: ""

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onStartNewCall,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("new_call_fab")
      ) {
        Icon(Icons.Default.Call, contentDescription = "Start New Call")
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)
    ) {
      Text(
        text = "Recent Calls",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(12.dp))

      if (uiState.callHistory.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              Icons.Default.Call,
              contentDescription = null,
              modifier = Modifier.size(56.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
              text = "No recent calls",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "To call a contact, open a chat or tap the call button below.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(uiState.callHistory, key = { it.id }) { call ->
            val isOutgoing = call.callerId == myUid
            val otherName = if (isOutgoing) call.receiverName else call.callerName
            val isMissed = call.status in listOf("missed", "rejected")

            val timeText = remember(call.createdAt) {
              if (call.createdAt != null) {
                SimpleDateFormat("MMM dd, hh:mm a", Locale.getDefault()).format(call.createdAt.toDate())
              } else "Recently"
            }

            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  modifier = Modifier.size(46.dp),
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primaryContainer
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = otherName.take(1).uppercase(),
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                  }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = otherName,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
                  )
                  Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                  ) {
                    Icon(
                      imageVector = if (isOutgoing) Icons.Default.CallMade else Icons.Default.CallReceived,
                      contentDescription = null,
                      tint = if (isMissed) CallRed else CallGreen,
                      modifier = Modifier.size(16.dp)
                    )
                    Text(
                      text = timeText,
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (call.durationSeconds > 0) {
                      Text(
                        text = "(${call.durationSeconds}s)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                  }
                }

                IconButton(
                  onClick = {
                    val contact = uiState.contacts.firstOrNull { it.displayName == otherName }
                      ?: com.example.data.model.UserContact(
                        contactUserId = if (isOutgoing) call.receiverId else call.callerId,
                        displayName = otherName,
                        username = otherName.lowercase().replace(" ", "_"),
                        userCode = "PK000000"
                      )
                    viewModel.startCall(contact, call.callType)
                  }
                ) {
                  Icon(
                    imageVector = if (call.callType == "video") Icons.Default.Videocam else Icons.Default.Call,
                    contentDescription = "Call Again",
                    tint = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
