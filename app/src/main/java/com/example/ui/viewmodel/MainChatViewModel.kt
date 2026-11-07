package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CallSession
import com.example.data.model.ChatConversation
import com.example.data.model.ChatMessage
import com.example.data.model.CloudBackup
import com.example.data.model.Community
import com.example.data.model.ContactRequest
import com.example.data.model.StatusUpdate
import com.example.data.model.UserCodeDirectory
import com.example.data.model.UserContact
import com.example.data.model.UserProfile
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class MainUiState(
  val currentTab: Int = 0, // 0: Chats, 1: Updates, 2: Communities, 3: Calls, 4: Settings
  val activeScreen: String? = null, // "chat", "new_contact", "contact_requests", "new_group", "new_community", "call", "backup", "privacy", "account"
  val userProfile: UserProfile? = null,
  val chats: List<ChatConversation> = emptyList(),
  val contacts: List<UserContact> = emptyList(),
  val incomingRequests: List<ContactRequest> = emptyList(),
  val communities: List<Community> = emptyList(),
  val statuses: List<StatusUpdate> = emptyList(),
  val callHistory: List<CallSession> = emptyList(),
  val latestBackup: CloudBackup? = null,
  // Active Chat State
  val activeChatId: String? = null,
  val activeChatContact: UserContact? = null,
  val activeChatTitle: String = "",
  val activeChatMessages: List<ChatMessage> = emptyList(),
  val replyToMessage: ChatMessage? = null,
  // Search State
  val searchQuery: String = "",
  val searchedUser: UserCodeDirectory? = null,
  val isSearchingUser: Boolean = false,
  val searchErrorMessage: String? = null,
  // Voice recording state
  val isRecordingVoice: Boolean = false,
  val recordingDurationSec: Long = 0L,
  // Active Call State
  val activeCallSession: CallSession? = null,
  val isCallMuted: Boolean = false,
  val isSpeakerOn: Boolean = false,
  val isVideoCameraOff: Boolean = false,
  val isFrontCamera: Boolean = true,
  val callDurationSec: Long = 0L,
  // Status viewer
  val viewingStatus: StatusUpdate? = null,
  // App general info/messages
  val infoBanner: String? = null,
  val isLoading: Boolean = false
)

class MainChatViewModel(
  private val repository: ChatRepository,
  private val currentUserId: String
) : ViewModel() {

  private val _uiState = MutableStateFlow(MainUiState())
  val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

  private var activeChatMessagesJob: Job? = null
  private var activeCallJob: Job? = null
  private var callTimerJob: Job? = null
  private var voiceRecordTimerJob: Job? = null

  init {
    loadInitialData()
  }

  private fun loadInitialData() {
    viewModelScope.launch {
      repository.observeUserProfile(currentUserId).collect { profile ->
        _uiState.value = _uiState.value.copy(userProfile = profile)
      }
    }

    viewModelScope.launch {
      repository.observeChats().collect { chatList ->
        _uiState.value = _uiState.value.copy(chats = chatList)
      }
    }

    viewModelScope.launch {
      repository.observeContacts().collect { contactList ->
        _uiState.value = _uiState.value.copy(contacts = contactList)
      }
    }

    viewModelScope.launch {
      repository.observeIncomingContactRequests().collect { requests ->
        _uiState.value = _uiState.value.copy(incomingRequests = requests)
      }
    }

    viewModelScope.launch {
      repository.observeStatuses().collect { statusList ->
        _uiState.value = _uiState.value.copy(statuses = statusList)
      }
    }

    viewModelScope.launch {
      repository.observeCommunities().collect { comms ->
        _uiState.value = _uiState.value.copy(communities = comms)
      }
    }

    viewModelScope.launch {
      repository.observeCallHistory().collect { history ->
        _uiState.value = _uiState.value.copy(callHistory = history)
      }
    }

    viewModelScope.launch {
      repository.observeLatestBackup().collect { backup ->
        _uiState.value = _uiState.value.copy(latestBackup = backup)
      }
    }

    viewModelScope.launch {
      repository.observeIncomingCall().collect { incomingCall ->
        if (incomingCall != null && _uiState.value.activeCallSession == null) {
          _uiState.value = _uiState.value.copy(
            activeCallSession = incomingCall,
            activeScreen = "call"
          )
        }
      }
    }
  }

  fun setTab(tab: Int) {
    _uiState.value = _uiState.value.copy(currentTab = tab, activeScreen = null)
  }

  fun navigateTo(screen: String) {
    _uiState.value = _uiState.value.copy(activeScreen = screen)
  }

  fun navigateBack() {
    val currentScreen = _uiState.value.activeScreen
    if (currentScreen == "chat") {
      activeChatMessagesJob?.cancel()
      _uiState.value = _uiState.value.copy(
        activeScreen = null,
        activeChatId = null,
        activeChatContact = null,
        activeChatMessages = emptyList(),
        replyToMessage = null
      )
    } else if (currentScreen == "call") {
      endCall()
    } else {
      _uiState.value = _uiState.value.copy(activeScreen = null, viewingStatus = null)
    }
  }

  fun setSearchQuery(query: String) {
    _uiState.value = _uiState.value.copy(searchQuery = query)
  }

  // --- CONTACT DISCOVERY ---

  fun searchContact(query: String) {
    val trimmed = query.trim()
    if (trimmed.isBlank()) {
      _uiState.value = _uiState.value.copy(searchedUser = null, searchErrorMessage = null)
      return
    }

    _uiState.value = _uiState.value.copy(isSearchingUser = true, searchErrorMessage = null, searchedUser = null)
    viewModelScope.launch {
      // First try user code (e.g. AF729184 or PK...)
      val byCode = repository.searchUserByCode(trimmed)
      if (byCode.isSuccess && byCode.getOrNull() != null) {
        val found = byCode.getOrNull()
        if (found?.userId == currentUserId) {
          _uiState.value = _uiState.value.copy(isSearchingUser = false, searchErrorMessage = "This is your own User Code!")
        } else {
          _uiState.value = _uiState.value.copy(isSearchingUser = false, searchedUser = found)
        }
        return@launch
      }

      // Next try username (e.g. @ahmedkhan)
      val byUsername = repository.searchUserByUsername(trimmed)
      if (byUsername.isSuccess && byUsername.getOrNull() != null) {
        val userDir = byUsername.getOrNull()
        if (userDir?.userId == currentUserId) {
          _uiState.value = _uiState.value.copy(isSearchingUser = false, searchErrorMessage = "This is your own Username!")
        } else {
          val found = UserCodeDirectory(
            userCode = userDir?.userCode ?: "",
            userId = userDir?.userId ?: "",
            username = userDir?.username ?: "",
            displayName = userDir?.displayName ?: "",
            avatarUrl = userDir?.avatarUrl
          )
          _uiState.value = _uiState.value.copy(isSearchingUser = false, searchedUser = found)
        }
      } else {
        _uiState.value = _uiState.value.copy(
          isSearchingUser = false,
          searchErrorMessage = "No user found with username or code '$trimmed'"
        )
      }
    }
  }

  fun sendContactRequest(targetUser: UserCodeDirectory) {
    val profile = _uiState.value.userProfile ?: return
    viewModelScope.launch {
      val result = repository.sendContactRequest(
        toUserId = targetUser.userId,
        fromUsername = profile.username,
        fromDisplayName = profile.displayName,
        fromUserCode = profile.userCode
      )
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(
          searchedUser = null,
          infoBanner = "Contact request sent to ${targetUser.displayName}!"
        )
      } else {
        _uiState.value = _uiState.value.copy(
          infoBanner = result.exceptionOrNull()?.localizedMessage ?: "Failed to send request"
        )
      }
    }
  }

  fun respondToRequest(request: ContactRequest, accept: Boolean) {
    val profile = _uiState.value.userProfile ?: return
    viewModelScope.launch {
      repository.respondToContactRequest(
        request = request,
        accept = accept,
        currentDisplayName = profile.displayName,
        currentUsername = profile.username,
        currentUserCode = profile.userCode
      )
      _uiState.value = _uiState.value.copy(
        infoBanner = if (accept) "Request accepted! Contact added." else "Request declined."
      )
    }
  }

  fun removeContact(contactUserId: String) {
    viewModelScope.launch {
      repository.removeContact(contactUserId)
      _uiState.value = _uiState.value.copy(infoBanner = "Contact removed")
    }
  }

  fun toggleBlockContact(contact: UserContact) {
    viewModelScope.launch {
      val newBlocked = !contact.isBlocked
      repository.toggleBlockContact(contact.contactUserId, newBlocked)
      _uiState.value = _uiState.value.copy(
        infoBanner = if (newBlocked) "User blocked" else "User unblocked"
      )
    }
  }

  // --- CHATS & MESSAGING ---

  fun openDirectChat(contact: UserContact) {
    val profile = _uiState.value.userProfile ?: return
    viewModelScope.launch {
      val result = repository.getOrCreateDirectChat(
        otherUserId = contact.contactUserId,
        otherUserName = contact.displayName,
        currentUserName = profile.displayName
      )
      if (result.isSuccess) {
        val chatId = result.getOrThrow()
        openChatById(chatId, contact.displayName, contact)
      }
    }
  }

  fun openChatConversation(chat: ChatConversation) {
    val profile = _uiState.value.userProfile
    val title = if (chat.type == "group") {
      chat.groupName ?: "Group Chat"
    } else {
      val otherId = chat.memberIds.firstOrNull { it != currentUserId }
      chat.memberNames[otherId] ?: "Chat"
    }
    openChatById(chat.id, title, null)
  }

  private fun openChatById(chatId: String, title: String, contact: UserContact?) {
    activeChatMessagesJob?.cancel()
    _uiState.value = _uiState.value.copy(
      activeChatId = chatId,
      activeChatTitle = title,
      activeChatContact = contact,
      activeScreen = "chat"
    )

    activeChatMessagesJob = viewModelScope.launch {
      repository.observeMessages(chatId).collect { msgList ->
        _uiState.value = _uiState.value.copy(activeChatMessages = msgList)
      }
    }
  }

  fun setReplyTo(message: ChatMessage?) {
    _uiState.value = _uiState.value.copy(replyToMessage = message)
  }

  fun sendTextMessage(text: String) {
    val chatId = _uiState.value.activeChatId ?: return
    val profile = _uiState.value.userProfile ?: return
    val reply = _uiState.value.replyToMessage
    val trimmed = text.trim()
    if (trimmed.isBlank()) return

    viewModelScope.launch {
      repository.sendMessage(
        chatId = chatId,
        senderName = profile.displayName,
        text = trimmed,
        type = "text",
        replyToMessageId = reply?.id,
        replyToText = reply?.text
      )
      _uiState.value = _uiState.value.copy(replyToMessage = null)
    }
  }

  fun sendMediaMessage(type: String, mediaName: String, mediaUrl: String, mediaSize: Long = 1024L) {
    val chatId = _uiState.value.activeChatId ?: return
    val profile = _uiState.value.userProfile ?: return

    viewModelScope.launch {
      repository.sendMessage(
        chatId = chatId,
        senderName = profile.displayName,
        text = if (type == "image") "Photo" else mediaName,
        type = type,
        mediaUrl = mediaUrl,
        mediaName = mediaName,
        mediaSize = mediaSize
      )
    }
  }

  fun startVoiceRecording() {
    voiceRecordTimerJob?.cancel()
    _uiState.value = _uiState.value.copy(isRecordingVoice = true, recordingDurationSec = 0L)
    voiceRecordTimerJob = viewModelScope.launch {
      while (_uiState.value.isRecordingVoice) {
        delay(1000L)
        _uiState.value = _uiState.value.copy(
          recordingDurationSec = _uiState.value.recordingDurationSec + 1
        )
      }
    }
  }

  fun cancelVoiceRecording() {
    voiceRecordTimerJob?.cancel()
    _uiState.value = _uiState.value.copy(isRecordingVoice = false, recordingDurationSec = 0L)
  }

  fun sendVoiceMessage() {
    val duration = _uiState.value.recordingDurationSec
    val chatId = _uiState.value.activeChatId ?: return
    val profile = _uiState.value.userProfile ?: return
    voiceRecordTimerJob?.cancel()
    _uiState.value = _uiState.value.copy(isRecordingVoice = false, recordingDurationSec = 0L)

    val actualDuration = if (duration == 0L) 2L else duration
    viewModelScope.launch {
      repository.sendMessage(
        chatId = chatId,
        senderName = profile.displayName,
        text = "Voice message (${actualDuration}s)",
        type = "audio",
        audioDurationSec = actualDuration,
        mediaName = "Voice Note.m4a"
      )
    }
  }

  fun sendLocationMessage(lat: Double = 33.6844, lng: Double = 73.0479, locationName: String = "Current Location") {
    val chatId = _uiState.value.activeChatId ?: return
    val profile = _uiState.value.userProfile ?: return

    viewModelScope.launch {
      repository.sendMessage(
        chatId = chatId,
        senderName = profile.displayName,
        text = "📍 $locationName ($lat, $lng)",
        type = "location"
      )
    }
  }

  fun reactToMessage(message: ChatMessage, emoji: String) {
    val chatId = _uiState.value.activeChatId ?: return
    viewModelScope.launch {
      repository.reactToMessage(chatId, message.id, emoji)
    }
  }

  fun deleteMessageForEveryone(message: ChatMessage) {
    val chatId = _uiState.value.activeChatId ?: return
    viewModelScope.launch {
      repository.deleteMessageForEveryone(chatId, message.id)
    }
  }

  // --- GROUPS & COMMUNITIES ---

  fun createGroup(groupName: String, description: String, selectedContactIds: List<String>) {
    val profile = _uiState.value.userProfile ?: return
    val memberNames = mutableMapOf<String, String>(currentUserId to profile.displayName)
    _uiState.value.contacts.filter { it.contactUserId in selectedContactIds }.forEach {
      memberNames[it.contactUserId] = it.displayName
    }

    viewModelScope.launch {
      val result = repository.createGroupChat(
        groupName = groupName,
        groupDescription = description,
        memberIds = selectedContactIds,
        memberNames = memberNames
      )
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(activeScreen = null, infoBanner = "Group '$groupName' created!")
      }
    }
  }

  fun createCommunity(name: String, description: String, groupIds: List<String>) {
    viewModelScope.launch {
      val result = repository.createCommunity(name, description, groupIds)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(activeScreen = null, infoBanner = "Community '$name' created!")
      }
    }
  }

  // --- CALLS (VOICE & VIDEO) ---

  fun startCall(contact: UserContact, callType: String) {
    val profile = _uiState.value.userProfile ?: return
    viewModelScope.launch {
      val result = repository.startCall(
        receiverId = contact.contactUserId,
        receiverName = contact.displayName,
        callerName = profile.displayName,
        callType = callType
      )
      if (result.isSuccess) {
        val callId = result.getOrThrow()
        val session = CallSession(
          id = callId,
          callerId = currentUserId,
          callerName = profile.displayName,
          receiverId = contact.contactUserId,
          receiverName = contact.displayName,
          callType = callType,
          status = "ringing"
        )
        _uiState.value = _uiState.value.copy(
          activeCallSession = session,
          activeScreen = "call",
          callDurationSec = 0L
        )
        observeCallSession(callId)
      }
    }
  }

  private fun observeCallSession(callId: String) {
    activeCallJob?.cancel()
    activeCallJob = viewModelScope.launch {
      repository.observeActiveCall(callId).collect { session ->
        if (session != null) {
          _uiState.value = _uiState.value.copy(activeCallSession = session)
          if (session.status == "accepted" && callTimerJob == null) {
            startCallTimer(callId)
          } else if (session.status in listOf("rejected", "ended", "missed")) {
            callTimerJob?.cancel()
            callTimerJob = null
            _uiState.value = _uiState.value.copy(activeScreen = null, activeCallSession = null)
          }
        }
      }
    }
  }

  fun acceptIncomingCall() {
    val session = _uiState.value.activeCallSession ?: return
    viewModelScope.launch {
      repository.updateCallStatus(session.id, "accepted")
      _uiState.value = _uiState.value.copy(callDurationSec = 0L)
      startCallTimer(session.id)
    }
  }

  fun rejectIncomingCall() {
    val session = _uiState.value.activeCallSession ?: return
    viewModelScope.launch {
      repository.updateCallStatus(session.id, "rejected")
      _uiState.value = _uiState.value.copy(activeCallSession = null, activeScreen = null)
    }
  }

  fun endCall() {
    val session = _uiState.value.activeCallSession
    val duration = _uiState.value.callDurationSec
    callTimerJob?.cancel()
    callTimerJob = null
    activeCallJob?.cancel()
    _uiState.value = _uiState.value.copy(
      activeCallSession = null,
      activeScreen = null,
      callDurationSec = 0L
    )

    if (session != null) {
      viewModelScope.launch {
        repository.updateCallStatus(session.id, "ended", duration)
      }
    }
  }

  private fun startCallTimer(callId: String) {
    callTimerJob?.cancel()
    callTimerJob = viewModelScope.launch {
      while (_uiState.value.activeCallSession?.status == "accepted") {
        delay(1000L)
        _uiState.value = _uiState.value.copy(
          callDurationSec = _uiState.value.callDurationSec + 1
        )
      }
    }
  }

  fun toggleMute() {
    _uiState.value = _uiState.value.copy(isCallMuted = !_uiState.value.isCallMuted)
  }

  fun toggleSpeaker() {
    _uiState.value = _uiState.value.copy(isSpeakerOn = !_uiState.value.isSpeakerOn)
  }

  fun toggleVideoCamera() {
    _uiState.value = _uiState.value.copy(isVideoCameraOff = !_uiState.value.isVideoCameraOff)
  }

  fun flipCamera() {
    _uiState.value = _uiState.value.copy(isFrontCamera = !_uiState.value.isFrontCamera)
  }

  // --- STATUS / STORIES ---

  fun postStatus(type: String, text: String, caption: String, bgColor: String = "#0B3C2D", mediaUrl: String? = null) {
    val profile = _uiState.value.userProfile ?: return
    viewModelScope.launch {
      val result = repository.postStatus(
        userName = profile.displayName,
        type = type,
        text = text,
        caption = caption,
        bgColor = bgColor,
        mediaUrl = mediaUrl
      )
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(infoBanner = "Status updated!")
      }
    }
  }

  fun viewStatus(status: StatusUpdate) {
    _uiState.value = _uiState.value.copy(viewingStatus = status)
    viewModelScope.launch {
      repository.markStatusViewed(status.id)
    }
  }

  fun closeStatusViewer() {
    _uiState.value = _uiState.value.copy(viewingStatus = null)
  }

  // --- CLOUD BACKUP ---

  fun triggerCloudBackup(isEncrypted: Boolean = true) {
    _uiState.value = _uiState.value.copy(isLoading = true)
    viewModelScope.launch {
      val result = repository.createCloudBackup(isEncrypted)
      if (result.isSuccess) {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          latestBackup = result.getOrThrow(),
          infoBanner = "Cloud backup completed successfully!"
        )
      } else {
        _uiState.value = _uiState.value.copy(
          isLoading = false,
          infoBanner = "Backup failed: ${result.exceptionOrNull()?.localizedMessage}"
        )
      }
    }
  }

  fun clearInfoBanner() {
    _uiState.value = _uiState.value.copy(infoBanner = null)
  }
}
