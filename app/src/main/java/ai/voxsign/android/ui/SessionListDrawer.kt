package ai.voxsign.android.ui

import ai.voxsign.android.R
import ai.voxsign.android.data.ChatSession
import ai.voxsign.android.data.SessionDefaults
import ai.voxsign.android.session.AppViewModel
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Slide-out session drawer.
 * Lists sessions sorted by updatedAt descending; tap to switch; "New" creates a session titled
 * "New Chat"; trash deletes (silently ignored when only one remains).
 *
 * RTL behavior (DESIGN.md §9.3): the panel slides from the right in ar locale. This is achieved
 * automatically because the Box defaults to TopStart alignment — in RTL, Start = right.
 * The header Row also auto-mirrors: Close lands on the screen-edge side, Add on the inner side.
 */
@Composable
fun SessionListDrawer(vm: AppViewModel, onClose: () -> Unit) {
    val sessions by vm.sessions.collectAsState()
    val currentId by vm.currentSessionId.collectAsState()
    val sorted = sessions.sortedByDescending { it.updatedAt }

    Box(Modifier.fillMaxSize()) {
        // Scrim
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable { onClose() }
        )
        // Panel — aligned to TopStart (left in LTR, right in RTL) automatically.
        Column(
            Modifier
                .width(300.dp)
                .fillMaxHeight()
                .background(Color(0xFFF7F7F8))
        ) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(12.dp, 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close))
                }
                Spacer(Modifier.weight(1f))
                Text(stringResource(R.string.sessions_title), fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = { vm.newSession(); onClose() }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_session))
                }
            }
            HorizontalDivider()
            LazyColumn(Modifier.weight(1f)) {
                items(sorted, key = { it.id }) { s ->
                    SessionRow(
                        session = s,
                        isCurrent = s.id == currentId,
                        onTap = { vm.switchSession(s.id); onClose() },
                        onDelete = { vm.deleteSession(s.id) }
                    )
                }
                if (sorted.isEmpty()) {
                    item {
                        Column(
                            Modifier
                                .fillMaxWidth()
                                .padding(40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(stringResource(R.string.no_sessions), color = Color(0xFF8A8A8E), fontSize = 14.sp)
                            Text(
                                stringResource(R.string.no_sessions_hint),
                                color = Color(0xFFAEAEB2),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SessionRow(session: ChatSession, isCurrent: Boolean, onTap: () -> Unit, onDelete: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable { onTap() }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(5.dp)
                .clip(CircleShape)
                .background(if (isCurrent) Color(0xFF5B8DEF) else Color.Transparent)
        )
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            // Localize the default "New Chat" sentinel; real titles are stored verbatim.
            val displayTitle = if (session.title == SessionDefaults.NEW_CHAT_TITLE || session.title.isEmpty()) {
                stringResource(R.string.new_chat)
            } else {
                session.title
            }
            Text(
                displayTitle,
                fontSize = 15.sp,
                fontWeight = if (isCurrent) FontWeight.SemiBold else FontWeight.Normal,
                maxLines = 1
            )
            Text(relativeTime(session.updatedAt), fontSize = 11.sp, color = Color(0xFF8A8A8E))
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.delete), tint = Color(0xFFC7C7CC))
        }
    }
}

private fun relativeTime(ts: Long): String {
    // Western digits by default (DESIGN.md §9.2); use system locale for date formatting.
    val f = SimpleDateFormat("MM-dd HH:mm", Locale.US)
    return f.format(Date(ts))
}
