package de.simles.timetracker.storage

import de.simles.timetracker.models.Comment

interface CommentStorageHandler {
    suspend fun getMonth(year: Int, month: Int): Result<List<Comment>>
    suspend fun setMonth(year: Int, month: Int, comments: List<Comment>)
}