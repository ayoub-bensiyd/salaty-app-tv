package com.prayertv.oujda

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.view.KeyEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.text.SimpleDateFormat
import java.util.*

/**
 * النشاط الرئيسي - عرض أوقات الصلاة
 */
class MainActivity : AppCompatActivity() {
    
    private val prefs by lazy { getSharedPreferences("PrayerTVPrefs", MODE_PRIVATE) }
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var audioPlayer: AudioPlayer
    private var localServer: LocalServer? = null
    private var wakeLock: PowerManager.WakeLock? = null
    
    // UI Elements
    private lateinit var rootLayout: RelativeLayout
    private lateinit var gregorianDate: TextView
    private lateinit var hijriDate: TextView
    private lateinit var fajrAdhan: TextView
    private lateinit var fajrIqama: TextView
    private lateinit var dhuhrAdhan: TextView
    private lateinit var dhuhrIqama: TextView
    private lateinit var asrAdhan: TextView
    private lateinit var asrIqama: TextView
    private lateinit var maghribAdhan: TextView
    private lateinit var maghribIqama: TextView
    private lateinit var ishaAdhan: TextView
    private lateinit var ishaIqama: TextView
    private lateinit var countdownText: TextView
    private lateinit var statusText: TextView
    
    private lateinit var fajrRow: LinearLayout
    private lateinit var dhuhrRow: LinearLayout
    private lateinit var asrRow: LinearLayout
    private lateinit var maghribRow: LinearLayout
    private lateinit var ishaRow: LinearLayout
    
    // Prayer times
    private var prayerTimes: PrayerCalculator.Companion.PrayerTimes? = null
    private var currentPrayer: String? = null
    private var isAdhanPlaying = false
    private var isIqamaCountdown = false
    private var iqamaTime: Calendar? = null
    private var prayerEndTime: Calendar? = null
    
    // Long press detection for PIN
    private var okPressStartTime = 0L
    private val longPressDuration = 3000L // 3 seconds
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        setContentView(R.layout.activity_main)
        
        initViews()
        initAudioPlayer()
        startLocalServer()
        calculatePrayerTimes()
        startClockUpdate()
        
        // Acquire wake lock
        val powerManager = getSystemService(POWER_SERVICE) as PowerManager
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP,
            "PrayerTV::WakeLock"
        )
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)
    }
    
    private fun initViews() {
        rootLayout = findViewById(R.id.rootLayout)
        gregorianDate = findViewById(R.id.gregorianDate)
        hijriDate = findViewById(R.id.hijriDate)
        fajrAdhan = findViewById(R.id.fajrAdhan)
        fajrIqama = findViewById(R.id.fajrIqama)
        dhuhrAdhan = findViewById(R.id.dhuhrAdhan)
        dhuhrIqama = findViewById(R.id.dhuhrIqama)
        asrAdhan = findViewById(R.id.asrAdhan)
        asrIqama = findViewById(R.id.asrIqama)
        maghribAdhan = findViewById(R.id.maghribAdhan)
        maghribIqama = findViewById(R.id.maghribIqama)
        ishaAdhan = findViewById(R.id.ishaAdhan)
        ishaIqama = findViewById(R.id.ishaIqama)
        countdownText = findViewById(R.id.countdownText)
        statusText = findViewById(R.id.statusText)
        
        fajrRow = findViewById(R.id.fajrRow)
        dhuhrRow = findViewById(R.id.dhuhrRow)
        asrRow = findViewById(R.id.asrRow)
        maghribRow = findViewById(R.id.maghribRow)
        ishaRow = findViewById(R.id.ishaRow)
    }
    
    private fun initAudioPlayer() {
        audioPlayer = AudioPlayer(this)
    }
    
    private fun startLocalServer() {
        try {
            localServer = LocalServer(this, 8080)
            localServer?.start()
            statusText.text = "Server: http://192.168.43.1:8080"
        } catch (e: Exception) {
            e.printStackTrace()
            statusText.text = "Server error"
        }
    }
    
    private fun calculatePrayerTimes() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        
        // Get adjustments from settings
        val adjustments = mapOf(
            "fajr" to prefs.getInt("fajr_adjustment", 0),
            "dhuhr" to prefs.getInt("dhuhr_adjustment", 0),
            "asr" to prefs.getInt("asr_adjustment", 0),
            "maghrib" to prefs.getInt("maghrib_adjustment", 0),
            "isha" to prefs.getInt("isha_adjustment", 0)
        )
        
        prayerTimes = PrayerCalculator.calculatePrayerTimes(year, month, day, adjustments)
        
        // Update UI
        prayerTimes?.let { times ->
            fajrAdhan.text = times.fajr
            dhuhrAdhan.text = times.dhuhr
            asrAdhan.text = times.asr
            maghribAdhan.text = times.maghrib
            ishaAdhan.text = times.isha
            
            // Calculate Iqama times
            fajrIqama.text = calculateIqamaTime(times.fajr, prefs.getInt("fajr_iqama", 20))
            dhuhrIqama.text = calculateIqamaTime(times.dhuhr, prefs.getInt("dhuhr_iqama", 15))
            asrIqama.text = calculateIqamaTime(times.asr, prefs.getInt("asr_iqama", 15))
            maghribIqama.text = calculateIqamaTime(times.maghrib, prefs.getInt("maghrib_iqama", 10))
            ishaIqama.text = calculateIqamaTime(times.isha, prefs.getInt("isha_iqama", 15))
        }
        
        // Update Hijri date
        hijriDate.text = HijriUtils.getHijriDate(calendar)
        
        // Update Gregorian date
        val dateFormat = SimpleDateFormat("EEEE، d MMMM yyyy", Locale("ar"))
        gregorianDate.text = dateFormat.format(calendar.time)
    }
    
    private fun calculateIqamaTime(adhanTime: String, durationMinutes: Int): String {
        try {
            val parts = adhanTime.split(":")
            val hours = parts[0].toInt()
            val minutes = parts[1].toInt()
            
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, hours)
            calendar.set(Calendar.MINUTE, minutes)
            calendar.add(Calendar.MINUTE, durationMinutes)
            
            return String.format("%02d:%02d", 
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
            )
        } catch (e: Exception) {
            return "--:--"
        }
    }
    
    private fun startClockUpdate() {
        handler.post(object : Runnable {
            override fun run() {
                updateClock()
                checkPrayerTime()
                handler.postDelayed(this, 1000) // Update every second
            }
        })
    }
    
    private fun updateClock() {
        val calendar = Calendar.getInstance()
        
        // Update night mode
        updateNightMode(calendar)
        
        // Update countdown if active
        if (isIqamaCountdown && iqamaTime != null) {
            updateCountdown(calendar, iqamaTime!!)
        } else if (prayerEndTime != null) {
            val now = calendar.timeInMillis
            if (now >= prayerEndTime!!.timeInMillis) {
                turnScreenOn()
                prayerEndTime = null
            }
        }
    }
    
    private fun updateNightMode(calendar: Calendar) {
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val currentMinutes = hour * 60 + minute
        
        // Get Isha and Fajr times
        prayerTimes?.let { times ->
            val ishaParts = times.isha.split(":")
            val ishaMinutes = ishaParts[0].toInt() * 60 + ishaParts[1].toInt()
            
            val fajrParts = times.fajr.split(":")
            val fajrMinutes = fajrParts[0].toInt() * 60 + fajrParts[1].toInt()
            
            // Night mode: after Isha until before Fajr
            val isNightMode = currentMinutes >= ishaMinutes || currentMinutes < fajrMinutes
            
            if (isNightMode) {
                rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_night))
                setTextColor(R.color.text_night)
            } else {
                rootLayout.setBackgroundColor(ContextCompat.getColor(this, R.color.background_day))
                setTextColor(R.color.text_day)
            }
        }
    }
    
    private fun setTextColor(colorRes: Int) {
        val color = ContextCompat.getColor(this, colorRes)
        gregorianDate.setTextColor(color)
        // Keep accent color for Hijri date
    }
    
    private fun checkPrayerTime() {
        if (isAdhanPlaying || isIqamaCountdown) return
        
        val calendar = Calendar.getInstance()
        val currentTime = String.format("%02d:%02d",
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE)
        )
        
        prayerTimes?.let { times ->
            when (currentTime) {
                times.fajr -> startAdhan("fajr")
                times.dhuhr -> startAdhan("dhuhr")
                times.asr -> startAdhan("asr")
                times.maghrib -> startAdhan("maghrib")
                times.isha -> startAdhan("isha")
            }
        }
    }
    
    private fun startAdhan(prayer: String) {
        if (!prefs.getBoolean("sound_enabled", true)) return
        
        isAdhanPlaying = true
        currentPrayer = prayer
        highlightPrayer(prayer)
        
        audioPlayer.playAdhan {
            isAdhanPlaying = false
            startIqamaCountdown(prayer)
        }
    }
    
    private fun startIqamaCountdown(prayer: String) {
        isIqamaCountdown = true
        
        val durationKey = "${prayer}_iqama"
        val duration = prefs.getInt(durationKey, 15)
        
        iqamaTime = Calendar.getInstance()
        iqamaTime?.add(Calendar.MINUTE, duration)
        
        countdownText.visibility = View.VISIBLE
        
        // Schedule Iqama
        handler.postDelayed({
            playIqama(prayer)
        }, duration * 60 * 1000L)
    }
    
    private fun updateCountdown(now: Calendar, target: Calendar) {
        val diff = target.timeInMillis - now.timeInMillis
        
        if (diff <= 0) {
            countdownText.visibility = View.GONE
            return
        }
        
        val minutes = (diff / 1000 / 60).toInt()
        val seconds = ((diff / 1000) % 60).toInt()
        
        countdownText.text = String.format("الإقامة بعد: %02d:%02d", minutes, seconds)
    }
    
    private fun playIqama(prayer: String) {
        isIqamaCountdown = false
        countdownText.visibility = View.GONE
        
        if (!prefs.getBoolean("sound_enabled", true)) {
            schedulePrayerEnd()
            return
        }
        
        audioPlayer.playIqama {
            schedulePrayerEnd()
        }
    }
    
    private fun schedulePrayerEnd() {
        val prayerDuration = prefs.getInt("prayer_duration", 30)
        
        prayerEndTime = Calendar.getInstance()
        prayerEndTime?.add(Calendar.MINUTE, prayerDuration)
        
        // Turn off screen
        turnScreenOff()
        
        unhighlightAllPrayers()
        currentPrayer = null
    }
    
    private fun highlightPrayer(prayer: String) {
        val highlightColor = ContextCompat.getColor(this, R.color.prayer_active)
        
        when (prayer) {
            "fajr" -> fajrRow.setBackgroundColor(highlightColor)
            "dhuhr" -> dhuhrRow.setBackgroundColor(highlightColor)
            "asr" -> asrRow.setBackgroundColor(highlightColor)
            "maghrib" -> maghribRow.setBackgroundColor(highlightColor)
            "isha" -> ishaRow.setBackgroundColor(highlightColor)
        }
    }
    
    private fun unhighlightAllPrayers() {
        val whiteColor = Color.WHITE
        val grayColor = Color.parseColor("#F5F5F5")
        
        fajrRow.setBackgroundColor(whiteColor)
        dhuhrRow.setBackgroundColor(grayColor)
        asrRow.setBackgroundColor(whiteColor)
        maghribRow.setBackgroundColor(grayColor)
        ishaRow.setBackgroundColor(whiteColor)
    }
    
    private fun turnScreenOff() {
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Dim the screen
        val params = window.attributes
        params.screenBrightness = 0.01f
        window.attributes = params
    }
    
    private fun turnScreenOn() {
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        
        // Restore brightness
        val params = window.attributes
        params.screenBrightness = WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
        window.attributes = params
        
        // Wake up screen
        wakeLock?.acquire(10*60*1000L /*10 minutes*/)
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Detect long press on OK/Center button
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            if (okPressStartTime == 0L) {
                okPressStartTime = System.currentTimeMillis()
            }
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
    
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
            val pressDuration = System.currentTimeMillis() - okPressStartTime
            okPressStartTime = 0L
            
            if (pressDuration >= longPressDuration) {
                // Long press detected - open PIN screen
                val intent = Intent(this, PinActivity::class.java)
                startActivity(intent)
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
        audioPlayer.stop()
        localServer?.stop()
        wakeLock?.release()
    }
    
    override fun onResume() {
        super.onResume()
        // Recalculate prayer times when returning from settings
        calculatePrayerTimes()
    }
}
