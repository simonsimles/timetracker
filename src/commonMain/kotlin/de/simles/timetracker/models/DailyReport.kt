package de.simles.timetracker.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class DailyReportEntry(val date: LocalDate, val duration: Time, val comment: String, val project: String)
