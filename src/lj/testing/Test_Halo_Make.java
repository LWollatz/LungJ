package lj.testing;

import lj.LJPrefs;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.io.IOException;
import java.util.Properties;

public class Test_Halo_Make implements PlugIn {
	//TODO: create function to read in blocks and replace halo by a chessboard pattern or similar
		
	
	public void run(String command){
		//TODO: fix macro recorder to record escape characters correctly over three levels
		
		IJ.showProgress(0, 100);
		int tWidth = 20;
		int tHeight = 20;
		int tDepth = 20;
		int hWidth = 3;
		int hHeight = 3;
		int hDepth = 3;
		
		ImagePlus imgout = IJ.createImage("HaloTestImage", tWidth, tHeight, tDepth, 16);
		
		IJ.log("test");
		
		for (int z=1; z<=tDepth; z++){
			//int index = imgout.getStackIndex(1, z, f);
			//ImageProcessor imageIP = image.getStack().getProcessor(z); //TODO: load active image
			ImageProcessor outputIP = imgout.getStack().getProcessor(z);
		
			//byte[] iPixels = (byte[])imageIP.getPixels();
			short[] oPixels = (short[])outputIP.getPixels();
			
			for (int y=0; y<tHeight; y++){
	    		for (int x=0, p=x+y*tWidth; x<tWidth; x++,p++){
	    			/*
	    			if (iPixels[p] == (byte)0){
						oPixels[p] = (int)0;
	    			}else{*/
	    			if (y<hHeight ||  y>=tHeight-hHeight || x<hWidth ||  x>=tWidth-hWidth || z<=hDepth ||  z>tDepth-hDepth){
						oPixels[p] = (short)(40000);
	    			}
	    		}
			}
			IJ.showProgress(z, tDepth);
			
			outputIP.setPixels(oPixels);
			
		}
		
		
		
		IJ.log("test end");
		
		imgout.show();
		
		IJ.showProgress(100, 100);
		
	}
	
	
}
