package com.example.data.dao

import androidx.room.*
import com.example.data.model.SavedAddress
import com.example.data.model.Trip
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface TaxiDao {
    // Trip queries
    @Query("SELECT * FROM trips ORDER BY timestamp DESC")
    fun getAllTrips(): Flow<List<Trip>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: Trip): Long

    @Update
    suspend fun updateTrip(trip: Trip)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTripById(id: Int)

    // Saved Addresses queries
    @Query("SELECT * FROM saved_addresses ORDER BY label ASC")
    fun getAllSavedAddresses(): Flow<List<SavedAddress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedAddress(address: SavedAddress): Long

    @Query("DELETE FROM saved_addresses WHERE id = :id")
    suspend fun deleteSavedAddressById(id: Int)

    // User Profile queries
    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfileFlow(): Flow<UserProfile?>

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    suspend fun getUserProfile(): UserProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfile)
}
