package com.arixo.arixoglass.base;

/**
 * Created by lovart on 2019/1/24
 */
public interface BaseMvp<M extends Model, V extends View, P extends Presenter> {
    M createModel();

    V createView();

    P createPresenter();
}
