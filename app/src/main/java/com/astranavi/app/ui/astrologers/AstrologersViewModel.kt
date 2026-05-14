package com.astranavi.app.ui.astrologers

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.astranavi.app.data.model.Astrologer
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AstrologersViewModel : ViewModel() {
    private val _astrologers = mutableStateOf<List<Astrologer>>(emptyList())
    val astrologers: State<List<Astrologer>> = _astrologers

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    init {
        fetchAstrologers()
    }

    fun fetchAstrologers() {
        viewModelScope.launch {
            _isLoading.value = true
            delay(1500) // Simulate network call to show skeleton
            _astrologers.value = listOf(
                Astrologer(1, "Acharya Vashist", 18, listOf("Vedic", "Vastu"), 4.9, 1240, 25, true, ""),
                Astrologer(2, "Pandit Rahul", 12, listOf("Marriage", "Career"), 4.8, 850, 15, true, ""),
                Astrologer(3, "Guru Maa Anandi", 22, listOf("Healing", "KP Astrology"), 5.0, 2100, 40, true, ""),
                Astrologer(4, "Dr. S. K. Sharma", 15, listOf("Medical", "Prashna"), 4.7, 630, 20, false, ""),
                Astrologer(5, "Yogi Bharat", 10, listOf("Palmistry", "Face Reading"), 4.6, 420, 12, true, ""),
                Astrologer(6, "Acharya Meenakshi", 14, listOf("Numerology", "Tarot"), 4.8, 980, 18, true, ""),
                Astrologer(7, "Pandit Ji Govind", 30, listOf("Muhurat", "Karma"), 4.9, 3400, 50, true, ""),
                Astrologer(8, "Swami Tej", 8, listOf("Nadi", "Lal Kitab"), 4.5, 210, 10, true, ""),
                Astrologer(9, "Jyotishi Priya", 16, listOf("Psychological", "Vedic"), 4.8, 1120, 22, false, ""),
                Astrologer(10, "Acharya Kapil", 20, listOf("Gemology", "Business"), 4.9, 1560, 30, true, "")
            )
            _isLoading.value = false
        }
    }
}
