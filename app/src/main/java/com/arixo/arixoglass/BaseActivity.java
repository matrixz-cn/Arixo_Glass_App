package com.arixo.arixoglass;

import android.os.Bundle;

import com.arixo.arixoglass.base.BaseMvpActivity;
import com.arixo.arixoglass.base.Model;
import com.arixo.arixoglass.base.Presenter;
import com.arixo.arixoglass.base.View;

/**
 * Created by lovart on 2019/1/24
 */
public abstract class BaseActivity<M extends Model, V extends View, P extends Presenter> extends BaseMvpActivity<M, V, P> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initView();
        initData();
    }

    protected abstract void initView();

    protected abstract void initData();
}
