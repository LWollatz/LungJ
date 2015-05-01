/**
 * 
 */
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.*;
import ij.plugin.*;
import ij.process.*;

import java.awt.*;
import java.awt.event.*;
import java.util.*;

/**
 * @author Lasse Wollatz
 *
 */
public class Subdivide_3D implements PlugIn{
	
	private static String BC_outDirectory = "J:\\Biomedical Imaging Unit\\Research\\Research temporary\\3D IfLS Lung Project\\temp\\20150324_IfLS_Segmentation\\output";
	private static int stepX = 250;
	private static int stepY = 250;
	private static int stepZ = 250;
	private static int z_offset = 0;
	
	public void run(String command){
		IJ.showStatus("Creating blocks...");
		IJ.showProgress(0, 100);
		ImagePlus image = WindowManager.getCurrentImage();
		int[] properties = image.getDimensions(); //width, height, nChannels, nSlices, nFrames
		int maxX = properties[0];
		int maxY = properties[1];
		int maxZ = properties[3];
		int bits = image.getBitDepth();
		
		GenericDialog gd = new GenericDialog(command+" Subdivide image and save into directory");
		gd.addStringField("Output directory", BC_outDirectory, 100);
		gd.addMessage("filename will be 'z'_'y'_'x'.tif");
		gd.addMessage("Block Properties:");
		gd.addNumericField("width", stepX, 0);
		gd.addNumericField("height", stepY, 0);
		gd.addNumericField("depth", stepZ, 0);
		gd.addMessage("Other Settings:");
		gd.addNumericField("z-offset", z_offset, 0, 4,  "(affects filename only)");
		gd.showDialog();
		if (gd.wasCanceled()){
        	return;
        }
		BC_outDirectory = gd.getNextString();
		stepX = (int)gd.getNextNumber();
		stepY = (int)gd.getNextNumber();
		stepZ = (int)gd.getNextNumber();
		z_offset = (int)gd.getNextNumber();
		
		ImageProcessor ip = image.getProcessor();
		
		for (int z=0; z<maxZ; z+=stepZ) {
			ip.setSliceNumber(z);
			IJ.showProgress(z, maxZ);
			//run("Image Sequence...", "open=["+stackdirectory+"] number=250 file=DigiSens_ sort");
			for (int x=0; x<maxX; x+=stepX) {
				for (int y=0; y<maxY; y+=stepY) {
					Roi tempRoi = new Roi(x, y, stepX, stepY);
					image.setRoi(tempRoi);
					ip.setRoi(tempRoi);
					int lastSlice = (z+stepZ < maxZ) ? z+stepZ : maxZ;
					ImagePlus imgblock = new Duplicator().run(image,z+1,lastSlice);
					String fileout = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,(z+z_offset),y,x);
					IJ.saveAsTiff(imgblock,fileout);
					IJ.showProgress(z, maxZ);
					IJ.log("saved "+fileout);
					imgblock = null;
				}
			}
		}
		IJ.showStatus("Blocks have been saved!");
		IJ.showProgress(100, 100);
	}

}
