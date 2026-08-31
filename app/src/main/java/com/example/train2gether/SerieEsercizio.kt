package com.example.train2gether

data class SerieEsercizio(
    var numeroSet: Int,
    var kg: Double,
    var reps: Int,
    var completata: Boolean = false,
    var serieEseguitaId: Int? = null
)

data class EsercizioConSerie(
    var nomeEsercizio: String,
    var tempoRecuperoSecondi: Long = 60,
    val listaSerie: MutableList<SerieEsercizio> = mutableListOf(),
    var esercizioId: Int = 0
)