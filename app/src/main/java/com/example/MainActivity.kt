package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.FamilyViewModel
import com.example.ui.components.FamilyTreeDashboard
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: FamilyViewModel = viewModel()
                
                // Collect states from Room-backed ViewModel
                val allMembers by viewModel.allMembers.collectAsState()
                val focusMember by viewModel.currentFocusMember.collectAsState()
                val spouse by viewModel.currentSpouse.collectAsState()
                val currentLanguage by viewModel.currentLanguage.collectAsState()
                
                val father by viewModel.currentFather.collectAsState()
                val mother by viewModel.currentMother.collectAsState()
                val children by viewModel.currentChildren.collectAsState()
                val siblings by viewModel.currentSiblings.collectAsState()
                
                val patGrandfather by viewModel.paternalGrandfather.collectAsState()
                val patGrandmother by viewModel.paternalGrandmother.collectAsState()
                val maternalGrandfather by viewModel.maternalGrandfather.collectAsState()
                val maternalGrandmother by viewModel.maternalGrandmother.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    FamilyTreeDashboard(
                        allMembers = allMembers,
                        focusMember = focusMember,
                        spouse = spouse,
                        father = father,
                        mother = mother,
                        children = children,
                        siblings = siblings,
                        patGrandfather = patGrandfather,
                        patGrandmother = patGrandmother,
                        matGrandfather = maternalGrandfather,
                        matGrandmother = maternalGrandmother,
                        currentLanguage = currentLanguage,
                        onLanguageChange = { viewModel.setLanguage(it) },
                        onLoadPresetDynasty = { viewModel.loadPresetDynasty(it) },
                        onSetFocus = { viewModel.setFocusMember(it) },
                        onAddRelative = { relateToId, type -> 
                            // This is handled by opening dialog, managed in Core Dashboard layout
                        },
                        onEditMember = { viewModel.updateMember(it) },
                        onAddStandaloneRoot = { fName, lName, gender, bDate, bPl ->
                            viewModel.addRootMember(fName, lName, gender, bDate, bPl)
                        },
                        onResetDemo = { viewModel.resetDatabaseToDemo() },
                        onClearAll = { viewModel.clearDatabase() },
                        onAddRelativeDirectly = { fName, lName, gender, bDate, bPl, dDate, dPl, bio, phone, email, relateId, type ->
                            viewModel.addMemberWithRelationship(
                                firstName = fName,
                                lastName = lName,
                                gender = gender,
                                birthDate = bDate,
                                birthPlace = bPl,
                                deathDate = dDate,
                                deathPlace = dPl,
                                biography = bio,
                                phoneNumber = phone,
                                email = email,
                                relateToId = relateId,
                                relationshipType = type
                            )
                        }
                    )
                }
            }
        }
    }
}
