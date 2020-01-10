package com.pca.gomusic.Activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.answers.LoginEvent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.pca.gomusic.Constant.Constant
import com.pca.gomusic.R
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_sigin.*

class SignInActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor
    private var currentTheme: String? = null

    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        currentTheme = sharedPreferences.getString(Constant.THEME, "default")

        when {
            currentTheme.equals("night") -> setTheme(R.style.AppTheme_MainDark)
            else -> setTheme(R.style.AppTheme)
        }
        setContentView(R.layout.activity_sigin)

        when{
            currentTheme.equals("night") -> dismiss.setImageResource(R.drawable.ic_close_white)
            currentTheme.equals("mimi") ||  currentTheme.equals("cyran") || currentTheme.equals("default") -> dismiss.setImageResource(R.drawable.ic_close)
            else -> dismiss.setImageResource(R.drawable.ic_close_accent)
        }


        dismiss.setOnClickListener {
            finish()
            overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_from_right)}


        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)
        auth = FirebaseAuth.getInstance()

        btnGoogle.setOnClickListener {
            sign_in_progress.visibility = View.VISIBLE
            signIn()
        }


        btnFacebook.setOnClickListener {
            Toast.makeText(this, "Coming soon...", Toast.LENGTH_SHORT).show()
        }


    }




    private fun signIn() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }



    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
                Log.w(Fabric.TAG, "Google sign in failed", e)
                Crashlytics.getInstance().answers.logLogin(LoginEvent().putSuccess(false))
                sign_in_progress.visibility = View.GONE
            }
        }
    }



    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        Log.d(Fabric.TAG, "firebaseAuthWithGoogle:" + acct.id!!)
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        auth.signInWithCredential(credential)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        // Sign in success, update UI with the signed-in user's information
                        Log.d(Fabric.TAG, "signInWithCredential:success")
                        val user = auth.currentUser
                        editor = sharedPreferences.edit()
                        editor.putString(Constant.PROFILE_URL, user?.photoUrl.toString())
                        editor.putString(Constant.USER_NAME, user?.displayName)
                        editor.putString(Constant.USER_EMAIL, user?.email)
                        editor.apply()
                        sign_in_progress.visibility = View.GONE
                        Crashlytics.getInstance().answers.logLogin(LoginEvent().putSuccess(true))
                        Toast.makeText(this, "Welcome !", Toast.LENGTH_SHORT).show()
                        val intent = Intent()
                        intent.action = MainActivity.SIGNED_IN
                        sendBroadcast(intent)
                        finish()
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w(Fabric.TAG, "signInWithCredential:failure", task.exception)
                        Snackbar.make(card, "Authentication Failed.", Snackbar.LENGTH_SHORT).show()
                        Crashlytics.getInstance().answers.logLogin(LoginEvent().putSuccess(false))

                    }
                }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
        overridePendingTransition(R.anim.slide_in_from_left, R.anim.slide_out_from_right)
    }



    companion object{
        const val RC_SIGN_IN = 199
    }


    }

