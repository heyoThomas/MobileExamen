package com.example.mobilemonteurpvbthomasanjema

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException

/**
 * Bewaart alle onverzonden meldingen. De lijst staat in het geheugen (snel) en wordt bij
 * elke wijziging naar een JSON-bestand in de app-map geschreven, zodat meldingen een
 * herstart of een "gekilde" app overleven.
 */
object MeldingRepository {

    private const val FILE_NAME = "meldingen.json"

    private val items = mutableListOf<Melding>()
    private var storageFile: File? = null

    /** Live, alleen-lezen weergave. Wijzigen gaat via add / saveChanges / removeByIds. */
    val meldingen: List<Melding> get() = items

    /** Wordt eenmalig aangeroepen vanuit MonteurApp.onCreate(). */
    fun init(context: Context) {
        if (storageFile != null) return
        storageFile = File(context.applicationContext.filesDir, FILE_NAME)
        load()
    }

    fun getMeldingById(id: Long): Melding? = items.find { it.id == id }

    fun getByIds(ids: Collection<Long>): List<Melding> = items.filter { it.id in ids }

    fun nextId(): Long = (items.maxOfOrNull { it.id } ?: 0L) + 1

    fun add(melding: Melding) {
        items.add(melding)
        save()
    }

    /** Aanroepen nadat je velden van een bestaande melding hebt aangepast. */
    fun saveChanges() = save()

    /**
     * Verwijdert de meldingen met deze id's, inclusief hun fotobestand.
     * Geeft het aantal verwijderde meldingen terug.
     */
    fun removeByIds(ids: Collection<Long>): Int {
        val teVerwijderen = items.filter { it.id in ids }
        teVerwijderen.forEach { melding ->
            val foto = File(melding.photoPath)
            if (foto.exists()) foto.delete()
        }
        items.removeAll(teVerwijderen)
        save()
        return teVerwijderen.size
    }

    private fun load() {
        val file = storageFile ?: return
        if (!file.exists()) return
        try {
            val array = JSONArray(file.readText())
            items.clear()
            for (i in 0 until array.length()) {
                val o = array.getJSONObject(i)
                items.add(
                    Melding(
                        id = o.getLong("id"),
                        photoPath = o.getString("photoPath"),
                        description = o.getString("description"),
                        engineerName = o.getString("engineerName"),
                        latitude = o.getDouble("latitude"),
                        longitude = o.getDouble("longitude"),
                        category = o.getString("category")
                    )
                )
            }
        } catch (e: Exception) {
            // Bestand kapot: bewaar een kopie zodat het volgende save() niets overschrijft.
            e.printStackTrace()
            try {
                file.copyTo(File(file.parentFile, "$FILE_NAME.corrupt"), overwrite = true)
            } catch (_: IOException) {
            }
            items.clear()
        }
    }

    private fun save() {
        val file = storageFile ?: return
        val array = JSONArray()
        items.forEach { m ->
            array.put(
                JSONObject().apply {
                    put("id", m.id)
                    put("photoPath", m.photoPath)
                    put("description", m.description)
                    put("engineerName", m.engineerName)
                    put("latitude", m.latitude)
                    put("longitude", m.longitude)
                    put("category", m.category)
                }
            )
        }
        try {
            // Eerst naar een tijdelijk bestand schrijven, zodat een crash halverwege
            // het echte bestand niet half-leeg achterlaat.
            val tmp = File(file.parentFile, "$FILE_NAME.tmp")
            tmp.writeText(array.toString())
            if (!tmp.renameTo(file)) {
                file.writeText(array.toString())
                tmp.delete()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
