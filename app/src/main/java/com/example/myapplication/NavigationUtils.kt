// NavigationUtils.kt
package com.example.myapplication

import android.content.Intent
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

fun setupBottomNav(currentActivity: AppCompatActivity) {
    val navHome = currentActivity.findViewById<ImageButton>(R.id.nav_home)
    val navDiscover = currentActivity.findViewById<ImageButton>(R.id.discover)
    val navGoals = currentActivity.findViewById<ImageButton>(R.id.goals)
    val navSimulation = currentActivity.findViewById<ImageButton>(R.id.simulation)

    navHome.setOnClickListener {
        if (currentActivity !is MainActivity) {
            val intent = Intent(currentActivity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            currentActivity.startActivity(intent)
            currentActivity.finish()
        }
    }

    navDiscover.setOnClickListener {
        if (currentActivity !is DiscoverActivity) {
            val intent = Intent(currentActivity, DiscoverActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            currentActivity.startActivity(intent)
            currentActivity.finish()
        }
    }

    navSimulation.setOnClickListener {
        if (currentActivity !is SimulationActivity) {
            val intent = Intent(currentActivity, SimulationActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            currentActivity.startActivity(intent)
            currentActivity.finish()
        }
    }
}
