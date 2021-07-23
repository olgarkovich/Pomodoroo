package com.example.pomodoro

interface TimerListener {

    fun start(id: Int, position: Int)

    fun stop(id: Int, currentTime: Long)

    fun delete(id: Int, position: Int)

    fun saveTimerState(id: Int, currentTime: Long)
}