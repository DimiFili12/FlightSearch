package com.example.flightsearch.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.AirportRepository
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FavoriteRepository
import com.example.flightsearch.data.UserPreferencesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeViewModel(
    private val airportRepository: AirportRepository,
    private val favoriteRepository: FavoriteRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    companion object {
        // Timeout for state sharing
        private const val TIMEOUT = 5_000L
    }

    // State flow for user input
    private val _userInputFlow = MutableStateFlow("")
    val userInputFlow: StateFlow<String> = _userInputFlow

    // State flow for departure IATA code
    private val _departureIataFlow = MutableStateFlow("")

    // State flow for selected departure airport
    private val _selectedDepartureFlow = MutableStateFlow(AirportDetails())

    // State flow to show/hide departures
    private val _showDepartures = MutableStateFlow(true)
    val showDepartures: StateFlow<Boolean> = _showDepartures

    // State flow for favorite flights
    private val _favorites = MutableStateFlow<List<FlightsWithFavoriteStatus>>(emptyList())
    val favorites: StateFlow<List<FlightsWithFavoriteStatus>> = _favorites

    // State flow for arrivals
    private val _arrivalsFlow = MutableStateFlow<List<AirportDetails>>(emptyList())

    // UI state flow for departures based on user input
    @OptIn(ExperimentalCoroutinesApi::class)
    var departureUiState: StateFlow<AirportUiState> =
        _userInputFlow.flatMapLatest { userInput ->
            airportRepository.getDepartures(userInput)
                .map { airports ->
                    AirportUiState(
                        airports.map {
                            AirportDetails(it.id, it.iataCode, it.name)
                        }
                    )
                }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(TIMEOUT),
            initialValue = AirportUiState()
        )

    // State flow for flights with favorite status
    private val _flightsWithFavoriteStatusUiState: MutableStateFlow<List<FlightsWithFavoriteStatus>> = MutableStateFlow(emptyList())
    val flightsWithFavoriteStatusUiState: StateFlow<List<FlightsWithFavoriteStatus>> = _flightsWithFavoriteStatusUiState

    init {
        // Load favorites from repository
        viewModelScope.launch {
            favoriteRepository.getFavorites().collect { favorites ->
                val favoritePairs = favorites.map { favorite ->
                    val departure = airportRepository.getAirport(favorite.departureCode).first()
                    val arrival = airportRepository.getAirport(favorite.destinationCode).first()
                    val flights = Pair(AirportDetails(departure.id, departure.iataCode, departure.name),
                        AirportDetails(arrival.id, arrival.iataCode, arrival.name))
                    FlightsWithFavoriteStatus(flights, true)
                }
                _favorites.value = favoritePairs
            }
        }

        // Load user preferences
        viewModelScope.launch {
            _userInputFlow.value = userPreferencesRepository.readPreferences()
        }
    }

    // Update search query and save to Preferences
    fun updateSearchQuery(query: String) {
        _userInputFlow.value = query
        viewModelScope.launch {
            userPreferencesRepository.savePreferences(query)
        }
    }

    fun showDepartures() {
        _showDepartures.value = true
    }

    fun hideDepartures() {
        _showDepartures.value = false
    }

    // Handle the selection of a departure airport
    fun selectDeparture(selected: AirportDetails) {
        _departureIataFlow.value = selected.iataCode
        getArrivals()

        _selectedDepartureFlow.value = selected
        getFlightsWithFavorite()
    }

    // Get arrivals based on the selected departure airport
    private fun getArrivals() {
        viewModelScope.launch {
            _departureIataFlow.collectLatest { iata ->
                airportRepository.getArrivals(iata).collect { arrivals ->
                    _arrivalsFlow.value = arrivals.map {
                        AirportDetails(it.id, it.iataCode, it.name)
                    }
                }
            }
        }
    }

    // Get routes with favorite status
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun getFlightsWithFavorite() {
        viewModelScope.launch {
            _selectedDepartureFlow.flatMapLatest { departure ->
                _arrivalsFlow.map { arrivals ->
                    arrivals.map { FlightsWithFavoriteStatus(Pair(departure, it), isInFavorites(Pair(departure, it)) )}
                }
            }.collect { flightsWithFavorites ->
                _flightsWithFavoriteStatusUiState.value = flightsWithFavorites
            }
        }
    }

    // Check if a route is in favorite table
    private suspend fun isInFavorites(pair: Pair<AirportDetails, AirportDetails>): Boolean {
        return withContext(Dispatchers.IO) {
            favoriteRepository.checkFav(pair.first.id + pair.second.id)
        }
    }

    // Insert a route to favorite table
    fun insertFavorite(flights: FlightsWithFavoriteStatus) {
        viewModelScope.launch {
            favoriteRepository.insertFav(flights.toFavorite())
            getFlightsWithFavorite()
        }
    }

    // Delete a route from favorite table
    fun deleteFavorite(flights: FlightsWithFavoriteStatus) {
        viewModelScope.launch {
            favoriteRepository.deleteFav(flights.toFavorite())
            getFlightsWithFavorite()
        }
    }
}

data class AirportUiState(val airportDetails: List<AirportDetails> = emptyList())

data class AirportDetails(
    val id: Int = 0,
    val iataCode: String = "",
    val name: String = ""
)

data class FlightsWithFavoriteStatus(
    val pair: Pair<AirportDetails, AirportDetails>,
    var isFavorite: Boolean
)

fun FlightsWithFavoriteStatus.toFavorite(): Favorite = Favorite(
    id = pair.first.id + pair.second.id,
    departureCode = pair.first.iataCode,
    destinationCode = pair.second.iataCode
)