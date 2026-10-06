package ai.voxsign.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.viewmodel.compose.viewModel
import ai.voxsign.android.data.Backend
import ai.voxsign.android.session.AppViewModel
import ai.voxsign.android.ui.VoxSignApp
import ai.voxsign.android.ui.VoxSignTheme

/** Single-activity host. Wires the ViewModel (with a Backend built from BuildConfig) into the UI. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val backend = Backend(BuildConfig.BACKEND_BASE_URL)
        setContent {
            VoxSignTheme {
                val vm: AppViewModel = viewModel(
                    factory = object : androidx.lifecycle.ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                            AppViewModel(backend) as T
                    }
                )
                VoxSignApp(vm)
            }
        }
    }
}
