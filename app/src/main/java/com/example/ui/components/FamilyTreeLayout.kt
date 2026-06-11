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
import androidx.compose.ui.res.painterResource
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
    onAddStandaloneRoot: (firstName: String, lastName: String, gender: String, birthDate: String?, birthPlace: String?) -> Unit,
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
    var showShareDialog by remember { mutableStateOf(false) }

    // Theme Config Colors
    val appColors = when (currentTheme) {
        VisualTheme.FROSTED_GLASS -> ThemeConfig(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF0F062E), Color(0xFF2E0854), Color(0xFF03010E))),
            surfaceColor = Color(0xFF1E103E).copy(alpha = 0.8f),
            textColor = Color(0xFFFDF4FF),
            textSecondaryColor = Color(0xFFA78BFA),
            accentColor = Color(0xFFD946EF),
            borderColor = Color(0xFF8B5CF6).copy(alpha = 0.6f),
            glowColor = Color(0xFF00FFFF),
            fontFamily = FontFamily.SansSerif,
            meshBlurEnabled = true
        )
        VisualTheme.VINTAGE_PARCHMENT -> ThemeConfig(
            bgBrush = Brush.linearGradient(listOf(Color(0xFFFFF9E6), Color(0xFFF3E5C8), Color(0xFFDFD0B3))),
            surfaceColor = Color(0xFFEADBBE),
            textColor = Color(0xFF3B2314),
            textSecondaryColor = Color(0xFF9E2A2B),
            accentColor = Color(0xFFB45309),
            borderColor = Color(0xFFD97706),
            glowColor = Color(0xFFFBBF24),
            fontFamily = FontFamily.Serif,
            meshBlurEnabled = false
        )
        VisualTheme.NORDIJSKI_MINIMAL -> ThemeConfig(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF022C22))),
            surfaceColor = Color(0xFF065F46).copy(alpha = 0.85f),
            textColor = Color(0xFFF0FDF4),
            textSecondaryColor = Color(0xFF34D399),
            accentColor = Color(0xFF10B981),
            borderColor = Color(0xFF059669).copy(alpha = 0.6f),
            glowColor = Color(0xFF34D399),
            fontFamily = FontFamily.SansSerif,
            meshBlurEnabled = false
        )
        VisualTheme.CYBERPUNK -> ThemeConfig(
            bgBrush = Brush.verticalGradient(listOf(Color(0xFF450A0A), Color(0xFF2D0606), Color(0xFF180000))),
            surfaceColor = Color(0xFF7F1D1D).copy(alpha = 0.65f),
            textColor = Color(0xFFFFFBEB),
            textSecondaryColor = Color(0xFFFBBF24),
            accentColor = Color(0xFFF59E0B),
            borderColor = Color(0xFFFBBF24).copy(alpha = 0.7f),
            glowColor = Color(0xFFFFD700),
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
                            text = t("ROOT", "KOREN", "КОРЕНЬ", "WURZEL", "RACINE", currentLanguage),
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
                                    .width(290.dp)
                                    .background(appColors.surfaceColor)
                            ) {
                                DropdownMenuItem(
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.AddCircle,
                                            contentDescription = null,
                                            tint = appColors.accentColor
                                        )
                                    },
                                    text = {
                                        Column {
                                             Text(
                                                 t("👥 Start My Family (Clear All)", "👥 Moja Porodica (Započni sveže)", "👥 Начать мою семью (Очистить)", "👥 Meine eigene Familie (Löschen)", "👥 Ma propre famille (Effacer tout)", currentLanguage),
                                                 color = appColors.accentColor,
                                                 fontSize = 13.sp,
                                                 fontWeight = FontWeight.ExtraBold,
                                                 fontFamily = appColors.fontFamily
                                             )
                                             Text(
                                                 t("Erase demo data to build your own", "Uklonite primer i počnite svoje stablo", "Очистить демо-данные для создания своего", "Demo-Daten löschen und neu starten", "Effacer la démo pour créer votre arbre", currentLanguage),
                                                 color = appColors.textSecondaryColor,
                                                 fontSize = 10.sp,
                                                 fontFamily = appColors.fontFamily
                                             )
                                        }
                                    },
                                    onClick = {
                                        dynastyMenuExpanded = false
                                        onClearAll()
                                    }
                                )
                                HorizontalDivider(color = appColors.borderColor.copy(alpha = 0.4f))

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

                        // Social sharing button
                        IconButton(
                            onClick = { showShareDialog = true },
                            modifier = Modifier
                                .size(36.dp)
                                .background(appColors.surfaceColor, CircleShape)
                                .border(1.dp, appColors.borderColor, CircleShape)
                                .testTag("social_share_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = t("Share Family Tree", "Podeli Porodično Stablo", "Поделиться древом", "Stammbaum teilen", "Partager l'arbre", currentLanguage),
                                tint = appColors.accentColor,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Extremely Beautiful Horizontal Theme Selector (highly visible!)
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val themesList = listOf(
                        Pair(VisualTheme.FROSTED_GLASS, Triple("🌌 Cosmic Nebula", "🌌 Kosmička Nebula", "🌌 Космическая Туманность")),
                        Pair(VisualTheme.VINTAGE_PARCHMENT, Triple("📜 Epic Parchment", "📜 Carski Pergament", "📜 Эпический Пергамент")),
                        Pair(VisualTheme.NORDIJSKI_MINIMAL, Triple("🌲 Emerald Dynasty", "🌲 Smaragdno Carstvo", "🌲 Изумрудное Царство")),
                        Pair(VisualTheme.CYBERPUNK, Triple("👑 Royal Gold & Red", "👑 Kraljevski Rubin i Zlato", "👑 Королевский Рубин и Золото"))
                    )
                    
                    items(themesList) { (themeKey, labels) ->
                        val themeName = when (currentLanguage) {
                            "SRB" -> labels.second
                            "RUS" -> labels.third
                            else -> labels.first
                        }
                        val isSelected = currentTheme == themeKey
                        val chipBg = if (isSelected) appColors.accentColor else appColors.surfaceColor
                        val chipBorderColor = if (isSelected) appColors.glowColor else appColors.borderColor
                        val chipTextColor = if (isSelected) Color.White else appColors.textColor
                        
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(chipBg)
                                .border(1.2.dp, chipBorderColor, RoundedCornerShape(20.dp))
                                .clickable { currentTheme = themeKey }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = themeName,
                                color = chipTextColor,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                fontFamily = appColors.fontFamily
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
                                text = t(
                                    "Tree is currently empty",
                                    "Stablo je trenutno prazno",
                                    "Древо в данный момент пусто",
                                    "Stammbaum ist leer",
                                    "L'arbre est actuellement vide",
                                    currentLanguage
                                ),
                                color = appColors.textColor,
                                fontWeight = FontWeight.Bold,
                                fontFamily = appColors.fontFamily,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = t(
                                    "Start your digital family roots by adding the first member or loading a rich historical demo family tree.",
                                    "Pokrenite digitalni koren porodice dodavanjem prvog člana ili učitavanjem bogatog istorijskog demo stabla.",
                                    "Создайте цифровой корень вашей семьи, добавив первого члена или загрузив историческое демо-древо.",
                                    "Starten Sie Ihre digitalen Familienwurzeln, indem Sie das erste Mitglied hinzufügen oder einen historischen Demo-Stammbaum laden.",
                                    "Démarrez vos racines familiales numériques en ajoutant le premier membre ou en chargeant un arbre historique de démonstration.",
                                    currentLanguage
                                ),
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
                                    Text(
                                        text = t(
                                            "Load Demo Tree",
                                            "Učitaj Demo Stablo",
                                            "Загрузить демо-древо",
                                            "Demo-Baum laden",
                                            "Charger l'arbre démo",
                                            currentLanguage
                                        ),
                                        color = Color.White,
                                        fontFamily = appColors.fontFamily
                                    )
                                }
                                OutlinedButton(
                                    onClick = { showAddStandaloneDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textColor)
                                ) {
                                    Text(
                                        text = t(
                                            "Add Manually",
                                            "Dodaj Ručno",
                                            "Добавить вручную",
                                            "Manuell hinzufügen",
                                            "Ajouter manuellement",
                                            currentLanguage
                                        ),
                                        fontFamily = appColors.fontFamily
                                    )
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
                val relTypeStr = when (relationshipType.uppercase()) {
                    "FATHER" -> t("Father", "Otac", "Отец", "Vater", "Père", currentLanguage)
                    "MOTHER" -> t("Mother", "Majka", "Мать", "Mutter", "Mère", currentLanguage)
                    "SPOUSE" -> t("Spouse", "Supružnik", "Супруг(а)", "Ehepartner", "Époux(se)", currentLanguage)
                    "CHILD" -> t("Child", "Dete", "Ребенок", "Kind", "Enfant", currentLanguage)
                    "SIBLING" -> t("Sibling", "Brat/Sestra", "Брат/Сестра", "Geschwister", "Frère/Sœur", currentLanguage)
                    else -> relationshipType
                }
                Text(
                    text = "${t("Add Relative", "Dodaj Srodnika", "Добавить родственника", "Verwandten hinzufügen", "Ajouter un parent", currentLanguage)}: $relTypeStr ${t("for member", "za člana", "для члена", "für Mitglied", "pour le membre", currentLanguage)} ${pivotPerson?.firstName}",
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
                        label = { Text(t("First Name *", "Ime *", "Имя *", "Vorname *", "Prénom *", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("input_fname")
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(t("Last Name *", "Prezime *", "Фамилия *", "Nachname *", "Nom de famille *", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth().testTag("input_lname")
                    )
                    
                    // Gender selection
                    Text(t("Gender:", "Pol:", "Пол:", "Geschlecht:", "Genre:", currentLanguage), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = appColors.fontFamily)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "MALE", onClick = { gender = "MALE" })
                            Text(t("Male", "Muški", "Мужской", "Männlich", "Masculin", currentLanguage), modifier = Modifier.clickable { gender = "MALE" }, fontFamily = appColors.fontFamily)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "FEMALE", onClick = { gender = "FEMALE" })
                            Text(t("Female", "Ženski", "Женский", "Weiblich", "Féminin", currentLanguage), modifier = Modifier.clickable { gender = "FEMALE" }, fontFamily = appColors.fontFamily)
                        }
                    }

                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text(t("Birth Date (e.g. 12.06.1950)", "Datum rođenja (npr. 12.06.1950)", "Дата рождения (напр. 12.06.1950)", "Geburtsdatum (z.B. 12.06.1950)", "Date de naissance (ex. 12.06.1950)", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = { birthPlace = it },
                        label = { Text(t("Birth Place (e.g. London, UK)", "Mesto rođenja (npr. Čačak, Srbija)", "Место рождения (напр. Москва, РФ)", "Geburtsort (z.B. Berlin, Deutschland)", "Lieu de naissance (ex. Paris, France)", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deathDate,
                        onValueChange = { deathDate = it },
                        label = { Text(t("Death Date (if deceased)", "Datum smrti (ako je preminuo)", "Дата смерти (если умер)", "Todesdatum (falls verstorben)", "Date de décès (si décédé)", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deathPlace,
                        onValueChange = { deathPlace = it },
                        label = { Text(t("Death Place", "Mesto smrti", "Место смерти", "Todesort", "Lieu de décès", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = biography,
                        onValueChange = { biography = it },
                        label = { Text(t("Short memories and biography", "Kratka sećanja i biografija", "Краткие воспоминания и биография", "Kurze Erinnerungen und Biografie", "Souvenirs courts et biographie", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 4
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text(t("Phone", "Telefon", "Телефон", "Telefon", "Téléphone", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(t("Email (will be used for collaboration)", "Email (koristiće se za saradnju)", "Email (будет использоваться для сотрудничества)", "E-Mail (wird für Zusammenarbeit verwendet)", "E-mail (sera utilisé pour la collaboration)", currentLanguage)) },
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
                    Text(t("Save", "Sačuvaj", "Сохранить", "Speichern", "Enregistrer", currentLanguage), color = Color.White, fontFamily = appColors.fontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = null }) {
                    Text(t("Cancel", "Otkaži", "Отмена", "Abbrechen", "Annuler", currentLanguage), fontFamily = appColors.fontFamily)
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
                    text = "${t("Edit Profile", "Izmeni Profil", "Редактировать профиль", "Profil bearbeiten", "Modifier le profil", currentLanguage)}: ${memberToEdit.firstName} ${memberToEdit.lastName}",
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
                        label = { Text(t("First Name", "Ime", "Имя", "Vorname", "Prénom", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(t("Last Name", "Prezime", "Фамилия", "Nachname", "Nom de famille", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text(t("Birth Date", "Datum rođenja", "Дата рождения", "Geburtsdatum", "Date de naissance", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = { birthPlace = it },
                        label = { Text(t("Birth Place", "Mesto rođenja", "Место рождения", "Geburtsort", "Lieu de naissance", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deathDate,
                        onValueChange = { deathDate = it },
                        label = { Text(t("Death Date", "Datum smrti", "Дата смерти", "Todesdatum", "Date de décès", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = deathPlace,
                        onValueChange = { deathPlace = it },
                        label = { Text(t("Death Place", "Mesto smrti", "Место смерти", "Todesort", "Lieu de décès", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = biography,
                        onValueChange = { biography = it },
                        label = { Text(t("Detailed Biography & Notes", "Detaljna Biografija & Beleške", "Подробная биография и заметки", "Detaillierte Biografie & Notizen", "Biographie détaillée & Notes", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 5
                    )
                    OutlinedTextField(
                        value = phoneNumber,
                        onValueChange = { phoneNumber = it },
                        label = { Text(t("Phone", "Telefon", "Телефон", "Telefon", "Téléphone", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text(t("Email Address", "Email adresa", "Адрес эл. почты", "E-Mail-Adresse", "Adresse e-mail", currentLanguage)) },
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
                    Text(t("Save Changes", "Sačuvaj Promene", "Сохранить изменения", "Änderungen speichern", "Enregistrer les modifications", currentLanguage), color = Color.White, fontFamily = appColors.fontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = null }) {
                    Text(t("Discard", "Odbaci", "Сбросить", "Verwerfen", "Abandonner", currentLanguage), fontFamily = appColors.fontFamily)
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
            title = {
                Text(
                    text = t(
                        "Set First Ancestor (Root)",
                        "Postavi Prvog Pretka (Koren)",
                        "Установить первого предка (Корень)",
                        "Ersten Vorfahren festlegen (Wurzel)",
                        "Définir le premier ancêtre (Racine)",
                        currentLanguage
                    ),
                    fontWeight = FontWeight.Bold,
                    fontFamily = appColors.fontFamily
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = firstName,
                        onValueChange = { firstName = it },
                        label = { Text(t("First Name", "Ime", "Имя", "Vorname", "Prénom", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = lastName,
                        onValueChange = { lastName = it },
                        label = { Text(t("Last Name", "Prezime", "Фамилия", "Nachname", "Nom de famille", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(t("Gender:", "Pol:", "Пол:", "Geschlecht:", "Genre:", currentLanguage), style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, fontFamily = appColors.fontFamily)
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "MALE", onClick = { gender = "MALE" })
                            Text(t("Male", "Muški", "Мужской", "Männlich", "Masculin", currentLanguage), modifier = Modifier.clickable { gender = "MALE" }, fontFamily = appColors.fontFamily)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = gender == "FEMALE", onClick = { gender = "FEMALE" })
                            Text(t("Female", "Ženski", "Женский", "Weiblich", "Féminin", currentLanguage), modifier = Modifier.clickable { gender = "FEMALE" }, fontFamily = appColors.fontFamily)
                        }
                    }
                    OutlinedTextField(
                        value = birthDate,
                        onValueChange = { birthDate = it },
                        label = { Text(t("Birth Date", "Datum rođenja", "Дата рождения", "Geburtsdatum", "Date de naissance", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = birthPlace,
                        onValueChange = { birthPlace = it },
                        label = { Text(t("Birth Place", "Mesto rođenja", "Место рождения", "Geburtsort", "Lieu de naissance", currentLanguage)) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (firstName.isNotBlank() && lastName.isNotBlank()) {
                            // Link into root via action
                            onAddStandaloneRoot(
                                firstName, 
                                lastName, 
                                gender, 
                                birthDate.ifBlank { null }, 
                                birthPlace.ifBlank { null }
                            )
                            showAddStandaloneDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                ) {
                    Text(t("Create Root", "Napravi Koren", "Создать корень", "Wurzel erstellen", "Créer la racine", currentLanguage), color = Color.White, fontFamily = appColors.fontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddStandaloneDialog = false }) {
                    Text(t("Cancel", "Poništi", "Отмена", "Abbrechen", "Annuler", currentLanguage), fontFamily = appColors.fontFamily)
                }
            }
        )
    }

    // Print On Demand Wizard dialog
    if (showPrintDialog) {
        var printOptionSelected by remember { mutableStateOf(0) } // 0: Luksuzna Knjiga, 1: Poster
        var sizeSelected by remember { mutableStateOf("A1 (Veliki Poster) - €29") }
        var leatherColorSelected by remember { mutableStateOf("Tamno Crvena sa zlatorezom") }
        var generatedStatus by remember { mutableStateOf<String?>(null) }
        var orderComplete by remember { mutableStateOf(false) }

        AlertDialog(
            onDismissRequest = { 
                showPrintDialog = false
                orderComplete = false
                generatedStatus = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocalPrintshop, null, tint = appColors.accentColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = t(
                            "Magic Button: Print Heritage",
                            "Magično Dugme: Štampaj Nasleđe",
                            "Волшебная кнопка: Печать наследия",
                            "Magischer Button: Erbe drucken",
                            "Bouton magique: Imprimer l'héritage",
                            currentLanguage
                        ),
                        fontWeight = FontWeight.Bold,
                        fontFamily = appColors.fontFamily
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = t(
                            "In partnership with local art print shops, transform your digital tree into beautiful physical heritage gifts in one click.",
                            "U partnerstvu sa lokalnim umetničkim štamparijama, jednim klikom pretvorite vaše digitalno stablo u prelepe fizičke poklone.",
                            "В партнерстве с местными типографиями превратите свое цифровое древо в прекрасные физические подарки в один клик.",
                            "In Partnerschaft mit lokalen Kunstdruckereien verwandeln Sie Ihren digitalen Baum mit einem Klick in wunderschöne physische Geschenke.",
                            "En partenariat avec des imprimeries d'art locales, transformez votre arbre numérique en de magnifiques cadeaux physiques en un clic.",
                            currentLanguage
                        ),
                        fontSize = 12.sp,
                        color = appColors.textSecondaryColor,
                        fontFamily = appColors.fontFamily
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
                            Text(
                                text = t(
                                    "Leather Book",
                                    "Kožna Knjiga",
                                    "Кожаная книга",
                                    "Lederbuch",
                                    "Livre en cuir",
                                    currentLanguage
                                ),
                                fontFamily = appColors.fontFamily
                            )
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
                            Text(
                                text = t(
                                    "Wall Poster",
                                    "Zidni Poster",
                                    "Настенный постер",
                                    "Wandposter",
                                    "Affiche murale",
                                    currentLanguage
                                ),
                                fontFamily = appColors.fontFamily
                            )
                        }
                    }

                    if (printOptionSelected == 0) {
                        // Leather book config
                        Text(
                            text = t(
                                "Book Configuration",
                                "Konfiguracija Knjige",
                                "Конфигурация книги",
                                "Buchkonfiguration",
                                "Configuration du livre",
                                currentLanguage
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = appColors.fontFamily
                        )
                        
                        val bookStyles = listOf(
                            Triple(
                                "Tamno Crvena sa zlatorezom",
                                "Dark Red with gold leaf",
                                "Dark Red with gold leaf || Tamno Crvena sa zlatorezom || Темно-красный с золотым тиснением || Dunkelrot mit Goldschnitt || Rouge foncé avec dorure"
                            ),
                            Triple(
                                "Kraljevski Plava sa srebrnim vezom",
                                "Royal Blue with silver embroidery",
                                "Royal Blue with silver embroidery || Kraljevski Plava sa srebrnim vezom || Королевский синий с серебряной вышивкой || Königsblau mit Silberstickerei || Bleu royal avec broderie d'argent"
                            ),
                            Triple(
                                "Arhivska Smeđa koža (Vintage)",
                                "Archival Brown leather (Vintage)",
                                "Archival Brown leather (Vintage) || Arhivska Smeđa koža (Vintage) || Архивная коричневая кожа (Винтаж) || Archiv-Ziegenleder Braun (Vintage) || Cuir marron d'archive (Vintage)"
                            )
                        )
                        bookStyles.forEach { styleTriple ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { leatherColorSelected = styleTriple.first }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = leatherColorSelected == styleTriple.first,
                                    onClick = { leatherColorSelected = styleTriple.first }
                                )
                                Text(
                                    text = getLocalizedText(styleTriple.third, currentLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = appColors.textColor,
                                    fontFamily = appColors.fontFamily
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "${t("Content", "Sadržina", "Содержание", "Inhalt", "Contenu", currentLanguage)}:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            fontFamily = appColors.fontFamily
                        )
                        Text(
                            text = t(
                                "- Generates up to ${allMembers.size} relative profiles with photos\n- Includes all gathered family stories and notes\n- Luxury hardcover with a family crest foil stamp.",
                                "- Generiše do ${allMembers.size} profila srodnika sa fotografijama\n- Uključuje sve sakupljene porodične priče i crtice\n- Luksuzni tvrdi povez sa porodičnim gerbom.",
                                "- Создает до ${allMembers.size} профилей родственников с фотографиями\n- Включает все собранные семейные истории и заметки\n- Роскошный твердый переплет с тиснением фамильного герба.",
                                "- Generiert bis zu ${allMembers.size} Verwandtenprofile mit Fotos\n- Enthält alle gesammelten Familiengeschichten und Notizen\n- Luxuriöser Hardcover-Einband mit Familienwappen-Prägung.",
                                "- Génère jusqu'à ${allMembers.size} profils de parents avec photos\n- Comprend toutes les histoires et notes familiales recueillies\n- Couverture rigide de luxe avec gaufrage des armoiries familiales.",
                                currentLanguage
                            ),
                            fontSize = 11.sp,
                            color = appColors.textSecondaryColor,
                            fontFamily = appColors.fontFamily
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
                                Text(
                                    text = "${t("Price", "Cena", "Цена", "Preis", "Prix", currentLanguage)}: €59.00",
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textColor,
                                    fontFamily = appColors.fontFamily
                                )
                                Text(
                                    text = t("Free delivery", "Dostava besplatna", "Бесплатная доставка", "Kostenlose Lieferung", "Livraison gratuite", currentLanguage),
                                    fontSize = 10.sp,
                                    color = appColors.textSecondaryColor,
                                    fontFamily = appColors.fontFamily
                                )
                            }
                            Text(
                                text = "${t("Profit margin", "Marža zarada", "Маржа прибыли", "Gewinnspanne", "Marge bénéficiaire", currentLanguage)}: 45%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981),
                                fontFamily = appColors.fontFamily
                            )
                        }
                    } else {
                        // Poster config
                        Text(
                            text = t(
                                "Poster Configuration",
                                "Konfiguracija Postera",
                                "Конфигурация постера",
                                "Posterkonfiguration",
                                "Configuration de l'affiche",
                                currentLanguage
                            ),
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            fontFamily = appColors.fontFamily
                        )
                        
                        val sizes = listOf(
                            Triple("A1 (Veliki Poster) - €29", "A1 (Large Poster) - €29", "A1 (Large Poster) - €29 || A1 (Veliki Poster) - €29 || A1 (Большой постер) - €29 || A1 (Großes Poster) - €29 || A1 (Grande affiche) - €29"),
                            Triple("A2 (Srednji Poster) - €19", "A2 (Medium Poster) - €19", "A2 (Medium Poster) - €19 || A2 (Srednji Poster) - €19 || A2 (Средний постер) - €19 || A2 (Mittleres Poster) - €19 || A2 (Affiche moyenne) - €19"),
                            Triple("B1 Premijum - €39", "B1 Premium - €39", "B1 Premium - €39 || B1 Premijum - €39 || B1 Премиум - €39 || B1 Premium - €39 || B1 Premium - €39")
                        )
                        sizes.forEach { sizeTriple ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { sizeSelected = sizeTriple.first }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = sizeSelected == sizeTriple.first,
                                    onClick = { sizeSelected = sizeTriple.first }
                                )
                                Text(
                                    text = getLocalizedText(sizeTriple.third, currentLanguage),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = appColors.textColor,
                                    fontFamily = appColors.fontFamily
                                )
                            }
                        }

                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = t(
                                "The poster is printed on 250g ultra-matte hot-pressed paper with a detailed vector display of the tree and branches. A perfect custom decoration for your living room.",
                                "Poster se štampa na 250g ultra-mat vrelom papiru sa detaljnim vektorskim prikazom stabla i grana. Savršen ukras za dnevnu sobu porodice.",
                                "Постер печатается на ультраматовой бумаге плотностью 250 г горячего прессования с детальным векторным отображением дерева и ветвей. Идеальное украшение для гостиной.",
                                "Das Poster wird auf supermattes 250g-Heißpresspapier mit detaillierter Vektordarstellung des Stammbaums und der Äste gedruckt. Eine perfekte Dekoration für das Wohnzimmer.",
                                "L'affiche est imprimée sur du papier ultra-mat de 250g pressé à chaud avec un tracé vectoriel détaillé de l'arbre et des branches. Une décoration parfaite pour le salon.",
                                currentLanguage
                            ),
                            fontSize = 11.sp,
                            color = appColors.textSecondaryColor,
                            fontFamily = appColors.fontFamily
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
                                val cleanSelectedSize = sizeSelected.replace("A1 (Veliki Poster)", t("Large", "Veliki", "Большой", "Groß", "Grand", currentLanguage))
                                    .replace("A2 (Srednji Poster)", t("Medium", "Srednji", "Средний", "Mittel", "Moyen", currentLanguage))
                                    .replace("B1 Premijum", t("Premium", "Premijum", "Премиум", "Premium", "Premium", currentLanguage))
                                Text(
                                    text = "${t("Price", "Cena", "Цена", "Preis", "Prix", currentLanguage)}: $cleanSelectedSize",
                                    fontWeight = FontWeight.Bold,
                                    color = appColors.textColor,
                                    fontFamily = appColors.fontFamily
                                )
                                Text(
                                    text = t("Print + shipping in hard roll", "Štampa + kartonski cilindar dostava", "Печать + доставка в картонном тубусе", "Druck + Versand in stabiler Rolle", "Impression + livraison en tube carton", currentLanguage),
                                    fontSize = 10.sp,
                                    color = appColors.textSecondaryColor,
                                    fontFamily = appColors.fontFamily
                                )
                            }
                            Text(
                                text = "${t("Net profit margin", "Naša neto zarada", "Наша чистая прибыль", "Netto-Marge", "Marge bénéficiaire nette", currentLanguage)}: 55%",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF10B981),
                                fontFamily = appColors.fontFamily
                            )
                        }
                    }

                    if (generatedStatus != null) {
                        val localizedStatus = when {
                            generatedStatus!!.contains("Sastavljanje") -> t("Assembling tree elements...", "Sastavljanje elemenata stabla...", "Сборка элементов древа...", "Stammbaumelemente zusammenfügen...", "Assemblage des éléments de l'arbre...", currentLanguage)
                            generatedStatus!!.contains("PDF") -> t("Creating beautiful PDF layout...", "Kreiranje prelepog PDF preloma...", "Создание красивого макета PDF...", "Erstelle ein schönes PDF-Layout...", "Création d'une belle mise en page PDF...", currentLanguage)
                            generatedStatus!!.contains("štampariji") -> t("Sending order to partner print shop...", "Šaljem nalog partnerskoj štampariji...", "Отправка заказа партнерской типографии...", "Sende Auftrag an Partnerdruckerei...", "Envoi de la commande à l'imprimerie partenaire...", currentLanguage)
                            generatedStatus!!.contains("Naručeno") -> t("Ordered! The product is going to production and will arrive at your doorstep in 3-5 business days! Thank you for your trust.", "Naručeno! Proizvod kreće u izradu i biće na kućnom pragu za 3-5 radnih dana! Hvala na poverenju.", "Заказано! Продукт отправлен в производство и будет у ваших дверей через 3-5 рабочих дней! Спасибо за доверие.", "Bestellt! Das Produkt geht in Produktion und wird in 3-5 Werktagen bei Ihnen eintreffen! Vielen Dank für Ihr Vertrauen.", "Commandé! Le produit part en fabrication et arrivera chez vous sous 3 à 5 jours ouvrés! Merci de votre confiance.", currentLanguage)
                            else -> generatedStatus!!
                        }
                        Text(
                            text = localizedStatus,
                            fontWeight = FontWeight.Bold,
                            color = appColors.accentColor,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                            fontFamily = appColors.fontFamily
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
                        Text(
                            text = t(
                                "Simulate Order (€)",
                                "Simuliraj porudžbinu (€)",
                                "Имитировать заказ (€)",
                                "Bestellung simulieren (€)",
                                "Simuler la commande (€)",
                                currentLanguage
                            ),
                            color = Color.White,
                            fontFamily = appColors.fontFamily
                        )
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
                        Text(
                            text = t(
                                "Finish",
                                "Završi",
                                "Завершить",
                                "Abschließen",
                                "Terminer",
                                currentLanguage
                            ),
                            color = Color.White,
                            fontFamily = appColors.fontFamily
                        )
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { 
                    showPrintDialog = false
                    orderComplete = false
                    generatedStatus = null
                }) {
                    Text(
                        text = t(
                            "Otkaži",
                            "Otkaži",
                            "Отмена",
                            "Abbrechen",
                            "Annuler",
                            currentLanguage
                        ),
                        fontFamily = appColors.fontFamily
                    )
                }
            }
        )
    }

    // Social Sharing Wizard Modal
    if (showShareDialog) {
        val shareContext = LocalContext.current
        var shareMessageText by remember(allMembers.size, currentLanguage) {
            mutableStateOf(
                t(
                    "Gradim digitalno porodično stablo sa ${allMembers.size} članova na prelepoj KOREN (ROOT) aplikaciji! Pogledajte naše autentično porodično nasleđe: 🌳✨",
                    "Gradim digitalno porodično stablo sa ${allMembers.size} članova na prelepoj KOREN (ROOT) aplikaciji! Pogledajte naše autentično porodično nasleđe: 🌳✨",
                    "Я строю цифровое семейное древо из ${allMembers.size} участников в приложении KOREN! Посмотрите на наше семейное наследие: 🌳✨",
                    "Ich baue einen digitalen Stammbaum mit ${allMembers.size} Mitgliedern in der KOREN-App! Schau dir unser Familienerbe an: 🌳✨",
                    "Je construis un arbre généalogique numérique avec ${allMembers.size} membres sur l'application KOREN! Découvrez notre héritage: 🌳✨",
                    currentLanguage
                )
            )
        }

        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Share, null, tint = appColors.accentColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = t(
                            "Share Family Heritage",
                            "Podeli Porodično Nasleđe",
                            "Поделиться наследием",
                            "Familien-Erbe teilen",
                            "Partager l'héritage",
                            currentLanguage
                        ),
                        fontWeight = FontWeight.Bold,
                        fontFamily = appColors.fontFamily,
                        color = appColors.textColor
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = t(
                            "Customize your sharing message before posting to social networks:",
                            "Prilagodite poruku za deljenje pre nego što je pošaljete na društvene mreže:",
                            "Настройте сообщение перед публикацией в социальных сетях:",
                            "Passe deine Nachricht an, bevor du sie in sozialen Netzwerken teilst:",
                            "Personnalisez votre message avant de le partager sur les réseaux sociaux :",
                            currentLanguage
                        ),
                        fontSize = 12.sp,
                        color = appColors.textColor.copy(alpha = 0.8f),
                        fontFamily = appColors.fontFamily
                    )

                    OutlinedTextField(
                        value = shareMessageText,
                        onValueChange = { shareMessageText = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(
                            color = appColors.textColor,
                            fontFamily = appColors.fontFamily
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = appColors.surfaceColor.copy(alpha = 0.4f),
                            unfocusedContainerColor = appColors.surfaceColor.copy(alpha = 0.2f),
                            focusedBorderColor = appColors.accentColor,
                            unfocusedBorderColor = appColors.borderColor
                        )
                    )

                    Text(
                        text = t(
                            "Select Social App to Send:",
                            "Izaberite aplikaciju za slanje:",
                            "Выберите приложение для отправки:",
                            "Wähle eine App zum Senden:",
                            "Sélectionnez l'application pour envoyer :",
                            currentLanguage
                        ),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = appColors.textColor,
                        fontFamily = appColors.fontFamily
                    )

                    // Responsive share triggers
                    val sharePlatforms = listOf(
                        SharePlatform("WhatsApp", "com.whatsapp", Color(0xFF25D366), "💬"),
                        SharePlatform("Viber", "com.viber.voip", Color(0xFF7360F2), "💜"),
                        SharePlatform("Facebook", "com.facebook.katana", Color(0xFF1877F2), "🔵"),
                        SharePlatform("Instagram", "com.instagram.android", Color(0xFFE1306C), "📸"),
                        SharePlatform("TikTok", "com.zhiliaoapp.musically", Color(0xFF010101), "🎵"),
                        SharePlatform("Twitter / X", "com.twitter.android", Color(0xFF000000), "🐦")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        sharePlatforms.chunked(2).forEach { rowChips ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowChips.forEach { chip ->
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(chip.color)
                                            .border(1.5.dp, if (chip.color == Color.Black) Color(0xFF00FFFF) else Color.Transparent, RoundedCornerShape(12.dp))
                                            .clickable {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                    type = "text/plain"
                                                    putExtra(android.content.Intent.EXTRA_TEXT, "$shareMessageText\n\nhttps://ais-pre-unpxhz2ytcok4ytvofrjfq-247852197987.europe-west2.run.app")
                                                    setPackage(chip.packageId)
                                                }
                                                try {
                                                    shareContext.startActivity(intent)
                                                } catch (e: Exception) {
                                                    // Fallback to system share chooser
                                                    val fallback = android.content.Intent.createChooser(
                                                        android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                                            type = "text/plain"
                                                            putExtra(android.content.Intent.EXTRA_TEXT, "$shareMessageText\n\nhttps://ais-pre-unpxhz2ytcok4ytvofrjfq-247852197987.europe-west2.run.app")
                                                        },
                                                        chip.name
                                                    )
                                                    shareContext.startActivity(fallback)
                                                }
                                            }
                                            .padding(vertical = 10.dp, horizontal = 12.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(chip.emoji, fontSize = 16.sp)
                                            Text(
                                                text = chip.name,
                                                color = Color.White,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                fontFamily = appColors.fontFamily
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // General System Share
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(appColors.accentColor)
                                .clickable {
                                    val sendIntent = android.content.Intent().apply {
                                        action = android.content.Intent.ACTION_SEND
                                        putExtra(android.content.Intent.EXTRA_TEXT, "$shareMessageText\n\nhttps://ais-pre-unpxhz2ytcok4ytvofrjfq-247852197987.europe-west2.run.app")
                                        type = "text/plain"
                                    }
                                    val chooser = android.content.Intent.createChooser(sendIntent, t("Share via...", "Podeli preko...", "Поделиться через...", "Teilen über...", "Partager de...", currentLanguage))
                                    shareContext.startActivity(chooser)
                                }
                                .padding(vertical = 12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(Icons.Default.Share, null, tint = Color.White, modifier = Modifier.size(16.dp))
                                Text(
                                    text = t("More Share Options (System)", "Sve Druge Opcije (Sistemski)", "Другие опции (Системный)", "Andere Optionen", "Plus options", currentLanguage),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = appColors.fontFamily
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showShareDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                ) {
                    Text(
                        text = t("Close", "Zatvori", "Закрыть", "Schließen", "Fermer", currentLanguage),
                        color = Color.White,
                        fontFamily = appColors.fontFamily
                    )
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
    val currentLanguage = LocalLanguage.current
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
                text = t("Interactive Branches", "Interaktivne Grane", "Интерактивные ветви", "Interaktive Zweige", "Branches interactives", currentLanguage),
                fontWeight = FontWeight.Bold,
                color = appColors.textColor,
                fontSize = 16.sp,
                fontFamily = appColors.fontFamily
            )
            
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                TextButton(onClick = onResetDemo) {
                    Text(t("Load Demo", "Učitaj demo", "Загрузить демо", "Demo laden", "Charger démo", currentLanguage), fontSize = 11.sp, color = appColors.accentColor)
                }
                TextButton(onClick = onClearAll) {
                    Text(t("Clear All", "Isprazni", "Очистить всё", "Alles löschen", "Tout effacer", currentLanguage), fontSize = 11.sp, color = appColors.textSecondaryColor)
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
                text = t(
                    "1. GENERATION (Ancestors & Roots)",
                    "1. GENERACIJA (Preci i Koreni)",
                    "1. ПОКОЛЕНИЕ (Предки и корни)",
                    "1. GENERATION (Vorfahren & Wurzeln)",
                    "1ère GÉNÉRATION (Ancêtres & Racines)",
                    currentLanguage
                ),
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
                    Text(
                        text = t("Paternal line", "Očeva loza", "Отцовская линия", "Väterliche Linie", "Ligne paternelle", currentLanguage),
                        fontSize = 8.sp,
                        color = appColors.textSecondaryColor
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniMemberNode(
                            member = patGrandfather,
                            placeholderLabel = t("Grandfather +", "Deda +", "Дедушка +", "Großvater +", "Grand-père +", currentLanguage),
                            onSelect = { onSetFocus(it) },
                            onAdd = { onAddRelative(father?.id ?: focusMember.id, "FATHER") },
                            appColors = appColors
                        )
                        MiniMemberNode(
                            member = patGrandmother,
                            placeholderLabel = t("Grandmother +", "Baba +", "Бабушка +", "Großmutter +", "Grand-mère +", currentLanguage),
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
                    Text(
                        text = t("Maternal line", "Majčina loza", "Материнская линия", "Mütterliche Linie", "Ligne maternelle", currentLanguage),
                        fontSize = 8.sp,
                        color = appColors.textSecondaryColor
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        MiniMemberNode(
                            member = matGrandfather,
                            placeholderLabel = t("Grandfather +", "Deda +", "Дедушка +", "Großvater +", "Grand-père +", currentLanguage),
                            onSelect = { onSetFocus(it) },
                            onAdd = { onAddRelative(mother?.id ?: focusMember.id, "FATHER") },
                            appColors = appColors
                        )
                        MiniMemberNode(
                            member = matGrandmother,
                            placeholderLabel = t("Grandmother +", "Baba +", "Бабушка +", "Großmutter +", "Grand-mère +", currentLanguage),
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
                    text = t(
                        "2. GENERATION (Parents)",
                        "2. GENERACIJA (Roditelji)",
                        "2. ПОКОЛЕНИЕ (Родители)",
                        "2. GENERATION (Eltern)",
                        "2ème GÉNÉRATION (Parents)",
                        currentLanguage
                    ),
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
                    role = t("Father", "Otac", "Отец", "Vater", "Père", currentLanguage),
                    placeholderLabel = t("Father +", "Otac +", "Отец +", "Vater +", "Père +", currentLanguage),
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
                    role = t("Mother", "Majka", "Мать", "Mutter", "Mère", currentLanguage),
                    placeholderLabel = t("Mother +", "Majka +", "Мать +", "Mutter +", "Mère +", currentLanguage),
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
                text = t(
                    "3. GENERATION (Active Branch)",
                    "3. GENERACIJA (Aktivno Koleno)",
                    "3. ПОКОЛЕНИЕ (Активная ветвь)",
                    "3. GENERATION (Aktiver Zweig)",
                    "3ème GÉNÉRATION (Branche active)",
                    currentLanguage
                ),
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
                        text = "${t("Focused Person", "Fokusirana Osoba", "Фокус-персона", "Fokusierte Person", "Personne focalisée", currentLanguage)} 🎯",
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
                                text = t("Spouse", "Suprug(a)", "Супруг(а)", "Ehepartner", "Époux(se)", currentLanguage),
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
                            text = t("Add spouse", "Dodaj supružnika", "Добавить супруга", "Ehepartner hinzufügen", "Ajouter un époux/se", currentLanguage),
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
                    text = t("SIBLINGS", "BRAĆA I SESTRE", "БРАТЬЯ И СЕСТРЫ", "GESCHWISTER", "FRÈRES & SŒURS", currentLanguage),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
                )
                Spacer(Modifier.height(8.dp))
                if (siblings.isEmpty()) {
                    Text(
                        text = t("No siblings added", "Nema unete braće/sestara", "Нет добавленных братьев/сестер", "Keine Geschwister hinzugefügt", "Aucun frère/sœur ajouté", currentLanguage),
                        fontSize = 10.sp,
                        color = appColors.textSecondaryColor,
                        fontStyle = FontStyle.Italic
                    )
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
                    text = t("DESCENDANTS (Children)", "POTOMCI (Deca)", "ПОТОМКИ (Дети)", "NACHKOMMEN (Kinder)", "DESCENDANTS (Enfants)", currentLanguage),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
                )
                Spacer(Modifier.height(8.dp))
                if (children.isEmpty()) {
                    Text(
                        text = t("No children added", "Nema unete dece", "Нет добавленных детей", "Keine Kinder hinzugefügt", "Aucun enfant ajouté", currentLanguage),
                        fontSize = 10.sp,
                        color = appColors.textSecondaryColor,
                        fontStyle = FontStyle.Italic
                    )
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
    val currentLanguage = LocalLanguage.current
    
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

    // World Era Context states
    var eraContextLoading by remember { mutableStateOf(false) }
    var eraContextText by remember { mutableStateOf("") }

    // Family Historian Q&A chatbot states
    var historianQuery by remember { mutableStateOf("") }
    var historianAnswer by remember { mutableStateOf("") }
    var historianLoading by remember { mutableStateOf(false) }

    // Ancestry Trivia Challenge states
    var triviaStarted by remember { mutableStateOf(false) }
    var triviaQuestion by remember { mutableStateOf("") }
    var triviaOptions by remember { mutableStateOf<List<String>>(emptyList()) }
    var triviaAnswer by remember { mutableStateOf("") }
    var triviaFeedback by remember { mutableStateOf("") }
    var triviaScore by remember { mutableStateOf(0) }
    var triviaStreak by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var questionCount by remember { mutableStateOf(0) }

    val generateNewTrivia: () -> Unit = {
        if (allMembers.size >= 2) {
            val randomMember = allMembers.random()
            val randomType = (0..2).random()
            selectedOption = null
            triviaFeedback = ""
            questionCount++
            
            when (randomType) {
                0 -> {
                    val birthPlace = randomMember.birthPlace ?: "Topola, Srbija"
                    triviaQuestion = t(
                        "Where was ${randomMember.firstName} ${randomMember.lastName} born?",
                        "Gde je prema zapisima rođen(a) ${randomMember.firstName} ${randomMember.lastName}?",
                        "Где родился(лась) ${randomMember.firstName} ${randomMember.lastName}?",
                        "Wo wurde ${randomMember.firstName} ${randomMember.lastName} geboren?",
                        "Où est né(e) ${randomMember.firstName} ${randomMember.lastName}?",
                        currentLanguage
                    )
                    triviaAnswer = birthPlace
                    
                    val incorrect = allMembers.mapNotNull { it.birthPlace }
                        .filter { it != birthPlace }
                        .distinct()
                        .shuffled()
                        .take(3)
                    val opts = (incorrect + birthPlace).shuffled()
                    triviaOptions = if (opts.size < 4) {
                        opts + listOf("Topola", "Beograd", "Kragujevac", "Novi Sad").filter { !opts.contains(it) }.take(4 - opts.size)
                    } else opts
                }
                1 -> {
                    val birthDate = randomMember.birthDate ?: "1950"
                    val birthYear = birthDate.split(".").lastOrNull()?.trim() ?: birthDate
                    triviaQuestion = t(
                        "In what year was ${randomMember.firstName} ${randomMember.lastName} born?",
                        "Koje godine je rođen(a) ${randomMember.firstName} ${randomMember.lastName}?",
                        "В каком году родился(лась) ${randomMember.firstName} ${randomMember.lastName}?",
                        "In welchem Jahr wurde ${randomMember.firstName} ${randomMember.lastName} geboren?",
                        "En quelle année est né(e) ${randomMember.firstName} ${randomMember.lastName}?",
                        currentLanguage
                    )
                    triviaAnswer = birthYear
                    
                    val incorrect = allMembers.mapNotNull { m ->
                        m.birthDate?.split(".")?.lastOrNull()?.trim()
                    }.filter { it != birthYear }.distinct().shuffled().take(3)
                    val opts = (incorrect + birthYear).shuffled()
                    triviaOptions = if (opts.size < 4) {
                        opts + listOf("1912", "1945", "1972", "1998").filter { !opts.contains(it) }.take(4 - opts.size)
                    } else opts
                }
                2 -> {
                    val spouseId = randomMember.spouseId
                    val spouseMember = allMembers.find { it.id == spouseId }
                    val spouseName = spouseMember?.let { "${it.firstName} ${it.lastName}" } ?: t("Unmarried", "Nema supružnika", "Не женат/замужем", "Ledig", "Célibataire", currentLanguage)
                    triviaQuestion = t(
                        "Who is the spouse of ${randomMember.firstName} ${randomMember.lastName}?",
                        "Ko je supružnik (bračni drug) osobe ${randomMember.firstName} ${randomMember.lastName}?",
                        "Кто супруг(а) ${randomMember.firstName} ${randomMember.lastName}?",
                        "Wer ist der Ehepartner von ${randomMember.firstName} ${randomMember.lastName}?",
                        "Qui est l'épou(se) de ${randomMember.firstName} ${randomMember.lastName}?",
                        currentLanguage
                    )
                    triviaAnswer = spouseName
                    
                    val incorrect = allMembers.filter { it.id != randomMember.id && it.id != spouseId }
                        .map { "${it.firstName} ${it.lastName}" }
                        .distinct()
                        .shuffled()
                        .take(3)
                    val opts = (incorrect + spouseName).shuffled()
                    triviaOptions = if (opts.size < 4) {
                        opts + listOf("Tetka Slavica", "Ujak Zoran", "Baka Milica", "Pradeda Čedomir").filter { !opts.contains(it) }.take(4 - opts.size)
                    } else opts
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (focusMember == null) {
            Text(
                text = t(
                    "Select a relative in the tree to enrich their historical time capsule.",
                    "Odaberite srodnika u stablu da biste obogatili njegovu vremensku kapsulu.",
                    "Выберите родственника в дереве, чтобы обогатить его капсулу времени.",
                    "Wählen Sie einen Verwandten im Baum aus, um seine Zeitkapsel zu bereichern.",
                    "Sélectionnez un membre dans l'arbre pour enrichir sa capsule temporelle.",
                    currentLanguage
                ),
                color = appColors.textSecondaryColor,
                fontFamily = appColors.fontFamily
            )
            return
        }

        // Section Title
        Text(
            text = t(
                "Time Capsule & AI Genogram Suite",
                "Vremenska Kapsula i AI Alati Rodoslova",
                "Капсула времени и AI инструменты",
                "Zeitkapsel & AI Genogram-Suite",
                "Capsule temporelle & Suite AI",
                currentLanguage
            ),
            fontWeight = FontWeight.Bold,
            color = appColors.textColor,
            fontSize = 18.sp,
            fontFamily = appColors.fontFamily
        )

        // 1. Ancestry Trivia Challenge (KVIZ) - MEGA VIRAL FEATURE!
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
                        Icon(Icons.Default.EmojiEvents, null, tint = appColors.accentColor)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = t(
                                "Ancestry Trivia Game 🏆",
                                "Porodični Kviz Znanja 🏆",
                                "Семейная викторина 🏆",
                                "Ahnen-Quizspiel 🏆",
                                "Jeu de quiz sur les ancêtres 🏆",
                                currentLanguage
                            ),
                            fontWeight = FontWeight.Bold,
                            color = appColors.textColor,
                            fontSize = 14.sp,
                            fontFamily = appColors.fontFamily
                        )
                    }
                    if (triviaStarted) {
                        Box(
                            modifier = Modifier
                                .background(appColors.accentColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${t("Streak", "Niz", "Серия", "Serie", "Série", currentLanguage)}: $triviaStreak",
                                fontSize = 10.sp,
                                color = appColors.textColor,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = t(
                        "Test how well you actually know your ancestors! Play with family during reunions.",
                        "Testirajte koliko dobro poznajete svoje pretke! Zabavite se sa rođacima na slavama i okupljanjima.",
                        "Проверьте, насколько хорошо вы знаете своих предков! Играйте на семейных встречах.",
                        "Teste, wie gut du deine Vorfahren kennst! Spiele mit deiner Familie bei Treffen.",
                        "Testez à quel point vous connaissez vos ancêtres ! Jouez en famille.",
                        currentLanguage
                    ),
                    fontSize = 11.sp,
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
                )
                Spacer(modifier = Modifier.height(12.dp))

                if (!triviaStarted) {
                    Button(
                        onClick = {
                            triviaStarted = true
                            generateNewTrivia()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                    ) {
                        Text(
                            text = t(
                                "Start Trivia Challenge",
                                "Pokreni Porodični Kviz",
                                "Начать викторину",
                                "Quiz-Herausforderung starten",
                                "Lancer le défi du quiz",
                                currentLanguage
                            ),
                            color = Color.White,
                            fontFamily = appColors.fontFamily
                        )
                    }
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = triviaQuestion,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = appColors.textColor,
                            fontFamily = appColors.fontFamily,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )

                        triviaOptions.forEach { option ->
                            val isSelected = selectedOption == option
                            val isCorrectAnswer = option == triviaAnswer
                            val btnBg = when {
                                selectedOption == null -> appColors.surfaceColor.copy(alpha = 0.5f)
                                isSelected && isCorrectAnswer -> Color(0xFF2E7D32).copy(alpha = 0.8f)
                                isSelected && !isCorrectAnswer -> Color(0xFFC62828).copy(alpha = 0.8f)
                                isCorrectAnswer -> Color(0xFF2E7D32).copy(alpha = 0.6f)
                                else -> appColors.surfaceColor.copy(alpha = 0.2f)
                            }
                            
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(btnBg)
                                    .border(1.dp, if (isSelected) appColors.accentColor else appColors.borderColor, RoundedCornerShape(8.dp))
                                    .clickable(enabled = selectedOption == null) {
                                        selectedOption = option
                                        if (option == triviaAnswer) {
                                            triviaScore += 10
                                            triviaStreak++
                                            triviaFeedback = "🎉 " + t("Correct! Amazing work!", "Tačno! Neverovatan uspeh!", "Правильно! Отличная работа!", "Richtig! Großartige Arbeit!", "Correct ! Superbe travail !", currentLanguage)
                                        } else {
                                            triviaStreak = 0
                                            triviaFeedback = "😢 " + t("Wrong option. Try again next!", "Netačno. Pokušajte sledeće!", "Неверно. Попробуйте еще раз!", "Falsch. Versuche es als nächstes!", "Faux. Essayez la suivante !", currentLanguage)
                                        }
                                    }
                                    .padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(option, fontSize = 12.sp, color = appColors.textColor, fontFamily = appColors.fontFamily)
                                    if (selectedOption != null && isCorrectAnswer) {
                                        Icon(Icons.Default.CheckCircle, null, tint = Color.Green, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }

                        if (triviaFeedback.isNotBlank()) {
                            Text(
                                text = triviaFeedback,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = appColors.textColor,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Spacer(Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Button(
                                    onClick = { generateNewTrivia() },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                                ) {
                                    Text(
                                        text = t("Next Question →", "Sledeće Pitanje →", "Следующий вопрос →", "Nächste Frage →", "Question suivante →", currentLanguage),
                                        color = Color.White
                                    )
                                }
                                OutlinedButton(
                                    onClick = { 
                                        triviaStarted = false
                                        triviaStreak = 0
                                    },
                                    modifier = Modifier.weight(1f),
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textColor)
                                ) {
                                    Text(t("Exit", "Izađi", "Выйти", "Beenden", "Quitter", currentLanguage))
                                }
                            }
                        }
                    }
                }
            }
        }

        // 2. Ancestor Photo Colorization / Restore Simulator
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, appColors.borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = t(
                        "AI Ancestor Photo Restorer 📸",
                        "AI Restaurator slika predaka 📸",
                        "AI Реставратор старых фото 📸",
                        "AI Ahnenfotorestaurator 📸",
                        "AI Restaurateur de photos d'ancêtres 📸",
                        currentLanguage
                    ),
                    fontWeight = FontWeight.Bold,
                    color = appColors.textColor,
                    fontSize = 14.sp,
                    fontFamily = appColors.fontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = t(
                        "Use modern neural networks to automatically denoise and colorize damaged historical photos of your ancestors.",
                        "Koristite neuronske mreže da automatski izoštrite i unesete prelepe boje u oštećene fotografije predaka.",
                        "Используйте нейросети для автоматической очистки и колоризации поврежденных исторических фото.",
                        "Nutze neuronale Netze, um beschädigte historische Fotos deiner Ahnen automatisch zu entrauschen und zu färben.",
                        "Utilisez des réseaux neuronaux pour restaurer et coloriser les photos historiques endommagées.",
                        currentLanguage
                    ),
                    fontSize = 11.sp,
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
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
                            text = if (isPhotoRestored) {
                                t("Photo beautifully colored!", "Fotografija uspešno obojena!", "Фото успешно колоризовано!", "Foto schön gefärbt!", "Photo colorisée avec succès !", currentLanguage)
                            } else {
                                t("State: Black & White / Damaged", "Stanje fotografije: Crno-bela (oštećena)", "Состояние: Черно-белая / Повреждена", "Status: Schwarz-Weiß / Beschädigt", "État : Noir & Blanc / Endommagé", currentLanguage)
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = appColors.textColor,
                            fontFamily = appColors.fontFamily
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
                            Text(
                                text = if (isPhotoRestored) t("Done", "Završeno", "Готово", "Fertig", "Terminé", currentLanguage) else t("Remove Dents & Colorize", "Ukloni oštećenja i oboj sliku", "Восстановить и колоризовать", "Kratzer entfernen & Färben", "Restaurer & Coloriser", currentLanguage),
                                fontSize = 11.sp,
                                color = Color.White,
                                fontFamily = appColors.fontFamily
                            )
                        }
                    }
                }
            }
        }

        // 3. Artistic AI Storyteller (REAL GEMINI INTEGRATION & FALLBACK!)
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
                        text = t(
                            "Artistic AI Storyteller (Live) ✨",
                            "Umetnički AI Pripovedač (Uživo) ✨",
                            "Художественный AI Рассказчик ✨",
                            "Künstlerischer AI-Erzähler (Live) ✨",
                            "Narrateur AI artistique (Direct) ✨",
                            currentLanguage
                        ),
                        fontWeight = FontWeight.Bold,
                        color = appColors.textColor,
                        fontSize = 14.sp,
                        fontFamily = appColors.fontFamily
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = t(
                        "Type standard phrases or anecdotes (e.g. they played accordion, built cabins), and the advanced Gemini AI will instantly weave a rich cinematic biography.",
                        "Upišite nekoliko bazičnih reči ili ključnih sećanja (npr. gde je radio, anegdote), a AI će isplesti romansiranu biografsku audio priču.",
                        "Введите базовые фразы или воспоминания, и искусственный интеллект сразу создаст кинематографическую биографию.",
                        "Gib kurze Sätze oder Geschichten ein, und Gemini AI webt daraus eine filmreife Biographie.",
                        "Saisissez des anecdotes et l'intelligence artificielle tissera une notice biographique captivante.",
                        currentLanguage
                    ),
                    fontSize = 11.sp,
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = prompt,
                    onValueChange = { prompt = it },
                    placeholder = { 
                        Text(
                            text = t(
                                "Example: Hardworking blacksmith, loved cherry orchard, built a wood mill...",
                                "Primer: Bio je vredan zidar, voleo voćnjak trešanja, svirao harmoniku...",
                                "Пример: Трудолюбивый кузнец, любил вишневый сад, построил водяную мельницу...",
                                "Beispiel: Fleißiger Schmied, liebte den Kirschgarten...",
                                "Exemple : Forgeron travailleur, aimait le verger...",
                                currentLanguage
                            ),
                            fontSize = 11.sp
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth().testTag("ai_biography_prompt"),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = appColors.textColor, unfocusedTextColor = appColors.textColor)
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            isStoryLoading = true
                            try {
                                aiStory = com.example.data.GeminiService.generateStory(
                                    firstName = focusMember.firstName,
                                    lastName = focusMember.lastName,
                                    gender = focusMember.gender,
                                    birthDate = focusMember.birthDate,
                                    birthPlace = focusMember.birthPlace,
                                    deathDate = focusMember.deathDate,
                                    deathPlace = focusMember.deathPlace,
                                    biography = focusMember.biography,
                                    customPrompt = prompt,
                                    lang = currentLanguage
                                )
                            } catch (e: Exception) {
                                aiStory = com.example.data.GeminiService.getPromptTemplateFallback(
                                    focusMember.firstName, focusMember.lastName, focusMember.gender, focusMember.birthDate, focusMember.birthPlace, prompt, currentLanguage
                                )
                            }
                            isStoryLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().testTag("ai_story_generate_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                ) {
                    if (isStoryLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = t(
                                "Generate Cinematic Tale (AI)",
                                "Generiši Romansiranu Priču (AI)",
                                "Создать красивую историю (AI)",
                                "Cinematic-Geschichte generieren (AI)",
                                "Générer un récit captivant (AI)",
                                currentLanguage
                            ),
                            color = Color.White,
                            fontFamily = appColors.fontFamily
                        )
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
                                text = aiStory,
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = appColors.textColor,
                                lineHeight = 18.sp,
                                fontFamily = appColors.fontFamily
                            )
                            Spacer(Modifier.height(10.dp))
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
                                        text = if (isNarratorReading) {
                                            t("AI Narrator reading aloud...", "AI Glas čita priču...", "AI Голос читает вслух...", "AI-Stimme liest vor...", "Voix AI lit l'histoire...", currentLanguage)
                                        } else {
                                            t("Tap to listen (Audiobook)", "Slušaj biografsku audio knjigu", "Слушать аудиокнигу", "Anhören (Hörbuch)", "Écouter l'audiolivre", currentLanguage)
                                        },
                                        fontSize = 11.sp,
                                        color = appColors.textSecondaryColor,
                                        fontFamily = appColors.fontFamily
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

        // 4. AI Family Historian Chat Companion (MEMBER Q&A CHATBOOT)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, appColors.borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.SupportAgent, null, tint = appColors.accentColor)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = t(
                            "AI Family Historian Q&A 📖",
                            "AI Istoričar Porodice (Pitanja) 📖",
                            "AI Историк семьи (Чат) 📖",
                            "AI Familienhistoriker Q&A 📖",
                            "AI Historien Q&A de Famille 📖",
                            currentLanguage
                        ),
                        fontWeight = FontWeight.Bold,
                        color = appColors.textColor,
                        fontSize = 14.sp,
                        fontFamily = appColors.fontFamily
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = t(
                        "Ask the intelligent historian chatbot questions about lineage, oldest members, or draft custom family greetings and poems!",
                        "Razgovarajte sa AI istoričarem porodice! Pitajte o srodstvu, ko su najstariji preci, ili zatražite da napiše pesmicu.",
                        "Спросите чат-бота о родословной, старейших предках или попросите написать стихотворение о семье!",
                        "Frage den intelligenten Historiker-Chatbot nach Wurzeln, alten Beziehungen oder Gedichten!",
                        "Posez des questions sur votre lignée ou demandez-lui d'écrire un poème sur vos ancêtres !",
                        currentLanguage
                    ),
                    fontSize = 11.sp,
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
                )
                Spacer(Modifier.height(12.dp))
                OutlinedTextField(
                    value = historianQuery,
                    onValueChange = { historianQuery = it },
                    placeholder = { 
                        Text(
                            text = t(
                                "Ask: Write a brief poem praising my grandfather's roots...",
                                "Pitajte: Napiši mi pesmicu o korenima moje familije...",
                                "Спросите: Напиши стих о моих предках...",
                                "Frage: Schreibe ein Gedicht über meine Ahnen...",
                                "Demandez : Écris un poème sur mes grands-parents...",
                                currentLanguage
                            ),
                            fontSize = 11.sp
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = appColors.textColor, unfocusedTextColor = appColors.textColor)
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            coroutineScope.launch {
                                historianLoading = true
                                historianAnswer = com.example.data.GeminiService.askHistorian(historianQuery, allMembers, currentLanguage)
                                historianLoading = false
                            }
                        },
                        modifier = Modifier.weight(1.3f),
                        colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                    ) {
                        if (historianLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                        } else {
                            Text(
                                text = t("Ask Historian 🎤", "Pitaj Istoričara 🎤", "Спросить 🎤", "Fragen 🎤", "Poser la question 🎤", currentLanguage),
                                color = Color.White,
                                fontSize = 11.sp
                            )
                        }
                    }
                    OutlinedButton(
                        onClick = {
                            historianQuery = t(
                                "Write a beautiful short greeting poem about my lineage and descendants",
                                "Napiši dirljivu kratku pesmu o našoj porodičnoj lozi i tradicijama",
                                "Напиши глубокое стихотворение о корнях моей семьи и потомках",
                                "Schreibe ein schönes Gedicht über unsere Wurzeln",
                                "Écris un court poème émouvant sur notre lignée et nos traditions",
                                currentLanguage
                            )
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = appColors.textColor)
                    ) {
                        Text(
                            text = t("Preset Poetry 📝", "Sastavi Stih 📝", "Стих 📝", "Gedicht 📝", "Créer un poème 📝", currentLanguage),
                            fontSize = 9.sp
                        )
                    }
                }

                if (historianAnswer.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = historianAnswer,
                            fontSize = 12.sp,
                            color = appColors.textColor,
                            fontFamily = appColors.fontFamily,
                            lineHeight = 18.sp
                        )
                    }
                }
            }
        }

        // 5. AI World Historical Era Context Explorer!
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = appColors.surfaceColor),
            border = BorderStroke(1.dp, appColors.borderColor)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = t(
                        "AI Historical Era Travel 🕰️",
                        "AI Vremenski Putnik (Istorijska Era) 🕰️",
                        "AI Историческая эпоха предка 🕰️",
                        "AI Historische Epochenreise 🕰️",
                        "Aventures temporelles AI 🕰️",
                        currentLanguage
                    ),
                    fontWeight = FontWeight.Bold,
                    color = appColors.textColor,
                    fontSize = 14.sp,
                    fontFamily = appColors.fontFamily
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${t("Analyze world history events in", "Istražite istorijski kontekst i sudbonosna zbivanja u svetu tokom godine", "Анализировать мировые события в году", "Analysiere Weltereignisse im Jahr", "Analyser les événements mondiaux de l'année", currentLanguage)} " + 
                           "${focusMember.birthDate?.split(".")?.lastOrNull()?.trim() ?: "1912"} " + 
                           "${t("when this ancestor was born.", "kada je srodnik rođen.", "когда родился этот родственник.", "als dieser Ahn geboren wurde.", "lors de la naissance de cet ancêtre.", currentLanguage)}",
                    fontSize = 11.sp,
                    color = appColors.textSecondaryColor,
                    fontFamily = appColors.fontFamily
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        coroutineScope.launch {
                            eraContextLoading = true
                            try {
                                val year = focusMember.birthDate?.split(".")?.lastOrNull()?.trim() ?: "1912"
                                eraContextText = com.example.data.GeminiService.exploreEraContext(year, focusMember.birthPlace, currentLanguage)
                            } catch (e: Exception) {
                                eraContextText = t("Error loading history details.", "Greška pri analizi epohe.", "Ошибка загрузки истории.", "Fehler.", "Erreur.", currentLanguage)
                            }
                            eraContextLoading = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = appColors.accentColor)
                ) {
                    if (eraContextLoading) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp))
                    } else {
                        Text(
                            text = t("Explore Grand History Eras", "Istraži Istorijska Zbivanja Epohe", "Исследовать события эпохи", "Historische Epoche erkunden", "Explorer l'époque historique", currentLanguage),
                            color = Color.White,
                            fontFamily = appColors.fontFamily
                        )
                    }
                }

                if (eraContextText.isNotBlank()) {
                    Spacer(Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = eraContextText,
                            fontSize = 11.sp,
                            color = appColors.textColor,
                            fontFamily = appColors.fontFamily,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }

        // 5.5 Hometown Vintage Snapshot 🏮 - EXTREMELY IMMERSIVE HISTORICAL VISUAL SNAPSHOTS!
        val birthPlaceRaw = focusMember.birthPlace ?: ""
        val birthPlaceClean = getLocalizedText(birthPlaceRaw, currentLanguage)

        if (birthPlaceClean.isNotBlank()) {
            val birthYear = focusMember.birthDate?.split(".")?.lastOrNull()?.trim() ?: "1912"
            val drawableRes = when {
                birthPlaceRaw.contains("Belgrade", ignoreCase = true) || birthPlaceRaw.contains("Beograd", ignoreCase = true) -> {
                    com.example.R.drawable.img_historical_belgrade
                }
                birthPlaceRaw.contains("Topola", ignoreCase = true) -> {
                    com.example.R.drawable.img_historical_topola
                }
                birthPlaceRaw.contains("Paris", ignoreCase = true) -> {
                    com.example.R.drawable.img_historical_paris
                }
                else -> null
            }

            Card(
                modifier = Modifier.fillMaxWidth().testTag("hometown_vintage_card"),
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
                            Icon(Icons.Default.Landscape, null, tint = appColors.accentColor)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = t(
                                    "Hometown Vintage Snapshot 🏮",
                                    "Zavičajna Istorijska Razglednica 🏮",
                                    "Историческая открытка родины 🏮",
                                    "Heimat-Postkarte im Vintage-Stil 🏮",
                                    "Carte postale vintage d'origine 🏮",
                                    currentLanguage
                                ),
                                fontWeight = FontWeight.Bold,
                                color = appColors.textColor,
                                fontSize = 14.sp,
                                fontFamily = appColors.fontFamily
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = t(
                            "See how the birthplace of your ancestor looked during their lifetime with dynamic historical illustration cards.",
                            "Pogledajte kako je rodno mesto vašeg pretka izgledalo u doba njegovog života kroz prelepe retro razglednice.",
                            "Посмотрите, как выглядела родина вашего предка в годы его жизни, на исторических открытках.",
                            "Erlebe, wie der Geburtsort deines Vorfahren zu seinen Lebzeiten aussah mit historischen Postkarten.",
                            "Découvrez à quoi ressemblait le lieu de naissance de votre ancêtre à son époque avec des cartes postales d'époque.",
                            currentLanguage
                        ),
                        fontSize = 11.sp,
                        color = appColors.textSecondaryColor,
                        fontFamily = appColors.fontFamily
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    if (drawableRes != null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(190.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, appColors.borderColor, RoundedCornerShape(12.dp))
                        ) {
                            Image(
                                painter = painterResource(id = drawableRes),
                                contentDescription = "Historical view of $birthPlaceClean",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            // Vintage overlay stamp inside the image
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                                    .background(Color.White.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                                    .border(1.dp, Color.Gray, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$birthPlaceClean ($birthYear)",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.DarkGray
                                )
                            }
                        }
                    } else {
                        // Procedurally styled stunning simulated vintage post envelope or postcard!
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Brush.linearGradient(listOf(appColors.surfaceColor, appColors.borderColor.copy(alpha = 0.5f))))
                                .border(1.dp, appColors.borderColor, RoundedCornerShape(12.dp))
                        ) {
                            // Draw a vintage postcard envelope layout
                            Canvas(modifier = Modifier.fillMaxSize()) {
                                // Draw diagonal lines on borders (vintage airmail envelope style)
                                val stripeWidth = 10f
                                for (i in 0..size.width.toInt() step 40) {
                                    drawLine(
                                        color = Color(0xFFC62828).copy(alpha = 0.15f),
                                        start = Offset(i.toFloat(), 0f),
                                        end = Offset(i.toFloat() + stripeWidth, 20f),
                                        strokeWidth = 3f
                                    )
                                    drawLine(
                                        color = Color(0xFF1565C0).copy(alpha = 0.15f),
                                        start = Offset(i.toFloat() + 20f, size.height - 20f),
                                        end = Offset(i.toFloat() + 20f + stripeWidth, size.height),
                                        strokeWidth = 3f
                                    )
                                }
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(
                                    modifier = Modifier.weight(1.2f),
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(Icons.Default.MarkunreadMailbox, null, tint = appColors.accentColor.copy(alpha = 0.7f), modifier = Modifier.size(32.dp))
                                    Spacer(Modifier.height(8.dp))
                                    Text(
                                        text = t("Heritage Postcard", "Rodna Razglednica", "Родная открытка", "Heimatbrief", "Carte d'origine", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = appColors.textColor,
                                        fontFamily = appColors.fontFamily
                                    )
                                    Text(
                                        text = "$birthPlaceClean ($birthYear)",
                                        fontSize = 11.sp,
                                        color = appColors.textSecondaryColor,
                                        fontFamily = appColors.fontFamily
                                    )
                                }

                                // Interactive Vintage Wax Seal or stamp representation
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .background(Color(0xFF8B0000).copy(alpha = 0.85f), CircleShape)
                                        .border(2.dp, Color(0xFFFFD700).copy(alpha = 0.6f), CircleShape)
                                        .shadow(4.dp, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = "PAST",
                                            fontSize = 9.sp,
                                            color = Color(0xFFFFD700),
                                            fontWeight = FontWeight.Bold
                                        )
                                        Icon(Icons.Default.FilterVintage, null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                                        Text(
                                            text = birthYear,
                                            fontSize = 9.sp,
                                            color = Color(0xFFFFD700),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Historical descriptions of the place in that decade
                    val localHistoricalFact = when {
                        birthPlaceRaw.contains("Belgrade", ignoreCase = true) || birthPlaceRaw.contains("Beograd", ignoreCase = true) -> {
                            t(
                                "Belgrade at this period transition was teeming with nostalgic horse carriages, cobblestone alleys, and early streetlights, with Terazije square serving as the royal focal heart.",
                                "Beograd je u ovom zlatnom periodu bio pun fijakera, turske kaldrme i prvih plinskih lampi, dok su Terazije pulsirale radosnim građanskim duhom i prelepim salonima.",
                                "Белград в этот золотой период был полон извозчиков, турецкой брусчатки и первых газовых фонарей.",
                                "Belgrad war in dieser Zeit voller Pferdekutschen, traditionellem Pflaster und frühen Gaslaternen.",
                                "Belgrade à cette époque était animée par les diligences, les pavés anciens, et les premiers réverbères.",
                                currentLanguage
                            )
                        }
                        birthPlaceRaw.contains("Topola", ignoreCase = true) -> {
                            t(
                                "Topola was a tranquil village under the oak forests of Oplenac, famous for traditional vineyards and red-roofed stone homesteads carrying medieval heroism.",
                                "Topola je bila mirno vinogradarsko mesto u senci hrastovih šuma Oplenca, poznata po starim kamenim podrumima, pčelarima i mirisu slavske pogače na ognjištu.",
                                "Топола была мирной винодельческой деревней в тени дубовых лесов Опленца.",
                                "Topola war ein ruhiges Winzerdorf im Schatten der Oplenac-Eichenwälder.",
                                "Topola était un paisible village de viticulteurs à l'ombre de la forêt d'Oplenac.",
                                currentLanguage
                            )
                        }
                        birthPlaceRaw.contains("Paris", ignoreCase = true) -> {
                            t(
                                "Paris of this historical era had massive stone boulevards, vintage horse-drawn omnibuses, gas-lit avenues, and bustling bohemian workshops near Seine.",
                                "Pariz ove epohe krasile su kamene avenije, prvi tramvaji sa konjskom vučom, boemski saloni uz Senu i ulični prodavci ruža.",
                                "Париж этой эпохи отличался каменными проспектами и первыми конными трамваями.",
                                "Paris dieser Ära war geprägt von steinernen Alleen und dem Charme der Seine-Künstler.",
                                "Le Paris de cette époque était marqué par de grands boulevards en pierre et les omnibus.",
                                currentLanguage
                            )
                        }
                        else -> {
                            t(
                                "This homeland featured rich agricultural fields, historic cobblestone streets, local artisan guilds, stable family zadrugas, and historical merchant squares.",
                                "Ovaj zavičaj u to vreme krasile su tradicionalne porodične zadruge, kaldrmisane stražarske staze i zanatlije koje su vredno radile na drvenim vodenicama.",
                                "Эта родина в то время отличалась традиционными семейными усадьбами и ремесленными мастерскими.",
                                "Diese Heimat zeichnete sich damals durch traditionelle Gehöfte und handwerkliche Werkstätten aus.",
                                "Cette région d'origine se caractérisait alors par des maisons familiales traditionnelles et des ateliers.",
                                currentLanguage
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(appColors.borderColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(10.dp)
                    ) {
                        Text(
                            text = localHistoricalFact,
                            fontSize = 11.sp,
                            fontStyle = FontStyle.Italic,
                            color = appColors.textColor,
                            fontFamily = appColors.fontFamily,
                            lineHeight = 16.sp
                        )
                    }
                }
            }
        }

        // 6. Realtime Collaboration - Viber / Slack Space simulator
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

data class SharePlatform(
    val name: String,
    val packageId: String,
    val color: Color,
    val emoji: String
)
