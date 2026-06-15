package com.wellness

import android.animation.ValueAnimator
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.wellness.model.Habit

class HabitsFragment : Fragment(), HabitsAdapter.HabitListener {

    private lateinit var prefs: PrefsRepository
    private lateinit var habitsRecyclerView: RecyclerView
    private lateinit var habitsAdapter: HabitsAdapter
    private var habitsList: MutableList<Habit> = mutableListOf()

    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var progressText: TextView
    private lateinit var emptyText: TextView
    private lateinit var fabAddHabit: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_habits, container, false)

        prefs = PrefsRepository(requireContext())
        habitsRecyclerView = view.findViewById(R.id.recyclerHabits)
        habitsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        progressBar = view.findViewById(R.id.circularProgress)
        progressText = view.findViewById(R.id.tvProgress)
        emptyText = view.findViewById(R.id.tvEmptyHabits)
        fabAddHabit = view.findViewById(R.id.fabAddHabit)

        loadHabits()
        habitsAdapter = HabitsAdapter(habitsList, this)
        habitsRecyclerView.adapter = habitsAdapter

        updateProgress(animated = false) // Initial load without animation

        fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }

        return view
    }

    private fun loadHabits() {
        habitsList.clear()
        habitsList.addAll(prefs.loadHabits())
        emptyText.visibility = if (habitsList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun saveHabits() {
        prefs.saveHabits(habitsList)
        updateProgress(animated = true)
    }

    /** Updated progress with smooth animation and animated text */
    private fun updateProgress(animated: Boolean) {
        val total = habitsList.size
        val completedCount = habitsList.count { it.completed }
        val percent = if (total == 0) 0 else (completedCount * 100) / total

        emptyText.visibility = if (total == 0) View.VISIBLE else View.GONE

        if (animated) {
            animateProgressBar(percent)
            animateProgressText(percent)
        } else {
            progressBar.progress = percent
            progressText.text = if (percent == 100) "All completed!" else "$percent% completed"
        }
    }

    private fun animateProgressBar(target: Int) {
        val animator = ValueAnimator.ofInt(progressBar.progress, target)
        animator.duration = 600
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            progressBar.progress = value
        }
        animator.start()
    }

    private fun animateProgressText(target: Int) {
        val currentPercent = progressText.text.toString().substringBefore("%").toIntOrNull() ?: 0
        val animator = ValueAnimator.ofInt(currentPercent, target)
        animator.duration = 600
        animator.interpolator = DecelerateInterpolator()
        animator.addUpdateListener { valueAnimator ->
            val value = valueAnimator.animatedValue as Int
            progressText.text = if (value == 100) "All completed!" else "$value% completed"
        }
        animator.start()
    }

    private fun showAddHabitDialog() {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        AlertDialog.Builder(requireContext())
            .setTitle("Add Habit")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotEmpty()) {
                    val habit = Habit(title = title, completed = false)
                    habitsList.add(habit)
                    habitsAdapter.notifyItemInserted(habitsList.size - 1)
                    saveHabits()
                    habitsRecyclerView.scrollToPosition(habitsList.size - 1)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onHabitChanged() {
        saveHabits()
    }

    override fun onEditHabit(habit: Habit, position: Int) {
        val input = EditText(requireContext())
        input.inputType = InputType.TYPE_CLASS_TEXT
        input.setText(habit.title)
        AlertDialog.Builder(requireContext())
            .setTitle("Edit Habit")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val title = input.text.toString().trim()
                if (title.isNotEmpty()) {
                    habit.title = title
                    habitsAdapter.notifyItemChanged(position)
                    saveHabits()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        loadHabits()
        habitsAdapter.notifyDataSetChanged()
        updateProgress(animated = false)
    }
}
