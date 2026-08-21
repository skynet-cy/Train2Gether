package com.example.train2gether.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
abstract class SchedaDao {


    //Inserisce una nuova Schedak, Restituisce l'id
    @Insert
    abstract suspend fun inserisciScheda(
        scheda: Scheda
    ): Long


    //Aggiorna una Scheda già esistente.
    @Update
    abstract suspend fun aggiornaScheda(
        scheda: Scheda
    )


    //Elimina una Scheda, di conseguenza tutti gli esercizi e serie
    @Delete
    abstract suspend fun eliminaScheda(
        scheda: Scheda
    )


    //Restituisce tutte le schede insieme al numero di esercizi contenuti.
    @Query(
        """
        SELECT
            s.id AS id,
            s.nome AS nome,
            COUNT(es.id) AS numeroEsercizi
        FROM schede AS s
        LEFT JOIN esercizi_scheda AS es
            ON es.scheda_id = s.id
        GROUP BY s.id, s.nome
        ORDER BY s.nome COLLATE NOCASE ASC
        """
    )
    abstract fun getTutteSchede(): Flow<List<SchedaRiepilogo>>


    //Restituisce Entity Scheda corrispondente all'id richiesto.
    @Query(
        """
        SELECT *
        FROM schede
        WHERE id = :schedaId
        LIMIT 1
        """
    )
    abstract suspend fun getSchedaById(
        schedaId: Int
    ): Scheda?

    //Restituisce una Scheda completa con nome, esercizi e serie
    @Transaction
    @Query(
        """
        SELECT *
        FROM schede
        WHERE id = :schedaId
        LIMIT 1
        """
    )
    abstract suspend fun getSchedaCompleta(
        schedaId: Int
    ): SchedaCompleta?

    //Inserisce un EsercizioScheda e restituisce id
    @Insert
    abstract suspend fun inserisciEsercizioScheda(
        esercizioScheda: EsercizioScheda
    ): Long


    @Update
    abstract suspend fun aggiornaEsercizioScheda(
        esercizioScheda: EsercizioScheda
    )


    @Delete
    abstract suspend fun eliminaEsercizioScheda(
        esercizioScheda: EsercizioScheda
    )


    //Elimina tutti gli esercizi appartenenti a una Scheda.
    @Query(
        """
        DELETE FROM esercizi_scheda
        WHERE scheda_id = :schedaId
        """
    )
    abstract suspend fun eliminaEserciziDellaScheda(
        schedaId: Int
    )

    //Inserisce una lista di SeriePrevista.
    @Insert
    abstract suspend fun inserisciSeriePreviste(
        serie: List<SeriePrevista>
    )


    @Update
    abstract suspend fun aggiornaSeriePrevista(
        serie: SeriePrevista
    )


    @Delete
    abstract suspend fun eliminaSeriePrevista(
        serie: SeriePrevista
    )


    //Restituisce le serie previste di uno specifico EsercizioScheda.
    @Query(
        """
        SELECT *
        FROM serie_previste
        WHERE esercizio_scheda_id = :esercizioSchedaId
        ORDER BY numero_serie ASC
        """
    )
    abstract suspend fun getSeriePreviste(
        esercizioSchedaId: Int
    ): List<SeriePrevista>


    //Elimina tutte le serie previste di uno specifico EsercizioScheda.
    @Query(
        """
        DELETE FROM serie_previste
        WHERE esercizio_scheda_id = :esercizioSchedaId
        """
    )
    abstract suspend fun eliminaSeriePreviste(
        esercizioSchedaId: Int
    )

    //Salva una nuova scheda completa.
    @Transaction
    open suspend fun creaSchedaCompleta(
        nome: String,
        esercizi: List<NuovoEsercizioScheda>
    ): Int {

        // Inserisce la Scheda e recupera l'id generato.
        val schedaId = inserisciScheda(
            Scheda(
                nome = nome
            )
        ).toInt()


        // inserimento esercizi e serie
        // posizioni determinate dall'ordine della lista
        esercizi.forEachIndexed { indiceEsercizio, nuovoEsercizio ->

            val esercizioSchedaId =
                inserisciEsercizioScheda(
                    EsercizioScheda(
                        schedaId = schedaId,
                        esercizioId = nuovoEsercizio.esercizioId,
                        riposoSecondi = nuovoEsercizio.riposoSecondi,
                        ordine = indiceEsercizio
                    )
                ).toInt()

            val serieDaInserire =
                nuovoEsercizio.serie.mapIndexed { indiceSerie, nuovaSerie ->

                    SeriePrevista(
                        esercizioSchedaId = esercizioSchedaId,

                        // Le serie partono da 1.
                        numeroSerie = indiceSerie + 1,

                        peso = nuovaSerie.peso,
                        ripetizioni = nuovaSerie.ripetizioni
                    )
                }

            if (serieDaInserire.isNotEmpty()) {
                inserisciSeriePreviste(
                    serieDaInserire
                )
            }
        }

        return schedaId
    }

    //Modifica completamente una Scheda.
    //aggiorna i dati della Scheda
    //elimina la vecchia struttura
    //ricrea esercizi e serie
    @Transaction
    open suspend fun modificaSchedaCompleta(
        schedaId: Int,
        nuovoNome: String,
        esercizi: List<NuovoEsercizioScheda>
    ) {

        // Aggiorna il nome.
        aggiornaScheda(
            Scheda(
                id = schedaId,
                nome = nuovoNome
            )
        )


        //Elimina la vecchia struttura.
        eliminaEserciziDellaScheda(
            schedaId
        )

        //Ricrea la nuova struttura.
        esercizi.forEachIndexed { indiceEsercizio, nuovoEsercizio ->

            val esercizioSchedaId =
                inserisciEsercizioScheda(
                    EsercizioScheda(
                        schedaId = schedaId,
                        esercizioId = nuovoEsercizio.esercizioId,
                        riposoSecondi = nuovoEsercizio.riposoSecondi,
                        ordine = indiceEsercizio
                    )
                ).toInt()

            val serieDaInserire =
                nuovoEsercizio.serie.mapIndexed { indiceSerie, nuovaSerie ->

                    SeriePrevista(
                        esercizioSchedaId = esercizioSchedaId,
                        numeroSerie = indiceSerie + 1,
                        peso = nuovaSerie.peso,
                        ripetizioni = nuovaSerie.ripetizioni
                    )
                }


            if (serieDaInserire.isNotEmpty()) {
                inserisciSeriePreviste(
                    serieDaInserire
                )
            }
        }
    }
}