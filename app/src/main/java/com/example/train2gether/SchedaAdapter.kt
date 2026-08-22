package com.example.train2gether

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
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
        val btnEliminaScheda: ImageButton = view.findViewById(R.id.btnEliminaScheda)
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

        holder.btnAvvia.setOnClickListener { onAvviaClick(scheda) }
        holder.btnModifica.setOnClickListener { onModificaClick(scheda) }

        holder.btnEliminaScheda.setOnClickListener {
            onEliminaClick(scheda)
        }
    }

    override fun getItemCount(): Int = listaSchede.size
}