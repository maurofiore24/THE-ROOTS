package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface FamilyMemberDao {
    @Query("SELECT * FROM family_members ORDER BY lastName ASC, firstName ASC")
    fun getAllMembers(): Flow<List<FamilyMember>>

    @Query("SELECT * FROM family_members WHERE id = :id")
    suspend fun getMemberById(id: Long): FamilyMember?

    @Query("SELECT * FROM family_members WHERE id = :id")
    fun getMemberByIdFlow(id: Long): Flow<FamilyMember?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMember(member: FamilyMember): Long

    @Update
    suspend fun updateMember(member: FamilyMember)

    @Delete
    suspend fun deleteMember(member: FamilyMember)

    @Query("DELETE FROM family_members")
    suspend fun deleteAll()
}
