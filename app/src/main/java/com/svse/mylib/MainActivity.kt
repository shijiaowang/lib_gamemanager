package com.svse.mylib

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.webkit.WebSettings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.sembozdemir.permissionskt.askPermissions
import com.sembozdemir.permissionskt.handlePermissionsResult
import com.tiange.gamemanager.GameManager
import com.tiange.gamemanager.callback.ProgressCallback
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var gameManager: GameManager = GameManager.getGameManager(applicationContext)!!
        buttonPermissionsCamera.setOnClickListener {
            askPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE) {
                onGranted {
                    toast("Camera permission is granted.")
                        // gameManager.downloadFile("http://173.248.234.130:33312/ZooRacing_h5/5.zip","zoo","5")
                }

                onDenied {
                    toast("Camera permission is denied")
                }

                onShowRationale { request ->
                    snack("You should grant permission for Camera") { request.retry() }
                }

                onNeverAskAgain {
                    toast("Never ask again for camera permission")
                }
            }
        }

        buttonPermissionsPhoneAndSms.setOnClickListener {
            askPermissions(Manifest.permission.READ_EXTERNAL_STORAGE) {
                onGranted {
                    toast("Call Phone and Read Sms permission is granted.")
                }

                onDenied {
                    it.forEach {
                        when (it) {
                            Manifest.permission.READ_EXTERNAL_STORAGE -> toast("Call Phone is denied")
                            Manifest.permission.WRITE_EXTERNAL_STORAGE -> toast("Read Sms is denied")
                        }
                    }
                }

                onShowRationale { request ->

                    var permissions = ""
                    request.permissions.forEach {

                        permissions += when (it) {
                            Manifest.permission.READ_EXTERNAL_STORAGE -> " Call Phone"
                            Manifest.permission.WRITE_EXTERNAL_STORAGE -> " Read Sms"
                            else -> ""
                        }

                    }

                    snack("You should grant permission for $permissions") {
                        request.retry()
                    }
                }

                onNeverAskAgain {
                    it.forEach {
                        when (it) {
                            Manifest.permission.READ_EXTERNAL_STORAGE -> toast("Never ask again for Call Phone")
                            Manifest.permission.WRITE_EXTERNAL_STORAGE -> toast("Never ask again for Read Sms")
                        }
                    }
                }
            }
        }
        gameManager.setProgressCallback(object: ProgressCallback {
            override fun onProgress(progress: Int, currentSize: Long, totalSize: Long) {
                Log.e("progress","progress=$progress,currentSize=$currentSize,totalSize=$totalSize")
            }
        })
        webview.setBackgroundColor(0)
        val settings: WebSettings = webview.getSettings()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
        }
        settings.javaScriptEnabled = true
        settings.domStorageEnabled = true
        settings.cacheMode = WebSettings.LOAD_NO_CACHE
        settings.useWideViewPort = true
        settings.setSupportZoom(true)
        loadwebview.setOnClickListener {
            var url:String="http://173.248.234.130:33312/ZooRacing_h5/5/index.html?gcidx=600034&token=99b5db8054c1694baea7968bddf9c61a&plat_type=0&pid=111"
            GameManager.getGameManager(applicationContext)?.gameFilter(webview,"zoo","http://173.248.234.130:33312/ZooRacing_h5",url,"5")
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        handlePermissionsResult(requestCode, permissions, grantResults)
    }

    private fun toast(messsage: String) {
        Toast.makeText(this, messsage, Toast.LENGTH_LONG).show()
    }

    private fun snack(message: String, action: () -> Unit = {}) {
        Snackbar.make(rootView, message, Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry", { _ -> action() })
            .show()
    }
}
