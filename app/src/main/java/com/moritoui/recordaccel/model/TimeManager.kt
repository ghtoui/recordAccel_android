package com.moritoui.recordaccel.model

import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

class TimeManager {
    private val customDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    fun textToDate(dateText: String): LocalDateTime {
        return LocalDateTime.parse(dateText, customDateFormatter)
    }

    fun dateToText(datetime: LocalDateTime): String {
        return datetime.format(customDateFormatter)
    }

    fun stringToEpochTime(stringDateTime: String): Long {
        val datetime = textToDate(stringDateTime)
        val instant = datetime.atZone(ZoneId.systemDefault()).toInstant()
        return instant.toEpochMilli()
    }

    fun diffEpochTime(targetEpochTime: Long, baseEpochTime: Long): String {
        val diffEpochTime = targetEpochTime - baseEpochTime
        // 差分の秒数 * 1000 で出るので、1000で割ってから日数で割る
        return (diffEpochTime / 1000 / 24 / 60 / 60).toString()
    }
}
