package io.legado.app.ui.main

import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter
import androidx.viewpager.widget.ViewPager
import com.xiaojinzi.component.anno.RouterAnno
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.timecat.identity.readonly.RouterHub
import io.legado.app.App
import io.legado.app.BuildConfig
import io.legado.app.R
import io.legado.app.base.VMBaseActivity
import io.legado.app.constant.EventBus
import io.legado.app.constant.PreferKey
import io.legado.app.help.AppConfig
import io.legado.app.help.BookHelp
import io.legado.app.help.storage.Backup
import io.legado.app.lib.theme.ATH
import io.legado.app.lib.theme.elevation
import io.legado.app.service.BaseReadAloudService
import io.legado.app.ui.main.bookshelf.BookshelfFragment
import io.legado.app.ui.main.explore.ExploreFragment
import io.legado.app.ui.main.my.MyFragment
import io.legado.app.ui.main.rss.RssFragment
import io.legado.app.ui.widget.dialog.TextDialog
import io.legado.app.utils.*
import kotlinx.android.synthetic.main.novel_activity_main.*
import org.jetbrains.anko.toast


@RouterAnno(hostAndPath = RouterHub.NOVEL_BookShelfActivity)
open class MainActivity : VMBaseActivity<MainViewModel>(R.layout.novel_activity_main),
    BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener,
    ViewPager.OnPageChangeListener by ViewPager.SimpleOnPageChangeListener() {
    override val viewModel: MainViewModel
        get() = getViewModel(MainViewModel::class.java)
    private var exitTime: Long = 0
    private var bookshelfReselected: Long = 0
    private var pagePosition = 0
    private val fragmentMap = hashMapOf<Int, Fragment>()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        ATH.applyEdgeEffectColor(view_pager_main)
        ATH.applyBottomNavigationColor(bottom_navigation_view)
        view_pager_main.offscreenPageLimit = 3
        view_pager_main.adapter = TabFragmentPageAdapter(supportFragmentManager)
        view_pager_main.addOnPageChangeListener(this)
        bottom_navigation_view.elevation =
            if (AppConfig.elevation < 0) elevation else AppConfig.elevation.toFloat()
        bottom_navigation_view.setOnNavigationItemSelectedListener(this)
        bottom_navigation_view.setOnNavigationItemReselectedListener(this)
        bottom_navigation_view.menu.findItem(R.id.menu_rss).isVisible = AppConfig.isShowRSS
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        upVersion()
        //自动更新书籍
        if (AppConfig.autoRefreshBook) {
            view_pager_main.postDelayed({
                viewModel.upAllBookToc()
            }, 1000)
        }
        view_pager_main.postDelayed({
            viewModel.postLoad()
        }, 3000)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_bookshelf -> view_pager_main.setCurrentItem(0, false)
            R.id.menu_find_book -> view_pager_main.setCurrentItem(1, false)
            R.id.menu_rss -> view_pager_main.setCurrentItem(2, false)
            R.id.menu_my_config -> view_pager_main.setCurrentItem(3, false)
        }
        return false
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        when (item.itemId) {
            R.id.menu_bookshelf -> {
                if (System.currentTimeMillis() - bookshelfReselected > 300) {
                    bookshelfReselected = System.currentTimeMillis()
                } else {
                    (fragmentMap[0] as? BookshelfFragment)?.gotoTop()
                }
            }
        }
    }

    private fun upVersion() {
        if (getPrefInt(PreferKey.versionCode) != App.versionCode) {
            putPrefInt(PreferKey.versionCode, App.versionCode)
            if (!BuildConfig.DEBUG) {
                val log = String(assets.open("updateLog.md").readBytes())
                TextDialog.show(supportFragmentManager, log, TextDialog.MD, 5000, true)
            }
        }
    }

    override fun onPageSelected(position: Int) {
        view_pager_main.hideSoftInput()
        pagePosition = position
        when (position) {
            0, 1, 3 -> bottom_navigation_view.menu.getItem(position).isChecked = true
            2 -> if (AppConfig.isShowRSS) {
                bottom_navigation_view.menu.getItem(position).isChecked = true
            } else {
                bottom_navigation_view.menu.getItem(3).isChecked = true
            }
        }
    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        event?.let {
            when (keyCode) {
                KeyEvent.KEYCODE_BACK -> if (event.isTracking && !event.isCanceled) {
                    if (pagePosition != 0) {
                        view_pager_main.currentItem = 0
                        return true
                    }
                    if (System.currentTimeMillis() - exitTime > 2000) {
                        toast(R.string.double_click_exit)
                        exitTime = System.currentTimeMillis()
                    } else {
                        if (BaseReadAloudService.pause) {
                            finish()
                        } else {
                            moveTaskToBack(true)
                        }
                    }
                    return true
                }
            }
        }
        return super.onKeyUp(keyCode, event)
    }

    override fun onPause() {
        super.onPause()
        if (!BuildConfig.DEBUG) {
            Backup.autoBack(this)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        BookHelp.clearRemovedCache()
    }

    override fun observeLiveBus() {
        observeEvent<String>(EventBus.RECREATE) {
            recreate()
        }
        observeEvent<String>(EventBus.SHOW_RSS) {
            bottom_navigation_view.menu.findItem(R.id.menu_rss).isVisible = AppConfig.isShowRSS
            view_pager_main.adapter?.notifyDataSetChanged()
            if (AppConfig.isShowRSS) {
                view_pager_main.setCurrentItem(3, false)
            }
        }
        observeEvent<String>(PreferKey.threadCount) {
            viewModel.upPool()
        }
    }

    private inner class TabFragmentPageAdapter(fm: FragmentManager) :
        FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

        val bookshelfFragment: Fragment
            get() {
                if (!fragmentMap.containsKey(0)) {
                    fragmentMap[0] = BookshelfFragment()
                }
                return fragmentMap.getValue(0)
            }

        val exploreFragment: Fragment
            get() {
                if (!fragmentMap.containsKey(1)) {
                    fragmentMap[1] = ExploreFragment()
                }
                return fragmentMap.getValue(1)
            }

        val rssFragment: Fragment
            get() {
                if (!fragmentMap.containsKey(2)) {
                    fragmentMap[2] = RssFragment()
                }
                return fragmentMap.getValue(2)
            }

        val myFragment: Fragment
            get() {
                if (!fragmentMap.containsKey(3)) {
                    fragmentMap[3] = MyFragment()
                }
                return fragmentMap.getValue(3)
            }

        override fun getItemPosition(`object`: Any): Int {
            return POSITION_NONE
        }

        override fun getItem(position: Int): Fragment {
            return when (position) {
                0 -> bookshelfFragment
                1 -> exploreFragment
                2 -> if (AppConfig.isShowRSS) rssFragment else myFragment
                else -> myFragment
            }
        }

        override fun getCount(): Int {
            return if (AppConfig.isShowRSS) 4 else 3
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as Fragment
            val id = when (position) {
                2 -> if (AppConfig.isShowRSS) 2 else 3
                else -> position
            }
            if (!fragmentMap.containsKey(id)) {
                fragmentMap[id] = fragment
            }
            return fragment
        }

    }

}