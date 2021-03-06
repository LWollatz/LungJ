import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.process.*;

import java.awt.*;

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

/*** Invert_Values
 * inverts the values in an image. 
 * <code>run("Invert Values","minimum=0 maximum=255");</code>
 * 
 * This code changes the actual values and mirrors them around the
 * centre between minimum and maximum. Values which exceed the
 * minimum or maximum specified are being cut of at the boundary. The
 * GUI looks up the minimum and maximum value in an image and 
 * suggests them as defaults.
 * 
 * @author Lasse Wollatz  
 ***/
public class Invert_Values implements ExtendedPlugInFilter, DialogListener {
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	private boolean previewing = false;
    private FloatProcessor previewEdm;
    private double globMin = 0;
    private double globMax = 1;
    /** defines what type of images this filter can be applied to **/
	private int flags = DOES_8G|DOES_16|DOES_32|DOES_STACKS|PARALLELIZE_STACKS|KEEP_PREVIEW;
	
	
	public int setup(String arg, ImagePlus imp) {
		//generate error message for older versions:
		if (IJ.versionLessThan("1.48n"))
			return DONE;
		if (arg.equals("about")) {
			showAbout();
			return DONE;
		}
		if (imp == null){
			return DONE;
		}
		return flags;
    }
	
	/*** showDialog ***
     * Called by ImageJ after setup.
     * 
     * @param  imp                 ImagePlus
     * @param  command             String
     * @param  pfr                 PlugInFilterRunner
     * @return                     int
     ***/
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        
    	
    	float[] minmax = new float[2];
    	
    	minmax = LJPrefs.getMinMax(imp);
    	globMin = (double)minmax[0];
    	globMax = (double)minmax[1];
    	
    	
    	
    	
        /** create dialog to request values from user: **/
        GenericDialog gd = new GenericDialog(command+"...");
        gd.addNumericField("minimum Value", globMin, 4);
        gd.addNumericField("maximum Value", globMax, 4);
        gd.addPreviewCheckbox(pfr);             // passing pfr makes the filter ready for preview
        if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
        gd.addDialogListener(this);             // the DialogItemChanged method will be called on user input
        previewing = true;
        gd.showDialog();                        // display the dialog; preview runs in the background now
        previewing = false;
        if (gd.wasCanceled()){
        	return DONE;
        }
        IJ.register(this.getClass());           // protect static class variables (filter parameters) from garbage collection
        return IJ.setupDialog(imp, flags);      // ask whether to process all slices of stack (if a stack)
    }
    
    /*** dialogItemChanged ***
     * Called after modifications to the dialog. Returns true if valid input.
     * 
     * @param  gd                  GenericDialog
     * @param  e                   AWTEvent
     * @return                     boolean
     ***/
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
    	globMin = gd.getNextNumber();
    	globMax = gd.getNextNumber();
		//values from user dialog extracted
        return (!gd.invalidNumber());
    }
    
	
	public void run(ImageProcessor ip) {
		FloatProcessor floatIP;
		int width = ip.getWidth();
        int height = ip.getHeight();
        
        //TODO: is this required?
        double tmin = globMin;
    	double tmax = globMax;
    	double tsum = tmax + tmin;
        
    	//TODO: do I still use previewEdm?
        if (previewing && previewEdm!=null) {
        	floatIP = previewEdm;
        } else {
        	floatIP = new FloatProcessor(width, height, (float[])ip.convertToFloat().getPixels());
            //if (floatIP==null) return;         //interrupted during preview?
            previewEdm = floatIP;
        }
        //float[] fPixels = (float[])floatIP.getPixels();
        
        Rectangle roiRect = ip.getRoi();
        
        if (ip.getBitDepth() == 8) {
        	byte[] bPixels = (byte[])ip.getPixels();
        	//IJ.log("("+String.valueOf(tmin)+"|"+String.valueOf(tmax)+")");
        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++){
        			tsum = tmax + tmin - (double)bPixels[p];
        			if (((byte) bPixels[p] & 0xFF) < tmin) {tsum = tmax;}
        			if (((byte) bPixels[p] & 0xFF) > tmax) {tsum = tmin;}
        			bPixels[p] = (byte) (tsum);
        		}
        	}
        }else if (ip.getBitDepth() == 16) {
        	short[] bPixels = (short[])ip.getPixels();
        	
        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++){
        			tsum = tmax + tmin - (double)bPixels[p];
        			if ((bPixels[p] & 0xFFFF) < tmin) {tsum = tmax;}
        			if ((bPixels[p] & 0xFFFF) > tmax) {tsum = tmin;}
        			bPixels[p] = (short) (tsum);	
        		}
        	}
        }else if (ip.getBitDepth() == 32) {
        	float[] bPixels = (float[])ip.getPixels();
        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++){
        			tsum = tmax + tmin - (double)bPixels[p];
        			if (bPixels[p] < tmin) {tsum = tmax;}
        			if (bPixels[p] > tmax) {tsum = tmin;}
        			bPixels[p] = (float) (tsum);
        		}
        	}
        }
        
        
        return;
	}
	
	/*** setNPasses ***
     * This method is called by ImageJ to set the number of calls to run(ip)
	 * corresponding to 100% of the progress bar. No progress bar here 
     * 
     * @param  nPasses             int
     ***/
    public void setNPasses (int nPasses) {}
    
	/*** showAbout ***
     * Displays Info about this method/class/ImageJ function.
     * 
     ***/
    void showAbout() {
    	IJ.showMessage("About Invert Values...",
    	"This plugin filter inverts 8-bit, 16-bit and 32-bit images.\n" +
    	"The default minimum and maximum value suggested equal the\n" +
    	"global minimum and maximum value of the hyperstack. Values \n" +
    	"are being mirrored at the centre between minimum and maximum\n" +
    	"and are saturated at the boundaries.\n"
    	);
    }
}




