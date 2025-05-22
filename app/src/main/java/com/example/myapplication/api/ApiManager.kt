package com.example.myapplication.api

import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.Response
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException


class ApiManager {

    interface ApiCallback<T> {
        fun onSuccess(result: T)
        fun onError(error: String)
    }

    private val client = OkHttpClient()
    private val baseUrl = "http://10.0.2.2:5000"


    fun verifyClient(clientId: String, callback: ApiCallback<ClientData>) {
        val json = JSONObject()
        json.put("client_id", clientId)

        val mediaType: MediaType = "application/json".toMediaType()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("$baseUrl/verify-client")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Eroare conexiune: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                if (response.isSuccessful && bodyString != null) {
                    val jsonResponse = JSONObject(bodyString)
                    val result = ClientData(
                        client_id = jsonResponse.getString("client_id"),
                        client_info = jsonResponse.getString("client_info") ,
                                balance = jsonResponse.getDouble("balance")
                    )
                    callback.onSuccess(result)
                } else {
                    val msg = JSONObject(bodyString ?: "{}").optString("error", "Eroare necunoscută")
                    callback.onError(msg)
                }
            }
        })
    }

    fun checkServerHealth(callback: ApiCallback<HealthData>) {
        val request = Request.Builder()
            .url("$baseUrl/health")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError("Eroare conexiune: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()
                if (response.isSuccessful && bodyString != null) {
                    val json = JSONObject(bodyString)
                    val result = HealthData(
                        csv_loaded = json.getBoolean("csv_loaded"),
                        total_clients = json.getInt("total_clients")
                    )
                    callback.onSuccess(result)
                } else {
                    callback.onError("Eroare server")
                }
            }
        })
    }

}
