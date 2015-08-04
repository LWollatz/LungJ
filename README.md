# LungJ README #

### What is this repository for? ###

* providing a Plug-In for Fiji / ImageJ for segmenting micro-CT scans of Lung Tissues
* current version: 0.3.0
* last stable version: 0.3
* [Learn Markdown](https://bitbucket.org/tutorials/markdowndemo)

### Requirements ###

* ImageJ 1.49s or later
* Java 1.8.0 (This has to be the Java used by ImageJ! Check under Help>About ImageJ)
* Trainable Weka Segmentation v2.2.1 (any other version will not work)
* Fiji (advised as it provides many extra tools including the Trainable Weka Segmentation)
* Windows 64-bit (advised as not tested for other platforms)

### How do I get set up? ###

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

### Contribution guidelines ###

* Writing tests
* Code review
* Other guidelines

### Who do I talk to? ###

* Repo owner: Lasse Wollatz (l.wollatz@soton.ac.uk)
* Other community or team contact