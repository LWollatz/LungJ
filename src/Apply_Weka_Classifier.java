import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.*;
import ij.gui.GenericDialog;
import ij.gui.DialogListener;

//import trainableSegmentation;
//import weka.core.Utils;

import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
* LungJ code for probability map creation
* run("Apply Weka Classifier", "directory=[C:\\mydirectory\\images] filename=imgname.end class=[C:\\mydirectory\\classifiers] class=classifiername.model class=1");
*
* directory is a string specifying the directory of the input image
* filename is a string specifying the filename of the input image
* class_directory is a string specifying the directory of the classifier
* class_filename is a string specifying the filename of the classifier
* output image is true binary (0 or 1) but remains the same bit-depth as the input image
* window-size and threshold are automatically adjusted and LUT set to grayscale
* this means that the output map clearly shows foreground and background and is ideal for mathematical operations!
*
* Code by Lasse Wollatz, version 2015-05-01
*/


public class Apply_Weka_Classifier implements PlugIn{
	/** plugin's name */
	public static final String PLUGIN_NAME = "LungJ";
	
	public static final String TWS_version = "v2.2.1";
	
	private static String LJ_srcDirectory = "J:\\Biomedical Imaging Unit\\Research\\Research temporary\\3D IfLS Lung Project\\temp\\20150324_IfLS_Segmentation\\tests";
	private static String LJ_srcFilename = "250x250x250x16bit.tif";
	private static String LJ_clsDirectory = "J:\\Biomedical Imaging Unit\\Research\\Research temporary\\3D IfLS Lung Project\\temp\\20150324_IfLS_Segmentation\\run02";
	private static String LJ_clsFilename = "background.model";
	private static int AC_channel = 1;
	
	public void run(String command){
		
		IJ.showStatus("Initializing...");
		IJ.showProgress(0, 100);
		//selectImage(LJ_srcID);
		/*
		if (WindowManager.getImageCount() > 0){
			ImagePlus image = WindowManager.getCurrentImage();
			LJ_srcFilename = image.getTitle();
		}*/
		
		GenericDialog gd = new GenericDialog(command+" Apply Weka Classifier");
		//gd.addString("Threshold", LJ_srcDirectory, 3);
		gd.addStringField("Directory", LJ_srcDirectory, 100);
		gd.addStringField("Filename", LJ_srcFilename, 100);
		gd.addStringField("Classifier_Directory", LJ_clsDirectory, 100);
		gd.addStringField("Classifier_Filename", LJ_clsFilename, 100);
		gd.addNumericField("Class No", 1, 0);
		gd.showDialog();
		if (gd.wasCanceled()){
        	return;
        }
		LJ_srcDirectory = gd.getNextString();
		LJ_srcFilename = gd.getNextString();
		LJ_clsDirectory = gd.getNextString();
		LJ_clsFilename = gd.getNextString();
		AC_channel = (int)gd.getNextNumber();
		//--Load WEKA--
		IJ.showStatus("Opening Trainable Weka Segmentation...");
		//TODO: import library to make direct calls
		IJ.run("Trainable Weka Segmentation", "open=["+LJ_srcDirectory+"\\"+LJ_srcFilename+"]");
		//IJ.run("Trainable Weka Segmentation", "open=["+LJ_srcDirectory+"\\"+LJ_srcFilename+"] inputfile=["+LJ_srcDirectory+"\\"+LJ_srcFilename+"] path=[Ljava.lang.String;@3f094d0a");
		IJ.showProgress(5, 100);
		//--Load Classifier--
		IJ.showStatus("Loading classifier...");
		IJ.runMacro("selectWindow('Trainable Weka Segmentation "+TWS_version+"'); call('trainableSegmentation.Weka_Segmentation.loadClassifier', '"+LJ_clsDirectory.replace("\\","\\\\")+"\\\\"+LJ_clsFilename.replace("\\","\\\\")+"');");
		IJ.showProgress(20, 100);
		//--Get Probability Map--
		IJ.showStatus("Getting Probability Map...");
		//call("trainableSegmentation.Weka_Segmentation.applyClassifier", LJ_srcDirectory, LJ_srcFilename, "showResults=true", "storeResults=false", "probabilityMaps=true", "");
		IJ.runMacro("call('trainableSegmentation.Weka_Segmentation.getProbability');");
		IJ.showProgress(90, 100);
		//--post processing--
		IJ.showStatus("Finishing...");
		IJ.runMacro("selectWindow('Trainable Weka Segmentation "+TWS_version+"'); close(); selectWindow('Probability maps');");
		//--remove background channel-- NEED TO MAKE THIS INDEPENDANT OF IMAGE SIZE!
		ImagePlus image = WindowManager.getCurrentImage();
		int[] properties = image.getDimensions(); //width, height, nChannels, nSlices, nFrames
		int nSlices = properties[3];
		int nChannels = 2; //how do I obtain this?
		AC_channel = (AC_channel < 0) ? 0 : AC_channel;
		AC_channel = (AC_channel > nChannels) ? nChannels : AC_channel;
		IJ.run("Slice Remover", "first="+AC_channel+" last="+nSlices+" increment="+nChannels);
		//IJ.showStatus("first="+AC_channel+" last="+nSlices+" increment="+nChannels);
		IJ.showStatus("Probability map created.");
		IJ.showProgress(100, 100);
	}
}
