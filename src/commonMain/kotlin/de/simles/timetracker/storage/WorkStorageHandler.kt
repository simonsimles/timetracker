package de.simles.timetracker.storage

import de.simles.timetracker.models.Work

interface WorkStorageHandler {
    suspend fun getYears(): List<Int>
    suspend fun getWeeks(year: Int): List<Int>
    suspend fun getWork(year: Int, week: Int): Result<List<Work>>
    suspend fun setWork(year: Int, week: Int, work: List<Work>)
}