import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.plugin.*;
import ij.plugin.frame.Recorder;
import ij.gui.GenericDialog;
import ij.gui.DialogListener;

//import trainableSegmentation.*;
//import weka.core.Utils;










import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;


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


public class Apply_Weka_Classifier implements PlugIn, ActionListener{
	/** plugin's name */
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	public static final String VERSION = LJPrefs.VERSION;
	
	public static final String TWS_version = "v2.2.1";
	
	private static String LJ_srcDirectory = LJPrefs.LJ_srcDirectory;
	private static String LJ_srcFilename = "250x250x250x16bit.tif";
	private static String LJ_clsDirectory = LJPrefs.LJ_clsDirectory;
	private static String LJ_clsFilename = "background.model";
	private static int AC_channel = 1;
	
	JButton clsfilebtn;
	JTextField clsdirtxt;
	JButton srcfilebtn;
	JTextField srcdirtxt;
	GenericDialog gd;
	
	public void run(String command){
		if (IJ.versionLessThan("1.49s"))        // generates an error message for older versions
			return;
		IJ.showStatus("Initializing...");
		IJ.showProgress(0, 100);
		//selectImage(LJ_srcID);
		/*
		if (WindowManager.getImageCount() > 0){
			ImagePlus image = WindowManager.getCurrentImage();
			LJ_srcFilename = image.getTitle();
		}*/
		
		String arguments = "";
		if (IJ.isMacro() && Macro.getOptions() != null && !Macro.getOptions().trim().isEmpty()) { 
			IJ.log("macro running\n");
			arguments = Macro.getOptions().trim();
			IJ.log(arguments);
			IJ.log("^- arguments\n");
		//}
		
		//if (IJ.isMacro()){
			
			//if (WindowManager.getImageCount() > 0){
			//	ImagePlus image = WindowManager.getCurrentImage();
			//	LJ_srcDirectory = image.getTitle();
			//}
			
			//LJ_srcDirectory = "test";
			//LJ_clsDirectory = "test";
			AC_channel = 6;
			
			LJ_srcDirectory = LJPrefs.retrieveOption(arguments, "filepath", LJ_srcDirectory);
			LJ_clsDirectory = LJPrefs.retrieveOption(arguments, "classifier", LJ_clsDirectory);
			AC_channel = LJPrefs.retrieveOption(arguments, "class", 1);
			
			IJ.log("filepath: "+LJ_srcDirectory);
			IJ.log("classifier: "+LJ_clsDirectory);
			IJ.log("channel: "+AC_channel);
			
			
		}else{
			IJ.log("no macro running\n");
			GenericDialog gd = new GenericDialog(command+" Apply Weka Classifier");
			//gd.addString("Threshold", LJ_srcDirectory, 3);
			Font gdFont = gd.getFont();
			
			JLabel srcdirlbl = new JLabel ("Image Filepath  ", JLabel.RIGHT);
			srcdirlbl.setFont(gdFont);
			srcdirtxt = new JTextField(LJ_srcDirectory,50);
			srcdirtxt.setFont(gdFont);
			srcfilebtn = new JButton("...");
			srcfilebtn.addActionListener(this);
			Panel srcpanel = new Panel();
			gd.setInsets(10, 10, 0);
			srcpanel.add(srcdirlbl,-1);
			srcpanel.add(srcdirtxt,-1);
			srcpanel.add(srcfilebtn,-1);
			gd.addPanel(srcpanel);
			//gd.addStringField("Directory", LJ_srcDirectory, 100);
			//gd.addStringField("Filename", LJ_srcFilename, 100);
			JLabel clsdirlbl = new JLabel ("Classifier Directory  ", JLabel.RIGHT);
			clsdirlbl.setFont(gdFont);
			clsdirtxt = new JTextField(LJ_clsDirectory,50);
			clsdirtxt.setFont(gdFont);
			clsfilebtn = new JButton("...");
			clsfilebtn.addActionListener(this);
			Panel clspanel = new Panel();
			//gd.setInsets(60, 10, 10);
			clspanel.add(clsdirlbl,-1);
			clspanel.add(clsdirtxt,-1);
			clspanel.add(clsfilebtn,-1);
			gd.addPanel(clspanel);
			//gd.addStringField("Classifier_Directory", LJ_clsDirectory, 100);
			//gd.addStringField("Classifier_Filename", LJ_clsFilename, 100);
			gd.addNumericField("Class No", 1, 0);
			IJ.showStatus("Waiting for user input...");
			gd.showDialog();
			if (gd.wasCanceled()){
				IJ.showStatus("Plug-In Aborted...");
				IJ.showProgress(100, 100);
	        	return;
	        }
			IJ.showStatus("Initializing...");
			
			//TODO: recorder needs to get escape characters correctly! i.e. "\\\\"->\\ not "\\"->\
			
			//LJ_srcDirectory = gd.getNextString();
			LJ_srcDirectory = srcdirtxt.getText();
			Recorder.recordOption("filepath", LJ_srcDirectory);
			LJPrefs.LJ_srcDirectory = LJ_srcDirectory;
			//LJ_srcFilename = gd.getNextString();
			//LJ_clsDirectory = gd.getNextString();
			LJ_clsDirectory = clsdirtxt.getText();
			Recorder.recordOption("classifier", LJ_clsDirectory);
			LJPrefs.LJ_clsDirectory = LJ_clsDirectory;
			//LJ_clsFilename = gd.getNextString();
			AC_channel = (int)gd.getNextNumber();
			LJPrefs.savePreferences();
		}
		
		IJ.log(LJ_srcDirectory);
		IJ.log(LJ_clsDirectory);
		File file = new File(LJ_srcDirectory);
		String LJ_srcPath = file.getParent();
		String LJ_srcFile = file.getName();
		
		IJ.log(LJ_srcPath);
		
		//IJ.openImage();
		
		//--Load WEKA--
		IJ.showStatus("Opening Trainable Weka Segmentation...");
		//TODO: import library to make direct calls
		//IJ.run("Trainable Weka Segmentation", "open=["+LJ_srcDirectory+"\\"+LJ_srcFilename+"]");
		//IJ.run("Trainable Weka Segmentation", "open=["+LJ_srcDirectory+"\\"+LJ_srcFilename+"] inputfile=["+LJ_srcDirectory+"\\"+LJ_srcFilename+"] path=["+LJ_srcDirectory+"]");
		IJ.log("open=["+LJ_srcDirectory+"] inputfile=["+LJ_srcDirectory+"] path=["+LJ_srcPath+"]");
		//IJ.run("Trainable Weka Segmentation", "open=["+LJ_srcDirectory+"] inputfile=["+LJ_srcDirectory+"] path=["+LJ_srcPath+"]");
		IJ.runMacro("open('"+LJ_srcDirectory.replace("\\", "\\\\")+"');");
		IJ.run("Trainable Weka Segmentation");
		IJ.runMacro("close('"+LJ_srcFile+"');");
		
		IJ.showProgress(5, 100);
		//--Load Classifier--
		IJ.showStatus("Loading classifier...");
		//IJ.runMacro("selectWindow('Trainable Weka Segmentation "+TWS_version+"'); call('trainableSegmentation.Weka_Segmentation.loadClassifier', '"+LJ_clsDirectory.replace("\\","\\\\")+"\\\\"+LJ_clsFilename.replace("\\","\\\\")+"');");
		IJ.runMacro("selectWindow('Trainable Weka Segmentation "+TWS_version+"'); call('trainableSegmentation.Weka_Segmentation.loadClassifier', '"+LJ_clsDirectory.replace("\\","\\\\")+"');");
		if (WindowManager.getNonImageTitles().length <= 0){
			IJ.log(Integer.toString(WindowManager.getNonImageTitles().length));
			IJ.log("failed to initialize Trainable Weka Segmentation");
			IJ.showProgress(100, 100);
			return;
		}
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

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == this.clsfilebtn ){
			JFileChooser chooser = new JFileChooser(LJ_clsDirectory);
			FileFilter filter = new FileNameExtensionFilter("Classifier Model", "model");
			chooser.addChoosableFileFilter(filter);
			chooser.setFileSelectionMode(0);
			chooser.setSelectedFile(new File(this.clsdirtxt.getText()));
			int returnVal = chooser.showDialog(gd,"Choose Classifier");
			if(returnVal == JFileChooser.APPROVE_OPTION)
	        {
				this.clsdirtxt.setText(chooser.getSelectedFile().getAbsolutePath());
	        }
        }
		if(e.getSource() == this.srcfilebtn ){
			JFileChooser chooser = new JFileChooser(LJ_srcDirectory);
			FileFilter tiffilter = new FileNameExtensionFilter("tif images (*.tif, *.tiff)", "tif", "tiff");
			FileFilter rawfilter = new FileNameExtensionFilter("raw data (*.raw)", "raw");
			chooser.addChoosableFileFilter(tiffilter);
			chooser.addChoosableFileFilter(rawfilter);
			chooser.setFileSelectionMode(0);
			chooser.setSelectedFile(new File(this.srcdirtxt.getText()));
			int returnVal = chooser.showDialog(gd,"Choose Image");
			if(returnVal == JFileChooser.APPROVE_OPTION)
	        {
				this.srcdirtxt.setText(chooser.getSelectedFile().getPath());
	        }
        }
	}
}
