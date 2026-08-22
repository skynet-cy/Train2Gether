package com.example.train2gether

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class HomeFragment : Fragment() {

    private lateinit var recyclerStorico: RecyclerView
    private lateinit var txtStoricoVuoto: TextView
    private val listaStorico = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerStorico = view.findViewById(R.id.recyclerStorico)
        txtStoricoVuoto = view.findViewById(R.id.txtStoricoVuoto)

        recyclerStorico.layoutManager = LinearLayoutManager(requireContext())

       return view
    }

    override fun onResume() {
        super.onResume()
        caricaStorico()
    }

    private fun caricaStorico() {
        if (listaStorico.isEmpty()) {
            txtStoricoVuoto.visibility = View.VISIBLE
            recyclerStorico.visibility = View.GONE
        } else {
            txtStoricoVuoto.visibility = View.GONE
            recyclerStorico.visibility = View.VISIBLE
        }
    }
}