package com.example.kurirdirectoryapp

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var editTextSearchName: EditText
    private lateinit var progressBarLoading: ProgressBar
    private lateinit var textViewErrorMessage: TextView
    private lateinit var textViewEmptyState: TextView
    private lateinit var recyclerViewUsers: RecyclerView

    private lateinit var userAdapter: UserAdapter

    // Single source of truth untuk seluruh data pengguna asli yang didapat dari API
    private var daftarPenggunaAsli: List<UserResponse> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupRecyclerView()
        setupSearchListener()
        fetchDataUsers()
    }

    private fun initViews() {
        editTextSearchName = findViewById(R.id.editTextSearchName)
        progressBarLoading = findViewById(R.id.progressBarLoading)
        textViewErrorMessage = findViewById(R.id.textViewErrorMessage)
        textViewEmptyState = findViewById(R.id.textViewEmptyState)
        recyclerViewUsers = findViewById(R.id.recyclerViewUsers)
    }

    private fun setupRecyclerView() {
        userAdapter = UserAdapter()
        recyclerViewUsers.layoutManager = LinearLayoutManager(this)
        recyclerViewUsers.adapter = userAdapter
    }

    private fun setupSearchListener() {
        editTextSearchName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterData(s?.toString().orEmpty())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun fetchDataUsers() {
        // Tampilkan loading indicator & sembunyikan tampilan lain
        progressBarLoading.visibility = View.VISIBLE
        textViewErrorMessage.visibility = View.GONE
        textViewEmptyState.visibility = View.GONE
        recyclerViewUsers.visibility = View.GONE

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.apiService.getAllUsers()
                withContext(Dispatchers.Main) {
                    progressBarLoading.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        daftarPenggunaAsli = response.body() ?: emptyList()
                        filterData(editTextSearchName.text.toString())
                    } else {
                        showErrorMessage("Gagal memuat data dari server. Kode error: ${response.code()}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBarLoading.visibility = View.GONE
                    showErrorMessage("Gagal terhubung ke server. Periksa koneksi internet Anda.")
                }
            }
        }
    }

    private fun filterData(kataKunci: String) {
        if (daftarPenggunaAsli.isEmpty()) return

        val query = kataKunci.trim()
        val hasilFilter = if (query.isEmpty()) {
            daftarPenggunaAsli
        } else {
            // Logika filter ganda: Nama ATAU Nama Perusahaan (case-insensitive) [Tugas 1]
            daftarPenggunaAsli.filter { pengguna ->
                pengguna.name.contains(query, ignoreCase = true) ||
                        pengguna.company.companyName.contains(query, ignoreCase = true)
            }
        }

        userAdapter.perbaruiDaftar(hasilFilter)

        // Penanganan Empty State [Tugas 2]
        if (hasilFilter.isEmpty()) {
            recyclerViewUsers.visibility = View.GONE
            textViewEmptyState.visibility = View.VISIBLE
        } else {
            recyclerViewUsers.visibility = View.VISIBLE
            textViewEmptyState.visibility = View.GONE
        }
    }

    private fun showErrorMessage(message: String) {
        textViewErrorMessage.text = message
        textViewErrorMessage.visibility = View.VISIBLE
        recyclerViewUsers.visibility = View.GONE
        textViewEmptyState.visibility = View.GONE
    }
}
