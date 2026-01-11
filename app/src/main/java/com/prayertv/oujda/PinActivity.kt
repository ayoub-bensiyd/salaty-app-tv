package com.prayertv.oujda

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

/**
 * شاشة إدخال رمز PIN
 */
class PinActivity : AppCompatActivity() {
    
    private lateinit var pinInput: EditText
    private lateinit var pinError: TextView
    private val prefs by lazy { getSharedPreferences("PrayerTVPrefs", MODE_PRIVATE) }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin)
        
        pinInput = findViewById(R.id.pinInput)
        pinError = findViewById(R.id.pinError)
        
        pinInput.requestFocus()
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        // Handle remote control number keys
        when (keyCode) {
            KeyEvent.KEYCODE_0, KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_2,
            KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_5,
            KeyEvent.KEYCODE_6, KeyEvent.KEYCODE_7, KeyEvent.KEYCODE_8,
            KeyEvent.KEYCODE_9 -> {
                val digit = keyCode - KeyEvent.KEYCODE_0
                val currentText = pinInput.text.toString()
                if (currentText.length < 4) {
                    pinInput.setText(currentText + digit)
                }
                return true
            }
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                verifyPin()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                finish()
                return true
            }
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private fun verifyPin() {
        val enteredPin = pinInput.text.toString()
        val savedPin = prefs.getString("pin", "1234") ?: "1234"
        
        if (enteredPin == savedPin) {
            // PIN correct, go to settings
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            // PIN incorrect
            pinError.visibility = TextView.VISIBLE
            pinInput.setText("")
            
            // Hide error after 2 seconds
            pinInput.postDelayed({
                pinError.visibility = TextView.GONE
            }, 2000)
        }
    }
}
