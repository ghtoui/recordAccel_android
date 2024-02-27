//package com.moritoui.recordaccel.worker

//@HiltWorker
//class SensorCollectWorker @AssistedInject constructor(
//    @Assisted appContext: Context,
//    @Assisted params: WorkerParameters,
//    private val sensorCollectSender: SensorCollectSender
//) : CoroutineWorker(appContext, params) {
//    override suspend fun doWork(): Result {
//        Log.d("test", "work start!")
//        sensorCollectSender.updateAndSumlizeData()
//        return Result.success()
//    }
//}
