package com.riynk.rymora

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.riynk.rymora.ui.RymoraApp
import com.riynk.rymora.ui.theme.RymoraTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RymoraTheme {
                RymoraApp()
            }
        }
    }
}
