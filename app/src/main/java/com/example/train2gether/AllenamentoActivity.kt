package com.example.train2gether

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AllenamentoActivity : AppCompatActivity() {

    private lateinit var txtNomeSchedaTitle: TextView
    private lateinit var txtTimer: TextView
    private lateinit var recyclerEsercizi: RecyclerView
    private lateinit var btnAggiungiEsercizio: Button
    private lateinit var btnSalvaOppureTermina: Button

    private lateinit var adapter: EsercizioAdapter
    private val listaEserciziMutable = mutableListOf<EsercizioConSerie>()

    private var schedaId: String? = null
    private var isModifica: Boolean = false
    private var schedaAttuale: SchedaAllenamento? = null

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Imposta la barra di stato nera o trasparente
        window.statusBarColor = android.graphics.Color.parseColor("#121212")

        // Per nascondere del tutto la Action Bar di sistema se presente:
        supportActionBar?.hide()
        setContentView(R.layout.activity_allenamento)

        // Inizializzazione Viste
        txtNomeSchedaTitle = findViewById(R.id.txtNomeSchedaTitle)
        txtTimer = findViewById(R.id.txtTimer)
        recyclerEsercizi = findViewById(R.id.recyclerEsercizi)
        btnAggiungiEsercizio = findViewById(R.id.btnAggiungiEsercizio)
        btnSalvaOppureTermina = findViewById(R.id.btnSalvaOppureTermina)

        // Svuotiamo/nascondiamo la scritta del timer in alto all'avvio
        txtTimer.text = ""
        txtTimer.visibility = View.GONE

        // Recupera dati passati tramite Intent
        schedaId = intent.getStringExtra("SCHEDA_ID")
        isModifica = intent.getBooleanExtra("IS_MODIFICA", false)

        if (schedaId != null) {
            schedaAttuale = GestoreSchede.caricaTutteLeSchede(this).find { it.id == schedaId }
        }

        if (schedaAttuale != null) {
            txtNomeSchedaTitle.text = schedaAttuale!!.nomeScheda
            listaEserciziMutable.clear()
            listaEserciziMutable.addAll(schedaAttuale!!.listaEsercizi)
        } else {
            txtNomeSchedaTitle.text = "Nuova Scheda"
        }

        // Impostazione testo bottone finale
        if (isModifica) {
            btnSalvaOppureTermina.text = "Salva Modifiche"
        } else {
            btnSalvaOppureTermina.text = "Termina Allenamento"
        }

        // Configurazione RecyclerView Esercizi
        recyclerEsercizi.layoutManager = LinearLayoutManager(this)

        adapter = EsercizioAdapter(
            listaEsercizi = listaEserciziMutable,
            isModifica = isModifica,
            onSerieSpuntata = { tempoSecondi ->
                // Avvia il timer specifico dell'esercizio spuntato
                avviaTimerRecupero(tempoSecondi * 1000)
            }
        )
        recyclerEsercizi.adapter = adapter

        // Bottone Aggiungi Esercizio
        btnAggiungiEsercizio.setOnClickListener {
            mostraPopupNuovoEsercizio()
        }

        // Bottone Salva / Termina
        btnSalvaOppureTermina.setOnClickListener {
            if (isModifica) {
                salvaScheda()
                Toast.makeText(this, "Scheda salvata con successo!", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Allenamento terminato e registrato! 💪", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun avviaTimerRecupero(millis: Long) {
        countDownTimer?.cancel() // Cancella eventuali timer attivi in precedenza

        txtTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minuti = totalSeconds / 60
                val secondi = totalSeconds % 60

                // Mostra il conto alla rovescia formattato come: "Recupero in corso: 02:00"
                txtTimer.text = String.format("Recupero in corso: %02d:%02d", minuti, secondi)
            }

            override fun onFinish() {
                txtTimer.text = "Recupero Terminato! 💪"

                // Scompare dopo 3 secondi dalla fine del recupero
                txtTimer.postDelayed({
                    if (!isFinishing) {
                        txtTimer.text = ""
                        txtTimer.visibility = View.GONE
                    }
                }, 3000)
            }
        }.start()
    }

    private fun mostraPopupNuovoEsercizio() {
        val inputNomeEsercizio = EditText(this).apply {
            hint = "Es. Panca Piana, Squat, Lat Machine..."
        }

        AlertDialog.Builder(this)
            .setTitle("Nuovo Esercizio")
            .setMessage("Inserisci il nome dell'esercizio:")
            .setView(inputNomeEsercizio)
            .setPositiveButton("Aggiungi") { _, _ ->
                val nome = inputNomeEsercizio.text.toString().trim()
                if (nome.isNotEmpty()) {
                    val nuovoEsercizio = EsercizioConSerie(
                        nomeEsercizio = nome,
                        tempoRecuperoSecondi = 60, // Default 60s
                        listaSerie = mutableListOf(SerieEsercizio(1, 0.0, 0))
                    )
                    listaEserciziMutable.add(nuovoEsercizio)
                    adapter.notifyItemInserted(listaEserciziMutable.size - 1)
                } else {
                    Toast.makeText(this, "Inserisci un nome valido!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun salvaScheda() {
        val idScheda = schedaId ?: System.currentTimeMillis().toString()
        val nomeScheda = txtNomeSchedaTitle.text.toString()

        val schedaDaSalvare = SchedaAllenamento(
            id = idScheda,
            nomeScheda = nomeScheda,
            listaEsercizi = listaEserciziMutable
        )

        GestoreSchede.salvaScheda(this, schedaDaSalvare)
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}