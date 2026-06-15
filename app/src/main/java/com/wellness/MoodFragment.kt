package com.wellness

import android.app.AlertDialog
import android.graphics.Canvas
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CalendarView
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.wellness.model.Mood
import java.util.*

class MoodFragment : Fragment() {

    private lateinit var prefs: PrefsRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: MoodAdapter
    private lateinit var tvEmptyMood: TextView
    private lateinit var tvMoodSummary: TextView
    private lateinit var calendarView: CalendarView

    private var moods = mutableListOf<Mood>()
    private var filteredMoods = mutableListOf<Mood>()
    private var selectedDate: Long = System.currentTimeMillis()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_mood, container, false)

        prefs = PrefsRepository(requireContext())
        moods = prefs.loadMoods().toMutableList()

        calendarView = view.findViewById(R.id.calendarView)
        calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance()
            cal.set(year, month, dayOfMonth, 0, 0, 0)
            selectedDate = cal.timeInMillis
            filterMoodsByDate()
        }

        recyclerView = view.findViewById(R.id.recyclerMoods)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        tvEmptyMood = view.findViewById(R.id.tvEmptyMood)
        tvMoodSummary = view.findViewById(R.id.tvMoodSummary)

        val fabAdd = view.findViewById<FloatingActionButton>(R.id.fabAddMood)
        fabAdd.setOnClickListener { showAddMoodDialog() }

        val itemTouchHelper = ItemTouchHelper(swipeCallback)
        itemTouchHelper.attachToRecyclerView(recyclerView)

        filterMoodsByDate() // initial load
        updateMoodSummary()

        return view
    }

    private fun showAddMoodDialog() {
        val emojis = arrayOf("😄", "😊", "😐", "😢", "😡", "😭")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Mood Emoji")
            .setItems(emojis) { _, which ->
                val selectedEmoji = emojis[which]

                val input = EditText(requireContext())
                input.hint = "Add description (optional)"
                input.inputType = InputType.TYPE_CLASS_TEXT

                AlertDialog.Builder(requireContext())
                    .setTitle("Mood Description")
                    .setView(input)
                    .setPositiveButton("Save") { _, _ ->
                        val mood = Mood(
                            emoji = selectedEmoji,
                            description = input.text.toString(),
                            timestamp = System.currentTimeMillis()
                        )
                        moods.add(0, mood)
                        prefs.saveMoods(moods)
                        filterMoodsByDate()
                        updateMoodSummary()
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            .show()
    }

    private val swipeCallback = object : ItemTouchHelper.SimpleCallback(
        0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
    ) {
        override fun onMove(
            recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder
        ): Boolean = false

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            val removedMood = filteredMoods[position]
            moods.remove(removedMood)
            prefs.saveMoods(moods)
            filterMoodsByDate()
            updateMoodSummary()

            view?.let {
                Snackbar.make(it, "Mood deleted", Snackbar.LENGTH_LONG).setAction("UNDO") {
                    moods.add(0, removedMood)
                    prefs.saveMoods(moods)
                    filterMoodsByDate()
                    updateMoodSummary()
                }.show()
            }
        }

        override fun onChildDraw(
            c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
            dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
        ) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
        }
    }

    private fun filterMoodsByDate() {
        val calSelected = Calendar.getInstance()
        calSelected.timeInMillis = selectedDate

        filteredMoods = moods.filter {
            val calMood = Calendar.getInstance()
            calMood.timeInMillis = it.timestamp
            calMood.get(Calendar.YEAR) == calSelected.get(Calendar.YEAR)
                    && calMood.get(Calendar.MONTH) == calSelected.get(Calendar.MONTH)
                    && calMood.get(Calendar.DAY_OF_MONTH) == calSelected.get(Calendar.DAY_OF_MONTH)
        }.toMutableList()

        adapter = MoodAdapter(filteredMoods)
        recyclerView.adapter = adapter
        tvEmptyMood.visibility = if (filteredMoods.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun updateMoodSummary() {
        val happy = moods.count { it.emoji == "😄" || it.emoji == "😊" }
        val neutral = moods.count { it.emoji == "😐" }
        val sad = moods.count { it.emoji == "😢" || it.emoji == "😭" }
        val angry = moods.count { it.emoji == "😡" }
        tvMoodSummary.text = "This week: $happy happy, $neutral neutral, $sad sad, $angry angry"
    }
}
