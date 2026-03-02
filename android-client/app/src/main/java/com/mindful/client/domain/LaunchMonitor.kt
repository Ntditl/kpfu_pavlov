package com.mindful.client.domain

/**
 * Platform adapter for foreground app detection.
 * Real implementation should use UsageStatsManager/Accessibility depending on chosen strategy.
 */
interface LaunchMonitor {
    fun start(onForegroundPackageChanged: (String) -> Unit)
    fun stop()
}
