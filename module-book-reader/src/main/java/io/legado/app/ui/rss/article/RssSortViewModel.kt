package io.legado.app.ui.rss.article

import android.app.Application
import android.content.Intent
import androidx.lifecycle.MutableLiveData
import io.legado.app.App
import io.legado.app.R
import io.legado.app.base.BaseViewModel
import io.legado.app.data.entities.RssArticle
import io.legado.app.data.entities.RssReadRecord
import io.legado.app.data.entities.RssSource


class RssSortViewModel(application: Application) : BaseViewModel(application) {
    var url: String? = null
    var rssSource: RssSource? = null
    val titleLiveData = MutableLiveData<String>()
    var order = System.currentTimeMillis()
    val isGridLayout get() = rssSource?.articleStyle == 2
    val layoutId
        get() = when (rssSource?.articleStyle) {
            1 -> R.layout.novel_item_rss_article_1
            2 -> R.layout.novel_item_rss_article_2
            else -> R.layout.novel_item_rss_article
        }

    fun initData(intent: Intent, finally: () -> Unit) {
        execute {
            url = intent.getStringExtra("url")
            url?.let { url ->
                rssSource = App.db.rssSourceDao().getByKey(url)
                rssSource?.let {
                    titleLiveData.postValue(it.sourceName)
                } ?: let {
                    rssSource = RssSource(sourceUrl = url)
                }
            }
        }.onFinally {
            finally()
        }
    }

    fun switchLayout() {
        rssSource?.let {
            if (it.articleStyle < 2) {
                it.articleStyle = it.articleStyle + 1
            } else {
                it.articleStyle = 0
            }
            execute {
                App.db.rssSourceDao().update(it)
            }
        }
    }

    fun read(rssArticle: RssArticle) {
        execute {
            App.db.rssArticleDao().insertRecord(RssReadRecord(rssArticle.link))
        }
    }

    fun clearArticles() {
        execute {
            url?.let {
                App.db.rssArticleDao().delete(it)
            }
            order = System.currentTimeMillis()
        }.onSuccess {

        }
    }

}