package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.Community
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState

@Composable
fun CommunitiesScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onCreateCommunity: () -> Unit
) {
  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = onCreateCommunity,
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        modifier = Modifier.testTag("create_community_fab")
      ) {
        Icon(Icons.Default.Groups, contentDescription = "New Community")
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
      // Intro Card
      Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          Surface(
            modifier = Modifier.size(54.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Groups, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(32.dp))
            }
          }
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = "Stay connected with a Community",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = "Communities bring related topic groups together, with an Announcements feed.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = "Your Communities (${uiState.communities.size})",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(8.dp))

      if (uiState.communities.isEmpty()) {
        Box(
          modifier = Modifier.fillMaxSize(),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(Icons.Default.Campaign, contentDescription = null, modifier = Modifier.size(56.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            Text("No communities joined yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Button(onClick = onCreateCommunity, shape = RoundedCornerShape(20.dp)) {
              Text("Start a Community")
            }
          }
        }
      } else {
        LazyColumn(
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          items(uiState.communities, key = { it.id }) { comm ->
            CommunityCard(community = comm)
          }
        }
      }
    }
  }
}

@Composable
fun CommunityCard(community: Community) {
  Card(
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    modifier = Modifier.fillMaxWidth()
  ) {
    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Surface(
          modifier = Modifier.size(46.dp),
          shape = RoundedCornerShape(12.dp),
          color = EmeraldPrimary.copy(alpha = 0.15f)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Groups, contentDescription = null, tint = EmeraldPrimary)
          }
        }
        Column {
          Text(text = community.name, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
          Text(text = "${community.memberIds.size} members • ${community.groupIds.size} groups", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      if (community.description.isNotBlank()) {
        Text(text = community.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      Surface(
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Campaign, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(20.dp))
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Announcements Feed Active", style = MaterialTheme.typography.labelMedium)
        }
      }
    }
  }
}
