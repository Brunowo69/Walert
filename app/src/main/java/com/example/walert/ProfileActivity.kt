package com.example.walert

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.walert.databinding.ActivityProfileBinding

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        // Aplicar el tema ANTES de llamar a super.onCreate y setContentView
        sharedPreferences = getSharedPreferences("WalertApp", Context.MODE_PRIVATE)
        setTheme(sharedPreferences.getInt("selected_theme", R.style.Theme_Walert))

        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadProfileData()
        setupThemeSelector()
        setupClickListeners()
    }

    private fun setupClickListeners() {
        binding.switchAutoGoal.setOnCheckedChangeListener { _, isChecked ->
            binding.etDailyGoal.isEnabled = !isChecked
        }

        binding.btnSaveChanges.setOnClickListener {
            saveProfileData()
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
        }

        binding.btnLogout.setOnClickListener {
            showConfirmationDialog("Cerrar Sesión", "¿Estás seguro de que quieres cerrar sesión?", this::logout)
        }

        binding.btnResetApp.setOnClickListener {
            showConfirmationDialog("Resetear Aplicación", "¿Estás seguro de que quieres resetear la aplicación? Todos los datos se perderán.", this::resetApp)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    private fun setupThemeSelector() {
        binding.themeBlue.setOnClickListener { selectTheme(R.style.Theme_Walert, "blue") }
        binding.themeGreen.setOnClickListener { selectTheme(R.style.Theme_Walert_Green, "green") }
        binding.themePurple.setOnClickListener { selectTheme(R.style.Theme_Walert_Purple, "purple") }
        binding.themeOrange.setOnClickListener { selectTheme(R.style.Theme_Walert_Orange, "orange") }
        binding.themeRed.setOnClickListener { selectTheme(R.style.Theme_Walert_Red, "red") }
    }

    private fun selectTheme(themeId: Int, themeName: String) {
        with(sharedPreferences.edit()) {
            putInt("selected_theme", themeId)
            putString("selected_theme_name", themeName)
            apply()
        }
        recreate()
    }

    private fun updateCheckmarks(selectedTheme: String) {
        binding.ivCheckBlue.visibility = if (selectedTheme == "blue") View.VISIBLE else View.GONE
        binding.ivCheckGreen.visibility = if (selectedTheme == "green") View.VISIBLE else View.GONE
        binding.ivCheckPurple.visibility = if (selectedTheme == "purple") View.VISIBLE else View.GONE
        binding.ivCheckOrange.visibility = if (selectedTheme == "orange") View.VISIBLE else View.GONE
        binding.ivCheckRed.visibility = if (selectedTheme == "red") View.VISIBLE else View.GONE
    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Aceptar") { _, _ -> onConfirm() }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        with(sharedPreferences.edit()) {
            putBoolean("isLoggedIn", false)
            apply()
        }
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun resetApp() {
        with(sharedPreferences.edit()) {
            clear()
            apply()
        }
        Toast.makeText(this, "Aplicación reseteada", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
    }

    private fun loadProfileData() {
        val imageUriString = sharedPreferences.getString("profile_image_uri", null)
        if (imageUriString != null) {
            binding.ivProfileImage.setImageURI(Uri.parse(imageUriString))
        }

        binding.etUsername.setText(sharedPreferences.getString("profile_username", ""))
        val weightStr = sharedPreferences.all["profile_weight"]?.toString() ?: ""
        binding.etWeight.setText(weightStr)
        binding.etHeight.setText(sharedPreferences.getString("profile_height", ""))
        binding.etAge.setText(sharedPreferences.getString("profile_age", ""))
        binding.rgGender.check(sharedPreferences.getInt("profile_gender", R.id.rbMale))
        binding.switchAutoGoal.isChecked = sharedPreferences.getBoolean("profile_auto_goal", true)
        val dailyGoalStr = sharedPreferences.all["profile_daily_goal"]?.toString() ?: ""
        binding.etDailyGoal.setText(dailyGoalStr)
        val activityLevel = sharedPreferences.all["profile_activity_level"]?.toString() ?: "sedentary"
        binding.rgActivityLevel.check(when (activityLevel) {
            "light" -> R.id.rbLightlyActive
            "active" -> R.id.rbActive
            "very_active" -> R.id.rbVeryActive
            else -> R.id.rbSedentary
        })
        binding.rgReminderInterval.check(sharedPreferences.getInt("profile_reminder_interval", R.id.rb1Hour))
        binding.etDailyGoal.isEnabled = !binding.switchAutoGoal.isChecked

        val selectedTheme = sharedPreferences.getString("selected_theme_name", "blue")
        updateCheckmarks(selectedTheme ?: "blue")
    }

    private fun saveProfileData() {
        with(sharedPreferences.edit()) {
            putString("profile_username", binding.etUsername.text.toString())
            putString("profile_weight", binding.etWeight.text.toString())
            putString("profile_height", binding.etHeight.text.toString())
            putString("profile_age", binding.etAge.text.toString())
            putInt("profile_gender", binding.rgGender.checkedRadioButtonId)
            putBoolean("profile_auto_goal", binding.switchAutoGoal.isChecked)
            putString("profile_daily_goal", binding.etDailyGoal.text.toString())
            val activityLevel = when (binding.rgActivityLevel.checkedRadioButtonId) {
                R.id.rbLightlyActive -> "light"
                R.id.rbActive -> "active"
                R.id.rbVeryActive -> "very_active"
                else -> "sedentary"
            }
            putString("profile_activity_level", activityLevel)
            putInt("profile_reminder_interval", binding.rgReminderInterval.checkedRadioButtonId)
            apply()
        }
    }
}
