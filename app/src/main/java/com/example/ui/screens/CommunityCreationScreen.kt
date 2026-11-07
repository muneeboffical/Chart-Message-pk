package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommunityCreationScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  var commName by remember { mutableStateOf("") }
  var commDescription by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        title = { Text("New Community") },
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
          Text(
            text = "Create a New Community",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
          )
          Text(
            text = "Bring related topic groups together and send announcements to all members.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          OutlinedTextField(
            value = commName,
            onValueChange = { commName = it },
            label = { Text("Community Name") },
            modifier = Modifier.fillMaxWidth().testTag("community_name_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = commDescription,
            onValueChange = { commDescription = it },
            label = { Text("Community Description") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 4,
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      Button(
        onClick = {
          viewModel.createCommunity(commName, commDescription, emptyList())
          onNavigateBack()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("create_community_submit_button"),
        shape = RoundedCornerShape(25.dp),
        enabled = commName.isNotBlank()
      ) {
        Icon(Icons.Default.Groups, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Create Community", style = MaterialTheme.typography.titleMedium)
      }
    }
  }
}
