package com.example.flightsearch.ui.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.flightsearch.data.AirportRepository
import com.example.flightsearch.data.Favorite
import com.example.flightsearch.data.FavoriteRepository
import com.example.flightsearch.data.UserPreferencesRepository
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

class HomeViewModel(
    private val airportRepository: AirportRepository,
    private val favoriteRepository: FavoriteRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
) : ViewModel() {
    companion object {
        private const val TIMEOUT = 5_000L
    }

    private var _userInput by mutableStateOf("")
    var userInput: String
        get() = _userInput
        private set(value) { _userInput = value }

    private val _departureIataFlow = MutableStateFlow("")

    private val _selectedDepartureFlow = MutableStateFlow(AirportDetails())

    private val _showDepartures = MutableStateFlow(true)
    val showDepartures: StateFlow<Boolean> = _showDepartures

    private val _favorites = MutableStateFlow<List<FlightsWithFavoriteStatus>>(emptyList())
    val favorites: StateFlow<List<FlightsWithFavoriteStatus>> = _favorites

    private val _arrivalsFlow = MutableStateFlow<List<AirportDetails>>(emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val departureUiState: StateFlow<AirportUiState> =
        snapshotFlow { userInput }
            .flatMapLatest { userInput ->
                airportRepository.getDepartures(userInput)
                    .map { airports ->
                        AirportUiState(
                            airports.map {
                                AirportDetails(it.id, it.iataCode, it.name)
                            }
                        )
                    }}
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT),
                initialValue = AirportUiState()
            )

    private val _flightsWithFavoriteStatusUiState: MutableStateFlow<List<FlightsWithFavoriteStatus>> = MutableStateFlow(emptyList())
    val flightsWithFavoriteStatusUiState: StateFlow<List<FlightsWithFavoriteStatus>> = _flightsWithFavoriteStatusUiState

    init {
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

        viewModelScope.launch {
            userInput = userPreferencesRepository.readPreferences()
        }
    }

    fun updateSearchQuery(query: String) {
        userInput = query
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

    fun selectDeparture(selected: AirportDetails) {
        _departureIataFlow.value = selected.iataCode
        getArrivals()

        _selectedDepartureFlow.value = selected
        getFlightsWithFavorite()
    }

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

    private suspend fun isInFavorites(pair: Pair<AirportDetails, AirportDetails>): Boolean {
        return favoriteRepository.checkFav(pair.first.id + pair.second.id)
    }

    fun insertFavorite(flights: FlightsWithFavoriteStatus) {
        viewModelScope.launch {
            favoriteRepository.insertFav(flights.toFavorite())
            getFlightsWithFavorite()
        }
    }

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