package com.pca.gomusic.Activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.ndk.CrashlyticsNdk
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.R
import io.fabric.sdk.android.Fabric

class WelcomeScreen : Activity() {
    private var permissionStorage = 0
    private var permissionWriteStorage = 0
    private var permissionLocation = 0
    private var currentTheme: String? = null
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Fabric.with(this, Crashlytics(), CrashlyticsNdk())
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = sharedPreferences.getString(Constant.THEME, "default")

        when {
            currentTheme.equals("night") -> setTheme(R.style.AppTheme_MainDark)
            else -> setTheme(R.style.AppTheme)
        }

        setContentView(R.layout.activity_welcome_screen)
        permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
        permissionWriteStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        val runnable = Runnable { startActivity(Intent(this@WelcomeScreen, PermissionActivity::class.java)) }
        val main = Runnable { startActivity(Intent(this@WelcomeScreen, MainActivity::class.java)) }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            if (permissionStorage != PackageManager.PERMISSION_GRANTED || permissionWriteStorage != PackageManager.PERMISSION_GRANTED || permissionLocation != PackageManager.PERMISSION_GRANTED) {
                Handler().postDelayed(runnable, 1000)
            } else {
                Handler().postDelayed(main, 2000)
            }
        } else {
            if (permissionStorage != PackageManager.PERMISSION_GRANTED || permissionLocation != PackageManager.PERMISSION_GRANTED || permissionWriteStorage != PackageManager.PERMISSION_GRANTED) {
                Handler().postDelayed(runnable, 1000)
            } else {
                Handler().postDelayed(main, 2000)
            }
        }
    }
}