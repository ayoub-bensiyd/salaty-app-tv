package com.prayertv.oujda

import android.content.Context
import fi.iki.elonen.NanoHTTPD
import org.json.JSONObject

/**
 * خادم ويب محلي للتحكم عبر الهاتف
 */
class LocalServer(private val context: Context, port: Int = 8080) : NanoHTTPD(port) {
    
    private val prefs = context.getSharedPreferences("PrayerTVPrefs", Context.MODE_PRIVATE)
    
    override fun serve(session: IHTTPSession): Response {
        val uri = session.uri
        
        return when {
            uri == "/" -> serveWebInterface()
            uri == "/api/settings" && session.method == Method.GET -> getSettings()
            uri == "/api/settings" && session.method == Method.POST -> saveSettings(session)
            uri == "/api/toggle_sound" -> toggleSound()
            uri == "/api/toggle_ramadan" -> toggleRamadan()
            else -> newFixedLengthResponse(Response.Status.NOT_FOUND, MIME_PLAINTEXT, "Not Found")
        }
    }
    
    private fun serveWebInterface(): Response {
        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>التحكم في أوقات الصلاة</title>
                <style>
                    * {
                        margin: 0;
                        padding: 0;
                        box-sizing: border-box;
                    }
                    body {
                        font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                        background: linear-gradient(135deg, #1B5E20 0%, #2E7D32 100%);
                        min-height: 100vh;
                        padding: 20px;
                        direction: rtl;
                    }
                    .container {
                        max-width: 600px;
                        margin: 0 auto;
                        background: white;
                        border-radius: 20px;
                        padding: 30px;
                        box-shadow: 0 10px 40px rgba(0,0,0,0.3);
                    }
                    h1 {
                        color: #1B5E20;
                        text-align: center;
                        margin-bottom: 30px;
                        font-size: 32px;
                    }
                    .setting-group {
                        margin-bottom: 25px;
                        padding: 20px;
                        background: #F5F5F5;
                        border-radius: 10px;
                    }
                    .setting-row {
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        margin-bottom: 15px;
                    }
                    .setting-row:last-child {
                        margin-bottom: 0;
                    }
                    label {
                        font-size: 18px;
                        color: #333;
                        font-weight: 500;
                    }
                    input[type="number"] {
                        width: 80px;
                        padding: 10px;
                        border: 2px solid #1B5E20;
                        border-radius: 8px;
                        font-size: 16px;
                        text-align: center;
                    }
                    .switch {
                        position: relative;
                        display: inline-block;
                        width: 60px;
                        height: 34px;
                    }
                    .switch input {
                        opacity: 0;
                        width: 0;
                        height: 0;
                    }
                    .slider {
                        position: absolute;
                        cursor: pointer;
                        top: 0;
                        left: 0;
                        right: 0;
                        bottom: 0;
                        background-color: #ccc;
                        transition: .4s;
                        border-radius: 34px;
                    }
                    .slider:before {
                        position: absolute;
                        content: "";
                        height: 26px;
                        width: 26px;
                        left: 4px;
                        bottom: 4px;
                        background-color: white;
                        transition: .4s;
                        border-radius: 50%;
                    }
                    input:checked + .slider {
                        background-color: #1B5E20;
                    }
                    input:checked + .slider:before {
                        transform: translateX(26px);
                    }
                    button {
                        width: 100%;
                        padding: 15px;
                        background: #1B5E20;
                        color: white;
                        border: none;
                        border-radius: 10px;
                        font-size: 20px;
                        font-weight: bold;
                        cursor: pointer;
                        transition: background 0.3s;
                        margin-top: 10px;
                    }
                    button:hover {
                        background: #2E7D32;
                    }
                    button:active {
                        transform: scale(0.98);
                    }
                    .status {
                        text-align: center;
                        padding: 15px;
                        margin-top: 20px;
                        border-radius: 10px;
                        font-size: 16px;
                        display: none;
                    }
                    .status.success {
                        background: #C8E6C9;
                        color: #1B5E20;
                        display: block;
                    }
                    .status.error {
                        background: #FFCDD2;
                        color: #C62828;
                        display: block;
                    }
                    h2 {
                        color: #1B5E20;
                        margin-bottom: 15px;
                        font-size: 22px;
                    }
                </style>
            </head>
            <body>
                <div class="container">
                    <h1>🕌 التحكم في أوقات الصلاة</h1>
                    
                    <div class="setting-group">
                        <h2>الإعدادات العامة</h2>
                        <div class="setting-row">
                            <label>تفعيل الصوت</label>
                            <label class="switch">
                                <input type="checkbox" id="soundEnabled">
                                <span class="slider"></span>
                            </label>
                        </div>
                        <div class="setting-row">
                            <label>وضع رمضان</label>
                            <label class="switch">
                                <input type="checkbox" id="ramadanMode">
                                <span class="slider"></span>
                            </label>
                        </div>
                    </div>
                    
                    <div class="setting-group">
                        <h2>مدة الإقامة (دقيقة)</h2>
                        <div class="setting-row">
                            <label>الفجر</label>
                            <input type="number" id="fajrIqama" min="5" max="60" value="20">
                        </div>
                        <div class="setting-row">
                            <label>الظهر</label>
                            <input type="number" id="dhuhrIqama" min="5" max="60" value="15">
                        </div>
                        <div class="setting-row">
                            <label>العصر</label>
                            <input type="number" id="asrIqama" min="5" max="60" value="15">
                        </div>
                        <div class="setting-row">
                            <label>المغرب</label>
                            <input type="number" id="maghribIqama" min="5" max="60" value="10">
                        </div>
                        <div class="setting-row">
                            <label>العشاء</label>
                            <input type="number" id="ishaIqama" min="5" max="60" value="15">
                        </div>
                    </div>
                    
                    <div class="setting-group">
                        <h2>إعدادات أخرى</h2>
                        <div class="setting-row">
                            <label>مدة الصلاة (دقيقة)</label>
                            <input type="number" id="prayerDuration" min="10" max="120" value="30">
                        </div>
                    </div>
                    
                    <button onclick="saveSettings()">💾 حفظ الإعدادات</button>
                    
                    <div id="status" class="status"></div>
                </div>
                
                <script>
                    // Load settings on page load
                    window.onload = function() {
                        fetch('/api/settings')
                            .then(response => response.json())
                            .then(data => {
                                document.getElementById('soundEnabled').checked = data.soundEnabled;
                                document.getElementById('ramadanMode').checked = data.ramadanMode;
                                document.getElementById('fajrIqama').value = data.fajrIqama;
                                document.getElementById('dhuhrIqama').value = data.dhuhrIqama;
                                document.getElementById('asrIqama').value = data.asrIqama;
                                document.getElementById('maghribIqama').value = data.maghribIqama;
                                document.getElementById('ishaIqama').value = data.ishaIqama;
                                document.getElementById('prayerDuration').value = data.prayerDuration;
                            })
                            .catch(error => console.error('Error:', error));
                    };
                    
                    function saveSettings() {
                        const settings = {
                            soundEnabled: document.getElementById('soundEnabled').checked,
                            ramadanMode: document.getElementById('ramadanMode').checked,
                            fajrIqama: parseInt(document.getElementById('fajrIqama').value),
                            dhuhrIqama: parseInt(document.getElementById('dhuhrIqama').value),
                            asrIqama: parseInt(document.getElementById('asrIqama').value),
                            maghribIqama: parseInt(document.getElementById('maghribIqama').value),
                            ishaIqama: parseInt(document.getElementById('ishaIqama').value),
                            prayerDuration: parseInt(document.getElementById('prayerDuration').value)
                        };
                        
                        fetch('/api/settings', {
                            method: 'POST',
                            headers: {
                                'Content-Type': 'application/json',
                            },
                            body: JSON.stringify(settings)
                        })
                        .then(response => response.json())
                        .then(data => {
                            showStatus('تم حفظ الإعدادات بنجاح! ✓', 'success');
                        })
                        .catch(error => {
                            showStatus('حدث خطأ أثناء الحفظ', 'error');
                        });
                    }
                    
                    function showStatus(message, type) {
                        const status = document.getElementById('status');
                        status.textContent = message;
                        status.className = 'status ' + type;
                        setTimeout(() => {
                            status.className = 'status';
                        }, 3000);
                    }
                </script>
            </body>
            </html>
        """.trimIndent()
        
        return newFixedLengthResponse(Response.Status.OK, "text/html; charset=utf-8", html)
    }
    
    private fun getSettings(): Response {
        val json = JSONObject().apply {
            put("soundEnabled", prefs.getBoolean("sound_enabled", true))
            put("ramadanMode", prefs.getBoolean("ramadan_mode", false))
            put("fajrIqama", prefs.getInt("fajr_iqama", 20))
            put("dhuhrIqama", prefs.getInt("dhuhr_iqama", 15))
            put("asrIqama", prefs.getInt("asr_iqama", 15))
            put("maghribIqama", prefs.getInt("maghrib_iqama", 10))
            put("ishaIqama", prefs.getInt("isha_iqama", 15))
            put("prayerDuration", prefs.getInt("prayer_duration", 30))
        }
        
        return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
    }
    
    private fun saveSettings(session: IHTTPSession): Response {
        val files = HashMap<String, String>()
        try {
            session.parseBody(files)
            val postData = files["postData"] ?: return newFixedLengthResponse(
                Response.Status.BAD_REQUEST, 
                "application/json", 
                "{\"error\":\"No data\"}"
            )
            
            val json = JSONObject(postData)
            
            prefs.edit().apply {
                putBoolean("sound_enabled", json.getBoolean("soundEnabled"))
                putBoolean("ramadan_mode", json.getBoolean("ramadanMode"))
                putInt("fajr_iqama", json.getInt("fajrIqama"))
                putInt("dhuhr_iqama", json.getInt("dhuhrIqama"))
                putInt("asr_iqama", json.getInt("asrIqama"))
                putInt("maghrib_iqama", json.getInt("maghribIqama"))
                putInt("isha_iqama", json.getInt("ishaIqama"))
                putInt("prayer_duration", json.getInt("prayerDuration"))
                apply()
            }
            
            return newFixedLengthResponse(
                Response.Status.OK, 
                "application/json", 
                "{\"success\":true}"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return newFixedLengthResponse(
                Response.Status.INTERNAL_ERROR, 
                "application/json", 
                "{\"error\":\"${e.message}\"}"
            )
        }
    }
    
    private fun toggleSound(): Response {
        val current = prefs.getBoolean("sound_enabled", true)
        prefs.edit().putBoolean("sound_enabled", !current).apply()
        
        val json = JSONObject().apply {
            put("soundEnabled", !current)
        }
        
        return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
    }
    
    private fun toggleRamadan(): Response {
        val current = prefs.getBoolean("ramadan_mode", false)
        prefs.edit().putBoolean("ramadan_mode", !current).apply()
        
        val json = JSONObject().apply {
            put("ramadanMode", !current)
        }
        
        return newFixedLengthResponse(Response.Status.OK, "application/json", json.toString())
    }
}
