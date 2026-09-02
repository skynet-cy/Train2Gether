package com.example.train2gether.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

//rappresenta un esercizio disponibile nell'applicazione
@Entity(
    tableName = "esercizi"
)
data class Esercizio(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String,

    @ColumnInfo(name = "gruppo_muscolare")
    val gruppoMuscolare: String
)


//rappresenta una scheda di allenamento creata dall'utente.
@Entity(
    tableName = "schede"
)
data class Scheda(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String
)

//rappresenta un esercizio associandolo alla scheda definendone ordine e tempo di recupero.
@Entity(
    tableName = "esercizi_scheda",

    foreignKeys = [
        ForeignKey(
            entity = Scheda::class,
            parentColumns = ["id"],
            childColumns = ["scheda_id"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            entity = Esercizio::class,
            parentColumns = ["id"],
            childColumns = ["esercizio_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["scheda_id"]),
        Index(value = ["esercizio_id"])
    ]
)
data class EsercizioScheda(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "scheda_id")
    val schedaId: Int,

    @ColumnInfo(name = "esercizio_id")
    val esercizioId: Int,

    @ColumnInfo(name = "riposo_secondi")
    val riposoSecondi: Int,

    val ordine: Int
)

//rappresenta una serie di un esercizio presente nella scheda con ripetizioni e peso previsti poiche modificabili in allenamento
@Entity(
    tableName = "serie_previste",

    foreignKeys = [
        ForeignKey(
            entity = EsercizioScheda::class,
            parentColumns = ["id"],
            childColumns = ["esercizio_scheda_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["esercizio_scheda_id"])
    ]
)
data class SeriePrevista(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "esercizio_scheda_id")
    val esercizioSchedaId: Int,

    @ColumnInfo(name = "numero_serie")
    val numeroSerie: Int,

    val peso: Double?,

    val ripetizioni: Int
)

//rappresenta un allenamento avviato derivante da una scheda.
@Entity(
    tableName = "allenamenti",

    foreignKeys = [
        ForeignKey(
            entity = Scheda::class,
            parentColumns = ["id"],
            childColumns = ["scheda_id_origine"],
            onDelete = ForeignKey.SET_NULL
        )
    ],

    indices = [
        Index(value = ["scheda_id_origine"])
    ]
)
data class Allenamento(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "scheda_id_origine")
    val schedaIdOrigine: Int? = null,

    //mantiene il nome scheda in caso di cancellazione della scheda stessa.
    @ColumnInfo(name = "nome_scheda")
    val nomeScheda: String? = null,

    @ColumnInfo(name = "data_inizio")
    val dataInizio: Long,

    @ColumnInfo(name = "durata_secondi")
    val durataSecondi: Int
)

//rappresenta una serie con peso e ripetizioni effettivamente eseguite durante l'allenamento.
@Entity(
    tableName = "serie_eseguite",

    foreignKeys = [
        ForeignKey(
            entity = Allenamento::class,
            parentColumns = ["id"],
            childColumns = ["allenamento_id"],
            onDelete = ForeignKey.CASCADE
        ),

        ForeignKey(
            entity = Esercizio::class,
            parentColumns = ["id"],
            childColumns = ["esercizio_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],

    indices = [
        Index(value = ["allenamento_id"]),
        Index(value = ["esercizio_id"])
    ]
)
data class SerieEseguita(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "allenamento_id")
    val allenamentoId: Int,

    @ColumnInfo(name = "esercizio_id")
    val esercizioId: Int,

    @ColumnInfo(name = "nome_esercizio")
    val nomeEsercizio: String,

    @ColumnInfo(name = "ordine_esercizio")
    val ordineEsercizio: Int,

    @ColumnInfo(name = "numero_serie")
    val numeroSerie: Int,

    val peso: Double,
    
    val ripetizioni: Int
)