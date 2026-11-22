package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.AmendmentEntity
import com.example.data.entity.DatabaseMetadataEntity
import com.example.data.entity.LawVerificationReportEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VerificationDao {
    @Query("SELECT * FROM verification_reports ORDER BY lawId ASC")
    fun getAllVerificationReports(): Flow<List<LawVerificationReportEntity>>

    @Query("SELECT * FROM verification_reports ORDER BY lawId ASC")
    suspend fun getAllVerificationReportsDirect(): List<LawVerificationReportEntity>

    @Query("SELECT * FROM verification_reports WHERE lawId = :lawId LIMIT 1")
    fun getVerificationReportForLaw(lawId: Long): Flow<LawVerificationReportEntity?>

    @Query("SELECT * FROM verification_reports WHERE lawId = :lawId LIMIT 1")
    suspend fun getVerificationReportForLawDirect(lawId: Long): LawVerificationReportEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationReport(report: LawVerificationReportEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationReports(reports: List<LawVerificationReportEntity>)

    // Metadata
    @Query("SELECT * FROM database_metadata WHERE `key` = :key LIMIT 1")
    suspend fun getMetadata(key: String): DatabaseMetadataEntity?

    @Query("SELECT * FROM database_metadata")
    fun getAllMetadata(): Flow<List<DatabaseMetadataEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun setMetadata(metadata: DatabaseMetadataEntity)

    // Amendments
    @Query("SELECT * FROM amendments WHERE lawId = :lawId ORDER BY amendmentYear DESC")
    fun getAmendmentsForLaw(lawId: Long): Flow<List<AmendmentEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAmendments(amendments: List<AmendmentEntity>)
}
