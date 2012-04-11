CamelCode - Geocode UK postcode addresses
=========================================

Prerequisites: MongoDB and Play Framework 2.0.

Setup
-----

Edit `conf/application.conf` and point it to a MongoDB installation, and execute

```
play run.
```

Then put the [CodePoint Open CSV](https://www.ordnancesurvey.co.uk/opendatadownload/products.html) (scroll down, halfway down, 20mb)
files in the codepointopen directory.

After they are processed, they will be moved to the `codepointopen/done` directory.

Then go to http://localhost:9000/ and you should see the welcome screen.

JSON
----

Access http://localhost:9000/latlng/POSTCODE to geocode a postcode. Response will be JSON.

Technology
----------

* Play Framework 2.0, thank god for this!
* Apache Camel to read and monitor the directory and to tell the actors about the postcodes (body split)
* Akka provides a nice concurrent model to process the 1.7 million postcodes in under one minute on modern hardware
* GeoTools converts the eastings/northings to latitude/longitude
* Guice for Dependency Injection (not too much to inject yet though)
* MongoDB and Morphia as storage and ORM
* Twitter Bootstrap and Font Awesome for the UI

This was developed in a single day, as a proof of concept. Do whatever you like with it. Free for commercial use.

Screenshots
-----------

![Welcome Page](https://github.com/analytically/camelcode/raw/master/screenshot.png)