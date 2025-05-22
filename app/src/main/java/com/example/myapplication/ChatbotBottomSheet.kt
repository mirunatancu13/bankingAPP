package com.example.myapplication

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.myapplication.databinding.FragmentChatbotBottomSheetBinding
import android.util.Log

class ChatbotBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentChatbotBottomSheetBinding
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private var clientId: String? = null // ← Adăugat

    companion object {
        private const val ARG_CLIENT_ID = "client_id"

        // ← Metoda pentru a crea instanța cu client_id
        fun newInstance(clientId: String): ChatbotBottomSheet {
            val fragment = ChatbotBottomSheet()
            val args = Bundle().apply {
                putString(ARG_CLIENT_ID, clientId)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ← Extrage client_id din arguments
        clientId = arguments?.getString(ARG_CLIENT_ID)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatbotBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = ChatAdapter(messages)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)

        binding.sendButton.setOnClickListener {
            val input = binding.messageInput.text.toString().trim()
            if (input.isNotEmpty()) {
                adaugaMesajUser(input)
                binding.messageInput.text?.clear()

                // ← Folosește client_id-ul primit prin Bundle
                clientId?.let { id ->
                    trimiteIntrebare(input, id)
                } ?: run {
                    adaugaMesajBot("❌ Eroare: Client ID lipsește")
                }
            }
        }
    }

    private fun trimiteIntrebare(question: String, clientId: String) {
        val url = "http://10.0.2.2:5000/ask"
        Log.d("AI-DEBUG", "Trimit întrebare: $question cu clientId: $clientId")

        val requestQueue = Volley.newRequestQueue(requireContext())
        val jsonBody = JSONObject().apply {
            put("question", question)
            put("client_id", clientId)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            { response ->
                Log.d("AI-DEBUG", "JSON complet: $response")
                val raspuns = try {
                    if (response.has("response")) {
                        response.getString("response")
                    } else if (response.has("error")) {
                        "Eroare server: ${response.getString("error")}"
                    } else {
                        "Răspuns neașteptat: $response"
                    }
                } catch (e: Exception) {
                    Log.e("AI-DEBUG", "Eroare la extragere JSON: ${e.message}")
                    "⚠ Eroare de parsing: ${e.message}"
                }
                adaugaMesajBot(raspuns)
            },
            { error ->
                Log.e("AI-DEBUG", "Eroare Volley: ${error.message}")
                val errorMsg = error.networkResponse?.let {
                    "Eroare ${it.statusCode}: ${String(it.data)}"
                } ?: "Eroare de rețea: ${error.message ?: "necunoscută"}"
                adaugaMesajBot(errorMsg)
            }
        )
        requestQueue.add(request)
    }

    private fun adaugaMesajUser(text: String) {
        Log.d("AI-DEBUG", "Mesaj trimis de utilizator: $text")
        messages.add(Message(text, isUser = true))
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.scrollToPosition(messages.size - 1)
    }

    private fun adaugaMesajBot(text: String) {
        Log.d("AI-DEBUG", "Mesaj primit de la AI: $text")
        messages.add(Message(text, isUser = false))
        adapter.notifyItemInserted(messages.size - 1)
        binding.recyclerView.scrollToPosition(messages.size - 1)
    }
}