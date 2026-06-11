package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.FamilyMember
import com.example.data.FamilyRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FamilyViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: FamilyRepository
    private val seeder: com.example.data.DynastySeeder
    
    val allMembers: StateFlow<List<FamilyMember>>
    
    private val _focusMemberId = MutableStateFlow<Long?>(null)
    val focusMemberId: StateFlow<Long?> = _focusMemberId.asStateFlow()

    private val _currentLanguage = MutableStateFlow("ENG")
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = FamilyRepository(database.familyMemberDao())
        seeder = com.example.data.DynastySeeder(database.familyMemberDao())
        
        allMembers = repository.allMembers
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )
        
        // Seed database ONLY ONCE on first-time cold startup if database is completely empty
        viewModelScope.launch {
            val initialList = repository.allMembers.first()
            if (initialList.isEmpty()) {
                seeder.seedDynasty("markovic")
            }
        }
        
        // Maintain the active focus member depending on the active membership list
        viewModelScope.launch {
            allMembers.collectLatest { list ->
                if (list.isNotEmpty() && _focusMemberId.value == null) {
                    val defaultFocus = if (list.size > 4) list[4] else list.firstOrNull()
                    _focusMemberId.value = defaultFocus?.id
                }
            }
        }
    }

    // Tree hierarchy states derived from the current focus member
    val currentFocusMember: StateFlow<FamilyMember?> = combine(allMembers, _focusMemberId) { members, id ->
        members.find { it.id == id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentSpouse: StateFlow<FamilyMember?> = combine(allMembers, currentFocusMember) { members, focus ->
        focus?.spouseId?.let { id -> members.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentFather: StateFlow<FamilyMember?> = combine(allMembers, currentFocusMember) { members, focus ->
        focus?.fatherId?.let { id -> members.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentMother: StateFlow<FamilyMember?> = combine(allMembers, currentFocusMember) { members, focus ->
        focus?.motherId?.let { id -> members.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentChildren: StateFlow<List<FamilyMember>> = combine(allMembers, currentFocusMember) { members, focus ->
        if (focus == null) emptyList()
        else members.filter { it.fatherId == focus.id || it.motherId == focus.id }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentSiblings: StateFlow<List<FamilyMember>> = combine(allMembers, currentFocusMember) { members, focus ->
        if (focus == null) emptyList()
        else members.filter { 
            it.id != focus.id && (
                (focus.fatherId != null && it.fatherId == focus.fatherId) || 
                (focus.motherId != null && it.motherId == focus.motherId)
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Grandparents
    val paternalGrandfather: StateFlow<FamilyMember?> = combine(allMembers, currentFather) { members, father ->
        father?.fatherId?.let { id -> members.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val paternalGrandmother: StateFlow<FamilyMember?> = combine(allMembers, currentFather) { members, father ->
        father?.motherId?.let { id -> members.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val maternalGrandfather: StateFlow<FamilyMember?> = combine(allMembers, currentMother) { members, mother ->
        mother?.fatherId?.let { id -> members.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val maternalGrandmother: StateFlow<FamilyMember?> = combine(allMembers, currentMother) { members, mother ->
        mother?.motherId?.let { id -> members.find { it.id == id } }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun setFocusMember(id: Long) {
        _focusMemberId.value = id
    }

    fun setLanguage(langCode: String) {
        _currentLanguage.value = langCode
    }

    fun deleteMember(member: FamilyMember) {
        viewModelScope.launch {
            repository.delete(member)
            if (_focusMemberId.value == member.id) {
                val remaining = allMembers.value.filter { it.id != member.id }
                _focusMemberId.value = remaining.firstOrNull()?.id
            }
        }
    }

    fun updateMember(member: FamilyMember) {
        viewModelScope.launch {
            repository.update(member)
        }
    }

    fun addMemberWithRelationship(
        firstName: String,
        lastName: String,
        gender: String,
        birthDate: String?,
        birthPlace: String?,
        deathDate: String?,
        deathPlace: String?,
        biography: String?,
        phoneNumber: String?,
        email: String?,
        relateToId: Long,
        relationshipType: String
    ) {
        viewModelScope.launch {
            val relatedPerson = repository.getMemberById(relateToId) ?: return@launch
            
            var newMember = FamilyMember(
                firstName = firstName,
                lastName = lastName,
                gender = gender,
                birthDate = birthDate.orEmpty().ifBlank { null },
                birthPlace = birthPlace.orEmpty().ifBlank { null },
                deathDate = deathDate.orEmpty().ifBlank { null },
                deathPlace = deathPlace.orEmpty().ifBlank { null },
                biography = biography.orEmpty().ifBlank { null },
                phoneNumber = phoneNumber.orEmpty().ifBlank { null },
                email = email.orEmpty().ifBlank { null }
            )

            when (relationshipType) {
                "FATHER" -> {
                    val insertedId = repository.insert(newMember)
                    val updatedChild = relatedPerson.copy(fatherId = insertedId)
                    repository.update(updatedChild)
                    
                    updatedChild.motherId?.let { momId ->
                        repository.getMemberById(momId)?.let { mom ->
                            repository.update(mom.copy(spouseId = insertedId))
                            val freshFather = repository.getMemberById(insertedId)
                            if (freshFather != null) {
                                repository.update(freshFather.copy(spouseId = momId))
                            }
                        }
                    }
                }
                "MOTHER" -> {
                    val insertedId = repository.insert(newMember)
                    val updatedChild = relatedPerson.copy(motherId = insertedId)
                    repository.update(updatedChild)
                    
                    updatedChild.fatherId?.let { dadId ->
                        repository.getMemberById(dadId)?.let { dad ->
                            repository.update(dad.copy(spouseId = insertedId))
                            val freshMother = repository.getMemberById(insertedId)
                            if (freshMother != null) {
                                repository.update(freshMother.copy(spouseId = dadId))
                            }
                        }
                    }
                }
                "SPOUSE" -> {
                    newMember = newMember.copy(spouseId = relatedPerson.id)
                    val insertedId = repository.insert(newMember)
                    repository.update(relatedPerson.copy(spouseId = insertedId))
                }
                "CHILD" -> {
                    if (relatedPerson.gender == "MALE") {
                        newMember = newMember.copy(
                            fatherId = relatedPerson.id,
                            motherId = relatedPerson.spouseId
                        )
                    } else {
                        newMember = newMember.copy(
                            motherId = relatedPerson.id,
                            fatherId = relatedPerson.spouseId
                        )
                    }
                    val insertedId = repository.insert(newMember)
                    _focusMemberId.value = insertedId
                }
                "SIBLING" -> {
                    newMember = newMember.copy(
                        fatherId = relatedPerson.fatherId,
                        motherId = relatedPerson.motherId
                    )
                    repository.insert(newMember)
                }
            }
        }
    }

    fun addRootMember(
        firstName: String,
        lastName: String,
        gender: String,
        birthDate: String?,
        birthPlace: String?
    ) {
        viewModelScope.launch {
            val root = FamilyMember(
                firstName = firstName,
                lastName = lastName,
                gender = gender,
                birthDate = birthDate.orEmpty().ifBlank { null },
                birthPlace = birthPlace.orEmpty().ifBlank { null }
            )
            val insertedId = repository.insert(root)
            _focusMemberId.value = insertedId
        }
    }

    fun clearDatabase() {
        viewModelScope.launch {
            repository.deleteAll()
            _focusMemberId.value = null
        }
    }

    fun resetDatabaseToDemo() {
        viewModelScope.launch {
            seeder.seedDynasty("markovic")
            val list = repository.allMembers.firstOrNull() ?: emptyList()
            if (list.isNotEmpty()) {
                val defaultFocus = if (list.size > 4) list[4] else list.firstOrNull()
                _focusMemberId.value = defaultFocus?.id
            }
        }
    }

    fun loadPresetDynasty(key: String) {
        viewModelScope.launch {
            seeder.seedDynasty(key)
            val list = repository.allMembers.firstOrNull() ?: emptyList()
            if (list.isNotEmpty()) {
                val defaultFocus = if (list.size > 4) list[4] else list.firstOrNull()
                _focusMemberId.value = defaultFocus?.id
            }
        }
    }
}
