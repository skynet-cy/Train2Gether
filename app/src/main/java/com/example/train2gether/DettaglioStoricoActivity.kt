package com.example.train2gether

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.train2gether.data.AppDatabase
import com.example.train2gether.data.SerieEseguita
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DettaglioStoricoActivity : AppCompatActivity() {

    private lateinit var txtTitolo: TextView
    private lateinit var txtData: TextView
    private lateinit var recyclerEsercizi: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.parseColor("#121212")
        supportActionBar?.hide()
        setContentView(R.layout.activity_dettaglio_storico)

        txtTitolo = findViewById(R.id.txtTitoloDettaglio)
        txtData = findViewById(R.id.txtSottotitoloData)
        recyclerEsercizi = findViewById(R.id.recyclerDettaglioEsercizi)
        recyclerEsercizi.layoutManager = LinearLayoutManager(this)

        val allenamentoId = intent.getIntExtra("ALLENAMENTO_ID", -1)
        if (allenamentoId != -1) {
            caricaDettagli(allenamentoId)
        }
    }

    private fun caricaDettagli(id: Int) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@DettaglioStoricoActivity)
            val allenamentoCompleto = db.allenamentoDao().getAllenamentoCompleto(id)

            if (allenamentoCompleto != null) {
                val allenamento = allenamentoCompleto.allenamento
                txtTitolo.text = allenamento.nomeScheda ?: "Allenamento Libero"

                val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
                val dataStr = sdf.format(Date(allenamento.dataInizio))
                val min = allenamento.durataSecondi / 60
                txtData.text = "$dataStr • Durata: ${min}m"

                val eserciziRaggruppati = allenamentoCompleto.seriePerEsercizio().values.toList()
                recyclerEsercizi.adapter = EsercizioDettaglioStoricoAdapter(eserciziRaggruppati)
            }
        }
    }
}

class EsercizioDettaglioStoricoAdapter(
    private val listaEsercizi: List<List<SerieEseguita>>
) : RecyclerView.Adapter<EsercizioDettaglioStoricoAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtNomeEsercizio: TextView = view.findViewById(R.id.txtNomeEsercizioDettaglio)
        val recyclerSerieEseguiteDettaglio: RecyclerView = view.findViewById(R.id.recyclerSerieEseguiteDettaglio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_esercizio_dettaglio, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val serieList = listaEsercizi[position]
        val primoElemento = serieList.firstOrNull()
        holder.txtNomeEsercizio.text = primoElemento?.nomeEsercizio ?: "Esercizio"

        holder.recyclerSerieEseguiteDettaglio.layoutManager = LinearLayoutManager(holder.itemView.context)
        holder.recyclerSerieEseguiteDettaglio.adapter = SerieEseguiteAdapter(serieList)
    }

    override fun getItemCount(): Int = listaEsercizi.size
}

class SerieEseguiteAdapter(
    private val listaSerie: List<SerieEseguita>
) : RecyclerView.Adapter<SerieEseguiteAdapter.SerieViewHolder>() {

    class SerieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val txtSet: TextView = view.findViewById(R.id.txtSetNumero)
        val txtDati: TextView = view.findViewById(R.id.txtDatiSet)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SerieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_serie_eseguita, parent, false)
        return SerieViewHolder(view)
    }

    override fun onBindViewHolder(holder: SerieViewHolder, position: Int) {
        val serie = listaSerie[position]
        holder.txtSet.text = "Set ${serie.numeroSerie}"
        holder.txtDati.text = "${serie.peso} kg × ${serie.ripetizioni} reps"
    }

    override fun getItemCount(): Int = listaSerie.size
}