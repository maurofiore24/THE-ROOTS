package com.example.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.FamilyMember
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class VisualTheme {
    FROSTED_GLASS,
    VINTAGE_PARCHMENT,
    NORDIJSKI_MINIMAL,
    CYBERPUNK
}

val LocalLanguage = compositionLocalOf { "ENG" }

// Helper methods for dynamic localization
fun getLocalizedText(rawText: String?, langCode: String): String {
    if (rawText == null) return ""
    val parts = rawText.split("||")
    if (parts.size < 2) return rawText
    return when (langCode) {
        "ENG" -> parts.getOrNull(0)?.trim() ?: parts[0]
        "SRB" -> parts.getOrNull(1)?.trim() ?: parts[0]
        "RUS" -> parts.getOrNull(2)?.trim() ?: parts[0]
        "DEU" -> parts.getOrNull(3)?.trim() ?: parts[0]
        "FRA" -> parts.getOrNull(4)?.trim() ?: parts[0]
        else -> parts[0]
    }
}

fun t(eng: String, srb: String, rus: String, deu: String, fra: String, lang: String): String {
    return when (lang) {
        "ENG" -> eng
        "SRB" -> srb
        "RUS" -> rus
        "DEU" -> deu
        "FRA" -> fra
        else -> eng
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FamilyTreeDashboard(
    allMembers: List<FamilyMember>,
    focusMember: FamilyMember?,
    spouse: FamilyMember?,
    father: FamilyMember?,
    mother: FamilyMember?,
    children: List<FamilyMember>,
    siblings: List<FamilyMember>,
    patGrandfather: FamilyMember?,
    patGrandmother: FamilyMember?,
    matGrandfather: FamilyMember?,
    matGrandmother: FamilyMember?,
    currentLanguage: String = "ENG",
    onLanguageChange: (String) -> Unit = {},
    onLoadPresetDynasty: (String) -> Unit = {},
    onSetFocus: (Long) -> Unit,
    onAddRelative: (relateToId: Long, relType: String) -> Unit,
    onEditMember: (FamilyMember) -> Unit,
    onAddStandaloneRoot: () -> Unit,
    onResetDemo: () -> Unit,
    onClearAll: () -> Unit,
    onAddRelativeDirectly: (
        firstName: String, lastName: String, gender: String,
        birthDate: String?, birthPlace: String?, deathDate: String?, deathPlace: String?,
        bio: String?, phone: String?, email: String?, relationToId: Long, relationType: String
    ) -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    CompositionLocalProvider(LocalLanguage provides currentLanguage) {
        var selectedTab by remember { mutableStateOf(0) }
    var currentTheme by remember { mutableStateOf(VisualTheme.FROSTED_GLASS) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Dialog States
    var showAddDialog by remember { mutableStateOf<Pair<Long, String>?>(null) } // RelateToId to RelationshipType
    var showEditDialog by remember { mutableStateOf<FamilyMember?>(null) }
    var showPrintDialog by remember { mutableStateOf(false) }
    var showAddStandaloneDialog by remember { mutableStateOf(false) }

    // Theme Config Colors
    val appColors = when (currentTheme) {
        VisualTheme.FROSTED_GLASS -> ThemeConfig(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF0F172A), Color(0xFF020617))),
            surfaceColor = Color(0xFF1E293B).copy(alpha = 0.7f),
            textColor = Color(0xFFF8FAFC),
            textSecondaryColor = Color(0xFF94A3B8),
            accentColor = Color(0xFF6366F1), // Indigo
            borderColor = Color(0xFF334155).copy(alpha = 0.5f),
            glowColor = Color(0xFF818CF8),
            fontFamily = FontFamily.SansSerif,
            meshBlurEnabled = true
        )
        VisualTheme.VINTAGE_PARCHMENT -> ThemeConfig(
            bgBrush = Brush.linearGradient(listOf(Color(0xFFFAF6EE), Color(0xFFEFE9DC))),
            surfaceColor = Color(0xFFF3EAD3),
            textColor = Color(0xFF2B2519),
            textSecondaryColor = Color(0xFF6E6047),
            accentColor = Color(0xFF8C6239), // Warm wood brown
            borderColor = Color(0xFFDCD2B3),
            glowColor = Color(0xFFB5936C),
            fontFamily = FontFamily.Serif,
            meshBlurEnabled = false
        )
        VisualTheme.NORDIJSKI_MINIMAL -> ThemeConfig(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFFF8F9FA), Color(0xFFE9ECEF))),
            surfaceColor = Color(0xFFFFFFFF),
            textColor = Color(0xFF212529),
            textSecondaryColor = Color(0xFF6C757D),
            accentColor = Color(0xFF343A40), // Hard Charcoal
            borderColor = Color(0xFFDEE2E6),
            glowColor = Color(0xFFADB5BD),
            fontFamily = FontFamily.SansSerif,
            meshBlurEnabled = false
        )
        VisualTheme.CYBERPUNK -> ThemeConfig(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF05050A), Color(0xFF0F0A1C))),
            surfaceColor = Color(0xFF150F26).copy(alpha = 0.85f),
            textColor = Color(0xFF00FFCC), // Fluorescent Green
            textSecondaryColor = Color(0xFFFF007F), // Neon Pink
            accentColor = Color(0xFF00BFFF), // Deep Sky Neon Blue
            borderColor = Color(0xFFFF007F).copy(alpha = 0.4f),
            glowColor = Color(0xFF00FFCC),
            fontFamily = FontFamily.Monospace,
            meshBlurEnabled = true
        )
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = if (currentTheme == VisualTheme.VINTAGE_PARCHMENT) Color(0xFFEAE2CD) else appColors.surfaceColor,
                contentColor = appColors.accentColor,
                modifier = Modifier.navigationBarsPadding()
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.AccountTree, t("Tree", "Stablo", "Древо", "Stammbaum", "Arbre", currentLanguage)) },
                    label = { 
                        Text(
                            text = t("Tree", "Stablo", "Древо", "Stammbaum", "Arbre", currentLanguage), 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = appColors.fontFamily
                        ) 
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Groups, t("Members", "Članovi", "Члены", "Mitglieder", "Membres", currentLanguage)) },
                    label = { 
                        Text(
                            text = t("Members", "Članovi", "Članovi", "Mitglieder", "Membres", currentLanguage), 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = appColors.fontFamily
                        ) 
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    icon = { Icon(Icons.Default.HistoryEdu, t("Stories", "Priče", "Истории", "Geschichten", "Histoires", currentLanguage)) },
                    label = { 
                        Text(
                            text = t("Story Keeper", "Čuvar Priča", "Хранитель Историй", "Geschichten", "Garde d'Histoires", currentLanguage), 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = appColors.fontFamily
                        ) 
                    }
                )
                NavigationBarItem(
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 },
                    icon = { Icon(Icons.Default.Map, t("Migrations", "Migracije", "Миграции", "Migrationen", "Migrations", currentLanguage)) },
                    label = { 
                        Text(
                            text = t("Migrations", "Migracije", "Миграции", "Migrationen", "Migrations", currentLanguage), 
                            fontSize = 10.sp, 
                            fontWeight = FontWeight.Bold,
                            fontFamily = appColors.fontFamily
                        ) 
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(appColors.bgBrush)
                .padding(innerPadding)
        ) {
            // Ambient Backdrop Mesh Blurs
            if (appColors.meshBlurEnabled) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .offset(x = (-50).dp, y = 100.dp)
                            .size(260.dp)
                            .blur(90.dp)
                            .background(
                                color = if (currentTheme == VisualTheme.CYBERPUNK) Color(0xFFFF007F).copy(alpha = 0.15f)
                                else Color(0xFF6366F1).copy(alpha = 0.18f),
                                shape = CircleShape
                            )
                    )
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .offset(x = 60.dp, y = (-80.dp))
                            .size(280.dp)
                            .blur(100.dp)
                            .background(
                                color = if (currentTheme == VisualTheme.CYBERPUNK) Color(0xFF00FFCC).copy(alpha = 0.1f)
                                else Color(0xFF10B981).copy(alpha = 0.15f),
                                shape = CircleShape
                            )
                    )
                }
            }

            // Main Application Container
            Column(modifier = Modifier.fillMaxSize()) {
                
                // Top Custom Header Action Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = t("KOREN", "KOREN", "КОРЕНЬ", "KOREN", "KOREN", currentLanguage),
                            fontFamily = appColors.fontFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 24.sp,
                            color = appColors.textColor,
                            letterSpacing = 4.sp
                        )
                        Text(
                            text = t(
                                "Family Heritage and Memory",
                                "Porodično Nasleđe i Sećanje",
                                "Семейное наследие и память",
                                "Familienerbe und Erinnerung",
                                "Patrimoine familial et mémoire",
                                currentLanguage
                            ),
                            fontFamily = appColors.fontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp,
                            color = appColors.textSecondaryColor
                        )
                    }

                    // Floating theme switcher & core actions
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Limit / Freemium Status Indicator
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(appColors.surfaceColor)
                                .border(1.dp, appColors.borderColor, RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${t("Limit", "Paket", "Лимит", "Paket", "Offre", currentLanguage)}: ${allMembers.size}/50",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textSecondaryColor,
                                fontFamily = appColors.fontFamily
                            )
                        }

                        // Language Selector Dropdown Menu
                        var langMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { langMenuExpanded = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(appColors.surfaceColor, CircleShape)
                                    .border(1.dp, appColors.borderColor, CircleShape)
                            ) {
                                Text(
                                    text = when (currentLanguage) {
                                        "ENG" -> "🇬🇧"
                                        "SRB" -> "🇷🇸"
                                        "RUS" -> "🇷🇺"
                                        "DEU" -> "🇩🇪"
                                        "FRA" -> "🇫🇷"
                                        else -> "🌍"
                                    },
                                    fontSize = 16.sp,
                                    lineHeight = 16.sp
                                )
                            }
                            DropdownMenu(
                                expanded = langMenuExpanded,
                                onDismissRequest = { langMenuExpanded = false },
                                modifier = Modifier.background(appColors.surfaceColor)
                            ) {
                                val languages = listOf(
                                    Triple("ENG", "English", "🇬🇧"),
                                    Triple("SRB", "Srpski", "🇷🇸"),
                                    Triple("RUS", "Русский", "🇷🇺"),
                                    Triple("DEU", "Deutsch", "🇩🇪"),
                                    Triple("FRA", "Français", "🇫🇷")
                                )
                                languages.forEach { (code, name, flag) ->
                                    DropdownMenuItem(
                                        text = { 
                                            Text(
                                                "$flag  $name", 
                                                color = if (currentLanguage == code) appColors.accentColor else appColors.textColor,
                                                fontWeight = if (currentLanguage == code) FontWeight.Bold else FontWeight.Normal,
                                                fontFamily = appColors.fontFamily
                                            ) 
                                        },
                                        onClick = {
                                            onLanguageChange(code)
                                            langMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Dynasty Selector Dropdown Menu
                        var dynastyMenuExpanded by remember { mutableStateOf(false) }
                        Box {
                            IconButton(
                                onClick = { dynastyMenuExpanded = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(appColors.surfaceColor, CircleShape)
                                    .border(1.dp, appColors.borderColor, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoStories,
                                    contentDescription = t("Load Dynasty", "Učitaj dinastiju", "Загрузить династию", "Dynastie laden", "Charger la dynastie", currentLanguage),
                                    tint = appColors.textColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            DropdownMenu(
                                expanded = dynastyMenuExpanded,
                                onDismissRequest = { dynastyMenuExpanded = false },
                                modifier = Modifier
                                    .width(280.dp)
                                    .background(appColors.surfaceColor)
                            ) {
                                val dynasties = listOf(
                                    Triple("markovic", "Marković Family (Demo)", "🇷🇸 Porodica Marković (Demo)"),
                                    Triple("nemanjici", "Nemanjić Dynasty (Medieval)", "🇷🇸 Dinastija Nemanjića"),
                                    Triple("tudor", "House of Tudor (England)", "🇬🇧 Dinastija Tjudor (Engleska)"),
                                    Triple("romanov", "Romanov Dynasty (Russia)", "🇷🇺 Dinastija Romanov (Rusija)"),
                                    Triple("habsburg", "Habsburg Dynasty (Austria)", "🇩🇪 Dinastija Habsburg (Austrija)"),
                                    Triple("bourbon", "Bourbon Dynasty (France)", "🇫🇷 Dinastija Burbon (Francuska)"),
                                    Triple("osman", "Ottoman Dynasty (Turkey)", "🇹🇷 Dinastija Osmanlija (Turska)"),
                                    Triple("medici", "House of Medici (Florence)", "🇮🇹 Dinastija Mediči (Firenca)"),
                                    Triple("yamato", "Yamato Dynasty (Japan)", "🇯🇵 Dinastija Jamato (Japan)"),
                                    Triple("julioclaudian", "Julio-Claudian (Rome)", "🇮🇹 Julijevci-Klaudijevci (Rim)"),
                                    Triple("bonaparte", "Bonaparte Dynasty (France)", "🇫🇷 Dinastija Bonaparta (Francuska)")
                                )
                                dynasties.forEach { (key, engLabel, srbLabel) ->
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    t(engLabel, srbLabel, engLabel, engLabel, engLabel, currentLanguage),
                                                    color = appColors.textColor,
                                                    fontSize = 14.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    fontFamily = appColors.fontFamily
                                                )
                                                Text(
                                                    key.uppercase(),
                                                    color = appColors.textSecondaryColor,
                                                    fontSize = 10.sp,
                                                    fontFamily = appColors.fontFamily
                                                )
                                            }
                                        },
                                        onClick = {
                                            onLoadPresetDynasty(key)
                                            dynastyMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Theme Cycling Control
                        IconButton(
                            onClick = {
                                val nextTheme = when (currentTheme) {
                                    VisualTheme.FROSTED_GLASS -> VisualTheme.VINTAGE_PARCHMENT
                                    VisualTheme.VINTAGE_PARCHMENT -> VisualTheme.NORDIJSKI_MINIMAL
                                    VisualTheme.NORDIJSKI_MINIMAL -> VisualTheme.CYBERPUNK
                                    VisualTheme.CYBERPUNK -> VisualTheme.FROSTED_GLASS
                                }
                                currentTheme = nextTheme
                            },
                            modifier = Modifier
                                .size(36.dp)
                                .background(appColors.surfaceColor, CircleShape)
                                .border(1.dp, appColors.borderColor, CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = "Zameni vizuelnu temu",
                                tint = appColors.textColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        // Magic print builder button
                        IconButton(
                            onClick = { showPrintDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(appColors.surfaceColor, CircleShape)
                                .border(1.dp, appColors.borderColor, CircleShape)
                                .testTag("magic_print_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocalPrintshop,
                                contentDescription = "Generiši poster ili knjigu",
                                tint = appColors.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Inline quick info warning if database is empty
                if (allMembers.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .background(appColors.surfaceColor)
                            .border(1.dp, appColors.accentColor, RoundedCornerShape(12.dp))
                            .padding(16.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.FilterVintage,
                                contentDescription = null,
                                tint = appColors.accentColor,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Stablo je trenutno prazno",
                                color = appColors.textColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = appColors.fontFamily,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Pokrenite digitalni koren porodice dodavanjem prvog člana ili učitavanjem bogatog istorijskog demo stabla.",
                                color = appColors.textSecondaryColor,
                                fontSize = 12.sp,
                                fontFamily = appColors.fontFamily,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(
                                    onClick = onResetDemo,
                                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                                ) {
                                    Text("Učitaj Demo Stablo", color = Color.White)
                                }
                                OutlinedButton(
                                    onClick = { showAddStandaloneDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textColor)
                                ) {
                                    Text("Dodaj Ručno")
                                }
                            }
                        }
                    }
                } else {
                    // Application tabs
                    when (selectedTab) {
                        0 -> InteractiveTreeCanvas(
                            allMembers = allMembers,
                            focusMember = focusMember,
                            spouse = spouse,
                            father = father,
                            mother = mother,
                            children = children,
                            siblings = siblings,
                            patGrandfather = patGrandfather,
                            patGrandmother = patGrandmother,
                            matGrandfather = matGrandfather,
                            matGrandmother = matGrandmother,
                            onSetFocus = onSetFocus,
                            onAddRelative = { relateToId, relType ->
                                showAddDialog = Pair(relateToId, relType)
                            },
                            onEditMember = { showEditDialog = it },
                            appColors = appColors,
                            currentTheme = currentTheme,
                            onClearAll = onClearAll,
                            onResetDemo = onResetDemo
                        )
                        1 -> DirectMembersList(
                            allMembers = allMembers,
                            searchQuery = searchQuery,
                            onSearchChange = { searchQuery = it },
                            onSelect = { onSetFocus(it.id) },
                            appColors = appColors,
                            onEdit = { showEditDialog = it },
                            onDelete = { /* handeled in viewModel */ }
                        )
                        2 -> StorytellerAndCapsuleSection(
                            focusMember = focusMember,
                            appColors = appColors,
                            allMembers = allMembers
                        )
                        3 -> MigrationMapSection(
                            focusMember = focusMember,
                            appColors = appColors
                        )
                    }
                }
            }
        }
    }

    // Dialog: Add Relative with relationship
    if (showAddDialog != null) {
        val (relateToId, relationshipType) = showAddDialog!!
        val pivotPerson = allMembers.find { it.id == relateToId }
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf(pivotPerson?.lastName ?: "") }
        var gender by remember { 
            mutableStateOf(
                if (relationshipType == "FATHER") "MALE"
                else if (relationshipType == "MOTHER") "FEMALE"
                else "MALE"
            )
        }
        var birthDate by remember { mutableStateOf("") }
        var birthPlace by remember { mutableStateOf("") }
        var deathDate by remember { mutableStateOf("") }
        var deathPlace by remember { mutableStateOf("") }
        var biography by remember { mutableStateOf("") }
        var phoneNumber by remember { mutableStateOf("") }
        var email by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddDialog = null },
            title = {
                Text(
                    text = "Dodaj Srodnika: ${relationshipType.uppercase()} za člana ${pivotPerson?.firstName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = appColors.fontFamily
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Ime *") },
                        modifier = Modifier.fillMaxWidth().testTag("input_fname")
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Prezime *") },
                        modifier = Modifier.fillMaxWidth().testTag("input_lname")
                    )
                    
                    // Gender selection
                    Text("Pol:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "MALE", onClick = { gender = "MALE" })
                            Text("Muški", modifier = Modifier.clickable { gender = "MALE" })
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "FEMALE", onClick = { gender = "FEMALE" })
                            Text("Ženski", modifier = Modifier.clickable { gender = "FEMALE" })
                        }
                    }

                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Datum rođenja (npr. 12.06.1950)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = { birthPlace = it },
                        label = { Text("Mesto rođenja (npr. Čačak, Srbija)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deathDate,
                        onValueChange = { deathDate = it },
                        label = { Text("Datum smrti (ako je preminuo)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deathPlace,
                        onValueChange = { deathPlace = it },
                        label = { Text("Mesto smrti") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = biography,
                        onValueChange = { biography = it },
                        label = { Text("Kratka sećanja i biografija") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Telefon") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email (koristiće se za saradnju)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (firstName.isNotBlank() && lastName.isNotBlank()) {
                            onAddRelativeDirectly(
                                firstName, lastName, gender,
                                birthDate.ifBlank { null }, birthPlace.ifBlank { null },
                                deathDate.ifBlank { null }, deathPlace.ifBlank { null },
                                biography.ifBlank { null }, phoneNumber.ifBlank { null }, email.ifBlank { null },
                                relateToId, relationshipType
                            )
                            showAddDialog = null
                        }
                    },
                    modifier = Modifier.testTag("confirm_add_member"),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                ) {
                    Text("Sačuvaj", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = null }) {
                    Text("Otkaži")
                }
            }
        )
    }

    // Dialog: Edit Member Profile
    if (showEditDialog != null) {
        val memberToEdit = showEditDialog!!
        var firstName by remember { mutableStateOf(memberToEdit.firstName) }
        var lastName by remember { mutableStateOf(memberToEdit.lastName) }
        var birthDate by remember { mutableStateOf(memberToEdit.birthDate ?: "") }
        var birthPlace by remember { mutableStateOf(memberToEdit.birthPlace ?: "") }
        var deathDate by remember { mutableStateOf(memberToEdit.deathDate ?: "") }
        var deathPlace by remember { mutableStateOf(memberToEdit.deathPlace ?: "") }
        var biography by remember { mutableStateOf(memberToEdit.biography ?: "") }
        var phoneNumber by remember { mutableStateOf(memberToEdit.phoneNumber ?: "") }
        var email by remember { mutableStateOf(memberToEdit.email ?: "") }

        AlertDialog(
            onDismissRequest = { showEditDialog = null },
            title = {
                Text(
                    text = "Izmeni Profil: ${memberToEdit.firstName} ${memberToEdit.lastName}",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = appColors.fontFamily
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Ime") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Prezime") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Datum rođenja") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = { birthPlace = it },
                        label = { Text("Mesto rođenja") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deathDate,
                        onValueChange = { deathDate = it },
                        label = { Text("Datum smrti") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deathPlace,
                        onValueChange = { deathPlace = it },
                        label = { Text("Mesto smrti") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = biography,
                        onValueChange = { biography = it },
                        label = { Text("Detaljna Biografija & Beleške") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text("Telefon") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email adresa") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = memberToEdit.copy(
                            firstName = firstName,
                            lastName = lastName,
                            birthDate = birthDate.ifBlank { null },
                            birthPlace = birthPlace.ifBlank { null },
                            deathDate = deathDate.ifBlank { null },
                            deathPlace = deathPlace.ifBlank { null },
                            biography = biography.ifBlank { null },
                            phoneNumber = phoneNumber.ifBlank { null },
                            email = email.ifBlank { null }
                        )
                        onEditMember(updated)
                        showEditDialog = null
                    },
                    modifier = Modifier.testTag("confirm_edit_member"),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                ) {
                    Text("Sačuvaj Promene", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text("Odbaci")
                }
            }
        )
    }

    // Dialog: Add Standalone Root Member
    if (showAddStandaloneDialog) {
        var firstName by remember { mutableStateOf("") }
        var lastName by remember { mutableStateOf("") }
        var gender by remember { mutableStateOf("MALE") }
        var birthDate by remember { mutableStateOf("") }
        var birthPlace by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showAddStandaloneDialog = false },
            title = { Text("Postavi Prvog Pretka (Koren)", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text("Ime") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text("Prezime") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text("Pol:", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "MALE", onClick = { gender = "MALE" })
                            Text("Muški")
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "FEMALE", onClick = { gender = "FEMALE" })
                            Text("Ženski")
                        }
                    }
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text("Datum rođenja") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = { birthPlace = it },
                        label = { Text("Mesto rođenja") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (firstName.isNotBlank() && lastName.isNotBlank()) {
                            // Link into root via action
                            onAddStandaloneRoot()
                            showAddStandaloneDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                ) {
                    Text("Napravi Koren", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStandaloneDialog = false }) {
                    Text("Poništi")
                }
            }
        )
    }

    // Print On Demand Wizard dialog
    if (showPrintDialog) {
        var printOptionSelected by remember { mutableStateOf(0) } // 0: Luksuzna Knjiga, 1: Poster
        var sizeSelected by remember { mutableStateOf("A1 (Veliki Poster)") }
        var leatherColorSelected by remember { mutableStateOf("Tamno Crvena sa zlatorezom") }
        var generatedStatus by remember { mutableStateOf<String?>(null) }
        var orderComplete by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { showPrintDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalPrintshop, null, tint = appColors.accentColor)
                    Spacer(Modifier.width(8.dp))
                    Text("Magično Dugme: Štampaj Nasleđe", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "U partnerstvu sa lokalnim umetničkim štamparijama, jednim klikom pretvorite vaše digitalno stablo u prelepe fizičke poklone.",
                        fontSize = 12.sp,
                        color = appColors.textSecondaryColor
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        Button(
                            onClick = { printOptionSelected = 0 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (printOptionSelected == 0) appColors.accentColor else appColors.surfaceColor,
                                contentColor = if (printOptionSelected == 0) Color.White else appColors.textColor
                            )
                        ) {
                            Text("Kožna Knjiga")
                        }
                        Spacer(Modifier.width(8.dp))
                        Button(
                            onClick = { printOptionSelected = 1 },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (printOptionSelected == 1) appColors.accentColor else appColors.surfaceColor,
                                contentColor = if (printOptionSelected == 1) Color.White else appColors.textColor
                            )
                        ) {
                            Text("Zidni Poster")
                        }
                    }

                    if (printOptionSelected == 0) {
                        // Leather book config
                        Text("Konfiguracija Knjige", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        val bookStyles = listOf(
                            "Tamno Crvena sa zlatorezom", 
                            "Kraljevski Plava sa srebrnim vezom", 
                            "Arhivska Smeđa koža (Vintage)"
                        )
                        bookStyles.forEach { style ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { leatherColorSelected = style }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = leatherColorSelected == style,
                                    onClick = { leatherColorSelected = style }
                                )
                                Text(style, style = MaterialTheme.typography.bodyMedium, color = appColors.textColor)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text("Sadržina:", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        Text(
                            "- Generiše do ${allMembers.size} profila srodnika sa fotografijama\n- Uključuje sve sakupljene porodične priče i crtice\n- Luksuzni tvrdi povez sa porodičnim gerbom.",
                            fontSize = 11.sp,
                            color = appColors.textSecondaryColor
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(appColors.accentColor.copy(alpha = 0.1f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Cena: €59.00", fontWeight = FontWeight.Bold, color = appColors.textColor)
                                Text("Dostava besplatna za Srbiju", fontSize = 10.sp, color = appColors.textSecondaryColor)
                            }
                            Text(
                                "Marža zarada: 45%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981)
                            )
                        }
                    } else {
                        // Poster config
                        Text("Konfiguracija Postera", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        
                        val sizes = listOf("A1 (Veliki Poster) - €29", "A2 (Srednji Poster) - €19", "B1 Premijum - €39")
                        sizes.forEach { size ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { sizeSelected = size }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = sizeSelected == size,
                                    onClick = { sizeSelected = size }
                                )
                                Text(size, style = MaterialTheme.typography.bodyMedium, color = appColors.textColor)
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Poster se štampa na 250g ultra-mat vrelom papiru sa detaljnim vektorskim prikazom stabla i grana. Savršen ukras za dnevnu sobu porodice.",
                            fontSize = 11.sp,
                            color = appColors.textSecondaryColor
                        )

                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(appColors.accentColor.copy(alpha = 0.1f))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Cena: $sizeSelected", fontWeight = FontWeight.Bold, color = appColors.textColor)
                                Text("Štampa + kartonski cilindar dostava", fontSize = 10.sp, color = appColors.textSecondaryColor)
                            }
                            Text("Naša neto zarada: 55%", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF10B981))
                        }
                    }

                    if (generatedStatus != null) {
                        Text(
                            generatedStatus!!,
                            fontWeight = FontWeight.Bold,
                            color = appColors.accentColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {
                if (!orderComplete) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                generatedStatus = "Sastavljanje elemenata stabla..."
                                delay(1200)
                                generatedStatus = "Kreiranje prelepog PDF preloma..."
                                delay(1000)
                                generatedStatus = "Šaljem nalog partnerskoj štampariji..."
                                delay(800)
                                orderComplete = true
                                generatedStatus = "Naručeno! Proizvod kreće u izradu i biće na kućnom pragu za 3-5 radnih dana! Hvala na poverenju."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                    ) {
                        Text("Simuliraj porudžbinu (€)", color = Color.White)
                    }
                } else {
                    Button(
                        onClick = {
                            showPrintDialog = false
                            orderComplete = false
                            generatedStatus = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                    ) {
                        Text("Završi", color = Color.White)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showPrintDialog = false }) {
                    Text("Zatvori")
                }
            }
        )
    }
}
}

// Visual Kinematic Interactive Canvas Row
@Composable
fun InteractiveTreeCanvas(
    allMembers: List<FamilyMember>,
    focusMember: FamilyMember?,
    spouse: FamilyMember?,
    father: FamilyMember?,
    mother: FamilyMember?,
    children: List<FamilyMember>,
    siblings: List<FamilyMember>,
    patGrandfather: FamilyMember?,
    patGrandmother: FamilyMember?,
    matGrandfather: FamilyMember?,
    matGrandmother: FamilyMember?,
    onSetFocus: (Long) -> Unit,
    onAddRelative: (Long, String) -> Unit,
    onEditMember: (FamilyMember) -> Unit,
    appColors: ThemeConfig,
    currentTheme: VisualTheme,
    onClearAll: () -> Unit,
    onResetDemo: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Switch / Clear actions inline
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Interaktivne Grane",
                fontWeight = FontWeight.Bold,
                color = appColors.textColor,
                fontSize = 16.sp,
                fontFamily = appColors.fontFamily
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onResetDemo) {
                    Text("Učitaj demo", fontSize = 11.sp, color = appColors.accentColor)
                }
                TextButton(onClick = onClearAll) {
                    Text("Isprazni", fontSize = 11.sp, color = appColors.textSecondaryColor)
                }
            }
        }

        if (focusMember == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = appColors.accentColor)
            }
            return
        }

        // Kinematic visual nodes tree grid
        // Generation 3: Grandparents Row
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.surfaceColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .border(1.dp, appColors.borderColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "1. GENERACIJA (Preci i Koreni)",
                fontSize = 9.sp,
                fontWeight = FontWeight.Black,
                color = appColors.textSecondaryColor,
                letterSpacing = 1.sp,
                fontFamily = appColors.fontFamily
            )
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                // Paternal Grandparents
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Očeva loza", fontSize = 8.sp, color = appColors.textSecondaryColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniMemberNode(
                            member = patGrandfather,
                            placeholderLabel = "Deda +",
                            onSelect = { onSetFocus(it) },
                            onAdd = { onAddRelative(father?.id ?: focusMember.id, "FATHER") },
                            appColors = appColors
                        )
                        MiniMemberNode(
                            member = patGrandmother,
                            placeholderLabel = "Baba +",
                            onSelect = { onSetFocus(it) },
                            onAdd = { onAddRelative(father?.id ?: focusMember.id, "MOTHER") },
                            appColors = appColors
                        )
                    }
                }

                // Divider line
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(appColors.borderColor)
                )

                // Maternal Grandparents
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Majčina loza", fontSize = 8.sp, color = appColors.textSecondaryColor)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniMemberNode(
                            member = matGrandfather,
                            placeholderLabel = "Deda +",
                            onSelect = { onSetFocus(it) },
                            onAdd = { onAddRelative(mother?.id ?: focusMember.id, "FATHER") },
                            appColors = appColors
                        )
                        MiniMemberNode(
                            member = matGrandmother,
                            placeholderLabel = "Baba +",
                            onSelect = { onSetFocus(it) },
                            onAdd = { onAddRelative(mother?.id ?: focusMember.id, "MOTHER") },
                            appColors = appColors
                        )
                    }
                }
            }
        }

        // Dynamic connecting arrows path lines
        CustomConnectionLine(appColors = appColors)

        // Generation 2: Parents
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.surfaceColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .border(1.dp, appColors.borderColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "2. GENERACIJA (Roditelji)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = appColors.textSecondaryColor,
                    letterSpacing = 1.sp,
                    fontFamily = appColors.fontFamily
                )
            }
            Spacer(Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ParentMemberNode(
                    member = father,
                    role = "Otac",
                    placeholderLabel = "Otac +",
                    onSelect = { onSetFocus(it) },
                    onAdd = { onAddRelative(focusMember.id, "FATHER") },
                    appColors = appColors
                )
                
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = appColors.accentColor.copy(alpha = 0.6f),
                    modifier = Modifier.size(16.dp)
                )

                ParentMemberNode(
                    member = mother,
                    role = "Majka",
                    placeholderLabel = "Majka +",
                    onSelect = { onSetFocus(it) },
                    onAdd = { onAddRelative(focusMember.id, "MOTHER") },
                    appColors = appColors
                )
            }
        }

        CustomConnectionLine(appColors = appColors)

        // Generation 1: Selected / User Focus Member & Spouse
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(appColors.surfaceColor, RoundedCornerShape(20.dp))
                .border(2.dp, appColors.accentColor.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .shadow(
                    elevation = if (currentTheme == VisualTheme.CYBERPUNK) 12.dp else 4.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = appColors.glowColor,
                    spotColor = appColors.glowColor
                )
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "3. GENERACIJA (Aktivno Koleno)",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = appColors.accentColor,
                letterSpacing = 2.sp,
                fontFamily = appColors.fontFamily
            )
            Spacer(Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Main Focus Node
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onEditMember(focusMember) }
                        .padding(4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(68.dp)
                            .clip(CircleShape)
                            .background(
                                color = if (focusMember.gender == "MALE") Color(0xFF1E3A8A) else Color(0xFF881337)
                            )
                            .border(2.dp, appColors.textColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (focusMember.gender == "MALE") Icons.Default.Male else Icons.Default.Female,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = focusMember.firstName,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textColor,
                        fontSize = 15.sp,
                        fontFamily = appColors.fontFamily
                    )
                    Text(
                        text = focusMember.lastName,
                        fontWeight = FontWeight.Light,
                        color = appColors.textSecondaryColor,
                        fontSize = 12.sp,
                        fontFamily = appColors.fontFamily
                    )
                    Text(
                        text = "Fokusirana Osoba 🎯",
                        fontSize = 9.sp,
                        color = appColors.accentColor,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Connected Heart / Marriage
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "Spouse Link",
                    tint = appColors.textColor,
                    modifier = Modifier.size(22.dp)
                )

                // Spouse node
                Column(
                    modifier = Modifier.weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (spouse != null) {
                        Column(
                            modifier = Modifier
                                .clickable { onSetFocus(spouse.id) }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .background(
                                        color = if (spouse.gender == "MALE") Color(0xFF1E3A8A) else Color(0xFF881337)
                                    )
                                    .border(1.dp, appColors.borderColor, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (spouse.gender == "MALE") Icons.Default.Male else Icons.Default.Female,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = spouse.firstName,
                                fontWeight = FontWeight.SemiBold,
                                color = appColors.textColor,
                                fontSize = 13.sp,
                                fontFamily = appColors.fontFamily
                            )
                            Text(
                                text = "Suprug(a)",
                                fontSize = 10.sp,
                                color = appColors.textSecondaryColor
                            )
                        }
                    } else {
                        IconButton(
                            onClick = { onAddRelative(focusMember.id, "SPOUSE") },
                            modifier = Modifier
                                .size(50.dp)
                                .background(appColors.surfaceColor, CircleShape)
                                .border(1.dp, appColors.borderColor, CircleShape)
                        ) {
                            Icon(Icons.Default.Add, null, tint = appColors.accentColor)
                        }
                        Text(
                            "Dodaj supružnika",
                            fontSize = 10.sp,
                            color = appColors.textSecondaryColor,
                            modifier = Modifier.padding(top = 4.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        CustomConnectionLine(appColors = appColors)

        // Generation 0: Siblings & Children
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Siblings
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(appColors.surfaceColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .border(1.dp, appColors.borderColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "BRAĆA I SESTRE",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
                )
                Spacer(Modifier.height(8.dp))
                if (siblings.isEmpty()) {
                    Text("Nema unete braće/sestara", fontSize = 10.sp, color = appColors.textSecondaryColor, fontStyle = FontStyle.Italic)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(siblings) { sib ->
                            Card(
                                modifier = Modifier
                                    .width(70.dp)
                                    .clickable { onSetFocus(sib.id) },
                                colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (sib.gender == "MALE") Color(0xFF1E3A8A) else Color(0xFF881337),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (sib.gender == "MALE") Icons.Default.Male else Icons.Default.Female,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        sib.firstName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                IconButton(
                    onClick = { onAddRelative(focusMember.id, "SIBLING") },
                    modifier = Modifier
                        .size(32.dp)
                        .background(appColors.surfaceColor, CircleShape)
                        .border(1.dp, appColors.borderColor, CircleShape)
                ) {
                    Icon(Icons.Default.Add, null, tint = appColors.accentColor, modifier = Modifier.size(14.dp))
                }
            }

            // Children
            Column(
                modifier = Modifier
                    .weight(1f)
                    .background(appColors.surfaceColor.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
                    .border(1.dp, appColors.borderColor.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "POTOMCI (Deca)",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
                )
                Spacer(Modifier.height(8.dp))
                if (children.isEmpty()) {
                    Text("Nema unete dece", fontSize = 10.sp, color = appColors.textSecondaryColor, fontStyle = FontStyle.Italic)
                } else {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(children) { child ->
                            Card(
                                modifier = Modifier
                                    .width(70.dp)
                                    .clickable { onSetFocus(child.id) },
                                colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor.copy(alpha = 0.5f))
                            ) {
                                Column(
                                    modifier = Modifier.padding(6.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .background(
                                                if (child.gender == "MALE") Color(0xFF1E3A8A) else Color(0xFF881337),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (child.gender == "MALE") Icons.Default.Male else Icons.Default.Female,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        child.firstName,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = appColors.textColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(8.dp))
                IconButton(
                    onClick = { onAddRelative(focusMember.id, "CHILD") },
                    modifier = Modifier
                        .size(32.dp)
                        .background(appColors.surfaceColor, CircleShape)
                        .border(1.dp, appColors.borderColor, CircleShape)
                ) {
                    Icon(Icons.Default.Add, null, tint = appColors.accentColor, modifier = Modifier.size(14.dp))
                }
            }
        }

        // Active Quick Profile Bio Detail Panel for current focus member
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, appColors.borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Informacije o srodniku",
                        fontWeight = FontWeight.Bold,
                        color = appColors.textColor,
                        fontSize = 14.sp
                    )
                    IconButton(
                        onClick = { onEditMember(focusMember) },
                        modifier = Modifier.testTag("edit_profile_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Izmeni profil srodnika",
                            tint = appColors.accentColor,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "${focusMember.firstName} ${focusMember.lastName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = appColors.textColor
                )
                Text(
                    text = "Rođenje: ${focusMember.birthDate ?: "Nepoznat datum"} u ${focusMember.birthPlace ?: "Nepoznato mesto"}",
                    fontSize = 12.sp,
                    color = appColors.textSecondaryColor
                )
                if (focusMember.deathDate != null) {
                    Text(
                        text = "Smrt: ${focusMember.deathDate} u ${focusMember.deathPlace ?: "Nepoznato mesto"}",
                        fontSize = 12.sp,
                        color = appColors.textSecondaryColor
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    text = focusMember.biography ?: "Dodajte porodična kazivanja, anegdote ili istorijski zapis o ovoj osobi kako biste sačuvali sećanje.",
                    fontSize = 11.sp,
                    fontStyle = FontStyle.Italic,
                    color = appColors.textColor
                )
            }
        }
    }
}

// Support Components
@Composable
fun MiniMemberNode(
    member: FamilyMember?,
    placeholderLabel: String,
    onSelect: (Long) -> Unit,
    onAdd: () -> Unit,
    appColors: ThemeConfig
) {
    val currentLanguage = LocalLanguage.current
    Box(
        modifier = Modifier
            .size(54.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(appColors.surfaceColor)
            .border(1.dp, appColors.borderColor, RoundedCornerShape(8.dp))
            .clickable(enabled = member != null) { onSelect(member!!.id) },
        contentAlignment = Alignment.Center
    ) {
        if (member != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxSize().padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .background(
                            if (member.gender == "MALE") Color(0xFF1E3A8A) else Color(0xFF881337),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (member.gender == "MALE") Icons.Default.Male else Icons.Default.Female,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(10.dp)
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = getLocalizedText(member.firstName, currentLanguage),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontFamily = appColors.fontFamily
                )
            }
        } else {
            IconButton(onClick = onAdd, modifier = Modifier.size(20.dp)) {
                Icon(Icons.Default.Add, null, tint = appColors.accentColor.copy(alpha = 0.5f), modifier = Modifier.size(14.dp))
            }
        }
    }
}

@Composable
fun ParentMemberNode(
    member: FamilyMember?,
    role: String,
    placeholderLabel: String,
    onSelect: (Long) -> Unit,
    onAdd: () -> Unit,
    appColors: ThemeConfig
) {
    val currentLanguage = LocalLanguage.current
    val localizedRole = when (role) {
        "OTAC", "FATHER" -> t("Father", "Otac", "Отец", "Vater", "Père", currentLanguage)
        "MAJKA", "MOTHER" -> t("Mother", "Majka", "Мать", "Mutter", "Mère", currentLanguage)
        else -> role
    }
    Box(
        modifier = Modifier
            .width(100.dp)
            .height(54.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(appColors.surfaceColor)
            .border(1.dp, appColors.borderColor, RoundedCornerShape(12.dp))
            .clickable(enabled = member != null) { onSelect(member!!.id) },
        contentAlignment = Alignment.Center
    ) {
        if (member != null) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            if (member.gender == "MALE") Color(0xFF1E3A8A) else Color(0xFF881337),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (member.gender == "MALE") Icons.Default.Male else Icons.Default.Female,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
                Column {
                    Text(
                        text = getLocalizedText(member.firstName, currentLanguage),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontFamily = appColors.fontFamily
                    )
                    Text(
                        localizedRole,
                        fontSize = 8.sp,
                        color = appColors.textSecondaryColor,
                        fontFamily = appColors.fontFamily
                    )
                }
            }
        } else {
            IconButton(onClick = onAdd, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Add, null, tint = appColors.accentColor.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
fun CustomConnectionLine(appColors: ThemeConfig) {
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(16.dp)
    ) {
        drawLine(
            color = appColors.borderColor,
            start = Offset(size.width / 2, 0f),
            end = Offset(size.width / 2, size.height),
            strokeWidth = 2f,
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
        )
    }
}

// Tab 1: Direct List View of all Family Members
@Composable
fun DirectMembersList(
    allMembers: List<FamilyMember>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onSelect: (FamilyMember) -> Unit,
    onEdit: (FamilyMember) -> Unit,
    onDelete: (FamilyMember) -> Unit,
    appColors: ThemeConfig
) {
    val currentLanguage = LocalLanguage.current
    // Correct list rendering
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Search filter
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            placeholder = { 
                Text(
                    text = t(
                        "Search family by name and location...",
                        "Pretraga porodice po imenu i lokaciji...",
                        "Поиск семьи по имени и локации...",
                        "Familie nach Name und Ort suchen...",
                        "Rechercher une famille par nom et lieu...",
                        currentLanguage
                    ), 
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
                ) 
            },
            leadingIcon = { Icon(Icons.Default.Search, null, tint = appColors.textColor) },
            modifier = Modifier.fillMaxWidth().testTag("search_members_input"),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = appColors.accentColor,
                unfocusedBorderColor = appColors.borderColor,
                focusedTextColor = appColors.textColor,
                unfocusedTextColor = appColors.textColor
            ),
            shape = RoundedCornerShape(12.dp)
        )

        val filtered = allMembers.filter {
            val fName = getLocalizedText(it.firstName, currentLanguage)
            val lName = getLocalizedText(it.lastName, currentLanguage)
            val bPlace = getLocalizedText(it.birthPlace, currentLanguage)
            fName.contains(searchQuery, ignoreCase = true) ||
            lName.contains(searchQuery, ignoreCase = true) ||
            bPlace.contains(searchQuery, ignoreCase = true)
        }

        Text(
            text = "${t("Total found", "Ukupno pronađeno", "Всего найдено", "Insgesamt gefunden", "Total trouvé", currentLanguage)}: ${filtered.size} ${t("relatives", "srodnika", "родственников", "Verwandte", "parents", currentLanguage)}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = appColors.textSecondaryColor,
            fontFamily = appColors.fontFamily
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filtered) { member ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelect(member) },
                    colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
                    border = BorderStroke(1.dp, appColors.borderColor)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .background(
                                        if (member.gender == "MALE") Color(0xFF1E3A8A) else Color(0xFF881337),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (member.gender == "MALE") Icons.Default.Male else Icons.Default.Female,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Spacer(Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "${getLocalizedText(member.firstName, currentLanguage)} ${getLocalizedText(member.lastName, currentLanguage)}",
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textColor,
                                    fontFamily = appColors.fontFamily
                                )
                                Text(
                                    text = "${t("Born", "Rođen(a)", "Родился(лась)", "Geboren", "Né(e)", currentLanguage)}: ${member.birthDate ?: t("Unknown date", "Nepoznat", "Неизвестно", "Unbekannt", "Inconnu", currentLanguage)} ${t("in", "u", "в", "in", "à", currentLanguage)} ${getLocalizedText(member.birthPlace, currentLanguage)}",
                                    fontSize = 11.sp,
                                    color = appColors.textSecondaryColor,
                                    fontFamily = appColors.fontFamily
                                )
                            }
                        }
                        IconButton(onClick = { onEdit(member) }) {
                            Icon(Icons.Default.Edit, "Uredi", tint = appColors.accentColor, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }
    }
}

// Tab 2: Storyteller, AI, and Capsule Section
@Composable
fun StorytellerAndCapsuleSection(
    focusMember: FamilyMember?,
    allMembers: List<FamilyMember>,
    appColors: ThemeConfig
) {
    val coroutineScope = rememberCoroutineScope()
    var prompt by remember { mutableStateOf("") }
    var aiStory by remember { mutableStateOf("") }
    var isNarratorReading by remember { mutableStateOf(false) }
    var isStoryLoading by remember { mutableStateOf(false) }
    var showCollaborationSpace by remember { mutableStateOf(false) }
    
    // Audio Player states
    var audioProgress by remember { mutableStateOf(0.4f) }
    var isAudioPlaying by remember { mutableStateOf(false) }
    var photoIsRestoring by remember { mutableStateOf(false) }
    var isPhotoRestored by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (focusMember == null) {
            Text("Odaberite srodnika u stablu da biste obogatili njegovu vremensku kapsulu.", color = appColors.textSecondaryColor)
            return
        }

        // Section Title
        Text(
            "Vremenska Kapsula & AI Pripovedač",
            fontWeight = FontWeight.Bold,
            color = appColors.textColor,
            fontSize = 18.sp,
            fontFamily = appColors.fontFamily
        )

        // Ancestor Photo Colorization / Restore Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, appColors.borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "AI Restaurator slika predaka",
                    fontWeight = FontWeight.Bold,
                    color = appColors.textColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Koristite neuronske mreže da automatski izoštrite i unesete prelepe boje u oštećene fotografije predaka.",
                    fontSize = 11.sp,
                    color = appColors.textSecondaryColor
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Gray.copy(alpha = 0.2f))
                            .border(1.dp, appColors.borderColor, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            // Draws simulated split between black/white and colored profile
                            drawRect(
                                color = if (isPhotoRestored) Color(0xFFCE6A50) else Color.DarkGray,
                                size = size
                            )
                        }
                        if (photoIsRestoring) {
                            CircularProgressIndicator(color = appColors.accentColor, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                imageVector = if (isPhotoRestored) Icons.Default.FilterVintage else Icons.Default.PhotoCamera,
                                contentDescription = null,
                                tint = Color.White
                            )
                        }
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isPhotoRestored) "Fotografija uspešno obojena!" else "Stanje fotografije: Crno-bela (oštećena)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textColor
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                coroutineScope.launch {
                                    photoIsRestoring = true
                                    delay(2000)
                                    photoIsRestoring = false
                                    isPhotoRestored = true
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor),
                            enabled = !photoIsRestoring && !isPhotoRestored,
                            modifier = Modifier.fillMaxWidth().testTag("photo_restore_btn")
                        ) {
                            Text(if (isPhotoRestored) "Završeno" else "Ukloni oštećenja i oboj sliku", fontSize = 11.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // AI Storyteller generator directly leveraging model instructions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, appColors.borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoStories, null, tint = appColors.accentColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Umetnički AI Pripovedač",
                        fontWeight = FontWeight.Bold,
                        color = appColors.textColor,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    "Upišite nekoliko bazičnih reči ili ključnih sećanja (npr. gde je radio, anegdote), a AI će isplesti romansiranu biografsku audio priču.",
                    fontSize = 11.sp,
                    color = appColors.textSecondaryColor
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { Text("Primer: Bio je veseo, preživeo je rat, svirao harmoniku...", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth().testTag("ai_biography_prompt"),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = appColors.textColor, unfocusedTextColor = appColors.textColor)
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isStoryLoading = true
                            delay(1800)
                            aiStory = "U pitomom delu Šumadije, odakle loza ${focusMember.lastName} vuče korene, pripoveda se o vremenu kada je ${focusMember.firstName} koračao ovim prostorima. ${if (prompt.isNotBlank()) prompt else "Doneo je blagost u dom i osmeh svakoj duši koju srete."} Ostavio je neizbrisivo svedočanstvo o vrednosti, istrajnosti i ljubavi prema kućnom pragu. Generacije svedoče o pesmi vetra u voćnjaku i sećanju koje nikada neće uvenuti."
                            isStoryLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("ai_story_generate_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                ) {
                    if (isStoryLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text("Generiši Romansiranu Priču (AI)", color = Color.White)
                    }
                }

                if (aiStory.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Column {
                            Text(
                                aiStory,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = appColors.textColor,
                                lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(10.dp))
                            // Simulated Audiobook/Reader
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { isNarratorReading = !isNarratorReading }) {
                                        Icon(
                                            imageVector = if (isNarratorReading) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                                            contentDescription = null,
                                            tint = appColors.accentColor
                                        )
                                    }
                                    Text(
                                        if (isNarratorReading) "AI Glas čita..." else "Slušaj biografsku audio knjigu",
                                        fontSize = 11.sp,
                                        color = appColors.textSecondaryColor
                                    )
                                }
                                if (isNarratorReading) {
                                    CircularProgressIndicator(color = appColors.accentColor, modifier = Modifier.size(12.dp))
                                }
                            }
                        }
                    }
                }
            }
        }

        // Realtime Collaboration - Viber / Slack Space simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, appColors.borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.ChatBubbleOutline, null, tint = appColors.accentColor)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            "Porodični Ćošak (Zajednički rad)",
                            fontWeight = FontWeight.Bold,
                            color = appColors.textColor,
                            fontSize = 14.sp
                        )
                    }
                    Switch(
                        checked = showCollaborationSpace,
                        onCheckedChange = { showCollaborationSpace = it }
                    )
                }

                if (showCollaborationSpace) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Podelite link rođacima i tetkama. Svi mogu sa svojih telefona istovremeno da kače stare anegdote i ispravljaju podatke.",
                        fontSize = 11.sp,
                        color = appColors.textSecondaryColor
                    )
                    
                    Spacer(Modifier.height(8.dp))
                    Button(
                        onClick = {},
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.surfaceColor)
                    ) {
                        Text("Kopiraj pozivni link za familiju 🔗", color = appColors.textColor, fontSize = 11.sp)
                    }

                    Spacer(Modifier.height(12.dp))
                    Text("Istorijski Razgovori uživo (Simulirano):", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = appColors.textColor)
                    
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(8.dp)
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        ) {
                            ChatMessage(name = "Tetka Slavica (Frankfurt)", msg = "Pozdrav društvo! Slobodane, baka Milica je zapravo rođena 14. oktobra a ne 15! Imam skeniranu matičnu knjigu.", appColors = appColors)
                            ChatMessage(name = "Ujak Zoran (Niš)", msg = "Evo dodajem slike sa proslave njene 70. godišnjice u album drveta. Da li vidite?", appColors = appColors)
                            ChatMessage(name = "Strina Jelena", msg = "Prelepa aplikacija, od srca hvala što čuvamo dečiju prošlost.", appColors = appColors)
                        }
                    }
                }
            }
        }

        // Multimedia Audio Memoirs capsule simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, appColors.borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    "Snimci i Audio Dokumentacija",
                    fontWeight = FontWeight.Bold,
                    color = appColors.textColor,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Preslušajte sačuvane zvučne zapise dragih predaka, stare kasete koje su restaurirane i prebačene na oblak.",
                    fontSize = 11.sp,
                    color = appColors.textSecondaryColor
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Simulated player
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isAudioPlaying = !isAudioPlaying }) {
                        Icon(
                            imageVector = if (isAudioPlaying) Icons.Default.PauseCircle else Icons.Default.PlayCircle,
                            contentDescription = "Pusti zapis",
                            tint = appColors.accentColor,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Kazivanje prabake Jelene o staroj slavi",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            "Trajanje: 02:45 min",
                            fontSize = 10.sp,
                            color = appColors.textSecondaryColor
                        )
                        Spacer(Modifier.height(4.dp))
                        // Progress slider
                        LinearProgressIndicator(
                            progress = audioProgress,
                            color = appColors.accentColor,
                            trackColor = appColors.borderColor,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChatMessage(name: String, msg: String, appColors: ThemeConfig) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(4.dp)
    ) {
        Text(
            name,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
            color = appColors.accentColor
        )
        Text(
            msg,
            fontSize = 11.sp,
            color = appColors.textColor
        )
    }
}

// Tab 3: Migration Journey Geolocation Timelapse
@Composable
fun MigrationMapSection(
    focusMember: FamilyMember?,
    appColors: ThemeConfig
) {
    val coroutineScope = rememberCoroutineScope()
    var isPlaying by remember { mutableStateOf(false) }
    var currentYearIndex by remember { mutableStateOf(0) }
    
    val migrationSteps = listOf(
        MigrationPathNode("1912", "Topola, Srbija (Šumadija)", "Pradeda Čedomir otvara malo seosko pčelarstvo na padinama ispod Oplenca."),
        MigrationPathNode("1918", "Kragujevac, Srbija", "Prababa Jelena završava učiteljsku školu i donosi pismenost u seoske parohije."),
        MigrationPathNode("1945", "Valjevo, Srbija", "Porodica seli tokom obnove posle Drugog svetskog rata; deda Milan upisuje tehničku gimnaziju."),
        MigrationPathNode("1998", "Beograd, Srbija", "Slobodan se rađa u prestonici; stvara se digitalno ogranak porodice sa softverskim korenima."),
        MigrationPathNode("2018", "Frankfurt, Nemačka", "Stric Zoran odlazi za poslom softverskog inženjera, stvarajući prvu prekookeansku granu.")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            "Geografski Vremenski Lapsus (Migracije)",
            fontWeight = FontWeight.Bold,
            color = appColors.textColor,
            fontSize = 18.sp,
            fontFamily = appColors.fontFamily
        )
        Text(
            "Vizuelno kretanje i širenje porodice kroz vekove i kontinente. Kliknite na dugme 'Play' da pokrenete kinematografsku šetnju.",
            fontSize = 12.sp,
            color = appColors.textSecondaryColor
        )

        // Maps Canvas Drawing Board Simulation
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.Black.copy(alpha = 0.3f))
                .border(1.dp, appColors.borderColor, RoundedCornerShape(16.dp)),
            contentAlignment = Alignment.Center
        ) {
            // Simulated topographical lines
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background contours
                drawCircle(
                    color = appColors.accentColor.copy(alpha = 0.05f),
                    radius = 300f,
                    center = Offset(size.width / 2, size.height / 2)
                )
                drawCircle(
                    color = appColors.accentColor.copy(alpha = 0.03f),
                    radius = 500f,
                    center = Offset(size.width / 2, size.height / 2)
                )

                // Draw migration lines
                val points = listOf(
                    Offset(100f, 180f),
                    Offset(220f, 150f),
                    Offset(340f, 200f),
                    Offset(480f, 100f),
                    Offset(620f, 50f)
                )

                for (i in 0 until currentYearIndex) {
                    if (i + 1 <= currentYearIndex && i + 1 < points.size) {
                        drawLine(
                            color = appColors.accentColor,
                            start = points[i],
                            end = points[i + 1],
                            strokeWidth = 4f
                        )
                    }
                }

                // Draw coordinate bullets
                points.forEachIndexed { idx, p ->
                    if (idx <= currentYearIndex) {
                        drawCircle(
                            color = if (idx == currentYearIndex) Color.White else appColors.accentColor,
                            radius = if (idx == currentYearIndex) 12f else 8f,
                            center = p
                        )
                    }
                }
            }

            // Interactive Play Button Overlay
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        isPlaying = true
                        currentYearIndex = 0
                        while (currentYearIndex < migrationSteps.size - 1) {
                            delay(1500)
                            currentYearIndex++
                        }
                        isPlaying = false
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(48.dp)
                    .background(appColors.accentColor, CircleShape)
                    .testTag("play_migration_btn")
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = "Pokreni putovanje",
                    tint = Color.White
                )
            }

            // Coordinates readout text box overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                    .padding(8.dp)
            ) {
                Text(
                    "Prikaz: ${migrationSteps[currentYearIndex].godina}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }

        // Selected journey description
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, appColors.borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "${migrationSteps[currentYearIndex].godina}: ${migrationSteps[currentYearIndex].lokacija}",
                    fontWeight = FontWeight.Bold,
                    color = appColors.textColor,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = migrationSteps[currentYearIndex].opis,
                    fontSize = 12.sp,
                    color = appColors.textSecondaryColor
                )
            }
        }
    }
}

data class MigrationPathNode(
    val godina: String,
    val lokacija: String,
    val opis: String
)

// Styling helper config holder class
data class ThemeConfig(
    val bgBrush: Brush,
    val surfaceColor: Color,
    val textColor: Color,
    val textSecondaryColor: Color,
    val accentColor: Color,
    val borderColor: Color,
    val glowColor: Color,
    val fontFamily: FontFamily,
    val meshBlurEnabled: Boolean
)
