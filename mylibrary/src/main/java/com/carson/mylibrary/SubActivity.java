package com.carson.mylibrary;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;

/**
 * Created by yujunyao on 1/19/22.
 */
public class SubActivity extends FragmentActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub);

        findViewById(R.id.button1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //对于接口有效
                startActivity(new Intent(SubActivity.this, Sub2Activity.class));
            }
        });

        findViewById(R.id.button2).setOnClickListener(v -> {
            startActivity(new Intent(SubActivity.this, Sub2Activity.class));
        });
    }
}
