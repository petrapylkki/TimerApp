package com.example.timer

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.example.timer.util.prefUtil
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity() {

    enum class TimerState{
        Stopped, Paused, Running
    }
    private lateinit var timer: CountDownTimer
    private var timerLengthSeconds = 0L
    private var timerState = TimerState.Stopped
    private var secondsRemaining = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        supportActionBar?.setIcon(R.drawable.ic_timer_black_24dp)
        supportActionBar?.setTitle(this.resources.getString((R.string.app_name)))

        fab_start.setOnClickListener { v ->
            startTimer()
            timerState = TimerState.Running
            updateButtons()
        }
        fab_pause.setOnClickListener { v ->
            timer.cancel()
            timerState = TimerState.Paused
            updateButtons()
        }
        fab_stop.setOnClickListener { v ->
            timer.cancel()
            onTimerFinished()
        }
    }
    override fun onResume() {
        super.onResume()

        initTimer()
    }

    override fun onPause() {
        super.onPause()

        if (timerState == TimerState.Running) {
            timer.cancel()
        }
        else if (timerState == TimerState.Paused) {

        }
        prefUtil.setPreviousTimerLengthSeconds(timerLengthSeconds, this)
        prefUtil.setSecondsRemaining(secondsRemaining, this)
        prefUtil.setTimerState(timerState, this)
    }
    private fun initTimer(){
        timerState = prefUtil.getTimerState(this)

        if (timerState == TimerState.Stopped)
            setNewTimerLength()
        else
            setPreviousTimerLength()

        secondsRemaining = if (timerState == TimerState.Running || timerState == TimerState.Paused)
            prefUtil.getSecondsRemaining(this)
        else
            timerLengthSeconds

        if (timerState == TimerState.Running)
            startTimer()

        updateButtons()
        updateCountdownUI()
    }
    private fun onTimerFinished(){
        timerState = TimerState.Stopped

        setNewTimerLength()

        countdown.progress = 0

        prefUtil.setSecondsRemaining(timerLengthSeconds, this)
        secondsRemaining = timerLengthSeconds

        updateButtons()
        updateCountdownUI()
    }
    private fun startTimer() {
        timerState = TimerState.Running

        timer = object : CountDownTimer(secondsRemaining * 1000, 1000){
            override fun onFinish() = onTimerFinished()

            override fun onTick(millisUntilFinished: Long) {
                secondsRemaining = millisUntilFinished / 1000
                updateCountdownUI()
            }
        }.start()
    }
    private fun setNewTimerLength(){
        val lengthInMinutes = prefUtil.getTimerLength(this)
        timerLengthSeconds = (lengthInMinutes * 60L)
        countdown.max = timerLengthSeconds.toInt()
    }
    private fun setPreviousTimerLength(){
        timerLengthSeconds = prefUtil.getPreviousTimerLengthSeconds(this)
        countdown.max = timerLengthSeconds.toInt()
    }
    private fun updateCountdownUI(){
        val minutesUntilFinished = secondsRemaining / 60
        val secondsInMinuteUntilFinished = secondsRemaining - minutesUntilFinished * 60
        val secondsStr = secondsInMinuteUntilFinished.toString()
        txt_countdown.text = "$minutesUntilFinished:${
        if (secondsStr.length == 2) secondsStr 
        else "0" + secondsStr}"
        countdown.progress = (timerLengthSeconds - secondsRemaining).toInt()
    }
    private fun updateButtons(){
        when(timerState){
            TimerState.Running -> {
                fab_start.isEnabled = false
                fab_pause.isEnabled =true
                fab_stop.isEnabled = true
            }
            TimerState.Stopped -> {
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = false
            }
            TimerState.Paused -> {
                fab_start.isEnabled = true
                fab_pause.isEnabled = false
                fab_stop.isEnabled = true
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> {
                val intent = Intent(this, Settings::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
