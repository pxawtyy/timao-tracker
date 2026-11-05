package com.timao.tracker

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.timaoDataStore by preferencesDataStore(name = "timao_preferences")

object TimaoPreferencesKeys {
    val NOME_TORCEDOR = stringPreferencesKey("nome_torcedor")
    val TORCIDA_ORGANIZADA = stringPreferencesKey("torcida_organizada")
    val MODO_PISTOLA = booleanPreferencesKey("modo_pistola")
    val FRASE_FAVORITA = stringPreferencesKey("frase_favorita")
}

data class TimaoPerfil(
    val nome: String,
    val torcida: String,
    val modoPistola: Boolean,
    val fraseFavorita: String
)

class TimaoDataStore(private val context: Context) {

    val perfilFlow: Flow<TimaoPerfil> = context.timaoDataStore.data.map { pref ->
        TimaoPerfil(
            nome = pref[TimaoPreferencesKeys.NOME_TORCEDOR] ?: "Gavião da Fiel",
            torcida = pref[TimaoPreferencesKeys.TORCIDA_ORGANIZADA] ?: "Gaviões da Fiel",
            modoPistola = pref[TimaoPreferencesKeys.MODO_PISTOLA] ?: true,
            fraseFavorita = pref[TimaoPreferencesKeys.FRASE_FAVORITA] ?: "CÊ É LOUCO PÃO DE QUEIJO! 🧀⚽"
        )
    }

    suspend fun salvarPerfil(nome: String, torcida: String, modoPistola: Boolean, frase: String) {
        context.timaoDataStore.edit { pref ->
            pref[TimaoPreferencesKeys.NOME_TORCEDOR] = nome
            pref[TimaoPreferencesKeys.TORCIDA_ORGANIZADA] = torcida
            pref[TimaoPreferencesKeys.MODO_PISTOLA] = modoPistola
            pref[TimaoPreferencesKeys.FRASE_FAVORITA] = frase
        }
    }
}
