package com.pca.gomusic.Activities

import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.R
import kotlinx.android.synthetic.main.activity_feedback.*

class FeedbackActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private var currentTheme: String? = null
    private lateinit var user: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = sharedPreferences.getString(Constant.THEME, "default")
        when {
            currentTheme.equals("night") -> setTheme(R.style.AppTheme_MainDark)
            else -> setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_feedback)

        user = FirebaseAuth.getInstance()

        toolbar.setTitleTextColor(resources.getColor(R.color.colorAccent))
        toolbar.setTitle(R.string.feedback)
        when{
            currentTheme.equals("night" ) || currentTheme.equals("mimi") ->{
                toolbar!!.setNavigationIcon(R.drawable.ic_back_white)
            }else -> {
            toolbar!!.setNavigationIcon(R.drawable.ic_back_black)
        } }


        if (user.currentUser != null){
            val fullName = user.currentUser!!.displayName
            full_name.setText(fullName)
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        submit.setOnClickListener {
            sendFeedback()
        }

    }


    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun sendFeedback(){
        feedback.setText("")
        full_name.setText("")
        feedback_submitted_text.visibility = View.VISIBLE
    }
}
