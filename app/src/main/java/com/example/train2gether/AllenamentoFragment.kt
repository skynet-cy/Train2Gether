package com.example.train2gether

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.train2gether.data.AppDatabase
import com.example.train2gether.data.SchedaRiepilogo
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Fragment che rappresenta la schermata "Allenamento"
class AllenamentoFragment : Fragment() {

    // Componenti grafiche dell'adapter
    private lateinit var recyclerSchede: RecyclerView
    private lateinit var adapter: SchedaAdapter

    // Lista mutable che contiene i riepiloghi delle schede visualizzate
    private val listaSchedeMutable = mutableListOf<SchedaRiepilogo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // Gonfia il layout del file XML
        val view = inflater.inflate(R.layout.fragment_allenamento, container, false)

        val btnNuovaScheda: Button = view.findViewById(R.id.btnNuovaScheda)
        recyclerSchede = view.findViewById(R.id.recyclerViewMainSchede)

        // Configura il layout manager della RecyclerView in formato verticale
        recyclerSchede.layoutManager = LinearLayoutManager(requireContext())

        // Inizializzazione dell'adapter e configurazione delle callback per le azioni sulle schede
        adapter = SchedaAdapter(
            listaSchede = listaSchedeMutable,

            // Callback eseguita quando si preme "Avvia"
            onAvviaClick = { scheda ->
                if (scheda.numeroEsercizi == 0) {
                    Toast.makeText(
                        requireContext(),
                        "Impossibile avviare: aggiungi almeno un esercizio alla scheda!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    // Avvia l'activity di allenamento
                    val intent = Intent(requireContext(), AllenamentoActivity::class.java)
                    intent.putExtra("SCHEDA_ID", scheda.id)
                    intent.putExtra("IS_MODIFICA", false)
                    startActivity(intent)
                }
            },

            // Callback eseguita quando si preme "Modifica"
            onModificaClick = { scheda ->
                // Avvia l'activity di allenamento in modifica
                val intent = Intent(requireContext(), AllenamentoActivity::class.java)
                intent.putExtra("SCHEDA_ID", scheda.id)
                intent.putExtra("IS_MODIFICA", true)
                startActivity(intent)
            },

            // Callback eseguita quando si preme "Elimina"
            onEliminaClick = { scheda ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Elimina Scheda")
                    .setMessage(
                        "Sei sicuro di voler eliminare definitivamente la scheda \"${scheda.nome}\"?"
                    )
                    .setPositiveButton("Elimina") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            // Esegue l'eliminazione dal DB
                            GestoreSchede.eliminaScheda(
                                requireContext(),
                                scheda.id
                            )

                            Toast.makeText(
                                requireContext(),
                                "Scheda eliminata",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                    .setNegativeButton("Annulla", null)
                    .show()
            }
        )

        recyclerSchede.adapter = adapter

        // Button per aprire il popup di inserimento della scheda
        btnNuovaScheda.setOnClickListener {
            mostraPopupNuovaScheda()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        osservaSchede()
    }

    // Osserva i cambiamenti della tabella della tabella nel DB
    private fun osservaSchede() {
        val schedaDao = AppDatabase.getDatabase(requireContext()).schedaDao()

        viewLifecycleOwner.lifecycleScope.launch {
            // Garantisce che il flusso venga "ascoltato" solo quando il fragment è attivo
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                schedaDao.getTutteSchede().collectLatest { schede ->
                    listaSchedeMutable.clear()
                    listaSchedeMutable.addAll(schede)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

    // Mostra un dialog per digitare il nome della scheda
    private fun mostraPopupNuovaScheda() {
        val inputNomeScheda = EditText(requireContext())
        inputNomeScheda.hint = "Es. Chest & Biceps"

        AlertDialog.Builder(requireContext())
            .setTitle("Nuova Scheda")
            .setMessage("Inserisci il nome della tua scheda:")
            .setView(inputNomeScheda)
            .setPositiveButton("Salva") { _, _ ->
                val nomeScheda = inputNomeScheda.text.toString().trim()

                if (nomeScheda.isNotEmpty()) {
                    val nuovaScheda = SchedaAllenamento(
                        id = null,
                        nomeScheda = nomeScheda,
                        listaEsercizi = emptyList()
                    )

                    viewLifecycleOwner.lifecycleScope.launch {
                        // Salva la scheda nel DB
                        GestoreSchede.salvaScheda(
                            requireContext(),
                            nuovaScheda
                        )
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Inserisci un nome valido!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}