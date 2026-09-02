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
import com.google.android.material.chip.Chip

// L'activity che gestisce la schermata di allenamento
class AllenamentoActivity : AppCompatActivity() {

    // Dichiarazione delle componenti della schermata
    private lateinit var txtNomeSchedaTitle: TextView
    private lateinit var txtTimer: TextView
    private lateinit var recyclerEsercizi: RecyclerView
    private lateinit var btnAggiungiEsercizio: Button
    private lateinit var btnSalvaOppureTermina: Button
    private lateinit var adapter: EsercizioAdapter
    private lateinit var allenamentoDao: AllenamentoDao

    // Liste di supporto e sincronizzatori per le operazioni async sul DB
    private val listaEserciziMutable = mutableListOf<EsercizioConSerie>()
    private val serieDbMutex = Mutex()

    // Variabili di stato della schermata e dell'allenamento corrente
    private var schedaId: Int? = null
    private var isModifica = false
    private var schedaAttuale: SchedaAllenamento? = null
    private var countDownTimer: CountDownTimer? = null
    private var isDataModified = false
    private var allenamentoId: Int? = null
    private var tempoInizioMillis: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Impostazione del colore della barra di stato
        window.statusBarColor = android.graphics.Color.parseColor("#121212")
        supportActionBar?.hide() // Nascondiamo la action bar
        setContentView(R.layout.activity_allenamento)

        // Collegamento delle variabili agli id del file XML
        txtNomeSchedaTitle = findViewById(R.id.txtNomeSchedaTitle)
        txtTimer = findViewById(R.id.txtTimer)
        recyclerEsercizi = findViewById(R.id.recyclerEsercizi)
        btnAggiungiEsercizio = findViewById(R.id.btnAggiungiEsercizio)
        btnSalvaOppureTermina = findViewById(R.id.btnSalvaOppureTermina)

        // Inizializzazione del Dao per interagire con le tabelle del DB
        allenamentoDao = AppDatabase.getDatabase(this).allenamentoDao()

        // Estrazione dei parametri passati dall'Activity precedente tramite Intent
        schedaId = intent.getIntExtra("SCHEDA_ID", -1).takeIf { it > 0 }
        isModifica = intent.getBooleanExtra("IS_MODIFICA", false)

        // Configurazione del timer
        txtTimer.text = ""
        txtTimer.visibility = View.GONE
        txtTimer.setOnClickListener { fermaTimerRecupero() }

        recyclerEsercizi.layoutManager = LinearLayoutManager(this)

        // Inizializzazione dell'adapter per gestire la lista degli esercizi e delle serie
        adapter = EsercizioAdapter(
            listaEsercizi = listaEserciziMutable,
            isModifica = isModifica,

            // Callback eseguita quando si spunta una serie
            onStatoSerieCambiato = { esercizio, ordineEsercizio, serie, checked ->
                gestisciStatoSerie(
                    esercizio = esercizio,
                    ordineEsercizio = ordineEsercizio,
                    serie = serie,
                    completata = checked
                )
            },

            // Callback per modificare il peso o le ripetizioni
            onSerieModificata = { esercizio, ordineEsercizio, serie ->
                aggiornaSerieEseguita(
                    esercizio = esercizio,
                    ordineEsercizio = ordineEsercizio,
                    serie = serie
                )
            },

            // Callback per avviare il timer
            onSerieSpuntata = { tempoSecondi ->
                avviaTimerRecupero(tempoSecondi * 1000L)
            },

            // Callback che traccia le modifiche in "modalità" modifica
            onDatoModificato = {
                if (isModifica) isDataModified = true
            }
        )

        recyclerEsercizi.adapter = adapter

        // Imposta il testo del pulsante principale in base alla modalità
        btnSalvaOppureTermina.text =
            if (isModifica) "Salva Modifiche" else "Termina Allenamento"

        // Disabilita temporaneamente i pulsanti finché i dati non vengono caricati
        if (!isModifica){
            btnAggiungiEsercizio.isEnabled= false
            btnSalvaOppureTermina.isEnabled = false
        }

        // Carica la scheda dal DB o impostiamo il titolo se è nuova
        if (schedaId != null) {
            caricaScheda()
        } else {
            txtNomeSchedaTitle.text = "Nuova Scheda"
        }

        // Button per aprire il popup di aggiunta di un esercizio
        btnAggiungiEsercizio.setOnClickListener {
            mostraPopupNuovoEsercizio()
        }

        // Button di fine operazione
        btnSalvaOppureTermina.setOnClickListener {
            if (isModifica) salvaModificheScheda()
            else terminaAllenamento()
        }

        // Intercettiamo il tasto indietro per gestire l'uscita
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    gestisciUscita()
                }
            }
        )
    }

    // Funzione per caricare i dati della scheda
    private fun caricaScheda() {
        val id = schedaId ?: return

        lifecycleScope.launch {
            schedaAttuale = GestoreSchede.caricaScheda(
                this@AllenamentoActivity,
                id
            )

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

            // Se siamo in esecuzione e l'allenamento non è stato creato, esso viene inserito nel DB
            if(!isModifica && allenamentoId == null){
                try{
                    creaAllenamentoNelDatabase(scheda)
                }catch(e: Exception){
                    Toast.makeText(this@AllenamentoActivity,
                        "Errore durante avvio alllenamento",
                        Toast.LENGTH_SHORT).show()
                    finish()
                    return@launch
                }
            }

            // Popoliamo la lista degli esercizi e aggiorniamo l'adapter
            listaEserciziMutable.clear()
            listaEserciziMutable.addAll(scheda.listaEsercizi)
            adapter.notifyDataSetChanged()

            // Riabilita i pulsanti una volta completato il caricamento
            if(!isModifica){
                btnAggiungiEsercizio.isEnabled=true
                btnSalvaOppureTermina.isEnabled=true
            }
        }
    }

    // Inserisce una nuova riga di allenamento in corso nel DB
    private suspend fun creaAllenamentoNelDatabase(scheda: SchedaAllenamento) {
        tempoInizioMillis = System.currentTimeMillis()

        val nuovoId = withContext(Dispatchers.IO) {
            allenamentoDao.inserisciAllenamento(
                Allenamento(
                    schedaIdOrigine = schedaId,
                    nomeScheda = scheda.nomeScheda,
                    dataInizio = tempoInizioMillis,
                    durataSecondi = 0
                )
            )
        }

        allenamentoId = nuovoId.toInt()
    }

    // Gestisce in modo thread-safe il salvataggio/rimozione di una singola serie quando viene spuntata la checkbox
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

                    } else {
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

    // Aggiorna i dati di una serie già registrata nel DB
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

    // Finalizza l'allenamento calcolandone la durata e salvandolo nel DB
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

    // Salva le modifiche apportate alla scheda
    private fun salvaModificheScheda() {
        val schedaDaSalvare = SchedaAllenamento(
            id = schedaId,
            nomeScheda = txtNomeSchedaTitle.text.toString(),
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

    // Gestisce l'uscita a seconda della modalità
    private fun gestisciUscita() {
        if (isModifica) {
            gestisciUscitaModificaScheda()
        } else {
            gestisciAbbandonoAllenamento()
        }
    }

    // Mostra un dialog se l'utente tenta di uscire senza salvare
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

    // Mostra un dialog se l'utente abbandona l'allenamento in corso
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

    // Elimina dal DB l'allenamento corrente in caso di abbandono
    private fun eliminaAllenamentoInCorso() {
        val id = allenamentoId

        if (id == null) {
            finish()
            return
        }

        lifecycleScope.launch {
            serieDbMutex.withLock {
                withContext(Dispatchers.IO) {
                    val allenamento = allenamentoDao.getAllenamentoById(id)

                    if (allenamento != null) {
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

    // Ferma manualente il timer
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

    // Mostra un popup per scegliere un esercizio dal DB e aggiungerlo alla scheda
    private fun mostraPopupNuovoEsercizio() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_selezione_esercizio, null)
        val searchView = dialogView.findViewById<SearchView>(R.id.searchEsercizio)
        val chipGroup = dialogView.findViewById<ChipGroup>(R.id.chipGroupGruppi)
        val recyclerCatalogo = dialogView.findViewById<RecyclerView>(R.id.recyclerCatalogo)

        var listaCompleta = listOf<Esercizio>()
        val listaFiltrata = mutableListOf<Esercizio>()
        var dialogAdapter: CatalogoDialogAdapter? = null
        var gruppoSelezionato = "Tutti"
        var testoRicerca = ""

        fun applicaFiltri() {
            listaFiltrata.clear()
            listaFiltrata.addAll(
                listaCompleta.filter { esercizio ->
                    val gruppoOk = gruppoSelezionato == "Tutti" ||
                            esercizio.gruppoMuscolare.equals(gruppoSelezionato, ignoreCase = true)

                    val ricercaOk = esercizio.nome.contains(testoRicerca, ignoreCase = true)
                    gruppoOk && ricercaOk
                }
            )
            dialogAdapter?.notifyDataSetChanged()
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle("Seleziona Esercizio")
            .setView(dialogView)
            .setNegativeButton("Annulla", null)
            .create()

        lifecycleScope.launch(Dispatchers.IO) {
            listaCompleta = AppDatabase.getDatabase(this@AllenamentoActivity)
                .esercizioDao()
                .getTuttiEsercizi()

            withContext(Dispatchers.Main) {
                val gruppi = listaCompleta
                    .map { it.gruppoMuscolare }
                    .distinct()
                    .sorted()

                (listOf("Tutti") + gruppi).forEachIndexed { index, gruppo ->
                    chipGroup.addView(
                        Chip(this@AllenamentoActivity).apply {
                            id = View.generateViewId()
                            text = gruppo
                            tag = gruppo
                            isCheckable = true
                            isChecked = index == 0
                        }
                    )
                }

                dialogAdapter = CatalogoDialogAdapter(listaFiltrata) { esercizioSelezionato ->
                    val nuovoEsercizio = EsercizioConSerie(
                        nomeEsercizio = esercizioSelezionato.nome,
                        tempoRecuperoSecondi = 60,
                        listaSerie = mutableListOf(
                            SerieEsercizio(numeroSet = 1, kg = 0.0, reps = 0)
                        ),
                        esercizioId = esercizioSelezionato.id
                    )

                    listaEserciziMutable.add(nuovoEsercizio)
                    adapter.notifyItemInserted(listaEserciziMutable.size - 1)

                    if (isModifica) isDataModified = true
                    dialog.dismiss()
                }

                recyclerCatalogo.layoutManager = LinearLayoutManager(this@AllenamentoActivity)
                recyclerCatalogo.adapter = dialogAdapter
                applicaFiltri()
            }
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                testoRicerca = newText.orEmpty()
                applicaFiltri()
                return true
            }
        })

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val chipId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            gruppoSelezionato = group.findViewById<Chip>(chipId).tag as String
            applicaFiltri()
        }

        dialog.show()
    }

    override fun onDestroy() {
        countDownTimer?.cancel()
        super.onDestroy()
    }
}