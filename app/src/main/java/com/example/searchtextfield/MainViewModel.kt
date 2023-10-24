package com.example.searchtextfield

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@OptIn(FlowPreview::class)
class MainViewModel : ViewModel() {

    private val _searchText = MutableStateFlow("")
    val searchText = _searchText.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val _persons = MutableStateFlow(allPerson)
    val persons = _searchText
        .debounce(500)
        .onEach { _isSearching.update { true } }
        // text -> refers to the current search query text that is being used for filtering the list of persons.
        // persons -> this parameter represents the latest value emitted by the _persons flow.
        // The lambda expression receives the latest values of the _searchText and _persons flows and,
        // determines how to combine them to produce the filtered list of persons based on the current search query text.
        .combine(_persons) { text, persons ->
            if (text.isBlank()) {
                persons
            } else {
                delay(500)
                persons.filter {
                    it.doesMatchSearchQuery(text)
                }
            }
        }
        .onEach { _isSearching.update { false } }
        // Transform the resulting flow into a state flow that can be observed by collectors
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            // Initial value of the state flow
            _persons.value
        )

    fun onSearchTextChange(text: String) {
        _searchText.value = text
    }

    data class Person(
        val firstName: String,
        val lastName: String
    ) {
        fun doesMatchSearchQuery(query: String): Boolean {

            val matchingCombination = listOf(
                "$firstName$lastName",
                "$firstName $lastName",
                "${firstName.first()}${lastName.first()}",
                "${firstName.first()} ${lastName.first()}"
            )

            // check if any element in the collection satisfies a given condition. if contain -->true , or false
            return matchingCombination.any { anyElement ->
                anyElement.contains(query, ignoreCase = true)
            }
        }
    }

}

private val allPerson = listOf(
    MainViewModel.Person(
        firstName = "Tarek",
        lastName = "Hendi"
    ),
    MainViewModel.Person(
        firstName = "Omar",
        lastName = "Hendi"
    ),
    MainViewModel.Person(
        firstName = "Ammar",
        lastName = "Hendi"
    ),
    MainViewModel.Person(
        firstName = "Nisreen",
        lastName = "Hendi"
    ),
    MainViewModel.Person(
        firstName = "Aya",
        lastName = "Hendi"
    )
)