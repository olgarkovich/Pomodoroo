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

    private var current = 0L

    fun bind(timer: Timer) {
        binding.timerText.text = timer.currentTime.displayTime()
        Log.e("III", "24 . currentTime ${timer.currentTime}")

        if (timer.currentSecond == 0L) {
            binding.customView.setCurrent(0L)
        }

        if (timer.isStarted) {
            startTimer(timer)
        } else {
            stopTimer(timer)
        }

        if (timer.isFinished) {
            binding.startPauseButton.isEnabled = false
            binding.timerLayout.setBackgroundColor(resources.getColor(R.color.purple_200, null))
        } else {
            binding.startPauseButton.isEnabled = true
            binding.timerLayout.setBackgroundColor(resources.getColor(R.color.transparent, null))
        }

        initButtonsListeners(timer)
    }

    private fun initButtonsListeners(timer: Timer) {
        binding.startPauseButton.setOnClickListener {
            if (timer.isStarted) {
                Log.e("III", "50 . currentTime ${timer.currentTime}")
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
        binding.customView.setPeriod(timer.fullTime)

        val startTime = System.currentTimeMillis()

        timerJob = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                timer.currentTime = timer.stopTime - System.currentTimeMillis() + startTime
                Log.e("III", "72 . currentTime ${timer.currentTime}")
                binding.timerText.text = (timer.currentTime).displayTime()
                listener.saveState(timer.id, adapterPosition, timer.currentTime)

                timer.currentSecond += INTERVAL
                binding.customView.setCurrent(timer.currentSecond)

                if (binding.timerText.text.toString() == ZERO_TIME) {
                    binding.timerLayout.setBackgroundColor(
                        resources.getColor(
                            R.color.purple_200,
                            null
                        )
                    )

                    binding.customView.setCurrent(timer.fullTime - 1L)

                    timer.isFinished = true
                    listener.stop(timer.id, 0L)
                    timerJob?.cancel()
                    return@launch
                }
                delay(INTERVAL)
            }
        }

        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(timer: Timer) {
        binding.startPauseButton.text = "start"

        if (timer.currentSecond > 0) {
            timer.currentSecond -= 1000L
        }

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

        return "${displaySlot(h)}:${displaySlot(m)}:${displaySlot(s)}"
    }

    private fun displaySlot(count: Long): String {
        return if (count / 10L > 0) {
            "$count"
        } else {
            "0$count"
        }
    }

    private companion object {

        private const val ZERO_TIME = "00:00:00"
        private const val INTERVAL = 1000L
    }
}