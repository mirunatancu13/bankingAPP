package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.api.ApiManager
import com.example.myapplication.api.ClientData
import com.example.myapplication.api.HealthData

class LoginActivity : AppCompatActivity() {

    private lateinit var input: EditText
    private lateinit var button: Button
    private lateinit var employeeButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var apiManager: ApiManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login)

        // Inițializare views
        input = findViewById(R.id.client_id_input)
        button = findViewById(R.id.login_button)
        employeeButton = findViewById(R.id.login_agent_button)
        progressBar = ProgressBar(this).apply {
            visibility = View.GONE
        }

        apiManager = ApiManager()

        // Logare client
        button.setOnClickListener {
            val clientId = input.text.toString().trim()
            if (clientId.isEmpty()) {
                input.error = "Te rog introdu un ID valid"
                return@setOnClickListener
            }
            verifyClientId(clientId)
        }

        // Buton logare angajat (fără verificare, doar acces direct)
        employeeButton.setOnClickListener {
            val intent = Intent(this@LoginActivity, AngajatActivity::class.java)
            startActivity(intent)
        }

        checkServerConnection()
    }

    private fun verifyClientId(clientId: String) {
        showLoading(true)

        apiManager.verifyClient(clientId, object : ApiManager.ApiCallback<ClientData> {
            override fun onSuccess(result: ClientData) {
                runOnUiThread {
                    showLoading(false)
                    saveClientId(result.client_id)

                    Toast.makeText(
                        this@LoginActivity,
                        "Client găsit! Bun venit!",
                        Toast.LENGTH_SHORT
                    ).show()

                    val intent = Intent(this@LoginActivity, MainActivity::class.java).apply {
                        putExtra("CLIENT_ID", result.client_id)
                        putExtra("BALANCE", result.balance.toString())
                    }
                    startActivity(intent)
                    finish()
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    showLoading(false)
                    input.error = error
                    Toast.makeText(
                        this@LoginActivity,
                        "Eroare: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    // ✅ Salvează client ID în SharedPreferences
    private fun saveClientId(clientId: String) {
        val prefs = getSharedPreferences("bcr_prefs", MODE_PRIVATE)
        prefs.edit().putString("client_id", clientId).apply()
    }

    private fun checkServerConnection() {
        apiManager.checkServerHealth(object : ApiManager.ApiCallback<HealthData> {
            override fun onSuccess(result: HealthData) {
                runOnUiThread {
                    if (result.csv_loaded) {
                        Toast.makeText(
                            this@LoginActivity,
                            "Server conectat - ${result.total_clients} clienți în baza de date",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            this@LoginActivity,
                            "Server conectat dar CSV-ul nu este încărcat",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }

            override fun onError(error: String) {
                runOnUiThread {
                    Toast.makeText(
                        this@LoginActivity,
                        "Nu se poate conecta la server: $error",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        })
    }

    private fun showLoading(show: Boolean) {
        if (show) {
            button.isEnabled = false
            button.text = "Se verifică..."
            progressBar.visibility = View.VISIBLE
        } else {
            button.isEnabled = true
            button.text = "Login"
            progressBar.visibility = View.GONE
        }
    }
}
