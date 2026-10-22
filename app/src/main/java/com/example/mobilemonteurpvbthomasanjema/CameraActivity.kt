package com.example.mobilemonteurpvbthomasanjema

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.widget.doAfterTextChanged
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.textfield.TextInputLayout
import java.io.File

class CameraActivity : AppCompatActivity() {

    companion object {
        private const val STATE_PHOTO_PATH = "photo_path"
        private const val STATE_PENDING_PATH = "pending_path"
    }

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var textLocation: TextView
    private var currentLatitude: Double = 0.0
    private var currentLongitude: Double = 0.0

    // Foto die klaar is voor de melding
    private var photoPath: String? = null

    // Bestand waar de camera-app op dit moment naartoe schrijft
    private var pendingPath: String? = null

    // Wordt true na opslaan; anders ruimen we de foto op bij sluiten (zie onDestroy)
    private var saved = false

    private val takePicture =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            pendingPath?.let { pending ->
                val file = File(pending)
                if (success && file.length() > 0) {
                    ImageUtils.shrinkPhoto(pending)
                    // Bij "Andere foto maken": de vorige foto weggooien
                    photoPath?.let { File(it).delete() }
                    photoPath = pending
                    showPhoto()
                } else {
                    file.delete()
                    // Camera geannuleerd en er is nog geen foto: terug naar het startscherm
                    if (photoPath == null) finish()
                }
            }
            pendingPath = null
        }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
            if (grants[Manifest.permission.ACCESS_FINE_LOCATION] == true) {
                getLastLocation()
            } else if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
                Toast.makeText(this, "Locatie-permissie geweigerd", Toast.LENGTH_SHORT).show()
            }

            if (hasPermission(Manifest.permission.CAMERA)) {
                launchCamera()
            } else {
                Toast.makeText(
                    this,
                    "Zonder camera-permissie kun je geen melding maken",
                    Toast.LENGTH_LONG
                ).show()
                finish()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableAppEdgeToEdge()
        setContentView(R.layout.activity_camera)
        applySystemInsets(findViewById(R.id.main), findViewById(R.id.layoutHeader))
        setupToolbar("Nieuwe melding")

        textLocation = findViewById(R.id.textLocation)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        photoPath = savedInstanceState?.getString(STATE_PHOTO_PATH)
        pendingPath = savedInstanceState?.getString(STATE_PENDING_PATH)

        findViewById<Button>(R.id.buttonTerug).setOnClickListener { finish() }
        findViewById<Button>(R.id.buttonRetake).setOnClickListener { launchCamera() }
        findViewById<Button>(R.id.buttonOpslaan).setOnClickListener { saveMelding() }

        // Foutmelding weghalen zodra de gebruiker iets typt
        wisFoutBijTypen(R.id.editTextBeschrijving, R.id.tilBeschrijving)
        wisFoutBijTypen(R.id.editTextCategorie, R.id.tilCategorie)
        wisFoutBijTypen(R.id.editTextMonteur, R.id.tilMonteur)

        if (hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) getLastLocation()

        if (photoPath != null) showPhoto() else showForm(false)

        // Alleen bij de eerste keer starten de camera openen, niet bij schermrotatie
        if (savedInstanceState == null) startFlow()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_PHOTO_PATH, photoPath)
        outState.putString(STATE_PENDING_PATH, pendingPath)
    }

    override fun onDestroy() {
        // Melding niet opgeslagen en scherm echt gesloten: foto niet laten rondslingeren
        if (isFinishing && !saved) {
            photoPath?.let { File(it).delete() }
            pendingPath?.let { File(it).delete() }
        }
        super.onDestroy()
    }

    private fun hasPermission(permission: String): Boolean =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    /** Vraagt ontbrekende permissies in een keer aan en opent daarna de camera. */
    private fun startFlow() {
        val ontbrekend = listOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
            .filter { !hasPermission(it) }
        if (ontbrekend.isEmpty()) {
            launchCamera()
        } else {
            permissionLauncher.launch(ontbrekend.toTypedArray())
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) return
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                textLocation.text = formatLocation(currentLatitude, currentLongitude)
            } else {
                textLocation.text = "Locatie onbekend"
            }
        }
    }

    private fun launchCamera() {
        val map = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        val file = File(map, "foto_${System.currentTimeMillis()}.jpg")
        pendingPath = file.absolutePath
        val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
        try {
            takePicture.launch(uri)
        } catch (e: ActivityNotFoundException) {
            pendingPath = null
            Toast.makeText(this, "Geen camera-app gevonden", Toast.LENGTH_LONG).show()
            if (photoPath == null) finish()
        }
    }

    private fun showPhoto() {
        photoPath?.let { findViewById<ImageView>(R.id.imageViewPreview).showPhoto(it, 1280) }
        showForm(true)
    }

    private fun showForm(photoAvailable: Boolean) {
        val zichtbaar = if (photoAvailable) View.VISIBLE else View.GONE
        findViewById<View>(R.id.layoutInputFields).visibility = zichtbaar
        findViewById<View>(R.id.buttonRetake).visibility = zichtbaar
        findViewById<Button>(R.id.buttonOpslaan).isEnabled = photoAvailable
    }

    private fun wisFoutBijTypen(editId: Int, layoutId: Int) {
        findViewById<EditText>(editId).doAfterTextChanged {
            findViewById<TextInputLayout>(layoutId).error = null
        }
    }

    private fun saveMelding() {
        val beschrijving = findViewById<EditText>(R.id.editTextBeschrijving).text.toString().trim()
        val categorie = findViewById<EditText>(R.id.editTextCategorie).text.toString().trim()
        val monteur = findViewById<EditText>(R.id.editTextMonteur).text.toString().trim()

        findViewById<TextInputLayout>(R.id.tilBeschrijving).error =
            if (beschrijving.isEmpty()) "Vul een beschrijving in" else null
        findViewById<TextInputLayout>(R.id.tilCategorie).error =
            if (categorie.isEmpty()) "Vul een categorie in" else null
        findViewById<TextInputLayout>(R.id.tilMonteur).error =
            if (monteur.isEmpty()) "Vul je naam in" else null

        if (beschrijving.isEmpty() || categorie.isEmpty() || monteur.isEmpty()) return

        val path = photoPath
        if (path == null) {
            Toast.makeText(this, "Geen foto beschikbaar", Toast.LENGTH_SHORT).show()
            return
        }

        MeldingRepository.add(
            Melding(
                id = MeldingRepository.nextId(),
                photoPath = path,
                description = beschrijving,
                engineerName = monteur,
                latitude = currentLatitude,
                longitude = currentLongitude,
                category = categorie
            )
        )
        saved = true

        Toast.makeText(this, "Melding opgeslagen", Toast.LENGTH_SHORT).show()
        finish()
    }
}
