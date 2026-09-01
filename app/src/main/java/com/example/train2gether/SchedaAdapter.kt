package com.example.train2gether

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.train2gether.data.SchedaRiepilogo

class SchedaAdapter(
    private val listaSchede: MutableList<SchedaRiepilogo>,
    private val onAvviaClick: (SchedaRiepilogo) -> Unit,
    private val onModificaClick: (SchedaRiepilogo) -> Unit,
    private val onEliminaClick: (SchedaRiepilogo) -> Unit
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

        holder.txtNome.text = scheda.nome
        holder.txtInfo.text =
            if (scheda.numeroEsercizi == 1) "1 Esercizio"
            else "${scheda.numeroEsercizi} Esercizi"

        holder.btnAvvia.setOnClickListener { onAvviaClick(scheda) }
        holder.btnModifica.setOnClickListener { onModificaClick(scheda) }
        holder.btnEliminaScheda.setOnClickListener { onEliminaClick(scheda) }
    }

    override fun getItemCount(): Int = listaSchede.size
}