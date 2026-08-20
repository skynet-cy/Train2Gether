package com.example.train2gether.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface EsercizioDao {

    @Insert
    suspend fun inserisciEsercizi(esercizi: List<Esercizio>)

    @Query("SELECT * FROM esercizi ORDER BY nome ASC")
    suspend fun getTuttiEsercizi(): List<Esercizio>

    @Query("SELECT * FROM esercizi WHERE gruppo_muscolare = :gruppoMuscolare ORDER BY nome ASC")
    suspend fun getEserciziPerGruppo( gruppoMuscolare: String): List<Esercizio>

}