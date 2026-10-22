package com.example.mobilemonteurpvbthomasanjema

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class VerslagViewActivity : AppCompatActivity() {

    private lateinit var adapter: VerslagAdapter
    private lateinit var textEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableAppEdgeToEdge()
        setContentView(R.layout.activity_batch_view)
        applySystemInsets(findViewById(R.id.main), findViewById(R.id.layoutHeader))
        setupToolbar("Onverzonden meldingen")

        textEmpty = findViewById(R.id.textEmpty)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerVerslagen)
        recyclerView.layoutManager = LinearLayoutManager(this)

        adapter = VerslagAdapter(MeldingRepository.meldingen) { melding ->
            val intent = Intent(this, EditVerslagActivity::class.java)
            intent.putExtra("MELDING_ID", melding.id)
            startActivity(intent)
        }
        recyclerView.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        // Na verwijderen of bewerken in het detailscherm moet de lijst bijgewerkt worden
        adapter.notifyDataSetChanged()
        textEmpty.visibility = if (MeldingRepository.meldingen.isEmpty()) View.VISIBLE else View.GONE
    }
}
