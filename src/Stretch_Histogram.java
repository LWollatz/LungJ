import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.ExtendedPlugInFilter;
import ij.plugin.filter.PlugInFilterRunner;
import ij.gui.GenericDialog;
import ij.gui.DialogListener;
import ij.process.*;

import java.awt.*;

import lj.LJPrefs;


/**
 * LungJ code for inverting the values in an image
 * run("Invert Values","minimum=0 maximum=255");
 *
 * this code changes the actual values and mirrors them around the centre between minimum
 * and maximum. Values which exceed the minimum or maximum specified are being cut of at 
 * the boundary. The GUI looks up the minimum and maximum value in an image and suggests 
 * them as defaults.
 *
 * @author Lasse Wollatz
 * 
 **/

public class Stretch_Histogram implements ExtendedPlugInFilter, DialogListener {
	/** plugin's name */
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version */
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	private boolean previewing = false;
    private FloatProcessor previewEdm;
    private double globMin = 0;
    private double globMax = 1;
    
    private double Aorig = 0;
    private double Anew = 0;
    private double Borig = 1;
    private double Bnew = 1;
    
    private double m = 1;
    private double b = 0;
    
	private int flags = DOES_8G|DOES_16|DOES_32|DOES_STACKS|PARALLELIZE_STACKS|KEEP_PREVIEW;
	
	
	public int setup(String arg, ImagePlus imp) {
		if (IJ.versionLessThan("1.48n"))        // generates an error message for older versions
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
	
	// Called by ImageJ after setup.
    public int showDialog(ImagePlus imp, String command, PlugInFilterRunner pfr) {
        
    	
    	float[] minmax = new float[2];
    	
    	minmax = LJPrefs.getMinMax(imp);
    	globMin = (double)minmax[0];
    	globMax = (double)minmax[1];
    	
    	
    	
    	
        // The dialog
        GenericDialog gd = new GenericDialog(command+"...");
        gd.addNumericField("A_orig", globMin, 4);
        gd.addNumericField("A_new", 25000, 4);
        gd.addNumericField("B_orig", globMax, 4);
        gd.addNumericField("B_new", 37000, 4);
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
    	Aorig = gd.getNextNumber();
    	Anew = gd.getNextNumber();
    	Borig = gd.getNextNumber();
    	Bnew = gd.getNextNumber();
    	m = (Bnew-Anew)/(Borig-Aorig);
    	b = Anew - m*Aorig;
        return (!gd.invalidNumber());
    }
    
	
	public void run(ImageProcessor ip) {
		FloatProcessor floatIP;
		int width = ip.getWidth();
        int height = ip.getHeight(); 
    	double tsum;
        
    	/*
selectWindow("0250_0750_1000.tif");
run("Window/Level...");
resetMinAndMax();
setMinAndMax(-1, 2);
run("Close");
run("16-bit");
run("Stretch Histogram", "a_orig=13055 a_new=24726 b_orig=34815 b_new=32255 stack");
run("Window/Level...");
resetMinAndMax();
run("Close");
    	 */
    	
    	
    	
    	
    	//TODO: do I still use previewEdm?
        if (previewing && previewEdm!=null) {
        	floatIP = previewEdm;
        } else {
        	floatIP = new FloatProcessor(width, height, (float[])ip.convertToFloat().getPixels());
            previewEdm = floatIP;
        }
        
        Rectangle roiRect = ip.getRoi();
        
        //TODO: need to sort out the maximum values
        if (ip.getBitDepth() == 8) {
        	byte[] bPixels = (byte[])ip.getPixels();
        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++){
        			tsum = b + m*(double)bPixels[p];
        			if (((byte) bPixels[p] & 0xFF) < 0) {tsum = 0;}
        			if (((byte) bPixels[p] & 0xFF) > 255) {tsum = 255;}
        			bPixels[p] = (byte) (tsum);
        		}
        	}
        }else if (ip.getBitDepth() == 16) {
        	short[] bPixels = (short[])ip.getPixels();
        	short sSum = 0;
        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++){
        			sSum = (short)(b + m*(double)bPixels[p]);
        			tsum = (b + m*(double)(bPixels[p] & 0xFFFF));
        			if ((bPixels[p] & 0xFFFF) < 0) {sSum = 0;}
        			//if ((bPixels[p] & 0xFFFF) > tmax) {tsum = tmin;}
        			bPixels[p] = (short)(tsum - 65536);	//-32768 //49152
        		}
        	}
        }else if (ip.getBitDepth() == 32) {
        	float[] bPixels = (float[])ip.getPixels();
        	for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
        		for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++){
        			tsum = b + m*(double)bPixels[p];
        			if (bPixels[p] < 0) {tsum = 0;}
        			//if (bPixels[p] > tmax) {tsum = tmin;}
        			bPixels[p] = (float) (tsum);
        		}
        	}
        }
        
        
        return;
	}
	
	/** This method is called by ImageJ to set the number of calls to run(ip)
     *  corresponding to 100% of the progress bar. No progress bar here */
    public void setNPasses (int nPasses) {}
    
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




