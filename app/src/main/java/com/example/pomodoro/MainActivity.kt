package com.example.pomodoro

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pomodoro.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), TimerListener {

    private lateinit var binding: ActivityMainBinding

    private val timerAdapter = TimerAdapter(this)
    private val timers = mutableListOf<Timer>()

    private var nextId = 0
    private var currentTimerId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = timerAdapter
        }

        binding.addNewTimerButton.setOnClickListener {
            if (binding.enterTimerMinutes.text.isNotEmpty()) {
                val ms = binding.enterTimerMinutes.text.toString().toInt() * 1000L
                timers.add(Timer(nextId++, ms, ms, ms, false, false))
                timerAdapter.submitList(timers.toList())
            } else {
                Toast.makeText(this, "Input minutes count", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun start(id: Int, position: Int) {
        changeStopwatch(id, timers[position].stopTime, true)

        // Need to check
        // Stop previous timer
        if (currentTimerId >= 0 && timers.size > 1 && currentTimerId != position) {
            timers[currentTimerId].stopTime = timers[currentTimerId].currentTime
            stop(timers[currentTimerId].id, timers[currentTimerId].currentTime)
        }
        currentTimerId = position
    }

    override fun stop(id: Int, currentTime: Long) {
        changeStopwatch(id, currentTime, false)
    }

    override fun delete(id: Int, position: Int) {
        checkCurrentTimer(position)
        timers.remove(timers.find { it.id == id })
        timerAdapter.submitList(timers.toList())
    }

    private fun changeStopwatch(id: Int, currentTime: Long?, isStarted: Boolean) {
        val newTimers = mutableListOf<Timer>()
        timers.forEach {
            if (it.id == id) {
                newTimers.add(Timer(it.id, it.fullTime, currentTime ?: it.currentTime, it.stopTime, isStarted, it.isFinished))
            } else {
                newTimers.add(it)
            }
        }
        timerAdapter.submitList(newTimers)
        timers.clear()
        timers.addAll(newTimers)
    }

    private fun checkCurrentTimer(id: Int) {

        // Need to check
        //Make less currentTimerId because delete item which before it in list
        if (id <= currentTimerId) {
            currentTimerId--
        }
    }
}