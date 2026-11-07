package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class UserProfile(
  @DocumentId val id: String = "",
  val userId: String = "",
  val email: String = "",
  val displayName: String = "",
  val username: String = "",
  val userCode: String = "",
  val avatarUrl: String? = null,
  val about: String = "Hey there! I am using Chats Message Pk.",
  val isOnline: Boolean = false,
  val lastSeen: Timestamp? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UsernameDirectory(
  @DocumentId val id: String = "",
  val username: String = "",
  val userId: String = "",
  val userCode: String = "",
  val displayName: String = "",
  val avatarUrl: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserCodeDirectory(
  @DocumentId val id: String = "",
  val userCode: String = "",
  val userId: String = "",
  val username: String = "",
  val displayName: String = "",
  val avatarUrl: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ContactRequest(
  @DocumentId val id: String = "",
  val fromUserId: String = "",
  val toUserId: String = "",
  val fromUsername: String = "",
  val fromDisplayName: String = "",
  val fromUserCode: String = "",
  val status: String = "pending", // pending, accepted, rejected
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class UserContact(
  @DocumentId val id: String = "",
  val userId: String = "",
  val contactUserId: String = "",
  val displayName: String = "",
  val username: String = "",
  val userCode: String = "",
  val avatarUrl: String? = null,
  val isBlocked: Boolean = false,
  val isPinned: Boolean = false,
  val isMuted: Boolean = false,
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatConversation(
  @DocumentId val id: String = "",
  val type: String = "direct", // direct, group
  val memberIds: List<String> = emptyList(),
  val memberNames: Map<String, String> = emptyMap(),
  val lastMessage: String = "",
  val lastMessageSenderId: String = "",
  val lastMessageTime: Timestamp? = null,
  val unreadCounts: Map<String, Long> = emptyMap(),
  val typingUsers: List<String> = emptyList(),
  val groupName: String? = null,
  val groupDescription: String? = null,
  val groupAdminIds: List<String> = emptyList(),
  val communityId: String? = null,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class ChatMessage(
  @DocumentId val id: String = "",
  val chatId: String = "",
  val senderId: String = "",
  val senderName: String = "",
  val text: String = "",
  val type: String = "text", // text, image, audio, document, location, contact
  val mediaUrl: String? = null,
  val mediaName: String? = null,
  val mediaSize: Long = 0L,
  val audioDurationSec: Long = 0L,
  val replyToMessageId: String? = null,
  val replyToText: String? = null,
  val status: String = "sent", // sent, delivered, read
  val isStarred: Boolean = false,
  val isDeletedForEveryone: Boolean = false,
  val reactions: Map<String, String> = emptyMap(),
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CallSession(
  @DocumentId val id: String = "",
  val callerId: String = "",
  val callerName: String = "",
  val receiverId: String = "",
  val receiverName: String = "",
  val callType: String = "voice", // voice, video
  val status: String = "ringing", // ringing, accepted, rejected, ended, missed
  val sdpOffer: String? = null,
  val sdpAnswer: String? = null,
  val durationSeconds: Long = 0L,
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class StatusUpdate(
  @DocumentId val id: String = "",
  val userId: String = "",
  val userName: String = "",
  val userAvatarUrl: String? = null,
  val type: String = "text", // text, image
  val caption: String = "",
  val text: String = "",
  val bgColor: String = "#0B3C2D",
  val mediaUrl: String? = null,
  val viewerIds: List<String> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null
)

@IgnoreExtraProperties
data class Community(
  @DocumentId val id: String = "",
  val name: String = "",
  val description: String = "",
  val adminId: String = "",
  val memberIds: List<String> = emptyList(),
  val groupIds: List<String> = emptyList(),
  @ServerTimestamp val createdAt: Timestamp? = null,
  @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class CloudBackup(
  @DocumentId val id: String = "",
  val userId: String = "",
  val backupTime: Timestamp? = null,
  val backupSize: Long = 0L,
  val chatCount: Long = 0L,
  val messageCount: Long = 0L,
  val isEncrypted: Boolean = false,
  val backupData: String = "",
  @ServerTimestamp val createdAt: Timestamp? = null
)
