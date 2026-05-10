package com.riversongai.ui.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.riversongai.data.model.*
import com.riversongai.data.repository.SportsRepository
import kotlinx.coroutines.launch

class SportsViewModel(private val sportsRepository: SportsRepository) : ViewModel() {

    private val _followedTeams = MutableLiveData<List<SportsTeam>>(emptyList())
    val followedTeams: LiveData<List<SportsTeam>> = _followedTeams

    private val _selectedTeam = MutableLiveData<SportsTeam?>(null)
    val selectedTeam: LiveData<SportsTeam?> = _selectedTeam

    private val _results = MutableLiveData<List<SportsMatch>>(emptyList())
    val results: LiveData<List<SportsMatch>> = _results

    private val _fixtures = MutableLiveData<List<SportsMatch>>(emptyList())
    val fixtures: LiveData<List<SportsMatch>> = _fixtures

    private val _standings = MutableLiveData<List<StandingEntry>>(emptyList())
    val standings: LiveData<List<StandingEntry>> = _standings

    private val _eventStats = MutableLiveData<Map<String, List<SportsEventStat>>>(emptyMap())
    val eventStats: LiveData<Map<String, List<SportsEventStat>>> = _eventStats

    private val _searchResults = MutableLiveData<List<SportsTeam>>(emptyList())
    val searchResults: LiveData<List<SportsTeam>> = _searchResults

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        loadFollowing()
    }

    fun loadFollowing() {
        viewModelScope.launch {
            _isLoading.value = true
            sportsRepository.getFollowing().onSuccess {
                _followedTeams.value = it
                if (_selectedTeam.value == null) {
                    loadFixturesAndResults(null)
                }
            }
            _isLoading.value = false
        }
    }

    fun selectTeam(team: SportsTeam?) {
        _selectedTeam.value = team
        loadFixturesAndResults(team?.id)
        team?.let { loadStandings(it.leagueId) }
    }

    fun loadFixturesAndResults(teamId: String?) {
        viewModelScope.launch {
            _isLoading.value = true
            sportsRepository.getResults(teamId).onSuccess { _results.value = it }
            sportsRepository.getFixtures(teamId).onSuccess { _fixtures.value = it }
            _isLoading.value = false
        }
    }

    fun loadStandings(leagueId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            sportsRepository.getStandings(leagueId).onSuccess { _standings.value = it }
            _isLoading.value = false
        }
    }

    fun loadEventStats(eventId: String) {
        if (_eventStats.value?.containsKey(eventId) == true) return
        viewModelScope.launch {
            sportsRepository.getEventStats(eventId).onSuccess { stats ->
                val current = _eventStats.value.orEmpty().toMutableMap()
                current[eventId] = stats
                _eventStats.value = current
            }
        }
    }

    fun searchTeams(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            sportsRepository.searchTeams(query).onSuccess { _searchResults.value = it }
        }
    }

    fun followTeam(team: SportsTeam) {
        viewModelScope.launch {
            sportsRepository.followTeam(team.id, team.leagueId).onSuccess {
                loadFollowing()
            }
        }
    }

    fun unfollowTeam(teamId: String) {
        viewModelScope.launch {
            sportsRepository.unfollowTeam(teamId).onSuccess {
                loadFollowing()
            }
        }
    }
}
