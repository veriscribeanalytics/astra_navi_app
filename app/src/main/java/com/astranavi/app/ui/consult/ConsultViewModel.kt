package com.astranavi.app.ui.consult

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.*
import com.astranavi.app.data.model.PaywallCardData
import com.astranavi.app.data.repository.AstrologyRepository
import com.astranavi.app.data.repository.EntitlementRepository
import com.astranavi.app.util.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

sealed class ConsultStep {
    object BirthDetails : ConsultStep()
    object CategorySelection : ConsultStep()
    object SubCategorySelection : ConsultStep()
    object QuestionSelection : ConsultStep()
    object Result : ConsultStep()
}

class ConsultViewModel(
    private val repository: AstrologyRepository,
    private val sessionManager: SessionManager,
    private val entitlementRepository: EntitlementRepository? = null
) : ViewModel() {

    private val _step = mutableStateOf<ConsultStep>(ConsultStep.BirthDetails)
    val step: State<ConsultStep> = _step

    val dob = mutableStateOf("")
    val tob = mutableStateOf("")
    val pob = mutableStateOf("")

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _tree = mutableStateOf<ConsultTree?>(null)
    val tree: State<ConsultTree?> = _tree

    val selectedCategory = mutableStateOf<Category?>(null)
    val highlightedCategory = mutableStateOf<Category?>(null)
    val selectedSubCategory = mutableStateOf<SubCategory?>(null)
    val selectedQuestion = mutableStateOf("")
    val selectedTone = mutableStateOf("warm")
    val selectedLanguage = mutableStateOf("English")
    val customNote = mutableStateOf("")

    private val _birthDetailsError = mutableStateOf<String?>(null)
    val birthDetailsError: State<String?> = _birthDetailsError

    private val _paywall = mutableStateOf<PaywallCardData?>(null)
    val paywall: State<PaywallCardData?> = _paywall

    fun validateBirthDetails(): Boolean {
        _birthDetailsError.value = null
        if (dob.value.isEmpty()) {
            _birthDetailsError.value = "Date of birth is required"
            return false
        }
        if (tob.value.isEmpty()) {
            _birthDetailsError.value = "Time of birth is required for accurate results"
            return false
        }
        if (!tob.value.matches(Regex("^\\d{2}:\\d{2}$")) && !tob.value.matches(Regex("^\\d{4}$"))) {
            _birthDetailsError.value = "Please enter birth time in HH:MM format"
            return false
        }
        if (pob.value.isEmpty()) {
            _birthDetailsError.value = "Place of birth is required"
            return false
        }
        return true
    }

    private fun formatBirthTime(): String {
        val raw = tob.value
        if (raw.matches(Regex("^\\d{2}:\\d{2}$"))) return raw
        if (raw.matches(Regex("^\\d{4}$"))) return "${raw.substring(0, 2)}:${raw.substring(2, 4)}"
        return ""
    }

    private val _consultResult = mutableStateOf<String?>(null)
    val consultResult: State<String?> = _consultResult

    init {
        viewModelScope.launch {
            dob.value = sessionManager.userDob.first() ?: ""
            tob.value = sessionManager.userTob.first() ?: ""
            pob.value = sessionManager.userPob.first() ?: ""
        }
    }

    fun fetchTree() {
        if (!validateBirthDetails()) return
        
        val age = calculateAge(dob.value)
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = repository.getConsultTree(
                    age = age,
                    lang = selectedLanguage.value
                )
                if (response.isSuccessful && response.body() != null) {
                    _tree.value = response.body()?.tree
                    _step.value = ConsultStep.CategorySelection
                }
            } catch (e: Exception) {
                // Error handling
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(category: Category) {
        viewModelScope.launch {
            highlightedCategory.value = category
            delay(200) // Phase 4B: Continuity Transition Hint delay
            selectedCategory.value = category
            _step.value = ConsultStep.SubCategorySelection
            highlightedCategory.value = null
        }
    }

    fun selectSubCategory(sub: SubCategory) {
        selectedSubCategory.value = sub
        _step.value = ConsultStep.QuestionSelection
    }

    fun selectQuestion(question: String) {
        selectedQuestion.value = question
        val formattedTob = formatBirthTime()
        if (formattedTob.isEmpty()) {
            _birthDetailsError.value = "Please enter birth time in HH:MM format"
            _step.value = ConsultStep.BirthDetails
            return
        }
        generateConsultation(formattedTob)
    }

    private fun generateConsultation(formattedTob: String) {
        _isLoading.value = true
        _step.value = ConsultStep.Result
        _consultResult.value = null
        
        viewModelScope.launch {
            try {
                val request = ConsultRequest(
                    birth_date = dob.value,
                    birth_time = formattedTob,
                    birth_place = if (pob.value.isEmpty()) "Unknown" else pob.value,
                    primary_category = selectedCategory.value?.key ?: "",
                    secondary_category = selectedSubCategory.value?.key ?: "",
                    final_question = if (selectedQuestion.value == "Other") customNote.value else selectedQuestion.value,
                    response_tone = selectedTone.value,
                    language = selectedLanguage.value
                )

                withContext(Dispatchers.IO) {
                    val responseBody = repository.generateConsultation(request)
                    responseBody.use { body ->
                        val contentType = body.contentType()
                        if (contentType != null && contentType.subtype == "json") {
                            val errorBody = body.string()
                            val paywallData = entitlementRepository?.parsePaywallFrom402Body(errorBody)
                            if (paywallData != null) {
                                launch(Dispatchers.Main) {
                                    _paywall.value = paywallData
                                    _isLoading.value = false
                                }
                                return@use
                            }
                            launch(Dispatchers.Main) {
                                _consultResult.value = "I'm sorry, I'm having trouble connecting. Please try again."
                                _isLoading.value = false
                            }
                            return@use
                        }
                        
                        var fullResponse = ""
                        body.byteStream().bufferedReader().use { reader ->
                            reader.forEachLine { line ->
                                if (line.startsWith("data: ")) {
                                    val dataStr = line.substring(6).trim()
                                    if (dataStr == "[DONE]") return@forEachLine
                                    
                                    try {
                                        val json = JSONObject(dataStr)
                                        val token = json.optString("token")
                                        if (token.isNotEmpty()) {
                                            fullResponse += token
                                            launch(Dispatchers.Main) {
                                                _consultResult.value = fullResponse
                                            }
                                        }
                                    } catch (_: Exception) {
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _consultResult.value = "I'm sorry, I'm having trouble connecting to the stars right now. Please try again later."
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun navigateBack() {
        when (_step.value) {
            ConsultStep.Result -> _step.value = ConsultStep.QuestionSelection
            ConsultStep.QuestionSelection -> _step.value = ConsultStep.SubCategorySelection
            ConsultStep.SubCategorySelection -> _step.value = ConsultStep.CategorySelection
            ConsultStep.CategorySelection -> _step.value = ConsultStep.BirthDetails
            ConsultStep.BirthDetails -> { /* Already at start */ }
        }
    }

    fun jumpToStep(index: Int) {
        val targetStep = when (index) {
            0 -> ConsultStep.BirthDetails
            1 -> if (_tree.value != null) ConsultStep.CategorySelection else null
            2 -> if (selectedCategory.value != null) ConsultStep.SubCategorySelection else null
            3 -> if (selectedSubCategory.value != null) ConsultStep.QuestionSelection else null
            4 -> if (_consultResult.value != null) ConsultStep.Result else null
            else -> null
        }
        targetStep?.let { _step.value = it }
    }

    private fun calculateAge(dobString: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val birthDate = sdf.parse(dobString) ?: return 25
            val today = Calendar.getInstance()
            val birth = Calendar.getInstance()
            birth.time = birthDate
            var age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR)
            if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--
            age
        } catch (e: Exception) {
            25
        }
    }
}
