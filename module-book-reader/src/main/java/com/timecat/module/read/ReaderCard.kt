package com.timecat.module.read

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import com.timecat.layout.ui.entity.BaseAdapter
import com.timecat.layout.ui.entity.BaseItem
import com.timecat.layout.ui.layout.setShakelessClickListener
import eu.davidea.flexibleadapter.FlexibleAdapter
import eu.davidea.flexibleadapter.items.IFlexible
import eu.davidea.viewholders.FlexibleViewHolder
import io.legado.app.R
import io.legado.app.constant.BookType
import io.legado.app.data.entities.Book
import io.legado.app.help.IntentDataHelp
import io.legado.app.ui.audio.AudioPlayActivity
import io.legado.app.ui.book.info.BookInfoActivity
import io.legado.app.ui.book.read.ReadBookActivity
import io.legado.app.ui.widget.anima.RotateLoading
import io.legado.app.ui.widget.image.CoverImageView
import io.legado.app.ui.widget.text.BadgeView
import org.jetbrains.anko.internals.AnkoInternals

/**
 * @author 林学渊
 * @email linxy59@mail2.sysu.edu.cn
 * @date 2021/1/20
 * @description null
 * @usage null
 */
class ReaderCard(
    val item: Book,
    val context: Context
) : BaseItem<ReaderCard.RepoCardVH>(item.bookUrl) {
    class RepoCardVH(view: View, adapter: FlexibleAdapter<*>) : FlexibleViewHolder(view, adapter) {
        val cv_content: View = view.findViewById(R.id.cv_content)
        val tv_name: TextView = view.findViewById(R.id.tv_name)
        val tv_last: TextView = view.findViewById(R.id.tv_last)
        val tv_read: TextView = view.findViewById(R.id.tv_read)
        val tv_author: TextView = view.findViewById(R.id.tv_author)
        val iv_author: ImageView = view.findViewById(R.id.iv_author)
        val iv_last: ImageView = view.findViewById(R.id.iv_last)
        val iv_read: ImageView = view.findViewById(R.id.iv_read)
        val iv_cover: CoverImageView = view.findViewById(R.id.iv_cover)
        val fl_has_new: FrameLayout = view.findViewById(R.id.fl_has_new)
        val bv_unread: BadgeView = view.findViewById(R.id.bv_unread)
        val rl_loading: RotateLoading = view.findViewById(R.id.rl_loading)
        val vw_foreground: View = view.findViewById(R.id.vw_foreground)
        val vw_select: View = view.findViewById(R.id.vw_select)
    }

    override fun getLayoutRes(): Int = R.layout.novel_item_bookshelf_list

    override fun createViewHolder(view: View, adapter: FlexibleAdapter<IFlexible<*>>): RepoCardVH {
        return RepoCardVH(view, adapter)
    }

    override fun bindViewHolder(adapter: FlexibleAdapter<IFlexible<*>>, holder: RepoCardVH, position: Int, payloads: MutableList<Any>?) {
        if (adapter is BaseAdapter) {
            adapter.bindViewHolderAnimation(holder)
        }
        holder.apply {
            tv_name.text = item.name
            tv_author.text = item.author
            tv_read.text = item.durChapterTitle
            tv_last.text = item.latestChapterTitle
            iv_cover.load(item.getDisplayCover(), item.name, item.author)
            upRefresh(this, item)
            cv_content.setShakelessClickListener {
                open(item)
            }
            cv_content.setOnLongClickListener {
                openBookInfo(item)
                true
            }
        }
    }

    fun open(book: Book) {
        when (book.type) {
            BookType.audio -> {
                val intent = AnkoInternals.createIntent(context, AudioPlayActivity::class.java, arrayOf(
                    "bookUrl" to book.bookUrl
                ))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
            else -> {
                val intent = AnkoInternals.createIntent(context, ReadBookActivity::class.java, arrayOf(
                    "bookUrl" to book.bookUrl,
                    "key" to IntentDataHelp.putData(book)
                ))
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            }
        }
    }

    fun openBookInfo(book: Book) {
        val intent = AnkoInternals.createIntent(context, BookInfoActivity::class.java, arrayOf(
            "name" to book.name,
            "author" to book.author
        ))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    private fun upRefresh(itemView: RepoCardVH, item: Book) = with(itemView) {
        rl_loading.hide()
        bv_unread.setHighlight(item.lastCheckCount > 0)
        bv_unread.setBadgeCount(item.getUnreadChapterNum())
    }
}