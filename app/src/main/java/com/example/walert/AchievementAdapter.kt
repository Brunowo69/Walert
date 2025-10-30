package com.example.walert

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.walert.databinding.ItemAchievementBinding

class AchievementAdapter(
    private val achievements: List<Achievement>,
    private val isSecretCategory: Boolean = false
) : RecyclerView.Adapter<AchievementAdapter.AchievementViewHolder>() {

    private val revealedAchievements = mutableSetOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AchievementViewHolder {
        val binding = ItemAchievementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AchievementViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AchievementViewHolder, position: Int) {
        holder.bind(achievements[position])
    }

    override fun getItemCount() = achievements.size

    inner class AchievementViewHolder(private val binding: ItemAchievementBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(achievement: Achievement) {
            val isRevealed = revealedAchievements.contains(achievement.title)

            binding.tvAchievementTitle.text = achievement.title

            if (isSecretCategory && !isRevealed) {
                binding.tvAchievementDescription.text = "Pulsa para revelar la descripción."
            } else {
                binding.tvAchievementDescription.text = achievement.description
            }

            binding.ivAchievementIcon.setImageResource(achievement.iconResId)

            // Controlar el color y la opacidad individualmente
            if (achievement.isUnlocked) {
                binding.tvAchievementTitle.setTextColor(Color.parseColor("#0A2540"))
                binding.tvAchievementDescription.setTextColor(Color.parseColor("#5B6B79"))
                binding.ivAchievementIcon.alpha = 1.0f
            } else {
                binding.tvAchievementTitle.setTextColor(Color.parseColor("#9E9E9E"))
                binding.tvAchievementDescription.setTextColor(Color.parseColor("#BDBDBD"))
                binding.ivAchievementIcon.alpha = 0.5f
            }

            // Configurar la acción de clic para revelar
            if (isSecretCategory && !isRevealed) {
                itemView.setOnClickListener {
                    AlertDialog.Builder(itemView.context)
                        .setTitle("Revelar Logro Secreto")
                        .setMessage("¿Estás seguro de que quieres revelar la descripción de este logro?")
                        .setPositiveButton("Aceptar") { _, _ ->
                            revealedAchievements.add(achievement.title)
                            notifyItemChanged(adapterPosition)
                        }
                        .setNegativeButton("Cancelar", null)
                        .show()
                }
            } else {
                itemView.setOnClickListener(null)
            }
        }
    }
}
