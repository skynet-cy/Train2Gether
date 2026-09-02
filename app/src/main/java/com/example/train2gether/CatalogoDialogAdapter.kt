package com.example.train2gether

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.train2gether.data.Esercizio

// Adapter per gestire la RecyclerView all'interno del dialog di selezione degli esercizi dal DB
class CatalogoDialogAdapter(
    private val listaEsercizi: List<Esercizio>,
    private val onEsercizioSelezionato: (Esercizio) -> Unit
) : RecyclerView.Adapter<CatalogoDialogAdapter.ViewHolder>() {

    // ViewHolder che mappa i campi per il nome e il gruppo muscolare
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNome: TextView = itemView.findViewById(android.R.id.text1)
        val txtGruppo: TextView = itemView.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Gonfia un layout nativo di Android a due righe per mostrare nome e categoria dell'esercizio
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val esercizio = listaEsercizi[position]

        // Assegna i valori dell'esercizio alle TextView corrispondenti
        holder.txtNome.text = esercizio.nome
        holder.txtGruppo.text = esercizio.gruppoMuscolare

        // Configura il click listener sulla riga per restituire l'esercizio selezionato tramite callback
        holder.itemView.setOnClickListener {
            onEsercizioSelezionato(esercizio)
        }
    }

    override fun getItemCount(): Int = listaEsercizi.size
}