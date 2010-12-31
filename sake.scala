import sake.Project._

// Define some convenient variables.
val srcDir   = "src" + environment.fileSeparator
var specDir  = "spec" + environment.fileSeparator
val buildDir = "build" + environment.fileSeparator
val libDir   = "lib" + environment.fileSeparator
val sxr      = libDir + environment.fileSeparator + "sxr-0.1.jar"

// Version strings used for the generated jars:
// What version of Sake? Can specify on the command line with VERSION=...
val version = environment.environmentVariables.getOrElse("VERSION", "1.1")
// What Scala version of Sake? Can specify on the command line with SCALA_VERSION=...
val scalaVersion = environment.environmentVariables.getOrElse("SCALA_VERSION", "2.8.0.RC7")

// If true, don't actually run any commands.
environment.dryRun = false

// If true, show stack traces when a failure happens (doesn't affect "errors").
showStackTracesOnFailures = false

// Logging level: Info, Notice, Warn, Error, Failure
log.threshold = Level.Info

// Add to the classpath using list semantics.
environment.classpath :::= (files(libDir + "*.jar") filterNot (files(libDir + "*src.jar")contains))
environment.classpath ::= (files(libDir + scalaVersion + environment.fileSeparator + "*.jar"))
environment.classpath ::= buildDir

target('all -> List('clean, 'compile, 'spec, 'jars))

target('jars -> List('jar, 'srcjar))

target('jar) {
    val jarName = buildDir+environment.fileSeparator+"sake-"+scalaVersion+"-"+version+".jar"
    sh("jar cf "+jarName+" -C "+buildDir+" sake")
    if (environment.isWindows)
    	sh("cmd /c copy "+jarName+" "+libDir+environment.fileSeparator+scalaVersion)
    else
    	sh("cp "+jarName+" "+libDir+environment.fileSeparator+scalaVersion)
}

target('srcjar) {
    val jarName = buildDir+environment.fileSeparator+"sake-"+scalaVersion+"-"+version+"-src.jar"
    sh("jar cf "+jarName+" -C "+buildDir+" sake")
    if (environment.isWindows)
    	sh("cmd /c copy "+jarName+" "+libDir+environment.fileSeparator+scalaVersion)
    else
    	sh("cp "+jarName+" "+libDir+environment.fileSeparator+scalaVersion)
}

target('spec) {
    specs(
       'classpath -> environment.classpath, 
       'path -> "./spec/**/*.scala", 
       'pattern -> ".*Spec.*"
    )
}

target('compile -> List('clean, 'build_dir)) {
    scalac(
        'files     -> files(srcDir+"**/*.scala", specDir+"**/*.scala"),
        'classpath -> environment.classpath,
        'd         -> buildDir,
        'opts      -> ("-unchecked -deprecation") // -Xplugin:" + sxr +" -P:sxr:base-directory:.")
    )
}

target('clean) {
    deleteRecursively(buildDir)
}

target('build_dir) {
    mkdir(buildDir)
}

target('fail) {
    fail("boo!")
}

import sake.util._
target('ls) {
	if (environment.isWindows)
		shell('command -> "cmd", 'opts -> List("/c","dir","."), 'outputFile -> File("foo.txt"))
	else
		shell('command -> "ls", 'opts -> ".", 'outputFile -> File("foo.txt"))
}
target('cat ) {
	if (environment.isWindows)
		shell('command -> "cmd", 'opts -> List("/c","type"), 'inputFile -> File("foo.txt"), 'outputFile -> File("foo.txt2"))
	else
		shell('command -> "cat", 'inputFile -> File("foo.txt"), 'outputFile -> File("foo.txt2"))
}

