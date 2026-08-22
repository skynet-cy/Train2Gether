package com.example.train2gether

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.train2gether.data.Esercizio

class CatalogoDialogAdapter(
    private val listaEsercizi: List<Esercizio>,
    private val onEsercizioSelezionato: (Esercizio) -> Unit
) : RecyclerView.Adapter<CatalogoDialogAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtNome: TextView = itemView.findViewById(android.R.id.text1)
        val txtGruppo: TextView = itemView.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val esercizio = listaEsercizi[position]
        holder.txtNome.text = esercizio.nome
        holder.txtGruppo.text = esercizio.gruppoMuscolare

        holder.itemView.setOnClickListener {
            onEsercizioSelezionato(esercizio)
        }
    }

    override fun getItemCount(): Int = listaEsercizi.size
}