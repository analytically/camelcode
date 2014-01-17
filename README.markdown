CamelCode [![Build Status](https://travis-ci.org/analytically/camelcode.png)](https://travis-ci.org/analytically/camelcode)
=========

A tech demo built using [Play Framework 2.2](http://www.playframework.org) (java) that imports the
[CodePoint Open](https://www.ordnancesurvey.co.uk/opendatadownload/products.html) UK postcode dataset
and offers a Geocoding RESTful API and a map. It also demonstrates how
[Google Guice](http://code.google.com/p/google-guice/) can be integrated in a Play Framework Java application.

Development sponsored by [Coen Recruitment](http://www.coen.co.uk). Follow [@analytically](http://twitter.com/analytically) for updates.

### Requirements

- JDK 6 or later
- [Play Framework 2.2](http://www.playframework.org)
- [MongoDB](http://www.mongodb.org)

### Setup

Edit `conf/application.conf` and point it to a MongoDB installation (defaults to `localhost`), and execute

``` sh
play run
```

Then drop the [CodePoint Open CSV](https://www.ordnancesurvey.co.uk/opendatadownload/products.html) (scroll halfway down, 20mb)
files in the `codepointopen` directory.

After each file is imported, it will be moved to the `codepointopen/done` directory.

Then visit [http://localhost:9000](http://localhost:9000) and you should see the welcome screen.
Check out the [server metrics](http://localhost:9000/servermetrics) or the [map](http://localhost:9000/map).

### REST API and JSON

GET [http://localhost:9000/latlng/POSTCODE](http://localhost:9000/latlng/BS106TF) to geocode a UK postcode. Response will be JSON:

``` json
{"latitude":51.505615,"longitude":-2.6120315}
```

### Screenshots

![Welcome Page](screenshot.png)

![Map](screenshot2.png)

### Technology

* [Play Framework 2.2](http://www.playframework.org), as web framework
* [Apache Camel](http://camel.apache.org) to [process and monitor](https://github.com/analytically/camelcode/blob/master/app/Global.java#L103) the `codepointopen` directory and to tell the actors about the postcodes (split(body()))
* [Akka](http://akka.io) provides a nice concurrency model [to process the 1.7 million postcodes](https://github.com/analytically/camelcode/blob/master/app/actors/ProcessCPOCsvEntry.java) in under one minute on modern hardware
* [GeoTools](http://www.geotools.org) [converts](https://github.com/analytically/camelcode/blob/master/app/actors/ProcessCPOCsvEntry.java) the eastings/northings to latitude/longitude
* [Guice](http://code.google.com/p/google-guice/) for [Dependency Injection](https://github.com/analytically/camelcode/blob/master/app/Global.java#L53) (not too much to inject yet though)
* [Metrics](http://metrics.codahale.com/) for metrics
* [MongoDB](http://www.mongodb.org) as database with two-dimensional geospatial indexes (see [Geospatial Indexing](http://www.mongodb.org/display/DOCS/Geospatial+Indexing))
* [Morphia](https://github.com/mongodb/morphia) for 'Object-Document Mapping'
* [Leaflet](http://leafletjs.com/) for the map
* [Bootstrap](http://getbootstrap.com/) and [Font Awesome](http://fortawesome.github.com/Font-Awesome/) for the UI

### License

Licensed under the [WTFPL](http://en.wikipedia.org/wiki/WTFPL).

This data contains Ordnance Survey data &copy; Crown copyright and database right 2013. Code-Point Open contains
Royal Mail data &copy; Royal Mail copyright and database right 2012. Code-Point Open and ONSPD contains National Statistics
data &copy; Crown copyright and database right 2013.

OS data may be used under the terms of the [OS OpenData licence](http://www.ordnancesurvey.co.uk/oswebsite/docs/licences/os-opendata-licence.pdf).
