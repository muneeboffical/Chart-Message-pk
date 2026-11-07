package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ContactRequest
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactRequestsScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        title = { Text("Contact Requests") },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp)
    ) {
      if (uiState.incomingRequests.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              Icons.Default.PersonAdd,
              contentDescription = null,
              modifier = Modifier.size(56.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
              text = "No pending contact requests",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "When someone adds you using your User Code or Username, you'll see them here.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(uiState.incomingRequests, key = { it.id }) { req ->
            RequestItemCard(
              request = req,
              onAccept = { viewModel.respondToRequest(req, accept = true) },
              onReject = { viewModel.respondToRequest(req, accept = false) }
            )
          }
        }
      }
    }
  }
}

@Composable
fun RequestItemCard(
  request: ContactRequest,
  onAccept: () -> Unit,
  onReject: () -> Unit
) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth().testTag("request_item_card")
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        modifier = Modifier.size(50.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = request.fromDisplayName.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = request.fromDisplayName,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Text(
          text = "@${request.fromUsername} • ${request.fromUserCode}",
          style = MaterialTheme.typography.bodySmall,
          color = EmeraldPrimary
        )
        Text(
          text = "Wants to add you as a contact",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        IconButton(
          onClick = onReject,
          modifier = Modifier.size(36.dp).background(MaterialTheme.colorScheme.errorContainer, CircleShape)
        ) {
          Icon(Icons.Default.Close, contentDescription = "Decline", tint = MaterialTheme.colorScheme.onErrorContainer)
        }
        IconButton(
          onClick = onAccept,
          modifier = Modifier.size(36.dp).background(EmeraldPrimary, CircleShape).testTag("accept_request_button")
        ) {
          Icon(Icons.Default.Check, contentDescription = "Accept", tint = Color.White)
        }
      }
    }
  }
}
