package com.example.myapplication
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class DiscoverActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_discover)

        val navHome = findViewById<ImageButton>(R.id.nav_home)
        val navDiscover = findViewById<ImageButton>(R.id.discover)
        val navGoals = findViewById<ImageButton>(R.id.goals)
        val navSimulation = findViewById<ImageButton>(R.id.simulation)

        navHome.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        navGoals.setOnClickListener {
            val intent = Intent(this, GoalsActivity::class.java)
            startActivity(intent)
            finish()
        }

        navSimulation.setOnClickListener {
            val intent = Intent(this, SimulationActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}
