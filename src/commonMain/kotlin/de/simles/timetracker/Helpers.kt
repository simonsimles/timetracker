package de.simles.timetracker

import de.simles.timetracker.models.Time
import kotlinx.datetime.*

fun LocalDate.weekNumber(): Int {
    fun getFirstMonday(year: Int): LocalDate {
        val jan4 = LocalDate(year, 1, 4)
        return jan4.plus(DatePeriod(days = 1 - jan4.dayOfWeek.isoDayNumber))
    }
    if (this >= LocalDate(this.year, 12, 29)) {
        val nextYearsFirstDay = getFirstMonday(this.year + 1)
        if (this >= nextYearsFirstDay) {
            return 1
        }
    }
    return getFirstMonday(this.year).daysUntil(this) / 7 + 1
}

fun LocalDate.display(): String = "${dayOfWeek.name}, ${this}"

fun LocalDateTime.toTimeUnit(): Time = Time(this.hour, this.minute)

fun LocalDate.firstWorkDayOfWeek(): LocalDate = this.minus(
    this.dayOfWeek.isoDayNumber - DayOfWeek.MONDAY.isoDayNumber,
    DateTimeUnit.DAY
)

fun LocalDate.lastWorkDayOfWeek(): LocalDate = this.plus(
    DayOfWeek.FRIDAY.isoDayNumber - this.dayOfWeek.isoDayNumber,
    DateTimeUnit.DAY
)

fun LocalDateTime.Companion.currentDateTime(): LocalDateTime =
    Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())

fun LocalDate.Companion.currentDate(): LocalDate = LocalDateTime.currentDateTime().date

data class YearWeek(val year: Int?, val week: Int?) {
    override fun toString(): String {
        return year.toString() + "-W" + week.toString().padStart(2, '0')
    }

    fun mondayInWeek(): LocalDate? = year?.let {
        LocalDate(it, 1, 4)
            .plus(DatePeriod(days = week?.minus(1)?.times(7) ?: 0))
            .firstWorkDayOfWeek()
    }

    companion object YearWeek {
        fun current(): de.simles.timetracker.YearWeek {
            val currentDate = LocalDate.currentDate()
            return YearWeek(currentDate.year, currentDate.weekNumber())
        }
    }
}

fun String.toYearWeek(): YearWeek =
    Regex("""(\d{4})-W(\d{2})""").matchEntire(this)?.groups.let {
        YearWeek(it?.get(1)?.value?.toInt(), it?.get(2)?.value?.toInt())
    }

data class YearMonth(val year: Int?, val month: Int?) {
    override fun toString(): String {
        return year.toString() + "-" + month.toString().padStart(2, '0')
    }

    fun firstDay(): LocalDate =
        LocalDate(
            year ?: LocalDate.currentDate().year,
            month ?: LocalDate.currentDate().monthNumber,
            1
        )

    fun lastDay(): LocalDate =
        LocalDate(
            year ?: LocalDate.currentDate().year,
            month ?: LocalDate.currentDate().monthNumber,
            1
        ).plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

    companion object YearMonth {
        fun current(): de.simles.timetracker.YearMonth {
            val currentDate = LocalDate.currentDate()
            return YearMonth(currentDate.year, currentDate.monthNumber)
        }
    }
}

fun String.toYearMonth(): YearMonth =
    Regex("""(\d{4})-(\d{2})""").matchEntire(this)?.groups.let {
        YearMonth(it?.get(1)?.value?.toInt(), it?.get(2)?.value?.toInt())
    }
