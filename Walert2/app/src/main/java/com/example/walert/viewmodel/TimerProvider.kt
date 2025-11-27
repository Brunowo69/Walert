package com.example.walert.viewmodel

import android.os.CountDownTimer

interface TimerProvider {
    fun create(millisInFuture: Long, countDownInterval: Long, onTick: (Long) -> Unit, onFinish: () -> Unit): CountDownTimerWrapper
}

class AndroidTimerProvider : TimerProvider {
    override fun create(millisInFuture: Long, countDownInterval: Long, onTick: (Long) -> Unit, onFinish: () -> Unit): CountDownTimerWrapper {
        return AndroidCountDownTimer(millisInFuture, countDownInterval, onTick, onFinish)
    }
}

interface CountDownTimerWrapper {
    fun start()
    fun cancel()
}

class AndroidCountDownTimer(
    millisInFuture: Long,
    countDownInterval: Long,
    private val onTickCallback: (Long) -> Unit, // Renombrado para evitar ambigüedad
    private val onFinishCallback: () -> Unit      // Renombrado para consistencia
) : CountDownTimerWrapper {

    private val timer = object : CountDownTimer(millisInFuture, countDownInterval) {
        override fun onTick(millisUntilFinished: Long) {
            onTickCallback(millisUntilFinished) // Usamos el nuevo nombre
        }

        override fun onFinish() {
            onFinishCallback() // Usamos el nuevo nombre
        }
    }

    override fun start() {
        timer.start()
    }

    override fun cancel() {
        timer.cancel()
    }
}
