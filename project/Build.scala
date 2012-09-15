import sbt._
import Keys._
import PlayProject._

object Build extends sbt.Build {
  val appName = "camelcode"
  val appVersion = "1.0-SNAPSHOT"

  val appDependencies = Seq(
    "org.apache.camel" % "camel-core" % "2.9.3",
    "org.apache.camel" % "camel-csv" % "2.9.3",
    "org.apache.camel" % "camel-bindy" % "2.9.3",
    "org.apache.camel" % "camel-jackson" % "2.9.3",
    "org.apache.camel" % "camel-http" % "2.9.3",

    "xml-apis" % "xml-apis-xerces" % "2.7.1" from "http://repo.opengeo.org/xml-apis/xml-apis-xerces/2.7.1/xml-apis-xerces-2.7.1.jar",
    "jgridshift" % "jgridshift" % "1.0" from "http://download.osgeo.org/webdav/geotools/jgridshift/jgridshift/1.0/jgridshift-1.0.jar",

    "org.geotools" % "gt-main" % "8.0" excludeAll (
      ExclusionRule(organization = "javax.media")
      ),

    "org.geotools" % "gt-epsg-hsql" % "8.0" excludeAll (
      ExclusionRule(organization = "javax.media")
      ),

    // Metrics
    "com.yammer.metrics" % "metrics-core" % "2.1.2",

    // Guice
    "com.google.inject" % "guice" % "3.0",
    "com.google.inject.extensions" % "guice-assistedinject" % "3.0",
    "com.google.inject.extensions" % "guice-multibindings" % "3.0",
    "com.google.inject.extensions" % "guice-throwingproviders" % "3.0",

    // Morphia
    "com.google.code.morphia" % "morphia" % "0.99.1-SNAPSHOT", // checkout Morphia manually and execute 'mvn install'
    "com.google.code.morphia" % "morphia-logging-slf4j" % "0.99"
  )

  val main = PlayProject(appName, appVersion, appDependencies, mainLang = JAVA).settings(
    lessEntryPoints <<= baseDirectory(_ ** "camelcode.less"),

    resolvers += "Local Maven Repository" at "file://" + Path.userHome + "/.m2/repository",
    resolvers += "Codehaus Repository" at "http://repository.codehaus.org/",
    resolvers += "Morphia Repository" at "http://morphia.googlecode.com/svn/mavenrepo/",
    resolvers += "Java.NET" at "http://download.java.net/maven/2",
    resolvers += "Open Source Geospatial Foundation Repository" at "http://download.osgeo.org/webdav/geotools/",
    resolvers += "OpenGeo Maven Repository" at "http://repo.opengeo.org"
  )
}
