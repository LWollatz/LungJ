
	import ij.IJ;
	import ij.ImagePlus;
	import ij.WindowManager;
	import ij.gui.GenericDialog;
	import ij.plugin.PlugIn;
	import ij.plugin.filter.Analyzer;
	import ij.process.ImageProcessor;
	import ij.measure.ResultsTable;

	/** 
	 * Compares two binary masks by combining them into a single colour-coded image. Agreed 
	 * foreground is white and agreed background black. Foreground detected as background is 
	 * blue and background detected as foreground red.
	 * 
	 * - As a preparation, make sure both masks are binary where the background is minimum 
	 *   and the foreground maximum.
	 * - One image should represent the correct masks, while the other one should represent 
	 *   the test-mask
	 * - Use Compare Masks providing the correct masks first and the test-mask second to 
	 *   combine the two and get a results table with the pixel statistics.
	 *   
	 * @author Lasse Wollatz
	 *   
	 **/

	public class Compare_MapMask implements PlugIn {
		
		private double POB = 0;
		private double PBO = 0;
		private double PO = 0;
		private double PB = 0;
		
		public void run(String command){
			if (IJ.versionLessThan("1.48n"))        // generates an error message for older versions
				return;
			IJ.showStatus("Applying Mask...");
			
			//get available images:
			int Nimg = WindowManager.getImageCount();
			if (Nimg < 2){
				IJ.error("At least two images are required - the correct mask and the mask to be tested.");
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
			
			GenericDialog gd = new GenericDialog(command+" Compare Mask");
			gd.addChoice("Actual Mask", lstMasks, lstMasks[masksID]);
			gd.addChoice("Detected Map", lstMaps, lstMaps[mapsID]);
			gd.showDialog();
			if (gd.wasCanceled()){
	        	return;
	        }
			int AM_srcID = lstImageIds[gd.getNextChoiceIndex()];
			int AM_maskID = lstImageIds[gd.getNextChoiceIndex()];
			
			ImagePlus image = WindowManager.getImage(AM_srcID);
			ImagePlus mask = WindowManager.getImage(AM_maskID);
			
			//TODO: NEED TO CHECK IF SAME SIZE...
			
			int tHeight = image.getHeight();
			int tWidth = image.getWidth();
			int tDepth = image.getNSlices();
			int tFrames = mask.getNFrames();
			int tFrames2 = image.getNFrames();
			int tChannels = mask.getNChannels();
			int tChannels2 = image.getNChannels();
			
			if (tFrames != tFrames2 || tChannels != tChannels2){
				IJ.error("Images do not have the same size. (Also check number of frames and channels.)");
				return;
			}
			
			if (tChannels > 1){
				IJ.error("Can't handle multi-channel images.");
				return;
			}
			
			ImagePlus imgout = IJ.createHyperStack("Result", tWidth, tHeight, 3, tDepth, tFrames, 32);
			
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
			    			tempR = iPixels[p] * (1-mPixels[p]);
			    			tempG = (1-iPixels[p]) * mPixels[p];
			    			tempB = iPixels[p];
			    			
			    			POB += tempG;
			    			PBO += tempR;
			    			PO += tempB;
			    			PB += 1-tempB;
			    			
			    			oPixelsR[p] = mPixels[p];
			    			oPixelsG[p] = iPixels[p] * mPixels[p];
			    			oPixelsB[p] = iPixels[p];
			    		}
					}
				}
			}
			
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
			
			POB = POB/PB;
			PBO = PBO/PO;
			double total = PO + PB;
			PB = PB/total;
			PO = PO/total;
			double Perr = PB*POB+PO*PBO;
			
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

