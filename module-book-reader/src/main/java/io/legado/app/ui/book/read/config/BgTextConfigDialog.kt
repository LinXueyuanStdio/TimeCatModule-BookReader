package io.legado.app.ui.book.read.config

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.*
import androidx.documentfile.provider.DocumentFile
import com.bumptech.glide.request.RequestOptions
import com.jaredrummler.android.colorpicker.ColorPickerDialog
import io.legado.app.R
import io.legado.app.base.BaseDialogFragment
import io.legado.app.base.adapter.ItemViewHolder
import io.legado.app.base.adapter.SimpleRecyclerAdapter
import io.legado.app.constant.EventBus
import io.legado.app.help.ImageLoader
import io.legado.app.help.ReadBookConfig
import io.legado.app.help.http.HttpHelper
import io.legado.app.help.permission.Permissions
import io.legado.app.help.permission.PermissionsCompat
import io.legado.app.lib.dialogs.alert
import io.legado.app.lib.dialogs.customView
import io.legado.app.lib.dialogs.noButton
import io.legado.app.lib.dialogs.okButton
import io.legado.app.lib.theme.bottomBackground
import io.legado.app.lib.theme.getPrimaryTextColor
import io.legado.app.lib.theme.getSecondaryTextColor
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.filechooser.FileChooserDialog
import io.legado.app.ui.filechooser.FilePicker
import io.legado.app.ui.widget.text.AutoCompleteTextView
import io.legado.app.utils.*
import kotlinx.android.synthetic.main.novel_dialog_edit_text.view.*
import kotlinx.android.synthetic.main.novel_dialog_read_bg_text.*
import kotlinx.android.synthetic.main.novel_item_bg_image.view.*
import org.jetbrains.anko.sdk27.listeners.onCheckedChange
import org.jetbrains.anko.sdk27.listeners.onClick
import java.io.File

class BgTextConfigDialog : BaseDialogFragment(), FileChooserDialog.CallBack {

    companion object {
        const val TEXT_COLOR = 121
        const val BG_COLOR = 122
    }

    private val requestCodeBg = 123
    private val requestCodeExport = 131
    private val requestCodeImport = 132
    private val configFileName = "readConfig.zip"
    private lateinit var adapter: BgAdapter
    var primaryTextColor = 0
    var secondaryTextColor = 0

    override fun onStart() {
        super.onStart()
        val dm = DisplayMetrics()
        activity?.let {
            it.windowManager?.defaultDisplay?.getMetrics(dm)
        }
        dialog?.window?.let {
            it.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
            it.setBackgroundDrawableResource(R.color.background)
            it.decorView.setPadding(0, 0, 0, 0)
            val attr = it.attributes
            attr.dimAmount = 0.0f
            attr.gravity = Gravity.BOTTOM
            it.attributes = attr
            it.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.novel_dialog_read_bg_text, container)
    }

    override fun onFragmentCreated(view: View, savedInstanceState: Bundle?) {
        initView()
        initData()
        initEvent()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        ReadBookConfig.save()
    }

    private fun initView() {
        val bg = requireContext().bottomBackground
        val isLight = ColorUtils.isColorLight(bg)
        primaryTextColor = requireContext().getPrimaryTextColor(isLight)
        secondaryTextColor = requireContext().getSecondaryTextColor(isLight)
        root_view.setBackgroundColor(bg)
        sw_dark_status_icon.setTextColor(primaryTextColor)
        iv_import.setColorFilter(primaryTextColor)
        iv_export.setColorFilter(primaryTextColor)
        iv_delete.setColorFilter(primaryTextColor)
        tv_bg_image.setTextColor(primaryTextColor)
    }

    @SuppressLint("InflateParams")
    private fun initData() = with(ReadBookConfig.durConfig) {
        sw_dark_status_icon.isChecked = statusIconDark()
        adapter = BgAdapter(requireContext())
        recycler_view.adapter = adapter
        val headerView = LayoutInflater.from(requireContext())
            .inflate(R.layout.novel_item_bg_image, recycler_view, false)
        adapter.addHeaderView(headerView)
        headerView.tv_name.setTextColor(secondaryTextColor)
        headerView.tv_name.text = getString(R.string.select_image)
        headerView.iv_bg.setImageResource(R.drawable.novel_ic_image)
        headerView.iv_bg.setColorFilter(primaryTextColor)
        headerView.onClick { selectImage() }
        requireContext().assets.list("bg/")?.let {
            adapter.setItems(it.toList())
        }
    }

    private fun initEvent() = with(ReadBookConfig.durConfig) {
        sw_dark_status_icon.onCheckedChange { buttonView, isChecked ->
            if (buttonView?.isPressed == true) {
                setStatusIconDark(isChecked)
                (activity as? ReadBookActivity)?.upSystemUiVisibility()
            }
        }
        tv_text_color.onClick {
            ColorPickerDialog.newBuilder()
                .setColor(textColor())
                .setShowAlphaSlider(false)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setDialogId(TEXT_COLOR)
                .show(requireActivity())
        }
        tv_bg_color.onClick {
            val bgColor =
                if (bgType() == 0) Color.parseColor(bgStr())
                else Color.parseColor("#015A86")
            ColorPickerDialog.newBuilder()
                .setColor(bgColor)
                .setShowAlphaSlider(false)
                .setDialogType(ColorPickerDialog.TYPE_CUSTOM)
                .setDialogId(BG_COLOR)
                .show(requireActivity())
        }
        iv_import.onClick {
            val importFormNet = "网络导入"
            val otherActions = arrayListOf(importFormNet)
            FilePicker.selectFile(
                this@BgTextConfigDialog,
                requestCodeImport,
                title = getString(R.string.import_str),
                allowExtensions = arrayOf("zip"),
                otherActions = otherActions
            ) { action ->
                when (action) {
                    importFormNet -> importNetConfigAlert()
                }
            }
        }
        iv_export.onClick {
            FilePicker.selectFolder(
                this@BgTextConfigDialog,
                requestCodeExport,
                title = getString(R.string.export_str)
            )
        }
        iv_delete.onClick {
            if (ReadBookConfig.deleteDur()) {
                postEvent(EventBus.UP_CONFIG, true)
                dismiss()
            } else {
                toast("数量以是最少,不能删除.")
            }
        }
    }

    private fun selectImage() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "image/*"
        startActivityForResult(intent, requestCodeBg)
    }

    inner class BgAdapter(context: Context) :
        SimpleRecyclerAdapter<String>(context, R.layout.novel_item_bg_image) {

        override fun convert(holder: ItemViewHolder, item: String, payloads: MutableList<Any>) {
            with(holder.itemView) {
                ImageLoader.load(context, context.assets.open("bg/$item").readBytes())
                    .apply(RequestOptions().centerCrop())
                    .into(iv_bg)
                tv_name.setTextColor(secondaryTextColor)
                tv_name.text = item.substringBeforeLast(".")
            }
        }

        override fun registerListener(holder: ItemViewHolder) {
            holder.itemView.apply {
                this.onClick {
                    getItemByLayoutPosition(holder.layoutPosition)?.let {
                        ReadBookConfig.durConfig.setBg(1, it)
                        ReadBookConfig.upBg()
                        postEvent(EventBus.UP_CONFIG, false)
                    }
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun exportConfig(uri: Uri) {
        execute {
            val exportFiles = arrayListOf<File>()
            val configDirPath = FileUtils.getPath(requireContext().eCacheDir, "readConfig")
            FileUtils.deleteFile(configDirPath)
            val configDir = FileUtils.createFolderIfNotExist(configDirPath)
            val configExportPath = FileUtils.getPath(configDir, "readConfig.json")
            FileUtils.deleteFile(configExportPath)
            val configExportFile = FileUtils.createFileIfNotExist(configExportPath)
            configExportFile.writeText(GSON.toJson(ReadBookConfig.getExportConfig()))
            exportFiles.add(configExportFile)
            val fontPath = ReadBookConfig.textFont
            if (fontPath.isNotEmpty()) {
                val fontName = FileUtils.getName(fontPath)
                val fontBytes = fontPath.parseToUri().readBytes(requireContext())
                fontBytes?.let {
                    val fontExportFile = FileUtils.createFileIfNotExist(configDir, fontName)
                    fontExportFile.writeBytes(it)
                    exportFiles.add(fontExportFile)
                }
            }
            if (ReadBookConfig.durConfig.bgType() == 2) {
                val bgName = FileUtils.getName(ReadBookConfig.durConfig.bgStr())
                val bgFile = File(ReadBookConfig.durConfig.bgStr())
                if (bgFile.exists()) {
                    val bgExportFile = File(FileUtils.getPath(configDir, bgName))
                    bgFile.copyTo(bgExportFile)
                    exportFiles.add(bgExportFile)
                }
            }
            val configZipPath = FileUtils.getPath(requireContext().eCacheDir, configFileName)
            if (ZipUtils.zipFiles(exportFiles, File(configZipPath))) {
                if (uri.isContentPath()) {
                    DocumentFile.fromTreeUri(requireContext(), uri)?.let { treeDoc ->
                        treeDoc.findFile(configFileName)?.delete()
                        treeDoc.createFile("", configFileName)
                            ?.writeBytes(requireContext(), File(configZipPath).readBytes())
                    }
                } else {
                    val exportPath = FileUtils.getPath(File(uri.path!!), configFileName)
                    FileUtils.deleteFile(exportPath)
                    FileUtils.createFileIfNotExist(exportPath)
                        .writeBytes(File(configZipPath).readBytes())
                }
            }
        }.onSuccess {
            toast("导出成功, 文件名为 $configFileName")
        }.onError {
            it.printStackTrace()
            longToast("导出失败:${it.localizedMessage}")
        }
    }

    @SuppressLint("InflateParams")
    private fun importNetConfigAlert() {
        alert("输入地址") {
            var editText: AutoCompleteTextView? = null
            customView {
                layoutInflater.inflate(R.layout.novel_dialog_edit_text, null).apply {
                    editText = edit_view
                }
            }
            okButton {
                editText?.text?.toString()?.let { url ->
                    importNetConfig(url)
                }
            }
            noButton { }
        }.show().applyTint()
    }

    private fun importNetConfig(url: String) {
        execute {
            HttpHelper.simpleGetBytesAsync(url)?.let {
                importConfig(it)
            } ?: throw Exception("获取失败")
        }.onError {
            longToast(it.msg)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun importConfig(uri: Uri) {
        execute {
            importConfig(uri.readBytes(requireContext())!!)
        }.onError {
            it.printStackTrace()
            longToast("导入失败:${it.localizedMessage}")
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun importConfig(byteArray: ByteArray) {
        execute {
            val configZipPath = FileUtils.getPath(requireContext().eCacheDir, configFileName)
            FileUtils.deleteFile(configZipPath)
            val zipFile = FileUtils.createFileIfNotExist(configZipPath)
            zipFile.writeBytes(byteArray)
            val configDirPath = FileUtils.getPath(requireContext().eCacheDir, "readConfig")
            FileUtils.deleteFile(configDirPath)
            ZipUtils.unzipFile(zipFile, FileUtils.createFolderIfNotExist(configDirPath))
            val configDir = FileUtils.createFolderIfNotExist(configDirPath)
            val configFile = FileUtils.getFile(configDir, "readConfig.json")
            val config: ReadBookConfig.Config = GSON.fromJsonObject(configFile.readText())!!
            if (config.textFont.isNotEmpty()) {
                val fontName = FileUtils.getName(config.textFont)
                val fontPath =
                    FileUtils.getPath(requireContext().externalFilesDir, "font", fontName)
                if (!FileUtils.exist(fontPath)) {
                    FileUtils.getFile(configDir, fontName).copyTo(File(fontPath))
                }
                config.textFont = fontPath
            }
            if (config.bgType() == 2) {
                val bgName = FileUtils.getName(config.bgStr())
                val bgPath = FileUtils.getPath(requireContext().externalFilesDir, "bg", bgName)
                if (!FileUtils.exist(bgPath)) {
                    FileUtils.getFile(configDir, bgName).copyTo(File(bgPath))
                }
            }
            ReadBookConfig.durConfig = config
            postEvent(EventBus.UP_CONFIG, true)
        }.onSuccess {
            toast("导入成功")
        }.onError {
            it.printStackTrace()
            longToast("导入失败:${it.localizedMessage}")
        }
    }

    override fun onFilePicked(requestCode: Int, currentPath: String) {
        when (requestCode) {
            requestCodeImport -> importConfig(Uri.fromFile(File(currentPath)))
            requestCodeExport -> exportConfig(Uri.fromFile(File(currentPath)))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            requestCodeBg -> if (resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    setBgFromUri(uri)
                }
            }
            requestCodeImport -> if (resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    importConfig(uri)
                }
            }
            requestCodeExport -> if (resultCode == RESULT_OK) {
                data?.data?.let { uri ->
                    exportConfig(uri)
                }
            }
        }
    }

    private fun setBgFromUri(uri: Uri) {
        if (uri.toString().isContentPath()) {
            val doc = DocumentFile.fromSingleUri(requireContext(), uri)
            doc?.name?.let {
                val file =
                    FileUtils.createFileIfNotExist(requireContext().externalFilesDir, "bg", it)
                kotlin.runCatching {
                    DocumentUtils.readBytes(requireContext(), doc.uri)
                }.getOrNull()?.let { byteArray ->
                    file.writeBytes(byteArray)
                    ReadBookConfig.durConfig.setBg(2, file.absolutePath)
                    ReadBookConfig.upBg()
                    postEvent(EventBus.UP_CONFIG, false)
                } ?: toast("获取文件出错")
            }
        } else {
            PermissionsCompat.Builder(this)
                .addPermissions(
                    Permissions.READ_EXTERNAL_STORAGE,
                    Permissions.WRITE_EXTERNAL_STORAGE
                )
                .rationale(R.string.bg_image_per)
                .onGranted {
                    RealPathUtil.getPath(requireContext(), uri)?.let { path ->
                        ReadBookConfig.durConfig.setBg(2, path)
                        ReadBookConfig.upBg()
                        postEvent(EventBus.UP_CONFIG, false)
                    }
                }
                .request()
        }
    }
}