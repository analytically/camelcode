import sbt._
import Keys._
import play.Project._

object Build extends sbt.Build {
  val appName = "camelcode"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    javaCore,

    "org.apache.camel" % "camel-core" % "2.10.4",
    "org.apache.camel" % "camel-csv" % "2.10.4",
    "org.apache.camel" % "camel-bindy" % "2.10.4",
    "org.apache.camel" % "camel-jackson" % "2.10.4",
    "org.apache.camel" % "camel-http" % "2.10.4",

    "xml-apis" % "xml-apis-xerces" % "2.7.1" from "http://repo.opengeo.org/xml-apis/xml-apis-xerces/2.7.1/xml-apis-xerces-2.7.1.jar",
    "jgridshift" % "jgridshift" % "1.0" from "http://download.osgeo.org/webdav/geotools/jgridshift/jgridshift/1.0/jgridshift-1.0.jar",

    "org.geotools" % "gt-main" % "8.7" excludeAll (
      ExclusionRule(organization = "javax.media")
      ),

    "org.geotools" % "gt-epsg-hsql" % "8.7" excludeAll (
      ExclusionRule(organization = "javax.media")
      ),

    // Metrics
    "com.yammer.metrics" % "metrics-core" % "2.2.0",

    // Guice
    "com.google.inject" % "guice" % "3.0",
    "com.google.inject.extensions" % "guice-assistedinject" % "3.0",
    "com.google.inject.extensions" % "guice-multibindings" % "3.0",
    "com.google.inject.extensions" % "guice-throwingproviders" % "3.0",

    // Morphia
    "com.google.code.morphia" % "morphia" % "0.101.0-SNAPSHOT",
    "com.google.code.morphia" % "morphia-logging-slf4j" % "0.101.0-SNAPSHOT"
  )

  val main = play.Project(appName, appVersion, appDependencies).settings(
    resolvers += "Morphia Repo" at "http://morphia.googlecode.com/svn/mavenrepo/",
    resolvers += "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/",
    resolvers += "OpenGeo Maven Repository" at "http://repo.opengeo.org"
  )
}
