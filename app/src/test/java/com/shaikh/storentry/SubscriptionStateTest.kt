package com.shaikh.storentry

import com.shaikh.storentry.domain.model.SubscriptionState
import com.shaikh.storentry.utils.SubscriptionConfig
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * SubscriptionStateTest — Unit tests validating critical subscription configurations,
 * limitations, and modeling.
 */
class SubscriptionStateTest {

    @Test
    fun testFreeTierLimitsAreCorrect() {
        assertEquals(100, SubscriptionConfig.FREE_PRODUCT_LIMIT)
        assertEquals(5, SubscriptionConfig.FREE_REMINDER_LIMIT)
        assertEquals("premium", SubscriptionConfig.ENTITLEMENT_PREMIUM)
    }

    @Test
    fun testSubscriptionStateMapping() {
        val freeState = SubscriptionState(
            isPremium = false,
            activeEntitlements = emptyList(),
            expirationDateMillis = null
        )
        assertFalse(freeState.isPremium)
        assertTrue(freeState.activeEntitlements.isEmpty())
        assertEquals(null, freeState.expirationDateMillis)

        val premiumState = SubscriptionState(
            isPremium = true,
            activeEntitlements = listOf(SubscriptionConfig.ENTITLEMENT_PREMIUM),
            expirationDateMillis = 1716298800000L
        )
        assertTrue(premiumState.isPremium)
        assertEquals(1, premiumState.activeEntitlements.size)
        assertEquals(SubscriptionConfig.ENTITLEMENT_PREMIUM, premiumState.activeEntitlements.first())
        assertEquals(1716298800000L, premiumState.expirationDateMillis)
    }
}
