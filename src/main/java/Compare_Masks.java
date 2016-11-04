import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;

import lj.LJPrefs;


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

/*** Compare_Masks
 * compares two binary masks by combining them into a single
 * colour-coded image. Agreed foreground is white and agreed
 * background black. Foreground detected as background is blue and
 * background detected as foreground red.
 * 
 * - As a preparation, make sure both masks are binary where the
 *   background is minimum and the foreground maximum.
 * - One image should represent the correct masks, while the other
 *   one should represent the test-mask
 * - Use Compare Masks providing the correct masks first and the
 *   test-mask second to combine the two and get a results table with
 *   the pixel statistics.
 * 
 * @author Lasse Wollatz  
 ***/
public class Compare_Masks implements PlugIn {
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = Compare_Masks.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	/** statistic values **/
	private double TP = 0;
	private double TN = 0;
	private double FP = 0;
	private double FN = 0;
	
	/* (non-Javadoc)
	 * @see ij.plugin.PlugIn#run(java.lang.String)
	 */
	public void run(String command){
		/** generate error message for older versions: **/
		if (IJ.versionLessThan("1.48n"))
			return;
		IJ.showStatus("Applying Mask...");
		
		/** get available images: **/
		int Nimg = WindowManager.getImageCount();
		if (Nimg < 2){
			IJ.error("At least two images are required - the correct mask and the mask to be tested.");
			return;
		}
		String[] lstImages = WindowManager.getImageTitles();
		int[] lstImageIds = WindowManager.getIDList();
		String[] lstMasks = new String[Nimg];
		int Nmasks = Nimg;
		for (int i = 0; i < Nimg; i++){
			ImageProcessor temp = WindowManager.getImage(lstImageIds[i]).getProcessor();
			if((temp.getMin() == 0 && temp.getMax() == 1) || temp.isBinary()){
				lstMasks[i] = lstImages[i];
			}else{
				lstMasks[i] = "--not a mask--";
				Nmasks--;
				IJ.log(String.valueOf(temp.getMin()) + " - " + String.valueOf(temp.getMax()));
			}
		}
		if (Nmasks < 2){
			IJ.error("Could not find two valid masks. (A mask needs to be binary, or only contain values between 0 and 1)");
			return;
		}
		
		/**create dialog to request values from user: **/
		GenericDialog gd = new GenericDialog(command+" Compare Mask");
		gd.addChoice("Actual Mask", lstMasks, lstMasks[0]);
		gd.addChoice("Detected Mask", lstMasks, lstMasks[0]);
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
        	return;
        }
		int AM_srcID = lstImageIds[gd.getNextChoiceIndex()];
		int AM_maskID = lstImageIds[gd.getNextChoiceIndex()];
		/** values from user dialog extracted **/
		
		/** load images **/
		ImagePlus image = WindowManager.getImage(AM_srcID);
		ImagePlus mask = WindowManager.getImage(AM_maskID);
		
		/** load image properties **/
		int tHeight = image.getHeight();
		int tWidth = image.getWidth();
		int tDepth = image.getNSlices();
		int tFrames = mask.getNFrames();
		int tChannels = mask.getNChannels();
		
		/** check if images are valid **/
		//TODO: NEED TO CHECK IF SAME SIZE...
		if (tChannels > 1){
			IJ.error("Can't handle multi-channel images.");
			return;
		}
		
		/** prepare output image **/
		ImagePlus imgout = IJ.createHyperStack("Result", tWidth, tHeight, 3, tDepth, tFrames, 8);
		
		/** process the images **/
		for (int z=1; z<=tDepth; z++){
			for (int f=1; f<=tFrames; f++){
				int index = mask.getStackIndex(1, z, f);
				int indexi = image.getStackIndex(1, z, f);
				int indexR = imgout.getStackIndex(1, z, f);
				int indexG = imgout.getStackIndex(2, z, f);
				int indexB = imgout.getStackIndex(3, z, f);
				
				ImageProcessor imageIP = image.getStack().getProcessor(indexi).convertToByte(true);
				ImageProcessor maskIP = mask.getStack().getProcessor(index).convertToByte(true);
				
				ImageProcessor outputIPR = imgout.getStack().getProcessor(indexR);
				ImageProcessor outputIPG = imgout.getStack().getProcessor(indexG);
				ImageProcessor outputIPB = imgout.getStack().getProcessor(indexB);
				
				byte[] mPixels = (byte[])maskIP.getPixels();
				byte[] iPixels = (byte[])imageIP.getPixels();
				byte[] oPixelsR = (byte[])outputIPR.getPixels();
				byte[] oPixelsG = (byte[])outputIPG.getPixels();
				byte[] oPixelsB = (byte[])outputIPB.getPixels();
				
				for (int y=0; y<tHeight; y++){
		    		for (int x=0, p=x+y*tWidth; x<tWidth; x++,p++){
		    			if (mPixels[p] == iPixels[p] && iPixels[p] == (byte)0){
		    				TN += 1;
							oPixelsR[p] = (byte)0;
							oPixelsG[p] = (byte)0;
							oPixelsB[p] = (byte)0;
		    			}else if (mPixels[p] == iPixels[p]){
		    				TP += 1;
							oPixelsR[p] = (byte)255;
							oPixelsG[p] = (byte)255;
							oPixelsB[p] = (byte)255;
		    			}else if (mPixels[p] < iPixels[p]){
		    				FP += 1;
							oPixelsR[p] = (byte)255;
							oPixelsG[p] = (byte)0;
							oPixelsB[p] = (byte)0;
		    			}else if (mPixels[p] > iPixels[p]){
		    				FN += 1;
							oPixelsR[p] = (byte)0;
							oPixelsG[p] = (byte)0;
							oPixelsB[p] = (byte)255;
		    			}
		    		}
				}
			}
		}
		
		/** create results table and do final calculations **/
		ResultsTable rt = Analyzer.getResultsTable();
		if (rt == null) {
		        rt = new ResultsTable();
		        Analyzer.setResultsTable(rt);
		}
		
		double po = (TP+TN)/(TN+TP+FP+FN);
		double pc = ((TP+FP)*(TP+FN)+(TN+FP)*(TN+FN))/((TN+TP+FP+FN)*(TN+TP+FP+FN));
		double kappa = (po-pc)/(1-pc);
		double S = (2*TP)/(2*TP+FP+FN);
		
		rt.incrementCounter();
		rt.addLabel("True");
		rt.addValue("Positive", TP);
		rt.addValue("Negative", TN);
		rt.addValue("Percentage", "");
		rt.incrementCounter();
		rt.addLabel("False");
		rt.addValue("Positive", FP);
		rt.addValue("Negative", FN);
		rt.addValue("Percentage", "");
		rt.incrementCounter();
		rt.addValue("Positive", "");
		rt.addValue("Negative", "");
		rt.addValue("Percentage", "");
		rt.incrementCounter();
		rt.addLabel("sensitivity");
		rt.addValue("Percentage", 100*TP/(TP+FN));
		rt.addValue("Positive", "");
		rt.addValue("Negative", "");
		rt.incrementCounter();
		rt.addLabel("specificity");
		rt.addValue("Percentage", 100*TN/(TN+FP));
		rt.addValue("Positive", "");
		rt.addValue("Negative", "");
		rt.incrementCounter();
		rt.addLabel("alpha");
		rt.addValue("Percentage", 100*FP/(TN+FP));
		rt.addValue("Positive", "");
		rt.addValue("Negative", "");
		rt.incrementCounter();
		rt.addLabel("beta");
		rt.addValue("Percentage", 100*FN/(TP+FN));
		rt.addValue("Positive", "");
		rt.addValue("Negative", "");
		rt.incrementCounter();
		rt.addLabel("accuracy");
		rt.addValue("Percentage", 100*(TP+TN)/(TN+TP+FP+FN));
		rt.addValue("Positive", "");
		rt.addValue("Negative", "");
		rt.incrementCounter();
		rt.addLabel("kappa");
		rt.addValue("Percentage", 100*kappa);
		rt.addValue("Positive", "");
		rt.addValue("Negative", "");
		rt.incrementCounter();
		rt.addLabel("similarity index");
		rt.addValue("Percentage", 100*S);
		rt.addValue("Positive", "");
		rt.addValue("Negative", "");
		
		rt.showRowNumbers(false);
		imgout.show();
		rt.show("Results");
		
		
		IJ.showStatus("Masks Compared.");
	}
}
