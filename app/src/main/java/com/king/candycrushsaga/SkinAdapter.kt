package com.king.candycrushsaga

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SkinAdapter(
    private val packs: List<SkinPack>,
    private val isActive: (SkinPack) -> Boolean,
    private val onApply: (SkinPack) -> Unit,
    private val onRevert: (SkinPack) -> Unit,
) : RecyclerView.Adapter<SkinAdapter.VH>() {

    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val name: TextView = v.findViewById(android.R.id.text1)
        val ver: TextView = v.findViewById(android.R.id.text2)
        val btn: Button = v.findViewById(android.R.id.button1)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = packs[position]
        val active = isActive(p)
        holder.name.text = "Skin ${p.skinId}"
        holder.ver.text = "${p.version} · ${if (active) "ACTIVE" else "idle"}"
        holder.btn.text = if (active) "Revert" else "Apply"
        holder.btn.setOnClickListener { if (active) onRevert(p) else onApply(p) }
    }

    override fun getItemCount() = packs.size
}
