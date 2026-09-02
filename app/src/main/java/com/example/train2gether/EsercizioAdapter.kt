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

// Adapter principale per la RecyclerView degli esercizi presenti nella scheda
class EsercizioAdapter(
    private val listaEsercizi: MutableList<EsercizioConSerie>,
    private val isModifica: Boolean,
    private val onStatoSerieCambiato: (EsercizioConSerie, Int, SerieEsercizio, Boolean) -> Unit,
    private val onSerieModificata: (EsercizioConSerie, Int, SerieEsercizio) -> Unit,
    private val onSerieSpuntata: (Long) -> Unit,
    private val onDatoModificato: () -> Unit
) : RecyclerView.Adapter<EsercizioAdapter.EsercizioViewHolder>() {

    // ViewHolder che mantiene i riferimenti agli elementi grafici del singolo esercizio
    class EsercizioViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNomeEsercizio: TextView = view.findViewById(R.id.txtNomeEsercizio)
        val txtTimerEsercizio: TextView = view.findViewById(R.id.txtTimerEsercizio)
        val btnEliminaEsercizio: ImageButton = view.findViewById(R.id.btnEliminaEsercizio)
        val recyclerSerie: RecyclerView = view.findViewById(R.id.recyclerSerie)
        val btnAggiungiSerie: Button = view.findViewById(R.id.btnAggiungiSerieEsercizio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EsercizioViewHolder {
        // Gonfia il layout del file XML
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_esercizio, parent, false)
        return EsercizioViewHolder(view)
    }

    override fun onBindViewHolder(holder: EsercizioViewHolder, position: Int) {
        val esercizio = listaEsercizi[position]
        holder.txtNomeEsercizio.text = esercizio.nomeEsercizio

        // Imposta e formatta il testo del timer del singolo esercizio
        aggiornaTestoTimer(holder.txtTimerEsercizio, esercizio.tempoRecuperoSecondi)

        // Al click sul timer, apre il popup di modifica
        holder.txtTimerEsercizio.setOnClickListener {
            mostraPickerPersonalizzato(holder.itemView.context, esercizio) { nuoviSecondi ->
                esercizio.tempoRecuperoSecondi = nuoviSecondi
                aggiornaTestoTimer(holder.txtTimerEsercizio, nuoviSecondi)
                onDatoModificato()
            }
        }

        // Configura la RecyclerView interna per gestire le singole serie
        holder.recyclerSerie.layoutManager = LinearLayoutManager(holder.itemView.context)

        lateinit var serieAdapter: SerieAdapter

        serieAdapter = SerieAdapter(

            listaSerie = esercizio.listaSerie,

            isModifica = isModifica,

            // Callback esegui quando si spunta una serie
            onStatoSerieCambiato = {
                    serie,
                    checked ->

                val ordineEsercizio =
                    holder.bindingAdapterPosition

                if (
                    ordineEsercizio !=
                    RecyclerView.NO_POSITION
                ) {
                    // Notifica all'activity il cambio di stato della serie
                    onStatoSerieCambiato(
                        esercizio,
                        ordineEsercizio,
                        serie,
                        checked
                    )

                    // Se la serie è stata completata fa partire il timer di recupero
                    if (
                        checked &&
                        !isModifica
                    ) {

                        onSerieSpuntata(
                            esercizio
                                .tempoRecuperoSecondi
                        )
                    }
                }
            },

            // Callback per aggiornare i kg o le ripetizioni di una serie
            onSerieModificata = { serie ->

                val ordineEsercizio =
                    holder.bindingAdapterPosition

                if (
                    ordineEsercizio !=
                    RecyclerView.NO_POSITION
                ) {

                    onSerieModificata(
                        esercizio,
                        ordineEsercizio,
                        serie
                    )
                }
            },

            // Callback per eliminare una singola serie
            onEliminaSerie = { index ->

                if (
                    index in
                    0 until
                    esercizio.listaSerie.size
                ) {

                    esercizio.listaSerie.removeAt(index)

                    serieAdapter.notifyDataSetChanged()

                    onDatoModificato()
                }
            },

            onDatoModificato = {
                onDatoModificato()
            }
        )

        holder.recyclerSerie.adapter = serieAdapter

        // Bottone per aggiungere una nuova serie
        holder.btnAggiungiSerie.setOnClickListener {
            val nuovoSet = esercizio.listaSerie.size + 1
            esercizio.listaSerie.add(SerieEsercizio(nuovoSet, 0.0, 0))
            serieAdapter.notifyItemInserted(esercizio.listaSerie.size - 1)
            onDatoModificato()
        }

        // Mostra il pulsante per eliminare l'esercizio in base alla modalità
        if (isModifica) {
            holder.btnEliminaEsercizio.visibility = View.VISIBLE

            holder.btnEliminaEsercizio.setOnClickListener {
                val pos = holder.bindingAdapterPosition

                if (pos != RecyclerView.NO_POSITION) {
                    listaEsercizi.removeAt(pos)
                    notifyItemRemoved(pos)
                    notifyItemRangeChanged(pos, listaEsercizi.size)
                    onDatoModificato()
                }
            }
        } else {
            holder.btnEliminaEsercizio.visibility = View.GONE
            holder.btnEliminaEsercizio.setOnClickListener(null)
        }
    }

    override fun getItemCount(): Int = listaEsercizi.size

    // Converte i secondi totali nel formato "mm:ss"
    private fun aggiornaTestoTimer(textView: TextView, secondiTotali: Long) {
        val minuti = secondiTotali / 60
        val secondi = secondiTotali % 60
        textView.text = String.format("⏱️ %02d:%02d", minuti, secondi)
    }

    // Costruisce e mostra un selettore numerico per minuti e secondi di recupero
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

        val twoDigitFormatter = NumberPicker.Formatter { value ->
            String.format("%02d", value)
        }

        val pickerMinuti = NumberPicker(context).apply {
            minValue = 0
            maxValue = 15
            setFormatter(twoDigitFormatter)
            value = (esercizio.tempoRecuperoSecondi / 60).toInt()
        }

        val txtSeparatore = TextView(context).apply {
            text = " : "
            textSize = 24f
            setPadding(20, 0, 20, 0)
        }

        val pickerSecondi = NumberPicker(context).apply {
            minValue = 0
            maxValue = 59
            setFormatter(twoDigitFormatter)
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