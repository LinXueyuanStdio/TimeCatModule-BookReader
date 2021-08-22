package com.timecat.module.read

import android.content.Context
import com.google.android.material.chip.Chip
import com.timecat.component.router.app.NAV
import com.timecat.data.room.record.RoomRecord
import com.timecat.identity.readonly.RouterHub
import com.timecat.layout.ui.business.breadcrumb.Path
import com.timecat.layout.ui.layout.setShakelessClickListener
import com.timecat.middle.block.service.*
import com.xiaojinzi.component.anno.ServiceAnno
import io.legado.app.App.Companion.db
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * @author 林学渊
 * @email linxy59@mail2.sysu.edu.cn
 * @date 2021/1/20
 * @description null
 * @usage null
 */
@ServiceAnno(ContainerService::class, name = [RouterHub.GLOBAL_ReaderContainerService])
class ReaderContainerService : ContainerService {
    override fun loadContext(path: Path, context: Context, parentUuid: String, record: RoomRecord?, homeService: HomeService) {
        homeService.loadMenu(EmptyMenuContext())
        homeService.loadHeader(listOf())
        homeService.loadChipType(listOf())
        homeService.loadPanel(EmptyPanelContext())
        homeService.loadChipButtons(listOf(
            Chip(context).apply {
            text = "书架"
            setShakelessClickListener {
                NAV.go(RouterHub.NOVEL_BookShelfActivity)
                homeService.itemCommonListener().close()
            }
        },
            Chip(context).apply {
            text = "搜索"
            setShakelessClickListener {
                NAV.go(RouterHub.NOVEL_SearchBookActivity)
                homeService.itemCommonListener().close()
            }
        },
        ))
        homeService.loadCommand(EmptyCommandContext())
        homeService.loadInputSend(EmptyInputContext())
        homeService.reloadData()
    }

    override fun loadContextRecord(
        path: Path,
        context: Context,
        parentUuid: String,
        homeService: HomeService
    ) {
        homeService.loadContextRecord(null)
    }

    override fun loadForVirtualPath(context: Context, parentUuid: String, homeService: HomeService, callback: ContainerService.LoadCallback) {
        homeService.adapter().setEndlessProgressItem(null)
        GlobalScope.launch(Dispatchers.IO) {
            val allBooks = db.bookDao().all
            val cards = allBooks.map { ReaderCard(it, context) }
            withContext(Dispatchers.Main) {
                callback.onVirtualLoadSuccess(cards)
            }
        }
    }

    override fun loadMoreForVirtualPath(context: Context, parentUuid: String, offset: Int, homeService: HomeService, callback: ContainerService.LoadMoreCallback) {
        callback.onVirtualLoadSuccess(listOf())
    }
}