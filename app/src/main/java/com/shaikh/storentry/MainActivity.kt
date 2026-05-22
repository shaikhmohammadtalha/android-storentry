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
    private val pendingProductId = androidx.compose.runtime.mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        // Install OS-level splash screen before super.onCreate
        installSplashScreen()

        super.onCreate(savedInstanceState)

        // Handle intent if launched from notification
        handleIntent(intent)

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
                AppNavGraph(
                    pendingProductId = pendingProductId.value,
                    onNotificationHandled = { pendingProductId.value = null }
                )
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: android.content.Intent?) {
        val productId = intent?.getStringExtra("PRODUCT_ID")
        if (!productId.isNullOrBlank()) {
            pendingProductId.value = productId
        }
    }
}
