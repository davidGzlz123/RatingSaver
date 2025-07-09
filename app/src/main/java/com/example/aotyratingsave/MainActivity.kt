package com.example.aotyratingsave

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList

class MainActivity : AppCompatActivity(), EntradaView.OnEntradaEliminadaListener {

    private lateinit var agregarEntradaButton: Button
    private lateinit var listaEntradasLayout: LinearLayout
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var entradas: ArrayList<Entrada>
    private lateinit var gson: Gson
    private lateinit var type: Type
    private lateinit var searchEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        lateinit var toolbar: Toolbar

        agregarEntradaButton = findViewById(R.id.agregarEntradaButton)
        listaEntradasLayout = findViewById(R.id.listaEntradasLayout)
        searchEditText = findViewById(R.id.searchEditText)

        sharedPreferences = getSharedPreferences("music_data", Context.MODE_PRIVATE)
        gson = Gson()
        type = object : TypeToken<ArrayList<Entrada>>() {}.type
        entradas = cargarEntradas()

        agregarEntradaButton.setOnClickListener {
            val intent = Intent(this, AgregarEntradaActivity::class.java)
            startActivity(intent)
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                mostrarEntradasFiltradas(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        mostrarEntradas()
    }

    override fun onResume() {
        super.onResume()
        entradas = cargarEntradas()
        mostrarEntradas()
    }

    private fun cargarEntradas(): ArrayList<Entrada> {
        val json = sharedPreferences.getString("entradas", null)
        return gson.fromJson(json, type) ?: ArrayList()
    }

    private fun guardarEntradas() {
        val json = gson.toJson(entradas)
        sharedPreferences.edit().putString("entradas", json).apply()
    }

    private fun mostrarEntradas() {
        mostrarEntradasFiltradas("")
    }

    private fun mostrarEntradasFiltradas(filtro: String) {
        val entradasFiltradas = entradas.filter { entrada ->
            entrada.artista.contains(filtro, ignoreCase = true) ||
                    entrada.album.contains(filtro, ignoreCase = true)
        }
        listaEntradasLayout.removeAllViews()
        for (entrada in entradasFiltradas) {
            val entradaView = EntradaView(this, entrada, this)
            listaEntradasLayout.addView(entradaView)
        }
    }

    override fun onEntradaEliminada(entrada: Entrada) {
        entradas.remove(entrada)
        guardarEntradas()
        mostrarEntradasFiltradas(searchEditText.text.toString())
    }

    override fun onEntradaEditada() {
        entradas = cargarEntradas()
        mostrarEntradasFiltradas(searchEditText.text.toString())
    }
}

// Data class compatible para uso en toda la app
data class Entrada(
    val id: String = java.util.UUID.randomUUID().toString(),
    var artista: String,
    var album: String,
    var numeroCanciones: Int,
    var valoresCanciones: DoubleArray,
    var promedio: Double
)
