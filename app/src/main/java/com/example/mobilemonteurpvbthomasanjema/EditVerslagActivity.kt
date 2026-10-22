package com.example.mobilemonteurpvbthomasanjema

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/** Detailscherm van een melding, met knoppen om te bewerken of te verwijderen. */
class EditVerslagActivity : AppCompatActivity() {

    private var meldingId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableAppEdgeToEdge()
        setContentView(R.layout.activity_edit_verslag)
        applySystemInsets(findViewById(R.id.main), findViewById(R.id.layoutHeader))
        setupToolbar("Melding")

        meldingId = intent.getLongExtra("MELDING_ID", -1)
        if (meldingId == -1L) {
            finish()
            return
        }

        findViewById<View>(R.id.buttonEdit).setOnClickListener {
            val intent = Intent(this, ItemMeldingActivity::class.java)
            intent.putExtra("MELDING_ID", meldingId)
            startActivity(intent)
        }
        findViewById<View>(R.id.buttonDelete).setOnClickListener { bevestigVerwijderen() }
    }

    override fun onResume() {
        super.onResume()
        toonMelding()
    }

    private fun toonMelding() {
        val melding = MeldingRepository.getMeldingById(meldingId)
        if (melding == null) {
            // Bestaat niet (meer), bijvoorbeeld net verwijderd
            finish()
            return
        }

        findViewById<TextView>(R.id.textDescription).text = melding.description
        findViewById<TextView>(R.id.textEngineerName).text = melding.engineerName
        findViewById<TextView>(R.id.textCategory).text = melding.category
        findViewById<TextView>(R.id.textLocation).text =
            formatLocation(melding.latitude, melding.longitude)
        findViewById<ImageView>(R.id.imagePhoto).showPhoto(melding.photoPath, 1280)
    }

    private fun bevestigVerwijderen() {
        MaterialAlertDialogBuilder(this)
            .setTitle("Melding verwijderen?")
            .setMessage("De melding en de foto worden van dit toestel verwijderd. Dit kan niet ongedaan worden gemaakt.")
            .setNegativeButton("Annuleren", null)
            .setPositiveButton("Verwijderen") { _, _ ->
                MeldingRepository.removeByIds(listOf(meldingId))
                Toast.makeText(this, "Melding verwijderd", Toast.LENGTH_SHORT).show()
                finish()
            }
            .show()
    }
}
