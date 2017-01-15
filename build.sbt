name := """demo"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
	javaCore,
  cache,
  javaWs,
  "org.webjars" % "webjars-play_2.11" % "2.5.0-2",
  "org.webjars" % "bootstrap" % "4.0.0-alpha.2",
  	"org.apache.httpcomponents" % "httpclient" % "4.3.6",
  	"org.mindrot" % "jbcrypt" % "0.3m",
  	"commons-lang" % "commons-lang" % "2.6",
  	"org.mongojack" % "mongojack" % "2.5.1",
  	"commons-codec" % "commons-codec" % "1.10",
  	"commons-io" % "commons-io" % "2.4",  	
  	"org.json" % "json" % "20090211",    
    "com.googlecode.json-simple" % "json-simple" % "1.1.1",
    "org.jsmpp" % "jsmpp" % "2.2.3",
    "it.innove" % "play2-pdf" % "1.5.1"
)


lazy val demo = (project in file(".")).enablePlugins(PlayJava)


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator

//ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
//EclipseKeys.createSrc := EclipseCreateSrc.All

parallelExecution in Test := false