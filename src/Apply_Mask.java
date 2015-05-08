import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;


public class Apply_Mask implements PlugIn{
	
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
		gd.addChoice("Image", lstImages, lstImages[0]);
		gd.addChoice("Mask", lstMasks, lstMasks[0]);
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
		ImagePlus imgout = IJ.createHyperStack("Result", tWidth, tHeight, 1, tDepth, tFrames, 8);
		
		for (int z=1; z<=tDepth; z++){
			ImageProcessor imageIP = image.getStack().getProcessor(z).convertToByte(true);
			for (int f=1; f<=tFrames; f++){
				int index = mask.getStackIndex(1, z, f);
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
							oPixels[p] = (int)0;
		    			}else{
							oPixels[p] = (byte)iPixels[p];
		    			}
		    		}
				}
			}
		}
		
		//ImageCalculator ic = new ImageCalculator();
		//ImagePlus output = ic.run("Multiply create 32-bit stack", image, mask);
		imgout.show();
		
		
		
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
