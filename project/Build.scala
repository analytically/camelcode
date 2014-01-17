import sbt._
import Keys._
import play.Project._

object Build extends sbt.Build {
  val appName = "camelcode"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    javaCore,

    "org.apache.camel" % "camel-core" % "2.11.3",
    "org.apache.camel" % "camel-csv" % "2.11.3",
    "org.apache.camel" % "camel-bindy" % "2.11.3",
    "org.apache.camel" % "camel-jackson" % "2.11.3",
    "org.apache.camel" % "camel-http" % "2.11.3",

    "org.geotools" % "gt-main" % "10.3" excludeAll
      ExclusionRule(organization = "javax.media")
    ,

    "org.geotools" % "gt-epsg-hsql" % "10.3" excludeAll
      ExclusionRule(organization = "javax.media")
    ,

    "org.reflections" % "reflections" % "0.9.9-RC1",

    // Metrics
    "com.yammer.metrics" % "metrics-core" % "2.2.0",

    // Guice
    "com.google.inject" % "guice" % "3.0",
    "com.google.inject.extensions" % "guice-assistedinject" % "3.0",
    "com.google.inject.extensions" % "guice-multibindings" % "3.0",
    "com.google.inject.extensions" % "guice-throwingproviders" % "3.0",

    // Morphia
    "org.mongodb" % "mongo-java-driver" % "2.11.3",
    "org.mongodb.morphia" % "morphia" % "0.105",
    "org.mongodb.morphia" % "morphia-logging-slf4j" % "0.105"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/",
    resolvers += "OpenGeo Maven Repository" at "http://repo.opengeo.org"
  )
}