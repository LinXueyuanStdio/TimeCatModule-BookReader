package io.legado.app.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import io.legado.app.App
import io.legado.app.data.dao.*
import io.legado.app.data.entities.*


@Database(
    entities = [Book::class, BookGroup::class, BookSource::class, BookChapter::class,
        ReplaceRule::class, SearchBook::class, SearchKeyword::class, Cookie::class,
        RssSource::class, Bookmark::class, RssArticle::class, RssReadRecord::class,
        RssStar::class, TxtTocRule::class, ReadRecord::class, HttpTTS::class],
    version = 20,
    exportSchema = true
)
abstract class AppDatabase: RoomDatabase() {
    
    companion object {
        
        private const val DATABASE_NAME = "bookshelf.db"
        
        fun createDatabase(context: Context) =
            Room.databaseBuilder(context, AppDatabase::class.java, DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .addMigrations(
                    migration_10_11,
                    migration_11_12,
                    migration_12_13,
                    migration_13_14,
                    migration_14_15,
                    migration_15_17,
                    migration_17_18,
                    migration_18_19,
                    migration_19_20
                )
                .allowMainThreadQueries()
                .build()
        
        private val migration_10_11 = object: Migration(10, 11) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE txtTocRules")
                database.execSQL(
                    """
                    CREATE TABLE txtTocRules(id INTEGER NOT NULL, 
                    name TEXT NOT NULL, rule TEXT NOT NULL, serialNumber INTEGER NOT NULL, 
                    enable INTEGER NOT NULL, PRIMARY KEY (id))
                    """
                )
            }
        }
        
        private val migration_11_12 = object: Migration(11, 12) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE rssSources ADD style TEXT ")
            }
        }
        
        private val migration_12_13 = object: Migration(12, 13) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE rssSources ADD articleStyle INTEGER NOT NULL DEFAULT 0 ")
            }
        }
        
        private val migration_13_14 = object: Migration(13, 14) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `books_new` (`bookUrl` TEXT NOT NULL, `tocUrl` TEXT NOT NULL, `origin` TEXT NOT NULL, `originName` TEXT NOT NULL, 
                    `name` TEXT NOT NULL, `author` TEXT NOT NULL, `kind` TEXT, `customTag` TEXT, `coverUrl` TEXT, `customCoverUrl` TEXT, `intro` TEXT,
                    `customIntro` TEXT, `charset` TEXT, `type` INTEGER NOT NULL, `group` INTEGER NOT NULL, `latestChapterTitle` TEXT, `latestChapterTime` INTEGER NOT NULL,
                    `lastCheckTime` INTEGER NOT NULL, `lastCheckCount` INTEGER NOT NULL, `totalChapterNum` INTEGER NOT NULL, `durChapterTitle` TEXT, 
                    `durChapterIndex` INTEGER NOT NULL, `durChapterPos` INTEGER NOT NULL, `durChapterTime` INTEGER NOT NULL, `wordCount` TEXT, `canUpdate` INTEGER NOT NULL, 
                    `order` INTEGER NOT NULL, `originOrder` INTEGER NOT NULL, `useReplaceRule` INTEGER NOT NULL, `variable` TEXT, PRIMARY KEY(`bookUrl`))
                    """
                )
                database.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_books_name_author` ON `books_new` (`name`, `author`) ")
                database.execSQL("INSERT INTO books_new select * from books ")
                database.execSQL("DROP TABLE books")
                database.execSQL("ALTER TABLE books_new RENAME TO books")
            }
        }
        
        private val migration_14_15 = object: Migration(14, 15) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE bookmarks ADD bookAuthor TEXT NOT NULL DEFAULT ''")
            }
        }
        
        private val migration_15_17 = object: Migration(15, 17) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `readRecord` (`bookName` TEXT NOT NULL, `readTime` INTEGER NOT NULL, PRIMARY KEY(`bookName`))")
            }
        }
        
        private val migration_17_18 = object: Migration(17, 18) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `httpTTS` (`id` INTEGER NOT NULL, `name` TEXT NOT NULL, `url` TEXT NOT NULL, PRIMARY KEY(`id`))")
            }
        }
        
        private val migration_18_19 = object: Migration(18, 19) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `readRecordNew` (`androidId` TEXT NOT NULL, `bookName` TEXT NOT NULL, `readTime` INTEGER NOT NULL, PRIMARY KEY(`androidId`, `bookName`))")
                database.execSQL("INSERT INTO readRecordNew(androidId, bookName, readTime) select '${App.androidId}' as androidId, bookName, readTime from readRecord")
                database.execSQL("DROP TABLE readRecord")
                database.execSQL("ALTER TABLE readRecordNew RENAME TO readRecord")
            }
        }
        private val migration_19_20 = object: Migration(19, 20) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE book_sources ADD bookSourceComment TEXT")
            }
        }
    }
    
    abstract fun bookDao(): BookDao
    abstract fun bookGroupDao(): BookGroupDao
    abstract fun bookSourceDao(): BookSourceDao
    abstract fun bookChapterDao(): BookChapterDao
    abstract fun replaceRuleDao(): ReplaceRuleDao
    abstract fun searchBookDao(): SearchBookDao
    abstract fun searchKeywordDao(): SearchKeywordDao
    abstract fun rssSourceDao(): RssSourceDao
    abstract fun bookmarkDao(): BookmarkDao
    abstract fun rssArticleDao(): RssArticleDao
    abstract fun rssStarDao(): RssStarDao
    abstract fun cookieDao(): CookieDao
    abstract fun txtTocRule(): TxtTocRuleDao
    abstract fun readRecordDao(): ReadRecordDao
    abstract fun httpTTSDao(): HttpTTSDao
}