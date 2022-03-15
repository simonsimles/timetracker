package de.simles.timetracker.models

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Comment(val date: LocalDate, val comment: String)