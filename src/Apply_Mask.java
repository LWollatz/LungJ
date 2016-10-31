import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import lj.LJPrefs;


/*** Apply_Mask
 * applies a mask to an image, leaving the foreground as it is and
 * replacing the background by black.
 * 
 * - As a preparation, make sure the mask is binary where the 
 *   background is minimum and the foreground maximum.
 * - One image should represent the mask, while the other one should
 *   represent the original data.
 * - Use Apply Mask to replace background values in the original data
 *   with black.
 * 
 * @author Lasse Wollatz
 ***/
public class Apply_Mask implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;

	/*** run
	 * 
	 * @param  command             String
	 * 
	 * @see    ij.gui.GenericDialog
	 ***/
	public void run(String command){
		/** generate error message for older versions: **/
		if (IJ.versionLessThan("1.48n"))        
			return;
		IJ.showStatus("Applying Mask...");

		/** get available images: **/
		int Nimg = WindowManager.getImageCount();
		if (Nimg < 2){
			IJ.error("At least two images are required - the original and the mask.");
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
			}
		}
		if (Nmasks < 1){
			IJ.error("Could not find valid mask. (A mask needs to be binary, or only contain values between 0 and 1)");
			return;
		}
		
		/** create dialog to request values from user: **/
		GenericDialog gd = new GenericDialog(command+" Apply Mask");
		gd.addChoice("Image", lstImages, lstImages[0]);
		gd.addChoice("Mask", lstMasks, lstMasks[0]);
		if (IJ.getVersion().compareTo("1.42p")>=0)
			gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
			return;
		}
		int AM_srcID = lstImageIds[gd.getNextChoiceIndex()];
		int AM_maskID = lstImageIds[gd.getNextChoiceIndex()];
		/** values from user dialog extracted **/

		/** get images **/
		ImagePlus image = WindowManager.getImage(AM_srcID);
		ImagePlus mask = WindowManager.getImage(AM_maskID);
		
		/** get image properties **/
		int tHeight = image.getHeight();
		int tWidth = image.getWidth();
		int tDepth = image.getNSlices();
		int tFrames = mask.getNFrames();
		int tChannels = mask.getNChannels();
		//TODO: NEED TO CHECK IF SAME SIZE...

		/** create output image **/
		ImagePlus imgout = IJ.createHyperStack("Result", tWidth, tHeight, tChannels, tDepth, tFrames, 8);

		/** process image **/
		for (int z=1; z<=tDepth; z++){
			ImageProcessor imageIP = image.getStack().getProcessor(z).convertToByte(true);
			for (int f=1; f<=tFrames; f++){
				for (int c=1; c<=tChannels; c++){
					int index = mask.getStackIndex(c, z, f);
					ImageProcessor maskIP = mask.getStack().getProcessor(index).convertToByte(true);
					String label = mask.getStack().getShortSliceLabel(index);
					ImageProcessor outputIP = imgout.getStack().getProcessor(index);
					imgout.getStack().setSliceLabel(label, index);

					byte[] mPixels = (byte[])maskIP.getPixels();
					byte[] iPixels = (byte[])imageIP.getPixels();
					byte[] oPixels = (byte[])outputIP.getPixels();

					for (int y=0; y<tHeight; y++){
						for (int x=0, p=x+y*tWidth; x<tWidth; x++,p++){
							if (mPixels[p] == (byte)0){
								oPixels[p] = (byte)0;
							}else{
								oPixels[p] = (byte)iPixels[p];
							}
						}
					}
				}
			}
		}
		
		/** finalize **/
		imgout.show();
		IJ.showStatus("Mask Applied.");
	}
}
