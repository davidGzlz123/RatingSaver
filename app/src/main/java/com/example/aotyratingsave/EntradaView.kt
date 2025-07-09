package com.example.aotyratingsave

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class EntradaView(
    context: Context,
    val entrada: Entrada,
    val onEntradaEliminadaListener: OnEntradaEliminadaListener
) : LinearLayout(context) {

    interface OnEntradaEliminadaListener {
        fun onEntradaEliminada(entrada: Entrada)
        fun onEntradaEditada()
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.entrada_view, this, true)
        val artistaAlbumTextView = findViewById<TextView>(R.id.artistaAlbumTextView)
        val numeroCancionesTextView = findViewById<TextView>(R.id.numeroCancionesTextView)
        val promedioTextView = findViewById<TextView>(R.id.promedioTextView)

        artistaAlbumTextView.text = "${entrada.artista} - ${entrada.album}"
        numeroCancionesTextView.text = "Canciones: ${entrada.numeroCanciones}"

        val promedioFormateado = String.format("%.1f", (entrada.promedio / 10.0))
        val promedioText = "Rating: $promedioFormateado"
        val spannableString = SpannableString(promedioText)

        spannableString.setSpan(
            ForegroundColorSpan(Color.BLACK),
            0,
            7,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        spannableString.setSpan(
            ForegroundColorSpan(Color.parseColor("#a75cfc")),
            7,
            promedioText.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        spannableString.setSpan(
            StyleSpan(Typeface.BOLD),
            7,
            promedioText.length,
            SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        promedioTextView.text = spannableString

        setOnClickListener {
            actualizarValores()
        }

        setOnLongClickListener {
            mostrarOpciones()
            true
        }
    }

    private fun mostrarOpciones() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Opciones")
        builder.setItems(arrayOf("Editar Entrada", "Eliminar Entrada")) { _, which ->
            when (which) {
                0 -> editarEntrada()
                1 -> confirmarEliminarEntrada()
            }
        }
        builder.show()
    }

    private fun actualizarValores() {
        val intent = Intent(context, ValoresCancionesActivity::class.java).apply {
            putExtra("artista", entrada.artista)
            putExtra("album", entrada.album)
            putExtra("numeroCanciones", entrada.numeroCanciones)
            putExtra("valoresCanciones", entrada.valoresCanciones)
        }
        context.startActivity(intent)
    }

    private fun editarEntrada() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Editar Entrada")

        val layout = LinearLayout(context)
        layout.orientation = LinearLayout.VERTICAL

        val artistaEditText = EditText(context)
        artistaEditText.hint = "Artista"
        artistaEditText.setText(entrada.artista)
        layout.addView(artistaEditText)

        val albumEditText = EditText(context)
        albumEditText.hint = "Álbum"
        albumEditText.setText(entrada.album)
        layout.addView(albumEditText)

        val cancionesEditText = EditText(context)
        cancionesEditText.hint = "Número de canciones"
        cancionesEditText.setText(entrada.numeroCanciones.toString())
        layout.addView(cancionesEditText)

        builder.setView(layout)

        builder.setPositiveButton("Guardar") { _, _ ->
            val nuevoArtista = artistaEditText.text.toString()
            val nuevoAlbum = albumEditText.text.toString()
            val nuevasCanciones = cancionesEditText.text.toString().toIntOrNull() ?: entrada.numeroCanciones

            entrada.artista = nuevoArtista
            entrada.album = nuevoAlbum
            entrada.numeroCanciones = nuevasCanciones

            guardarCambiosEnSharedPreferences()

            onEntradaEliminadaListener.onEntradaEditada()
        }

        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun guardarCambiosEnSharedPreferences() {
        val sharedPreferences = context.getSharedPreferences("music_data", Context.MODE_PRIVATE)
        val gson = Gson()
        val jsonActual = sharedPreferences.getString("entradas", null)
        val type = object : TypeToken<ArrayList<Entrada>>() {}.type
        val lista = gson.fromJson<ArrayList<Entrada>>(jsonActual, type) ?: ArrayList()

        val index = lista.indexOfFirst { it.id == entrada.id }

        if (index != -1) {
            lista[index] = entrada
        } else {
            lista.add(entrada)
        }

        val nuevoJson = gson.toJson(lista)
        sharedPreferences.edit().putString("entradas", nuevoJson).apply()
    }

    private fun confirmarEliminarEntrada() {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Confirmar Eliminación")
        builder.setMessage("¿Seguro que desea eliminar esta entrada?")
        builder.setPositiveButton("Eliminar") { _, _ ->
            eliminarEntrada()
        }
        builder.setNegativeButton("Cancelar", null)
        builder.show()
    }

    private fun eliminarEntrada() {
        onEntradaEliminadaListener.onEntradaEliminada(entrada)
    }
}
