package com.example.mobilemonteurpvbthomasanjema

import android.graphics.Color
import android.view.View
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import java.util.Locale

/**
 * Teken achter de systeembalken (edge-to-edge). De statusbalk heeft een donkere navy
 * achtergrond, dus daar horen lichte iconen bij. De navigatiebalk is licht.
 * Aanroepen VOOR setContentView().
 */
fun AppCompatActivity.enableAppEdgeToEdge() {
    enableEdgeToEdge(
        statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
    )
}

/**
 * Houdt inhoud uit de systeembalken (en boven het toetsenbord):
 * - de root krijgt padding links, rechts en onder
 * - de header krijgt de hoogte van de statusbalk erbij, zodat de navy kleur er doorheen loopt
 */
fun applySystemInsets(root: View, header: View?) {
    val headerLeft = header?.paddingLeft ?: 0
    val headerTop = header?.paddingTop ?: 0
    val headerRight = header?.paddingRight ?: 0
    val headerBottom = header?.paddingBottom ?: 0

    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val bars = insets.getInsets(
            WindowInsetsCompat.Type.systemBars() or
                WindowInsetsCompat.Type.displayCutout() or
                WindowInsetsCompat.Type.ime()
        )
        view.setPadding(bars.left, 0, bars.right, bars.bottom)
        header?.setPadding(headerLeft, headerTop + bars.top, headerRight, headerBottom)
        insets
    }
}

/** Zet de titel en de terugpijl van de gedeelde toolbar (include_toolbar.xml). */
fun AppCompatActivity.setupToolbar(title: String) {
    val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
    toolbar.title = title
    toolbar.setNavigationOnClickListener { onBackPressedDispatcher.onBackPressed() }
}

/** Co-ordinaten netjes tonen, bijv. "52.96010, 5.79220". */
fun formatLocation(latitude: Double, longitude: Double): String =
    String.format(Locale.US, "%.5f, %.5f", latitude, longitude)
