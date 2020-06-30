package com.tiange.gamemanager.callback

/**
 * User: ljx
 * Date: 2017/12/1
 * Time: 20:22
 */
interface ProgressCallback {
    fun onProgress(
        progress: Int,
        currentSize: Long,
        totalSize: Long
    )
}