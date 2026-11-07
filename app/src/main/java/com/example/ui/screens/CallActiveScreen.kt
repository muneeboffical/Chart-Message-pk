package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CallGreen
import com.example.ui.theme.CallRed
import com.example.ui.viewmodel.MainChatViewModel
import com.example.ui.viewmodel.MainUiState

@Composable
fun CallActiveScreen(
  viewModel: MainChatViewModel,
  uiState: MainUiState
) {
  val session = uiState.activeCallSession ?: return
  val isIncoming = session.receiverId == uiState.userProfile?.userId && session.status == "ringing"
  val isConnected = session.status == "accepted"
  val isVideo = session.callType == "video"

  BackHandler { viewModel.endCall() }

  // Animated pulse for ringing
  val infiniteTransition = rememberInfiniteTransition(label = "ring_pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.15f,
    animationSpec = infiniteRepeatable(
      animation = tween(800, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulse_scale"
  )

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(if (isVideo) Color(0xFF0F172A) else Color(0xFF064E3B))
      .padding(24.dp)
      .testTag("call_active_screen"),
    contentAlignment = Alignment.Center
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top Info
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 40.dp)
      ) {
        Surface(
          color = Color.White.copy(alpha = 0.2f),
          shape = CircleShape
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              if (isVideo) Icons.Default.Videocam else Icons.Default.Call,
              contentDescription = null,
              tint = Color.White,
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = if (isVideo) "Chats Message Pk Video Call" else "Chats Message Pk Voice Call",
              color = Color.White,
              fontSize = 12.sp
            )
          }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Remote user avatar with pulse
        Surface(
          modifier = Modifier
            .size(120.dp)
            .scale(if (!isConnected) pulseScale else 1f),
          shape = CircleShape,
          color = Color.White.copy(alpha = 0.25f),
          shadowElevation = 8.dp
        ) {
          Box(contentAlignment = Alignment.Center) {
            val displayName = if (session.callerId == uiState.userProfile?.userId) session.receiverName else session.callerName
            Text(
              text = displayName.take(1).uppercase(),
              style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
              color = Color.White
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val remoteName = if (session.callerId == uiState.userProfile?.userId) session.receiverName else session.callerName
        Text(
          text = remoteName,
          style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
          color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Status or Duration
        val statusText = if (isConnected) {
          val minutes = uiState.callDurationSec / 60
          val seconds = uiState.callDurationSec % 60
          String.format("%02d:%02d", minutes, seconds)
        } else if (isIncoming) {
          "Incoming ${if (isVideo) "Video" else "Voice"} Call..."
        } else {
          "Ringing..."
        }

        Text(
          text = statusText,
          style = MaterialTheme.typography.titleMedium,
          color = Color.White.copy(alpha = 0.8f)
        )
      }

      // Simulated WebRTC Camera Video Frame for Video Call
      if (isVideo && isConnected && !uiState.isVideoCameraOff) {
        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
          shape = MaterialTheme.shapes.large,
          color = Color(0xFF1E293B)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
              Icon(
                Icons.Default.Videocam,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(48.dp)
              )
              Text(
                text = "Live WebRTC Camera Active (${if (uiState.isFrontCamera) "Front" else "Back"})",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.bodySmall
              )
            }
          }
        }
      }

      // Call Controls
      Column(
        modifier = Modifier.padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
      ) {
        if (isIncoming) {
          // Accept or Reject Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
          ) {
            IconButton(
              onClick = { viewModel.rejectIncomingCall() },
              modifier = Modifier
                .size(68.dp)
                .background(CallRed, CircleShape)
                .testTag("reject_call_button")
            ) {
              Icon(Icons.Default.CallEnd, contentDescription = "Decline Call", tint = Color.White, modifier = Modifier.size(32.dp))
            }

            IconButton(
              onClick = { viewModel.acceptIncomingCall() },
              modifier = Modifier
                .size(68.dp)
                .background(CallGreen, CircleShape)
                .testTag("accept_call_button")
            ) {
              Icon(Icons.Default.Call, contentDescription = "Answer Call", tint = Color.White, modifier = Modifier.size(32.dp))
            }
          }
        } else {
          // Connected / Outgoing Controls: Mute, Speaker, Video Toggle, Flip, End Call
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Mute Button
            IconButton(
              onClick = { viewModel.toggleMute() },
              modifier = Modifier
                .size(54.dp)
                .background(if (uiState.isCallMuted) Color.White else Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
              Icon(
                if (uiState.isCallMuted) Icons.Default.MicOff else Icons.Default.Mic,
                contentDescription = "Mute",
                tint = if (uiState.isCallMuted) Color.Black else Color.White
              )
            }

            // Speaker Button
            IconButton(
              onClick = { viewModel.toggleSpeaker() },
              modifier = Modifier
                .size(54.dp)
                .background(if (uiState.isSpeakerOn) Color.White else Color.White.copy(alpha = 0.2f), CircleShape)
            ) {
              Icon(
                if (uiState.isSpeakerOn) Icons.Default.VolumeUp else Icons.Default.VolumeDown,
                contentDescription = "Speaker",
                tint = if (uiState.isSpeakerOn) Color.Black else Color.White
              )
            }

            if (isVideo) {
              // Camera Off/On
              IconButton(
                onClick = { viewModel.toggleVideoCamera() },
                modifier = Modifier
                  .size(54.dp)
                  .background(if (uiState.isVideoCameraOff) Color.White else Color.White.copy(alpha = 0.2f), CircleShape)
              ) {
                Icon(
                  if (uiState.isVideoCameraOff) Icons.Default.VideocamOff else Icons.Default.Videocam,
                  contentDescription = "Camera Toggle",
                  tint = if (uiState.isVideoCameraOff) Color.Black else Color.White
                )
              }

              // Flip Camera
              IconButton(
                onClick = { viewModel.flipCamera() },
                modifier = Modifier
                  .size(54.dp)
                  .background(Color.White.copy(alpha = 0.2f), CircleShape)
              ) {
                Icon(Icons.Default.FlipCameraAndroid, contentDescription = "Flip Camera", tint = Color.White)
              }
            }

            // End Call Button
            IconButton(
              onClick = { viewModel.endCall() },
              modifier = Modifier
                .size(64.dp)
                .background(CallRed, CircleShape)
                .testTag("end_call_button")
            ) {
              Icon(Icons.Default.CallEnd, contentDescription = "End Call", tint = Color.White, modifier = Modifier.size(30.dp))
            }
          }
        }
      }
    }
  }
}
