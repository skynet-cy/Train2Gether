package com.example.train2gether

import android.content.Context
import com.example.train2gether.data.AppDatabase
import com.example.train2gether.data.NuovaSeriePrevista
import com.example.train2gether.data.NuovoEsercizioScheda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SchedaAllenamento(
    var id: Int?,
    var nomeScheda: String,
    var listaEsercizi: List<EsercizioConSerie>
)

object GestoreSchede {

    suspend fun caricaScheda(
        context: Context,
        schedaId: Int
    ): SchedaAllenamento? = withContext(Dispatchers.IO) {

        val schedaDao = AppDatabase.getDatabase(context).schedaDao()
        val schedaCompleta =
            schedaDao.getSchedaCompleta(schedaId) ?: return@withContext null

        val eserciziConvertiti = schedaCompleta.eserciziOrdinati().map { esCompleto ->
            EsercizioConSerie(
                nomeEsercizio = esCompleto.esercizio.nome,
                tempoRecuperoSecondi = esCompleto.esercizioScheda.riposoSecondi.toLong(),

                listaSerie = esCompleto.serieOrdinate().map { seriePrevista ->
                    SerieEsercizio(
                        numeroSet = seriePrevista.numeroSerie,
                        kg = seriePrevista.peso ?: 0.0,
                        reps = seriePrevista.ripetizioni
                    )
                }.toMutableList(),

                esercizioId = esCompleto.esercizio.id
            )
        }

        SchedaAllenamento(
            id = schedaCompleta.scheda.id,
            nomeScheda = schedaCompleta.scheda.nome,
            listaEsercizi = eserciziConvertiti
        )
    }

    suspend fun salvaScheda(
        context: Context,
        scheda: SchedaAllenamento
    ) = withContext(Dispatchers.IO) {

        val schedaDao = AppDatabase.getDatabase(context).schedaDao()
        val idScheda = scheda.id

        val eserciziPerDb = scheda.listaEsercizi.map { itemEsercizio ->
            require(itemEsercizio.esercizioId > 0) {
                "ID esercizio non valido per ${itemEsercizio.nomeEsercizio}"
            }

            val seriePerDb = itemEsercizio.listaSerie.map { serie ->
                NuovaSeriePrevista(
                    peso = if (serie.kg > 0) serie.kg else null,
                    ripetizioni = serie.reps
                )
            }

            NuovoEsercizioScheda(
                esercizioId = itemEsercizio.esercizioId,
                riposoSecondi = itemEsercizio.tempoRecuperoSecondi.toInt(),
                serie = seriePerDb
            )
        }

        if (idScheda == null) {
            schedaDao.creaSchedaCompleta(
                nome = scheda.nomeScheda,
                esercizi = eserciziPerDb
            )
        } else {
            schedaDao.modificaSchedaCompleta(
                schedaId = idScheda,
                nuovoNome = scheda.nomeScheda,
                esercizi = eserciziPerDb
            )
        }
    }

    suspend fun eliminaScheda(
        context: Context,
        schedaId: Int
    ) = withContext(Dispatchers.IO) {

        val schedaDao = AppDatabase.getDatabase(context).schedaDao()

        // Manteniamo la SELECT perché @Delete richiede l'entità Scheda.
        val scheda = schedaDao.getSchedaById(schedaId)

        if (scheda != null) {
            schedaDao.eliminaScheda(scheda)
        }
    }
}