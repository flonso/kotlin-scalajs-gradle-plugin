package ch.epfl.k2sjs

import ch.epfl.k2sjs.tasks.CompileTask
import org.gradle.api.Action
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task

class K2SJSCompilerPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {
        // TODO: Configure own sourceset so that there is no need for the Kotlin plugin anymore
        // See -> https://github.com/gradle/gradle/search?utf8=%E2%9C%93&q=GroovySourceSet&type=
        project.logger.info('Applying kotlin plugin')
        project.pluginManager.apply('kotlin2js')
        project.logger.info('Plugins applied')

        final tasks = project.tasks

        final buildAction = new Action<CompileTask>() {
            @Override
            void execute(CompileTask compileTask) {
                // Default plugin configuration
                compileTask.srcFiles = project.sourceSets.main.kotlin.files
                compileTask.setKotlinHome(scala.util.Properties.envOrElse("KOTLIN_HOME", "/usr/share/kotlin" ))
                compileTask.outputDir = new File(project.getBuildDir().absolutePath + "/k2sjs")
                compileTask.setDstFile(project.getBuildDir().absolutePath + "/" + project.getProjectDir().name +".js")
                compileTask.setCompilerOptions("")
                compileTask.setLinkerOptions("")
                compileTask.setOptimize("fastOpt")
            }
        }

        final buildTask = tasks.create("k2sjs", CompileTask.class, buildAction)

        project.logger.info(buildTask.name + " task added")


        // Backup the kotlin plugin build task and replace it with ours
        final ktBuild = tasks.getByName("build")
        final ktActions = ktBuild.actions.collect()
        final ktDependencies = ktBuild.dependsOn.collect()
        ktBuild.deleteAllActions()
        ktBuild.dependsOn.clear()
        ktBuild.dependsOn(buildTask)

        // Keep access to the original
        final ktOriginalBuild = tasks.create("build-original")
        ktOriginalBuild.setActions(ktActions)
        ktOriginalBuild.setDependsOn(ktDependencies)
    }
}
