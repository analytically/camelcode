CamelCode
=========

A tech demo built on top of [Play Framework 2.0](http://www.playframework.org/) that imports the
[CodePoint Open](https://www.ordnancesurvey.co.uk/opendatadownload/products.html) UK postcode dataset
and offers a Geocoding RESTful API and a map.

Prerequisites: [MongoDB](http://www.mongodb.org/) and [Play Framework 2.0](http://www.playframework.org/).

Setup
-----

Edit `conf/application.conf` and point it to a MongoDB installation, and execute

```
play run
```

Then put the [CodePoint Open CSVs](https://www.ordnancesurvey.co.uk/opendatadownload/products.html) (scroll halfway down, 20mb)
in the `codepointopen` directory.

After they are processed, they will be moved to the `codepointopen/done` directory.

Then visit [http://localhost:9000/](http://localhost:9000/) and you should see the welcome screen.
Visit [http://localhost:9000/servermetrics](http://localhost:9000/servermetrics) for server metrics.

JSON
----

GET [http://localhost:9000/latlng/POSTCODE](http://localhost:9000/latlng/BS106TF) to geocode a UK postcode. Response will be JSON:

```
{"latitude":51.505615,"longitude":-2.6120315}
```

Technology
----------

* [Play Framework 2.0](http://www.playframework.org/), thank god for this!
* [Apache Camel](http://camel.apache.org/) to [process and monitor](https://github.com/analytically/camelcode/blob/master/app/Global.java#L103) the `codepointopen` directory and to tell the actors about the postcodes (split(body()))
* [Akka 2.0](http://akka.io/) provides a nice concurrent model [to process the 1.7 million postcodes](https://github.com/analytically/camelcode/blob/master/app/actors/ProcessCodePointOpenCsv.java) in under one minute on modern hardware
* [GeoTools 8](http://www.geotools.org/) [converts](https://github.com/analytically/camelcode/blob/master/app/actors/ProcessCodePointOpenCsv.java#L68) the eastings/northings to latitude/longitude
* [Guice](http://code.google.com/p/google-guice/) for [Dependency Injection](https://github.com/analytically/camelcode/blob/master/app/Global.java#L53) (not too much to inject yet though)
* [Metrics](https://github.com/codahale/metrics) for metrics
* [MongoDB](http://www.mongodb.org/) and [Morphia](http://code.google.com/p/morphia/) as storage and ORM
* [Leaflet](http://leaflet.cloudmade.com/) for the map
* [Twitter Bootstrap](http://twitter.github.com/bootstrap/) and [Font Awesome](http://fortawesome.github.com/Font-Awesome/) for the UI

License
-------

Licensed under the [WTFPL](http://en.wikipedia.org/wiki/WTFPL).

This data contains Ordnance Survey data &copy; Crown copyright and database right 2011. Code-Point Open contains
Royal Mail data &copy; Royal Mail copyright and database right 2011. Code-Point Open and ONSPD contains National Statistics
data &copy; Crown copyright and database right 2011.

OS data may be used under the terms of the [OS OpenData licence](http://www.ordnancesurvey.co.uk/oswebsite/docs/licences/os-opendata-licence.pdf).

Todo
----------

* Fix distance calculation, Morphia does't do geoNear yet (grmbl)
* Add add a map using [Leaflet](http://leaflet.cloudmade.com/) displaying all UK postcodes (need to think how to dynamically add layers depending on zoom level)

Screenshots
-----------

![Welcome Page](https://github.com/analytically/camelcode/raw/master/screenshot.png)
![Map](https://github.com/analytically/camelcode/raw/master/screenshot2.png)