package com.example.aotyratingsave

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AgregarEntradaActivity : AppCompatActivity() {

    private lateinit var artistaEditText: EditText
    private lateinit var albumEditText: EditText
    private lateinit var numeroCancionesEditText: EditText
    private lateinit var guardarButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_agregar_entrada)

        // Inicializar vistas
        artistaEditText = findViewById(R.id.artistaEditText)
        albumEditText = findViewById(R.id.albumEditText)
        numeroCancionesEditText = findViewById(R.id.numeroCancionesEditText)
        guardarButton = findViewById(R.id.guardarButton)

        // Configurar el listener del botón guardar
        guardarButton.setOnClickListener {
            guardarEntradaInicial()
        }
    }

    private fun guardarEntradaInicial() {
        val artista = artistaEditText.text.toString()
        val album = albumEditText.text.toString()
        val numeroCancionesStr = numeroCancionesEditText.text.toString()

        if (artista.isNotBlank() && album.isNotBlank() && numeroCancionesStr.isNotBlank()) {
            val numeroCanciones = numeroCancionesStr.toIntOrNull()
            if (numeroCanciones != null && numeroCanciones > 0) {
                // Crear un intent para iniciar la actividad de valores de canciones
                val intent = Intent(this, ValoresCancionesActivity::class.java)
                intent.putExtra("artista", artista)
                intent.putExtra("album", album)
                intent.putExtra("numeroCanciones", numeroCanciones)
                startActivity(intent)
                finish() // Cerrar esta actividad después de iniciar la siguiente
            } else {
                numeroCancionesEditText.error = "Ingrese un número válido de canciones (mayor que 0)"
            }
        } else {
            if (artista.isBlank()) artistaEditText.error = "Ingrese el nombre del artista"
            if (album.isBlank()) albumEditText.error = "Ingrese el nombre del álbum"
            if (numeroCancionesStr.isBlank()) numeroCancionesEditText.error = "Ingrese el número de canciones"
        }
    }
}