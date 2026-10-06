package ai.voxsign.android.ui

import ai.voxsign.android.R
import ai.voxsign.android.session.AppViewModel
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlin.math.sin

/**
 * Doubao-style bottom input bar.
 *  - Default: big dark "Hold to talk" capsule (56dp).
 *  - Press & hold -> full-width waveform overlay (red animated bars) + semi-transparent scrim.
 *  - Release -> sends a canned voice transcript; slide far enough in the cancel direction -> cancels.
 *    In LTR the cancel gesture is slide-up; per DESIGN.md §9.3 it mirrors to slide-down in RTL.
 *  - Tap the keyboard circle -> text-input mode with a send arrow.
 */
@Composable
fun InputBar(vm: AppViewModel) {
    var textMode by remember { mutableStateOf(false) }
    var recording by remember { mutableStateOf(false) }
    var cancelling by remember { mutableStateOf(false) }
    val input by vm.inputText.collectAsState()
    val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

    Column(Modifier.fillMaxWidth().background(Color(0xFFF2F2F4))) {
        // Recording overlay: waveform + caption
        if (recording) {
            RecordingOverlay(cancelling)
        }

        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (textMode) {
                TextField(
                    value = input,
                    onValueChange = { vm.onInputChange(it) },
                    placeholder = { Text(stringResource(R.string.message_placeholder), fontSize = 15.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(18.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFECECEF),
                        unfocusedContainerColor = Color(0xFFECECEF),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent
                    )
                )
                IconButton(onClick = { vm.sendText() }, enabled = input.isNotBlank()) {
                    Icon(Icons.Default.ArrowUpward, contentDescription = stringResource(R.string.send), tint = Color(0xFF5B8DEF))
                }
            } else {
                // Big hold-to-talk capsule
                Box(
                    Modifier
                        .weight(1f)
                        .height(56.dp)
                        .clip(RoundedCornerShape(50))
                        .background(Color(0xFF1A1A1A))
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragStart = {
                                    recording = true
                                    cancelling = false
                                },
                                onDragEnd = {
                                    if (!cancelling) {
                                        // Release to send: canned mock transcript (no real mic in MVP).
                                        vm.sendVoice("voice message", heldSeconds = 2)
                                    }
                                    recording = false
                                    cancelling = false
                                },
                                onDragCancel = {
                                    recording = false
                                    cancelling = false
                                },
                                onDrag = { change, dragAmount ->
                                    change.consume()
                                    // Cancel gesture direction mirrors in RTL:
                                    //   LTR: slide up (negative Y) past threshold -> cancel
                                    //   RTL: slide down (positive Y) past threshold -> cancel
                                    val shouldCancel = if (isRtl) {
                                        dragAmount.y > 4f
                                    } else {
                                        dragAmount.y < -4f
                                    }
                                    if (shouldCancel) cancelling = true
                                }
                            )
                        }
                        // Light tap (no drag) also starts/stops a short record
                        .pointerInput(Unit) {
                            detectTapGestures(
                                onPress = {
                                    recording = true
                                    cancelling = false
                                    val released = tryAwaitRelease()
                                    if (released && !cancelling) {
                                        vm.sendVoice("voice message", heldSeconds = 1)
                                    }
                                    recording = false
                                    cancelling = false
                                }
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.hold_to_talk), color = Color.White, fontSize = 17.sp)
                }
            }

            // Keyboard toggle
            CircleIconButton(onClick = { textMode = !textMode }) {
                Icon(Icons.Default.Keyboard, contentDescription = stringResource(R.string.toggle_keyboard), tint = Color(0xFF8A8A8E))
            }
        }
    }
}

@Composable
private fun CircleIconButton(onClick: () -> Unit, content: @Composable () -> Unit) {
    Box(
        Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(Color(0xFFE4E4E8))
            .pointerInput(Unit) { detectTapGestures(onTap = { onClick() }) },
        contentAlignment = Alignment.Center
    ) { content() }
}

/**
 * Full-width held-state overlay: animated red bars + caption. Semi-transparent scrim.
 * The Row automatically mirrors in RTL, so the waveform fills from the leading edge
 * (right side in RTL) per DESIGN.md §9.3.
 */
@Composable
private fun RecordingOverlay(cancelling: Boolean) {
    val transition = rememberInfiniteTransition()
    val phase by transition.animateFloat(
        initialValue = 0f, targetValue = (2.0 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(tween(900, easing = LinearEasing))
    )
    val bars = 20
    Row(
        Modifier
            .fillMaxWidth()
            .background(Color.White.copy(alpha = 0.15f))
            .padding(vertical = 24.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(bars) { i ->
            val breath = 0.5f + 0.5f * sin(phase + i * 0.55f)
            val h = (12 + breath * 36).dp
            Box(
                Modifier
                    .padding(horizontal = 2.dp)
                    .width(6.dp)
                    .height(h)
                    .clip(RoundedCornerShape(3.dp))
                    .background(Color(0xFFFF3B30))
            )
        }
    }
    Box(
        Modifier.fillMaxWidth().padding(bottom = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            if (cancelling) stringResource(R.string.release_to_cancel)
            else stringResource(R.string.release_to_send_slide_up),
            color = Color(0xFFFF3B30),
            fontSize = 14.sp
        )
    }
}
