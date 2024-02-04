package com.moritoui.recordaccel

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.moritoui.recordaccel.navigation.Navigation
import com.moritoui.recordaccel.ui.theme.RecordAccelTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            RecordAccelTheme {
                Navigation(
                    modifier = Modifier.safeDrawingPadding()
                )
            }
        }
    }
}
