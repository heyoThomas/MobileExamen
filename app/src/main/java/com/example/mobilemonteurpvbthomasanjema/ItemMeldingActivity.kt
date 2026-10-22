package com.example.mobilemonteurpvbthomasanjema

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.google.android.material.textfield.TextInputLayout

/** Formulier om een bestaande melding aan te passen. */
class ItemMeldingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableAppEdgeToEdge()
        setContentView(R.layout.item_melding)
        applySystemInsets(findViewById(R.id.main), findViewById(R.id.layoutHeader))
        setupToolbar("Melding bewerken")

        val meldingId = intent.getLongExtra("MELDING_ID", -1L)
        if (meldingId == -1L) {
            finish()
            return
        }

        val melding = MeldingRepository.getMeldingById(meldingId)
        if (melding == null) {
            Toast.makeText(this, "Melding niet gevonden", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        val editDescription = findViewById<EditText>(R.id.editDescription)
        val editCategory = findViewById<EditText>(R.id.editCategory)
        val editEngineerName = findViewById<EditText>(R.id.editEngineerName)
        val editLatitude = findViewById<EditText>(R.id.editLatitude)
        val editLongitude = findViewById<EditText>(R.id.editLongitude)

        val tilDescription = findViewById<TextInputLayout>(R.id.tilDescription)
        val tilCategory = findViewById<TextInputLayout>(R.id.tilCategory)
        val tilEngineerName = findViewById<TextInputLayout>(R.id.tilEngineerName)
        val tilLatitude = findViewById<TextInputLayout>(R.id.tilLatitude)
        val tilLongitude = findViewById<TextInputLayout>(R.id.tilLongitude)

        // Alleen invullen bij de eerste keer, niet opnieuw na schermrotatie (dan staat
        // de ingetypte tekst al in de velden)
        if (savedInstanceState == null) {
            editDescription.setText(melding.description)
            editCategory.setText(melding.category)
            editEngineerName.setText(melding.engineerName)
            editLatitude.setText(melding.latitude.toString())
            editLongitude.setText(melding.longitude.toString())
        }

        editDescription.doAfterTextChanged { tilDescription.error = null }
        editCategory.doAfterTextChanged { tilCategory.error = null }
        editEngineerName.doAfterTextChanged { tilEngineerName.error = null }
        editLatitude.doAfterTextChanged { tilLatitude.error = null }
        editLongitude.doAfterTextChanged { tilLongitude.error = null }

        findViewById<Button>(R.id.buttonSave).setOnClickListener {
            val description = editDescription.text.toString().trim()
            val category = editCategory.text.toString().trim()
            val engineerName = editEngineerName.text.toString().trim()
            val latitude = editLatitude.text.toString().toDoubleOrNull()
            val longitude = editLongitude.text.toString().toDoubleOrNull()

            tilDescription.error = if (description.isEmpty()) "Vul een beschrijving in" else null
            tilCategory.error = if (category.isEmpty()) "Vul een categorie in" else null
            tilEngineerName.error = if (engineerName.isEmpty()) "Vul een naam in" else null
            tilLatitude.error = if (latitude == null) "Geen geldig getal" else null
            tilLongitude.error = if (longitude == null) "Geen geldig getal" else null

            if (description.isEmpty() || category.isEmpty() || engineerName.isEmpty() ||
                latitude == null || longitude == null
            ) {
                return@setOnClickListener
            }

            melding.description = description
            melding.category = category
            melding.engineerName = engineerName
            melding.latitude = latitude
            melding.longitude = longitude
            MeldingRepository.saveChanges()

            Toast.makeText(this, "Melding opgeslagen", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, VerslagViewActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }
}
