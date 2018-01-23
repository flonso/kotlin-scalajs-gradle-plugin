# Using the plugin

You need to clone the [Kotlin-Scala.js compiler](https://github.com/flonso/Kotlin-Scala.js) and publish it locally using `sbt publishM2`. (You might need to append the version number with SNAPSHOT if you intend to make changes to the plugin)

Then, publish the plugin locally by doing `gradle publish` from this project directory.

Finally you can launch the example project build by typing `gradle k2sjs`.
