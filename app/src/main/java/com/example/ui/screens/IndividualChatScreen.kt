package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatMessage
import com.example.data.model.UserContact
import com.example.ui.theme.*
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndividualChatScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState,
  onNavigateBack: () -> Unit
) {
  BackHandler { onNavigateBack() }

  val context = LocalContext.current
  var inputText by remember { mutableStateOf("") }
  var showAttachmentSheet by remember { mutableStateOf(false) }
  var selectedMessageForOptions by remember { mutableStateOf<ChatMessage?>(null) }
  val listState = rememberLazyListState()

  // Auto-scroll to bottom on new message
  LaunchedEffect(uiState.activeChatMessages.size) {
    if (uiState.activeChatMessages.isNotEmpty()) {
      listState.animateScrollToItem(uiState.activeChatMessages.size - 1)
    }
  }

  val activeContact = uiState.activeChatContact ?: uiState.contacts.firstOrNull {
    it.contactUserId in (uiState.chats.firstOrNull { c -> c.id == uiState.activeChatId }?.memberIds ?: emptyList())
  } ?: UserContact(
    contactUserId = "other",
    displayName = uiState.activeChatTitle,
    username = "chat_user",
    userCode = "PK000000"
  )

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onNavigateBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        title = {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              modifier = Modifier.size(40.dp),
              shape = CircleShape,
              color = MaterialTheme.colorScheme.primaryContainer
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = uiState.activeChatTitle.take(1).uppercase(),
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
              }
            }

            Column {
              Text(
                text = uiState.activeChatTitle,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                maxLines = 1
              )
              Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Box(
                  modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF25D366))
                )
                Text(
                  text = "Online",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        },
        actions = {
          // Video Call button
          IconButton(
            onClick = { viewModel.startCall(activeContact, "video") },
            modifier = Modifier.testTag("video_call_button")
          ) {
            Icon(Icons.Default.Videocam, contentDescription = "Video Call", tint = MaterialTheme.colorScheme.primary)
          }

          // Voice Call button
          IconButton(
            onClick = { viewModel.startCall(activeContact, "voice") },
            modifier = Modifier.testTag("voice_call_button")
          ) {
            Icon(Icons.Default.Call, contentDescription = "Voice Call", tint = MaterialTheme.colorScheme.primary)
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.surface
        )
      )
    },
    bottomBar = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.surface)
          .padding(horizontal = 8.dp, vertical = 6.dp)
      ) {
        // Reply Quote Banner if replying
        if (uiState.replyToMessage != null) {
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 6.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier.padding(8.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Replying to ${uiState.replyToMessage.senderName}",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = EmeraldPrimary
                )
                Text(
                  text = uiState.replyToMessage.text,
                  style = MaterialTheme.typography.bodySmall,
                  maxLines = 1
                )
              }
              IconButton(onClick = { viewModel.setReplyTo(null) }) {
                Icon(Icons.Default.Close, contentDescription = "Cancel reply", modifier = Modifier.size(16.dp))
              }
            }
          }
        }

        // Voice Recording State Banner
        if (uiState.isRecordingVoice) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(52.dp)
              .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(26.dp))
              .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Mic, contentDescription = null, tint = Color.Red)
              Text(
                text = "Recording... ${uiState.recordingDurationSec}s",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.Red
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              TextButton(onClick = { viewModel.cancelVoiceRecording() }) {
                Text("Cancel", color = MaterialTheme.colorScheme.error)
              }
              IconButton(
                onClick = { viewModel.sendVoiceMessage() },
                modifier = Modifier
                  .size(40.dp)
                  .background(EmeraldPrimary, CircleShape)
              ) {
                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send Voice", tint = Color.White)
              }
            }
          }
        } else {
          // Standard Text & Media Input Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              modifier = Modifier
                .weight(1f)
                .heightIn(min = 48.dp, max = 120.dp),
              shape = RoundedCornerShape(24.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
              ) {
                IconButton(onClick = { showAttachmentSheet = true }) {
                  Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Attach Media",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }

                TextField(
                  value = inputText,
                  onValueChange = { inputText = it },
                  placeholder = { Text("Message...") },
                  modifier = Modifier
                    .weight(1f)
                    .testTag("chat_message_input"),
                  colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                  ),
                  maxLines = 4
                )

                IconButton(onClick = {
                  viewModel.sendMediaMessage("image", "Photo.jpg", "https://picsum.photos/400/300")
                }) {
                  Icon(
                    Icons.Default.CameraAlt,
                    contentDescription = "Camera",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                  )
                }
              }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Mic / Send Toggle Button
            if (inputText.isNotBlank()) {
              IconButton(
                onClick = {
                  viewModel.sendTextMessage(inputText)
                  inputText = ""
                },
                modifier = Modifier
                  .size(48.dp)
                  .background(MaterialTheme.colorScheme.primary, CircleShape)
                  .testTag("send_message_button")
              ) {
                Icon(
                  Icons.AutoMirrored.Filled.Send,
                  contentDescription = "Send",
                  tint = MaterialTheme.colorScheme.onPrimary
                )
              }
            } else {
              IconButton(
                onClick = { viewModel.startVoiceRecording() },
                modifier = Modifier
                  .size(48.dp)
                  .background(MaterialTheme.colorScheme.primary, CircleShape)
                  .testTag("voice_record_button")
              ) {
                Icon(
                  Icons.Default.Mic,
                  contentDescription = "Record Voice Message",
                  tint = MaterialTheme.colorScheme.onPrimary
                )
              }
            }
          }
        }
      }
    }
  ) { padding ->
    LazyColumn(
      state = listState,
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
        .padding(horizontal = 12.dp, vertical = 8.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(uiState.activeChatMessages, key = { it.id }) { message ->
        val isMine = message.senderId == uiState.userProfile?.userId
        MessageBubble(
          message = message,
          isMine = isMine,
          onMessageClick = { selectedMessageForOptions = message },
          onReact = { emoji -> viewModel.reactToMessage(message, emoji) }
        )
      }
    }
  }

  // Attachment Bottom Sheet
  if (showAttachmentSheet) {
    ModalBottomSheet(onDismissRequest = { showAttachmentSheet = false }) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Text(
          text = "Share Content",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          AttachmentIcon(
            icon = Icons.Default.Image,
            label = "Gallery",
            color = Color(0xFFC75DEB),
            onClick = {
              showAttachmentSheet = false
              viewModel.sendMediaMessage("image", "Shared Image.jpg", "https://picsum.photos/400/300")
            }
          )
          AttachmentIcon(
            icon = Icons.Default.Description,
            label = "Document",
            color = Color(0xFF5F66CD),
            onClick = {
              showAttachmentSheet = false
              viewModel.sendMediaMessage("document", "Project_Report.pdf", "https://example.com/doc.pdf", 204800L)
            }
          )
          AttachmentIcon(
            icon = Icons.Default.Audiotrack,
            label = "Audio",
            color = Color(0xFFF07936),
            onClick = {
              showAttachmentSheet = false
              viewModel.sendMediaMessage("audio", "Audio Recording.mp3", "https://example.com/audio.mp3", 512000L)
            }
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceAround
        ) {
          AttachmentIcon(
            icon = Icons.Default.LocationOn,
            label = "Location",
            color = Color(0xFF1EA362),
            onClick = {
              showAttachmentSheet = false
              viewModel.sendLocationMessage(33.6844, 73.0479, "Islamabad, PK")
            }
          )
          AttachmentIcon(
            icon = Icons.Default.Person,
            label = "Contact",
            color = Color(0xFF009DE2),
            onClick = {
              showAttachmentSheet = false
              viewModel.sendMediaMessage("contact", "Ahmed Khan (+92 300 1234567)", "")
            }
          )
        }
      }
    }
  }

  // Message Options Dialog
  if (selectedMessageForOptions != null) {
    val msg = selectedMessageForOptions!!
    val isMine = msg.senderId == uiState.userProfile?.userId

    AlertDialog(
      onDismissRequest = { selectedMessageForOptions = null },
      title = { Text("Message Options") },
      text = {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          // Quick Reaction Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
          ) {
            listOf("👍", "❤️", "😂", "😮", "😢", "🙏").forEach { emoji ->
              Text(
                text = emoji,
                fontSize = 24.sp,
                modifier = Modifier
                  .clickable {
                    viewModel.reactToMessage(msg, emoji)
                    selectedMessageForOptions = null
                  }
                  .padding(4.dp)
              )
            }
          }

          HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

          TextButton(
            onClick = {
              viewModel.setReplyTo(msg)
              selectedMessageForOptions = null
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
              Icon(Icons.AutoMirrored.Filled.Reply, contentDescription = null)
              Spacer(modifier = Modifier.width(12.dp))
              Text("Reply")
            }
          }

          TextButton(
            onClick = {
              val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
              clipboard.setPrimaryClip(ClipData.newPlainText("Chat Message", msg.text))
              Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
              selectedMessageForOptions = null
            },
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
              Icon(Icons.Default.ContentCopy, contentDescription = null)
              Spacer(modifier = Modifier.width(12.dp))
              Text("Copy text")
            }
          }

          if (isMine && !msg.isDeletedForEveryone) {
            TextButton(
              onClick = {
                viewModel.deleteMessageForEveryone(msg)
                selectedMessageForOptions = null
              },
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(12.dp))
                Text("Delete for everyone", color = MaterialTheme.colorScheme.error)
              }
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { selectedMessageForOptions = null }) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
fun AttachmentIcon(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  label: String,
  color: Color,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier.clickable(onClick = onClick)
  ) {
    Surface(
      modifier = Modifier.size(56.dp),
      shape = CircleShape,
      color = color
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(icon, contentDescription = label, tint = Color.White)
      }
    }
    Spacer(modifier = Modifier.height(4.dp))
    Text(text = label, style = MaterialTheme.typography.labelSmall)
  }
}

@Composable
fun MessageBubble(
  message: ChatMessage,
  isMine: Boolean,
  onMessageClick: () -> Unit,
  onReact: (String) -> Unit
) {
  val bubbleColor = if (isMine) {
    if (MaterialTheme.colorScheme.background == SlateDarkBackground) OutgoingBubbleDark else OutgoingBubbleLight
  } else {
    if (MaterialTheme.colorScheme.background == SlateDarkBackground) IncomingBubbleDark else IncomingBubbleLight
  }

  val timeString = remember(message.createdAt) {
    if (message.createdAt != null) {
      SimpleDateFormat("hh:mm a", Locale.getDefault()).format(message.createdAt.toDate())
    } else ""
  }

  Column(
    modifier = Modifier.fillMaxWidth(),
    horizontalAlignment = if (isMine) Alignment.End else Alignment.Start
  ) {
    Surface(
      color = bubbleColor,
      shape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isMine) 16.dp else 4.dp,
        bottomEnd = if (isMine) 4.dp else 16.dp
      ),
      shadowElevation = 1.dp,
      modifier = Modifier
        .widthIn(min = 80.dp, max = 300.dp)
        .clickable(onClick = onMessageClick)
    ) {
      Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
        // Replied Message header if present
        if (!message.replyToText.isNullOrBlank()) {
          Surface(
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
            shape = RoundedCornerShape(6.dp),
            modifier = Modifier
              .fillMaxWidth()
              .padding(bottom = 4.dp)
          ) {
            Text(
              text = message.replyToText,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
              maxLines = 1
            )
          }
        }

        // Voice Message View
        if (message.type == "audio") {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            Surface(
              modifier = Modifier.size(36.dp),
              shape = CircleShape,
              color = EmeraldPrimary
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play Audio", tint = Color.White)
              }
            }
            Column {
              // Simulated Waveform bars
              Row(
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                listOf(12, 20, 8, 24, 16, 28, 10, 18, 22, 14, 26, 12, 18).forEach { barHeight ->
                  Box(
                    modifier = Modifier
                      .width(3.dp)
                      .height(barHeight.dp)
                      .background(EmeraldPrimary.copy(alpha = 0.7f), RoundedCornerShape(1.dp))
                  )
                }
              }
              Text(
                text = "${message.audioDurationSec}s",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        } else if (message.type == "image") {
          // Image preview card
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .height(160.dp),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(48.dp), tint = EmeraldPrimary)
            }
          }
        } else if (message.type == "document") {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(vertical = 4.dp)
          ) {
            Icon(Icons.Default.Description, contentDescription = null, tint = EmeraldPrimary)
            Text(
              text = message.mediaName ?: "Document.pdf",
              style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
            )
          }
        }

        // Message text
        Text(
          text = message.text,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurface
        )

        // Time and tick status
        Row(
          modifier = Modifier
            .align(Alignment.End)
            .padding(top = 2.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          Text(
            text = timeString,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (isMine) {
            Icon(
              Icons.Default.DoneAll,
              contentDescription = "Read status",
              tint = BlueTick,
              modifier = Modifier.size(14.dp)
            )
          }
        }
      }
    }

    // Reaction badge
    if (message.reactions.isNotEmpty()) {
      val emojis = message.reactions.values.distinct().joinToString(" ")
      Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = CircleShape,
        shadowElevation = 2.dp,
        modifier = Modifier.padding(top = 2.dp)
      ) {
        Text(
          text = emojis,
          fontSize = 12.sp,
          modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
      }
    }
  }
}
