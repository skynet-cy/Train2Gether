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
    private val onSerieSpuntata: () -> Unit,
    private val onEliminaSerie: (Int) -> Unit
) : RecyclerView.Adapter<SerieAdapter.SerieViewHolder>() {

    class SerieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val containerRow: LinearLayout = view as LinearLayout
        val txtNumero: TextView = view.findViewById(R.id.txtNumeroSerie)
        val editKg: EditText = view.findViewById(R.id.editKgSerie)
        val editReps: EditText = view.findViewById(R.id.editRipetizioni)
        val chkCompleto: CheckBox = view.findViewById(R.id.chkCompleto)
        val btnElimina: ImageButton = view.findViewById(R.id.btnEliminaSerie)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SerieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_serie, parent, false)
        return SerieViewHolder(view)
    }

    override fun onBindViewHolder(holder: SerieViewHolder, position: Int) {
        val data = listaSerie[position]

        data.numeroSet = position + 1
        holder.txtNumero.text = data.numeroSet.toString()
        holder.editKg.setText(if (data.kg > 0) data.kg.toString() else "")
        holder.editReps.setText(if (data.reps > 0) data.reps.toString() else "")

        if (isModifica) {
            holder.chkCompleto.visibility = View.GONE
        } else {
            holder.chkCompleto.visibility = View.VISIBLE
        }

        holder.editKg.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                data.kg = s.toString().toDoubleOrNull() ?: 0.0
            }
        })

        holder.editReps.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                data.reps = s.toString().toIntOrNull() ?: 0
            }
        })

        holder.chkCompleto.setOnClickListener {
            if (holder.chkCompleto.isChecked) {
                holder.containerRow.setBackgroundColor(Color.parseColor("#1B5E20")) // Verde scuro per tema dark
                onSerieSpuntata()
            } else {
                holder.containerRow.setBackgroundColor(Color.TRANSPARENT)
            }
        }

        holder.btnElimina.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onEliminaSerie(pos)
            }
        }
    }

    override fun getItemCount(): Int = listaSerie.size
}