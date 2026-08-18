package com.example.train2gether

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerSchede: RecyclerView
    private val listaSchedeMutable = mutableListOf<SchedaAllenamento>()
    private lateinit var adapter: SchedaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())

            // Converte 20dp (il margine laterale che vogliamo) in pixel reali dello schermo
            val paddingLateralePx = (20 * resources.displayMetrics.density).toInt()
            val paddingTopPx = (16 * resources.displayMetrics.density).toInt()

            // Applica il padding aggiungendo l'altezza della status bar in alto
            v.setPadding(
                paddingLateralePx,
                systemBars.top + paddingTopPx,
                paddingLateralePx,
                systemBars.bottom
            )
            insets
        }

        val btnNuovaScheda: Button = findViewById(R.id.btnNuovaScheda)
        recyclerSchede = findViewById(R.id.recyclerViewMainSchede)

        // Configurazione della RecyclerView
        recyclerSchede.layoutManager = LinearLayoutManager(this)

        adapter = SchedaAdapter(
            listaSchede = listaSchedeMutable,
            onAvviaClick = { scheda ->
                val intent = Intent(this, AllenamentoActivity::class.java)
                intent.putExtra("SCHEDA_ID", scheda.id)
                intent.putExtra("IS_MODIFICA", false) // 👈 MODALITÀ ALLENAMENTO
                startActivity(intent)
            },
            onModificaClick = { scheda ->
                val intent = Intent(this, AllenamentoActivity::class.java)
                intent.putExtra("SCHEDA_ID", scheda.id)
                intent.putExtra("IS_MODIFICA", true)  // 👈 MODALITÀ EDITOR SCHEDA
                startActivity(intent)
            },
            onEliminaClick = { scheda ->
                GestoreSchede.eliminaScheda(this, scheda.id)
                caricaSchedeSalvate()
                Toast.makeText(this, "Scheda eliminata", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerSchede.adapter = adapter

        btnNuovaScheda.setOnClickListener {
            mostraPopupNuovaScheda()
        }
    }

    override fun onResume() {
        super.onResume()
        caricaSchedeSalvate()
    }

    private fun caricaSchedeSalvate() {
        listaSchedeMutable.clear()
        listaSchedeMutable.addAll(GestoreSchede.caricaTutteLeSchede(this))
        adapter.notifyDataSetChanged()
    }

    private fun mostraPopupNuovaScheda() {
        val inputNomeScheda = EditText(this)
        inputNomeScheda.hint = "Es. Chest & Biceps"

        AlertDialog.Builder(this)
            .setTitle("Nuova Scheda")
            .setMessage("Inserisci il nome della tua scheda:")
            .setView(inputNomeScheda)
            .setPositiveButton("Salva") { _, _ ->
                val nomeScheda = inputNomeScheda.text.toString().trim()

                if (nomeScheda.isNotEmpty()) {
                    val nuovaScheda = SchedaAllenamento(
                        id = System.currentTimeMillis().toString(),
                        nomeScheda = nomeScheda,
                        listaEsercizi = listOf(
                            EsercizioConSerie(
                                nomeEsercizio = "Panca Piana",
                                listaSerie = mutableListOf(SerieEsercizio(1, 20.0, 10))
                            )
                        )
                    )

                    GestoreSchede.salvaScheda(this, nuovaScheda)
                    caricaSchedeSalvate()
                } else {
                    Toast.makeText(this, "Inserisci un nome valido!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}