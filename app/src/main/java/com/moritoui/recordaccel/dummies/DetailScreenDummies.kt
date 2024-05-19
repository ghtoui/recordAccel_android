package com.moritoui.recordaccel.dummies

import com.moritoui.recordaccel.model.AccDataList

object DetailScreenDummies {
    val accDataList = AccDataList.getAccDataList()
    val dateList = accDataList.groupBy { it.date }.keys.toList()
    val selectDate = dateList.last()
}
