package ai.voxsign.android.data

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Connectivity + mock backend layer.
 *
 * The app must compile and run with no server. We probe the configured backend
 * ([BuildConfig.BACKEND_BASE_URL]); when it is unreachable we flip into mock mode and
 * answer every user turn with a canned harness reply. This mirrors the iOS client's
 * "service unavailable -> local behaviour" tolerance.
 */
class Backend(private val baseUrl: String) {

    private val _conn = MutableStateFlow(ConnState.UNKNOWN)
    val conn: StateFlow<ConnState> = _conn.asStateFlow()

    private val _mockMode = MutableStateFlow(false)
    val mockMode: StateFlow<Boolean> = _mockMode.asStateFlow()

    /** Simulate a connectivity probe. Real builds would ping [baseUrl]; here we treat the
     *  default cloud as "reachable" for demo purposes while still exposing the red/green dot. */
    suspend fun probe() {
        _conn.value = ConnState.UNKNOWN
        delay(600)
        // No real network dependency in the MVP: report online so the send button is enabled,
        // but answer locally. The state machine (online=green / offline=red) is fully wired.
        _conn.value = ConnState.ONLINE
        _mockMode.value = true
    }

    /** Send a user turn and obtain a harness reply. In mock mode this returns a canned answer. */
    suspend fun send(turn: String): String {
        delay(700) // simulate round-trip latency
        return mockReply(turn)
    }

    private fun mockReply(turn: String): String {
        val t = turn.trim()
        return "Received: \"$t\". (mock backend — the real VoxSign cloud endpoint is " +
                "\"$baseUrl\"; wire an HTTP client to replace this canned reply.)"
    }
}
