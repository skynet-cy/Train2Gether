package com.example.train2gether

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

data class SchedaAllenamento(
    var id: String,
    var nomeScheda: String,
    var listaEsercizi: List<EsercizioConSerie>
)

object GestoreSchede {

    private const val PREFS_NAME = "Train2getherPrefs"
    private const val KEY_SCHEDE = "SchedeAllenamento"

    // 2. DICHIARAZIONE DI gson (mancava questa riga!)
    private val gson = Gson()

    private fun getPreferences(context: Context) =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun salvaScheda(context: Context, nuovaScheda: SchedaAllenamento) {
        val schede = caricaTutteLeSchede(context).toMutableList()
        val index = schede.indexOfFirst { it.id == nuovaScheda.id }
        if (index != -1) {
            schede[index] = nuovaScheda
        } else {
            schede.add(nuovaScheda)
        }
        val json = gson.toJson(schede)
        getPreferences(context).edit().putString(KEY_SCHEDE, json).apply()
    }

    fun caricaTutteLeSchede(context: Context): List<SchedaAllenamento> {
        val json = getPreferences(context).getString(KEY_SCHEDE, null) ?: return emptyList()
        val type = object : TypeToken<List<SchedaAllenamento>>() {}.type
        return gson.fromJson(json, type)
    }

    fun eliminaScheda(context: Context, schedaId: String) {
        val schede = caricaTutteLeSchede(context).toMutableList()
        schede.removeAll { it.id == schedaId }
        val json = gson.toJson(schede)
        getPreferences(context).edit().putString(KEY_SCHEDE, json).apply()
    }
    fun aggiornaScheda(context: Context, schedaAggiornata: SchedaAllenamento) {
        salvaScheda(context, schedaAggiornata)
    }
}
