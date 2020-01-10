package com.pca.gomusic.Activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import com.pca.gomusic.Adapters.ThemeColorAdapter
import com.pca.gomusic.Classes.ThemeColors
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.R
import kotlinx.android.synthetic.main.activity_settings.*

class SettingsActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var currentTheme: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
         sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
         currentTheme = sharedPreferences.getString(Constant.THEME, "default")


        when {
            currentTheme.equals("night") -> setTheme(R.style.AppTheme_MainDark)
            else -> setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_settings)
        editor = sharedPreferences.edit()


        when{
            currentTheme.equals("night") -> back.setImageResource(R.drawable.ic_back_white)
            currentTheme.equals("mimi") ||  currentTheme.equals("cyran") || currentTheme.equals("default") -> back.setImageResource(R.drawable.ic_back_black)
            else -> back.setImageResource(R.drawable.ic_close_accent)
        }


        theme_colors.layoutManager = GridLayoutManager(this, 4)
        theme_colors.adapter = ThemeColorAdapter(this, ThemeColors().themeColors())


        back.setOnClickListener {
            val hasChanged = sharedPreferences.getBoolean(Constant.hasChanged, false)
            if (hasChanged) {
                startActivity(Intent(this, MainActivity::class.java))
                editor.putBoolean(Constant.hasChanged, false)
                editor.apply()
            }else{
                finish()
            }
        }



        privacy.setOnClickListener {
            val privacyDialog = AlertDialog.Builder(this)
            val view = LayoutInflater.from(this).inflate(R.layout.privacy_dialog, null, false)
            privacyDialog.setView(view)
            val show = privacyDialog.create()
            show.show()
        }
    }





    override fun onBackPressed() {
        val hasChanged = sharedPreferences.getBoolean(Constant.hasChanged, false)
        if (hasChanged) {
            startActivity(Intent(this, MainActivity::class.java))
            editor.putBoolean(Constant.hasChanged, false)
            editor.apply()
        }else{
            finish()
        }
    }
}
