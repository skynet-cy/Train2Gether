package com.example.train2gether.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


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



@Entity(
    tableName = "schede"
)
data class Scheda(

    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val nome: String
)


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

    @ColumnInfo(name = "nome_scheda")
    val nomeScheda: String? = null,

    @ColumnInfo(name = "data_inizio")
    val dataInizio: Long,

    @ColumnInfo(name = "durata_secondi")
    val durataSecondi: Int
)


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