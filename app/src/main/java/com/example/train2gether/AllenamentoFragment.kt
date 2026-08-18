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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AllenamentoFragment : Fragment() {

    private lateinit var recyclerSchede: RecyclerView
    private val listaSchedeMutable = mutableListOf<SchedaAllenamento>()
    private lateinit var adapter: SchedaAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_allenamento, container, false)

        val btnNuovaScheda: Button = view.findViewById(R.id.btnNuovaScheda)
        recyclerSchede = view.findViewById(R.id.recyclerViewMainSchede)

        recyclerSchede.layoutManager = LinearLayoutManager(requireContext())

        adapter = SchedaAdapter(
            listaSchede = listaSchedeMutable,
            onAvviaClick = { scheda ->
                val intent = Intent(requireContext(), AllenamentoActivity::class.java)
                intent.putExtra("SCHEDA_ID", scheda.id)
                intent.putExtra("IS_MODIFICA", false)
                startActivity(intent)
            },
            onModificaClick = { scheda ->
                val intent = Intent(requireContext(), AllenamentoActivity::class.java)
                intent.putExtra("SCHEDA_ID", scheda.id)
                intent.putExtra("IS_MODIFICA", true)
                startActivity(intent)
            },
            onEliminaClick = { scheda ->
                GestoreSchede.eliminaScheda(requireContext(), scheda.id)
                caricaSchedeSalvate()
                Toast.makeText(requireContext(), "Scheda eliminata", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerSchede.adapter = adapter

        btnNuovaScheda.setOnClickListener {
            mostraPopupNuovaScheda()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        caricaSchedeSalvate()
    }

    private fun caricaSchedeSalvate() {
        listaSchedeMutable.clear()
        listaSchedeMutable.addAll(GestoreSchede.caricaTutteLeSchede(requireContext()))
        adapter.notifyDataSetChanged()
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
                        listaEsercizi = listOf(
                            EsercizioConSerie(
                                nomeEsercizio = "Panca Piana",
                                listaSerie = mutableListOf(SerieEsercizio(1, 20.0, 10))
                            )
                        )
                    )

                    GestoreSchede.salvaScheda(requireContext(), nuovaScheda)
                    caricaSchedeSalvate()
                } else {
                    Toast.makeText(requireContext(), "Inserisci un nome valido!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Annulla", null)
            .show()
    }
}