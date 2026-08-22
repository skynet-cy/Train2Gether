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
import androidx.recyclerview.widget.ItemTouchHelper
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
                if (scheda.listaEsercizi.isEmpty()) {
                    Toast.makeText(
                        requireContext(),
                        "Impossibile avviare: aggiungi almeno un esercizio alla scheda!",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    val intent = Intent(requireContext(), AllenamentoActivity::class.java)
                    intent.putExtra("SCHEDA_ID", scheda.id)
                    intent.putExtra("IS_MODIFICA", false)
                    startActivity(intent)
                }
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

        val touchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN, 0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val posizioneIniziale = viewHolder.adapterPosition
                val posizioneFinale = target.adapterPosition
                adapter.spostaElemento(posizioneIniziale, posizioneFinale)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            }

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.6f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }
        }

        val itemTouchHelper = ItemTouchHelper(touchHelperCallback)
        itemTouchHelper.attachToRecyclerView(recyclerSchede)

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
                        listaEsercizi = emptyList()
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