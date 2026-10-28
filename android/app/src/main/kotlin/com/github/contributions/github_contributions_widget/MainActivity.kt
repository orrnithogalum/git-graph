package com.github.contributions.github_contributions_widget

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel

class MainActivity: FlutterActivity() {
    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        // Lets the app draw the Material You theme with the same colors as the widget
        MethodChannel(flutterEngine.dartExecutor.binaryMessenger, "gitgraph/material_you")
            .setMethodCallHandler { call, result ->
                if (call.method == "getLevelColors") {
                    // Send as unsigned ARGB so Dart gets the same value as 0xAARRGGBB
                    result.success(GitHubContributionsWidget.getMaterialYouColors(this)?.map { it.toLong() and 0xFFFFFFFFL })
                } else {
                    result.notImplemented()
                }
            }
    }
}
