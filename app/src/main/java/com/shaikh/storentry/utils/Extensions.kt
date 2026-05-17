package com.shaikh.storentry.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed

/**
 * Context extensions.
 */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

/**
 * Modifier extensions.
 */
fun Modifier.clickableWithRipple(onClick: () -> Unit): Modifier = composed {
    clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = ripple(),
        onClick = onClick
    )
}
/**
 * Format currency to a readable string (e.g., 45K for 45000).
 */
fun formatCurrency(value: Double): String {
    return if (value >= 1000) {
        String.format("%.1fK", value / 1000)
    } else {
        String.format("%.0f", value)
    }
}
