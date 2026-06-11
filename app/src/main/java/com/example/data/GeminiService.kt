package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateStory(
        firstName: String,
        lastName: String,
        gender: String,
        birthDate: String?,
        birthPlace: String?,
        deathDate: String?,
        deathPlace: String?,
        biography: String?,
        customPrompt: String,
        lang: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.w(TAG, "Gemini API key is not configured or placeholder.")
            return@withContext getPromptTemplateFallback(firstName, lastName, gender, birthDate, birthPlace, customPrompt, lang)
        }

        val prompt = constructStoryPrompt(firstName, lastName, gender, birthDate, birthPlace, deathDate, deathPlace, biography, customPrompt, lang)
        return@withContext makeApiQuery(apiKey, prompt) ?: getPromptTemplateFallback(firstName, lastName, gender, birthDate, birthPlace, customPrompt, lang)
    }

    suspend fun askHistorian(
        question: String,
        allMembers: List<FamilyMember>,
        lang: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getHistorianFallback(question, lang)
        }

        val prompt = constructChatHistorianPrompt(question, allMembers, lang)
        return@withContext makeApiQuery(apiKey, prompt) ?: getHistorianFallback(question, lang)
    }

    suspend fun exploreEraContext(
        birthYear: String,
        birthPlace: String?,
        lang: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext getEraFallback(birthYear, birthPlace, lang)
        }
        val prompt = constructEraPrompt(birthYear, birthPlace, lang)
        return@withContext makeApiQuery(apiKey, prompt) ?: getEraFallback(birthYear, birthPlace, lang)
    }

    private fun constructStoryPrompt(
        first: String, last: String, gender: String, bDate: String?, bPl: String?, dDate: String?, dPl: String?, bio: String?, notes: String, lang: String
    ): String {
        return """
            You are a master family genealogist and storyteller. Write a highly romanticized, beautiful, engaging biography of an ancestor.
            Details:
            - Name: $first $last
            - Gender: $gender
            - Born: ${bDate ?: "Unknown"} in ${bPl ?: "Unknown"}
            - Died: ${dDate ?: "Still Alive / Unknown"} in ${dPl ?: "Unknown"}
            - Existing biography highlights: ${bio ?: "None basic details"}
            - Custom user insights or anecdotes: $notes
            
            Write this story in ${getLanguageName(lang)} language. Ensure it is written in an elegant, touching, and storytelling emotional style. Keep it to 1-2 rich paragraphs. Do not output markdown lists, just flowy paragraphs.
        """.trimIndent()
    }

    private fun constructChatHistorianPrompt(question: String, members: List<FamilyMember>, lang: String): String {
        val membersListStr = members.joinToString("\n") { m ->
            "- ID: ${m.id}, Name: ${m.firstName} ${m.lastName}, Gender: ${m.gender}, Born: ${m.birthDate ?: "Unknown"} in ${m.birthPlace ?: "Unknown"}, Died: ${m.deathDate ?: "N/A"}, Bio: ${m.biography ?: "N/A"}, FatherID: ${m.fatherId ?: "None"}, MotherID: ${m.motherId ?: "None"}, SpouseID: ${m.spouseId ?: "None"}"
        }

        return """
            You are the "AI Family Historian", an expert virtual assistant who knows everything about the family tree provided below.
            
            Here is the total family tree records:
            $membersListStr
            
            The user wants to interact with you in ${getLanguageName(lang)}. 
            User question: "$question"
            
            Answer the user's question clearly, warmly, and helpfully based on the records. If the question lacks details, guide them or help them discover cool connections (like direct lines, oldest members, migration patterns, or common surnames). Sastavi odgovor na jeziku koji je tražen (${getLanguageName(lang)}). Keep it warm, engaging and friendly.
        """.trimIndent()
    }

    private fun constructEraPrompt(birthYear: String, birthPlace: String?, lang: String): String {
        return """
            You are a world history expert. Provide a fascinating local and global history snapshot for someone born/living in the year "$birthYear" in "${birthPlace ?: "Europe"}".
            Identify:
            1. Major world transformations occurring in $birthYear or that decade.
            2. Daily life, technological levels, or historical events of that period in ${birthPlace ?: "their region"}.
            Keep your account vivid, educational, and engaging. Written in ${getLanguageName(lang)} language. Wrap it nicely in 1-2 scannable paragraphs. Do not use markdown headers, just beautiful prose.
        """.trimIndent()
    }

    private fun makeApiQuery(apiKey: String, prompt: String): String? {
        return try {
            val url = "$BASE_URL?key=$apiKey"
            val jsonBody = JSONObject().apply {
                val contents = JSONArray().apply {
                    val contentObj = JSONObject().apply {
                        val parts = JSONArray().apply {
                            put(JSONObject().apply { put("text", prompt) })
                        }
                        put("parts", parts)
                    }
                    put(contentObj)
                }
                put("contents", contents)
            }

            val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.e(TAG, "API call failed with code: ${response.code}")
                    return null
                }
                val bodyString = response.body?.string() ?: return null
                Log.d(TAG, "Raw response received successfully.")
                val jsonObject = JSONObject(bodyString)
                val textResponse = jsonObject.getJSONArray("candidates")
                    .getJSONObject(0)
                    .getJSONObject("content")
                    .getJSONArray("parts")
                    .getJSONObject(0)
                    .getString("text")
                return textResponse
            }
        } catch (e: Exception) {
            Log.e(TAG, "Network exception querying Gemini API", e)
            null
        }
    }

    private fun getLanguageName(code: String): String {
        return when (code) {
            "SRB" -> "Serbian"
            "RUS" -> "Russian"
            "DEU" -> "German"
            "FRA" -> "French"
            else -> "English"
        }
    }

    fun getPromptTemplateFallback(first: String, last: String, gender: String, bDate: String?, bPl: String?, notes: String, lang: String): String {
        return when (lang) {
            "SRB" -> "U pitomom delu Šumadije, odakle loza $last vuče korene, pripoveda se o vremenu kada je $first koračao ovim prostorima. Rođen je ${bDate ?: "vremena prošla"} u mestu ${bPl ?: "predaka"}. ${if (notes.isNotBlank()) notes else "Doneo je blagost u dom i osmeh svakoj duši koju srete."} Ostavio je neizbrisivo svedičanstvo o mudrosti i ljubavi prema kućnom pragu. Generacije svedoče o sećanju koje nikada neće uvenuti."
            "RUS" -> "В живописном уголке, откуда род $last берет свои истоки, рассказывают о временах, когда $first ступал по этой земле. ${if (notes.isNotBlank()) notes else "Он принес мир в дом и тепло в сердца близких."} Его жизненный путь оставил глубокий след в истории семьи, став примером мужества и доброты."
            "DEU" -> "In der malerischen Region, aus der die Familie $last stammt, wird die Geschichte erzählt, als $first diese Wege beschritt. ${if (notes.isNotBlank()) notes else "Er brachte Licht und Freude in die Herzen aller."} Ein unvergängliches Vermächtnis von Fleiß, Fürsorge und Familienwerten."
            "FRA" -> "Dans le paisible berceau familial d'où la lignée $last tire ses origines, on raconte l'époque où $first parcourait ces terres. ${if (notes.isNotBlank()) notes else "Il a apporté la joie et la paix au foyer."} Un héritage éternel d'amour et de dévouement pour les générations futures."
            else -> "In the beautiful and peaceful homeland from which the $last lineage originates, a story is told of when $first walked these fields. Born in ${bPl ?: "lands of history"} around ${bDate ?: "past times"}, ${if (notes.isNotBlank()) notes else "they brought light and warmth to the hearts of everyone they met."} Leaving an eternal legacy of high wisdom, honor, and love for the home threshold."
        }
    }

    private fun getHistorianFallback(question: String, lang: String): String {
        val qLower = question.lowercase()
        return when (lang) {
            "SRB" -> {
                if (qLower.contains("pesm") || qLower.contains("stih")) {
                    "Kroz magle vremena i vetar što duva,\nPorodični koren staru tajnu čuva.\nIz svakog lista pesma se ori,\nO časti, o radu, o večnoj zori!"
                } else if (qLower.contains("najstarij") || qLower.contains("prv")) {
                    "Naši prvi preci bili su temelji ove kuće, u vremenima teškim i ponosnim. Pregledajte '1. GENERACIJU' na stablu kako biste otkrili koren cele loze."
                } else {
                    "Kao vaš porodični istoričar, ponosan sam na priče koje čuvate. Svaki unos u porodičnom stablu nosi jedinstvenu tajnu i snagu predaka."
                }
            }
            else -> {
                "As your family historian, I cherish the deep roots of your genealogy. Keep adding birth dates, places, and biographies to reveal the complete journey of your family."
            }
        }
    }

    private fun getEraFallback(birthYear: String, birthPlace: String?, lang: String): String {
        return when (lang) {
            "SRB" -> "Godina $birthYear u mestu ${birthPlace ?: "našim krajevima"} bila je obeležena velikim društvenim promenama, ranim industrijskim napretkom i očuvanjem tradicionalnih porodičnih zadruga. Život je bio sporiji, duboko povezan sa zemljom i lokalnim tradicijama."
            "RUS" -> "Год $birthYear был периодом глубоких исторических преобразований, важными открытиями и укреплением вековых традиций. Люди жили в тесной связи с традициями предков."
            "DEU" -> "Das Jahr $birthYear war geprägt von rasantem technologischem Fortschritt, gesellschaftlichen Veränderungen und einer starken Bindung an traditionelle Familienwerte."
            else -> "The year $birthYear represents a fascinating historical epoch, bridging older agricultural traditions and rapid mechanical leaps. Communities relied heavily on tight-knitted family bonds and shared village labor."
        }
    }
}
