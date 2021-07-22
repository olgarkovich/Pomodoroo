package com.example.pomodoro

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding
import com.example.pomodoro.service.ForegroundService

class MainActivity : AppCompatActivity(), TimerListener, LifecycleObserver {

    private lateinit var binding: ActivityMainBinding

    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<Timer>()

    private var nextId = 0
    private var previousTimerId = -1
    private var workingTimer: Timer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            if (binding.enterTimerMinutes.text.isNotEmpty()) {
                val ms = binding.enterTimerMinutes.text.toString().toInt() * 1000L
                timers.add(Timer(nextId++, ms, ms, 0L, ms, false, false))
                timerAdapter.submitList(timers.toList())
            } else {
                Toast.makeText(this, "Input minutes count", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun start(id: Int, position: Int) {
        workingTimer = timers[position]
        changeStopwatch(id, timers[position].stopTime, true)

        // Stop previous timer
        if (previousTimerId >= 0 && timers.size > 1 && previousTimerId != position) {
            timers[previousTimerId].stopTime = timers[previousTimerId].currentTime
            stop(timers[previousTimerId].id, timers[previousTimerId].currentTime)
        }
        previousTimerId = position
    }

    override fun stop(id: Int, currentTime: Long) {
        // java.lang.ArrayIndexOutOfBoundsException: length=10; index=-1
        timers[previousTimerId].stopTime = currentTime
        changeStopwatch(id, currentTime, false)

        if (workingTimer != null) {
            if (workingTimer?.id == id) {
                workingTimer = null
            }
        }
    }

    override fun delete(id: Int, position: Int) {
        checkCurrentTimer(position)
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())

        if (workingTimer != null) {
            if (workingTimer?.id == id) {
                workingTimer = null
            }
        }
    }

    override fun saveState(id: Int, position: Int, currentTime: Long) {
        workingTimer?.currentTime = currentTime
    }

    private fun changeStopwatch(id: Int, currentTime: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Timer>()
        timers.forEach {
            if (it.id == id) {
                newTimers.add(
                    Timer(
                        it.id,
                        it.fullTime,
                        currentTime ?: it.currentTime,
                        it.currentSecond,
                        it.stopTime,
                        isStarted,
                        it.isFinished
                    )
                )
            } else {
                newTimers.add(it)
            }
        }
        timerAdapter.submitList(newTimers)
        timers.clear()
        timers.addAll(newTimers)
    }

    override fun onStop() {
        super.onStop()

    }

    private fun checkCurrentTimer(id: Int) {

        //Make less currentTimerId because delete item which before it in list
        if (id <= previousTimerId) {
            previousTimerId--
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        if (workingTimer != null) {
            val startIntent = Intent(this, ForegroundService::class.java)
            startIntent.putExtra(COMMAND_ID, COMMAND_START)
            startIntent.putExtra(STARTED_TIMER_TIME_MS, workingTimer!!.currentTime)
            startService(startIntent)
        }

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        val stopIntent = Intent(this, ForegroundService::class.java)
        stopIntent.putExtra(COMMAND_ID, COMMAND_STOP)
        startService(stopIntent)
    }
}