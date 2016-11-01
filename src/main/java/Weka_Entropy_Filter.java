
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import trainableSegmentation.filters.Entropy_Filter;

/*** Entropy_Filter
 * class implements a circular entropy filter
 * This class has been copied from the TWS and adapted for independent use.
 * 
 * @author Trainable WEKA Segmentation
 * @author m-ezzat
 * @author imagejan
 * @author iarganda
 * @author dscho
 ***/
public class Weka_Entropy_Filter implements PlugInFilter{
	Entropy_Filter WekaFilter = new Entropy_Filter();
	/** original image */
	ImagePlus origImg = null;
	/** radius to use (in pixels) */
	int radius = 2;
	/** number of bins to use in the histogram */
	int numBins = 256;
	
	/**
	 * Main method when called as a plugin filter
	 * @param ip input image
	 */
	public void run(ImageProcessor ip){
		WekaFilter.run(ip);	
	}

	/**
	 * Setup method
	 * @param arg filter arguments
	 * @param imp input image
	 */
	public int setup(String arg, ImagePlus imp) {
		return WekaFilter.setup(arg, imp);
	}

//	
//	
//	/**
//	 * Get the entropy filter version of an image
//	 * @param ip input image
//	 * @param radius radius to use (in pixels)
//	 * @param numBins number of bins to use in the histogram
//	 * @return entropy image (32-bit)
//	 */
//	public FloatProcessor getEntropy(ImageProcessor ip, int radius, int numBins){
//		return WekaFilter.getEntropy(ip, radius, numBins);
//	}
//	
//	/**
//	 * Apply entropy filter to an image
//	 * @param ip input image
//	 * @param radius radius to use (in pixels)
//	 * @param numBins number of bins to use in the histogram
//	 */
//	public void applyEntropy(ImageProcessor ip, int radius, int numBins){
//		final FloatProcessor fp = getEntropy(ip, radius, numBins);
//		
//		ImageProcessor ip2;
//		
//		// rescale to the corresponding number of bits
//		if (ip instanceof FloatProcessor == false) 
//		{
//			if (ip instanceof ByteProcessor)	
//			{
//				//IJ.log("Float2Byte");
//				ip2 = fp.convertToByte(true);
//			}
//			else
//			{
//				//IJ.log("Float2Short");
//				ip2 = fp.convertToShort(true);
//			}
//			ip.setPixels(ip2.getPixels());
//		}
//		else
//		{
//			//IJ.log("keeping output");
//			ip.setPixels( fp.getPixels() );
//		}
//		
//		//IJ.log("resetminmax");
//		ip.resetMinAndMax();
//		
//	}

	/**
	 * Display filter information
	 */
	void showAbout(){
		IJ.showMessage("Entropy filter...",
				"Circular entropy filter by I. Arganda-Carreras\n"+
				"ImageJ local entropy filter. Output is 32-bit\n");
	}
	
}
