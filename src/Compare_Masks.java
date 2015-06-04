



import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;
import ij.measure.ResultsTable;


public class Compare_Masks implements PlugIn {
	
	private double TP = 0;
	private double TN = 0;
	private double FP = 0;
	private double FN = 0;
	
	public void run(String command){
		if (IJ.versionLessThan("1.48n"))        // generates an error message for older versions
			return;
		IJ.showStatus("Applying Mask...");
		
		//get available images:
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
		
		GenericDialog gd = new GenericDialog(command+" Apply Mask");
		gd.addChoice("Actual Mask", lstMasks, lstMasks[0]);
		gd.addChoice("Detected Mask", lstMasks, lstMasks[0]);
		gd.showDialog();
		if (gd.wasCanceled()){
        	return;
        }
		int AM_srcID = lstImageIds[gd.getNextChoiceIndex()];
		int AM_maskID = lstImageIds[gd.getNextChoiceIndex()];
		
		/*
		int AM_srcID = 0;
		int AM_maskID = 0;
		for (int i = 0; i < Nimg; i++){
			String temp = lstImages[i];
			if(temp == AM_srcTitle){
				AM_srcID = lstImageIds[i];
			}
			if(temp == AM_maskTitle){
				AM_maskID = lstImageIds[i];
			}
		}*/
		
		ImagePlus image = WindowManager.getImage(AM_srcID);
		ImagePlus mask = WindowManager.getImage(AM_maskID);
		
		//TODO: NEED TO CHECK IF SAME SIZE...
		
		int tHeight = image.getHeight();
		int tWidth = image.getWidth();
		int tDepth = image.getNSlices();
		int tFrames = mask.getNFrames();
		int tChannels = mask.getNChannels();
		
		if (tChannels > 1){
			IJ.error("Can't handle multi-channel images.");
			return;
		}
		
		/*
		boolean isBin = false;
		if(mask.getProcessor().isBinary()){
			IJ.runMacro("selectImage("+AM_maskID+");");
			//NOT GOOD! NEED TO DO THIS ON COPY!
			IJ.run("Divide...", "value=255 stack");
			isBin = true;
		}
		*/
		
		//ImagePlus imgout = IJ.createImage("Result", "8-bit", tWidth, tHeight, tDepth*tFrames);
		ImagePlus imgout = IJ.createHyperStack("Result", tWidth, tHeight, 3, tDepth, tFrames, 8);
		
		for (int z=1; z<=tDepth; z++){
			
			for (int f=1; f<=tFrames; f++){
			//for (int c=1; c<=tChannels; c++){
				
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
			//}
			}
		}
		
		ResultsTable rt = Analyzer.getResultsTable();
		if (rt == null) {
		        rt = new ResultsTable();
		        Analyzer.setResultsTable(rt);
		}
		rt.incrementCounter();
		/*rt.addValue("True Positive", TP);
		rt.addValue("True Negative", TN);
		rt.addValue("False Positive", FP);
		rt.addValue("False Negative", FN);
		*/
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
		
		rt.showRowNumbers(false);
		//ImageCalculator ic = new ImageCalculator();
		//ImagePlus output = ic.run("Multiply create 32-bit stack", image, mask);
		imgout.show();
		rt.show("Results");
		
		
		/*
		LJ_srcID = confirmImg(LJ_srcID,LJ_srcFilename,"original image",true);
		LJ_mapID = confirmImg(LJ_mapID,LJ_mapFilename,"mask",false);
		//__start__
		selectImage(LJ_mapID);
		LJ_mapFilename = getTitle;
		getMinAndMax(min, max);
		IJ.run("Divide...", "value="+max+" stack");
		imageCalculator("Multiply create stack", LJ_srcFilename,LJ_mapFilename);
		selectImage(LJ_mapID);
		IJ.run("Multiply...", "value="+max+" stack");
		//__end__
		if (LJ_opt_Autosave){
			LJ_segFilename = "autosave_"+LJ_srcFilename+"_"+LJ_clsName+"_th"+LJ_Threshold+"_segmentation.tif";
			selectWindow("Result of "+LJ_srcFilename);
			saveAs("Tiff", LJ_srcDirectory+"\\"+LJ_segFilename);
		}
		rename("Segmented Image");
		LJ_segID = getImageID();
		*/
		
		/*
		if(isBin){
			IJ.runMacro("selectImage("+AM_maskID+");");
			IJ.run("Multiply...", "value=255 stack");
		}
		*/
		
		IJ.showStatus("Mask Applied.");
	}
}
