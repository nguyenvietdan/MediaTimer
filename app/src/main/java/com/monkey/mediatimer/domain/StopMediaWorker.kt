package com.monkey.mediatimer.domain

import android.content.Context
import android.util.Log
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.monkey.mediatimer.datasharing.MediaTimerSharedDataHolder
import com.monkey.mediatimer.datasharing.getEvent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class StopMediaWorker @Inject constructor(
    @ApplicationContext private val context: Context,
    private val params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val TAG = "StopMediaWorker"
    override suspend fun doWork(): Result {
        return try {
            Log.i(TAG, "doWork: ")
            stopAllMedia()
            Result.success()
        } catch (e: Throwable) {
            Log.i(TAG, "doWork: fail due to ${e.printStackTrace()}")
            Result.failure()
        }
    }

    private fun stopAllMedia() {
        runBlocking {
            Log.i(TAG, "stopAllMedia: checking ${MediaTimerSharedDataHolder.mediaTimerSharedData}")
            MediaTimerSharedDataHolder.mediaTimerSharedData?.let {
                it.getEvent<Unit>("StopMedias")?.emit(Unit)
            }
        }
    }
}