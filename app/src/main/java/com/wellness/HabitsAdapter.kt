package com.wellness

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.wellness.model.Habit

class HabitsAdapter(
    private val habits: MutableList<Habit>,
    private val listener: HabitListener
) : RecyclerView.Adapter<HabitsAdapter.HabitViewHolder>() {

    interface HabitListener {
        fun onHabitChanged()
        fun onEditHabit(habit: Habit, position: Int)
    }

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkBox: CheckBox = itemView.findViewById(R.id.checkBox)
        val title: TextView = itemView.findViewById(R.id.tvHabitTitle)
        val editBtn: ImageButton = itemView.findViewById(R.id.btnEdit)
        val deleteBtn: ImageButton = itemView.findViewById(R.id.btnDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun getItemCount() = habits.size

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val habit = habits[position]
        holder.title.text = habit.title
        holder.checkBox.isChecked = habit.completed

        // Checkbox toggle
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            habit.completed = isChecked
            listener.onHabitChanged()
        }

        // Edit button
        holder.editBtn.setOnClickListener {
            listener.onEditHabit(habit, position)
        }

        // Delete button
        holder.deleteBtn.setOnClickListener {
            habits.removeAt(position)
            notifyItemRemoved(position)
            listener.onHabitChanged()
        }
    }
}
