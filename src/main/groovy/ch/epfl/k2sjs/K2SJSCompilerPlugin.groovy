package ch.epfl.k2sjs

import ch.epfl.k2sjs.tasks.CompileTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

class K2SJSCompilerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        project.logger.info('Applying kotlin plugin')
        project.pluginManager.apply('kotlin')
        project.logger.info('Plugins applied')


        final tasks = project.tasks

        final compile = tasks.create("k2sjs", CompileTask.class, new Action<CompileTask>() {
            @Override
            void execute(CompileTask compileTask) {
                // Default plugin configuration
                compileTask.setKotlinHome(scala.util.Properties.envOrElse("KOTLIN_HOME", "/usr/share/kotlin" ))
                compileTask.outputDir = project.getLayout().getBuildDirectory().dir("k2sjs").get()
                compileTask.dstFile = new File(compileTask.outputDir.asFile.getAbsolutePath() + "/out.js")
                compileTask.setCompilerOptions("")
                compileTask.setLinkerOptions("")
            }
        })
        project.logger.info(compile.name + " task added")
    }
}
