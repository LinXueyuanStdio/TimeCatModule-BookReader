package io.legado.app.data.entities

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import io.legado.app.utils.GSON
import io.legado.app.utils.fromJsonObject
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize


@Parcelize
@Entity(
    tableName = "chapters",
    primaryKeys = ["url", "bookUrl"],
    indices = [(Index(value = ["bookUrl"], unique = false)),
        (Index(value = ["bookUrl", "index"], unique = true))],
    foreignKeys = [(ForeignKey(
        entity = Book::class,
        parentColumns = ["bookUrl"],
        childColumns = ["bookUrl"],
        onDelete = ForeignKey.CASCADE
    ))]
)    // 删除书籍时自动删除章节
data class BookChapter(
    var url: String = "",               // 章节地址
    var title: String = "",              // 章节标题
    var bookUrl: String = "",           // 书籍地址
    var index: Int = 0,                 // 章节序号
    var resourceUrl: String? = null,    // 音频真实URL
    var tag: String? = null,            //
    var start: Long? = null,            // 章节起始位置
    var end: Long? = null,               // 章节终止位置
    var variable: String? = null        //变量
) : Parcelable {

    @Ignore
    @IgnoredOnParcel
    var variableMap: HashMap<String, String>? = null
        private set
        get() {
            if (field == null) {
                field = GSON.fromJsonObject<HashMap<String, String>>(variable) ?: HashMap()
            }
            return field
        }

    fun putVariable(key: String, value: String) {
        variableMap?.put(key, value)
        variable = GSON.toJson(variableMap)
    }

    override fun hashCode() = url.hashCode()

    override fun equals(other: Any?) = if (other is BookChapter) other.url == url else false

}

