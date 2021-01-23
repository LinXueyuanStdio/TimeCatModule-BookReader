
## ButterKnife
java, module-book-reader

import com.kunfei.bookshelf.R;   ->   import com.kunfei.bookshelf.R;import com.kunfei.bookshelf.R2;

(@[a-zA-Z]+\()R(\.[.a-zA-Z0-9_]+\))   ->   $1R2$2

## Rename Layout Files
regex, module-book-reader

(R\.layout\.)    ->    $1novel_
(@layout/)
(@menu/)
(R\.menu\.)

## Manifest
MatchCase
android:name=".     ->       android:name="com.kunfei.bookshelf.

## Application

MApplication.getInstance(),     ->       BaseApplication.getInstance(),
MApplication.getInstance())     ->       BaseApplication.getInstance())

## case
手动，变为 if
case R.id

## Route

MainActivity
BookDetailActivity
ReadBookActivity
ReadStyleActivity
SearchBookActivity

## 重定向

版本更新

说明页面

### 捐赠页面 DonateActivity

MainActivity setUpNavigationView

} else if (itemId == R.id.action_about) {
//     handler.postDelayed(() -> AboutActivity.startThis(this), 200);
    AlertDialog alertDialog = DialogPool.showAbout(this);
    ATH.setAlertDialogTint(alertDialog);
} else if (itemId == R.id.action_donate) {
    ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
    ClipData clipData = ClipData.newPlainText(null, "524223828");
    if (clipboard != null) {
        clipboard.setPrimaryClip(clipData);
        Toast.makeText(MainActivity.this,
                       "高级功能已开启\n红包码已复制\n支付宝首页搜索“524223828” 立即领红包",
                       Toast.LENGTH_LONG).show();
    }
    try {
        PackageManager packageManager = getApplicationContext().getPackageManager();
        Intent intent = packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone");
        assert intent != null;
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        MApplication.getInstance().upDonateHb();
    }
    handler.postDelayed(() -> NAV.go(this, RouterHub.ABOUT_DonateActivity), 200);



## UI

### MainActivity
#### menu_main_activity.xml

android:icon="@drawable/ic_add"

#### setUpNavigationView

ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
ClipData clipData = ClipData.newPlainText(null, "524223828");
if (clipboard != null) {
    clipboard.setPrimaryClip(clipData);
    Toast.makeText(MainActivity.this,
            "高级功能已开启\n红包码已复制\n支付宝首页搜索“524223828” 立即领红包",
            Toast.LENGTH_LONG).show();
}
try {
    PackageManager packageManager = getApplicationContext().getPackageManager();
    Intent intent = packageManager.getLaunchIntentForPackage("com.eg.android.AlipayGphone");
    assert intent != null;
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(intent);
} catch (Exception e) {
    e.printStackTrace();
} finally {
    DEF.config().save("DonateHb", System.currentTimeMillis());
}
handler.postDelayed(() -> NAV.go(this, RouterHub.ABOUT_DonateActivity), 200);


## 文案修饰

updateLog.md

## 功能添加

### burstlink
novel_menu_book_read_activity.xml

<item
  android:id="@+id/action_burst_link"
  android:icon="@drawable/ic_blur_on_black_24dp"
  android:title="Burst Link !"
  app:showAsAction="ifRoom"/>

ic_blur_on_black_24dp.xml 颜色
<vector xmlns:android="http://schemas.android.com/apk/res/android"
        android:width="24dp"
        android:height="24dp"
        android:viewportWidth="24.0"
        android:viewportHeight="24.0">
    <path
        android:fillColor="#595757"
        android:pathData="M6,13c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM6,17c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM6,9c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM3,9.5c-0.28,0 -0.5,0.22 -0.5,0.5s0.22,0.5 0.5,0.5 0.5,-0.22 0.5,-0.5 -0.22,-0.5 -0.5,-0.5zM6,5c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM21,10.5c0.28,0 0.5,-0.22 0.5,-0.5s-0.22,-0.5 -0.5,-0.5 -0.5,0.22 -0.5,0.5 0.22,0.5 0.5,0.5zM14,7c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1 -1,0.45 -1,1 0.45,1 1,1zM14,3.5c0.28,0 0.5,-0.22 0.5,-0.5s-0.22,-0.5 -0.5,-0.5 -0.5,0.22 -0.5,0.5 0.22,0.5 0.5,0.5zM3,13.5c-0.28,0 -0.5,0.22 -0.5,0.5s0.22,0.5 0.5,0.5 0.5,-0.22 0.5,-0.5 -0.22,-0.5 -0.5,-0.5zM10,20.5c-0.28,0 -0.5,0.22 -0.5,0.5s0.22,0.5 0.5,0.5 0.5,-0.22 0.5,-0.5 -0.22,-0.5 -0.5,-0.5zM10,3.5c0.28,0 0.5,-0.22 0.5,-0.5s-0.22,-0.5 -0.5,-0.5 -0.5,0.22 -0.5,0.5 0.22,0.5 0.5,0.5zM10,7c0.55,0 1,-0.45 1,-1s-0.45,-1 -1,-1 -1,0.45 -1,1 0.45,1 1,1zM10,12.5c-0.83,0 -1.5,0.67 -1.5,1.5s0.67,1.5 1.5,1.5 1.5,-0.67 1.5,-1.5 -0.67,-1.5 -1.5,-1.5zM18,13c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM18,17c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM18,9c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM18,5c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM21,13.5c-0.28,0 -0.5,0.22 -0.5,0.5s0.22,0.5 0.5,0.5 0.5,-0.22 0.5,-0.5 -0.22,-0.5 -0.5,-0.5zM14,17c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM14,20.5c-0.28,0 -0.5,0.22 -0.5,0.5s0.22,0.5 0.5,0.5 0.5,-0.22 0.5,-0.5 -0.22,-0.5 -0.5,-0.5zM10,8.5c-0.83,0 -1.5,0.67 -1.5,1.5s0.67,1.5 1.5,1.5 1.5,-0.67 1.5,-1.5 -0.67,-1.5 -1.5,-1.5zM10,17c-0.55,0 -1,0.45 -1,1s0.45,1 1,1 1,-0.45 1,-1 -0.45,-1 -1,-1zM14,12.5c-0.83,0 -1.5,0.67 -1.5,1.5s0.67,1.5 1.5,1.5 1.5,-0.67 1.5,-1.5 -0.67,-1.5 -1.5,-1.5zM14,8.5c-0.83,0 -1.5,0.67 -1.5,1.5s0.67,1.5 1.5,1.5 1.5,-0.67 1.5,-1.5 -0.67,-1.5 -1.5,-1.5z"/>
</vector>


ReadBookActivity onOptionsItemSelected

} else if (id == R.id.action_burst_link) {
  ARouter.getInstance().build(RouterHub.BURSTLINK_BurstLinkActivity)
      .withString("title", mPresenter.getBookShelf().getBookInfoBean().getName())
      .withString("curFunctionKey",
          RouterHub.OPEN_TIMECAT_AT + RouterHub.NOVEL_ReadBookActivity +
              RouterHub.AND + mPresenter.getBookShelf().getBookInfoBean().getNoteUrl())
      .navigation(this);


### link

#### ReadBookPresenter

loadBook
if (bookShelf == null && !isEmpty(mView.getBookUrl())) {
    bookShelf = BookshelfHelp.getBook(mView.getBookUrl());
}

#### ReadBookActivity

@Autowired
String bookUrl;

NAV.inject(this);

@Override
public String getBookUrl() {
  return bookUrl;
}

### 加载默认源
BookSourceManager

  public static void initDefaultBookSource(Context context) {
    if (getAllBookSource() == null || getAllBookSource().size() == 0) {
      new AlertDialog.Builder(context)
          .setTitle("加载默认书源")
          .setMessage("当前书源为空,是否加载默认书源?")
          .setPositiveButton(R.string.ok, (dialog, which) -> {
            try {
              String text = "https://raw.githubusercontent.com/LinXueyuanStdio/WelcomeToTimeCat/master/default_source_url.txt";
              Observable<List<BookSourceBean>> observable = BookSourceManager.importSource(text);
              if (observable != null) {
                observable.subscribe(new Observer<List<BookSourceBean>>() {
                  @Override
                  public void onSubscribe(Disposable d) {

                  }

                  @Override
                  public void onNext(List<BookSourceBean> bookSourceBeans) {
                    Toast.makeText(context, "成功导入 " + bookSourceBeans.size() + " 个默认书源", Toast.LENGTH_SHORT).show();
                  }

                  @Override
                  public void onError(Throwable e) {
                    Toast.makeText(context, "默认书源加载失败.", Toast.LENGTH_SHORT).show();
                  }

                  @Override
                  public void onComplete() {

                  }
                });
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          })
          .setNegativeButton(R.string.cancel, (dialogInterface, i) -> {
          })
          .show();
    }
  }

## shortcuts

修改targetPackage -> com.time.cat
