package com.example.train2gether.data

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room

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

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "train2gether_database"
                ).build().also {
                    INSTANCE = it
                }
            }
        }
    }

}