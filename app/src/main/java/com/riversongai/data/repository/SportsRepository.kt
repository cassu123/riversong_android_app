package com.riversongai.data.repository

import com.riversongai.data.model.*
import com.riversongai.data.remote.RiverSongApiService

class SportsRepository(private val apiService: RiverSongApiService) {

    suspend fun getFollowing() = try {
        val response = apiService.getSportsFollowing()
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getResults(teamId: String?) = try {
        val response = apiService.getSportsResults(teamId)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getFixtures(teamId: String?) = try {
        val response = apiService.getSportsFixtures(teamId)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun searchTeams(query: String) = try {
        val response = apiService.searchSportsTeams(query)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getStandings(leagueId: String) = try {
        val response = apiService.getSportsStandings(leagueId)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun getEventStats(eventId: String) = try {
        val response = apiService.getSportsEventStats(eventId)
        if (response.isSuccessful) Result.success(response.body() ?: emptyList())
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun followTeam(teamId: String, leagueId: String) = try {
        val body = mapOf("team_id" to teamId, "league_id" to leagueId)
        val response = apiService.followSportsTeam(body)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun unfollowTeam(teamId: String) = try {
        val response = apiService.unfollowSportsTeam(teamId)
        if (response.isSuccessful) Result.success(Unit)
        else Result.failure(Exception("Error ${response.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
}
