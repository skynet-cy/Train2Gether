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

class HomeFragment : Fragment() {

    private lateinit var recyclerStorico: RecyclerView
    private lateinit var txtStoricoVuoto: TextView
    private val listaStorico = mutableListOf<AllenamentoRiepilogo>()
    private lateinit var storicoAdapter: StoricoAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerStorico = view.findViewById(R.id.recyclerStorico)
        txtStoricoVuoto = view.findViewById(R.id.txtStoricoVuoto)

        recyclerStorico.layoutManager = LinearLayoutManager(requireContext())

        storicoAdapter = StoricoAdapter(listaStorico) { allenamentoRiepilogo ->
            val intent = Intent(requireContext(), DettaglioStoricoActivity::class.java)
            intent.putExtra("ALLENAMENTO_ID", allenamentoRiepilogo.id)
            startActivity(intent)
        }
        recyclerStorico.adapter = storicoAdapter

        osservaStorico()

        return view
    }

    private fun osservaStorico() {
        val allenamentoDao = AppDatabase.getDatabase(requireContext()).allenamentoDao()
        viewLifecycleOwner.lifecycleScope.launch {
            allenamentoDao.getStoricoAllenamenti().collectLatest { lista ->
                listaStorico.clear()
                listaStorico.addAll(lista)
                storicoAdapter.notifyDataSetChanged()

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