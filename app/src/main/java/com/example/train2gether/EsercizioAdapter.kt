package com.example.train2gether

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EsercizioAdapter(
    private val listaEsercizi: MutableList<EsercizioConSerie>,
    private val isModifica: Boolean, // 👈 NUOVO PARAMETRO
    private val onSerieSpuntata: () -> Unit
) : RecyclerView.Adapter<EsercizioAdapter.EsercizioViewHolder>() {

    class EsercizioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNomeEsercizio: TextView = view.findViewById(R.id.txtNomeEsercizio)
        val btnEliminaEsercizio: ImageButton = view.findViewById(R.id.btnEliminaEsercizio)
        val recyclerSerie: RecyclerView = view.findViewById(R.id.recyclerSerie)
        val btnAggiungiSerie: Button = view.findViewById(R.id.btnAggiungiSerieEsercizio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EsercizioViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_esercizio, parent, false)
        return EsercizioViewHolder(view)
    }
    override fun onBindViewHolder(holder: EsercizioViewHolder, position: Int) {
        val esercizio = listaEsercizi[position]
        holder.txtNomeEsercizio.text = esercizio.nomeEsercizio

        holder.recyclerSerie.layoutManager = LinearLayoutManager(holder.itemView.context)

        lateinit var serieAdapter: SerieAdapter
        serieAdapter = SerieAdapter(
            listaSerie = esercizio.listaSerie,
            isModifica = isModifica, // 👈 Passa isModifica qui!
            onSerieSpuntata = onSerieSpuntata,
            onEliminaSerie = { index ->
                esercizio.listaSerie.removeAt(index)
                serieAdapter.notifyDataSetChanged()
            }
        )

        holder.recyclerSerie.adapter = serieAdapter

        // Tasto aggiungi serie
        holder.btnAggiungiSerie.setOnClickListener {
            val nuovoSet = esercizio.listaSerie.size + 1
            esercizio.listaSerie.add(SerieEsercizio(nuovoSet, 0.0, 0))
            serieAdapter.notifyItemInserted(esercizio.listaSerie.size - 1)
        }

        // Tasto elimina esercizio
        holder.btnEliminaEsercizio.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                AlertDialog.Builder(holder.itemView.context)
                    .setTitle("Elimina Esercizio")
                    .setMessage("Vuoi davvero rimuovere ${esercizio.nomeEsercizio}?")
                    .setPositiveButton("Rimuovi") { _, _ ->
                        listaEsercizi.removeAt(pos)
                        notifyItemRemoved(pos)
                        notifyItemRangeChanged(pos, listaEsercizi.size)
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
        }
    }

    override fun getItemCount(): Int = listaEsercizi.size
}