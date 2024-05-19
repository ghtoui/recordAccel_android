package com.moritoui.recordaccel.model

import java.time.LocalDateTime

data class AccData(
    val resultAcc: Double,
    val date: LocalDateTime,
    val isMove: Boolean = resultAcc !in (0.0 .. 0.2)
)
