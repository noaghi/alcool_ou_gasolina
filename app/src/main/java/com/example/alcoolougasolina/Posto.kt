package com.example.alcoolougasolina

data class Posto (
    val nome: String,
    val gasolina: String,
    val alcool: String,
    val usa75: Boolean,
    val dataIns: String,
    val lat: Double,
    val lon: Double
)