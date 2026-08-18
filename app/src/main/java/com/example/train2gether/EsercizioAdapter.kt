package com.example.train2gether

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class EsercizioAdapter(
    private val listaEsercizi: MutableList<EsercizioConSerie>,
    private val isModifica: Boolean,
    private val onSerieSpuntata: (Long) -> Unit // Restituisce i secondi di recupero dell'esercizio
) : RecyclerView.Adapter<EsercizioAdapter.EsercizioViewHolder>() {

    class EsercizioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNomeEsercizio: TextView = view.findViewById(R.id.txtNomeEsercizio)
        val txtTimerEsercizio: TextView = view.findViewById(R.id.txtTimerEsercizio)
        val btnEliminaEsercizio: ImageButton = view.findViewById(R.id.btnEliminaEsercizio)
        val recyclerSerie: RecyclerView = view.findViewById(R.id.recyclerSerie)
        val btnAggiungiSerie: Button = view.findViewById(R.id.btnAggiungiSerieEsercizio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EsercizioViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_esercizio, parent, false)
        return EsercizioViewHolder(view)
    }

    override fun onBindViewHolder(holder: EsercizioViewHolder, position: Int) {
        val esercizio = listaEsercizi[position]
        holder.txtNomeEsercizio.text = esercizio.nomeEsercizio

        // Formatta e mostra il tempo in MM:SS (es. 02:00)
        aggiornaTestoTimer(holder.txtTimerEsercizio, esercizio.tempoRecuperoSecondi)

        // Click sulla targhetta ➔ Apre DIRETTAMENTE il selettore minuti:secondi
        holder.txtTimerEsercizio.setOnClickListener {
            mostraPickerPersonalizzato(holder.itemView.context, esercizio) { nuoviSecondi ->
                esercizio.tempoRecuperoSecondi = nuoviSecondi
                aggiornaTestoTimer(holder.txtTimerEsercizio, nuoviSecondi)
            }
        }

        // Configurazione RecyclerView delle serie
        holder.recyclerSerie.layoutManager = LinearLayoutManager(holder.itemView.context)

        lateinit var serieAdapter: SerieAdapter
        serieAdapter = SerieAdapter(
            listaSerie = esercizio.listaSerie,
            isModifica = isModifica,
            onSerieSpuntata = {
                if (!isModifica) {
                    // Fa partire il timer specifico dell'esercizio corrente
                    onSerieSpuntata(esercizio.tempoRecuperoSecondi)
                }
            },
            onEliminaSerie = { index ->
                if (index in 0 until esercizio.listaSerie.size) {
                    esercizio.listaSerie.removeAt(index)
                    serieAdapter.notifyDataSetChanged()
                }
            }
        )

        holder.recyclerSerie.adapter = serieAdapter

        // Bottone aggiungi serie
        holder.btnAggiungiSerie.setOnClickListener {
            val nuovoSet = esercizio.listaSerie.size + 1
            esercizio.listaSerie.add(SerieEsercizio(nuovoSet, 0.0, 0))
            serieAdapter.notifyItemInserted(esercizio.listaSerie.size - 1)
        }

        // Bottone elimina esercizio
        holder.btnEliminaEsercizio.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                listaEsercizi.removeAt(pos)
                notifyItemRemoved(pos)
                notifyItemRangeChanged(pos, listaEsercizi.size)
            }
        }
    }

    override fun getItemCount(): Int = listaEsercizi.size

    private fun aggiornaTestoTimer(textView: TextView, secondiTotali: Long) {
        val minuti = secondiTotali / 60
        val secondi = secondiTotali % 60
        textView.text = String.format("⏱️ %02d:%02d", minuti, secondi)
    }

    // Dialog con le rotelle direttamente accessibile al click
    private fun mostraPickerPersonalizzato(
        context: Context,
        esercizio: EsercizioConSerie,
        onTempoSelezionato: (Long) -> Unit
    ) {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(50, 40, 50, 20)
            gravity = Gravity.CENTER
        }

        // Formattatore per mostrare sempre 2 cifre (00, 01, 02, 03...)
        val twoDigitFormatter = NumberPicker.Formatter { value ->
            String.format("%02d", value)
        }

        // Selettore Minuti (00, 01, 02...)
        val pickerMinuti = NumberPicker(context).apply {
            minValue = 0
            maxValue = 15
            setFormatter(twoDigitFormatter) // 👈 Formato a 2 cifre
            value = (esercizio.tempoRecuperoSecondi / 60).toInt()
        }

        val txtSeparatore = TextView(context).apply {
            text = " : "
            textSize = 24f
            setPadding(20, 0, 20, 0)
        }

        // Selettore Secondi (00, 01, 02...)
        val pickerSecondi = NumberPicker(context).apply {
            minValue = 0
            maxValue = 59
            setFormatter(twoDigitFormatter) // 👈 Formato a 2 cifre
            value = (esercizio.tempoRecuperoSecondi % 60).toInt()
        }

        layout.addView(pickerMinuti)
        layout.addView(txtSeparatore)
        layout.addView(pickerSecondi)

        AlertDialog.Builder(context)
            .setTitle("Tempo recupero (${esercizio.nomeEsercizio})")
            .setView(layout)
            .setPositiveButton("Conferma") { _, _ ->
                val min = pickerMinuti.value
                val sec = pickerSecondi.value
                val totaleSecondi = (min * 60 + sec).toLong()
                if (totaleSecondi > 0) {
                    onTempoSelezionato(totaleSecondi)
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}