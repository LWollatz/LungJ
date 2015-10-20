


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import ij.ImagePlus;
import ij.ImageStack;
import ij.Prefs;
import ij.process.ImageProcessor;
import ij.util.Tools;
import ij.plugin.frame.Recorder;

import java.awt.Color;


public class LJPrefs{
	//TODO: clean this mess up!
	//TODO: remove unneeded tracking data
	//TODO: add path to relevant ImageJ files and then remove commented code
	public static final String PLUGIN_NAME = "LungJ";
	public static final String VERSION = "0.3.1b";
	/** file.separator system property */
	public static String separator = System.getProperty("file.separator");
	public static String LJ_dir =  System.getProperty("user.dir") + separator + "plugins" + separator + "LungJ";//getDirectory("imagej") + "/plugins/LungJ";
	public static boolean LJ_isNew = true;
	
	public static String LJ_srcDirectory = LJ_dir;
	public static String LJ_srcFilename = "";
	public static String LJ_clsDirectory = Prefs.get("LJ.CLASSIFIER_DIR", LJ_dir);
	public static String LJ_clsFilename = "";
	public static String LJ_clsName = "";
	public static String LJ_prbFilename = "";
	public static String LJ_mapFilename = "";
	public static String LJ_segFilename = "";
	public static String LJ_inpDirectory = Prefs.get("LJ.INPUT_DIR", LJ_dir);
	public static String LJ_outDirectory = Prefs.get("LJ.OUTPUT_DIR", LJ_dir);
	/*
	public static int LJ_srcID = 1;
	public static int LJ_prbID = 1;
	public static int LJ_mapID = 1;
	public static int LJ_segID = 1;
	*/
	
	public static boolean LJ_makeMap = Prefs.get("LJ.MAKEMAP", true);
	public static boolean LJ_makeMask = Prefs.get("LJ.MAKEMASK", true);
	public static double LJ_threshold = Prefs.get("LJ.THRESHOLD", 0.67);
	
	
	public static String LJ_segname1 = Prefs.get("LJ.SEGNAME1", "Tissue");
	public static String LJ_segname2 = Prefs.get("LJ.SEGNAME2", "Fibre");
	public static String LJ_segname3 = Prefs.get("LJ.SEGNAME3", "Vessels");
	public static String LJ_segname4 = Prefs.get("LJ.SEGNAME4", "Airways");
	public static String LJ_segname5 = Prefs.get("LJ.SEGNAME5", "Bloodvessels");
	
	
	public static Color  LJ_bgColor =  getPref("LJ.BGCOLOR", new Color(0, 0, 0));     		//background
	public static Color  LJ_Color1 =   getPref("LJ.COLOR1", new Color(255, 255, 255));		//Tissue
	public static Color  LJ_Color2 =   getPref("LJ.COLOR2", new Color(0, 153, 153));  		//Fibre
	public static Color  LJ_Color3 =   getPref("LJ.COLOR3", new Color(255, 102, 102));		//Vessels
	public static Color  LJ_Color4 =   getPref("LJ.COLOR4", new Color(0, 0, 204));    		//Airways
	public static Color  LJ_Color5 =   getPref("LJ.COLOR5", new Color(204, 0, 0));    		//Bloodvessels
	public static Color[] LJ_Colors = {LJ_bgColor, LJ_Color1, LJ_Color2, LJ_Color3, LJ_Color4};
	//public static Color[] LJColors = new Color[5];
	
	/*
	public static double LJ_th1 =      Prefs.get("LJ.THRESHOLD1", 0.8);                  	//Tissue
	public static double LJ_th2 =      Prefs.get("LJ.THRESHOLD2",0.5);   					//Fibre
	public static double LJ_th3 = 	   Prefs.get("LJ.THRESHOLD3",0.3); 	 					//Vessels
	public static double LJ_th4 = 	   Prefs.get("LJ.THRESHOLD4",0.3);   				  	//Airways
	public static double LJ_th5 = 	   Prefs.get("LJ.THRESHOLD5",0.3);   				  	//Bloodvessels
	*/
	
	public static int LJ_win_Top = 20;
	public static int LJ_win_Left = 20;
	public static int LJ_win_Height = 20;
	public static int LJ_win_Width = 20;
	public static int LJ_win_Spacing = 60;
	//public static double LJ_Threshold = 0.2;
	
		
	//public static String[] LJ_classifiers = {"None"};
	public static List<String> LJ_classifiers = new ArrayList<String>();
	
	/*ImageJ standards*/
	//Font gdFont = new Font ("Garamond", style , 11);
	
	
	

/**** LungJ PREFERENCES ****/
	
	/**
	 * saves all of LungJs preferences with ImageJs global preference saver
	 */
	public static void savePreferences() {
		Prefs.set("LJ.CLASSIFIER_DIR", LJ_clsDirectory);
		Prefs.set("LJ.INPUT_DIR", LJ_inpDirectory);
		Prefs.set("LJ.OUTPUT_DIR", LJ_outDirectory);

		Prefs.set("LJ.BGCOLOR", Tools.c2hex(LJ_Colors[0]));
		Prefs.set("LJ.COLOR1", Tools.c2hex(LJ_Colors[1]));
		Prefs.set("LJ.COLOR2", Tools.c2hex(LJ_Colors[2]));
		Prefs.set("LJ.COLOR3", Tools.c2hex(LJ_Colors[3]));
		Prefs.set("LJ.COLOR4", Tools.c2hex(LJ_Colors[4]));
		Prefs.set("LJ.COLOR5", Tools.c2hex(LJ_Color5));
		
		Prefs.set("LJ.MAKEMAP", LJ_makeMap);
		Prefs.set("LJ.MAKEMASK", LJ_makeMask);
		Prefs.set("LJ.THRESHOLD", LJ_threshold);
		
		Prefs.savePreferences();
	}
	
	
	
	/**
	 * 
	 * @param key:     String containing the key of the value needed
	 * @param Default: Default String
	 * @return Color:  - Color specified by key in ImageJ preferences or
	 *                 - Color specified by Default if key is not found in ImageJ preferences or does not encode a color
	 */
	public static Color getPref(String key, Color defaultValue) {
		String s = Prefs.get(key, "");
		Color c = null;
		if (s!=null && s!= "") {
			try {c = Color.decode(s);}
			catch (NumberFormatException e) {c = null;}
			if (c!=null)
				return(c);
		}
		return defaultValue;
	}
	
	
	
	
	
/**** WORKING WITH PROPERTIES OBJECTS ****/
	
/*** GETTING VALUES ***/
	
	/** Uses the keyword <code>key</code> to retrieve a string from the
	Properties object. Returns <code>defaultValue</code> if the key
	is not found. */
	public static String getPref(Properties prefs, String key, String defaultValue) {
		String value = prefs.getProperty(key);
		if (value == null)
			return defaultValue;
		else
			return value;
	}

	/** Uses the keyword <code>key</code> to retrieve an integer from the
	Properties object. Returns <code>defaultValue</code> if the key
	is not found. */
	public static int getPref(Properties prefs, String key, int defaultValue) {
		String s = prefs.getProperty(key);
		Double d = null;
		if (s!=null) {
			try {d = new Double(s);}
			catch (NumberFormatException e) {d = null;}
			if (d!=null)
				return(d.intValue());
		}
		return defaultValue;
	}
	
	/** Uses the keyword <code>key</code> to retrieve a number from the
	Properties object. Returns <code>defaultValue</code> if the key
	is not found. */
	public static double getPref(Properties prefs, String key, double defaultValue) {
		String s = prefs.getProperty(key);
		Double d = null;
		if (s!=null) {
			try {d = new Double(s);}
			catch (NumberFormatException e) {d = null;}
			if (d!=null)
				return(d.doubleValue());
		}
		return defaultValue;
	}

	/** Uses the keyword <code>key</code> to retrieve a boolean from
	the Properties object. Returns <code>defaultValue</code> if
	the key is not found. */
	public static boolean getPref(Properties prefs, String key, boolean defaultValue) {
		String value = prefs.getProperty(key);
		if (value==null)
			return defaultValue;
		else
			return value.equals("true");
	}
	
	/** Uses the keyword <code>key</code> to retrieve a colour from
	the Properties object. Returns <code>defaultValue</code> if
	the key is not found. */
	public static Color getPref(Properties prefs, String key, Color defaultValue) {
		String s = prefs.getProperty(key);
		Color c = null;
		if (s!=null) {
			try {c = Color.decode(s);}
			catch (NumberFormatException e) {c = null;}
			if (c!=null)
				return(c);
		}
		return defaultValue;
	}

	
/*** FILE I/O ***/
	
	/**
	 * 
	 * @param prefs:       Properties object (from java.util.*) to be written to a file
	 * @param filepath:    full file-path
	 * @throws IOException
	 */
	public static void writeProperties(Properties prefs, String filepath) throws IOException{
		FileOutputStream fos = new FileOutputStream(filepath);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		prefs.store(bos, "LungJ ImageInfo");
		bos.close();
	}
	
	/**
	 * 
	 * @param filepath:    full file-path of text file containing preferences.
	 * @return             Properties object (from java.util.*)
	 * @throws IOException
	 */
	public static Properties readProperties(String filepath) throws IOException{
		Properties prefs = new Properties();
		System.out.println(filepath);
		InputStream f = new FileInputStream(filepath);
		f = new BufferedInputStream(f);
		prefs.load(f);
		f.close();
		/*boolean ok =  loadPrefs(filepath);
		if (!ok) { // not found
			System.out.println("preferences not found");
			savePreferences();
		}*/
		return prefs;
	}
	
	
	
	
	/*
	public static void savePrefs(Properties prefs, String path) throws IOException{
		FileOutputStream fos = new FileOutputStream(path);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		prefs.store(bos, "LungJ "+LJ_version+" Preferences");
		bos.close();
	}
	
	public static void savePreferences() {
		String path = null;
		try {
			Properties prefs = new Properties();
			prefs.put("CLASSIFIER_DIR", LJ_clsDirectory);
			prefs.put("SEGNAME1", LJ_segname1);
			prefs.put("SEGNAME2", LJ_segname2);
			prefs.put("SEGNAME3", LJ_segname3);
			prefs.put("SEGNAME4", LJ_segname4);
			prefs.put("SEGNAME5", LJ_segname5);
			prefs.put("THRESHOLD1", Double.toString(LJ_th1));
			prefs.put("THRESHOLD2", Double.toString(LJ_th2));
			prefs.put("THRESHOLD3", Double.toString(LJ_th3));
			prefs.put("THRESHOLD4", Double.toString(LJ_th4));
			prefs.put("THRESHOLD5", Double.toString(LJ_th5));
			prefs.put("BGCOLOR", Tools.c2hex(LJ_bgColor));
			prefs.put("COLOR1", Tools.c2hex(LJ_Color1));
			prefs.put("COLOR2", Tools.c2hex(LJ_Color2));
			prefs.put("COLOR3", Tools.c2hex(LJ_Color3));
			prefs.put("COLOR4", Tools.c2hex(LJ_Color4));
			prefs.put("COLOR5", Tools.c2hex(LJ_Color5));
			
			/*
			if (threads>1) prefs.put(THREADS, Integer.toString(threads));
			if (IJ.isMacOSX()) useJFileChooser = false;
			saveOptions(prefs);
			savePluginPrefs(prefs);
			IJ.getInstance().savePreferences(prefs);
			Menus.savePreferences(prefs);
			ParticleAnalyzer.savePreferences(prefs);
			Analyzer.savePreferences(prefs);
			ImportDialog.savePreferences(prefs);
			PlotWindow.savePreferences(prefs);
			NewImage.savePreferences(prefs);*//*
			path = LJ_dir+separator+"preferences.txt";
			/*
			if (prefsDir.endsWith(".imagej")) {
				File f = new File(prefsDir);
				if (!f.exists()) f.mkdir(); // create .imagej directory
			}
			if (resetPreferences) {
				new File(path).delete();
				resetPreferences = false;
			} else
			*//*
			savePrefs(prefs, path);
		} catch (Throwable t) {
			String msg = t.getMessage();
			if (msg==null) msg = ""+t;
			int delay = 4000;
			try {
				new TextWindow("Error Saving Preferences:\n"+path, msg, 500, 200);
				IJ.wait(delay);
			} catch (Throwable t2) {}
		}
	}
	
	static void loadPreferences() {
		String path = LJ_dir+separator+"preferences.txt";
		System.out.println(path);
		boolean ok =  loadPrefs(path);
		if (!ok) { // not found
			System.out.println("preferences not found");
			savePreferences();
		}

	}

	static boolean loadPrefs(String path) {
		try {
			Properties ljPrefs = new Properties();
			InputStream is = new BufferedInputStream(new FileInputStream(path));
			ljPrefs.load(is);
			is.close();
			LJ_clsDirectory = getPref(ljPrefs,"CLASSIFIER_DIR",LJ_clsDirectory);
			LJ_segname1 = getPref(ljPrefs,"SEGNAME1",LJ_segname1);
			LJ_segname2 = getPref(ljPrefs,"SEGNAME2",LJ_segname2);
			LJ_segname3 = getPref(ljPrefs,"SEGNAME3",LJ_segname3);
			LJ_segname4 = getPref(ljPrefs,"SEGNAME4",LJ_segname4);
			LJ_segname5 = getPref(ljPrefs,"SEGNAME5",LJ_segname5);
			LJ_Color1 = getPref(ljPrefs,"COLOR1",LJ_Color1);
			LJ_Color2 = getPref(ljPrefs,"COLOR2",LJ_Color2);
			LJ_Color3 = getPref(ljPrefs,"COLOR3",LJ_Color3);
			LJ_Color4 = getPref(ljPrefs,"COLOR4",LJ_Color4);
			LJ_Color5 = getPref(ljPrefs,"COLOR5",LJ_Color5);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
	
	/** Uses the keyword <code>key</code> to retrieve a string from the
	preferences file. Returns <code>defaultValue</code> if the key
	is not found. *//*
public static String getPref(Properties ljPrefs, String key, String defaultValue) {
	String value = ljPrefs.getProperty(key);
	System.out.println(value);
	if (value == null)
		return defaultValue;
	else
		return value;
}

/** Uses the keyword <code>key</code> to retrieve a number from the
	preferences file. Returns <code>defaultValue</code> if the key
	is not found. *//*
public static double getPref(Properties ljPrefs, String key, double defaultValue) {
	String s = ljPrefs.getProperty(key);
	Double d = null;
	if (s!=null) {
		try {d = new Double(s);}
		catch (NumberFormatException e) {d = null;}
		if (d!=null)
			return(d.doubleValue());
	}
	return defaultValue;
}

/** Uses the keyword <code>key</code> to retrieve a color from the
preferences file. Returns <code>defaultValue</code> if the key
is not found. *//*
public static Color getPref(Properties ljPrefs, String key, Color defaultValue) {
String s = ljPrefs.getProperty(key);
Color c = null;
if (s!=null) {
	try {c = Color.decode(s);}
	catch (NumberFormatException e) {c = null;}
	if (c!=null)
		return(c);
}
return defaultValue;
}

/** Uses the keyword <code>key</code> to retrieve a boolean from
	the preferences file. Returns <code>defaultValue</code> if
	the key is not found. *//*
public static boolean getPref(Properties ljPrefs, String key, boolean defaultValue) {
	String value = ljPrefs.getProperty(key);
	if (value==null)
		return defaultValue;
	else
		return value.equals("true");
}*/
	
	
	
	
	
	
/**** MACRO RECORDING ****/
	
	/**
	 * tells ImageJs macro recorder to record a macro as
	 * run('command', 'key0=value0 key1=value1, ...');
	 * @param command  String representing the ImageJ command to run the plugin
	 * @param keys     String array containing the keys
	 * @param values   String array containing the values
	 */
	public static void recordRun(String command, String[] keys, String[] values){
	 String macrostr = "run(\"" + command + "\",\"";
	 for (int i=0; i<keys.length; i++){
		 macrostr += " "+keys[i] + "=["+values[i]+"]";
	 }
	 macrostr += "\");\n";
	 if(Recorder.record)
	   Recorder.recordString(macrostr);
	}
	
	
	/**
	 * 
	 * @param Options String returned by Macro.getOptions() (with ij.Macro)
	 * @param key     String containing the key of the value needed
	 * @param Default Default String
	 * @return String: - String specified by key in Options or
	 *                 - String specified by Default if key is not found in Options or does not encode a color
	 */
	public static int retrieveOption(String Options, String key, int Default){
		int a = Options.indexOf(" "+key+"=");
		if (a < 0){
			a = Options.indexOf(key+"=");
			if (a != 0){
				return Default;
			}
		}
		a = Options.indexOf("[",a);
		int b = Options.indexOf("]",a);
		String s = Options.substring(a+1, b);
		Double d = null;
		if (s!=null) {
			try {d = new Double(s);}
			catch (NumberFormatException e) {d = null;}
			if (d!=null)
				return(d.intValue());
		}
		return Default;
	}
	
	
	/**
	 * 
	 * @param Options String returned by Macro.getOptions() (with ij.Macro)
	 * @param key     String containing the key of the value needed
	 * @param Default Default String
	 * @return String - String specified by key in Options or
	 *                - String specified by Default if key is not found in Options or does not encode a color
	 */
	public static String retrieveOption(String Options, String key, String Default){
		int a = Options.indexOf(" "+key+"=");
		if (a < 0){
			a = Options.indexOf(key+"=");
			if (a != 0){
				return Default;
			}
		}
		a = Options.indexOf("[",a);
		int b = Options.indexOf("]",a);
		return Options.substring(a+1, b);
	}
	
	/**
	 * 
	 * @param Options String returned by Macro.getOptions() (with ij.Macro)
	 * @param key     String containing the key of the value needed
	 * @param Default Default Color
	 * @return Color  - colour specified by key in Options or
	 *                - colour specified by Default if key is not found in Options or does not encode a color
	 */
	public static Color retrieveOption(String Options, String key, Color Default) {
		int a = Options.indexOf(" "+key+"=");
		if (a < 0){
			a = Options.indexOf(key+"=");
			if (a != 0){
				return Default;
			}
		}
		a = Options.indexOf("[",a);
		int b = Options.indexOf("]",a);
		String s = Options.substring(a+1, b);
		System.out.println(s);
		Color c = null;
		if (s!=null && s!= "") {
			try {c = Color.decode(s);}
			catch (NumberFormatException e) {c = null;}
			if (c!=null)
				return(c);
		}
		return Default;
	}
	
	
/**** OTHER ****/
	
	/**
	 * read in all classifier files from the LJ_clsDirectory directory and stores them in LJ_classifiers.
	 */
	public static void loadClassifier(){
		LJ_classifiers.clear();
		LJ_classifiers.add("...NONE...");
		File file = new File(LJ_clsDirectory);
		File folder = file.getParentFile();
		File[] listOfFiles = folder.listFiles();
		System.out.println("Directory: " + LJ_clsDirectory);
		for (int i = 0; i < listOfFiles.length; i++) {
		   if (listOfFiles[i].isFile()) {
			   System.out.println("File: " + listOfFiles[i].getName());
			   if (listOfFiles[i].getName().endsWith(".model")){
				   System.out.println("Classifier: " + listOfFiles[i].getName());
				   LJ_classifiers.add(listOfFiles[i].getName());
			   }
		   }
		}
	}
	
	
/*** UTILITIES ***/
	
	/**
	 * display color choose dialog with default color and returns selected color.
	 * @param label Dialog Caption
	 * @param defaultCol default colour to return
	 * @return Color selected colour
	 */
	public static Color getColor(String label, Color defaultCol){
		Color selColor = JColorChooser.showDialog(null, label, defaultCol);
		System.out.println(selColor);
		return selColor;
	}
	
	
	//TODO: is this function ever used apart from the GUI???
	/**
	 * display file chooser dialog to choose WEKA classifier...
	 */
	public static String getClassifier(){
		FileFilter filter = new FileNameExtensionFilter("Weka Classifier", "model");
		JFileChooser chooser = new JFileChooser(LJPrefs.LJ_clsDirectory);
		chooser.addChoosableFileFilter(filter);
		chooser.showDialog(null, "Select");
		//String cls = chooser.getSelectedFile().getAbsolutePath();
		String cls = chooser.getSelectedFile().getParent();
		LJPrefs.LJ_clsDirectory = cls;
		LJPrefs.loadClassifier();
		return cls;
	}
	
	/**
	 * get minimum and maximum pixel value from a 3D image stack
	 * @param imp ImagePlus 3D Image
	 * @return float[2]: [0] minimum pixel value
	 *                   [1] maximum pixel value
	 */
	public static float[] getMinMax(ImagePlus imp) {
		/*
		 * float[] = getMinMax(ImagePlus imp)
		 * INPUT:
		 * - ImagePlus: 3D Image Stack
		 * OUTPUT:
		 * - float[2] with float[0] = minimum pixel value
		 *                 float[1] = maximum pixel value
		 */
		float max = -Float.MAX_VALUE;
		float min = Float.MAX_VALUE;
		ImageStack stack = imp.getStack();
		int width = imp.getWidth();
		int height = imp.getHeight();
		int n = width*height;
		int images = imp.getStackSize();
		if(imp.getBitDepth() == 24){
			/*RGB case*/
			for (int img=1; img<=images; img++) {
				ImageProcessor ip = stack.getProcessor(img);
				for (int y=0; y<height; y++){
		    		for (int x=0, p=x+y*width; x<width; x++,p++){
		    			int[] pixel = new int[3];
		    			ip.getPixel(x, y,pixel);
		    			//float v = ip.getf(i);
		    			if (pixel[0]>max) {
		    				max = pixel[0]; 
		    			}
		    			if (pixel[1]>max) {
		    				max = pixel[1]; 
		    			}
		    			if (pixel[2]>max) {
		    				max = pixel[2]; 
		    			}
		    			if (pixel[0]<min) {
		    				min = pixel[0];
		    			}
		    			if (pixel[1]<min) {
		    				min = pixel[1];
		    			}
		    			if (pixel[2]<min) {
		    				min = pixel[2];
		    			}
		    		}
				}
			}
		}else{
			for (int img=1; img<=images; img++) {
				ImageProcessor ip = stack.getProcessor(img);
				for (int i=0; i<n; i++) {
					float v = ip.getf(i);
					if (v>max) {
						max = v; 
					}
					if (v<min) {
						min = v;
					}
				}
			}
		}
		float[] mm = new float[2];
		mm[0]=min;
		mm[1]=max;
		return mm;
	}
	
}
