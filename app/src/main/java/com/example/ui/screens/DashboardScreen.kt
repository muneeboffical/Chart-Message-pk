package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onOpenSettings: () -> Unit,
  onOpenNewContact: () -> Unit,
  onOpenRequests: () -> Unit,
  onOpenNewGroup: () -> Unit,
  onOpenNewCommunity: () -> Unit
) {
  var showMenu by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Chats Message Pk",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        },
        actions = {
          // Contact Requests notification icon
          val pendingCount = uiState.incomingRequests.size
          IconButton(onClick = onOpenRequests) {
            BadgedBox(badge = {
              if (pendingCount > 0) {
                Badge { Text("$pendingCount") }
              }
            }) {
              Icon(Icons.Default.PersonAdd, contentDescription = "Contact Requests")
            }
          }

          // Search button
          IconButton(onClick = onOpenNewContact) {
            Icon(Icons.Default.Search, contentDescription = "Search Contacts")
          }

          // More options menu
          IconButton(onClick = { showMenu = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More Options")
          }

          DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
          ) {
            DropdownMenuItem(
              text = { Text("New Contact") },
              leadingIcon = { Icon(Icons.Default.PersonAdd, contentDescription = null) },
              onClick = {
                showMenu = false
                onOpenNewContact()
              }
            )
            DropdownMenuItem(
              text = { Text("New Group") },
              leadingIcon = { Icon(Icons.Default.GroupAdd, contentDescription = null) },
              onClick = {
                showMenu = false
                onOpenNewGroup()
              }
            )
            DropdownMenuItem(
              text = { Text("New Community") },
              leadingIcon = { Icon(Icons.Default.Groups, contentDescription = null) },
              onClick = {
                showMenu = false
                onOpenNewCommunity()
              }
            )
            DropdownMenuItem(
              text = { Text("Settings") },
              leadingIcon = { Icon(Icons.Default.Settings, contentDescription = null) },
              onClick = {
                showMenu = false
                onOpenSettings()
              }
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("dashboard_navigation_bar")
      ) {
        val totalUnread = uiState.chats.sumOf {
          it.unreadCounts[uiState.userProfile?.userId ?: ""] ?: 0L
        }

        // 1. Chats
        NavigationBarItem(
          selected = uiState.currentTab == 0,
          onClick = { viewModel.setTab(0) },
          icon = {
            BadgedBox(badge = {
              if (totalUnread > 0) {
                Badge { Text("$totalUnread") }
              }
            }) {
              Icon(
                if (uiState.currentTab == 0) Icons.Filled.Chat else Icons.Outlined.Chat,
                contentDescription = "Chats"
              )
            }
          },
          label = { Text("Chats") }
        )

        // 2. Updates / Status
        NavigationBarItem(
          selected = uiState.currentTab == 1,
          onClick = { viewModel.setTab(1) },
          icon = {
            BadgedBox(badge = {
              if (uiState.statuses.isNotEmpty()) {
                Badge(containerColor = MaterialTheme.colorScheme.primary)
              }
            }) {
              Icon(
                if (uiState.currentTab == 1) Icons.Filled.Update else Icons.Outlined.Update,
                contentDescription = "Updates"
              )
            }
          },
          label = { Text("Updates") }
        )

        // 3. Communities
        NavigationBarItem(
          selected = uiState.currentTab == 2,
          onClick = { viewModel.setTab(2) },
          icon = {
            Icon(
              if (uiState.currentTab == 2) Icons.Filled.Groups else Icons.Outlined.Groups,
              contentDescription = "Communities"
            )
          },
          label = { Text("Communities") }
        )

        // 4. Calls
        NavigationBarItem(
          selected = uiState.currentTab == 3,
          onClick = { viewModel.setTab(3) },
          icon = {
            Icon(
              if (uiState.currentTab == 3) Icons.Filled.Call else Icons.Outlined.Call,
              contentDescription = "Calls"
            )
          },
          label = { Text("Calls") }
        )

        // 5. Settings
        NavigationBarItem(
          selected = uiState.currentTab == 4,
          onClick = { viewModel.setTab(4) },
          icon = {
            Icon(
              if (uiState.currentTab == 4) Icons.Filled.Settings else Icons.Outlined.Settings,
              contentDescription = "Settings"
            )
          },
          label = { Text("Settings") }
        )
      }
    }
  ) { innerPadding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(innerPadding)
    ) {
      when (uiState.currentTab) {
        0 -> ChatListScreen(
          viewModel = viewModel,
          uiState = uiState,
          onOpenNewContact = onOpenNewContact
        )
        1 -> StatusesScreen(
          viewModel = viewModel,
          uiState = uiState
        )
        2 -> CommunitiesScreen(
          viewModel = viewModel,
          uiState = uiState,
          onCreateCommunity = onOpenNewCommunity
        )
        3 -> CallsScreen(
          viewModel = viewModel,
          uiState = uiState,
          onStartNewCall = onOpenNewContact
        )
        4 -> SettingsHomeScreen(
          viewModel = viewModel,
          uiState = uiState,
          onNavigate = { viewModel.navigateTo(it) }
        )
      }
    }
  }
}
