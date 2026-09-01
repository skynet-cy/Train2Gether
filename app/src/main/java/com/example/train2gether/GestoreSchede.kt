package com.example.train2gether

import android.content.Context
import com.example.train2gether.data.AppDatabase
import com.example.train2gether.data.NuovaSeriePrevista
import com.example.train2gether.data.NuovoEsercizioScheda
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class SchedaAllenamento(
    var id: String,
    var nomeScheda: String,
    var listaEsercizi: List<EsercizioConSerie>
)

object GestoreSchede {

    suspend fun caricaScheda(
        context: Context,
        schedaId: String
    ): SchedaAllenamento? = withContext(Dispatchers.IO) {

        val idNumerico = schedaId.toIntOrNull() ?: return@withContext null
        val schedaDao = AppDatabase.getDatabase(context).schedaDao()

        val schedaCompleta =
            schedaDao.getSchedaCompleta(idNumerico) ?: return@withContext null

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
            id = schedaCompleta.scheda.id.toString(),
            nomeScheda = schedaCompleta.scheda.nome,
            listaEsercizi = eserciziConvertiti
        )
    }

    suspend fun salvaScheda(
        context: Context,
        scheda: SchedaAllenamento
    ) = withContext(Dispatchers.IO) {

        val db = AppDatabase.getDatabase(context)
        val schedaDao = db.schedaDao()
        val esercizioDao = db.esercizioDao()

        val idNumerico = scheda.id.toIntOrNull()
        val eserciziDb = esercizioDao.getTuttiEsercizi()

        val eserciziPerDb = mutableListOf<NuovoEsercizioScheda>()

        for (itemEsercizio in scheda.listaEsercizi) {
            val esercizioTrovato = eserciziDb.find {
                it.nome.equals(itemEsercizio.nomeEsercizio, ignoreCase = true)
            }

            val esercizioIdDb = esercizioTrovato?.id ?: 1

            val seriePerDb = itemEsercizio.listaSerie.map { serie ->
                NuovaSeriePrevista(
                    peso = if (serie.kg > 0) serie.kg else null,
                    ripetizioni = serie.reps
                )
            }

            eserciziPerDb.add(
                NuovoEsercizioScheda(
                    esercizioId = esercizioIdDb,
                    riposoSecondi = itemEsercizio.tempoRecuperoSecondi.toInt(),
                    serie = seriePerDb
                )
            )
        }

        if (idNumerico != null && schedaDao.getSchedaById(idNumerico) != null) {
            schedaDao.modificaSchedaCompleta(
                schedaId = idNumerico,
                nuovoNome = scheda.nomeScheda,
                esercizi = eserciziPerDb
            )
        } else {
            schedaDao.creaSchedaCompleta(
                nome = scheda.nomeScheda,
                esercizi = eserciziPerDb
            )
        }
    }

    suspend fun eliminaScheda(
        context: Context,
        schedaId: String
    ) = withContext(Dispatchers.IO) {

        val schedaDao = AppDatabase.getDatabase(context).schedaDao()
        val idNumerico = schedaId.toIntOrNull()

        if (idNumerico != null) {
            val scheda = schedaDao.getSchedaById(idNumerico)

            if (scheda != null) {
                schedaDao.eliminaScheda(scheda)
            }
        }
    }
}