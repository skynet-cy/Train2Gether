package com.example.train2gether

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.Button
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.train2gether.data.AppDatabase
import com.example.train2gether.data.Esercizio
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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
    private var isDataModified: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.parseColor("#121212")

        supportActionBar?.hide()
        setContentView(R.layout.activity_allenamento)

        txtNomeSchedaTitle = findViewById(R.id.txtNomeSchedaTitle)
        txtTimer = findViewById(R.id.txtTimer)
        recyclerEsercizi = findViewById(R.id.recyclerEsercizi)
        btnAggiungiEsercizio = findViewById(R.id.btnAggiungiEsercizio)
        btnSalvaOppureTermina = findViewById(R.id.btnSalvaOppureTermina)

        txtTimer.text = ""
        txtTimer.visibility = View.GONE

        txtTimer.setOnClickListener {
            fermaTimerRecupero()
        }

        schedaId = intent.getStringExtra("SCHEDA_ID")
        isModifica = intent.getBooleanExtra("IS_MODIFICA", false)

        if (schedaId != null) {
            lifecycleScope.launch {
                val tutteLeSchede = GestoreSchede.caricaTutteLeSchede(this@AllenamentoActivity)
                schedaAttuale = tutteLeSchede.find { it.id == schedaId }

                if (schedaAttuale != null) {
                    txtNomeSchedaTitle.text = schedaAttuale!!.nomeScheda
                    listaEserciziMutable.clear()
                    listaEserciziMutable.addAll(schedaAttuale!!.listaEsercizi)
                    adapter.notifyDataSetChanged()
                }
            }
        } else {
            txtNomeSchedaTitle.text = "Nuova Scheda"
        }

        if (isModifica) {
            btnSalvaOppureTermina.text = "Salva Modifiche"
        } else {
            btnSalvaOppureTermina.text = "Termina Allenamento"
        }

        recyclerEsercizi.layoutManager = LinearLayoutManager(this)

        adapter = EsercizioAdapter(
            listaEsercizi = listaEserciziMutable,
            isModifica = isModifica,
            onSerieSpuntata = { tempoSecondi ->
                isDataModified = true
                avviaTimerRecupero(tempoSecondi * 1000L)
            },
            onDatoModificato = {
                isDataModified = true
            }
        )
        recyclerEsercizi.adapter = adapter

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                gestisciUscita()
            }
        })

        btnAggiungiEsercizio.setOnClickListener {
            mostraPopupNuovoEsercizio()
        }

        btnSalvaOppureTermina.setOnClickListener {
            if (!isModifica) {
                gestisciUscita()
            } else {
                val idScheda = schedaId ?: System.currentTimeMillis().toString()
                val nomeScheda = txtNomeSchedaTitle.text.toString()
                val schedaDaSalvare = SchedaAllenamento(
                    id = idScheda,
                    nomeScheda = nomeScheda,
                    listaEsercizi = listaEserciziMutable
                )

                lifecycleScope.launch {
                    GestoreSchede.salvaScheda(this@AllenamentoActivity, schedaDaSalvare)
                    isDataModified = false
                    Toast.makeText(this@AllenamentoActivity, "Modifiche salvate con successo!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun gestisciUscita() {
        if (isDataModified) {
            AlertDialog.Builder(this)
                .setTitle("Modifiche non salvate")
                .setMessage("Hai modificato i dati dell'allenamento. Vuoi salvarli prima di uscire?")
                .setPositiveButton("Salva") { _, _ ->
                    val idScheda = schedaId ?: System.currentTimeMillis().toString()
                    val nomeScheda = txtNomeSchedaTitle.text.toString()
                    val schedaDaSalvare = SchedaAllenamento(
                        id = idScheda,
                        nomeScheda = nomeScheda,
                        listaEsercizi = listaEserciziMutable
                    )

                    lifecycleScope.launch {
                        GestoreSchede.salvaScheda(this@AllenamentoActivity, schedaDaSalvare)
                        isDataModified = false
                        Toast.makeText(this@AllenamentoActivity, "Scheda salvata!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                }
                .setNegativeButton("Esci senza salvare") { _, _ ->
                    isDataModified = false
                    finish()
                }
                .setNeutralButton("Annulla", null)
                .show()
        } else {
            val idScheda = schedaId ?: System.currentTimeMillis().toString()
            val nomeScheda = txtNomeSchedaTitle.text.toString()
            val schedaDaSalvare = SchedaAllenamento(
                id = idScheda,
                nomeScheda = nomeScheda,
                listaEsercizi = listaEserciziMutable
            )

            lifecycleScope.launch {
                GestoreSchede.salvaScheda(this@AllenamentoActivity, schedaDaSalvare)
                Toast.makeText(this@AllenamentoActivity, "Allenamento salvato con successo! 💪", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }

    private fun avviaTimerRecupero(millis: Long) {
        countDownTimer?.cancel()
        txtTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(millis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minuti = totalSeconds / 60
                val secondi = totalSeconds % 60
                txtTimer.text = String.format("Recupero in corso: %02d:%02d (Tocca per saltare)", minuti, secondi)
            }

            override fun onFinish() {
                txtTimer.text = "Recupero Terminato! 💪"
                txtTimer.postDelayed({
                    if (!isFinishing) {
                        txtTimer.text = ""
                        txtTimer.visibility = View.GONE
                    }
                }, 3000)
            }
        }.start()
    }

    private fun fermaTimerRecupero() {
        countDownTimer?.cancel()
        countDownTimer = null
        txtTimer.text = "Recupero saltato ⏭️"
        txtTimer.postDelayed({
            if (!isFinishing) {
                txtTimer.text = ""
                txtTimer.visibility = View.GONE
            }
        }, 1000)
    }

    private fun mostraPopupNuovoEsercizio() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_selezione_esercizio, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchEsercizio)
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chipGroupGruppi)
        val recyclerCatalogo = dialogView.findViewById<RecyclerView>(R.id.recyclerCatalogo)

        var listaCompleta = listOf<Esercizio>()
        val listaFiltrata = mutableListOf<Esercizio>()
        var dialogAdapter: CatalogoDialogAdapter? = null

        val dialog = AlertDialog.Builder(this)
            .setTitle("Seleziona Esercizio")
            .setView(dialogView)
            .setNegativeButton("Annulla", null)
            .create()

        lifecycleScope.launch(Dispatchers.IO) {
            val db = AppDatabase.getDatabase(this@AllenamentoActivity)
            listaCompleta = db.esercizioDao().getTuttiEsercizi()
            listaFiltrata.addAll(listaCompleta)

            withContext(Dispatchers.Main) {
                dialogAdapter = CatalogoDialogAdapter(listaFiltrata) { esercizioSelezionato ->
                    val nuovoEsercizio = EsercizioConSerie(
                        nomeEsercizio = esercizioSelezionato.nome,
                        tempoRecuperoSecondi = 60,
                        listaSerie = mutableListOf(SerieEsercizio(1, 0.0, 0))
                    )
                    listaEserciziMutable.add(nuovoEsercizio)
                    adapter.notifyItemInserted(listaEserciziMutable.size - 1)
                    isDataModified = true
                    dialog.dismiss()
                }
                recyclerCatalogo.layoutManager = LinearLayoutManager(this@AllenamentoActivity)
                recyclerCatalogo.adapter = dialogAdapter
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false
            override fun onQueryTextChange(newText: String?): Boolean {
                val query = newText.orEmpty().lowercase()
                listaFiltrata.clear()
                listaFiltrata.addAll(listaCompleta.filter { it.nome.lowercase().contains(query) })
                dialogAdapter?.notifyDataSetChanged()
                return true
            }
        })

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val chipId = checkedIds.firstOrNull()
            val gruppoSelezionato = if (chipId == R.id.chipPetto) {
                "Petto"
            } else if (chipId == R.id.chipBicipiti) {
                "Bicipiti"
            } else if (chipId == R.id.chipTricipiti) {
                "Tricipiti"
            } else if (chipId == R.id.chipSpalle) {
                "Spalle"
            } else if (chipId == R.id.chipDorsali) {
                "Dorsali"
            } else if (chipId == R.id.chipGambe) {
                "Quadricipiti"
            } else {
                "Tutti"
            }

            listaFiltrata.clear()
            if (gruppoSelezionato == "Tutti") {
                listaFiltrata.addAll(listaCompleta)
            } else {
                listaFiltrata.addAll(listaCompleta.filter {
                    it.gruppoMuscolare.equals(gruppoSelezionato, ignoreCase = true)
                })
            }
            dialogAdapter?.notifyDataSetChanged()
        }

        dialog.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}