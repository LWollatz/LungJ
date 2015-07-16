import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.gui.GenericDialog;
import ij.gui.DialogListener;
import ij.process.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
* LungJ code for filling holes in a mask
* run("Create Threshold Mask","threshold=0.5");
*
* as a preparation, make sure your mask is binary, where the background is minimum and the foreground maximum.
* use Flood Fill (3D) to fill the background with a grey value between max and min, leaving the holes black
* use Fill Holes Manual (3D) to replace black values with white and grey with black.
* this is ideal for filling a mask with many holes in the foreground but only one or few connected background(s).
*
* Code by Lasse Wollatz, version 2015-06-10
*/

public class Fill_Holes_Manual_3D implements PlugInFilter {
	/** plugin's name */
	public static final String PLUGIN_NAME = "LungJ";
	/** plugin's current version */
	public static final String PLUGIN_VERSION = "v" + "0.2.1";
	//LungJ_.class.getPackage().getImplementationVersion();
	
	private static double LJ_threshold = 0.5;           // where to cut off
	private boolean previewing = false;
    private FloatProcessor previewEdm;
    private double globMin = 0;
    private double globMax = 1;
    
    //private byte[] fPixels;
	private int flags = DOES_8G|DOES_16|DOES_32|DOES_STACKS|PARALLELIZE_STACKS|FINAL_PROCESSING; //KEEP_PREVIEW
	//ImagePlus gimp;
	
	
	public int setup(String arg, ImagePlus imp) {
		if (IJ.versionLessThan("1.48n"))        // generates an error message for older versions
			return DONE;
		if (imp == null){
			return DONE;
		}
		if (arg == "final"){
		
		}else{
			float[] minmax = new float[2];
			minmax = LJPrefs.getMinMax(imp);
	    	globMin = (double)minmax[0];
	    	globMax = (double)minmax[1];
		}
		
		//gimp = imp;
		return flags;
    }
	
	/*
	// Called by ImageJ after setup.
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        
    	//if (!imp.getProcessor().isBinary()) {
            //IJ.error("8-bit binary image (0 and 255) required.");
            //return DONE;
    		//imp.convertToGray8();
    		//ImageProcessor newip = ip.convertToByteProcessor();
    		//ImagePlus newimp = ImagePlus("temp8bit", newip);
    		//IJ.run("8-bit");
        //}
    	
    	float[] minmax = new float[2];
    	
    	minmax = LJPrefs.getMinMax(imp);
    	globMin = (double)minmax[0];
    	globMax = (double)minmax[1];
    	
        // The dialog
        GenericDialog gd = new GenericDialog(command+"...");
        //gd.addNumericField("Threshold", LJ_threshold, 3);
        gd.addSlider("Threshold (in %)", 0.00, 100.00, LJ_threshold);
        gd.addNumericField("minimum Value", globMin, 4);
        gd.addNumericField("maximum Value", globMax, 4);
        gd.addPreviewCheckbox(pfr);             // passing pfr makes the filter ready for preview
        /*if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp("http://rsb.info.nih.gov/ij/plugins/erode-demo.html");*//*
        //gd.addDialogListener(this);             // the DialogItemChanged method will be called on user input
        //previewing = true;
        gd.showDialog();                        // display the dialog; preview runs in the background now
        //previewing = false;
        if (gd.wasCanceled()){
        	return DONE;
        }
        IJ.register(this.getClass());           // protect static class variables (filter parameters) from garbage collection
        return IJ.setupDialog(imp, flags);      // ask whether to process all slices of stack (if a stack)
    }*/
    /*
    // Called after modifications to the dialog. Returns true if valid input.
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
    	LJ_threshold = gd.getNextNumber()/100;
    	globMin = gd.getNextNumber();
    	globMax = gd.getNextNumber();
        return (!gd.invalidNumber() && LJ_threshold>=0 && LJ_threshold<=1);
    }*/
    
	
	public void run(ImageProcessor ip) {
		FloatProcessor floatIP;
		int width = ip.getWidth();
        int height = ip.getHeight();
        
        
        if (previewing && previewEdm!=null) {
        	floatIP = previewEdm;
        } else {
        	floatIP = new FloatProcessor(width, height, (float[])ip.convertToFloat().getPixels());
            if (floatIP==null) return;         //interrupted during preview?
            previewEdm = floatIP;
        }
        float[] fPixels = (float[])floatIP.getPixels();
        Rectangle roiRect = ip.getRoi();
        
        
        // Pixels with a value < LJ_threshold will be set to background
        if (ip.getBitDepth() == 8) {
        	byte[] bPixels = (byte[])ip.getPixels();
        
        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++)
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++)
        			if (fPixels[p] == (float)255)
        				bPixels[p] = (byte)255;
        			else if(fPixels[p] == (float)0)
        				bPixels[p] = (byte)255;
        			else
        				bPixels[p] = (byte)0;
        }else if (ip.getBitDepth() == 16) {
        	short[] bPixels = (short[])ip.getPixels();
        	double tmin = globMin;
        	double tmax = globMax;

        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++)
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++)
        			if (fPixels[p] == (float)tmax)
        				bPixels[p] = (short)tmax;
        			else if (fPixels[p] == (float)tmin)
        				bPixels[p] = (short)tmax;
        			else
        				bPixels[p] = (short)tmin;
        				
        }else if (ip.getBitDepth() == 32) {
        	float[] bPixels = (float[])ip.getPixels();
        	double tmin = globMin;
        	double tmax = globMax;

        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++)
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++)
        			if (fPixels[p] == (float)tmax)
        				bPixels[p] = (float)tmax;
        			else if (fPixels[p] == (float)tmin)
        				bPixels[p] = (float)tmax;
        			else
        				bPixels[p] = (float)tmin;
        }
        
        
        return;
	}
	
	/** This method is called by ImageJ to set the number of calls to run(ip)
     *  corresponding to 100% of the progress bar. No progress bar here */
    public void setNPasses (int nPasses) {}
}


