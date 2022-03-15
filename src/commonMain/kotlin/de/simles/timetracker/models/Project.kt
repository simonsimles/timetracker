package de.simles.timetracker.models

import kotlinx.serialization.Serializable

@Serializable
data class Project(val name: String, val category: String)