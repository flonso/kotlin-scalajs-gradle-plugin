package ch.epfl.k2sjs.tasks

import ch.epfl.k2sjsir.K2SJSIRCompiler
import org.gradle.api.GradleException
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.jetbrains.kotlin.cli.common.ExitCode
import org.scalajs.cli.Scalajsld

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.*
import org.gradle.api.tasks.TaskAction

class CompileTask extends DefaultTask {
    final String description = "Converts all kotlin files to sjsir, then compiles them."

    final String scalaJsJar = "scalajs-library_2.12-1.0.0-M2.jar"

    /**
     * Input source files
     */
    @InputFiles
    public Set<File> srcFiles

    /**
     * Where to put the generated .sjsir and .js files
     */
    public Directory outputDir

    /**
     * The file to which the final JS code will be written
     */
    @OutputFile
    public File dstFile

    /**
     * The path to Kotlin install directory
     */
    private File kotlinHome

    File getKotlinHome() {
        return this.kotlinHome
    }

    File setKotlinHome(String path) {
        File tmp = new File(path)
        assertFileExistsAndIsDirectory(tmp)
        this.kotlinHome = tmp

        return this.kotlinHome
    }

    /**
     * The compiler still depends on the ScalaJS stdlib, this is
     * the path to a jar.
     */
    private File scalaJsLib

    File getScalaJsLib() {
        return this.scalaJsLib
    }

    File setScalaJsLib(String path) {
        File tmp = new File(path)
        assertFileExistsAndIsDirectory(tmp)
        this.scalaJsLib = tmp

        return this.scalaJsLib
    }

    /**
     * All other compiler options in the form of a String :
     * -Xallow-kotlin-package, -d "
     */
    private String compilerOptions

    String getCompilerOptions() {
        return this.compilerOptions
    }

    String setCompilerOptions(String options) {
        this.compilerOptions = options
    }

    /**
     * Linker options as a string
     */
    private String linkerOptions

    String getLinkerOptions() {
        return this.linkerOptions
    }

    String setLinkerOptions(String options) {
        this.linkerOptions = options
    }

    private static void assertFileExistsAndIsDirectory(File f) {
        if (!f.exists() || !f.isDirectory())
            throw new GradleException("ScalaJS stdlib jar file must exist and be a directory")
    }

    @TaskAction
    def run() {

        if (dstFile == null)
            throw new GradleException("Destination file must not be null")

        if (srcFiles == null || srcFiles.isEmpty())
            throw new GradleException("You must specify at least one source file")

        ArrayList<String> compilerArgs = new ArrayList<>()
        // Add user arguments
        compilerArgs.add("-kotlin-home")
        compilerArgs.add(getKotlinHome().getAbsolutePath())
        compilerArgs.add("-d")
        compilerArgs.add(outputDir.getAsFile().getAbsolutePath())
        compilerArgs.add("-output")
        compilerArgs.add(dstFile.getAbsolutePath())

        if (getCompilerOptions() != "")
            getCompilerOptions().split(" ").each { compilerArgs.add(it) }

        // Add source files
        srcFiles.each {
            compilerArgs.add(it.getPath())
        }

        project.logger.info("Running the compiler with arguments : $compilerArgs")

        // Compile
        String[] tmp = compilerArgs.toArray() as String[]
        final output = (new K2SJSIRCompiler()).exec(System.err, tmp)

        if (ExitCode.OK != output) {
            throw new GradleException("Compilation failed with exit code $output. See log above for details. (Use --info)")
        } else {
            project.logger.info("Compilation successful !")
        }

        // Prepare linker options
        ArrayList<String> linkerArgs = new ArrayList<>()
        linkerArgs.add("--stdlib")
        linkerArgs.add(getScalaJsLib().getAbsolutePath() + "/" + scalaJsJar)
        linkerArgs.add("-o")
        linkerArgs.add(dstFile.getAbsolutePath())
        linkerArgs.add(outputDir.getAsFile().getAbsolutePath())
        linkerArgs.add("-c")
        linkerArgs.add("-u")

        if (getLinkerOptions() != "")
            linkerArgs.add(getLinkerOptions())

        project.logger.info("Linker will be run with arguments $linkerArgs")

        project.logger.info("Running ScalaJS linker...")
        tmp = linkerArgs.toArray() as String[]
        Scalajsld.main(tmp)
        project.logger.info("\nDone with the ScalaJS linker.")
    }

}
