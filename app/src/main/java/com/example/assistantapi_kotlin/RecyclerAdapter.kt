package com.example.assistantapi_kotlin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class RecyclerItem(val assistantName: String, val assistantId: String, val assistantInstruction: String, val isEqual: Boolean = false)

class RecyclerAdapter(private val items: List<RecyclerItem>): RecyclerView.Adapter<RecyclerAdapter.RecyclerViewHolder>() {
    class RecyclerViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val assistantNameTextView: TextView = itemView.findViewById(R.id.assistant_name_card_textview)

        val assistantIdTextView: TextView = itemView.findViewById(R.id.assistant_id_card_textview)

        val assistantInstructionTextView: TextView = itemView.findViewById(R.id.assistant_instruction_card_textview)

        val assistantIsEqualTextView: TextView = itemView.findViewById(R.id.assistant_isEqual_textview)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.assistant_list_recycler_item, parent, false)
        return RecyclerViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerViewHolder, position: Int) {
        holder.assistantNameTextView.text = items[position].assistantName
        holder.assistantIdTextView.text = items[position].assistantId
        holder.assistantInstructionTextView.text = items[position].assistantInstruction
        if(items[position].isEqual) {
            holder.assistantIsEqualTextView.text = "現在のアシスタント"
        }
    }

    override fun getItemCount(): Int {
        return items.size
    }
}