package de.simles.timetracker.storage

import de.simles.timetracker.models.DailyReportEntry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.File
import kotlin.io.path.Path

@ExperimentalSerializationApi
object FileBasedDailyReportsStorage : FileBasedStorage(), DailyReportStorageHandler {
    private val mutex = Mutex()

    private fun getMonthFile(year: Int, month: Int, forBackup: Boolean = false): File {
        if (forBackup && backupPath != null) {
            val now = Clock.System.now().epochSeconds
            return Path(basePath, backupPath, "dailyReport", "$year", "$month-$now.json").toFile()
        }
        return Path(basePath, "dailyReport", "$year", "$month.json").toFile()
    }

    override suspend fun getDailyReports(year: Int, month: Int): Result<List<DailyReportEntry>> {
        return kotlin.runCatching {
            mutex.withLock {
                val file = getMonthFile(year, month)
                if (file.exists()) {
                    Json.decodeFromStream(file.inputStream())
                } else {
                    emptyList()
                }
            }
        }
    }

    override suspend fun setDailyReports(year: Int, month: Int, dailyReports: List<DailyReportEntry>) {
        mutex.withLock {
            val file = getMonthFile(year, month)
            if (backupPath != null && file.exists()) {
                file.copyTo(getMonthFile(year, month, forBackup = true))
            }
            if (!file.parentFile.exists()) {
                file.parentFile.mkdirs()
            }
            Json.encodeToStream(dailyReports, file.outputStream())
        }
    }
}