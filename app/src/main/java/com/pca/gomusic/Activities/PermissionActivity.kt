package com.pca.gomusic.Activities


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.crashlytics.android.Crashlytics
import com.crashlytics.android.ndk.CrashlyticsNdk
import com.google.android.material.snackbar.Snackbar
import com.pca.gomusic.R
import io.fabric.sdk.android.Fabric
import kotlinx.android.synthetic.main.activity_permission.*


class PermissionActivity :AppCompatActivity() {

  companion object{
    const val REQUEST_CODE = 191
  }
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    Fabric.with(this, Crashlytics(), CrashlyticsNdk())
    setContentView(R.layout.activity_permission)

    request.setOnClickListener {
      checkAndRequestPermissions()
    }
  }



  private fun checkAndRequestPermissions() {
    val listPermissionsNeeded = ArrayList<String>()
        listPermissionsNeeded.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        listPermissionsNeeded.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        listPermissionsNeeded.add(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
      listPermissionsNeeded.add(android.Manifest.permission.FOREGROUND_SERVICE)
    }
    listPermissionsNeeded.add(android.Manifest.permission.WRITE_SETTINGS)
        if (listPermissionsNeeded.isNotEmpty()) {
          ActivityCompat.requestPermissions(this,
                  listPermissionsNeeded.toArray(arrayOfNulls<String>(0)),
                  REQUEST_CODE)
        }
  }




  private fun permissionDenied() {
    // no permissions granted.
    val snackbar = Snackbar.make(some_text, R.string.denied, Snackbar.LENGTH_INDEFINITE)
            .setAction(R.string.retry) { checkAndRequestPermissions() }
    val sbView = snackbar.view
    sbView.setBackgroundColor(resources.getColor(R.color.red))
    snackbar.setActionTextColor(resources.getColor(R.color.white))
    snackbar.show()
  }


  override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String?>, grantResults: IntArray) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    if (requestCode == REQUEST_CODE) {
      lock.visibility = View.GONE
      request.visibility = View.GONE
      some_text.text = "Thanks for granting us permission. Enjoy the moment."
      Handler().postDelayed({ startActivity(Intent(this, MainActivity::class.java)) }, 1000)
    } else {
      permissionDenied()
    }
  }

}