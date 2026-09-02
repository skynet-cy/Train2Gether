package com.example.train2gether.data

import androidx.room.Embedded
import androidx.room.Relation


//Riepilogo utilizzato nella schermata dello storico
data class AllenamentoRiepilogo(

    val id: Int,

    val nomeScheda: String?,

    val dataInizio: Long,

    val durataSecondi: Int,

    val numeroSerie: Int,

    val numeroEsercizi: Int
)


//allenamento completo, comprese tutte le serie eseguite
data class AllenamentoCompleto(

    @Embedded
    val allenamento: Allenamento,

    @Relation(
        parentColumn = "id",
        entityColumn = "allenamento_id"
    )
    val serieEseguite: List<SerieEseguita>
) {

    //Restituisce le serie ordinate prima per esercizio e poi per numero della serie.
    fun serieOrdinate(): List<SerieEseguita> {
        return serieEseguite.sortedWith(
            compareBy<SerieEseguita> {
                it.ordineEsercizio
            }.thenBy {
                it.numeroSerie
            }
        )
    }


    //Raggruppa le serie in base all'ordine dell'esercizio nell'allenamento.
    fun seriePerEsercizio(): Map<Int, List<SerieEseguita>> {
        return serieOrdinate().groupBy {
            it.ordineEsercizio
        }
    }
}