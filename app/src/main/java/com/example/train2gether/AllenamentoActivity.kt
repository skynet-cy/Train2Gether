package com.example.train2gether

import android.os.Bundle
import android.os.CountDownTimer
import android.text.InputType
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

    private var isModifica: Boolean = false
    private var countDownTimer: CountDownTimer? = null
    private lateinit var txtTimer: TextView
    private var tempoRecuperoSecondi: Long = 60

    private var schedaAttuale: SchedaAllenamento? = null
    private val listaEserciziMutable = mutableListOf<EsercizioConSerie>()
    private lateinit var adapter: EsercizioAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_allenamento)

        // 1. Leggiamo la modalità dall'Intent
        isModifica = intent.getBooleanExtra("IS_MODIFICA", false)

        txtTimer = findViewById(R.id.txtTimer)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewEsercizi)
        val btnAggiungiEsercizio: Button = findViewById(R.id.btnAggiungiEsercizio)
        val btnSalvaAllenamento: Button = findViewById(R.id.btnSalvaAllenamento)

        val schedaId = intent.getStringExtra("SCHEDA_ID")
        val tutteLeSchede = GestoreSchede.caricaTutteLeSchede(this)
        schedaAttuale = tutteLeSchede.find { it.id == schedaId }

        // 2. Personalizzazione della grafica in base alla modalità
        if (isModifica) {
            title = "Modifica: ${schedaAttuale?.nomeScheda ?: "Scheda"}"
            txtTimer.visibility = View.GONE // Nascosto in modalità modifica
            btnSalvaAllenamento.text = "💾 Salva Modifiche Scheda"
        } else {
            title = "Allenamento: ${schedaAttuale?.nomeScheda ?: "Workout"}"
            txtTimer.visibility = View.VISIBLE // Visibile in allenamento
            btnSalvaAllenamento.text = "✅ Termina Allenamento"
        }

        if (schedaAttuale != null) {
            listaEserciziMutable.addAll(schedaAttuale!!.listaEsercizi)
        } else {
            // Esercizio di default se la scheda è nuova/vuota
            listaEserciziMutable.add(
                EsercizioConSerie(
                    nomeEsercizio = "Panca Piana",
                    listaSerie = mutableListOf(
                        SerieEsercizio(1, 20.0, 10),
                        SerieEsercizio(2, 22.5, 8)
                    )
                )
            )
        }

        aggiornaTestoTimerIniziale()
        txtTimer.setOnClickListener { mostraDialogImpostaTimer() }

        // 3. Inizializzazione della RecyclerView con i 3 parametri richiesti
        recyclerView.layoutManager = LinearLayoutManager(this)
        // In AllenamentoActivity.kt dentro onCreate():

        adapter = EsercizioAdapter(
            listaEsercizi = listaEserciziMutable,
            isModifica = isModifica,
            onSerieSpuntata = { tempoSecondi ->
                // Avvia il timer usando i secondi dell'esercizio spuntato
                avviaTimerRecupero(tempoSecondi * 1000)
            }
        )
        recyclerView.adapter = adapter

        // ➕ AGGIUNGI UN ESERCIZIO
        btnAggiungiEsercizio.setOnClickListener {
            mostraPopupNuovoEsercizio()
        }

        // 💾 SALVA MODIFICHE / TERMINA
        btnSalvaAllenamento.setOnClickListener {
            salvaModificheScheda()
        }
    }

    private fun mostraPopupNuovoEsercizio() {
        val input = EditText(this)
        input.hint = "Es. Squat, Lat Machine..."

        AlertDialog.Builder(this)
            .setTitle("Nuovo Esercizio")
            .setMessage("Inserisci il nome dell'esercizio:")
            .setView(input)
            .setPositiveButton("Aggiungi") { _, _ ->
                val nome = input.text.toString().trim()
                if (nome.isNotEmpty()) {
                    val nuovoEsercizio = EsercizioConSerie(
                        nomeEsercizio = nome,
                        listaSerie = mutableListOf(SerieEsercizio(1, 0.0, 0))
                    )
                    listaEserciziMutable.add(nuovoEsercizio)
                    adapter.notifyItemInserted(listaEserciziMutable.size - 1)
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun salvaModificheScheda() {
        val scheda = schedaAttuale ?: return

        if (isModifica) {
            // Modalità Modifica: Salva direttamente le modifiche alla scheda
            val schedaAggiornata = SchedaAllenamento(
                id = scheda.id,
                nomeScheda = scheda.nomeScheda,
                listaEsercizi = listaEserciziMutable
            )
            GestoreSchede.salvaScheda(this, schedaAggiornata)
            Toast.makeText(this, "Scheda aggiornata!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            // Modalità Avvia (Allenamento): Chiede all'utente se desidera salvare i nuovi pesi/reps
            AlertDialog.Builder(this)
                .setTitle("Termina Allenamento")
                .setMessage("Vuoi salvare i pesi e le ripetizioni inserite come nuovo standard per questa scheda?")
                .setPositiveButton("Sì, aggiorna scheda") { _, _ ->
                    val schedaAggiornata = SchedaAllenamento(
                        id = scheda.id,
                        nomeScheda = scheda.nomeScheda,
                        listaEsercizi = listaEserciziMutable
                    )
                    GestoreSchede.salvaScheda(this, schedaAggiornata)
                    Toast.makeText(this, "Allenamento completato e scheda aggiornata!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNegativeButton("No, termina e basta") { _, _ ->
                    Toast.makeText(this, "Ottimo allenamento!", Toast.LENGTH_SHORT).show()
                    finish()
                }
                .setNeutralButton("Annulla", null)
                .show()
        }
    }

    private fun aggiornaTestoTimerIniziale() {
        txtTimer.text = "⏱️ Recupero: ${tempoRecuperoSecondi}s (Tocca per cambiare)"
    }

    private fun mostraDialogImpostaTimer() {
        val opzioni = arrayOf("30 secondi", "60 secondi", "90 secondi", "120 secondi", "Personalizzato")
        AlertDialog.Builder(this)
            .setTitle("Tempo di recupero")
            .setItems(opzioni) { _, which ->
                when (which) {
                    0 -> impostaNuovoTempo(30)
                    1 -> impostaNuovoTempo(60)
                    2 -> impostaNuovoTempo(90)
                    3 -> impostaNuovoTempo(120)
                    4 -> mostraInputCustomTimer()
                }
            }
            .show()
    }

    private fun mostraInputCustomTimer() {
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_NUMBER
        AlertDialog.Builder(this)
            .setTitle("Imposta Secondi")
            .setView(input)
            .setPositiveButton("Ok") { _, _ ->
                val secondi = input.text.toString().toLongOrNull()
                if (secondi != null && secondi > 0) impostaNuovoTempo(secondi)
            }
            .setNegativeButton("Annulla", null)
            .show()
    }

    private fun impostaNuovoTempo(secondi: Long) {
        tempoRecuperoSecondi = secondi
        countDownTimer?.cancel()
        aggiornaTestoTimerIniziale()
    }

    private fun avviaTimerRecupero(tempoInMilli: Long) {
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(tempoInMilli, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondiRimasti = millisUntilFinished / 1000
                txtTimer.text = String.format("⏱️ Recupero in corso: %02d s", secondiRimasti)
            }

            override fun onFinish() {
                txtTimer.text = "🔔 Recupero Terminato!"
                Toast.makeText(this@AllenamentoActivity, "Prossima serie!", Toast.LENGTH_SHORT).show()
            }
        }.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}