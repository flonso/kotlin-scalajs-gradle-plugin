package ch.epfl.k2sjs.tasks

import ch.epfl.k2sjsir.K2SJSIRCompiler
import org.gradle.api.GradleException
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.js.K2JSCompiler
import org.scalajs.cli.Scalajsld

import org.gradle.api.DefaultTask
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction


public class CompileTask extends DefaultTask {
    final String description = "Converts all kotlin files to sjsir, then compiles them."

    @InputFiles
    public FileCollection srcFiles

    @OutputFile
    public File dstFile

    // Compiler options
    private ROOT_LIB = "src/test/resources/lib"
    private ROOT_OUT = "src/test/resources/out"
    private ROOT_LIB_OUT = "src/test/resources/kotlin-out"
    private SCALA_JS_VERSION = "1.0.0-M1"
    private SCALA_JS_JAR = "scalajs-library_2.12-"+ SCALA_JS_VERSION +".jar"
    private KOTLIN_HOME = scala.util.Properties.envOrElse("KOTLIN_HOME", "/usr/share/kotlin" )

    private String[] k2sjsOptions = ["-Xallow-kotlin-package"/*, "-d", ROOT_OUT*/, "-kotlin-home", KOTLIN_HOME, "-output", "output"]

    // Scalajsld options
    private String[] linkerOptions = ["--stdlib", "$ROOT_LIB/$SCALA_JS_JAR", ROOT_OUT, ROOT_LIB_OUT, "-c"]

    @TaskAction
    def run() {
        ArrayList<String> args = new ArrayList<>()
        // Add source files
        project.sourceSets.main.kotlin.files.each {
            args.add(it.getPath())
        }
        // Add user arguments
        k2sjsOptions.each {
            args.add(it)
        }

        // Compile to sjsir
        project.logger.info("Running the compiler...")
        String[] tmp = args.toArray() as String[]
        final output = (new K2SJSIRCompiler()).exec(System.err, tmp) // --> Abstract method exception
        //final output = (new K2JSCompiler()).exec(System.err, tmp) //  --> Compile to JS nicely

        if (ExitCode.OK != output) {
            throw new GradleException("Compilation failed with exit code $output. See log for details.")
        } else {
            project.logger.info("Compilation successful !")
        }

        /*
        args = new ArrayList<>()
        linkerOptions.each {
            args.add(it)
        }

        args.add("-o")
        if (dstFile != null) {
            args.add(dstFile.getAbsolutePath())
        } else {
            args.add(project.buildDir.getAbsolutePath() + "/" + project.name + ".js")
        }

        project.logger.info("Proceeding with the ScalaJS linker...")
        tmp = args.toArray() as String[]
        tmp.each {
            project.logger.info(it)
        }
        Scalajsld.main(tmp)
        project.logger.info("\nDone with the ScalaJS linker.")
        */
    }

}
