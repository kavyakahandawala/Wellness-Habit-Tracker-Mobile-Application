package com.wellness

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.wellness.model.Mood
import java.text.SimpleDateFormat
import java.util.*

class MoodAdapter(private val moods: List<Mood>) :
    RecyclerView.Adapter<MoodAdapter.MoodViewHolder>() {

    class MoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvEmoji: TextView = itemView.findViewById(R.id.tvEmoji)
        val tvMoodDesc: TextView = itemView.findViewById(R.id.tvMoodDesc)
        val tvMoodTime: TextView = itemView.findViewById(R.id.tvMoodTime)
        val card: MaterialCardView = itemView.findViewById(R.id.moodCard)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MoodViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_mood, parent, false)
        return MoodViewHolder(view)
    }

    override fun onBindViewHolder(holder: MoodViewHolder, position: Int) {
        val mood = moods[position]

        holder.tvEmoji.text = mood.emoji
        holder.tvMoodDesc.text = if (mood.description.isNotEmpty()) mood.description else "No description"

        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        holder.tvMoodTime.text = sdf.format(Date(mood.timestamp))

        val bgColor = when (mood.emoji) {
            "😄" -> Color.parseColor("#A8DCAB")
            "😊" -> Color.parseColor("#FFF59D")
            "😐" -> Color.parseColor("#B0BEC5")
            "😢" -> Color.parseColor("#90CAF9")
            "😡" -> Color.parseColor("#EF9A9A")
            "😭" -> Color.parseColor("#90CAF9")
            else -> Color.parseColor("#E0E0E0")
        }
        holder.card.setCardBackgroundColor(bgColor)
        holder.card.setContentPadding(24, 24, 24, 24)
        holder.card.radius = 20f
        holder.card.cardElevation = 8f

        holder.tvEmoji.setTextColor(Color.BLACK)
        holder.tvMoodDesc.setTextColor(Color.BLACK)
        holder.tvMoodTime.setTextColor(Color.parseColor("#424242"))
    }

    override fun getItemCount(): Int = moods.size
}
