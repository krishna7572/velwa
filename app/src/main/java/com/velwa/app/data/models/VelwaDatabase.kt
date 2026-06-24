package com.velwa.app.data.models

import androidx.lifecycle.LiveData
import androidx.room.*

// ─── DAO ───────────────────────────────────────────────
@Dao
interface DeviceDao {

    @Query("SELECT * FROM saved_devices ORDER BY isFavorite DESC, lastConnected DESC")
    fun getAllDevices(): LiveData<List<VelwaDevice>>

    @Query("SELECT * FROM saved_devices WHERE isFavorite = 1")
    fun getFavoriteDevices(): LiveData<List<VelwaDevice>>

    @Query("SELECT * FROM saved_devices WHERE address = :address LIMIT 1")
    suspend fun getDeviceByAddress(address: String): VelwaDevice?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: VelwaDevice)

    @Update
    suspend fun updateDevice(device: VelwaDevice)

    @Delete
    suspend fun deleteDevice(device: VelwaDevice)

    @Query("UPDATE saved_devices SET isFavorite = :fav WHERE address = :address")
    suspend fun setFavorite(address: String, fav: Boolean)

    @Query("UPDATE saved_devices SET alias = :alias WHERE address = :address")
    suspend fun updateAlias(address: String, alias: String)

    @Query("UPDATE saved_devices SET autoConnect = :auto WHERE address = :address")
    suspend fun setAutoConnect(address: String, auto: Boolean)

    @Query("UPDATE saved_devices SET lastConnected = :time WHERE address = :address")
    suspend fun updateLastConnected(address: String, time: Long)
}

// ─── DATABASE ──────────────────────────────────────────
@Database(entities = [VelwaDevice::class], version = 1, exportSchema = false)
abstract class VelwaDatabase : RoomDatabase() {
    abstract fun deviceDao(): DeviceDao

    companion object {
        @Volatile
        private var INSTANCE: VelwaDatabase? = null

        fun getDatabase(context: android.content.Context): VelwaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    VelwaDatabase::class.java,
                    "velwa_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
