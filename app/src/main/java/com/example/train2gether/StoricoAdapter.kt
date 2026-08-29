package com.example.train2gether

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.train2gether.data.AllenamentoRiepilogo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StoricoAdapter(
    private val listaStorico: List<AllenamentoRiepilogo>,
    private val onElementoClick: (AllenamentoRiepilogo) -> Unit
) : RecyclerView.Adapter<StoricoAdapter.StoricoViewHolder>() {

    class StoricoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNomeScheda: TextView = view.findViewById(R.id.txtNomeSchedaStorico)
        val txtData: TextView = view.findViewById(R.id.txtDataStorico)
        val txtDettagli: TextView = view.findViewById(R.id.txtDettagliStorico)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoricoViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_storico, parent, false)
        return StoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoricoViewHolder, position: Int) {
        val item = listaStorico[position]
        holder.txtNomeScheda.text = item.nomeScheda ?: "Allenamento Libero"

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.txtData.text = sdf.format(Date(item.dataInizio))

        val minuti = item.durataSecondi / 60
        holder.txtDettagli.text = "${item.numeroEsercizi} Esercizi • ${item.numeroSerie} Serie • ${minuti}m"

        holder.itemView.setOnClickListener {
            onElementoClick(item)
        }
    }

    override fun getItemCount(): Int = listaStorico.size
}