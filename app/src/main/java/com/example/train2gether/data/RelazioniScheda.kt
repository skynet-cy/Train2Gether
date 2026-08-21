package com.example.train2gether.data


import androidx.room.Embedded
import androidx.room.Relation



data class SchedaRiepilogo(
    val id: Int,
    val nome: String,
    val numeroEsercizi: Int
)



data class EsercizioSchedaCompleto(

    @Embedded
    val esercizioScheda: EsercizioScheda,

    @Relation(
        parentColumn = "esercizio_id",
        entityColumn = "id"
    )
    val esercizio: Esercizio,

    @Relation(
        parentColumn = "id",
        entityColumn = "esercizio_scheda_id"
    )
    val seriePreviste: List<SeriePrevista>
) {


    fun serieOrdinate(): List<SeriePrevista> {
        return seriePreviste.sortedBy { it.numeroSerie }
    }
}



data class SchedaCompleta(

    @Embedded
    val scheda: Scheda,

    @Relation(
        entity = EsercizioScheda::class,
        parentColumn = "id",
        entityColumn = "scheda_id"
    )
    val esercizi: List<EsercizioSchedaCompleto>
) {


    fun eserciziOrdinati(): List<EsercizioSchedaCompleto> {
        return esercizi.sortedBy {
            it.esercizioScheda.ordine
        }
    }
}



data class NuovaSeriePrevista(
    val peso: Double?,
    val ripetizioni: Int
)



data class NuovoEsercizioScheda(

    val esercizioId: Int,

    val riposoSecondi: Int,

    val serie: List<NuovaSeriePrevista>
)