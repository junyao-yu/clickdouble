package com.carson.click

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by yujunyao on 1/19/22.
 */
class ClickPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        println("***************************************************************************")
        println("**********************************防重插件**********************************")
        println("***************************************************************************")

        project.extensions.create("clickDouble", ClickDoubleExtension)
        project.afterEvaluate {
            println("是否开启 = " + project.clickDouble.isOpen)
        }

        AppExtension appExtension = project.extensions.findByType(AppExtension.class)
        appExtension.registerTransform(new ClickDoubleTransform(project, project.clickDouble))
    }

}
