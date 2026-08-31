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

        var kgWatcher: TextWatcher? = null
        var repsWatcher: TextWatcher? = null
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SerieViewHolder {

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

        data.numeroSet = position + 1

        holder.txtNumero.text =
            data.numeroSet.toString()

        holder.kgWatcher?.let {
            holder.editKg.removeTextChangedListener(it)
        }

        holder.repsWatcher?.let {
            holder.editReps.removeTextChangedListener(it)
        }

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

        if (isModifica) {

            holder.chkCompleto.visibility =
                View.GONE

        } else {

            holder.chkCompleto.visibility =
                View.VISIBLE
        }

        holder.chkCompleto.setOnCheckedChangeListener(null)

        holder.chkCompleto.isChecked =
            data.completata

        aggiornaColoreSerie(
            holder,
            data.completata
        )

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


        holder.btnElimina.setOnClickListener {

            val pos =
                holder.bindingAdapterPosition

            if (
                pos !=
                RecyclerView.NO_POSITION
            ) {
                onEliminaSerie(pos)
            }
        }
    }

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