# Using the plugin

You need to clone the [Kotlin-Scala.js compiler](https://github.com/flonso/Kotlin-Scala.js) here and publish it locally using `sbt publishM2`. (Maybe change the version number with SNAPSHOT if you need to make changes)

Then, publish the plugin locally by doing `gradle publish` from this project directory.

Finally you can launch the example project build by typing `gradle k2sjs`.
