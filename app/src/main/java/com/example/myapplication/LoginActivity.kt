package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login) // legat de login.xml

        val input = findViewById<EditText>(R.id.client_id_input)
        val button = findViewById<Button>(R.id.login_button)

        button.setOnClickListener {
            val id = input.text.toString().trim()
            if (id.isNotEmpty()) {
                // Trimite către MainActivity
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                input.error = "Te rog introdu un ID valid"
            }
        }
    }
}
