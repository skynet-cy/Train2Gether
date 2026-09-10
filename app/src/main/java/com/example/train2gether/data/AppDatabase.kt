package com.example.train2gether.data


import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import net.zetetic.database.sqlcipher.SupportOpenHelperFactory

//database room principale dell'applicazione con entita gestite
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
abstract class AppDatabase : RoomDatabase() {

    //DAO utilizzati per acedere ai dati dell'applicazione
    abstract fun esercizioDao(): EsercizioDao
    abstract fun schedaDao(): SchedaDao
    abstract fun allenamentoDao(): AllenamentoDao

    companion object {

        private const val DATABASE_NAME = "train2gether_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        //popola il database con gli esercizi iniziali alla prima creazione.
        private val DATABASE_CALLBACK = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)

                DatiIniziali.esercizi.forEach { esercizio ->
                    db.execSQL(
                        """ INSERT INTO esercizi (nome, gruppo_muscolare) VALUES (?, ?) """.trimIndent(),
                        arrayOf(esercizio.nome, esercizio.gruppoMuscolare)
                    )
                }
            }
        }

        //Gestisce l'istanza singleton del database, creandola quando necessario.
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: run {
                    val appContext = context.applicationContext

                    //carica la libreria nativa SQLCipher.
                    System.loadLibrary("sqlcipher")

                    //recupera la chiave del database protetta dal Keystore.
                    val passphrase = DatabaseKeyManager.getOrCreateDatabaseKey(appContext)
                    val factory = SupportOpenHelperFactory(passphrase)
                    val databaseFile = appContext.getDatabasePath(DATABASE_NAME)

                    //crea il database Room utilizzando SQLCipher.
                    Room.databaseBuilder(
                        appContext,
                        AppDatabase::class.java,
                        databaseFile.absolutePath
                    )
                        .openHelperFactory(factory)
                        .addCallback(DATABASE_CALLBACK)
                        .build()
                        .also { INSTANCE = it }
                }
            }
        }
    }
}