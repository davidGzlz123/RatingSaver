package com.example.aotyratingsave

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.lang.reflect.Type
import java.util.ArrayList

class ValoresCancionesActivity : AppCompatActivity() {

    private lateinit var artistaAlbumTextView: TextView
    private lateinit var valoresCancionesLayout: LinearLayout
    private lateinit var guardarValoresButton: Button
    private lateinit var artista: String
    private lateinit var album: String
    private var numeroCanciones: Int = 0
    private lateinit var entradas: ArrayList<Entrada>
    private lateinit var sharedPreferences: SharedPreferences
    private val valoresEditTexts = ArrayList<EditText>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_valores_canciones)

        artistaAlbumTextView = findViewById(R.id.artistaAlbumTextView)
        valoresCancionesLayout = findViewById(R.id.valoresCancionesLayout)
        guardarValoresButton = findViewById(R.id.guardarValoresButton)

        artista = intent.getStringExtra("artista") ?: ""
        album = intent.getStringExtra("album") ?: ""
        numeroCanciones = intent.getIntExtra("numeroCanciones", 0)
        val valoresCancionesExtra = intent.getDoubleArrayExtra("valoresCanciones")

        sharedPreferences = getSharedPreferences("music_data", MODE_PRIVATE)
        entradas = cargarEntradas()

        artistaAlbumTextView.text = "$artista - $album"

        crearEditTextValoresCanciones(valoresCancionesExtra)

        guardarValoresButton.setOnClickListener {
            guardarValoresCanciones()
        }
    }

    private fun crearEditTextValoresCanciones(valoresCancionesExtra: DoubleArray?) {
        valoresCancionesLayout.removeAllViews()
        valoresEditTexts.clear()
        for (i in 0 until numeroCanciones) {
            val cancionLayout = LinearLayout(this)
            cancionLayout.orientation = LinearLayout.HORIZONTAL
            cancionLayout.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

            val indiceTextView = TextView(this)
            indiceTextView.text = "${i + 1}.  "
            indiceTextView.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            cancionLayout.addView(indiceTextView)

            val editText = EditText(this)
            editText.layoutParams = LinearLayout.LayoutParams(
                0,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                1f
            )
            editText.hint = "Valor de la canción"
            editText.inputType =
                android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL

            if (valoresCancionesExtra != null && i < valoresCancionesExtra.size) {
                editText.setText(valoresCancionesExtra[i].toString())
            }

            cancionLayout.addView(editText)
            valoresEditTexts.add(editText)

            valoresCancionesLayout.addView(cancionLayout)
        }
    }

    private fun guardarValoresCanciones() {
        val valoresCanciones = DoubleArray(numeroCanciones)
        var isValid = true

        for (i in 0 until numeroCanciones) {
            val editText = valoresEditTexts[i]
            val valorStr = editText.text.toString()

            if (valorStr.isNotBlank()) {
                val valor = valorStr.toDoubleOrNull()
                if (valor != null) {
                    valoresCanciones[i] = valor
                } else {
                    isValid = false
                    editText.error = "Ingrese un valor numérico válido"
                    valoresCanciones[i] = 0.0
                }
            } else {
                valoresCanciones[i] = 0.0
            }
        }

        if (isValid) {
            val promedio = valoresCanciones.average()
            val existingIndex = entradas.indexOfFirst { it.artista == artista && it.album == album }
            if (existingIndex != -1) {
                entradas[existingIndex].valoresCanciones = valoresCanciones
                entradas[existingIndex].promedio = promedio
            } else {
                val nuevaEntrada = Entrada(
                    id = java.util.UUID.randomUUID().toString(),
                    artista = artista,
                    album = album,
                    numeroCanciones = numeroCanciones,
                    valoresCanciones = valoresCanciones,
                    promedio = promedio
                )
                entradas.add(nuevaEntrada)
            }
            guardarEntradas()
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun cargarEntradas(): ArrayList<Entrada> {
        val gson = Gson()
        val json = sharedPreferences.getString("entradas", null)
        val type: Type = object : TypeToken<ArrayList<Entrada>>() {}.type
        return gson.fromJson(json, type) ?: ArrayList()
    }

    private fun guardarEntradas() {
        val gson = Gson()
        val json = gson.toJson(entradas)
        sharedPreferences.edit().putString("entradas", json).apply()
    }
}
