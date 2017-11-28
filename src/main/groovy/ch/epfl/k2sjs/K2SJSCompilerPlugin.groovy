package ch.epfl.k2sjs

import org.gradle.api.Plugin
import org.gradle.api.Project

import ch.epfl.k2sjs.tasks.CompileTask

class K2SJSCompilerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.logger.info('Applying java plugin')
        project.pluginManager.apply('java')
        project.logger.info('Applying kotlin plugin')
        project.pluginManager.apply('kotlin')
        project.logger.info('Plugins applied')


        final tasks = project.tasks

        final compile = tasks.create("k2sjs", CompileTask.class)
        project.logger.info(compile.name + " task added")
    }
}
