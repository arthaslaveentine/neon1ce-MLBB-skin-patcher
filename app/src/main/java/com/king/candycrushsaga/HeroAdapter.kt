package com.king.candycrushsaga

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HeroAdapter(
    private val heroes: List<Hero>,
    private val packCounts: Map<Int, Int>,
    private val onClick: (Hero) -> Unit,
) : RecyclerView.Adapter<HeroAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(android.R.id.text1)
        val sub: TextView = v.findViewById(android.R.id.text2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val h = heroes[position]
        holder.name.text = h.name
        val count = packCounts[h.index] ?: 0
        holder.sub.text = "base skin ${h.baseSkin} · $count pack(s)"
        holder.itemView.setOnClickListener { onClick(h) }
    }

    override fun getItemCount() = heroes.size
}
