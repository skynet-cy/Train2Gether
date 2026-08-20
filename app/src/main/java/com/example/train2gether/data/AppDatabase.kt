package com.example.train2gether.data

import androidx.room.Database
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase

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

        private val DATABASE_CALLBACK = object : RoomDatabase.Callback() {

            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                DatiIniziali.esercizi.forEach { esercizio ->

                    db.execSQL(
                        """ INSERT INTO esercizi (nome, gruppo_muscolare) VALUES (?, ?) """.trimIndent(),
                        arrayOf(
                            esercizio.nome,
                            esercizio.gruppoMuscolare
                        )
                    )
                }
            }
        }

        fun getDatabase(context: Context): AppDatabase {

            return INSTANCE ?: synchronized(this) {

                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "train2gether_database"
                ).addCallback(DATABASE_CALLBACK).build().also {
                    INSTANCE = it
                }
            }
        }
    }

}