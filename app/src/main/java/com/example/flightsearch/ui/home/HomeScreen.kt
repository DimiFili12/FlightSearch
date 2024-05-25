package com.example.flightsearch.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.flightsearch.R
import com.example.flightsearch.ui.AppViewModelProvider


@Composable
fun HomeScreen(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val departureUiState by viewModel.departureUiState.collectAsState()
    val flightsUiState by viewModel.flightsWithFavoriteStatusUiState.collectAsState()
    val userInput by viewModel.userInputFlow.collectAsState()
    val favoriteUiState by viewModel.favorites.collectAsState()
    val showDepartures by viewModel.showDepartures.collectAsState()

    MainScreen(
        departureList = departureUiState.airportDetails,
        flightPairs = flightsUiState,
        favoritePairs = favoriteUiState,
        userInput = userInput,
        showDepartures = showDepartures,
        onUserInputChanged = { input ->
            viewModel.updateSearchQuery(input)
            viewModel.showDepartures()
        },
        onSelectDeparture = { selected ->
            viewModel.selectDeparture(selected)
            viewModel.hideDepartures()
        },
        onSaveFavorite = { pair -> viewModel.insertFavorite(pair) },
        onDeleteFavorite = { pair -> viewModel.deleteFavorite(pair) },
        contentPadding = contentPadding,
        modifier = modifier
    )
}

@Composable
fun MainScreen(
    departureList: List<AirportDetails>,
    flightPairs: List<FlightsWithFavoriteStatus>,
    favoritePairs: List<FlightsWithFavoriteStatus>,
    userInput: String,
    showDepartures: Boolean,
    onUserInputChanged: (String) -> Unit,
    onSelectDeparture: (AirportDetails) -> Unit,
    onSaveFavorite: (FlightsWithFavoriteStatus) -> Unit,
    onDeleteFavorite: (FlightsWithFavoriteStatus) -> Unit,
    contentPadding: PaddingValues,
    modifier: Modifier
) {
    Column(
        horizontalAlignment = Alignment.Start,
        verticalArrangement = Arrangement.Top,
        modifier = modifier.padding(contentPadding),
    ) {
        SearchBar(
            userInput = userInput,
            onUserInputChanged = onUserInputChanged,
        )
        if (userInput.isEmpty()) {
            if (favoritePairs.isNotEmpty()) {
                FlightsList(
                    pairs = favoritePairs,
                    text = stringResource(id = R.string.favorite_routes),
                    onSaveFavorite = onSaveFavorite,
                    onDeleteFavorite = onDeleteFavorite
                )
            } else {
                Text(
                    text = stringResource(id = R.string.no_favorite),
                    modifier = Modifier.padding(16.dp)
                )
            }
        } else {
            if (showDepartures){
                if (departureList.isNotEmpty()) {
                    DepartureList(
                        departureList = departureList,
                        onSelectedDeparture = onSelectDeparture,
                    )
                } else {
                    Text(
                        text = stringResource(id = R.string.no_airport),
                        modifier = Modifier.padding(16.dp)
                    )
                }
            } else {
                if (flightPairs.isNotEmpty()) {
                    FlightsList(
                        pairs = flightPairs,
                        text = stringResource(R.string.flights_from, flightPairs.first().pair.first.iataCode),
                        onSaveFavorite = onSaveFavorite,
                        onDeleteFavorite = onDeleteFavorite
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    userInput: String,
    onUserInputChanged: (String) -> Unit
) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value = userInput,
        onValueChange = onUserInputChanged,
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                softwareKeyboardController?.hide()
            }
        ),
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.padding(start = 16.dp)
            )
        },
        placeholder = { Text( text = stringResource(R.string.search_label)) },
        shape = MaterialTheme.shapes.extraLarge,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
    )
}

@Composable
fun DepartureList(
    departureList: List<AirportDetails>,
    onSelectedDeparture: (AirportDetails) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        items(
            items = departureList,
            key = { it.id }
        ) { departure ->
            DepartureCard(
                departure = departure,
                onSelectedDeparture = onSelectedDeparture
            )
        }
    }
}

@Composable
fun DepartureCard(
    departure: AirportDetails,
    onSelectedDeparture: (AirportDetails) -> Unit,
) {
    Row(
        modifier = Modifier
            .clickable { onSelectedDeparture(departure) }
    ) {
        Text(
            text = departure.iataCode,
            modifier = Modifier.padding(end = 4.dp)
        )
        Text(text = departure.name)
    }
}

@Composable
fun FlightsList(
    pairs: List<FlightsWithFavoriteStatus>,
    text: String,
    onSaveFavorite: (FlightsWithFavoriteStatus) -> Unit,
    onDeleteFavorite: (FlightsWithFavoriteStatus) -> Unit,
) {
    Text(
        text = text,
        fontWeight = FontWeight.ExtraBold,
        modifier = Modifier.padding(8.dp)
    )
    LazyColumn() {
        items(
            items = pairs,
            key = { it.pair.first.id + it.pair.second.id }
        ) {pair ->
            FlightCard(
                pairs = pair,
                onSaveFavorite,
                onDeleteFavorite
            )
        }
    }
}

@Composable
fun FlightCard(
    pairs: FlightsWithFavoriteStatus,
    onSaveFavorite: (FlightsWithFavoriteStatus) -> Unit,
    onDeleteFavorite: (FlightsWithFavoriteStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(8.dp)
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(8.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.weight(8f)
            ) {
                Text(text = "DEPART :")
                Row( modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = pairs.pair.first.iataCode,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = pairs.pair.first.name,
                    )
                }
                Text(text = "ARRIVE :")
                Row( modifier = Modifier.padding(4.dp)
                ) {
                    Text(
                        text = pairs.pair.second.iataCode,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(end = 4.dp)
                    )
                    Text(
                        text = pairs.pair.second.name,
                    )
                }
            }
            IconButton(
                onClick = {
                    if (pairs.isFavorite) {
                        onDeleteFavorite(pairs)
                    } else {
                        onSaveFavorite(pairs)
                    }
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .weight(1f)
            ) {
                Icon(
                    imageVector = if (pairs.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = null
                )
            }
        }
    }
}