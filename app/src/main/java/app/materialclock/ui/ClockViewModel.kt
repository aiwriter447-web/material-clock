package app.materialclock.ui

import android.app.Application
import android.os.SystemClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameMillis
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import app.materialclock.alarm.AlarmScheduler
import app.materialclock.alarm.AlarmService
import app.materialclock.alarm.LiveUpdateService
import app.materialclock.alarm.Notifications
import app.materialclock.alarm.TimerScheduler
import app.materialclock.core.Alarm
import app.materialclock.core.AlarmGroup
import app.materialclock.core.ClockTimer
import app.materialclock.core.Lap
import app.materialclock.core.Stopwatch
import app.materialclock.core.TimerPreset
import app.materialclock.core.TimerState
import app.materialclock.core.WorldCity
import app.materialclock.data.ClockSettings
import app.materialclock.data.ClockStore
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId

@Composable
fun rememberElapsedTicker(active: Boolean = true): State<Long> =
    produceState(initialValue = SystemClock.elapsedRealtime(), active) {
        while (active) {
            withFrameMillis { }
            value = SystemClock.elapsedRealtime()
        }
    }

@Composable
fun rememberWallTicker(periodMillis: Long = 1000L): State<Long> =
    produceState(initialValue = System.currentTimeMillis(), periodMillis) {
        while (true) {
            val now = System.currentTimeMillis()
            value = now
            delay(periodMillis - (now % periodMillis))
        }
    }

class ClockViewModel(app: Application) : AndroidViewModel(app) {

    private val store = ClockStore(app)
    private val ctx get() = getApplication<Application>()

    private val _isLoaded = MutableStateFlow(false)
    val isLoaded: StateFlow<Boolean> = _isLoaded

    val settings: StateFlow<ClockSettings> =
        store.settings.stateIn(viewModelScope, SharingStarted.Eagerly, ClockSettings())
        
    val alarms: StateFlow<List<Alarm>> =
        store.alarms
            .map { list ->
                list.sortedWith(
                    compareByDescending<Alarm> { it.pinnedAt != null }
                        .thenByDescending { it.pinnedAt ?: 0L }
                        .thenByDescending { it.enabled }
                        .thenBy { it.time }
                )
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            
    val groups: StateFlow<List<AlarmGroup>> =
        store.groups.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
        
    val cities: StateFlow<List<WorldCity>> =
        store.cities
            .map { list ->
                list.sortedWith(
                    compareByDescending<WorldCity> { it.pinnedAt != null }
                        .thenByDescending { it.pinnedAt ?: 0L }
                        .thenBy { it.city }
                )
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
            
    val timer: StateFlow<ClockTimer?> =
        store.timer.stateIn(viewModelScope, SharingStarted.Eagerly, null)
    val stopwatch: StateFlow<Stopwatch> =
        store.stopwatch.stateIn(viewModelScope, SharingStarted.Eagerly, Stopwatch())
    val presets: StateFlow<List<TimerPreset>> =
        store.presets.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val homeZone: StateFlow<ZoneId> = settings
        .map { it.world.homeZoneOverride }
        .distinctUntilChanged()
        .map { override -> override?.let { runCatching { ZoneId.of(it) }.getOrNull() } ?: ZoneId.systemDefault() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ZoneId.systemDefault())

    init {
        viewModelScope.launch {
            store.settings.first()
            _isLoaded.value = true
            Notifications.ensureChannels(ctx)
            AlarmScheduler.scheduleAll(ctx, store.alarmsNow())
        }
    }

    fun updateSettings(block: (ClockSettings) -> ClockSettings) {
        viewModelScope.launch { store.update(block) }
    }

    fun toggleAlarm(id: Long) = viewModelScope.launch {
        val next = store.alarmsNow().map {
            if (it.id == id) it.copy(enabled = !it.enabled, snoozedUntilMillis = null) else it
        }
        store.putAlarms(next)
        next.firstOrNull { it.id == id }?.let { a ->
            if (a.enabled) AlarmScheduler.schedule(ctx, a) else AlarmScheduler.cancel(ctx, id)
        }
    }

    fun saveAlarm(draft: Alarm) = viewModelScope.launch {
        val id = if (draft.id == 0L) store.nextId() else draft.id
        val alarm = draft.copy(id = id, snoozedUntilMillis = null)
        val current = store.alarmsNow()
        val next = if (current.any { it.id == id }) {
            current.map { if (it.id == id) alarm else it }
        } else {
            current + alarm
        }
        store.putAlarms(next)
        AlarmScheduler.schedule(ctx, alarm)
    }

    fun deleteAlarm(id: Long) = viewModelScope.launch {
        AlarmScheduler.cancel(ctx, id)
        store.putAlarms(store.alarmsNow().filterNot { it.id == id })
    }

    fun togglePinAlarm(id: Long) = viewModelScope.launch {
        val next = store.alarmsNow().map {
            if (it.id == id) {
                it.copy(pinnedAt = if (it.pinnedAt != null) null else System.currentTimeMillis())
            } else it
        }
        store.putAlarms(next)
    }

    fun deleteSelectedAlarms(alarmIds: Set<Long>, groupIds: Set<Long>) = viewModelScope.launch {
        alarmIds.forEach { AlarmScheduler.cancel(ctx, it) }
        store.putAlarms(store.alarmsNow().filterNot { it.id in alarmIds || it.groupId in groupIds })
        store.putGroups(store.groupsNow().filterNot { it.id in groupIds })
    }

    fun setAlarmsEnabledState(alarmIds: Set<Long>, groupIds: Set<Long>, enabled: Boolean) = viewModelScope.launch {
        val next = store.alarmsNow().map { alarm ->
            if (alarm.id in alarmIds || alarm.groupId in groupIds) {
                alarm.copy(enabled = enabled, snoozedUntilMillis = null)
            } else alarm
        }
        store.putAlarms(next)
        next.filter { it.id in alarmIds || it.groupId in groupIds }.forEach { a ->
            if (a.enabled) AlarmScheduler.schedule(ctx, a) else AlarmScheduler.cancel(ctx, a.id)
        }
    }

    fun ungroupSelectedAlarms(alarmIds: Set<Long>, groupIds: Set<Long>) = viewModelScope.launch {
        val next = store.alarmsNow().map { alarm ->
            if (alarm.id in alarmIds || alarm.groupId in groupIds) {
                alarm.copy(groupId = null)
            } else alarm
        }
        store.putAlarms(next)
    }

    fun addGroup(name: String) = viewModelScope.launch {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return@launch
        store.putGroups(store.groupsNow() + AlarmGroup(id = store.nextId(), name = trimmed))
    }

    fun renameGroup(id: Long, name: String) = viewModelScope.launch {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return@launch
        store.putGroups(store.groupsNow().map { if (it.id == id) it.copy(name = trimmed) else it })
    }

    fun deleteGroup(id: Long) = viewModelScope.launch {
        store.putGroups(store.groupsNow().filterNot { it.id == id })
        store.putAlarms(store.alarmsNow().map { if (it.groupId == id) it.copy(groupId = null) else it })
    }

    fun setAlarmGroup(alarmId: Long, groupId: Long?) = viewModelScope.launch {
        store.putAlarms(store.alarmsNow().map { if (it.id == alarmId) it.copy(groupId = groupId) else it })
    }

    fun toggleGroup(groupId: Long, enabled: Boolean) = viewModelScope.launch {
        val next = store.alarmsNow().map {
            if (it.groupId == groupId) it.copy(enabled = enabled, snoozedUntilMillis = null) else it
        }
        store.putAlarms(next)
        next.filter { it.groupId == groupId }.forEach { a ->
            if (a.enabled) AlarmScheduler.schedule(ctx, a) else AlarmScheduler.cancel(ctx, a.id)
        }
    }

    fun addCity(city: WorldCity) = viewModelScope.launch {
        val current = store.cities.first()
        if (current.none { it.zone == city.zone }) store.putCities(current + city)
    }

    fun removeCity(zone: ZoneId) = viewModelScope.launch {
        store.putCities(store.cities.first().filterNot { it.zone == zone })
    }

    fun deleteSelectedCities(zones: Set<ZoneId>) = viewModelScope.launch {
        store.putCities(store.cities.first().filterNot { it.zone in zones })
    }

    fun togglePinCity(zone: ZoneId) = viewModelScope.launch {
        val current = store.cities.first()
        val next = current.map {
            if (it.zone == zone) {
                it.copy(pinnedAt = if (it.pinnedAt != null) null else System.currentTimeMillis())
            } else it
        }
        store.putCities(next)
    }

    var timerDigits: String by mutableStateOf("")
        private set

    val draftDuration: Duration
        get() {
            val d = timerDigits.padStart(6, '0')
            return Duration.ofHours(d.substring(0, 2).toLong())
                .plusMinutes(d.substring(2, 4).toLong())
                .plusSeconds(d.substring(4, 6).toLong())
        }

    fun pressDigit(c: Char) {
        if (timerDigits.length >= 6) return
        if (timerDigits.isEmpty() && c == '0') return
        timerDigits += c
    }

    fun backspace() {
        timerDigits = timerDigits.dropLast(1)
    }

    fun windToMinutes(minutes: Int) {
        timerDigits = if (minutes <= 0) "" else "%d%02d00".format(minutes / 60, minutes % 60).trimStart('0')
    }

    fun startTimer(label: String = "Timer") = viewModelScope.launch {
        val total = draftDuration
        if (total.isZero) return@launch
        val t = ClockTimer(
            id = store.nextId(),
            label = label,
            total = total,
            state = TimerState.RUNNING,
            deadlineElapsedMillis = SystemClock.elapsedRealtime() + total.toMillis(),
        )
        timerDigits = ""
        store.putTimer(t)
        TimerScheduler.sync(ctx, t)
        LiveUpdateService.ensureRunning(ctx)
    }

    fun startPreset(preset: TimerPreset) = viewModelScope.launch {
        if (preset.totalSeconds <= 0) return@launch
        val t = ClockTimer(
            id = store.nextId(),
            label = preset.name,
            total = Duration.ofSeconds(preset.totalSeconds.toLong()),
            state = TimerState.RUNNING,
            deadlineElapsedMillis = SystemClock.elapsedRealtime() + preset.totalSeconds * 1000L,
        )
        store.putTimer(t)
        TimerScheduler.sync(ctx, t)
        LiveUpdateService.ensureRunning(ctx)
    }

    fun savePreset(preset: TimerPreset) = viewModelScope.launch {
        val current = presets.value
        val next = if (preset.id == 0L) {
            current + preset.copy(id = store.nextId())
        } else {
            current.map { if (it.id == preset.id) preset else it }
        }
        store.putPresets(next)
    }

    fun deletePreset(id: Long) = viewModelScope.launch {
        store.putPresets(presets.value.filterNot { it.id == id })
    }

    fun pauseOrResumeTimer() = viewModelScope.launch {
        val t = store.timerNow() ?: return@launch
        val now = SystemClock.elapsedRealtime()
        val next = when (t.state) {
            TimerState.RUNNING -> t.copy(state = TimerState.PAUSED, pausedRemaining = t.remaining(now))
            else -> t.copy(state = TimerState.RUNNING, deadlineElapsedMillis = now + t.pausedRemaining.toMillis())
        }
        store.putTimer(next)
        TimerScheduler.sync(ctx, next)
        if (next.state == TimerState.RUNNING) LiveUpdateService.ensureRunning(ctx)
    }

    fun addTenSeconds() = viewModelScope.launch {
        val t = store.timerNow() ?: return@launch
        val next = when (t.state) {
            TimerState.RUNNING -> t.copy(
                total = t.total.plusSeconds(10),
                deadlineElapsedMillis = t.deadlineElapsedMillis + 10_000,
            )
            else -> t.copy(total = t.total.plusSeconds(10), pausedRemaining = t.pausedRemaining.plusSeconds(10))
        }
        store.putTimer(next)
        TimerScheduler.sync(ctx, next)
    }

    fun cancelTimer() = viewModelScope.launch {
        AlarmService.stop(ctx)
        store.putTimer(null)
        TimerScheduler.sync(ctx, null)
    }

    fun toggleStopwatch() = viewModelScope.launch {
        val sw = store.stopwatch.first()
        val now = SystemClock.elapsedRealtime()
        val next = if (sw.running) {
            sw.copy(running = false, accumulated = sw.elapsed(now))
        } else {
            sw.copy(running = true, startedAtElapsed = now)
        }
        store.putStopwatch(next)
        Notifications.showStopwatch(ctx, next)
        if (next.running) LiveUpdateService.ensureRunning(ctx)
    }

    fun lap() = viewModelScope.launch {
        val sw = store.stopwatch.first()
        if (!sw.running) return@launch
        val total = sw.elapsed(SystemClock.elapsedRealtime())
        val previous = sw.laps.firstOrNull()?.total ?: Duration.ZERO
        val next = sw.copy(laps = listOf(Lap(sw.laps.size + 1, total.minus(previous), total)) + sw.laps)
        store.putStopwatch(next)
        Notifications.showStopwatch(ctx, next)
    }

    fun resetStopwatch() = viewModelScope.launch {
        store.putStopwatch(Stopwatch())
        Notifications.hideStopwatch(ctx)
    }

    fun blankAlarm(): Alarm = Alarm(
        id = 0L,
        time = LocalTime.now().plusHours(1).withMinute(0).withSecond(0).withNano(0),
        label = "",
        days = emptySet<DayOfWeek>(),
    )
}
