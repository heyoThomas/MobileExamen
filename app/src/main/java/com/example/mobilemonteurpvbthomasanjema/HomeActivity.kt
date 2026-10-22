package com.example.mobilemonteurpvbthomasanjema

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var textBatchCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableAppEdgeToEdge()
        setContentView(R.layout.activity_home)
        applySystemInsets(findViewById(R.id.main), findViewById(R.id.layoutHeader))

        textBatchCount = findViewById(R.id.textBatchCount)

        findViewById<View>(R.id.buttonMeldingMaken).setOnClickListener {
            startActivity(Intent(this, CameraActivity::class.java))
        }
        findViewById<View>(R.id.buttonBatchBekijken).setOnClickListener {
            startActivity(Intent(this, VerslagViewActivity::class.java))
        }
        findViewById<View>(R.id.buttonBatchVersturen).setOnClickListener {
            startActivity(Intent(this, BatchListActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        textBatchCount.text = when (val aantal = MeldingRepository.meldingen.size) {
            0 -> "Geen onverzonden meldingen"
            1 -> "1 onverzonden melding"
            else -> "$aantal onverzonden meldingen"
        }
    }
}
