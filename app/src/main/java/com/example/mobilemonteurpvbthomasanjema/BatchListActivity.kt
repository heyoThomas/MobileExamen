package com.example.mobilemonteurpvbthomasanjema

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File

class BatchListActivity : AppCompatActivity() {

    companion object {
        private const val MAIL_ONTVANGER = "thomas1anjema@gmail.com"
        private const val STATE_SELECTED = "selected_ids"
        private const val STATE_PENDING = "pending_ids"
    }

    // Vinkjes van de gebruiker
    private val selectedIds = mutableSetOf<Long>()

    // Meldingen die naar de mailapp zijn gestuurd, maar waarvan we nog niet weten
    // of de mail echt is verzonden. Pas na bevestiging van de gebruiker worden ze gewist.
    private var pendingIds: List<Long> = emptyList()

    private var confirmDialog: AlertDialog? = null

    private lateinit var adapter: SelectableMeldingAdapter
    private lateinit var checkSelectAll: CheckBox
    private lateinit var textSelectedCount: TextView
    private lateinit var textEmpty: TextView
    private lateinit var buttonSend: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableAppEdgeToEdge()
        setContentView(R.layout.activity_batch_list)
        applySystemInsets(findViewById(R.id.main), findViewById(R.id.layoutHeader))
        setupToolbar("Batch versturen")

        // Herstel selectie en "wacht op bevestiging" na bijv. schermrotatie
        savedInstanceState?.let { state ->
            state.getLongArray(STATE_SELECTED)?.let { selectedIds.addAll(it.toList()) }
            state.getLongArray(STATE_PENDING)?.let { pendingIds = it.toList() }
        }

        checkSelectAll = findViewById(R.id.checkSelectAll)
        textSelectedCount = findViewById(R.id.textSelectedCount)
        textEmpty = findViewById(R.id.textEmpty)
        buttonSend = findViewById(R.id.buttonBatchAccept)

        val recycler = findViewById<RecyclerView>(R.id.recyclerBatch)
        recycler.layoutManager = LinearLayoutManager(this)
        adapter = SelectableMeldingAdapter(MeldingRepository.meldingen, selectedIds) { updateUi() }
        recycler.adapter = adapter

        checkSelectAll.setOnClickListener {
            if (checkSelectAll.isChecked) adapter.selectAll() else adapter.clearSelection()
        }

        buttonSend.setOnClickListener { verzendGeselecteerdeMeldingen() }

        updateUi()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLongArray(STATE_SELECTED, selectedIds.toLongArray())
        outState.putLongArray(STATE_PENDING, pendingIds.toLongArray())
    }

    override fun onResume() {
        super.onResume()

        // Lijst kan veranderd zijn, haal verdwenen meldingen uit de selectie
        selectedIds.retainAll(MeldingRepository.meldingen.map { it.id }.toSet())
        adapter.notifyDataSetChanged()
        updateUi()

        // Terug uit de mailapp: vraag of de mail echt is verstuurd
        if (pendingIds.isNotEmpty() && confirmDialog?.isShowing != true) {
            toonBevestiging()
        }
    }

    override fun onDestroy() {
        confirmDialog?.dismiss()
        super.onDestroy()
    }

    private fun updateUi() {
        val totaal = MeldingRepository.meldingen.size
        val geselecteerd = selectedIds.size

        textSelectedCount.text = "$geselecteerd van $totaal geselecteerd"
        checkSelectAll.isChecked = totaal > 0 && geselecteerd == totaal
        checkSelectAll.isEnabled = totaal > 0
        buttonSend.isEnabled = geselecteerd > 0
        buttonSend.text = if (geselecteerd > 0) "Verstuur ($geselecteerd)" else "Verstuur"
        textEmpty.visibility = if (totaal == 0) View.VISIBLE else View.GONE
    }

    private fun verzendGeselecteerdeMeldingen() {
        val geselecteerd = MeldingRepository.getByIds(selectedIds)

        if (geselecteerd.isEmpty()) {
            Toast.makeText(this, "Selecteer eerst minstens één melding.", Toast.LENGTH_SHORT).show()
            return
        }

        val mailBody = buildString {
            geselecteerd.forEachIndexed { index, melding ->
                append("Melding ${index + 1}:\n")
                append("ID: ${melding.id}\n")
                append("Omschrijving: ${melding.description}\n")
                append("Categorie: ${melding.category}\n")
                append("Locatie: ${melding.latitude}, ${melding.longitude}\n")
                append("Monteur: ${melding.engineerName}\n\n")
            }
        }

        val fotoUris = ArrayList<Uri>()
        geselecteerd.forEach { melding ->
            val file = File(melding.photoPath)
            if (file.exists()) {
                try {
                    fotoUris.add(
                        FileProvider.getUriForFile(this, "${packageName}.provider", file)
                    )
                } catch (e: IllegalArgumentException) {
                    e.printStackTrace()
                }
            }
        }

        val emailIntent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
            type = "message/rfc822"
            putExtra(Intent.EXTRA_EMAIL, arrayOf(MAIL_ONTVANGER))
            putExtra(Intent.EXTRA_SUBJECT, "Batch meldingen")
            putExtra(Intent.EXTRA_TEXT, mailBody)
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, fotoUris)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        try {
            startActivity(Intent.createChooser(emailIntent, "Verzend meldingen via..."))
            // Pas hier onthouden: als er geen mailapp is, is er niets om op te wachten.
            pendingIds = geselecteerd.map { it.id }
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(
                this,
                "Geen e-mailapp gevonden. Je meldingen zijn bewaard.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun toonBevestiging() {
        val aantal = pendingIds.size
        confirmDialog = MaterialAlertDialogBuilder(this)
            .setTitle("E-mail verstuurd?")
            .setMessage(
                "Is de e-mail met $aantal melding(en) echt verstuurd?\n\n" +
                    "Alleen als je 'Ja' kiest worden deze meldingen en foto's van het toestel verwijderd."
            )
            .setCancelable(false)
            .setPositiveButton("Ja, verstuurd") { _, _ -> verwijderVerzondenMeldingen() }
            .setNegativeButton("Nee, bewaar ze") { _, _ ->
                pendingIds = emptyList()
                Toast.makeText(this, "Meldingen blijven bewaard.", Toast.LENGTH_LONG).show()
            }
            .show()
    }

    private fun verwijderVerzondenMeldingen() {
        val aantal = MeldingRepository.removeByIds(pendingIds)
        pendingIds = emptyList()
        selectedIds.clear()
        Toast.makeText(
            this,
            "$aantal melding(en) en foto's verwijderd van het toestel.",
            Toast.LENGTH_LONG
        ).show()

        // Volgens de activity flow: na verzenden terug naar het startscherm
        val home = Intent(this, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
        startActivity(home)
        finish()
    }
}
