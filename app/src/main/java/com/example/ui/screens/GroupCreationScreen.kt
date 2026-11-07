package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Group
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupCreationScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  var groupName by remember { mutableStateOf("") }
  var groupDescription by remember { mutableStateOf("") }
  val selectedContactIds = remember { mutableStateListOf<String>() }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        title = { Text("New Group") },
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
          OutlinedTextField(
            value = groupName,
            onValueChange = { groupName = it },
            label = { Text("Group Name") },
            modifier = Modifier.fillMaxWidth().testTag("group_name_input"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          OutlinedTextField(
            value = groupDescription,
            onValueChange = { groupDescription = it },
            label = { Text("Group Description (Optional)") },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3,
            shape = RoundedCornerShape(12.dp)
          )
        }
      }

      Text(
        text = "Select Participants (${selectedContactIds.size} selected)",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )

      if (uiState.contacts.isEmpty()) {
        Text(
          text = "You don't have any contacts yet. Add contacts to invite them to groups.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      } else {
        LazyColumn(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(uiState.contacts, key = { it.contactUserId }) { contact ->
            val isSelected = selectedContactIds.contains(contact.contactUserId)
            Card(
              shape = RoundedCornerShape(12.dp),
              colors = CardDefaults.cardColors(
                containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
              ),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  if (isSelected) selectedContactIds.remove(contact.contactUserId)
                  else selectedContactIds.add(contact.contactUserId)
                }
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Surface(
                  modifier = Modifier.size(40.dp),
                  shape = CircleShape,
                  color = MaterialTheme.colorScheme.primary
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Text(
                      text = contact.displayName.take(1).uppercase(),
                      color = Color.White,
                      fontWeight = FontWeight.Bold
                    )
                  }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                  Text(text = contact.displayName, fontWeight = FontWeight.SemiBold)
                  Text(text = "@${contact.username} • ${contact.userCode}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Checkbox(
                  checked = isSelected,
                  onCheckedChange = { checked ->
                    if (checked) selectedContactIds.add(contact.contactUserId)
                    else selectedContactIds.remove(contact.contactUserId)
                  }
                )
              }
            }
          }
        }
      }

      Button(
        onClick = {
          viewModel.createGroup(groupName, groupDescription, selectedContactIds.toList())
          onNavigateBack()
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp)
          .testTag("create_group_submit_button"),
        shape = RoundedCornerShape(25.dp),
        enabled = groupName.isNotBlank()
      ) {
        Icon(Icons.Default.Group, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text("Create Group", style = MaterialTheme.typography.titleMedium)
      }
    }
  }
}
