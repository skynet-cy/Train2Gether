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

class AllenamentoFragment : Fragment() {

    private lateinit var recyclerSchede: RecyclerView
    private lateinit var adapter: SchedaAdapter

    private val listaSchedeMutable = mutableListOf<SchedaRiepilogo>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_allenamento, container, false)

        val btnNuovaScheda: Button = view.findViewById(R.id.btnNuovaScheda)
        recyclerSchede = view.findViewById(R.id.recyclerViewMainSchede)

        recyclerSchede.layoutManager = LinearLayoutManager(requireContext())

        adapter = SchedaAdapter(
            listaSchede = listaSchedeMutable,

            onAvviaClick = { scheda ->
                if (scheda.numeroEsercizi == 0) {
                    Toast.makeText(
                        requireContext(),
                        "Impossibile avviare: aggiungi almeno un esercizio alla scheda!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val intent = Intent(requireContext(), AllenamentoActivity::class.java)
                    intent.putExtra("SCHEDA_ID", scheda.id.toString())
                    intent.putExtra("IS_MODIFICA", false)
                    startActivity(intent)
                }
            },

            onModificaClick = { scheda ->
                val intent = Intent(requireContext(), AllenamentoActivity::class.java)
                intent.putExtra("SCHEDA_ID", scheda.id.toString())
                intent.putExtra("IS_MODIFICA", true)
                startActivity(intent)
            },

            onEliminaClick = { scheda ->
                AlertDialog.Builder(requireContext())
                    .setTitle("Elimina Scheda")
                    .setMessage(
                        "Sei sicuro di voler eliminare definitivamente la scheda \"${scheda.nome}\"?"
                    )
                    .setPositiveButton("Elimina") { _, _ ->
                        viewLifecycleOwner.lifecycleScope.launch {
                            GestoreSchede.eliminaScheda(
                                requireContext(),
                                scheda.id.toString()
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

        btnNuovaScheda.setOnClickListener {
            mostraPopupNuovaScheda()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        osservaSchede()
    }

    private fun osservaSchede() {
        val schedaDao = AppDatabase.getDatabase(requireContext()).schedaDao()

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                schedaDao.getTutteSchede().collectLatest { schede ->
                    listaSchedeMutable.clear()
                    listaSchedeMutable.addAll(schede)
                    adapter.notifyDataSetChanged()
                }
            }
        }
    }

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
                        id = System.currentTimeMillis().toString(),
                        nomeScheda = nomeScheda,
                        listaEsercizi = emptyList()
                    )

                    viewLifecycleOwner.lifecycleScope.launch {
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