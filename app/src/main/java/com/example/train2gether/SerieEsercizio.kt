package com.example.train2gether

data class SerieEsercizio(
    var numeroSet: Int,
    var kg: Double,
    var reps: Int
)

data class EsercizioConSerie(
    var nomeEsercizio: String,
    val listaSerie: MutableList<SerieEsercizio> = mutableListOf()
)