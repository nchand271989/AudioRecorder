package android.obbfile.utils

import java.util.*
import java.util.concurrent.TimeUnit

class AudioDateTime {
    fun getTimeAgo(duration: Long): String? {
        val now = Date()
        val seconds = TimeUnit.MILLISECONDS.toSeconds(now.time - duration)
        val minutes = TimeUnit.MILLISECONDS.toMinutes(now.time - duration)
        val hours = TimeUnit.MILLISECONDS.toHours(now.time - duration)
        val days = TimeUnit.MILLISECONDS.toDays(now.time - duration)
        return when {
            seconds < 60 -> {
                "just now"
            }
            minutes == 1L -> {
                "a minute ago"
            }
            minutes in 2..59 -> {
                "$minutes minutes ago"
            }
            hours == 1L -> {
                "an hour ago"
            }
            hours in 2..23 -> {
                "$hours hours ago"
            }
            days == 1L -> {
                "a day ago"
            }
            else -> {
                "$days days ago"
            }
        }
    }
}