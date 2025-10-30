package com.example.walert

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.walert.databinding.ActivityCalendarBinding
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.daysOfWeek
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class CalendarActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCalendarBinding
    private lateinit var sharedPreferences: SharedPreferences
    private var selectedDate: LocalDate? = null
    private val dateFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferences = getSharedPreferences("WalertApp", Context.MODE_PRIVATE)
        val theme = sharedPreferences.getInt("selected_theme", R.style.Theme_Walert)
        setTheme(theme)

        super.onCreate(savedInstanceState)
        binding = ActivityCalendarBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.editDayCard.visibility = View.GONE

        setupBarChart()
        setupCalendar()
        setupEditDayCard()
    }

    private fun setupEditDayCard() {
        binding.btnAddWater.setOnClickListener {
            updateWaterCount(1)
        }

        binding.btnRemoveWater.setOnClickListener {
            updateWaterCount(-1)
        }

        binding.btnSaveNote.setOnClickListener {
            saveNote()
        }
    }

    private fun saveNote() {
        selectedDate?.let {
            with(sharedPreferences.edit()) {
                putString("${it}_note", binding.etNote.text.toString())
                apply()
            }
            Toast.makeText(this, "Nota guardada", Toast.LENGTH_SHORT).show()
            binding.calendarView.notifyDateChanged(it)
        }
    }

    private fun updateWaterCount(amount: Int) {
        selectedDate?.let {
            var waterCount = sharedPreferences.getInt(it.toString(), 0)
            waterCount += amount
            if (waterCount >= 0) {
                with(sharedPreferences.edit()) {
                    putInt(it.toString(), waterCount)
                    if (waterCount > 0) {
                        putBoolean("${it}_edited", true)
                    } else {
                        remove("${it}_edited")
                    }
                    apply()
                }
                updateSelectedDay(it)
                setupBarChart()
                binding.calendarView.notifyDateChanged(it)
            }
        }
    }

    private fun setupBarChart() {
        val entries = ArrayList<BarEntry>()
        val days = ArrayList<String>()
        val today = LocalDate.now()

        for (i in 6 downTo 0) {
            val day = today.minusDays(i.toLong())
            val waterCount = sharedPreferences.getInt(day.toString(), 0)
            entries.add(BarEntry( (6 - i).toFloat(), waterCount.toFloat()))
            days.add(day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()))
        }

        val dataSet = BarDataSet(entries, "Vasos de agua")
        dataSet.color = Color.parseColor("#42A5F5")
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        binding.barChart.data = barData

        val xAxis = binding.barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(days)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)

        binding.barChart.axisRight.isEnabled = false
        binding.barChart.legend.isEnabled = false
        binding.barChart.description.isEnabled = false

        binding.barChart.invalidate()
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        val startMonth = currentMonth.minusMonths(100)
        val endMonth = currentMonth.plusMonths(100)
        val daysOfWeek = daysOfWeek(firstDayOfWeek = DayOfWeek.MONDAY)

        binding.calendarView.setup(startMonth, endMonth, daysOfWeek.first())
        binding.calendarView.scrollToMonth(currentMonth)

        class DayViewContainer(view: View) : ViewContainer(view) {
            lateinit var day: CalendarDay
            val textView = view.findViewById<TextView>(R.id.tvDay)
            val noteIndicator = view.findViewById<View>(R.id.vNoteIndicator)

            init {
                view.setOnClickListener {
                    if (day.position == DayPosition.MonthDate) {
                        if (day.date.isAfter(LocalDate.now())) {
                            Toast.makeText(view.context, "No se pueden editar días futuros", Toast.LENGTH_SHORT).show()
                        } else {
                            if (selectedDate != day.date) {
                                val oldDate = selectedDate
                                selectedDate = day.date
                                binding.calendarView.notifyDateChanged(day.date)
                                oldDate?.let { binding.calendarView.notifyDateChanged(it) }
                                updateSelectedDay(day.date)
                            }
                        }
                    }
                }
            }
        }

        binding.calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) {
                container.day = data
                container.textView.text = data.date.dayOfMonth.toString()

                container.textView.background = null
                container.textView.setTextColor(Color.BLACK)
                container.noteIndicator.visibility = View.GONE

                if (data.position == DayPosition.MonthDate) {
                    if (data.date.isAfter(LocalDate.now())) {
                        container.textView.setTextColor(Color.LTGRAY)
                    } else {
                        val waterCount = sharedPreferences.getInt(data.date.toString(), 0)
                        val wasEdited = sharedPreferences.getBoolean("${data.date}_edited", false)
                        val note = sharedPreferences.getString("${data.date}_note", "")

                        if (note?.isNotEmpty() == true) {
                            container.noteIndicator.visibility = View.VISIBLE
                        }

                        if (data.date == selectedDate) {
                            container.textView.setBackgroundResource(R.drawable.selected_day_bg)
                            container.textView.setTextColor(Color.WHITE)
                        } else if (wasEdited) {
                            container.textView.setBackgroundResource(R.drawable.edited_day_bg)
                        } else if (waterCount > 0 && data.date == LocalDate.now()) {
                            container.textView.setBackgroundResource(R.drawable.today_water_bg)
                        }
                    }
                } else {
                    container.textView.setTextColor(Color.GRAY)
                }
            }
        }

        binding.calendarView.monthScrollListener = {
            binding.tvMonthTitle.text = it.yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + " ${it.yearMonth.year}"
        }
    }

    private fun updateSelectedDay(date: LocalDate) {
        binding.editDayCard.visibility = View.VISIBLE
        binding.tvSelectedDay.text = date.format(dateFormatter)

        if (date.isAfter(LocalDate.now())) {
            binding.tvWaterCount.visibility = View.GONE
            binding.btnAddWater.visibility = View.GONE
            binding.btnRemoveWater.visibility = View.GONE
        } else {
            binding.tvWaterCount.visibility = View.VISIBLE
            binding.btnAddWater.visibility = View.VISIBLE
            binding.btnRemoveWater.visibility = View.VISIBLE
            val waterCount = sharedPreferences.getInt(date.toString(), 0)
            binding.tvWaterCount.text = waterCount.toString()
        }

        val note = sharedPreferences.getString("${date}_note", "")
        binding.etNote.setText(note)
    }
}
