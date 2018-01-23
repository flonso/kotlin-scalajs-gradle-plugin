package ch.epfl.k2sjs.tasks

import ch.epfl.k2sjsir.K2SJSIRCompiler
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.*
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.jetbrains.kotlin.cli.common.ExitCode
import org.scalajs.cli.Scalajsld

class CompileTask extends DefaultTask {
    final String description = "Converts all kotlin files to sjsir, then compiles them."

    /**
     * Input source files
     */
    @InputFiles
    public Set<File> srcFiles

    /**
     * Where to put the generated .sjsir files
     */
    public File outputDir

    /**
     * The file to which the final JS code will be written
     */
    @OutputFile
    public File dstFile

    File getDstFile() {
        return this.dstFile
    }

    File setDstFile(String path) {
        File tmp = new File(path)
        assertIsFileAndHasJsExtension(tmp)

        this.dstFile = tmp

        return this.dstFile
    }

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
     * the path to its jar.
     */
    private File scalaJsLib

    File getScalaJsLib() {
        if (this.scalaJsLib == null) {
            final lib = project.configurations.compile.filter { it.getAbsolutePath().contains("scalajs-library")}
            this.scalaJsLib = new File(lib.getSingleFile().getAbsolutePath())
        }

        return this.scalaJsLib
    }

    /**
     * The optimization level the code should be generated with
     */
    private String optimize
    String getOptimize() {
        return this.optimize
    }

    String setOptimize(String opt) {
        if (!checkOptimizationLevel(opt))
            throw new GradleException("Optimization level must be one of: noOpt, fastOpt or fullOpt")

        this.optimize = opt
    }

    private final optimizationLevels = [
            "noOpt": "-n",
            "fastOpt": "-f",
            "fullOpt": "-u"
    ]

    private boolean checkOptimizationLevel(String lvl) {
        return optimizationLevels.containsKey(lvl)
    }

    private String getOptimizeArgument() {
        return optimizationLevels.get(this.optimize)
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

    private static void assertIsFileAndHasJsExtension(File f) {
        if (!f.getAbsolutePath().endsWith(".js"))
            throw new GradleException("The destination file must be a .js file but got " + f.getAbsolutePath())
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
        compilerArgs.add(outputDir.getAbsolutePath())
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
        linkerArgs.add(getScalaJsLib().getAbsolutePath())
        linkerArgs.add("-o")
        linkerArgs.add(dstFile.getAbsolutePath())
        linkerArgs.add(outputDir.getAbsolutePath())
        linkerArgs.add("-c")
        linkerArgs.add(getOptimizeArgument())

        if (getLinkerOptions() != "")
            getLinkerOptions().split(" ").each { linkerArgs.add(it) }

        project.logger.info("Linker will be run with arguments $linkerArgs")

        project.logger.info("Running ScalaJS linker...")
        tmp = linkerArgs.toArray() as String[]
        Scalajsld.main(tmp)

        if (!getDstFile().exists())
            throw new GradleException("Linking failed. See log above for details. (Use --info)")

        project.logger.info("\nDone with the ScalaJS linker.")
    }

}
