import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;

import lj.LJPrefs;

/*** Compare_MapMask
 * compares a masks and a probability map by combining them into a
 * single colour-coded image. Agreed foreground is white and agreed
 * background black. Foreground detected as background is blue and
 * background detected as foreground red.
 * 
 * - As a preparation, make sure the mask is binary where the
 *   background is 0 and the foreground 1. The map should be a
 *   probability map from 0 to 1
 * - One image should represent the correct masks, while the other
 *   one should represent the test-map
 * - Use Compare MapMask providing the correct masks first and the
 *   test-mask second to combine the two and get a results table with
 *   the pixel statistics.
 * 
 * @author Lasse Wollatz  
 ***/
public class Compare_MapMask implements PlugIn {
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = Compare_MapMask.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	/** statistic values **/
	private double POB = 0;
	private double PBO = 0;
	private double PO = 0;
	private double PB = 0;

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
			IJ.error("At least two images are required - the correct mask and the map to be tested.");
			return;
		}
		String[] lstImages = WindowManager.getImageTitles();
		int[] lstImageIds = WindowManager.getIDList();
		String[] lstMasks = new String[Nimg];
		String[] lstMaps = new String[Nimg];
		int Nmasks = Nimg;
		int Nmaps = Nimg;
		int masksID = -1;
		int mapsID = -1;
		for (int i = 0; i < Nimg; i++){
			ImageProcessor temp = WindowManager.getImage(lstImageIds[i]).getProcessor();
			if((temp.getMin() == 0 && temp.getMax() == 1) || temp.isBinary()){
				lstMaps[i] = lstImages[i];
				lstMasks[i] = lstImages[i];
				if(mapsID == -1){
					mapsID = i;
				}
				if(masksID == -1){
					masksID = i;
				}
			}else if((temp.getMin() >= 0 && temp.getMax() <= 1)){
				lstMaps[i] = lstImages[i];
				lstMasks[i] = "--not a mask--";
				Nmasks--;
				if(mapsID == -1){
					mapsID = i;
				}
				IJ.log(String.valueOf(temp.getMin()) + " - " + String.valueOf(temp.getMin()));
			}else{
				lstMaps[i] = "--not a map--";
				lstMasks[i] = "--not a mask--";
				Nmasks--;
				Nmaps--;
			}
		}
		if (Nmasks < 1){
			IJ.error("Could not find a valid mask. (A mask needs to be binary)");
			return;
		}
		if (Nmaps < 2){
			IJ.error("Could not find two valid input images. (A mask needs to be binary, and a map only contain values between 0 and 1)");
			return;
		}
		
		/**create dialog to request values from user: **/
		GenericDialog gd = new GenericDialog(command+" Compare Mask");
		gd.addChoice("Actual Mask", lstMasks, lstMasks[masksID]);
		gd.addChoice("Detected Map", lstMaps, lstMaps[mapsID]);
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
		int tFrames2 = image.getNFrames();
		int tChannels = mask.getNChannels();
		int tChannels2 = image.getNChannels();
		
		/** check if images are valid **/
		//TODO: NEED TO CHECK IF SAME SIZE...
		if (tFrames != tFrames2 || tChannels != tChannels2){
			IJ.error("Images do not have the same size. (Also check number of frames and channels.)");
			return;
		}
		if (tChannels > 1){
			IJ.error("Can't handle multi-channel images.");
			return;
		}
		
		/** prepare output image **/
		ImagePlus imgout = IJ.createHyperStack("Result", tWidth, tHeight, 3, tDepth, tFrames, 32);

		/** process the images **/
		for (int z=1; z<=tDepth; z++){
			for (int f=1; f<=tFrames; f++){
				int index = mask.getStackIndex(1, z, f);
				int indexi = image.getStackIndex(1, z, f);
				int indexR = imgout.getStackIndex(1, z, f);
				int indexG = imgout.getStackIndex(2, z, f);
				int indexB = imgout.getStackIndex(3, z, f);

				ImageProcessor imageIP = image.getStack().getProcessor(indexi).convertToFloat();
				ImageProcessor maskIP = mask.getStack().getProcessor(index).convertToFloat();

				ImageProcessor outputIPR = imgout.getStack().getProcessor(indexR);
				ImageProcessor outputIPG = imgout.getStack().getProcessor(indexG);
				ImageProcessor outputIPB = imgout.getStack().getProcessor(indexB);

				float[] mPixels = (float[])maskIP.getPixels(); //detected map
				float[] iPixels = (float[])imageIP.getPixels();//correct mask
				float[] oPixelsR = (float[])outputIPR.getPixels();
				float[] oPixelsG = (float[])outputIPG.getPixels();
				float[] oPixelsB = (float[])outputIPB.getPixels();

				float tempR = 0;
				float tempG = 0;
				float tempB = 0;

				for (int y=0; y<tHeight; y++){
					for (int x=0, p=x+y*tWidth; x<tWidth; x++,p++){
						
						if(iPixels[p] == (float)255){
							tempR = 1-mPixels[p];
							tempG = 0;
							tempB = 1;
						}else{
							tempR = iPixels[p] * (1-mPixels[p]);
							tempG = (1-iPixels[p]) * mPixels[p];
							tempB = iPixels[p];
						}
						
						POB += tempG;  //+probability of foreground detected if true background
						PBO += tempR;  //+probability of background detected if true foreground
						PO += tempB;   //+1 if true foreground
						PB += 1-tempB; //+1 if true background

						oPixelsR[p] = mPixels[p];
						oPixelsG[p] = iPixels[p] * mPixels[p];
						oPixelsB[p] = iPixels[p];
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
		rt.incrementCounter();
		rt.addLabel("Total");
		rt.addValue("Value", PO+PB);
		rt.addValue("Unit", "voxel");
		rt.incrementCounter();
		rt.addLabel("foreground");
		rt.addValue("Value", PO);
		rt.addValue("Unit", "voxel");
		rt.incrementCounter();
		rt.addLabel("background");
		rt.addValue("Value", PB);
		rt.addValue("Unit", "voxel");
		rt.incrementCounter();
		rt.addLabel("");
		rt.addValue("Value", "");
		rt.addValue("Unit", "");

		POB = POB/PB; //probability of foreground detected if correct would be background
		PBO = PBO/PO; //probability of background detected if correct would be foreground
		double total = PO + PB;
		PB = PB/total; //probability voxel to be background
		PO = PO/total; //probability voxel to be foreground
		double Perr = PB*POB+PO*PBO; //probability of error

		rt.incrementCounter();
		rt.addLabel("PO");
		rt.addValue("Value", 100*PO);
		rt.addValue("Unit", "%");
		rt.incrementCounter();
		rt.addLabel("PB");
		rt.addValue("Value", 100*PB);
		rt.addValue("Unit", "%");
		rt.incrementCounter();
		rt.addLabel("POB");
		rt.addValue("Value", 100*POB);
		rt.addValue("Unit", "%");
		rt.incrementCounter();
		rt.addLabel("PBO");
		rt.addValue("Value", 100*PBO);
		rt.addValue("Unit", "%");
		rt.incrementCounter();
		rt.addLabel("Perr");
		rt.addValue("Value", 100*Perr);
		rt.addValue("Unit", "%");

		rt.showRowNumbers(false);
		imgout.show();
		rt.show("Results");


		IJ.showStatus("Masks Compared.");
	}
}

