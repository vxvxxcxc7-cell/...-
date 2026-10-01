package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecoveredFileDao {
    @Query("SELECT * FROM recovered_files ORDER BY restoredTimestamp DESC")
    fun getAllRecoveredFiles(): Flow<List<RecoveredFileEntity>>

    @Query("SELECT * FROM recovered_files WHERE fileType = :type ORDER BY restoredTimestamp DESC")
    fun getRecoveredFilesByType(type: String): Flow<List<RecoveredFileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecoveredFile(file: RecoveredFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(files: List<RecoveredFileEntity>)

    @Query("DELETE FROM recovered_files WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM recovered_files")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM recovered_files")
    suspend fun getCount(): Int

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM recovered_files")
    suspend fun getTotalRestoredSize(): Long
}
