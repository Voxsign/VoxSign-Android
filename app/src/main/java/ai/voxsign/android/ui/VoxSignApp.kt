package ai.voxsign.android.ui

import ai.voxsign.android.R
import ai.voxsign.android.data.ConnState
import ai.voxsign.android.session.AppViewModel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material.icons.filled.Check
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Root scaffold: top bar (menu + machine status) over the chat screen, with a slide-out session drawer. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoxSignApp(vm: AppViewModel) {
    var drawerOpen by remember { mutableStateOf(false) }
    var machineSheetOpen by remember { mutableStateOf(false) }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    MachineStatusHeader(vm, onClick = { machineSheetOpen = true })
                },
                navigationIcon = {
                    IconButton(onClick = { drawerOpen = true }) {
                        Icon(Icons.Default.Menu, contentDescription = stringResource(R.string.sessions_menu_description))
                    }
                }
            )
            ChatScreen(vm, Modifier.weight(1f))
        }

        if (drawerOpen) {
            SessionListDrawer(vm, onClose = { drawerOpen = false })
        }
        if (machineSheetOpen) {
            MachinePickerSheet(vm, onDismiss = { machineSheetOpen = false })
        }
    }
}

/** Top-bar pill: green dot = connected, red = offline; tap opens the machine picker. */
@Composable
private fun MachineStatusHeader(vm: AppViewModel, onClick: () -> Unit) {
    val conn by vm.conn.collectAsState()
    val machine = vm.currentMachine()
    val dotColor = when (conn) {
        ConnState.ONLINE -> Color(0xFF2ECC71)
        ConnState.OFFLINE -> Color(0xFFFF3B30)
        ConnState.UNKNOWN -> Color(0xFF9AA0A6)
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Box(
            Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(Modifier.width(8.dp))
        Text(machine.name, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.width(6.dp))
        Text(
            when (conn) {
                ConnState.ONLINE -> stringResource(R.string.status_connected)
                ConnState.OFFLINE -> stringResource(R.string.status_offline)
                ConnState.UNKNOWN -> stringResource(R.string.status_checking)
            },
            fontSize = 11.sp,
            color = Color(0xFF8A8A8E)
        )
    }
}

/** Bottom sheet listing machines; default "VoxSign Cloud". Tap to switch. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MachinePickerSheet(vm: AppViewModel, onDismiss: () -> Unit) {
    val machines by vm.machines.collectAsState()
    val currentId by vm.currentMachineId.collectAsState()
    val conn by vm.conn.collectAsState()

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(bottom = 24.dp)) {
            Text(
                stringResource(R.string.choose_machine),
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                modifier = Modifier.padding(16.dp, 8.dp)
            )
            HorizontalDivider()
            machines.forEach { m ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            vm.selectMachine(m.id)
                            onDismiss()
                        }
                        .padding(16.dp, 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Cloud, contentDescription = null, tint = Color(0xFF5B8DEF))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(m.name, fontSize = 15.sp)
                        Text(m.baseUrl, fontSize = 11.sp, color = Color(0xFF8A8A8E))
                    }
                    if (m.id == currentId && conn == ConnState.ONLINE) {
                        Icon(Icons.Default.Check, contentDescription = stringResource(R.string.selected), tint = Color(0xFF5B8DEF))
                    }
                }
            }
        }
    }
}
