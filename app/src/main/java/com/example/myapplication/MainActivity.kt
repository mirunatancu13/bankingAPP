package com.example.myapplication

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home)

        val mainText = findViewById<TextView>(R.id.main_text)
        val navHome = findViewById<ImageButton>(R.id.nav_home)
        val navDiscover = findViewById<ImageButton>(R.id.discover)
        val navGoals = findViewById<ImageButton>(R.id.goals)
        val navSimulation = findViewById<ImageButton>(R.id.simulation)

        navHome.setOnClickListener {
            // Dacă ești deja în Home, nu face nimic
        }

        navDiscover.setOnClickListener {
            val intent = Intent(this, DiscoverActivity::class.java)
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
        val mascota = findViewById<ImageView>(R.id.mascota_button)
        mascota.setOnClickListener {
            val chatbot = ChatbotBottomSheet()
            chatbot.show(supportFragmentManager, "Chatbot")
        }
    }
}
