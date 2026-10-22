package com.example.mobilemonteurpvbthomasanjema

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView

/** Lijst met meldingen en een vinkje per rij, om te kiezen wat er verstuurd wordt. */
class SelectableMeldingAdapter(
    private val meldingen: List<Melding>,
    private val selectedIds: MutableSet<Long>,
    private val onSelectionChanged: () -> Unit
) : RecyclerView.Adapter<SelectableMeldingAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: MaterialCardView = view.findViewById(R.id.cardRoot)
        val checkBox: CheckBox = view.findViewById(R.id.checkSelect)
        val imageThumb: ImageView = view.findViewById(R.id.imageThumb)
        val textDescription: TextView = view.findViewById(R.id.textItemDescription)
        val textDetails: TextView = view.findViewById(R.id.textItemDetails)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_melding_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val melding = meldingen[position]

        holder.textDescription.text = melding.description
        holder.textDetails.text = "${melding.category} | ${melding.engineerName}"
        holder.imageThumb.showPhoto(melding.photoPath, 240)

        // Listener eerst weghalen, anders vuurt isChecked = ... de oude listener af
        // (RecyclerView hergebruikt rijen).
        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = melding.id in selectedIds
        styleCard(holder.card, holder.checkBox.isChecked)
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) selectedIds.add(melding.id) else selectedIds.remove(melding.id)
            styleCard(holder.card, isChecked)
            onSelectionChanged()
        }

        // Tik op de hele rij werkt ook
        holder.itemView.setOnClickListener { holder.checkBox.toggle() }
    }

    override fun getItemCount(): Int = meldingen.size

    fun selectAll() {
        selectedIds.addAll(meldingen.map { it.id })
        notifyDataSetChanged()
        onSelectionChanged()
    }

    fun clearSelection() {
        selectedIds.clear()
        notifyDataSetChanged()
        onSelectionChanged()
    }

    /** Geselecteerde rij: navy rand en lichtblauwe achtergrond. */
    private fun styleCard(card: MaterialCardView, selected: Boolean) {
        val context = card.context
        val density = context.resources.displayMetrics.density
        card.setCardBackgroundColor(
            ContextCompat.getColor(context, if (selected) R.color.navy_container else R.color.white)
        )
        card.strokeColor = ContextCompat.getColor(context, if (selected) R.color.navy else R.color.divider)
        card.strokeWidth = ((if (selected) 2 else 1) * density).toInt()
    }
}
