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
import org.json.JSONArray

class ChatbotBottomSheet : BottomSheetDialogFragment() {
    private lateinit var binding: FragmentChatbotBottomSheetBinding
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: ChatAdapter
    private var clientId: String? = null
    private var currentCategory: String? = null // ← Nou: categoria activă
    private var isShowingMenu = true // ← Nou: flag pentru starea meniului

    companion object {
        private const val ARG_CLIENT_ID = "client_id"

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
        clientId = arguments?.getString(ARG_CLIENT_ID)
        if (clientId == null) {
            clientId = loadClientIdFromPrefs()
        }
    }

    private fun loadClientIdFromPrefs(): String? {
        val prefs = requireContext().getSharedPreferences("bcr_prefs", 0)
        return prefs.getString("client_id", null)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentChatbotBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        setupRecyclerView()
        setupSendButton()

        // ← Afișează meniul principal la început
        clientId?.let { id ->
            incarcaMeniu(id)
        } ?: run {
            adaugaMesajBot("❌ Eroare: Client ID lipsește")
        }
    }

    private fun setupRecyclerView() {
        adapter = ChatAdapter(messages)
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun setupSendButton() {
        binding.sendButton.setOnClickListener {
            val input = binding.messageInput.text.toString().trim()
            if (input.isNotEmpty()) {
                adaugaMesajUser(input)
                binding.messageInput.text?.clear()

                clientId?.let { id ->
                    if (isShowingMenu) {
                        // Dacă suntem în meniu, verifică dacă input-ul este o selecție de categorie
                        proceseazaSelectieMeniu(input, id)
                    } else {
                        // Dacă suntem într-o categorie, trimite întrebarea
                        trimiteIntrebare(input, id)
                    }
                } ?: run {
                    adaugaMesajBot("❌ Eroare: Client ID lipsește")
                }
            }
        }
    }

    // ← NOU: Încarcă meniul principal de la server
    private fun incarcaMeniu(clientId: String) {
        val url = "http://10.0.2.2:5000/get-menu"
        Log.d("AI-DEBUG", "Încarc meniul pentru clientId: $clientId")

        val requestQueue = Volley.newRequestQueue(requireContext())
        val jsonBody = JSONObject().apply {
            put("client_id", clientId)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            { response ->
                Log.d("AI-DEBUG", "Meniu primit: $response")
                proceseazaRaspunsMeniu(response)
            },
            { error ->
                Log.e("AI-DEBUG", "Eroare la încărcarea meniului: ${error.message}")
                adaugaMesajBot("❌ Eroare la încărcarea meniului: ${error.message}")
            }
        )
        requestQueue.add(request)
    }

    // ← NOU: Procesează răspunsul cu meniul
    private fun proceseazaRaspunsMeniu(response: JSONObject) {
        try {
            val message = response.getString("message")
            val categories = response.getJSONArray("categories")

            var menuText = "$message\n\n"

            for (i in 0 until categories.length()) {
                val category = categories.getJSONObject(i)
                val title = category.getString("title")
                val description = category.getString("description")
                menuText += "${i + 1}️⃣ $title\n   $description\n\n"
            }

            menuText += "💡 Scrie numărul categoriei (1-3) sau numele categoriei direct."

            adaugaMesajBot(menuText)
            isShowingMenu = true
            currentCategory = null

        } catch (e: Exception) {
            Log.e("AI-DEBUG", "Eroare la procesarea meniului: ${e.message}")
            adaugaMesajBot("❌ Eroare la procesarea meniului")
        }
    }

    // ← NOU: Procesează selecția din meniu
    private fun proceseazaSelectieMeniu(input: String, clientId: String) {
        val categoryId = when (input.trim()) {
            "1", "analiza financiara", "analiză financiară", "financiar" -> "financial_analysis"
            "2", "planificare economii", "economii", "planificare" -> "savings_planning"
            "3", "recomandari produse", "recomandări produse", "produse", "recomandare" -> "product_recommendations"
            "menu", "meniu" -> {
                incarcaMeniu(clientId)
                return
            }
            else -> null
        }

        if (categoryId != null) {
            selecteazaCategoria(categoryId, clientId)
        } else {
            adaugaMesajBot("❓ Opțiune invalidă. Te rog alege 1, 2, 3 sau scrie numele categoriei.")
        }
    }

    // ← NOU: Selectează categoria pe server
    private fun selecteazaCategoria(categoryId: String, clientId: String) {
        val url = "http://10.0.2.2:5000/select-category"
        Log.d("AI-DEBUG", "Selectez categoria: $categoryId pentru clientId: $clientId")

        val requestQueue = Volley.newRequestQueue(requireContext())
        val jsonBody = JSONObject().apply {
            put("client_id", clientId)
            put("category_id", categoryId)
        }

        val request = JsonObjectRequest(
            Request.Method.POST,
            url,
            jsonBody,
            { response ->
                Log.d("AI-DEBUG", "Categorie selectată: $response")
                val message = response.getString("message")
                currentCategory = response.getString("category")
                isShowingMenu = false

                adaugaMesajBot(message)
            },
            { error ->
                Log.e("AI-DEBUG", "Eroare la selectarea categoriei: ${error.message}")
                adaugaMesajBot("❌ Eroare la selectarea categoriei: ${error.message}")
            }
        )
        requestQueue.add(request)
    }

    // ← ÎMBUNĂTĂȚIT: Trimite întrebarea către categoria activă
    private fun trimiteIntrebare(question: String, clientId: String) {
        val url = "http://10.0.2.2:5000/ask"
        Log.d("AI-DEBUG", "Trimit întrebare: $question cu clientId: $clientId în categoria: $currentCategory")

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

                // Verifică dacă server-ul cere să revenim la meniu
                if (response.has("action") && response.getString("action") == "show_menu") {
                    val raspuns = response.getString("response")
                    adaugaMesajBot(raspuns)
                    incarcaMeniu(clientId) // Reîncarcă meniul
                    return@JsonObjectRequest
                }

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