package com.example.pomodoro

data class Timer(
    val id: Int,
    var fullTime: Long,
    var currentTime: Long,
    var currentSecond: Long,
    var stopTime: Long,
    var isStarted: Boolean,
    var isFinished: Boolean
)