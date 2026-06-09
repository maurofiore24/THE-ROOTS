package com.example.data

import kotlinx.coroutines.flow.Flow

class FamilyRepository(private val familyMemberDao: FamilyMemberDao) {
    val allMembers: Flow<List<FamilyMember>> = familyMemberDao.getAllMembers()

    suspend fun getMemberById(id: Long): FamilyMember? = familyMemberDao.getMemberById(id)

    fun getMemberByIdFlow(id: Long): Flow<FamilyMember?> = familyMemberDao.getMemberByIdFlow(id)

    suspend fun insert(member: FamilyMember): Long = familyMemberDao.insertMember(member)

    suspend fun update(member: FamilyMember) = familyMemberDao.updateMember(member)

    suspend fun delete(member: FamilyMember) = familyMemberDao.deleteMember(member)

    suspend fun deleteAll() = familyMemberDao.deleteAll()
}
