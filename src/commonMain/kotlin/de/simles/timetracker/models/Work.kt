package de.simles.timetracker.models

import de.simles.timetracker.TimeSerializer
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable(with = TimeSerializer::class)
data class Time(val hour: Int, val minute: Int) {
    override fun toString(): String = (if (hour < 10) "0$hour" else "$hour") + ":" +
            (if (minute < 10) "0$minute" else "$minute")

    operator fun plus(time: Time) = Time(
        hour + time.hour + (minute + time.minute) / 60,
        (minute + time.minute) % 60
    )

    operator fun minus(time: Time) = Time(
        hour - time.hour - (if (minute < time.minute) 1 else 0),
        if (minute < time.minute) 60 + (minute - time.minute) else minute - time.minute
    )

    fun asDouble(): Double = hour + minute / 60.0

    companion object {
        fun now(): Time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).let {
            Time(it.hour, it.minute)
        }
    }
}

fun String.toTime(): Time? {
    val regex = Regex("""^(\d{1,2})?:(\d{1,2})?$""")
    return regex.matchEntire(this).let {
        val hour = it?.groups?.get(1)?.value?.toInt()
        val minute = it?.groups?.get(2)?.value?.toInt()
        if (hour != null && minute != null) {
            Time(hour, minute)
        } else {
            null
        }
    }
}


@Serializable
sealed class TimeUnit {
    abstract fun getTotalDuration(): Time
    abstract val date: LocalDate
    abstract fun withDate(newDate: LocalDate): TimeUnit
}

@Serializable
@SerialName("interval")
class IntervalUnit(
    override val date: LocalDate,
    var start: Time,
    var end: Time?,
    var pause: Time?
) : TimeUnit() {
    override fun getTotalDuration(): Time {
        val endOrNow = end ?: Time.now()
        val pauseOrZero = pause ?: Time(0, 0)
        return endOrNow - start - pauseOrZero
    }

    override fun withDate(newDate: LocalDate): TimeUnit = IntervalUnit(newDate, this.start, this.end, this.pause)

    fun withStart(newStart: Time) = IntervalUnit(date, newStart, end, pause)
    fun withEnd(newEnd: Time?) = IntervalUnit(date, start, newEnd, pause)
    fun withPause(newPause: Time?) = IntervalUnit(date, start, end, newPause)
}

@Serializable
@SerialName("absolute")
class AbsoluteUnit(override val date: LocalDate, var duration: Time) : TimeUnit() {
    override fun getTotalDuration(): Time = duration

    override fun withDate(newDate: LocalDate): TimeUnit = AbsoluteUnit(newDate, this.duration)
    fun withDuration(newDuration: Time) = AbsoluteUnit(date, newDuration)
}

@Serializable
data class Work(val id: Long?, val project: String, val time: TimeUnit) {
    fun withDate(date: LocalDate) = Work(this.id, this.project, this.time.withDate(date))
    fun withProject(project: String) = Work(this.id, project, this.time)
    fun withTime(time: TimeUnit) = Work(this.id, this.project, time)
}