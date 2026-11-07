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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatConversation
import com.example.data.model.UserContact
import com.example.ui.theme.BlueTick
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.UnreadBadge
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ChatListScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onOpenNewContact: () -> Unit
) {
  var selectedFilter by remember { mutableStateOf("All") }
  var searchText by remember { mutableStateOf("") }

  val myUid = uiState.userProfile?.userId ?: ""

  val filteredChats = uiState.chats.filter { chat ->
    val title = if (chat.type == "group") {
      chat.groupName ?: "Group"
    } else {
      val otherId = chat.memberIds.firstOrNull { it != myUid }
      chat.memberNames[otherId] ?: "Chat"
    }

    val matchesSearch = title.contains(searchText, ignoreCase = true) ||
      chat.lastMessage.contains(searchText, ignoreCase = true)

    val matchesFilter = when (selectedFilter) {
      "Unread" -> (chat.unreadCounts[myUid] ?: 0L) > 0L
      "Groups" -> chat.type == "group"
      else -> true
    }
    matchesSearch && matchesFilter
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onOpenNewContact,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("new_chat_fab")
      ) {
        Icon(Icons.Default.Chat, contentDescription = "New Contact / Chat")
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Search Box
      OutlinedTextField(
        value = searchText,
        onValueChange = { searchText = it },
        placeholder = { Text("Search chats and messages...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (searchText.isNotEmpty()) {
            IconButton(onClick = { searchText = "" }) {
              Icon(Icons.Default.Clear, contentDescription = "Clear")
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = OutlinedTextFieldDefaults.colors(
          focusedContainerColor = MaterialTheme.colorScheme.surface,
          unfocusedContainerColor = MaterialTheme.colorScheme.surface
        )
      )

      // Filter Chips Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        listOf("All", "Unread", "Groups").forEach { filter ->
          FilterChip(
            selected = selectedFilter == filter,
            onClick = { selectedFilter = filter },
            label = { Text(filter) },
            shape = RoundedCornerShape(16.dp)
          )
        }
      }

      // User Code quick banner
      if (uiState.userProfile != null) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
          colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
          ),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.VpnKey, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
              Text(
                text = "Your User Code: ",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Text(
                text = uiState.userProfile.userCode,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                color = EmeraldPrimary
              )
            }

            Text(
              text = "@${uiState.userProfile.username}",
              style = MaterialTheme.typography.labelMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      if (filteredChats.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.Forum,
              contentDescription = null,
              modifier = Modifier.size(64.dp),
              tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Text(
              text = if (searchText.isBlank()) "No conversations yet" else "No matching chats found",
              style = MaterialTheme.typography.titleMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
              text = "Add contacts using their Username & User Code to start chatting securely.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
              textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(
              onClick = onOpenNewContact,
              shape = RoundedCornerShape(20.dp),
              modifier = Modifier.padding(top = 8.dp)
            ) {
              Icon(Icons.Default.PersonAdd, contentDescription = null)
              Spacer(modifier = Modifier.width(8.dp))
              Text("Add New Contact")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          contentPadding = PaddingValues(bottom = 80.dp)
        ) {
          items(filteredChats, key = { it.id }) { chat ->
            val otherId = chat.memberIds.firstOrNull { it != myUid }
            val title = if (chat.type == "group") {
              chat.groupName ?: "Group"
            } else {
              chat.memberNames[otherId] ?: "Chat"
            }
            val unreadCount = chat.unreadCounts[myUid] ?: 0L

            ChatItemRow(
              chat = chat,
              title = title,
              unreadCount = unreadCount,
              onClick = { viewModel.openChatConversation(chat) }
            )
            HorizontalDivider(
              modifier = Modifier.padding(start = 76.dp),
              thickness = 0.5.dp,
              color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
          }
        }
      }
    }
  }
}

@Composable
fun ChatItemRow(
  chat: ChatConversation,
  title: String,
  unreadCount: Long,
  onClick: () -> Unit
) {
  val timeText = remember(chat.lastMessageTime) {
    if (chat.lastMessageTime != null) {
      val date = chat.lastMessageTime.toDate()
      SimpleDateFormat("hh:mm a", Locale.getDefault()).format(date)
    } else ""
  }

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick)
      .padding(horizontal = 16.dp, vertical = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    // Avatar
    Surface(
      modifier = Modifier.size(52.dp),
      shape = CircleShape,
      color = if (chat.type == "group") MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.primaryContainer
    ) {
      Box(contentAlignment = Alignment.Center) {
        if (chat.type == "group") {
          Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer)
        } else {
          Text(
            text = title.take(1).uppercase(),
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onPrimaryContainer
          )
        }
      }
    }

    Spacer(modifier = Modifier.width(14.dp))

    // Middle text
    Column(modifier = Modifier.weight(1f)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = timeText,
          style = MaterialTheme.typography.bodySmall,
          color = if (unreadCount > 0) EmeraldPrimary else MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(3.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.weight(1f)
        ) {
          // Sent ticks indicator
          Icon(
            Icons.Default.DoneAll,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = BlueTick
          )
          Spacer(modifier = Modifier.width(4.dp))
          Text(
            text = chat.lastMessage.ifBlank { "No messages" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
        }

        if (unreadCount > 0) {
          Surface(
            color = UnreadBadge,
            shape = CircleShape,
            modifier = Modifier.size(20.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text(
                text = "$unreadCount",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }
      }
    }
  }
}
