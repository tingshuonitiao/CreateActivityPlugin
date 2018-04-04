package com.example.tsnt.template;

/**
 * @Author: zhangxiaozong
 * @Date: 2018-03-24 21:41
 * @Description:
 */

class ActivityTemplate {
    def template =
            '''package ${applicationId}.${packageName};

import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import ${applicationId}.R;

public class ${activityName} extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.${xmlName});
    }
}
'''
}
