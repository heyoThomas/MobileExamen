package com.example.mobilemonteurpvbthomasanjema

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class VerslagAdapter(
    private val meldingen: List<Melding>,
    private val onItemClick: (Melding) -> Unit
) : RecyclerView.Adapter<VerslagAdapter.VerslagViewHolder>() {

    class VerslagViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textDescription: TextView = view.findViewById(R.id.textDescription)
        val textEngineerName: TextView = view.findViewById(R.id.textEngineerName)
        val textCategory: TextView = view.findViewById(R.id.textCategory)
        val textLocation: TextView = view.findViewById(R.id.textLocation)
        val imagePhoto: ImageView = view.findViewById(R.id.imagePhoto)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VerslagViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_verslag_page, parent, false)
        return VerslagViewHolder(view)
    }

    override fun onBindViewHolder(holder: VerslagViewHolder, position: Int) {
        val melding = meldingen[position]

        holder.textDescription.text = melding.description
        holder.textEngineerName.text = melding.engineerName
        holder.textCategory.text = melding.category
        holder.textLocation.text = formatLocation(melding.latitude, melding.longitude)
        holder.imagePhoto.showPhoto(melding.photoPath, 320)

        holder.itemView.setOnClickListener { onItemClick(melding) }
    }

    override fun getItemCount(): Int = meldingen.size
}
