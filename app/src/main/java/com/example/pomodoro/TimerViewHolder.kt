package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.util.Log
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.TimerItemBinding
import kotlinx.coroutines.*

class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var timerJob: Job? = null
    private var circleJob: Job? = null

    fun bind(timer: Timer) {
        binding.timerText.text = timer.currentTime.displayTime()

        if (timer.isStarted) {
            startTimer(timer)
        } else {
            stopTimer(timer)
        }

        if (timer.isFinished) {
            binding.timerLayout.setBackgroundColor(resources.getColor(R.color.purple_200, null))
        } else {
            binding.timerLayout.setBackgroundColor(resources.getColor(R.color.transparent, null))
        }

        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startPauseButton.setOnClickListener {
            if (timer.isStarted) {
                timer.stopTime = timer.currentTime
                listener.stop(timer.id, timer.currentTime)
            } else {
                listener.start(timer.id, adapterPosition)
            }
        }

        binding.deleteButton.setOnClickListener { listener.delete(timer.id, adapterPosition) }
    }

    private fun startTimer(timer: Timer) {
        timer.isFinished = false
        binding.timerLayout.setBackgroundColor(resources.getColor(R.color.transparent, null))

        binding.startPauseButton.text = "stop"

        val startTime = System.currentTimeMillis()

        timerJob = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                timer.currentTime = timer.stopTime - System.currentTimeMillis() + startTime
                binding.timerText.text = (timer.currentTime).displayTime()
                delay(INTERVAL)

                // may restart time faster than time over
                if (binding.timerText.text.toString() == ZERO_TIME) {
                    Log.e("AAA", "timerText.text ${binding.timerText.text}")
                    binding.timerText.text = timer.fullTime.displayTime()
                    binding.timerLayout.setBackgroundColor(
                        resources.getColor(
                            R.color.purple_200,
                            null
                        )
                    )
                    timer.isFinished = true
                    listener.stop(timer.id, timer.fullTime)
                    timerJob?.cancel()
                    return@launch
                }
            }
        }

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()

//        binding.customView.setPeriod(timer.time)
//
//        circleJob = GlobalScope.launch(Dispatchers.Main) {
//            while (current < stopwatch.time) {
//                Log.e("AAA", "${stopwatch.time} stopwatch.time")
//                current += UNIT_TEN_MS
//                Log.e("AAA", "$current current")
//                binding.customViewTwo.setCurrent(current)
//                delay(UNIT_TEN_MS)
//            }
//        }
    }

    private fun stopTimer(timer: Timer) {
        binding.startPauseButton.text = "start"

        timerJob?.cancel()
        circleJob?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }


    private fun Long.displayTime(): String {
        if (this <= 0L) {
            return ZERO_TIME
        }
        val h = this / 1000 / 3600
        val m = this / 1000 % 3600 / 60
        val s = this / 1000 % 60
        val ms = this % 1000 / 10

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}:${displaySlot(ms)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val ZERO_TIME = "00:00:00:00"
        private const val INTERVAL = 10L
        private const val UNIT_TEN_MS = 10L
        private const val PERIOD = 1000L * 60L * 60L * 24L // Day
    }
}