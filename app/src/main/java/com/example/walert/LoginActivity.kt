package com.example.walert

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.walert.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("WalertApp", Context.MODE_PRIVATE)
        val theme = sharedPreferences.getInt("selected_theme", R.style.Theme_Walert)
        setTheme(theme)

        super.onCreate(savedInstanceState)

        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvGoToRegister.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.btnLogin.setOnClickListener {
            binding.etEmail.error = null
            binding.tilPassword.error = null

            var validationFailed = false
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

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

            if (validationFailed) {
                return@setOnClickListener
            }

            val savedEmail = sharedPreferences.getString("user_email", null)
            val savedPassword = sharedPreferences.getString("user_password", null)

            if (email == savedEmail && password == savedPassword) {
                with(sharedPreferences.edit()) {
                    putBoolean("isLoggedIn", true)
                    apply()
                }

                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()

                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            } else {
                Toast.makeText(this, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
