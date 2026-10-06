package com.king.candycrushsaga

import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class SlotRow(
    val slot: Int,
    val pack: SkinPack?,
    val isActive: Boolean,
    val skinName: String?,
)

class SkinAdapter(
    private val rows: List<SlotRow>,
    private val onInject: (SkinPack) -> Unit,
    private val onRemove: (SkinPack) -> Unit,
) : RecyclerView.Adapter<SkinAdapter.VH>() {

    class VH(v: LinearLayout) : RecyclerView.ViewHolder(v) {
        val title: TextView = v.getChildAt(0) as TextView
        val name: TextView = v.getChildAt(1) as TextView
        val row: LinearLayout = v.getChildAt(2) as LinearLayout
        val status: TextView = row.getChildAt(0) as TextView
        val btn: Button = row.getChildAt(1) as Button
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val ctx = parent.context
        val cell = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(28, 22, 28, 22)
            layoutParams = RecyclerView.LayoutParams(
                RecyclerView.LayoutParams.MATCH_PARENT,
                RecyclerView.LayoutParams.WRAP_CONTENT,
            )
        }
        val title = TextView(ctx).apply {
            setTextColor(Color.parseColor("#E8E8F0"))
            textSize = 15f
            typeface = Typeface.DEFAULT_BOLD
        }
        val name = TextView(ctx).apply {
            setTextColor(Color.parseColor("#B0B0C0"))
            textSize = 13f
            setPadding(0, 4, 0, 6)
        }
        val row = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }
        val status = TextView(ctx).apply {
            setTextColor(Color.parseColor("#8A8AA0"))
            textSize = 12f
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }
        val btn = Button(ctx).apply {
            textSize = 11f
            minWidth = 0
            minimumWidth = 0
            setPadding(24, 0, 24, 0)
        }
        row.addView(status)
        row.addView(btn)
        cell.addView(title)
        cell.addView(name)
        cell.addView(row)
        return VH(cell)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val r = rows[position]
        holder.title.text = if (r.slot == 0) "Skin 00 — base" else "Skin %02d".format(r.slot)
        holder.name.text = r.skinName ?: if (r.pack == null) "—" else "—"

        if (r.pack == null || !r.pack.hasFiles) {
            holder.status.text = "not installed"
            holder.status.setTextColor(0xFF5A5A70.toInt())
            holder.btn.visibility = Button.GONE
        } else {
            if (r.isActive) {
                holder.status.text = "ACTIVE · ${r.pack.version}"
                holder.status.setTextColor(0xFF00E5FF.toInt())
                holder.btn.text = "REMOVE"
                holder.btn.visibility = Button.VISIBLE
                holder.btn.setOnClickListener { onRemove(r.pack) }
            } else {
                holder.status.text = "ready · ${r.pack.version}"
                holder.status.setTextColor(0xFFFF2E88.toInt())
                holder.btn.text = "INJECT"
                holder.btn.visibility = Button.VISIBLE
                holder.btn.setOnClickListener { onInject(r.pack) }
            }
        }
    }

    override fun getItemCount() = rows.size
}
