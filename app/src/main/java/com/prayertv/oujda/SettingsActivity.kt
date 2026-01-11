package com.prayertv.oujda

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * شاشة الإعدادات
 */
class SettingsActivity : AppCompatActivity() {
    
    private val prefs by lazy { getSharedPreferences("PrayerTVPrefs", MODE_PRIVATE) }
    
    private lateinit var soundSwitch: Switch
    private lateinit var ramadanSwitch: Switch
    private lateinit var fajrIqamaDuration: EditText
    private lateinit var dhuhrIqamaDuration: EditText
    private lateinit var asrIqamaDuration: EditText
    private lateinit var maghribIqamaDuration: EditText
    private lateinit var ishaIqamaDuration: EditText
    private lateinit var prayerDuration: EditText
    private lateinit var newPin: EditText
    private lateinit var saveButton: Button
    private lateinit var cancelButton: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        initViews()
        loadSettings()
        setupListeners()
    }
    
    private fun initViews() {
        soundSwitch = findViewById(R.id.soundSwitch)
        ramadanSwitch = findViewById(R.id.ramadanSwitch)
        fajrIqamaDuration = findViewById(R.id.fajrIqamaDuration)
        dhuhrIqamaDuration = findViewById(R.id.dhuhrIqamaDuration)
        asrIqamaDuration = findViewById(R.id.asrIqamaDuration)
        maghribIqamaDuration = findViewById(R.id.maghribIqamaDuration)
        ishaIqamaDuration = findViewById(R.id.ishaIqamaDuration)
        prayerDuration = findViewById(R.id.prayerDuration)
        newPin = findViewById(R.id.newPin)
        saveButton = findViewById(R.id.saveButton)
        cancelButton = findViewById(R.id.cancelButton)
    }
    
    private fun loadSettings() {
        soundSwitch.isChecked = prefs.getBoolean("sound_enabled", true)
        ramadanSwitch.isChecked = prefs.getBoolean("ramadan_mode", false)
        fajrIqamaDuration.setText(prefs.getInt("fajr_iqama", 20).toString())
        dhuhrIqamaDuration.setText(prefs.getInt("dhuhr_iqama", 15).toString())
        asrIqamaDuration.setText(prefs.getInt("asr_iqama", 15).toString())
        maghribIqamaDuration.setText(prefs.getInt("maghrib_iqama", 10).toString())
        ishaIqamaDuration.setText(prefs.getInt("isha_iqama", 15).toString())
        prayerDuration.setText(prefs.getInt("prayer_duration", 30).toString())
    }
    
    private fun setupListeners() {
        saveButton.setOnClickListener {
            saveSettings()
        }
        
        cancelButton.setOnClickListener {
            finish()
        }
    }
    
    private fun saveSettings() {
        try {
            val editor = prefs.edit()
            
            editor.putBoolean("sound_enabled", soundSwitch.isChecked)
            editor.putBoolean("ramadan_mode", ramadanSwitch.isChecked)
            editor.putInt("fajr_iqama", fajrIqamaDuration.text.toString().toIntOrNull() ?: 20)
            editor.putInt("dhuhr_iqama", dhuhrIqamaDuration.text.toString().toIntOrNull() ?: 15)
            editor.putInt("asr_iqama", asrIqamaDuration.text.toString().toIntOrNull() ?: 15)
            editor.putInt("maghrib_iqama", maghribIqamaDuration.text.toString().toIntOrNull() ?: 10)
            editor.putInt("isha_iqama", ishaIqamaDuration.text.toString().toIntOrNull() ?: 15)
            editor.putInt("prayer_duration", prayerDuration.text.toString().toIntOrNull() ?: 30)
            
            // Save new PIN if provided
            val newPinText = newPin.text.toString()
            if (newPinText.isNotEmpty() && newPinText.length == 4) {
                editor.putString("pin", newPinText)
            }
            
            editor.apply()
            
            Toast.makeText(this, "تم حفظ الإعدادات بنجاح", Toast.LENGTH_SHORT).show()
            finish()
        } catch (e: Exception) {
            Toast.makeText(this, "حدث خطأ أثناء الحفظ", Toast.LENGTH_SHORT).show()
        }
    }
}
