package com.arixo.arixoglass.view.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.arixo.arixoglass.R;
import com.arixo.arixoglass.utils.ToolUtil;

public class AboutActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        TextView titleText = findViewById(R.id.tv_title_text);
        TextView optionText = findViewById(R.id.tv_option_button);
        TextView appVersion = findViewById(R.id.tv_app_version);
        TextView backButton = findViewById(R.id.tv_backward_button);

        titleText.setText(R.string.about_text);
        optionText.setVisibility(View.GONE);
        Button checkUpdateButton = findViewById(R.id.b_check_for_update);
        appVersion.setText(String.valueOf(ToolUtil.getVerName(this)));
        backButton.setOnClickListener(view -> onBackPressed());
    }
}
