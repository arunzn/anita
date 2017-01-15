name := """anita"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.11.7"

libraryDependencies ++= Seq(
	javaCore,
  cache,
  javaWs,
  "org.webjars" % "webjars-play_2.11" % "2.5.0-2",
  "org.webjars" % "bootstrap" % "4.0.0-alpha.2",
  	"org.mongodb" % "mongo-java-driver" % "3.2.2",
  	"org.apache.httpcomponents" % "httpclient" % "4.3.6",
  	"org.mindrot" % "jbcrypt" % "0.3m",
  	"commons-lang" % "commons-lang" % "2.6",
  	"org.mongojack" % "mongojack" % "2.5.1",
  	"commons-codec" % "commons-codec" % "1.10",
  	"org.springframework" % "spring-context-support" % "4.2.4.RELEASE",
  	"commons-io" % "commons-io" % "2.4",
  	"com.amazonaws" % "aws-java-sdk-s3" % "1.10.74",
  	"com.amazonaws" % "aws-java-sdk-core" % "1.10.74",
    "com.amazonaws" % "aws-java-sdk" % "1.10.74",
  	"org.apache.xmlbeans" % "xmlbeans" % "2.4.0",
  	"org.json" % "json" % "20090211",
    "joda-time" % "joda-time" % "2.9.4",
    "javax.mail" % "mail" % "1.5.0-b01",
    "org.jsmpp" % "jsmpp" % "2.2.3",
    "org.apache.poi" % "poi-ooxml" % "3.14",
    "com.google.gcm" % "gcm-server" % "1.0.0",
    "com.googlecode.json-simple" % "json-simple" % "1.1.1",
    "org.jsmpp" % "jsmpp" % "2.2.3",
    "it.innove" % "play2-pdf" % "1.5.1"
)


lazy val anita = (project in file(".")).enablePlugins(PlayJava)


// Play provides two styles of routers, one expects its actions to be injected, the
// other, legacy style, accesses its actions statically.
//routesGenerator := InjectedRoutesGenerator

//ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) }
//EclipseKeys.createSrc := EclipseCreateSrc.All

parallelExecution in Test := false