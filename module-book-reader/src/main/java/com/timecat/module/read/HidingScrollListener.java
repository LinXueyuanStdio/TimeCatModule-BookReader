package com.timecat.module.read;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author 林学渊
 * @email linxy59@mail2.sysu.edu.cn
 * @date 2020-02-04
 * @description null
 * @usage null
 */
public abstract class HidingScrollListener extends RecyclerView.OnScrollListener {
    private int distance = 0;

    private boolean bottomIsShowing = true;

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
        super.onScrolled(recyclerView, dx, dy);
        if (this.distance > 400 && this.bottomIsShowing) {
            goingUp();
            this.bottomIsShowing = false;
            this.distance = 0;
        } else if (this.distance < -20 && !this.bottomIsShowing) {
            goingDown();
            this.bottomIsShowing = true;
            this.distance = 0;
        }
        if ((this.bottomIsShowing && dy > 0) || (!this.bottomIsShowing && dy < 0)) {
            this.distance += dy;
        }
    }

    protected abstract void goingUp();

    protected abstract void goingDown();
}

