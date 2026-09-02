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

// Adapter per gestire la visualizzazione degli allenamenti passati nello storico
class StoricoAdapter(
    private val listaStorico: List<AllenamentoRiepilogo>,
    private val onElementoClick: (AllenamentoRiepilogo) -> Unit
) : RecyclerView.Adapter<StoricoAdapter.StoricoViewHolder>() {

    // ViewHolder che mantiene i riferimenti agli elementi grafici dello storico
    class StoricoViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNomeScheda: TextView = view.findViewById(R.id.txtNomeSchedaStorico)
        val txtData: TextView = view.findViewById(R.id.txtDataStorico)
        val txtDettagli: TextView = view.findViewById(R.id.txtDettagliStorico)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoricoViewHolder {
        // Gonfia il layout XML della singola riga dello storico
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_storico, parent, false)
        return StoricoViewHolder(view)
    }

    override fun onBindViewHolder(holder: StoricoViewHolder, position: Int) {
        val item = listaStorico[position]

        // Imposta il nome della scheda
        holder.txtNomeScheda.text = item.nomeScheda ?: "Allenamento Libero"

        // Converte il timestamp di inizio in una stringa di data e ora leggibile
        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        holder.txtData.text = sdf.format(Date(item.dataInizio))

        // Converte la durata totale formattandola in "mm:ss"
        val minuti = item.durataSecondi / 60
        val secondi = item.durataSecondi % 60
        val durataFormattata = String.format("%02d:%02d", minuti, secondi)

        // Compone la stringa di riepilogo
        holder.txtDettagli.text = "${item.numeroEsercizi} Esercizi • ${item.numeroSerie} Serie • $durataFormattata"

        // Configura il click listener per aprire la schermata dell'allenamento selezionato
        holder.itemView.setOnClickListener {
            onElementoClick(item)
        }
    }

    override fun getItemCount(): Int = listaStorico.size
}