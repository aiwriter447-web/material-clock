package app.materialclock.alarm

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Serializes notification state changes coming from notification actions and
 * LiveUpdateService.
 *
 * Without this lock, LiveUpdateService can read an old timer/stopwatch state,
 * while ClockActionReceiver has already written a new state, and then repost
 * the old notification over the user's action.
 */
object LiveUpdateCoordinator {

    private val mutex = Mutex()

    suspend fun <T> withNotificationLock(
        block: suspend () -> T,
    ): T = mutex.withLock {
        block()
    }
}