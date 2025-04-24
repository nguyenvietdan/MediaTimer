package com.monkey.mediatimer.common

/**
 * Lớp đại diện cho trạng thái hiện tại của hẹn giờ
 */
sealed class TimerState {
    abstract fun copy(): TimerState

    object Inactive : TimerState() {
        override fun copy(): Inactive = Inactive
    }

    sealed class Active(
        open val startTimeMillis: Long,
        open val remainingMillis: Long,
        open val totalDurationMillis: Long,
        open val useSleepMode: Boolean
    ) : TimerState() {

        val progressPercent: Float
            get() = 1f - (remainingMillis.toFloat() / totalDurationMillis.toFloat())
    }


    data class Running(
        override val startTimeMillis: Long,
        val endTimeMillis: Long,
        override val remainingMillis: Long,
        override val totalDurationMillis: Long,
        override val useSleepMode: Boolean
    ) : Active(startTimeMillis, remainingMillis, totalDurationMillis, useSleepMode) {
        override fun copy() = Running(
            startTimeMillis, endTimeMillis, remainingMillis, totalDurationMillis, useSleepMode
        )
    }

    data class Paused(
        override val startTimeMillis: Long,
        val pausedAtMillis: Long,
        override val remainingMillis: Long,
        override val totalDurationMillis: Long,
        override val useSleepMode: Boolean
    ) : Active(startTimeMillis, remainingMillis, totalDurationMillis, useSleepMode) {
        override fun copy() = Paused(
            startTimeMillis,
            pausedAtMillis,
            remainingMillis,
            totalDurationMillis,
            useSleepMode
        )
    }
}