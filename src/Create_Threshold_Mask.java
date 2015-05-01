import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.gui.GenericDialog;
import ij.gui.DialogListener;
import ij.process.*;
import ij.process.ImageProcessor;
import java.awt.*;
import java.awt.event.*;
import java.util.*;


/**
* LungJ code for binary map creation
* run("Create Threshold Mask","threshold=0.5");
*
* threshold is defined as a percentage between Min and Max
* output image is true binary (0 or 1) but remains the same bit-depth as the input image
* window-size and threshold are automatically adjusted and LUT set to grayscale
* this means that the output map clearly shows foreground and background and is ideal for mathematical operations!
*
* Code by Lasse Wollatz, version 2015-04-30
*/

public class Create_Threshold_Mask implements ExtendedPlugInFilter, DialogListener {
	/** plugin's name */
	public static final String PLUGIN_NAME = "LungJ";
	/** plugin's current version */
	public static final String PLUGIN_VERSION = "v" + "0.2.0";
	//LungJ_.class.getPackage().getImplementationVersion();
	
	private static double LJ_threshold = 0.5;           // where to cut off
	private boolean previewing = false;
    private FloatProcessor previewEdm;
    //private byte[] fPixels;
	private int flags = DOES_8G|DOES_16|DOES_32|SUPPORTS_MASKING|KEEP_PREVIEW|PARALLELIZE_STACKS|FINAL_PROCESSING;
	ImagePlus gimp;
	
	
	public int setup(String arg, ImagePlus imp) {
		if (arg == "final"){
		if(imp.getProcessor().getBitDepth() == 8){
			ImageProcessor ip = imp.getProcessor();
			Rectangle roiRect = ip.getRoi();
			int width = ip.getWidth();
	        byte[] bPixels = (byte[])ip.getPixels();
			for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
	            for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++) {
	                if (bPixels[p] == (byte)255){
	                    bPixels[p] = (byte)1;
	                }
				}
			}
		}else if(imp.getProcessor().getBitDepth() == 16){
			ImageProcessor ip = imp.getProcessor();
			Rectangle roiRect = ip.getRoi();
			int width = ip.getWidth();
	        short[] bPixels = (short[])ip.getPixels();
			for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
	            for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++) {
	                if (bPixels[p] == (short)ip.getMax()){
	                    bPixels[p] = (short)1;
	                }else{
	                	bPixels[p] = (short)0;
	                }
				}
			}
		}else if(imp.getProcessor().getBitDepth() == 32){
			ImageProcessor ip = imp.getProcessor();
			Rectangle roiRect = ip.getRoi();
			int width = ip.getWidth();
	        float[] bPixels = (float[])ip.getPixels();
			for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
	            for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++) {
	                if (bPixels[p] == (float)ip.getMax()){
	                    bPixels[p] = (float)1;
	                }else{
	                	bPixels[p] = (float)0;
	                }
				}
			}
		}
		IJ.setMinAndMax(0, 1);
    	IJ.setThreshold(0.0,0.5,"BLACK_AND_WHITE_LUT");
		}
		if (IJ.versionLessThan("1.42n"))        // generates an error message for older versions
			return DONE;
		gimp = imp;
		return flags;
    }
	
	// Called by ImageJ after setup.
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        
    	if (!imp.getProcessor().isBinary()) {
            //IJ.error("8-bit binary image (0 and 255) required.");
            //return DONE;
    		//imp.convertToGray8();
    		//ImageProcessor newip = ip.convertToByteProcessor();
    		//ImagePlus newimp = ImagePlus("temp8bit", newip);
    		//IJ.run("8-bit");
        }
        // The dialog
        GenericDialog gd = new GenericDialog(command+"...");
        gd.addNumericField("Threshold", LJ_threshold, 3);
        gd.addPreviewCheckbox(pfr);             // passing pfr makes the filter ready for preview
        /*if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp("http://rsb.info.nih.gov/ij/plugins/erode-demo.html");*/
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
    
    // Called after modifications to the dialog. Returns true if valid input.
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
    	LJ_threshold = gd.getNextNumber();
        return (!gd.invalidNumber() && LJ_threshold>=0 && LJ_threshold<=1);
    }
    
	
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
        			if (fPixels[p] <= (float)(255*LJ_threshold))
        				bPixels[p] = (byte)0;
        			else if(previewing)
        				bPixels[p] = (byte)255;
        			else
        				bPixels[p] = (byte)1;
        }else if (ip.getBitDepth() == 16) {
        	short[] bPixels = (short[])ip.getPixels();
        	double tmin = ip.convertToFloat().getMin();
        	double tmax = ip.convertToFloat().getMax();
        	float bound = (float)(tmin+(tmax-tmin)*LJ_threshold);
        	//IJ.log(tmin + "<" + bound + "<" + tmax);
        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++)
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++)
        			if (fPixels[p] <= bound && previewing)
        				bPixels[p] = (short)ip.getMin();
        			else if (fPixels[p] <= bound)
        				bPixels[p] = (short)0;
        			else if(previewing)
        				bPixels[p] = (short)ip.getMax();
        			else
        				bPixels[p] = (short)1;
        				
        }else if (ip.getBitDepth() == 32) {
        	float[] bPixels = (float[])ip.getPixels();
        	double tmin = ip.convertToFloat().getMin();
        	double tmax = ip.convertToFloat().getMax();
        	float bound = (float)(tmin+(tmax-tmin)*LJ_threshold);
        	//IJ.log(tmin + "<" + bound + "<" + tmax);
        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++)
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++)
        			if (fPixels[p] <= bound && previewing)
        				bPixels[p] = (float)ip.getMin();
        			else if (fPixels[p] <= bound)
        				bPixels[p] = (float)0;
        			else if(previewing)
        				bPixels[p] = (float)ip.getMax();
        			else
        				bPixels[p] = (float)1;
        }
        
        
        return;
	}
	
	/** This method is called by ImageJ to set the number of calls to run(ip)
     *  corresponding to 100% of the progress bar. No progress bar here */
    public void setNPasses (int nPasses) {}
}
