package ai.voxsign.android.ui

import ai.voxsign.android.data.ConnState
import ai.voxsign.android.data.StoredMessage
import ai.voxsign.android.session.AppViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Chat page: scrollable bubble list + the bottom hold-to-talk input bar. */
@Composable
fun ChatScreen(vm: AppViewModel, modifier: Modifier = Modifier) {
    val sessions by vm.sessions.collectAsState()
    val currentId by vm.currentSessionId.collectAsState()
    val session = sessions.firstOrNull { it.id == currentId } ?: sessions.firstOrNull()
    val conn by vm.conn.collectAsState()
    val listState = rememberLazyListState()

    LaunchedEffect(session?.messages?.size) {
        session?.messages?.let {
            if (it.isNotEmpty()) listState.animateScrollToItem(it.size - 1)
        }
    }

    Column(modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val msgs = session?.messages ?: emptyList()
            if (msgs.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(top = 80.dp), contentAlignment = Alignment.Center) {
                        Text("Start a conversation", color = Color(0xFFAEAEB2), fontSize = 14.sp)
                    }
                }
            }
            items(msgs, key = { it.id }) { m -> MessageBubble(m) }
        }

        if (conn == ConnState.ONLINE) {
            InputBar(vm)
        } else {
            OfflineBar()
        }
    }
}

/** One chat bubble: user right-aligned (dark), harness left-aligned (light gray). */
@Composable
private fun MessageBubble(m: StoredMessage) {
    val isUser = m.role == "user"
    Row(
        Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (!isUser) {
            Box(Modifier.width(6.dp))
        }
        Column(
            Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(if (isUser) Color(0xFF1A1A1A) else Color(0xFFECECEF))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                m.text,
                color = if (isUser) Color.White else Color(0xFF1A1A1A),
                fontSize = 15.sp
            )
            if (m.fromVoice && m.voiceSeconds != null) {
                Text(
                    "🎙 ${m.voiceSeconds}s",
                    fontSize = 11.sp,
                    color = if (isUser) Color.White.copy(alpha = 0.7f) else Color(0xFF8A8A8E)
                )
            }
        }
    }
}

@Composable
private fun OfflineBar() {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(12.dp, 10.dp)
            .clip(RoundedCornerShape(50))
            .background(Color(0xFFECECEF))
            .padding(vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text("Offline · tap the machine name above to switch", color = Color(0xFF8A8A8E), fontSize = 14.sp)
    }
}
