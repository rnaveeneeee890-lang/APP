package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.TaxiDao
import com.example.data.model.SavedAddress
import com.example.data.model.Trip
import com.example.data.model.UserProfile

@Database(
    entities = [Trip::class, SavedAddress::class, UserProfile::class],
    version = 1,
    exportSchema = false
)
abstract class TaxiDatabase : RoomDatabase() {
    abstract fun taxiDao(): TaxiDao

    companion object {
        @Volatile
        private var INSTANCE: TaxiDatabase? = null

        fun getDatabase(context: Context): TaxiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TaxiDatabase::class.java,
                    "taxi_future_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
