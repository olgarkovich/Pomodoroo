package com.example.pomodoro

interface TimerListener {

    fun start(id: Int, position: Int)

    fun stop(id: Int, currentTime: Long)

    fun delete(id: Int, position: Int)

    fun saveState(id: Int, position: Int, currentTime: Long)
}