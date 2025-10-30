package com.example.walert

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.walert.databinding.ActivityRegisterBinding

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("WalertApp", Context.MODE_PRIVATE)
        val theme = sharedPreferences.getInt("selected_theme", R.style.Theme_Walert)
        setTheme(theme)

        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnRegister.setOnClickListener {
            binding.etEmail.error = null
            binding.tilPassword.error = null
            binding.tilConfirmPassword.error = null

            var validationFailed = false
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            if (email.isEmpty()) {
                binding.etEmail.error = "Por favor, introduce tu correo"
                validationFailed = true
            } else if (!email.contains("@")) {
                binding.etEmail.error = "Formato inválido, debe incluir un '@'"
                validationFailed = true
            }

            if (password.isEmpty()) {
                binding.tilPassword.error = "Por favor, introduce tu contraseña"
                validationFailed = true
            } else if (password.length < 6) {
                binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
                validationFailed = true
            }

            if (confirmPassword.isEmpty()) {
                binding.tilConfirmPassword.error = "Por favor, confirma tu contraseña"
                validationFailed = true
            } else if (password != confirmPassword) {
                binding.tilConfirmPassword.error = "Las contraseñas no coinciden"
                validationFailed = true
            }

            if (validationFailed) {
                return@setOnClickListener
            }

            with(sharedPreferences.edit()) {
                putString("user_email", email)
                putString("user_password", password)
                putBoolean("profile_auto_goal", false)
                putString("profile_daily_goal", "8")
                apply()
            }

            Toast.makeText(this, "Registro exitoso", Toast.LENGTH_SHORT).show()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        binding.tvGoToLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
