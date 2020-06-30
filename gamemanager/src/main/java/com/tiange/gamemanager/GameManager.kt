package com.tiange.gamemanager

import android.content.Context
import android.os.Build.VERSION_CODES
import android.util.Log
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.tiange.gamemanager.callback.ProgressCallback
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Response
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.net.URLDecoder
import java.util.concurrent.ConcurrentHashMap


class GameManager private  constructor(context:Context){
    private var context:Context = context
    private var gamePath:String
    private var progressCallback: ProgressCallback? =null

    companion object{
        private var gameManager:GameManager?=null

        fun getGameManager(context:Context):GameManager?{
            if (gameManager==null){
                gameManager= GameManager(context)
            }
            return gameManager
        }
    }
    init {
        this.gamePath=getGamePath()
    }


    fun getGamePath(): String {
        val file = FileUtil.getCacheFileByType(context, "game")
        return file.absolutePath
    }

    fun setProgressCallback(progressCallback: ProgressCallback){
        this.progressCallback=progressCallback
    }

    fun downloadFile(url:String,cacheDir:String,gameVersion:String){
        var file:File= File(this.gamePath+File.separator+cacheDir+File.separator+gameVersion)
        if(!file.exists()){
            ThreadPoolManager.getInstance().addTask("game",DownFileRunnable(url,this.gamePath+File.separator+cacheDir,
                "$gameVersion.zip"
                       , progressCallback))
        }
    }

    class DownFileRunnable internal constructor(
        private val url: String,
        private val destPath: String,
        private val fileName: String,
        private val progressCallback: ProgressCallback?=null
    ) :
        Runnable {
        override fun run() {
            OkHttpUtil.downloadFile(url,object: ProgressCallback {
                override fun onProgress(progress: Int, currentSize: Long, totalSize: Long) {
                    //Log.e("progress","progress=$progress,currentSize=$currentSize,totalSize=$totalSize")
                    progressCallback?.onProgress(progress,currentSize,totalSize)
                }

            },object:Callback{
                override fun onFailure(call: Call, e: IOException) {
                    //Log.e("onFailure","e=${e.toString()}")
                }

                override fun onResponse(call: Call, response: Response) {
                    //Log.e("onResponse","response=${response.message}")
                    val isSuccess: Boolean = IOUtil.write(response.body?.byteStream(),
                        destPath + File.separator+fileName
                    )
                    if(isSuccess&&response.message=="OK"){
                        FileUtil.unzipFile(
                            destPath + File.separator+fileName,
                            destPath + File.separator
                        )
                    }
                }
            })
        }
    }

    fun gameFilter(webGame:WebView,cacheDir: String,gameHost:String,gameUrl:String,gameVersion: String) {
        val mimeTypeList: MutableMap<String, String> = ConcurrentHashMap()
        mimeTypeList["js"] = "application/x-javascript"
        mimeTypeList["png"] = "image/png"
        mimeTypeList["jpg"] = "image/jpeg"
        mimeTypeList["jpeg"] = "image/jpeg"
        mimeTypeList["ico"] = "application/octet-stream"
        mimeTypeList["xml"] = "text/xml"
        mimeTypeList["css"] = "text/css"
        mimeTypeList["html"] = "text/html"
        mimeTypeList["vsh"] = "text/plain"
        mimeTypeList["fsh"] = "text/plain"
        mimeTypeList["txt"] = "text/plain"
        mimeTypeList["atlas"] = "application/json"
        mimeTypeList["json"] = "application/json"
        mimeTypeList["tmx"] = "text/plain"
        mimeTypeList["ExportJson"] = "application/json"
        mimeTypeList["plist"] = "text/xml"
        mimeTypeList["mp3"] = "audio/x-mpeg"
        mimeTypeList["wav"] = "audio/x-wav"
        mimeTypeList["mp4"] = "video/mp4"
        webGame.webViewClient = object : WebViewClient() {
            @RequiresApi(api = VERSION_CODES.LOLLIPOP)
            override fun shouldInterceptRequest(
                view: WebView,
                request: WebResourceRequest
            ): WebResourceResponse? {
                val url = request.url.toString()
                //拦截替换加载本地资源文件
                var response: WebResourceResponse? = null
                if (url.startsWith(gameHost)) {
                    var path = url.substring(gameHost.length, url.length)
                    try {
                        path = URLDecoder.decode(path, "UTF-8")
                        val index = path.lastIndexOf(".")
                        if (index > 0) {
                            val mimeType =
                                mimeTypeList[path.substring(index + 1, path.length)]
                            if (mimeType != null) {
                                path =gamePath+File.separator+cacheDir+path
                                Log.e("path=",path)
                                response = WebResourceResponse(
                                    mimeType,
                                    "UTF-8",
                                    FileInputStream(path)
                                )
                            }
                        }
                    } catch (e: Exception) {
                        return super.shouldInterceptRequest(view, request)
                    }
                }
                return response
                    ?: super.shouldInterceptRequest(
                        view,
                        request
                    )
            }
        }
        webGame.loadUrl(gameUrl)
    }


}