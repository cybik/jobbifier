jobbifier-cybik
=========

Java GUI app for creating OBB files. Based on the [Android jobb tool](http://developer.android.com/tools/help/jobb.html)

This repository freely copies the code from monkey0506/jobbifier and improves upon it in the following ways:

* Gets rid of extraneous build configurations
    * No .idea at the root of the repo for a cleaner local import and for cleaner CI
    * .gitignores unified into the root.
* Offers a command-line interface for easy CI, while maintaining the UI if the user so chooses
    * The command line is easier to understand and simpler than the upstream jobb, 当然.
* jObbifier has been renamed from app
    * jObbifier also uses shadow to create a fatjar easily
* This repository offers its users and forkers a Travis configuration if they so desire

jObbifier
---------
Main project files for GUI app. Slightly altered from monkey0506's code to allow for fun stuffs.

jobb
----
Modified Android jobb tool source. Contains bugfix for [Issue 220717](https://code.google.com/p/android/issues/detail?id=220717)
~~[Issue 60294](https://code.google.com/p/android/issues/detail?id=60294)~~.

libfat32
--------
Modified FAT library source. Contains bugfix for [Issue 220717](https://code.google.com/p/android/issues/detail?id=220717)
~~[Issue 60294](https://code.google.com/p/android/issues/detail?id=60294)~~.
