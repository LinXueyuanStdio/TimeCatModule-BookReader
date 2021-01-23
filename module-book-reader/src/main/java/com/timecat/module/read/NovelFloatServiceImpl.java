package com.timecat.module.read;

import com.timecat.identity.service.DataForFloatView;
import com.timecat.identity.service.NovelFloatService;
import com.xiaojinzi.component.anno.ServiceAnno;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import io.legado.app.App;
import io.legado.app.data.entities.Book;

/**
 * @author 林学渊
 * @email linxy59@mail2.sysu.edu.cn
 * @date 2019-07-10
 * @description null
 * @usage null
 */
@ServiceAnno(NovelFloatService.class)
public class NovelFloatServiceImpl implements NovelFloatService {

    @NotNull
    @Override
    public List<DataForFloatView> getAllData() {
        List<DataForFloatView> result = new ArrayList<>();
        List<Book> allBooks = App.Companion.getDb().bookDao().getAll();
        for (Book book : allBooks) {
            String icon = book.getCoverUrl();
            if (icon == null) icon = "R.drawable.ic_notebook";
            String name = book.getName();
            String id = book.getBookUrl();
            result.add(new DataForFloatView(icon, name, id));
        }
        return result;
    }
}
