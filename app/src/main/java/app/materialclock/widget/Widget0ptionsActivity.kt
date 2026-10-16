package app.materialclock.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import app.materialclock.core.Alarm
import app.materialclock.core.WorldCity
import app.materialclock.data.ClockSettings
import app.materialclock.data.ClockStore
import app.materialclock.data.WidgetConfig
import app.materialclock.ui.theme.ClockTheme
import kotlinx.coroutines.launch

/**
 * The settings screen for the World Clock and Alarm widgets: which cities or which alarm to show,
 * the background, and the colours. It opens when one of those widgets is placed, and again when
 * it is reconfigured.
 *
 * It follows the same rules as [ClockWidgetConfigActivity], which explains them at length:
 * no `launchMode`, `taskAffinity` or translucent theme; `RESULT_CANCELED` before any UI; and on
 * Save the order is persist, redraw, `RESULT_OK`, finish.
 */
class WidgetOptionsActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID,
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID

        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))

        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }

        // Exported, so anyone can launch it with any id: only act on our own two widget kinds.
        val kind = when (WidgetProviders.kindOf(this, appWidgetId)) {
            WidgetKind.WORLD -> WidgetOptionsKind.WORLD
            WidgetKind.ALARM -> WidgetOptionsKind.ALARM
            else -> {
                finish()
                return
            }
        }

        val store = ClockStore(applicationContext)

        setContent {
            val settings by store.settings.collectAsStateWithLifecycle(initialValue = ClockSettings())
            val existing by store.widgetConfig(appWidgetId).collectAsStateWithLifecycle(initialValue = null)
            val cities by store.cities.collectAsStateWithLifecycle(initialValue = emptyList<WorldCity>())
            val alarms by store.alarms.collectAsStateWithLifecycle(initialValue = emptyList<Alarm>())

            ClockTheme(settings.theme) {
                WidgetOptionsScreen(
                    kind = kind,
                    initial = existing ?: WidgetConfig(),
                    settings = settings,
                    cities = cities,
                    alarms = alarms,
                    isNew = existing == null,
                    onCancel = { finish() },
                    onSave = { config -> commit(appWidgetId, kind, config) },
                )
            }
        }
    }

    private fun commit(appWidgetId: Int, kind: WidgetOptionsKind, config: WidgetConfig) {
        lifecycleScope.launch {
            val app = applicationContext
            ClockStore(app).putWidgetConfig(appWidgetId, config)
            val manager = AppWidgetManager.getInstance(app)
            when (kind) {
                WidgetOptionsKind.WORLD -> WorldClockRenderer.update(app, manager, appWidgetId)
                WidgetOptionsKind.ALARM -> AlarmWidgetRenderer.update(app, manager, appWidgetId)
            }
            // The launcher may not have settled the widget's size yet; draw once more shortly.
            WidgetRefresh.again(app, appWidgetId)
            setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
            finish()
        }
    }
}
