package com.example.train2gether

import android.graphics.Color
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Adapter per gestire la lista delle serie associate a un esercizio
class SerieAdapter(

    private val listaSerie: MutableList<SerieEsercizio>,

    private val isModifica: Boolean,

    private val onStatoSerieCambiato:
        (SerieEsercizio, Boolean) -> Unit,

    private val onSerieModificata:
        (SerieEsercizio) -> Unit,

    private val onEliminaSerie: (Int) -> Unit,

    private val onDatoModificato: () -> Unit

) : RecyclerView.Adapter<SerieAdapter.SerieViewHolder>() {

    // ViewHolder che mantiene i riferimenti agli elementi grafici della serie
    class SerieViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        val containerRow: LinearLayout = view as LinearLayout

        val txtNumero: TextView =
            view.findViewById(R.id.txtNumeroSerie)

        val editKg: EditText =
            view.findViewById(R.id.editKgSerie)

        val editReps: EditText =
            view.findViewById(R.id.editRipetizioni)

        val chkCompleto: CheckBox =
            view.findViewById(R.id.chkCompleto)

        val btnElimina: ImageButton =
            view.findViewById(R.id.btnEliminaSerie)

        // Riferimenti ai TextWatcher per prevenire bug di scrittura incontrollata durante il riciclo delle viste
        var kgWatcher: TextWatcher? = null
        var repsWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SerieViewHolder {
        // Gonfia il layout dell'XML che rappresenta la singola riga della serie
        val view = LayoutInflater
            .from(parent.context)
            .inflate(
                R.layout.item_serie,
                parent,
                false
            )

        return SerieViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: SerieViewHolder,
        position: Int
    ) {

        val data = listaSerie[position]

        // Imposta il numero progressivo del set
        data.numeroSet = position + 1

        holder.txtNumero.text =
            data.numeroSet.toString()

        // Rimuove i vecchi TextWatcher prima di impostare il testo per evitare loop di eventi dovuti al riciclo delle viste
        holder.kgWatcher?.let {
            holder.editKg.removeTextChangedListener(it)
        }

        holder.repsWatcher?.let {
            holder.editReps.removeTextChangedListener(it)
        }

        // Imposta i valori di peso e ripetizioni nei campi di input
        holder.editKg.setText(
            if (data.kg > 0)
                data.kg.toString()
            else
                ""
        )

        holder.editReps.setText(
            if (data.reps > 0)
                data.reps.toString()
            else
                ""
        )

        // Mostra o nasconde la checkbox in base alla modalità
        if (isModifica) {

            holder.chkCompleto.visibility =
                View.GONE

        } else {

            holder.chkCompleto.visibility =
                View.VISIBLE
        }

        // Resetta temporaneamente il listener della checkbox per evitare trigger indesiderati durante il binding
        holder.chkCompleto.setOnCheckedChangeListener(null)

        holder.chkCompleto.isChecked =
            data.completata

        // Aggiorna il colore della riga in base allo stato
        aggiornaColoreSerie(
            holder,
            data.completata
        )

        // Imposta il listener per gestire il cambio di stato della checkbox
        holder.chkCompleto
            .setOnCheckedChangeListener { _, checked ->

                data.completata = checked

                aggiornaColoreSerie(
                    holder,
                    checked
                )

                onStatoSerieCambiato(
                    data,
                    checked
                )
            }

        // Crea e assegna il TextWatcher per monitorare le modifiche in tempo reale sul peso
        holder.kgWatcher =
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {

                    data.kg =
                        s
                            .toString()
                            .toDoubleOrNull()
                            ?: 0.0

                    onDatoModificato()

                    // Se la serie è già completata e salvata nel DB, invia un aggiornamento immediato
                    if (
                        data.completata &&
                        data.serieEseguitaId != null
                    ) {
                        onSerieModificata(data)
                    }
                }
            }

        holder.editKg.addTextChangedListener(
            holder.kgWatcher
        )

        // Crea e assegna il TextWatcher per monitorare le modifiche sulle ripetizioni
        holder.repsWatcher =
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {

                    data.reps =
                        s
                            .toString()
                            .toIntOrNull()
                            ?: 0

                    onDatoModificato()

                    // Aggiorna i dati nel DB in tempo reale se la serie è attiva e completata
                    if (
                        data.completata &&
                        data.serieEseguitaId != null
                    ) {
                        onSerieModificata(data)
                    }
                }
            }

        holder.editReps.addTextChangedListener(
            holder.repsWatcher
        )

        // Gestisce il pulsante di eliminazione in modalità modifica
        if (isModifica) {
            holder.btnElimina.visibility = View.VISIBLE

            holder.btnElimina.setOnClickListener {
                val pos = holder.bindingAdapterPosition

                if (pos != RecyclerView.NO_POSITION) {
                    onEliminaSerie(pos)
                }
            }
        } else {
            holder.btnElimina.visibility = View.GONE
            holder.btnElimina.setOnClickListener(null)
        }
    }

    // Cambia il colore della riga della serie
    private fun aggiornaColoreSerie(
        holder: SerieViewHolder,
        completata: Boolean
    ) {

        if (completata) {

            holder.containerRow.setBackgroundColor(
                Color.parseColor("#1B5E20")
            )

        } else {

            holder.containerRow.setBackgroundColor(
                Color.TRANSPARENT
            )
        }
    }

    override fun getItemCount(): Int =
        listaSerie.size
}