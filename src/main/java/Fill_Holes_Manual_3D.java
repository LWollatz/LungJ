import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.*;

import java.awt.*;

import lj.LJPrefs;


/*** Fill_Holes_Manual_3D
 * fills holes in a mask.
 * 
 * - Select a mask
 * - Use Fill 3D tool to fill in the outside in gray
 * - The function will then set all black voxel to white and
 *   all gray voxel to black
 *
 * @author Lasse Wollatz
 * 
 * @see    <a href="http://imagej.net/Flood_Fill(3D)">Flood_Fill(3D)</a>
 ***/
public class Fill_Holes_Manual_3D implements PlugInFilter {
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();

	private boolean previewing = false;
	private FloatProcessor previewEdm;
	private double globMin = 0;
	private double globMax = 1;

	/** defines what type of images this filter can be applied to **/
	private int flags = DOES_8G|DOES_16|DOES_32|DOES_STACKS|PARALLELIZE_STACKS|FINAL_PROCESSING; //KEEP_PREVIEW

	/*** setup
	 * 
	 * @param  arg            String 
	 * @param  imp            ImagePlus 
	 * 
	 * @return                int
	 * 
	 * @see    LJPrefs#getMinMax
	 ***/
	public int setup(String arg, ImagePlus imp) {
		/** generate error message for older versions: **/
		if (IJ.versionLessThan("1.48n"))
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

		return flags;
	}

	/*** run
	 * 
	 * @param  ip                  ImageProcessor
	 ***/
	public void run(ImageProcessor ip) {
		FloatProcessor floatIP;
		int width = ip.getWidth();
		int height = ip.getHeight();


		if (previewing && previewEdm!=null) {
			floatIP = previewEdm;
		} else {
			floatIP = new FloatProcessor(width, height, (float[])ip.convertToFloat().getPixels());
			//if (floatIP==null) return;         //interrupted during preview?
			previewEdm = floatIP;
		}
		float[] fPixels = (float[])floatIP.getPixels();
		Rectangle roiRect = ip.getRoi();



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

	/*** setNPasses
	 * This method is called by ImageJ to set the number of calls to
	 * run(ip) corresponding to 100% of the progress bar. No progress bar
	 * here.
	 * 
	 * @param  nPasses        int 
	 ***/
	public void setNPasses(int nPasses) {
	}
}


