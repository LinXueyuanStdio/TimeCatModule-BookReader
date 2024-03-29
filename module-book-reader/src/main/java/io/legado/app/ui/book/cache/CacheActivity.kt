package io.legado.app.ui.book.cache

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import io.legado.app.App
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.constant.AppConst
import io.legado.app.constant.EventBus
import io.legado.app.constant.PreferKey
import io.legado.app.data.entities.Book
import io.legado.app.data.entities.BookChapter
import io.legado.app.data.entities.BookGroup
import io.legado.app.help.BookHelp
import io.legado.app.service.help.CacheBook
import io.legado.app.ui.filechooser.FileChooserDialog
import io.legado.app.ui.filechooser.FilePicker
import io.legado.app.ui.widget.dialog.TextListDialog
import io.legado.app.utils.*
import kotlinx.android.synthetic.main.novel_activity_download.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArraySet


class CacheActivity : VMBaseActivity<CacheViewModel>(R.layout.novel_activity_download),
    FileChooserDialog.CallBack,
    CacheAdapter.CallBack {
    private val exportRequestCode = 32
    private val exportBookPathKey = "exportBookPath"
    lateinit var adapter: CacheAdapter
    private var groupLiveData: LiveData<List<BookGroup>>? = null
    private var booksLiveData: LiveData<List<Book>>? = null
    private var menu: Menu? = null
    private var exportPosition = -1
    private val groupList: ArrayList<BookGroup> = arrayListOf()
    private var groupId: Int = -1

    override val viewModel: CacheViewModel
        get() = getViewModel(CacheViewModel::class.java)

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        groupId = intent.getIntExtra("groupId", -1)
        title_bar.subtitle = intent.getStringExtra("groupName") ?: getString(R.string.all)
        initRecyclerView()
        initGroupData()
        initBookData()
    }

    override fun onCompatCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.novel_download, menu)
        return super.onCompatCreateOptionsMenu(menu)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu
        upMenu()
        return super.onPrepareOptionsMenu(menu)
    }

    private fun upMenu() {
        menu?.findItem(R.id.menu_book_group)?.subMenu?.let { subMenu ->
            subMenu.removeGroup(R.id.menu_group)
            groupList.forEach { bookGroup ->
                subMenu.add(R.id.menu_group, bookGroup.groupId, Menu.NONE, bookGroup.groupName)
            }
        }
    }

    override fun onCompatOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_download -> launch(IO) {
                if (adapter.downloadMap.isNullOrEmpty()) {
                    adapter.getItems().forEach { book ->
                        CacheBook.start(
                            this@CacheActivity,
                            book.bookUrl,
                            book.durChapterIndex,
                            book.totalChapterNum
                        )
                    }
                } else {
                    CacheBook.stop(this@CacheActivity)
                }
            }
            R.id.menu_log -> {
                TextListDialog.show(supportFragmentManager, getString(R.string.log), CacheBook.logs)
            }
            R.id.menu_no_group -> {
                title_bar.subtitle = getString(R.string.no_group)
                groupId = AppConst.bookGroupNone.groupId
                initBookData()
            }
            R.id.menu_all -> {
                title_bar.subtitle = item.title
                groupId = AppConst.bookGroupAll.groupId
                initBookData()
            }
            else -> if (item.groupId == R.id.menu_group) {
                title_bar.subtitle = item.title
                groupId = item.itemId
                initBookData()
            }
        }
        return super.onCompatOptionsItemSelected(item)
    }

    private fun initRecyclerView() {
        recycler_view.layoutManager = LinearLayoutManager(this)
        adapter = CacheAdapter(this, this)
        recycler_view.adapter = adapter
    }

    private fun initBookData() {
        booksLiveData?.removeObservers(this)
        booksLiveData = when (groupId) {
            AppConst.bookGroupAll.groupId -> App.db.bookDao().observeAll()
            AppConst.bookGroupNone.groupId -> App.db.bookDao().observeNoGroup()
            else -> App.db.bookDao().observeByGroup(groupId)
        }
        booksLiveData?.observe(this, { list ->
            val booksDownload = list.filter {
                it.isOnLineTxt()
            }
            val books = when (getPrefInt(PreferKey.bookshelfSort)) {
                1 -> booksDownload.sortedByDescending { it.latestChapterTime }
                2 -> booksDownload.sortedBy { it.name }
                3 -> booksDownload.sortedBy { it.order }
                else -> booksDownload.sortedByDescending { it.durChapterTime }
            }
            adapter.setItems(books)
            initCacheSize(books)
        })
    }

    private fun initGroupData() {
        groupLiveData?.removeObservers(this)
        groupLiveData = App.db.bookGroupDao().liveDataAll()
        groupLiveData?.observe(this, {
            groupList.clear()
            groupList.addAll(it)
            adapter.notifyDataSetChanged()
            upMenu()
        })
    }

    private fun initCacheSize(books: List<Book>) {
        launch(IO) {
            books.forEach { book ->
                val chapterCaches = hashSetOf<String>()
                val cacheNames = BookHelp.getChapterFiles(book)
                App.db.bookChapterDao().getChapterList(book.bookUrl).forEach { chapter ->
                    if (cacheNames.contains(BookHelp.formatChapterName(chapter))) {
                        chapterCaches.add(chapter.url)
                    }
                }
                adapter.cacheChapters[book.bookUrl] = chapterCaches
                withContext(Dispatchers.Main) {
                    adapter.notifyItemRangeChanged(0, adapter.getActualItemCount(), true)
                }
            }
        }
    }

    override fun observeLiveBus() {
        observeEvent<ConcurrentHashMap<String, CopyOnWriteArraySet<BookChapter>>>(EventBus.UP_DOWNLOAD) {
            if (it.isEmpty()) {
                menu?.findItem(R.id.menu_download)?.setIcon(R.drawable.novel_ic_play_24dp)
                menu?.applyTint(this)
            } else {
                menu?.findItem(R.id.menu_download)?.setIcon(R.drawable.novel_ic_stop_black_24dp)
                menu?.applyTint(this)
            }
            adapter.downloadMap = it
            adapter.notifyItemRangeChanged(0, adapter.getActualItemCount(), true)
        }
        observeEvent<BookChapter>(EventBus.SAVE_CONTENT) {
            adapter.cacheChapters[it.bookUrl]?.add(it.url)
        }
    }

    override fun export(position: Int) {
        exportPosition = position
        val default = arrayListOf<String>()
        val path = ACache.get(this@CacheActivity).getAsString(exportBookPathKey)
        if (!path.isNullOrEmpty()) {
            default.add(path)
        }
        FilePicker.selectFolder(this, exportRequestCode, otherActions = default) {
            startExport(it)
        }
    }

    private fun startExport(path: String) {
        adapter.getItem(exportPosition)?.let { book ->
            Snackbar.make(title_bar, R.string.exporting, Snackbar.LENGTH_INDEFINITE)
                .show()
            viewModel.export(path, book) {
                title_bar.snackbar(it)
            }
        }
    }

    override fun onFilePicked(requestCode: Int, currentPath: String) {
        when (requestCode) {
            exportRequestCode -> {
                ACache.get(this@CacheActivity).put(exportBookPathKey, currentPath)
                startExport(currentPath)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            exportRequestCode -> if (resultCode == Activity.RESULT_OK) {
                data?.data?.let { uri ->
                    contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                    )
                    ACache.get(this@CacheActivity).put(exportBookPathKey, uri.toString())
                    startExport(uri.toString())
                }
            }

        }
    }
}