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
import com.example.train2gether.data.Allenamento
import com.example.train2gether.data.AllenamentoDao
import com.example.train2gether.data.AppDatabase
import com.example.train2gether.data.Esercizio
import com.example.train2gether.data.SerieEseguita
import com.google.android.material.chip.ChipGroup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class AllenamentoActivity : AppCompatActivity() {

    private lateinit var txtNomeSchedaTitle: TextView
    private lateinit var txtTimer: TextView
    private lateinit var recyclerEsercizi: RecyclerView
    private lateinit var btnAggiungiEsercizio: Button
    private lateinit var btnSalvaOppureTermina: Button
    private lateinit var adapter: EsercizioAdapter
    private lateinit var allenamentoDao: AllenamentoDao

    private val listaEserciziMutable = mutableListOf<EsercizioConSerie>()
    private val serieDbMutex = Mutex()

    private var schedaId: String? = null
    private var isModifica = false
    private var schedaAttuale: SchedaAllenamento? = null
    private var countDownTimer: CountDownTimer? = null
    private var isDataModified = false

    // Allenamento attualmente aperto nel DB
    private var allenamentoId: Int? = null
    private var tempoInizioMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = android.graphics.Color.parseColor("#121212")
        supportActionBar?.hide()
        setContentView(R.layout.activity_allenamento)

        // View
        txtNomeSchedaTitle = findViewById(R.id.txtNomeSchedaTitle)
        txtTimer = findViewById(R.id.txtTimer)
        recyclerEsercizi = findViewById(R.id.recyclerEsercizi)
        btnAggiungiEsercizio = findViewById(R.id.btnAggiungiEsercizio)
        btnSalvaOppureTermina = findViewById(R.id.btnSalvaOppureTermina)

        // Database
        allenamentoDao = AppDatabase.getDatabase(this).allenamentoDao()

        // Dati ricevuti
        schedaId = intent.getStringExtra("SCHEDA_ID")
        isModifica = intent.getBooleanExtra("IS_MODIFICA", false)

        // Timer
        txtTimer.text = ""
        txtTimer.visibility = View.GONE
        txtTimer.setOnClickListener { fermaTimerRecupero() }

        // RecyclerView
        recyclerEsercizi.layoutManager = LinearLayoutManager(this)

        adapter = EsercizioAdapter(
            listaEsercizi = listaEserciziMutable,
            isModifica = isModifica,

            onStatoSerieCambiato = { esercizio, ordineEsercizio, serie, checked ->
                gestisciStatoSerie(
                    esercizio = esercizio,
                    ordineEsercizio = ordineEsercizio,
                    serie = serie,
                    completata = checked
                )
            },

            onSerieModificata = { esercizio, ordineEsercizio, serie ->
                aggiornaSerieEseguita(
                    esercizio = esercizio,
                    ordineEsercizio = ordineEsercizio,
                    serie = serie
                )
            },

            onSerieSpuntata = { tempoSecondi ->
                avviaTimerRecupero(tempoSecondi * 1000L)
            },

            onDatoModificato = {
                if (isModifica) isDataModified = true
            }
        )

        recyclerEsercizi.adapter = adapter

        btnSalvaOppureTermina.text =
            if (isModifica) "Salva Modifiche" else "Termina Allenamento"

        if (schedaId != null) {
            caricaScheda()
        } else {
            txtNomeSchedaTitle.text = "Nuova Scheda"
        }

        btnAggiungiEsercizio.setOnClickListener {
            mostraPopupNuovoEsercizio()
        }

        btnSalvaOppureTermina.setOnClickListener {
            if (isModifica) salvaModificheScheda()
            else terminaAllenamento()
        }

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    gestisciUscita()
                }
            }
        )
    }

    // =========================================================
    // CARICAMENTO SCHEDA
    // =========================================================

    private fun caricaScheda() {
        lifecycleScope.launch {
            val tutteLeSchede = GestoreSchede.caricaTutteLeSchede(this@AllenamentoActivity)
            schedaAttuale = tutteLeSchede.find { it.id == schedaId }

            val scheda = schedaAttuale

            if (scheda == null) {
                Toast.makeText(
                    this@AllenamentoActivity,
                    "Scheda non trovata",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
                return@launch
            }

            txtNomeSchedaTitle.text = scheda.nomeScheda

            listaEserciziMutable.clear()
            listaEserciziMutable.addAll(scheda.listaEsercizi)
            adapter.notifyDataSetChanged()

            // In modalità allenamento creiamo subito Allenamento con durata = 0.
            if (!isModifica && allenamentoId == null) {
                creaAllenamentoNelDatabase(scheda)
            }
        }
    }

    // =========================================================
    // CREAZIONE ALLENAMENTO
    // =========================================================

    private suspend fun creaAllenamentoNelDatabase(scheda: SchedaAllenamento) {
        tempoInizioMillis = System.currentTimeMillis()

        val nuovoId = withContext(Dispatchers.IO) {
            allenamentoDao.inserisciAllenamento(
                Allenamento(
                    schedaIdOrigine = schedaId?.toIntOrNull(),
                    nomeScheda = scheda.nomeScheda,
                    dataInizio = tempoInizioMillis,
                    durataSecondi = 0
                )
            )
        }

        allenamentoId = nuovoId.toInt()
    }

    // =========================================================
    // CHECK / UNCHECK SERIE
    // =========================================================

    private fun gestisciStatoSerie(
        esercizio: EsercizioConSerie,
        ordineEsercizio: Int,
        serie: SerieEsercizio,
        completata: Boolean
    ) {
        if (isModifica) return

        val idAllenamento = allenamentoId

        if (idAllenamento == null) {
            Toast.makeText(
                this,
                "Allenamento non ancora inizializzato",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        lifecycleScope.launch {
            serieDbMutex.withLock {
                try {

                    // CHECK -> INSERT
                    if (completata) {
                        if (serie.serieEseguitaId != null) return@withLock

                        val nuovaSerie = SerieEseguita(
                            allenamentoId = idAllenamento,
                            esercizioId = esercizio.esercizioId,
                            nomeEsercizio = esercizio.nomeEsercizio,
                            ordineEsercizio = ordineEsercizio,
                            numeroSerie = serie.numeroSet,
                            peso = serie.kg,
                            ripetizioni = serie.reps
                        )

                        val nuovoId = withContext(Dispatchers.IO) {
                            allenamentoDao.inserisciSerieEseguita(nuovaSerie)
                        }

                        serie.serieEseguitaId = nuovoId.toInt()
                        serie.completata = true
                    }

                    // UNCHECK -> DELETE
                    else {
                        val idSerie = serie.serieEseguitaId ?: return@withLock

                        val serieDaEliminare = SerieEseguita(
                            id = idSerie,
                            allenamentoId = idAllenamento,
                            esercizioId = esercizio.esercizioId,
                            nomeEsercizio = esercizio.nomeEsercizio,
                            ordineEsercizio = ordineEsercizio,
                            numeroSerie = serie.numeroSet,
                            peso = serie.kg,
                            ripetizioni = serie.reps
                        )

                        withContext(Dispatchers.IO) {
                            allenamentoDao.eliminaSerieEseguita(serieDaEliminare)
                        }

                        serie.serieEseguitaId = null
                        serie.completata = false
                    }

                } catch (e: Exception) {
                    serie.completata = serie.serieEseguitaId != null
                    adapter.notifyDataSetChanged()

                    Toast.makeText(
                        this@AllenamentoActivity,
                        "Errore durante il salvataggio della serie",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // =========================================================
    // UPDATE SERIE GIÀ SPUNTATA
    // =========================================================

    private fun aggiornaSerieEseguita(
        esercizio: EsercizioConSerie,
        ordineEsercizio: Int,
        serie: SerieEsercizio
    ) {
        if (isModifica) return

        val idAllenamento = allenamentoId ?: return
        if (serie.serieEseguitaId == null) return

        lifecycleScope.launch {
            serieDbMutex.withLock {
                val idSerie = serie.serieEseguitaId ?: return@withLock

                val serieAggiornata = SerieEseguita(
                    id = idSerie,
                    allenamentoId = idAllenamento,
                    esercizioId = esercizio.esercizioId,
                    nomeEsercizio = esercizio.nomeEsercizio,
                    ordineEsercizio = ordineEsercizio,
                    numeroSerie = serie.numeroSet,
                    peso = serie.kg,
                    ripetizioni = serie.reps
                )

                try {
                    withContext(Dispatchers.IO) {
                        allenamentoDao.aggiornaSerieEseguita(serieAggiornata)
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this@AllenamentoActivity,
                        "Errore durante l'aggiornamento della serie",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // =========================================================
    // TERMINA ALLENAMENTO
    // =========================================================

    private fun terminaAllenamento() {
        val id = allenamentoId

        if (id == null) {
            Toast.makeText(
                this,
                "Allenamento non inizializzato",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val durataSecondi =
            ((System.currentTimeMillis() - tempoInizioMillis) / 1000).toInt()

        lifecycleScope.launch {
            /*
             * Il mutex assicura che un eventuale INSERT della serie
             * appena spuntata venga concluso prima del controllo del DAO.
             */
            val terminato = serieDbMutex.withLock {
                withContext(Dispatchers.IO) {
                    allenamentoDao.terminaAllenamento(
                        allenamentoId = id,
                        durataSecondi = durataSecondi
                    )
                }
            }

            if (terminato) {
                allenamentoId = null

                Toast.makeText(
                    this@AllenamentoActivity,
                    "Allenamento completato e salvato! 💪",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            } else {
                Toast.makeText(
                    this@AllenamentoActivity,
                    "Completa almeno una serie prima di terminare l'allenamento",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    // =========================================================
    // MODIFICA SCHEDA
    // =========================================================

    private fun salvaModificheScheda() {
        val idScheda = schedaId ?: System.currentTimeMillis().toString()
        val nomeScheda = txtNomeSchedaTitle.text.toString()

        val schedaDaSalvare = SchedaAllenamento(
            id = idScheda,
            nomeScheda = nomeScheda,
            listaEsercizi = listaEserciziMutable
        )

        lifecycleScope.launch {
            GestoreSchede.salvaScheda(
                this@AllenamentoActivity,
                schedaDaSalvare
            )

            isDataModified = false

            Toast.makeText(
                this@AllenamentoActivity,
                "Modifiche salvate con successo!",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    // =========================================================
    // USCITA
    // =========================================================

    private fun gestisciUscita() {
        if (isModifica) {
            gestisciUscitaModificaScheda()
        } else {
            gestisciAbbandonoAllenamento()
        }
    }

    private fun gestisciUscitaModificaScheda() {
        if (!isDataModified) {
            finish()
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Modifiche non salvate")
            .setMessage("Hai modificato la scheda. Vuoi salvare le modifiche prima di uscire?")
            .setPositiveButton("Salva") { _, _ ->
                salvaModificheScheda()
            }
            .setNegativeButton("Esci senza salvare") { _, _ ->
                isDataModified = false
                finish()
            }
            .setNeutralButton("Annulla", null)
            .show()
    }

    private fun gestisciAbbandonoAllenamento() {
        AlertDialog.Builder(this)
            .setTitle("Abbandona allenamento?")
            .setMessage("Le serie completate in questo allenamento verranno eliminate.")
            .setPositiveButton("Abbandona") { _, _ ->
                eliminaAllenamentoInCorso()
            }
            .setNegativeButton("Continua allenamento", null)
            .show()
    }

    private fun eliminaAllenamentoInCorso() {
        val id = allenamentoId

        if (id == null) {
            finish()
            return
        }

        lifecycleScope.launch {
            /*
             * Aspettiamo eventuali INSERT/UPDATE/DELETE delle serie
             * prima di cancellare l'allenamento.
             */
            serieDbMutex.withLock {
                withContext(Dispatchers.IO) {
                    val allenamento = allenamentoDao.getAllenamentoById(id)

                    if (allenamento != null) {
                        /*
                         * SerieEseguita ha FK CASCADE verso Allenamento:
                         * cancellando Allenamento spariscono anche le sue serie.
                         */
                        allenamentoDao.eliminaAllenamento(allenamento)
                    }
                }
            }

            allenamentoId = null

            Toast.makeText(
                this@AllenamentoActivity,
                "Allenamento abbandonato",
                Toast.LENGTH_SHORT
            ).show()

            finish()
        }
    }

    // =========================================================
    // TIMER RECUPERO
    // =========================================================

    private fun avviaTimerRecupero(millis: Long) {
        countDownTimer?.cancel()
        txtTimer.visibility = View.VISIBLE

        countDownTimer = object : CountDownTimer(millis, 1000) {

            override fun onTick(millisUntilFinished: Long) {
                val totalSeconds = millisUntilFinished / 1000
                val minuti = totalSeconds / 60
                val secondi = totalSeconds % 60

                txtTimer.text = String.format(
                    "Recupero in corso: %02d:%02d (Tocca per saltare)",
                    minuti,
                    secondi
                )
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

    // =========================================================
    // AGGIUNTA ESERCIZIO
    // =========================================================

    private fun mostraPopupNuovoEsercizio() {
        val dialogView = layoutInflater.inflate(
            R.layout.dialog_selezione_esercizio,
            null
        )

        val searchView =
            dialogView.findViewById<SearchView>(R.id.searchEsercizio)

        val chipGroup =
            dialogView.findViewById<ChipGroup>(R.id.chipGroupGruppi)

        val recyclerCatalogo =
            dialogView.findViewById<RecyclerView>(R.id.recyclerCatalogo)

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

                    /*
                     * IMPORTANTE:
                     * conserviamo anche l'ID Room dell'esercizio.
                     */
                    val nuovoEsercizio = EsercizioConSerie(
                        nomeEsercizio = esercizioSelezionato.nome,
                        tempoRecuperoSecondi = 60,
                        listaSerie = mutableListOf(
                            SerieEsercizio(
                                numeroSet = 1,
                                kg = 0.0,
                                reps = 0
                            )
                        ),
                        esercizioId = esercizioSelezionato.id
                    )

                    listaEserciziMutable.add(nuovoEsercizio)
                    adapter.notifyItemInserted(listaEserciziMutable.size - 1)

                    if (isModifica) {
                        isDataModified = true
                    }

                    dialog.dismiss()
                }

                recyclerCatalogo.layoutManager =
                    LinearLayoutManager(this@AllenamentoActivity)

                recyclerCatalogo.adapter = dialogAdapter
            }
        }

        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {

                override fun onQueryTextSubmit(query: String?): Boolean {
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    val query = newText.orEmpty().lowercase()

                    listaFiltrata.clear()
                    listaFiltrata.addAll(
                        listaCompleta.filter {
                            it.nome.lowercase().contains(query)
                        }
                    )

                    dialogAdapter?.notifyDataSetChanged()
                    return true
                }
            }
        )

        chipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            val chipId = checkedIds.firstOrNull()

            val gruppoSelezionato = when (chipId) {
                R.id.chipPetto -> "Petto"
                R.id.chipBicipiti -> "Bicipiti"
                R.id.chipTricipiti -> "Tricipiti"
                R.id.chipSpalle -> "Spalle"
                R.id.chipDorsali -> "Dorsali"
                R.id.chipGambe -> "Quadricipiti"
                else -> "Tutti"
            }

            listaFiltrata.clear()

            if (gruppoSelezionato == "Tutti") {
                listaFiltrata.addAll(listaCompleta)
            } else {
                listaFiltrata.addAll(
                    listaCompleta.filter {
                        it.gruppoMuscolare.equals(
                            gruppoSelezionato,
                            ignoreCase = true
                        )
                    }
                )
            }

            dialogAdapter?.notifyDataSetChanged()
        }

        dialog.show()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}