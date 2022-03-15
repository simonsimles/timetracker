package de.simles.timetracker

import de.simles.timetracker.models.Comment
import de.simles.timetracker.models.DailyReportEntry
import de.simles.timetracker.models.Project
import de.simles.timetracker.models.Work
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.utils.io.core.*

abstract class Api {
    abstract fun getClient(): HttpClient

    suspend fun getProjects(): List<Project> = getClient().use {
        it.get("/api/project")
    }

    suspend fun getYears(): List<Int> = getClient().use {
        it.get("/api/work")
    }

    suspend fun getWork(year: Int, week: Int): List<Work> = getClient().use {
        return it.get("/api/work/$year/$week")
    }

    suspend fun newProject(project: Project): Project = getClient().use {
        it.post("/api/project") {
            body = project
        }
    }

    suspend fun updateProject(project: Project): Project = getClient().use {
        it.patch("/api/project") {
            body = project
        }
    }

    suspend fun newWork(work: Work): Work = getClient().use {
        it.post("/api/work") {
            body = work
        }
    }

    suspend fun updateWork(work: Work): Work = getClient().use {
        it.patch("/api/work/${work.time.date.year}/${work.time.date.weekNumber()}/${work.id}") {
            body = work
        }
    }

    suspend fun deleteWork(work: Work): Long = getClient().use {
        it.delete("/api/work/${work.time.date.year}/${work.time.date.weekNumber()}/${work.id}")
    }

    suspend fun getComments(year: Int, month: Int): List<Comment> = getClient().use {
        it.get("/api/comment/$year/$month")
    }

    suspend fun addComment(comment: Comment): Comment = getClient().use {
        it.post("/api/comment") {
            body = comment
        }
    }

    suspend fun updateComment(comment: Comment): Comment = getClient().use {
        it.patch("/api/comment") {
            body = comment
        }
    }


    suspend fun getDailyReports(year: Int, month: Int): List<DailyReportEntry> = getClient().use {
        it.get("/api/dailyReport/$year/$month")
    }

    suspend fun addDailyReport(dailyReportEntry: DailyReportEntry): DailyReportEntry = getClient().use {
        it.post("/api/dailyReport") {
            body = dailyReportEntry
        }
    }

    suspend fun updateDailyReportEntry(dailyReportEntry: DailyReportEntry): DailyReportEntry = getClient().use {
        it.patch("/api/dailyReport") {
            body = dailyReportEntry
        }
    }
}