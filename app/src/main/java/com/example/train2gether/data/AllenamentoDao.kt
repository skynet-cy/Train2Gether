package com.example.train2gether.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
abstract class AllenamentoDao {


    //Crea un nuovo allenamento.
    @Insert
    abstract suspend fun inserisciAllenamento(
        allenamento: Allenamento
    ): Long


    //Aggiorna un Allenamento esistente.
    @Update
    abstract suspend fun aggiornaAllenamento(
        allenamento: Allenamento
    )


    //Elimina completamente un allenamento, con le serie eseguite
    @Delete
    abstract suspend fun eliminaAllenamento(
        allenamento: Allenamento
    )


    //Recupera un singolo Allenamento tramite id.
    @Query(
        """
        SELECT *
        FROM allenamenti
        WHERE id = :allenamentoId
        LIMIT 1
        """
    )
    abstract suspend fun getAllenamentoById(
        allenamentoId: Int
    ): Allenamento?


    //Cerca l'ultimo allenamento ancora in corso.
    @Query(
        """
        SELECT *
        FROM allenamenti
        WHERE durata_secondi = 0
        ORDER BY data_inizio DESC
        LIMIT 1
        """
    )
    abstract suspend fun getAllenamentoInCorso(): Allenamento?


    //Aggiorna solamente la durata finale, Restituisce il numero di righe modificate.
    @Query(
        """
        UPDATE allenamenti
        SET durata_secondi = :durataSecondi
        WHERE id = :allenamentoId
        """
    )
    abstract suspend fun impostaDurataAllenamento(
        allenamentoId: Int,
        durataSecondi: Int
    ): Int

    //Inserisce una serie nel momento in cui
    @Insert
    abstract suspend fun inserisciSerieEseguita(
        serie: SerieEseguita
    ): Long


    //Aggiorna una SerieEseguita.
    @Update
    abstract suspend fun aggiornaSerieEseguita(
        serie: SerieEseguita
    )


    //Elimina una SerieEseguita.
    @Delete
    abstract suspend fun eliminaSerieEseguita(
        serie: SerieEseguita
    )


    //Recupera tutte le serie eseguite, appartenenti a un allenamento.
    @Query(
        """
        SELECT *
        FROM serie_eseguite
        WHERE allenamento_id = :allenamentoId
        ORDER BY ordine_esercizio ASC, numero_serie ASC
        """
    )
    abstract suspend fun getSerieEseguite(
        allenamentoId: Int
    ): List<SerieEseguita>


    //Conta quante serie completate durante l'allenamento.
    @Query(
        """
        SELECT COUNT(*)
        FROM serie_eseguite
        WHERE allenamento_id = :allenamentoId
        """
    )
    abstract suspend fun contaSerieEseguite(
        allenamentoId: Int
    ): Int

    //Recupera un Allenamento insieme a tutte le serie esguite.
    @Transaction
    @Query(
        """
        SELECT *
        FROM allenamenti
        WHERE id = :allenamentoId
        LIMIT 1
        """
    )
    abstract suspend fun getAllenamentoCompleto(
        allenamentoId: Int
    ): AllenamentoCompleto?

    //Restituisce il riepilogo degli allenamenti conclusi (durata>0s) e ne calcola esercizi svolti e serie completate
    @Query(
        """
        SELECT
            a.id AS id,
            a.nome_scheda AS nomeScheda,
            a.data_inizio AS dataInizio,
            a.durata_secondi AS durataSecondi,
            COUNT(se.id) AS numeroSerie,
            COUNT(DISTINCT se.ordine_esercizio) AS numeroEsercizi
        FROM allenamenti AS a
        INNER JOIN serie_eseguite AS se
            ON se.allenamento_id = a.id
        WHERE a.durata_secondi > 0
        GROUP BY
            a.id,
            a.nome_scheda,
            a.data_inizio,
            a.durata_secondi
        ORDER BY a.data_inizio DESC
        """
    )
    abstract fun getStoricoAllenamenti(): Flow<List<AllenamentoRiepilogo>>


    //Prova a terminare un allenamento.
    //true se conclusione consentita.
    //false se non consentita
    @Transaction
    open suspend fun terminaAllenamento(
        allenamentoId: Int,
        durataSecondi: Int
    ): Boolean {

        // Una durata non valida non può concludere
        if (durataSecondi <= 0) {
            return false
        }

        val numeroSerie = contaSerieEseguite(allenamentoId)


        // allenamento non terminabile con 0 serie
        if (numeroSerie == 0) {
            return false
        }

        val righeAggiornate =
            impostaDurataAllenamento(
                allenamentoId = allenamentoId,
                durataSecondi = durataSecondi
            )
        return righeAggiornate == 1
    }
}