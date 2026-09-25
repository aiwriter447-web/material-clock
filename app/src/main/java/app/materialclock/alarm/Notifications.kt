package app.materialclock.alarm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.getSystemService
import app.materialclock.MainActivity
import app.materialclock.R
import app.materialclock.core.ClockTimer
import app.materialclock.core.Stopwatch
import app.materialclock.core.TimerState
import app.materialclock.core.clockFormat
import app.materialclock.core.parts
import app.materialclock.core.stopwatchParts
import java.time.Duration
import java.time.LocalDateTime

/**
 * Every channel and every notification the app posts.
 *
 * ## The timer and stopwatch notifications carry no service
 *
 * They do not need one. A countdown is a *deadline*, not a process: the remaining time is
 * `deadline − elapsedRealtime()`, correct whether or not this app has run since. So the ongoing
 * notification is posted once with `setUsesChronometer` and the system renders the ticking digits
 * itself, at zero cost to the app, for as long as the notification exists. Notifications outlive
 * the process that posted them. A foreground service would burn a wakelock to recompute a number
 * the platform can derive from a timestamp.