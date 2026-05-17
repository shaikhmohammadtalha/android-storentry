package com.shaikh.storentry

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.shaikh.storentry.presentation.navigation.AppNavGraph
import com.shaikh.storentry.presentation.theme.AppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install OS-level splash screen before super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Skill: edge-to-edge — Step 2.
        // enableEdgeToEdge from ComponentActivity handles status/nav bar icon colors automatically.
        enableEdgeToEdge()

        // Skill: Navigation Bar Contrast — prevent system from adding a translucent scrim
        // behind our NavigationBar so its background extends to the bottom edge.
        if (android.os.Build.VERSION.SDK_INT >= 29) {
            window.isNavigationBarContrastEnforced = false
        }

        setContent {
            AppTheme {
                // No Surface wrapper here — it would fight with edge-to-edge insets.
                // Each Scaffold handles its own background color.
                AppNavGraph()
            }
        }
    }
}
