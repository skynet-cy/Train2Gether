package com.example.train2gether.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Esercizio::class,
        Scheda::class,
        EsercizioScheda::class,
        SeriePrevista::class,
        Allenamento::class,
        SerieEseguita::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase: RoomDatabase() {

    abstract fun esercizioDao(): EsercizioDao

}