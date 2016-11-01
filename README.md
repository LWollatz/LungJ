[![DOI](https://img.shields.io/badge/doi-10.5258%2FSOTON%2F401280-blue.svg)](https://doi.org/10.5258/SOTON/401280)
# LungJ README #

### What is this repository for? ###

* providing a Plug-In for Fiji / ImageJ for segmenting images, too large to process in RAM at once as well as several other tools for image segmentation & processing.
* current version: 0.5.0
* last stable version: 0.3.0

### Requirements ###

* ImageJ 1.49s or later
* Trainable Weka Segmentation Plugin
* Flood Fill (3D) Plugin
* 3D Viewer Plugin
* FeatureJ (http://imagej.net/FeatureJ)
* Fiji (advised as it provides many extra tools including the Trainable Weka Segmentation and Flood Fill (3D))

### How do I get set up? ###

**Automatic**

* Add http://sites.imagej.net/LungJ/ to your download pages

**Easy**

* The main directory includes the latest build
* copy the .jar file into the Fiji ./*plugins*/*LungJ* directory
* restart Fiji

**Detailed**

* download LungJ_.jar from the main directory
* place LungJ_.jar file into the Fiji ./*plugins*/*LungJ* directory of your Fiji installation
* if there are problems, try creating a subfolder so that the file is placed in *./plugins/LungJ_/LungJ_.jar*
* restart Fiji
* the new functions should be available under Plugins>LungJ>

**For Developers**

* Java sourcecode is in the *src* folder
* Fiji configuration file (for menu creation) is under *config*
* run javac (comes with Fiji) on the *src* folder to create compiled java classes in the *class* folder
* run jar (comes with Java JDK) to combine the classes and config files into a .jar
* copy the .jar file into the Fiji *./plugins* directory
* restart Fiji

### Who do I talk to? ###

* Repo owner: Lasse Wollatz (l.wollatz@soton.ac.uk)
* More info: http://imagej.net/LungJ
