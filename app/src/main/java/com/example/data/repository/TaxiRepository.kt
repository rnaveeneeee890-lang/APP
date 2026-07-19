package com.example.data.repository

import com.example.data.dao.TaxiDao
import com.example.data.model.SavedAddress
import com.example.data.model.Trip
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class TaxiRepository(private val taxiDao: TaxiDao) {
    val allTrips: Flow<List<Trip>> = taxiDao.getAllTrips()
    val savedAddresses: Flow<List<SavedAddress>> = taxiDao.getAllSavedAddresses()
    val userProfile: Flow<UserProfile?> = taxiDao.getUserProfileFlow()

    suspend fun insertTrip(trip: Trip): Long {
        return taxiDao.insertTrip(trip)
    }

    suspend fun updateTrip(trip: Trip) {
        taxiDao.updateTrip(trip)
    }

    suspend fun deleteTrip(id: Int) {
        taxiDao.deleteTripById(id)
    }

    suspend fun insertSavedAddress(address: SavedAddress): Long {
        return taxiDao.insertSavedAddress(address)
    }

    suspend fun deleteSavedAddress(id: Int) {
        taxiDao.deleteSavedAddressById(id)
    }

    suspend fun getUserProfileDirect(): UserProfile? {
        return taxiDao.getUserProfile()
    }

    suspend fun updateUserProfile(profile: UserProfile) {
        taxiDao.insertUserProfile(profile)
    }

    suspend fun prepopulateIfNeeded() {
        val currentProfile = taxiDao.getUserProfile()
        if (currentProfile == null) {
            // Seed default profile
            taxiDao.insertUserProfile(UserProfile())
        }

        // Check if saved addresses is empty and seed some defaults
        val addresses = taxiDao.getAllSavedAddresses().firstOrNull()
        if (addresses.isNullOrEmpty()) {
            taxiDao.insertSavedAddress(
                SavedAddress(
                    label = "Home",
                    address = "124 Quantum Avenue, Sector 7",
                    lat = 37.7749,
                    lng = -122.4194
                )
            )
            taxiDao.insertSavedAddress(
                SavedAddress(
                    label = "Work",
                    address = "Tech Nebula Plaza, Suite 404",
                    lat = 37.7891,
                    lng = -122.4014
                )
            )
            taxiDao.insertSavedAddress(
                SavedAddress(
                    label = "Gym",
                    address = "Hyperion Athletics, 88 Pulse St",
                    lat = 37.7654,
                    lng = -122.4432
                )
            )
        }
    }
}
