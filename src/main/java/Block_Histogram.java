import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.HistogramWindow;
import ij.plugin.PlugIn;

import java.io.IOException;
import java.util.Properties;

import lj.LJPrefs;
import lj.process.VirtualBlockStatistics;

/** License Statement
 * Copyright 2016 Lasse Wollatz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

/*** Block_Histogram
 * creates the histogram of a 3D block image.
 * 
 * - Run the plug-in
 * - Provide a directory path containing 3D blocks with a LungJ
 *   property file available
 * - Choose the number of histogram bins, the range of the histogram
 *   and if the property file should be updated.
 * - Press OK.
 * - The blocks will be analysed and a histogram of the full block
 *   will be shown, just like the histogram of a normal image.
 * 
 * @author Lasse Wollatz  
 ***/
public class Block_Histogram implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	private static String BC_inDirectory = LJPrefs.LJ_outDirectory;
	private static int maxX = 1;
	private static int maxY = 1;
	private static int maxZ = 1;
	private static int stepX = 1;
	private static int stepY = 1;
	private static int stepZ = 1;
	private static int haloX = 0;
	private static int haloY = 0;
	private static int haloZ = 0;
	private static double globMax = 255;
	private static double globMin = 0;
	private boolean usePrefs = true;
	private boolean doUpdate = true;
	private int nBins = 256;
	
	/*** run
     * requests a directory and uses VirtualBlockStatistics to get a
     * histogram. It then displays the histogram and saves results if
     * requested.
     * 
     * @param  command        String 
     * 
     * @see    LJPrefs#readProperties
     * @see    LJPrefs#getPref
     * @see    LJPrefs#writeProperties
     * @see    lj.process.VirtualBlockStatistics
     * @see    ij.gui.GenericDialog
     * @see    ij.gui.HistogramWindow
     ***/
	public void run(String command){
		double min = globMin;
		double max = globMax;
		
		IJ.showStatus("Getting data...");
		IJ.showProgress(0, 100);
		
		/**create dialog to request values from user: **/
		GenericDialog gd = new GenericDialog(command+" Histogram of 3D Blocks");
		gd.addStringField("Input directory", BC_inDirectory, 100);
		gd.addNumericField("Bins", nBins, 4);
		gd.addCheckbox("Use pixel value range", usePrefs);
		gd.addMessage("or use:");
		gd.addNumericField("X_min", min, 4);
		gd.addNumericField("X_max", max, 4);
		gd.addCheckbox("Update Properties.txt", doUpdate);
		
		IJ.showStatus("Waiting for user input...");
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showStatus("Plug In cancelled...");
			IJ.showProgress(100, 100);
        	return;
        }
		
		/** getting user input **/
		IJ.showStatus("Getting data...");
		BC_inDirectory = gd.getNextString();
		nBins = (int) gd.getNextNumber();
		usePrefs = gd.getNextBoolean();
		min = gd.getNextNumber();
		max = gd.getNextNumber();
		doUpdate = gd.getNextBoolean();
		Properties prefs = new Properties();
		try {
			prefs = LJPrefs.readProperties(BC_inDirectory + "\\properties.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block if no LJinfo file found
			e.printStackTrace();
		}
		
		/** reading image information from preference file **/
		maxX = LJPrefs.getPref(prefs, "maxX", maxX);
		maxY = LJPrefs.getPref(prefs, "maxY", maxY);
		maxZ = LJPrefs.getPref(prefs, "maxZ", maxZ);
		stepX = LJPrefs.getPref(prefs, "stepX", stepX);
		stepY = LJPrefs.getPref(prefs, "stepY", stepY);
		stepZ = LJPrefs.getPref(prefs, "stepZ", stepZ);
		haloX = LJPrefs.getPref(prefs, "haloX", haloX);
		haloY = LJPrefs.getPref(prefs, "haloY", haloY);
		haloZ = LJPrefs.getPref(prefs, "haloZ", haloZ);
		globMin = (float)LJPrefs.getPref(prefs, "minVal", globMin);
		globMax = (float)LJPrefs.getPref(prefs, "maxVal", globMax);
		if (usePrefs){
			min = globMin;
			max = globMax;
		}
		
		/** update Status **/
		IJ.showStatus("Reading blocks...");
		IJ.showProgress(0, 100);
		
		/** generate statistics **/
		VirtualBlockStatistics stats = new VirtualBlockStatistics(BC_inDirectory,nBins,min,max);
		
		/** update the image property file **/
		if (doUpdate){
			prefs = new Properties();
			prefs.put("maxX", Double.toString(maxX));
			prefs.put("maxY", Double.toString(maxY));
			prefs.put("maxZ", Double.toString(maxZ));
			prefs.put("stepX", Double.toString(stepX));
			prefs.put("stepY", Double.toString(stepY));
			prefs.put("stepZ", Double.toString(stepZ));
			prefs.put("haloX", Double.toString(haloX));
			prefs.put("haloY", Double.toString(haloY));
			prefs.put("haloZ", Double.toString(haloZ));
			prefs.put("minVal", Double.toString(stats.min));
			prefs.put("maxVal", Double.toString(stats.max));
			prefs.put("meanVal", Double.toString(stats.mean));
			prefs.put("StdDev", Double.toString(stats.stdDev));
			try {
				LJPrefs.writeProperties(prefs, BC_inDirectory + "\\properties.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		/** load an image block as the Histogram needs to be attached to an image. **/
		int x = 0;
		int y = 0;
		int z = 0;
		String filein = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z,y,x);
		ImagePlus imp2 = IJ.openImage(filein);
		imp2.show();
		
		/** display the histogram **/
		new HistogramWindow("Histogram of "+BC_inDirectory, imp2, stats);
		
		/** finalise **/
		imp2.close();
		IJ.showStatus("Histogram created");
		IJ.showProgress(100, 100);
	}
}
