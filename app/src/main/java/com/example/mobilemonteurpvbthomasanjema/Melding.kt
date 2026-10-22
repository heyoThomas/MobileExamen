package com.example.mobilemonteurpvbthomasanjema

data class Melding(
    val id: Long,
    val photoPath: String,
    var description: String,
    var engineerName: String,
    var latitude: Double,
    var longitude: Double,
    var category: String
)
