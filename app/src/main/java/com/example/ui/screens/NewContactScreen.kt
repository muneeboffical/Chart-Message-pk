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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.UserContact
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewContactScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  var usernameInput by remember { mutableStateOf("") }
  var userCodeInput by remember { mutableStateOf("") }
  var contactsSearchQuery by remember { mutableStateOf("") }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        title = { Text("New Contact & Discovery") },
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
      // Search / Discovery Card
      item {
        Card(
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Text(
              text = "Find User by Username & Code",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Every user in Chats Message Pk has a unique Username and User Code. Enter either or both to find them.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
              value = usernameInput,
              onValueChange = { usernameInput = it },
              label = { Text("1. Enter Username (e.g. ahmedkhan)") },
              prefix = { Text("@") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("search_username_input"),
              shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
              value = userCodeInput,
              onValueChange = { userCodeInput = it.uppercase() },
              label = { Text("2. Enter Unique User Code (e.g. AF729184)") },
              singleLine = true,
              modifier = Modifier.fillMaxWidth().testTag("search_usercode_input"),
              shape = RoundedCornerShape(12.dp)
            )

            Button(
              onClick = {
                val query = if (userCodeInput.isNotBlank()) userCodeInput else usernameInput
                viewModel.searchContact(query)
              },
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .testTag("search_user_button"),
              shape = RoundedCornerShape(24.dp),
              enabled = (usernameInput.isNotBlank() || userCodeInput.isNotBlank()) && !uiState.isSearchingUser
            ) {
              if (uiState.isSearchingUser) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
              } else {
                Icon(Icons.Default.Search, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Search User")
              }
            }

            if (uiState.searchErrorMessage != null) {
              Text(
                text = uiState.searchErrorMessage,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
        }
      }

      // Found User Card Preview
      if (uiState.searchedUser != null) {
        val found = uiState.searchedUser
        val alreadyContact = uiState.contacts.any { it.contactUserId == found.userId }

        item {
          Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
            modifier = Modifier.fillMaxWidth().testTag("found_user_card")
          ) {
            Column(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Surface(
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = found.displayName.take(1).uppercase(),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                  )
                }
              }

              Text(
                text = found.displayName,
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onPrimaryContainer
              )

              Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)
                ) {
                  Text(
                    text = "@${found.username}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }

                Surface(
                  shape = RoundedCornerShape(8.dp),
                  color = EmeraldPrimary.copy(alpha = 0.2f)
                ) {
                  Text(
                    text = "Code: ${found.userCode}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                    color = EmeraldPrimary,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                  )
                }
              }

              Spacer(modifier = Modifier.height(4.dp))

              if (alreadyContact) {
                Text(
                  text = "✓ Already in your Contacts",
                  color = EmeraldPrimary,
                  style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                )
              } else {
                Button(
                  onClick = { viewModel.sendContactRequest(found) },
                  colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                  modifier = Modifier.testTag("add_contact_button")
                ) {
                  Icon(Icons.Default.PersonAdd, contentDescription = null)
                  Spacer(modifier = Modifier.width(8.dp))
                  Text("Add Contact / Send Request")
                }
              }
            }
          }
        }
      }

      // Existing Contacts Section
      item {
        Text(
          text = "Your Contacts (${uiState.contacts.size})",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onBackground
        )
      }

      item {
        OutlinedTextField(
          value = contactsSearchQuery,
          onValueChange = { contactsSearchQuery = it },
          placeholder = { Text("Search your contacts...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )
      }

      val filteredContacts = uiState.contacts.filter {
        it.displayName.contains(contactsSearchQuery, ignoreCase = true) ||
          it.username.contains(contactsSearchQuery, ignoreCase = true) ||
          it.userCode.contains(contactsSearchQuery, ignoreCase = true)
      }

      if (filteredContacts.isEmpty()) {
        item {
          Text(
            text = "No contacts in your list yet. Search above to add friends!",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 12.dp)
          )
        }
      } else {
        items(filteredContacts, key = { it.contactUserId }) { contact ->
          ContactListItem(
            contact = contact,
            onStartChat = {
              viewModel.openDirectChat(contact)
            },
            onToggleBlock = { viewModel.toggleBlockContact(contact) },
            onRemove = { viewModel.removeContact(contact.contactUserId) }
          )
        }
      }
    }
  }
}

@Composable
fun ContactListItem(
  contact: UserContact,
  onStartChat: () -> Unit,
  onToggleBlock: () -> Unit,
  onRemove: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Card(
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth()
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onStartChat)
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        modifier = Modifier.size(44.dp),
        shape = CircleShape,
        color = MaterialTheme.colorScheme.primaryContainer
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = contact.displayName.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }

      Spacer(modifier = Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = contact.displayName,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          Text(
            text = "@${contact.username}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Text(
            text = "• Code: ${contact.userCode}",
            style = MaterialTheme.typography.bodySmall,
            color = EmeraldPrimary
          )
        }
      }

      IconButton(onClick = onStartChat) {
        Icon(Icons.Default.Chat, contentDescription = "Chat", tint = MaterialTheme.colorScheme.primary)
      }

      Box {
        IconButton(onClick = { showMenu = true }) {
          Icon(Icons.Default.MoreVert, contentDescription = "Options")
        }
        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
          DropdownMenuItem(
            text = { Text(if (contact.isBlocked) "Unblock" else "Block") },
            leadingIcon = { Icon(Icons.Default.Block, contentDescription = null) },
            onClick = {
              showMenu = false
              onToggleBlock()
            }
          )
          DropdownMenuItem(
            text = { Text("Remove Contact", color = MaterialTheme.colorScheme.error) },
            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
            onClick = {
              showMenu = false
              onRemove()
            }
          )
        }
      }
    }
  }
}
