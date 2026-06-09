package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity(tableName = "family_members")
data class FamilyMember(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val firstName: String,
    val lastName: String,
    val gender: String, // "MALE", "FEMALE", "OTHER"
    val birthDate: String? = null,
    val birthPlace: String? = null,
    val deathDate: String? = null,
    val deathPlace: String? = null,
    val biography: String? = null,
    val avatarResName: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    
    // Relationships: IDs of other family members
    val fatherId: Long? = null,
    val motherId: Long? = null,
    val spouseId: Long? = null
) : Serializable
