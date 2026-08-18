package com.example.train2gether

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView

class SchedaAdapter(
    private val listaSchede: MutableList<SchedaAllenamento>,
    private val onAvviaClick: (SchedaAllenamento) -> Unit,
    private val onModificaClick: (SchedaAllenamento) -> Unit,
    private val onEliminaClick: (SchedaAllenamento) -> Unit
) : RecyclerView.Adapter<SchedaAdapter.SchedaViewHolder>() {

    class SchedaViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNome: TextView = view.findViewById(R.id.txtNomeScheda)
        val txtInfo: TextView = view.findViewById(R.id.txtInfoEsercizi)
        val btnAvvia: Button = view.findViewById(R.id.btnAvviaAllenamento)
        val btnModifica: Button = view.findViewById(R.id.btnModificaScheda)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SchedaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_scheda, parent, false)
        return SchedaViewHolder(view)
    }

    override fun onBindViewHolder(holder: SchedaViewHolder, position: Int) {
        val scheda = listaSchede[position]

        holder.txtNome.text = scheda.nomeScheda
        val numEsercizi = scheda.listaEsercizi.size
        holder.txtInfo.text = if (numEsercizi == 1) "1 Esercizio" else "$numEsercizi Esercizi"

        // 1. Click su "▶️ Avvia" -> Avvia la sessione di allenamento
        holder.btnAvvia.setOnClickListener {
            onAvviaClick(scheda)
        }

        // 2. Click su "✏️ Modifica" -> Apre l'editor della scheda
        holder.btnModifica.setOnClickListener {
            onModificaClick(scheda)
        }

        // 3. Pressione prolungata sulla Card -> Elimina scheda con popup
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("Elimina Scheda")
                .setMessage("Vuoi davvero eliminare la scheda \"${scheda.nomeScheda}\"?")
                .setPositiveButton("Elimina") { _, _ ->
                    onEliminaClick(scheda)
                }
                .setNegativeButton("Annulla", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = listaSchede.size
}