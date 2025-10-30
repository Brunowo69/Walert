package com.example.walert

import android.Manifest
import android.animation.ObjectAnimator
import android.app.Dialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.walert.databinding.ActivityMainBinding
import java.time.LocalDate
import java.time.LocalTime
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var waterGoal = 8
    private var currentWaterCount = 0
    private var drinkReminderTimer: CountDownTimer? = null
    private var currentTheme: Int = 0
    private val waterFacts = listOf(
        "¿Sabías que el agua ayuda a regular la temperatura corporal?",
        "Un 70% de la Tierra está cubierta de agua.",
        "Beber agua puede ayudar a prevenir dolores de cabeza.",
        "El agua es esencial para la digestión de los alimentos.",
        "Un cuerpo humano adulto está compuesto en un 60% de agua.",
        "Beber suficiente agua mejora la salud de la piel.",
        "El agua transporta nutrientes y oxígeno a todas las células del cuerpo.",
        "Sentir sed ya es un signo de deshidratación.",
        "El agua puede existir en tres estados: sólido, líquido y gaseoso.",
        "Aproximadamente el 97% del agua del mundo es salada y no potable."
    )

    private val factHandler = Handler(Looper.getMainLooper())
    private lateinit var factRunnable: Runnable

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permiso concedido
        } else {
            // Permiso denegado
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("WalertApp", Context.MODE_PRIVATE)
        currentTheme = sharedPreferences.getInt("selected_theme", R.style.Theme_Walert)
        setTheme(currentTheme)

        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        createNotificationChannel()
        askForNotificationPermission()
        loadWaterGoal()
        loadWaterCount()
        updateWaterLevel(false)
        setupFactTimer()

        binding.tvTitle.setOnClickListener {
            unlockAchievement("ach_explorer", "¡Explorador!")
        }

        binding.btnProfile.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.btnCalendar.setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
        }

        binding.btnAchievements.setOnClickListener {
            startActivity(Intent(this, AchievementsActivity::class.java))
        }

        binding.btnAddWater.setOnClickListener {
            if (currentWaterCount < waterGoal) {
                currentWaterCount++
                updateWaterLevel(true)
                startDrinkReminderTimer()
                saveWaterCount()
                checkAchievements()
            }
        }

        binding.btnRemoveWater.setOnClickListener {
            if (currentWaterCount > 0) {
                currentWaterCount--
                updateWaterLevel(true)
                saveWaterCount()
            }
        }
    }

    private fun checkAchievements() {
        if (currentWaterCount >= 1 && !sharedPreferences.getBoolean("ach_first_glass", false)) {
            unlockAchievement("ach_first_glass", "¡Primer Vaso!")
        }
        if (currentWaterCount >= waterGoal && !sharedPreferences.getBoolean("ach_daily_goal", false)) {
            unlockAchievement("ach_daily_goal", "¡Meta Diaria Completada!")
        }
        if (currentWaterCount >= waterGoal * 2 && !sharedPreferences.getBoolean("ach_super_hydrated", false)) {
            unlockAchievement("ach_super_hydrated", "¡Súper Hidratado!")
        }
        val now = LocalTime.now()
        if (now.isBefore(LocalTime.of(8, 0)) && !sharedPreferences.getBoolean("ach_early_bird", false)) {
            unlockAchievement("ach_early_bird", "¡Madrugador!")
        }
        if (now.isAfter(LocalTime.of(22, 0)) && !sharedPreferences.getBoolean("ach_night_owl", false)) {
            unlockAchievement("ach_night_owl", "¡Noctámbulo!")
        }
    }

    private fun unlockAchievement(key: String, name: String) {
        if (sharedPreferences.getBoolean(key, false)) return
        with(sharedPreferences.edit()) {
            putBoolean(key, true)
            apply()
        }
        showAchievementDialog(name)
    }

    private fun showAchievementDialog(name: String) {
        val dialog = Dialog(this)
        dialog.setContentView(R.layout.dialog_achievement_unlocked)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val achievementName = dialog.findViewById<TextView>(R.id.tvAchievementNameDialog)
        achievementName.text = name

        val icon = dialog.findViewById<ImageView>(R.id.ivAchievementIconDialog)
        val zoomIn = AnimationUtils.loadAnimation(this, R.anim.zoom_in)
        icon.startAnimation(zoomIn)

        dialog.show()

        // Ocultar el diálogo después de unos segundos
        Handler(Looper.getMainLooper()).postDelayed({ dialog.dismiss() }, 3000)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Recordatorios de Walert"
            val descriptionText = "Canal para recordatorios de beber agua."
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("walert_reminder_channel", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun showDrinkWaterNotification() {
        val username = sharedPreferences.getString("profile_username", "")
        val title = "¡Hora de hidratarse!"
        val message = if (username?.isNotEmpty() == true) {
            "¡Hey $username! Es momento de tomar un vaso de agua."
        } else {
            "Es momento de tomar un vaso de agua."
        }

        val builder = NotificationCompat.Builder(this, "walert_reminder_channel")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(this)) {
                notify(1, builder.build())
            }
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun loadWaterGoal() {
        val isAutoGoal = sharedPreferences.getBoolean("profile_auto_goal", true)
        waterGoal = if (isAutoGoal) {
            calculateAutoGoal()
        } else {
            val dailyGoalStr = sharedPreferences.all["profile_daily_goal"]?.toString() ?: "8"
            dailyGoalStr.toIntOrNull() ?: 8
        }
        if (waterGoal == 0) waterGoal = 8
    }

    private fun calculateAutoGoal(): Int {
        val weightStr = sharedPreferences.all["profile_weight"]?.toString() ?: "70"
        val weight = weightStr.toFloatOrNull() ?: 70f
        val activityLevel = sharedPreferences.all["profile_activity_level"]?.toString() ?: "sedentary"

        var baseGoal = (weight * 35).toInt()

        when (activityLevel) {
            "light" -> baseGoal += 300
            "active" -> baseGoal += 600
            "very_active" -> baseGoal += 900
        }

        val result = (baseGoal / 250)
        return if (result > 0) result else 1
    }

    private fun loadWaterCount() {
        val today = LocalDate.now().toString()
        currentWaterCount = sharedPreferences.getInt(today, 0)
    }

    private fun saveWaterCount() {
        val today = LocalDate.now().toString()
        with(sharedPreferences.edit()) {
            putInt(today, currentWaterCount)
            apply()
        }
    }

    private fun setupFactTimer() {
        factRunnable = Runnable {
            showRandomFact()
            factHandler.postDelayed(factRunnable, 1800000)
        }
        factHandler.post(factRunnable)
    }

    private fun showRandomFact() {
        binding.tvFact.text = waterFacts.random()
    }

    private fun startDrinkReminderTimer() {
        drinkReminderTimer?.cancel()
        binding.tvTimer.visibility = View.VISIBLE

        val reminderIntervalId = sharedPreferences.getInt("profile_reminder_interval", R.id.rb1Hour)
        val duration = when (reminderIntervalId) {
            R.id.rb2Hours -> TimeUnit.HOURS.toMillis(2)
            R.id.rb4Hours -> TimeUnit.HOURS.toMillis(4)
            else -> TimeUnit.HOURS.toMillis(1)
        }

        drinkReminderTimer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished)
                val minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60
                val seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60
                binding.tvTimer.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            }

            override fun onFinish() {
                binding.tvTimer.visibility = View.INVISIBLE
                showDrinkWaterNotification()
            }
        }.start()
    }

    private fun updateWaterLevel(animate: Boolean) {
        binding.tvProgress.text = "$currentWaterCount / $waterGoal"

        val newLevel = if (waterGoal > 0) {
            (currentWaterCount.toFloat() / waterGoal.toFloat() * 10000).toInt()
        } else {
            0
        }

        if (animate) {
            val animator = ObjectAnimator.ofInt(binding.bottleFill.drawable, "level", binding.bottleFill.drawable.level, newLevel)
            animator.duration = 500
            animator.interpolator = DecelerateInterpolator()
            animator.start()
        } else {
            binding.bottleFill.setImageLevel(newLevel)
        }
    }

    override fun onResume() {
        super.onResume()
        val selectedTheme = sharedPreferences.getInt("selected_theme", R.style.Theme_Walert)
        if (currentTheme != selectedTheme) {
            recreate()
        }
        loadWaterGoal()
        updateWaterLevel(false)
    }

    override fun onDestroy() {
        super.onDestroy()
        drinkReminderTimer?.cancel()
        factHandler.removeCallbacks(factRunnable)
    }
}
