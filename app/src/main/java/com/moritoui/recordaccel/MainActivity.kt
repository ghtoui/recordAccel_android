package com.moritoui.recordaccel

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.moritoui.recordaccel.navigation.Navigation
import com.moritoui.recordaccel.service.ForegroundState
import com.moritoui.recordaccel.service.SensorDataService
import com.moritoui.recordaccel.ui.theme.RecordAccelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val intent = Intent(applicationContext, SensorDataService::class.java).also {
            it.action = ForegroundState.START.name
        }
        // 多分今はActivityResultで行う？
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                } else {
                }
            }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }
        startForegroundService(intent)

        setContent {
            RecordAccelTheme {
                Surface(
                    color = MaterialTheme.colorScheme.background,
                ) {
                    Navigation(
                        modifier = Modifier.safeDrawingPadding(),
                    )
                }
            }
        }
    }
}
