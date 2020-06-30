package com.tiange.gamemanager

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import androidx.annotation.Nullable
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.tiange.gamemanager.PermissionsUtil.TipInfo


class PermissionActivity : AppCompatActivity() {
    private var isRequireCheck = false
    private lateinit var permission: Array<String>
    private var key: String? = null
    private var showTip = false
    private var tipInfo: TipInfo? = null
    private val defaultTitle = "帮助"
    private val defaultContent = "当前应用缺少必要权限。\n \n 请点击 \"设置\"-\"权限\"-打开所需权限。"
    private val defaultCancel = "取消"
    private val defaultEnsure = "设置"
    override fun onCreate(@Nullable savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (intent == null || !intent.hasExtra("permission")) {
            finish()
            return
        }
        isRequireCheck = true
        permission = intent.getStringArrayExtra("permission")
        key = intent.getStringExtra("key")
        showTip = intent.getBooleanExtra("showTip", true)
        val ser = intent.getSerializableExtra("tip")
        tipInfo = if (ser == null) {
            TipInfo(defaultTitle, defaultContent, defaultCancel, defaultEnsure)
        } else {
            ser as TipInfo
        }
    }

    override fun onResume() {
        super.onResume()
        if (isRequireCheck) {
            if (PermissionsUtil.hasPermission(this, *permission)) {
                permissionsGranted()
            } else {
                requestPermissions(permission) // 请求权限,回调时会触发onResume
                isRequireCheck = false
            }
        } else {
            isRequireCheck = true
        }
    }

    // 请求权限兼容低版本
    private fun requestPermissions(permission: Array<String>) {
        ActivityCompat.requestPermissions(
            this,
            permission,
            PERMISSION_REQUEST_CODE
        )
    }

    /**
     * 用户权限处理,
     * 如果全部获取, 则直接过.
     * 如果权限缺失, 则提示Dialog.
     *
     * @param requestCode  请求码
     * @param permissions  权限
     * @param grantResults 结果
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        //部分厂商手机系统返回授权成功时，厂商可以拒绝权限，所以要用PermissionChecker二次判断
        if (requestCode == PERMISSION_REQUEST_CODE && PermissionsUtil.isGranted(
                *grantResults
            )
            && PermissionsUtil.hasPermission(this, *permissions)
        ) {
            permissionsGranted()
        } else if (showTip) {
            showMissingPermissionDialog()
        } else { //不需要提示用户
            permissionsDenied()
        }
    }

    // 显示缺失权限提示
    private fun showMissingPermissionDialog() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this@PermissionActivity)
        builder.setTitle(if (TextUtils.isEmpty(tipInfo!!.title)) defaultTitle else tipInfo!!.title)
        builder.setMessage(if (TextUtils.isEmpty(tipInfo!!.content)) defaultContent else tipInfo!!.content)
        builder.setNegativeButton(
            if (TextUtils.isEmpty(tipInfo!!.cancel)) defaultCancel else tipInfo!!.cancel,
            DialogInterface.OnClickListener { dialog, which -> permissionsDenied() })
        builder.setPositiveButton(
            if (TextUtils.isEmpty(tipInfo!!.ensure)) defaultEnsure else tipInfo!!.ensure,
            DialogInterface.OnClickListener { dialog, which -> PermissionsUtil.gotoSetting(this@PermissionActivity) })
        builder.setCancelable(false)
        builder.show()
    }

    private fun permissionsDenied() {
        val listener = PermissionsUtil.fetchListener(key)
        listener?.permissionDenied(permission)
        finish()
    }

    // 全部权限均已获取
    private fun permissionsGranted() {
        val listener = PermissionsUtil.fetchListener(key)
        listener?.permissionGranted(permission)
        finish()
    }

    override fun onDestroy() {
        PermissionsUtil.fetchListener(key)
        super.onDestroy()
    }

    companion object {
        private const val PERMISSION_REQUEST_CODE = 64
    }
}