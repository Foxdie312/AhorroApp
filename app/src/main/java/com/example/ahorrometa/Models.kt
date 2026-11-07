package com.example.ahorrometa

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

@Serializable
data class Goal(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val targetAmount: Double,
    val savedAmount: Double = 0.0,
    val deadline: String = ""
) {
    val progress: Float
        get() = if (targetAmount <= 0.0) 0f
        else (savedAmount / targetAmount).toFloat().coerceIn(0f, 1f)
}

@Serializable
data class Movement(
    val id: String = UUID.randomUUID().toString(),
    val goalId: String? = null,
    val amount: Double,
    val isDeposit: Boolean,
    val note: String = "",
    val date: String
)

object Store {
    private const val PREFS = "ahorrometa_prefs"
    private const val KEY_GOALS = "goals"
    private const val KEY_MOVEMENTS = "movements"

    private val json = Json { ignoreUnknownKeys = true }

    fun loadGoals(context: android.content.Context): MutableList<Goal> {
        val prefs = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_GOALS, null) ?: return mutableListOf()
        return try {
            json.decodeFromString<List<Goal>>(raw).toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun loadMovements(context: android.content.Context): MutableList<Movement> {
        val prefs = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_MOVEMENTS, null) ?: return mutableListOf()
        return try {
            json.decodeFromString<List<Movement>>(raw).toMutableList()
        } catch (_: Exception) {
            mutableListOf()
        }
    }

    fun save(context: android.content.Context, goals: List<Goal>, movements: List<Movement>) {
        val prefs = context.getSharedPreferences(PREFS, android.content.Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_GOALS, json.encodeToString(goals))
            .putString(KEY_MOVEMENTS, json.encodeToString(movements))
            .apply()
    }
}
