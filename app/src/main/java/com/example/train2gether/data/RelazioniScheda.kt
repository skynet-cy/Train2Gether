package com.example.train2gether.data


import androidx.room.Embedded
import androidx.room.Relation


//Contiene i dati riepilogativi mostrati nell'elenco delle schede.
data class SchedaRiepilogo(
    val id: Int,
    val nome: String,
    val numeroEsercizi: Int
)


//Rappresenta un esercizio della scheda insieme ai suoi dati e alle serie previste.
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

    //Restituisce le serie previste ordinate per numero di serie.
    fun serieOrdinate(): List<SeriePrevista> {
        return seriePreviste.sortedBy { it.numeroSerie }
    }
}


//Rappresenta una scheda insieme a tutti gli esercizi che la compongono.
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

    //Restituisce gli esercizi nell'ordine previsto dalla scheda.
    fun eserciziOrdinati(): List<EsercizioSchedaCompleto> {
        return esercizi.sortedBy {
            it.esercizioScheda.ordine
        }
    }
}


//Contiene i dati necessari per creare una nuova serie prevista.
data class NuovaSeriePrevista(
    val peso: Double?,
    val ripetizioni: Int
)


//Contiene i dati necessari per aggiungere un esercizio a una scheda.
data class NuovoEsercizioScheda(

    val esercizioId: Int,

    val riposoSecondi: Int,

    val serie: List<NuovaSeriePrevista>
)