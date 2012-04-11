CamelCode - Geocode UK postcode addresses
=========================================

Prerequisites: MongoDB and Play Framework 2.0.

Setup
-----

Edit conf/application.conf and point it to a MongoDB installation, and execute

'''
play run.
'''

Then put the [CodePoint Open CSV](http://data.gov.uk/dataset/os-code-point-open) files in the codepointopen directory.
After they are processed, they will be moved to the codepointopen/done directory.

Screenshots
-----------

![Welcome Page](https://github.com/analytically/camelcode/raw/master/screenshot.png)