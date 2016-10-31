import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.*;
import ij.plugin.frame.Recorder;

import java.awt.*;
import java.awt.event.*;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import lj.LJPrefs;

//import trainableSegmentation.*;
//import weka.core.Utils;


/*** Apply_Weka_Classifier
 * applies a Weka classifier based on the filename of an image and
 * the filename of a classifier model. This implements the whole
 * application process in a single function and does not require
 * loading the original image into memory twice. This function is
 * likely to suffer from updates to the weka-segmentation plug-in but
 * will hopefully be directly implemented by the trainable
 * segmentation in a future version.
 * 
 * - Pre-process the image of interest according to your needs.
 *   Samples must have the correct resolution (bit-size) and value 
 *   range for the classifier you want to use. There is no need to
 *   apply a noise filter, as the Weka segmentation does this for 
 *   you.
 * - Select the location of the image you want to segment. It is not
 *   advised to load the image in ImageJ prior to calling this 
 *   function, as that requires extra memory.
 * - Select a classifier model from your files. LungJ offers some
 *   sample classifiers which currently have to be downloaded 
 *   separately from the java plug in.
 * - Choose the class you want to extract. Each classifier has at 
 *   least 2 classes (foreground and background). Future versions are
 *   planned to allow the choice of several classes.
 * - Press `OK’ and the Weka segmentation will be started, the image
 *   and the classifier loaded and eventually the classifier will be 
 *   applied. This process can take a long time and the progress bar 
 *   updates by the Trainable Weka Segmentation will sometimes 
 *   suggest that it has finished, even-though it is still working. 
 *   Apply_Weka_Classifier will close the Trainable Weka Segmentation
 *   window, once the segmentation has completed.
 *
 * @author Lasse Wollatz
 * 
 * @see    <a href="http://imagej.net/Trainable_Weka_Segmentation">Trainable Weka Segmentation</a>
 ***/
public class Apply_Weka_Classifier implements PlugIn, ActionListener{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	/** Version of the Trainable WEKA Segmentation **/
	public static final String PLUGIN_TWS = LJPrefs.PLUGIN_TWS_VERSION;
	/** Version of the Trainable WEKA Segmentation as included in the window name **/
	public static String TWS_version = "v" + PLUGIN_TWS; //as this is included in the window name...

	private static String LJ_srcDirectory = LJPrefs.LJ_inpDirectory;
	private static String LJ_clsDirectory = LJPrefs.LJ_clsDirectory;
	private static int AC_channel = 1;

	JButton clsfilebtn;
	JTextField clsdirtxt;
	JButton srcfilebtn;
	JTextField srcdirtxt;
	GenericDialog gd;

	/***
	 * @see LJPrefs#retrieveOption
	 * @see LJPrefs#savePreferences
	 * @see Recorder#recordOption
	 * @see ij.gui.GenericDialog
	 ***/
	public void run(String command){
		if (IJ.versionLessThan("1.49s"))        
			return;
		IJ.showStatus("Initializing...");
		IJ.showProgress(0, 100);

		String arguments = "";
		if (IJ.isMacro() && Macro.getOptions() != null && !Macro.getOptions().trim().isEmpty()) { 
			/**running as macro**/
			arguments = Macro.getOptions().trim();
			IJ.log(arguments);
			IJ.log("^- arguments\n");
			AC_channel = 6;

			LJ_srcDirectory = LJPrefs.retrieveOption(arguments, "filepath", LJ_srcDirectory);
			LJ_clsDirectory = LJPrefs.retrieveOption(arguments, "classifier", LJ_clsDirectory);
			AC_channel = LJPrefs.retrieveOption(arguments, "class", 1);

			IJ.log("filepath: "+LJ_srcDirectory);
			IJ.log("classifier: "+LJ_clsDirectory);
			IJ.log("channel: "+AC_channel);


		}else{
			/** create dialog to request values from user: **/
			/**  running as GUI  **/
			GenericDialog gd = new GenericDialog(command+" Apply Weka Classifier");
			Font gdFont = gd.getFont();
			/**  Image File Textbox  **/
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
			/**  Classifier Model File Textbox  **/
			JLabel clsdirlbl = new JLabel ("Classifier Directory  ", JLabel.RIGHT);
			clsdirlbl.setFont(gdFont);
			clsdirtxt = new JTextField(LJ_clsDirectory,50);
			clsdirtxt.setFont(gdFont);
			clsfilebtn = new JButton("...");
			clsfilebtn.addActionListener(this);
			Panel clspanel = new Panel();
			clspanel.add(clsdirlbl,-1);
			clspanel.add(clsdirtxt,-1);
			clspanel.add(clsfilebtn,-1);
			gd.addPanel(clspanel);
			/**  Class Number Textbox  **/
			//TODO: remove and reorder block to be ready for 4D visualisation instead.
			gd.addNumericField("Class No", 1, 0);
			IJ.showStatus("Waiting for user input...");
			if (IJ.getVersion().compareTo("1.42p")>=0)
				gd.addHelp(PLUGIN_HELP_URL);
			gd.showDialog();
			if (gd.wasCanceled()){
				IJ.showStatus("Plug-In Aborted...");
				IJ.showProgress(100, 100);
				return;
			}
			IJ.showStatus("Initializing...");
			//TODO: recorder needs to get escape characters correctly! i.e. "\\\\"->\\ not "\\"->\
			/**  get values from dialog  **/
			LJ_srcDirectory = srcdirtxt.getText();
			LJ_clsDirectory = clsdirtxt.getText();
			AC_channel = (int)gd.getNextNumber();
			/** values from user dialog extracted **/

			/** record macro **/
			Recorder.recordOption("filepath", LJ_srcDirectory);
			Recorder.recordOption("classifier", LJ_clsDirectory);

			/** save preferences for after Fiji restart: **/
			LJPrefs.LJ_srcDirectory = LJ_srcDirectory;
			LJPrefs.LJ_clsDirectory = LJ_clsDirectory;
			LJPrefs.savePreferences();
		}

		File file = new File(LJ_srcDirectory);
		//String LJ_srcPath = file.getParent();
		String LJ_srcFile = file.getName();

		/***Load WEKA***/
		IJ.showStatus("Opening Trainable Weka Segmentation...");
		//TODO: import library to make direct calls
		//IJ.run("Trainable Weka Segmentation", "open=["+LJ_srcDirectory+"\\"+LJ_srcFilename+"]");
		//IJ.run("Trainable Weka Segmentation", "open=["+LJ_srcDirectory+"\\"+LJ_srcFilename+"] inputfile=["+LJ_srcDirectory+"\\"+LJ_srcFilename+"] path=["+LJ_srcDirectory+"]");
		//IJ.log("open=["+LJ_srcDirectory+"] inputfile=["+LJ_srcDirectory+"] path=["+LJ_srcPath+"]");
		//IJ.run("Trainable Weka Segmentation", "open=["+LJ_srcDirectory+"] inputfile=["+LJ_srcDirectory+"] path=["+LJ_srcPath+"]");
		IJ.runMacro("open('"+LJ_srcDirectory.replace("\\", "\\\\")+"');");
		//IJ.openImage();
		IJ.run("Trainable Weka Segmentation");
		IJ.runMacro("close('"+LJ_srcFile+"');");

		int Nimg = WindowManager.getImageCount();
		String[] lstImages = WindowManager.getImageTitles();
		//int[] lstImageIds = WindowManager.getIDList();
		String tempStr = "";
		for (int i = 0; i < Nimg; i++){
			if (lstImages[i].startsWith("Trainable Weka Segmentation")){
				tempStr = lstImages[i].replace("Trainable Weka Segmentation ", "");
				if(!(TWS_version.equals(tempStr))){
					IJ.log("Newer version of the Trainable WEKA Segmentation found (found "+tempStr+", expected "+TWS_version+"). There might be compatibility issues...");
					TWS_version = tempStr;
				}
			}
		}

		IJ.showProgress(5, 100);
		/***Load Classifier***/
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
		/***Get Probability Map***/
		IJ.showStatus("Getting Probability Map...");
		//call("trainableSegmentation.Weka_Segmentation.applyClassifier", LJ_srcDirectory, LJ_srcFilename, "showResults=true", "storeResults=false", "probabilityMaps=true", "");
		IJ.runMacro("call('trainableSegmentation.Weka_Segmentation.getProbability');");
		IJ.showProgress(90, 100);
		/***post processing***/
		IJ.showStatus("Finishing...");
		IJ.runMacro("selectWindow('Trainable Weka Segmentation "+TWS_version+"'); close(); selectWindow('Probability maps');");
		/***remove background channel***/ 
		//TODO: NEED TO MAKE THIS INDEPENDANT OF IMAGE SIZE!
		ImagePlus image = WindowManager.getCurrentImage();
		int[] properties = image.getDimensions(); //width, height, nChannels, nSlices, nFrames
		int nSlices = properties[3];
		int nChannels = 2; //how do I obtain this?
		AC_channel = (AC_channel < 0) ? 0 : AC_channel;
		AC_channel = (AC_channel > nChannels) ? nChannels : AC_channel;
		IJ.run("Slice Remover", "first="+AC_channel+" last="+nSlices+" increment="+nChannels);
		IJ.showStatus("Probability map created.");
		IJ.showProgress(100, 100);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/** code to display file selection dialogs and store chosen result back into textbox**/
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
