package com.example.train2gether.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
//gestisce accessi agli esercizi memorizzati nel database.
@Dao
interface EsercizioDao {

    //inserisce una lista di esercizi nel database.
    @Insert
    suspend fun inserisciEsercizi(esercizi: List<Esercizio>)

    //recupera tutti gli esercizi ordinati alfabeticamente.
    @Query("SELECT * FROM esercizi ORDER BY nome ASC")
    suspend fun getTuttiEsercizi(): List<Esercizio>

    //recupera gli esercizi di un gruppo moscolare ordinati alfabeticamente.
    @Query("SELECT * FROM esercizi WHERE gruppo_muscolare = :gruppoMuscolare ORDER BY nome ASC")
    suspend fun getEserciziPerGruppo( gruppoMuscolare: String): List<Esercizio>

}