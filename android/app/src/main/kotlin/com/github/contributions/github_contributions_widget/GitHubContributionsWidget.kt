package com.github.contributions.github_contributions_widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import android.view.View
import android.widget.RemoteViews
import org.json.JSONObject

class GitHubContributionsWidget : AppWidgetProvider() {

    companion object {
        private const val TAG = "GitHubWidget"
        private const val PREFS_NAME = "FlutterSharedPreferences"
        private const val ACTION_WIDGET_TAP = "com.github.contributions.WIDGET_TAP"
        
        private val WIDGET_DATA_KEYS = arrayOf(
            "flutter.widget_contribution_data",
            "widget_contribution_data"
        )
        
        fun performHapticFeedback(context: Context) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                    val vibrator = vibratorManager.defaultVibrator
                    vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator.vibrate(VibrationEffect.createOneShot(10, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator.vibrate(10)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Haptic feedback failed", e)
            }
        }
        
        // Color themes: 0 = Green (default), 1 = Blue, 2 = Yellow, 3 = Material You
        private const val THEME_MATERIAL_YOU = 3
        
        private val GREEN_COLORS = arrayOf(
            Color.parseColor("#2d333b"), // Level 0 - Dark gray (empty days)
            Color.parseColor("#0e4429"), // Level 1 - Dark green
            Color.parseColor("#006d32"), // Level 2 - Medium green
            Color.parseColor("#26a641"), // Level 3 - Light green
            Color.parseColor("#39d353")  // Level 4 - Bright green
        )
        
        private val BLUE_COLORS = arrayOf(
            Color.parseColor("#2d333b"), // Level 0 - Dark gray (empty days)
            Color.parseColor("#1e3a5f"), // Level 1 - Dark blue
            Color.parseColor("#3b6ea5"), // Level 2 - Medium blue
            Color.parseColor("#6b9bd1"), // Level 3 - Light blue
            Color.parseColor("#a8c8e8")  // Level 4 - Bright blue
        )
        
        private val YELLOW_COLORS = arrayOf(
            Color.parseColor("#2d333b"), // Level 0 - Dark gray (empty days)
            Color.parseColor("#5c3d0e"), // Level 1 - Dark amber
            Color.parseColor("#8b5a1b"), // Level 2 - Medium amber
            Color.parseColor("#d4a03a"), // Level 3 - Light amber
            Color.parseColor("#f7c948")  // Level 4 - Bright yellow
        )
        
        // Material You theme - system palette generated from the wallpaper (Android 12+)
        // Passed as resources so the launcher resolves them, following wallpaper changes
        private val MATERIAL_YOU_COLOR_RES = intArrayOf(
            android.R.color.system_neutral2_800, // Level 0 - Muted surface (empty days)
            android.R.color.system_accent1_700,  // Level 1 - Dark accent
            android.R.color.system_accent1_500,  // Level 2 - Medium accent
            android.R.color.system_accent1_300,  // Level 3 - Light accent
            android.R.color.system_accent1_100   // Level 4 - Bright accent
        )
        private const val MATERIAL_YOU_BACKGROUND_RES = android.R.color.system_neutral1_900
        
        private fun getColorsForTheme(theme: Int): Array<Int> {
            return when (theme) {
                1 -> BLUE_COLORS
                2 -> YELLOW_COLORS
                else -> GREEN_COLORS
            }
        }
        
        // Cell layouts from largest to smallest: (layout, cell size, margin around cell)
        private val CELL_SIZES = listOf(
            Triple(R.layout.widget_cell_large, R.dimen.widget_cell_large, R.dimen.widget_cell_large_margin),
            Triple(R.layout.widget_cell_medium, R.dimen.widget_cell_medium, R.dimen.widget_cell_medium_margin),
            Triple(R.layout.widget_cell_compact, R.dimen.widget_cell_compact, R.dimen.widget_cell_compact_margin),
            Triple(R.layout.widget_cell_small, R.dimen.widget_cell_small, R.dimen.widget_cell_small_margin)
        )
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_WIDGET_TAP) {
            // Perform haptic feedback when widget is tapped
            performHapticFeedback(context)
            
            // Launch the app
            val launchIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(launchIntent)
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            try {
                val options = appWidgetManager.getAppWidgetOptions(appWidgetId)
                updateAppWidget(context, appWidgetManager, appWidgetId, options)
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget", e)
            }
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        try {
            updateAppWidget(context, appWidgetManager, appWidgetId, newOptions)
        } catch (e: Exception) {
            Log.e(TAG, "Error on resize", e)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        options: Bundle
    ) {
        val views = RemoteViews(context.packageName, R.layout.github_contributions_widget)
        
        // Set up click intent with haptic feedback
        val tapIntent = Intent(context, GitHubContributionsWidget::class.java).apply {
            action = ACTION_WIDGET_TAP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, tapIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)
        
        // Get widget dimensions
        val minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 200)
        val minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 100)
        
        // Space inside the widget's padding, in pixels
        val resources = context.resources
        val density = resources.displayMetrics.density
        val padding = 2 * resources.getDimensionPixelSize(R.dimen.widget_padding)
        val availableWidth = (minWidth * density).toInt() - padding
        val availableHeight = (minHeight * density).toInt() - padding
        
        // Select the largest cell size whose 7 rows fit the height. Sizes are measured
        // in pixels, rounded the same way the layout rounds them, so the bottom row
        // is never clipped
        val cellSizes = CELL_SIZES.map { (layoutId, size, margin) ->
            layoutId to resources.getDimensionPixelSize(size) + 2 * resources.getDimensionPixelSize(margin)
        }
        val (cellLayoutId, cellSizeWithMargin) = cellSizes.firstOrNull { 7 * it.second <= availableHeight }
            ?: cellSizes.last()
        
        // Calculate weeks based on width and cell size
        val weeksToShow = (availableWidth / cellSizeWithMargin).coerceIn(3, 52)
        
        Log.d(TAG, "Widget size: ${minWidth}x${minHeight}, cellSize: ${cellSizeWithMargin}px, showing $weeksToShow weeks")
        
        // Load contribution data
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        
        var widgetDataJson: String? = null
        for (key in WIDGET_DATA_KEYS) {
            widgetDataJson = prefs.getString(key, null)
            if (widgetDataJson != null) break
        }
        
        // Clear existing grid
        views.removeAllViews(R.id.contribution_grid)
        
        if (widgetDataJson != null) {
            try {
                val widgetData = JSONObject(widgetDataJson)
                val weeksArray = widgetData.getJSONArray("weeks")
                
                // Get color theme from widget data or SharedPreferences
                val colorTheme = if (widgetData.has("colorTheme")) {
                    widgetData.getInt("colorTheme")
                } else {
                    // Fallback: try to read from SharedPreferences directly
                    prefs.getInt("flutter.widget_color_theme", prefs.getInt("widget_color_theme", 0))
                }
                val levelColors = getColorsForTheme(colorTheme)
                val materialYou = colorTheme == THEME_MATERIAL_YOU
                
                // Launchers reuse the existing views on update, so clear the tint when
                // switching away from Material You
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (materialYou) {
                        views.setColorStateList(R.id.widget_container, "setBackgroundTintList", MATERIAL_YOU_BACKGROUND_RES)
                    } else {
                        views.setColorStateList(R.id.widget_container, "setBackgroundTintList", null as ColorStateList?)
                    }
                }
                
                Log.d(TAG, "Total weeks in data: ${weeksArray.length()}, colorTheme: $colorTheme")
                
                // Show as many weeks as will fit, with most recent on the RIGHT (GitHub style)
                val actualWeeks = minOf(weeksToShow, weeksArray.length())
                val startIndex = maxOf(0, weeksArray.length() - actualWeeks)
                
                Log.d(TAG, "Showing weeks from index $startIndex to ${weeksArray.length() - 1}")
                
                // Iterate from oldest to newest (left to right) - GitHub style
                // So the rightmost column is the most recent week
                for (i in startIndex until weeksArray.length()) {
                    val weekArray = weeksArray.getJSONArray(i)
                    val weekLayout = RemoteViews(context.packageName, R.layout.widget_week_column)
                    
                    val daysInWeek = weekArray.length()
                    
                    // For incomplete weeks (like the current week), we need to determine
                    // where to place the cells. GitHub API returns days starting from Sunday.
                    // If a week has fewer than 7 days, it could be:
                    // - First week of the year (starts mid-week)
                    // - Current week (ends mid-week)
                    
                    // For the LAST week (current week), days should be at the TOP
                    // because they represent the beginning of the week (Sunday onwards)
                    // For the FIRST week, days might start mid-week
                    
                    // Always add 7 cells per column for consistent layout
                    // Fill with actual data where available, empty cells otherwise
                    for (j in 0 until 7) {
                        val level = if (j < daysInWeek) {
                            weekArray.getInt(j)
                        } else {
                            -1 // Placeholder for days that don't exist yet (future days in current week)
                        }
                        
                        val cellView = RemoteViews(context.packageName, cellLayoutId)
                        
                        if (level >= 0) {
                            // Actual contribution day - tint the rounded cell shape
                            // (setBackgroundColor would replace it with a sharp square)
                            val levelIndex = level.coerceIn(0, 4)
                            if (materialYou && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                cellView.setColor(R.id.cell_view, "setColorFilter", MATERIAL_YOU_COLOR_RES[levelIndex])
                            } else {
                                cellView.setInt(R.id.cell_view, "setColorFilter", levelColors[levelIndex])
                            }
                        } else {
                            // Future day - hide it but keep its space
                            cellView.setViewVisibility(R.id.cell_view, View.INVISIBLE)
                        }
                        
                        weekLayout.addView(R.id.week_column, cellView)
                    }
                    
                    views.addView(R.id.contribution_grid, weekLayout)
                }
                
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing data", e)
            }
        } else {
            Log.d(TAG, "No widget data found")
        }
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}