package com.example.tsnt.plugin

import com.example.tsnt.extension.CreateActivityExtension
import com.example.tsnt.task.CreateActivityTask
import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * @Author: zhangxiaozong
 * @Date: 2018-03-24 11:06
 * @Description:
 */

class CreateActivityPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        def extension = project.extensions.create("activitySetting", CreateActivityExtension)
        project.task("createActivity", type: CreateActivityTask) {
            group "createActivity"
        }
    }
}
