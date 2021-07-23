package com.example.pomodoro

import android.content.res.Resources
import android.graphics.drawable.AnimationDrawable
import android.os.CountDownTimer
import android.view.View
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.RecyclerView
import com.example.pomodoro.databinding.TimerItemBinding

class TimerViewHolder(
    private val binding: TimerItemBinding,
    private val listener: TimerListener,
    private val resources: Resources
) : RecyclerView.ViewHolder(binding.root) {

    private var countDownTimer: CountDownTimer? = null

    fun bind(timer: Timer) {
        setIsRecyclable(true)
        binding.timerText.text = timer.currentTime.displayTime()

        binding.customView.setPeriod(timer.fullTime)

        if (timer.isStarted) {
            startTimer(timer)
        } else {
            stopTimer(timer)
        }

        if (timer.isFinished) {
            binding.startPauseButton.text = "start"
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
                listener.stop(timer.id, timer.currentTime)
            } else {
                listener.start(timer.id, adapterPosition)
            }
        }

        binding.deleteButton.setOnClickListener {
            setIsRecyclable(true)
            stopTimer(timer)
            timer.isStarted = false
            listener.delete(timer.id, adapterPosition)
        }
    }

    private fun startTimer(timer: Timer) {
        setIsRecyclable(false)

        timer.isFinished = false
        binding.timerLayout.setBackgroundColor(resources.getColor(R.color.transparent, null))

        countDownTimer?.cancel()
        countDownTimer = getCountDownTimer(timer)
        countDownTimer?.start()

        binding.startPauseButton.text = "stop"
        binding.blinkingIndicator.isInvisible = false
        (binding.blinkingIndicator.background as? AnimationDrawable)?.start()
    }

    private fun stopTimer(timer: Timer) {
        binding.startPauseButton.text = "start"

        binding.customView.setCurrent(timer.fullTime - timer.currentTime)
        countDownTimer?.cancel()

        binding.blinkingIndicator.isInvisible = true
        (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
    }

    private fun getCountDownTimer(timer: Timer): CountDownTimer {
        return object : CountDownTimer(timer.currentTime, 10L) {

            override fun onTick(millisToFinish: Long) {
                timer.currentTime = millisToFinish
                binding.timerText.text = millisToFinish.displayTime()
                binding.customView.setCurrent(timer.fullTime - timer.currentTime)
                listener.saveTimerState(timer.id, millisToFinish)
            }

            override fun onFinish() {
                timer.isFinished = true
                binding.timerLayout.setBackgroundColor(resources.getColor(R.color.purple_200, null))
                binding.customView.setCurrent(timer.fullTime - timer.currentTime)
                binding.blinkingIndicator.visibility = View.INVISIBLE
                binding.startPauseButton.isEnabled = false
                binding.startPauseButton.text = "STARt"
                setIsRecyclable(true)
                (binding.blinkingIndicator.background as? AnimationDrawable)?.stop()
            }
        }
    }
}