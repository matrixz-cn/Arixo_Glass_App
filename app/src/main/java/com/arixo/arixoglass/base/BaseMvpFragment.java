package com.arixo.arixoglass.base;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by lovart on 2019/1/24
 */
public abstract class BaseMvpFragment<M extends Model, V extends View, P extends Presenter> extends Fragment implements BaseMvp<M, V, P> {
    protected P presenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        presenter = createPresenter();
        if (presenter != null) {
            presenter.registerModel(createModel());
            presenter.registerView(createView());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (presenter != null) {
            presenter.destroy();
        }
    }
}
