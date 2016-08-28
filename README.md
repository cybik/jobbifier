Yet Another F[riendly/unky] Obbifier Tool
=========

Java GUI app for creating OBB files. 

YAFOT (Yet Another F[riendly/unky] Obbification Tool) is based on these tools:

* googlejobb: The [Android jobb tool](http://developer.android.com/tools/help/jobb.html)
    * Direct source dependency (com.android.jobb)
* libfat32
    * https://github.com/waldheinz/fat32-lib
* yafot: Most code from monkey0506/jobbifier, with the following improvements:
    * Gets rid of extraneous build configurations
        * No .idea at the root of the repo for a cleaner local import and for cleaner CI
        * .gitignores unified into the root.
    * Offers a command-line interface for easy CI, while maintaining the UI if the user so chooses
        * The command line is easier to understand and simpler than the upstream jobb, 当然.
    * jObbifier has been renamed from app
        * jObbifier also uses shadow to create a fatjar easily
    * This repository offers its users and forkers a Travis configuration if they so desire


Directory Structure
===================
The project structure is arranged thusly:

googlejobb
----
Modified Android jobb tool source. Contains bugfix for [Issue 220717](https://code.google.com/p/android/issues/detail?id=220717)
~~[Issue 60294](https://code.google.com/p/android/issues/detail?id=60294)~~.

libfat32
--------
Modified FAT library source. Contains bugfix for [Issue 220717](https://code.google.com/p/android/issues/detail?id=220717)
~~[Issue 60294](https://code.google.com/p/android/issues/detail?id=60294)~~.

yafot
---------
Main project files for GUI app. Altered from monkey0506's code to allow for fun stuffs.
