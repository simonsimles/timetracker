package de.simles.timetracker.storage

import de.simles.timetracker.models.DailyReportEntry

interface DailyReportStorageHandler {
    suspend fun getDailyReports(year: Int, month: Int): Result<List<DailyReportEntry>>
    suspend fun setDailyReports(year: Int, month: Int, dailyReports: List<DailyReportEntry>)
}