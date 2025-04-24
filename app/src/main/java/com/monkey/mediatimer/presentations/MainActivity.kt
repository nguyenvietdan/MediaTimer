package com.monkey.mediatimer.presentations

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.monkey.mediatimer.presentations.navigation.MediaTimerNavigation
import com.monkey.mediatimer.presentations.theme.MediaTimerTheme
import com.monkey.mediatimer.presentations.viewmodel.MediaViewModel
import com.monkey.mediatimer.utils.isNotificationServiceEnabled
import com.monkey.mediatimer.utils.openNotificationSettings
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val TAG = "MainActivity"

    private val mediaViewModel: MediaViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (!isNotificationServiceEnabled(this)) {
            Log.d(TAG, "Notification listener service is NOT enabled. Prompting user.")
            // Show a dialog or message explaining why you need notification access
            // and provide a button to call openNotificationSettings(this)
            // e.g. using an AlertDialog:

            AlertDialog.Builder(this)
                .setTitle("Notification Access Required")
                .setMessage("This app needs access to notifications to function correctly. Please enable the service in settings.")
                .setPositiveButton("Go to Settings") { dialog, which ->
                    openNotificationSettings(this)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
        setContent {
            MediaTimerTheme {
                Surface(modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background) {
                    MediaTimerNavigation(mediaViewModel)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        mediaViewModel.checkWriteSettingsPermission()
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MediaTimerTheme {
        Greeting("Android")
    }
}