package com.example.lingobuddypck.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.lingobuddypck.R

class TopicSuggestionAdapter(
    private val topics: List<String>,
    private val onTopicClick: (String) -> Unit
) : RecyclerView.Adapter<TopicSuggestionAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val button: Button = itemView.findViewById(R.id.topicChipButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_topic_chip, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val topic = topics[position]
        holder.button.text = topic
        holder.button.setOnClickListener { onTopicClick(topic) }
    }

    override fun getItemCount(): Int = topics.size
}


