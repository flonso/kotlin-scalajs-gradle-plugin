package ch.epfl.k2sjs

import ch.epfl.k2sjs.tasks.CompileTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project

class K2SJSCompilerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // TODO: Configure own sourceset so that there is no need for the Kotlin plugin anymore
        // See -> https://github.com/gradle/gradle/search?utf8=%E2%9C%93&q=GroovySourceSet&type=
        project.logger.info('Applying kotlin plugin')
        project.pluginManager.apply('kotlin')
        project.logger.info('Plugins applied')

        final tasks = project.tasks

        final buildTask = tasks.create("k2sjs", CompileTask.class, new Action<CompileTask>() {
            @Override
            void execute(CompileTask compileTask) {
                // Default plugin configuration
                compileTask.srcFiles = project.sourceSets.main.kotlin.files
                compileTask.setKotlinHome(scala.util.Properties.envOrElse("KOTLIN_HOME", "/usr/share/kotlin" ))
                compileTask.outputDir = project.getLayout().getBuildDirectory().dir("k2sjs").get()
                compileTask.dstFile = new File(compileTask.outputDir.asFile.getAbsolutePath() + "/out.js")
                compileTask.setCompilerOptions("")
                compileTask.setLinkerOptions("")
            }
        })

        project.logger.info(buildTask.name + " task added")
    }
}
