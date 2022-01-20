package com.carson.clickplugin

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by yujunyao on 1/19/22.
 */
class ClickPlugin implements Plugin<Project> {

    @Override
    void apply(Project target) {
        println("***************************************************************************")
        println("**********************************防重插件**********************************")
        println("***************************************************************************")

    }

}
