package com.example.train2gether

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.train2gether.data.AppDatabase
import com.example.train2gether.data.AllenamentoRiepilogo
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

// Fragment che rappresenta lo storico
class HomeFragment : Fragment() {

    // Dichiarazione dei componenti grafici e dell'adapter per lo storico
    private lateinit var recyclerStorico: RecyclerView
    private lateinit var txtStoricoVuoto: TextView
    private val listaStorico = mutableListOf<AllenamentoRiepilogo>()
    private lateinit var storicoAdapter: StoricoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Gonfia il layout del file XML
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Collegamento agli id dell'XML
        recyclerStorico = view.findViewById(R.id.recyclerStorico)
        txtStoricoVuoto = view.findViewById(R.id.txtStoricoVuoto)

        recyclerStorico.layoutManager = LinearLayoutManager(requireContext())

        // Inizializzazione dell'adapter dello storico e gestione del click sull'elemento
        storicoAdapter = StoricoAdapter(listaStorico) { allenamentoRiepilogo ->
            // Al click su un allenamento dello storico, avvia l'activity di dettaglio
            val intent = Intent(requireContext(), DettaglioStoricoActivity::class.java)
            intent.putExtra("ALLENAMENTO_ID", allenamentoRiepilogo.id)
            startActivity(intent)
        }
        recyclerStorico.adapter = storicoAdapter

        // Avvia l'ascolto reattivo dello storico dal database
        osservaStorico()

        return view
    }

    // Osserva in tempo reale i dati dello storico tramite un Flow reattivo di Room
    private fun osservaStorico() {
        val allenamentoDao = AppDatabase.getDatabase(requireContext()).allenamentoDao()

        viewLifecycleOwner.lifecycleScope.launch {
            allenamentoDao.getStoricoAllenamenti().collectLatest { lista ->
                listaStorico.clear()
                listaStorico.addAll(lista)
                storicoAdapter.notifyDataSetChanged()

                // Mostra un messaggio di testo vuoto o la RecyclerView in base alla presenza o meno di allenamenti
                if (listaStorico.isEmpty()) {
                    txtStoricoVuoto.visibility = View.VISIBLE
                    recyclerStorico.visibility = View.GONE
                } else {
                    txtStoricoVuoto.visibility = View.GONE
                    recyclerStorico.visibility = View.VISIBLE
                }
            }
        }
    }
}