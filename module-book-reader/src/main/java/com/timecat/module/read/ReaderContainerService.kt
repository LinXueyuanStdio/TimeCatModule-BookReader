package com.timecat.module.read

import android.content.Context
import com.timecat.identity.readonly.RouterHub
import com.timecat.middle.block.service.ContainerService
import com.timecat.middle.block.service.HomeService
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
    override fun loadForVirtualPath(context: Context, parentUuid: String, homeService: HomeService, callback: ContainerService.LoadCallback) {
        GlobalScope.launch(Dispatchers.IO) {
            val allBooks = db.bookDao().all
            val cards = allBooks.map { ReaderCard(it, context) }
            withContext(Dispatchers.Main) {
                callback.onVirtualLoadSuccess(cards)
            }
        }
    }
}