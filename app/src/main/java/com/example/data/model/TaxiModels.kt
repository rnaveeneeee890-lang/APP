package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "trips")
data class Trip(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val pickupAddress: String,
    val dropoffAddress: String,
    val timestamp: Long = System.currentTimeMillis(),
    val fare: Double,
    val distance: Double, // in km
    val duration: Int, // in minutes
    val tierName: String, // Eco, Premium, Moto, XL
    val driverName: String,
    val driverVehicle: String,
    val driverRating: Float,
    val status: String, // "COMPLETED", "CANCELLED"
    val userRating: Int = 0, // 0 means unrated
    val userFeedback: String = ""
) : Serializable

@Entity(tableName = "saved_addresses")
data class SavedAddress(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val label: String, // Home, Work, Gym, Partner, etc.
    val address: String,
    val lat: Double,
    val lng: Double
) : Serializable

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex Rivera",
    val email: String = "alex.rivera@futuremail.com",
    val walletBalance: Double = 50.00,
    val passengerRating: Float = 4.92f
) : Serializable
