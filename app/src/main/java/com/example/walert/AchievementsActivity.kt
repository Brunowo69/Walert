package com.example.walert

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.walert.databinding.ActivityAchievementsBinding

class AchievementsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAchievementsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val sharedPreferences = getSharedPreferences("WalertApp", Context.MODE_PRIVATE)
        val theme = sharedPreferences.getInt("selected_theme", R.style.Theme_Walert)
        setTheme(theme)

        super.onCreate(savedInstanceState)
        binding = ActivityAchievementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish() // Volver a la actividad anterior
        }

        setupAchievements()
    }

    private fun setupAchievements() {
        val sharedPreferences = getSharedPreferences("WalertApp", Context.MODE_PRIVATE)

        val generalAchievements = listOf(
            Achievement("Primer Vaso", "Bebiste tu primer vaso de agua del día.", R.drawable.ic_trophy, sharedPreferences.getBoolean("ach_first_glass", false)),
            Achievement("Meta Diaria", "Alcanzaste tu meta diaria de agua.", R.drawable.ic_trophy, sharedPreferences.getBoolean("ach_daily_goal", false))
        )

        val streakAchievements = listOf(
            Achievement("Racha de 3 Días", "Mantuviste tu meta por 3 días seguidos.", R.drawable.ic_trophy, false),
            Achievement("Racha de 7 Días", "¡Una semana completa!", R.drawable.ic_trophy, false)
        )

        val hiddenAchievements = listOf(
            Achievement("Explorador", "Descubriste una función oculta.", R.drawable.ic_trophy, sharedPreferences.getBoolean("ach_explorer", false)),
            Achievement("Madrugador", "Bebiste agua antes de las 8 AM.", R.drawable.ic_trophy, sharedPreferences.getBoolean("ach_early_bird", false)),
            Achievement("Noctámbulo", "Te hidrataste después de las 10 PM.", R.drawable.ic_trophy, sharedPreferences.getBoolean("ach_night_owl", false)),
            Achievement("Súper Hidratado", "Doblaste tu meta diaria de agua.", R.drawable.ic_trophy, sharedPreferences.getBoolean("ach_super_hydrated", false))
        )

        binding.rvGeneralAchievements.apply {
            layoutManager = LinearLayoutManager(this@AchievementsActivity)
            adapter = AchievementAdapter(generalAchievements)
        }

        binding.rvStreakAchievements.apply {
            layoutManager = LinearLayoutManager(this@AchievementsActivity)
            adapter = AchievementAdapter(streakAchievements)
        }

        binding.rvHiddenAchievements.apply {
            layoutManager = LinearLayoutManager(this@AchievementsActivity)
            adapter = AchievementAdapter(hiddenAchievements, isSecretCategory = true)
        }
    }
}
