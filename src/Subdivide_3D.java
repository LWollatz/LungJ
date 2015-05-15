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
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * @author Lasse Wollatz
 *
 */
public class Subdivide_3D implements PlugIn{
	
	private static String BC_outDirectory = "J:\\Biomedical Imaging Unit\\Research\\Research temporary\\3D IfLS Lung Project\\temp\\20150324_IfLS_Segmentation\\input";
	private static int stepX = 250;
	private static int stepY = 250;
	private static int stepZ = 250;
	private static int z_offset = 0;
	private double globMin = 0;
    private double globMax = 1;
	private static boolean saveProp = true;
	
	public void run(String command){
		IJ.showStatus("Creating blocks...");
		IJ.showProgress(0, 100);
		ImagePlus image = WindowManager.getCurrentImage();
		int[] properties = image.getDimensions(); //width, height, nChannels, nSlices, nFrames
		int maxX = properties[0];
		int maxY = properties[1];
		int maxZ = properties[3];
		int bits = image.getBitDepth();
		float[] minmax = new float[2];
    	minmax = LJPrefs.getMinMax(image);
		globMin = (double)minmax[0];
    	globMax = (double)minmax[1];
		
		GenericDialog gd = new GenericDialog(command+" Subdivide image and save into directory");
		gd.addStringField("Output directory", BC_outDirectory, 100);
		FileFilter filter = new FileNameExtensionFilter("Weka Classifier", "model");
		JFileChooser chooser = new JFileChooser(LJPrefs.LJ_clsDirectory);
		chooser.addChoosableFileFilter(filter);
		gd.add(chooser);
		gd.addMessage("filename will be 'z'_'y'_'x'.tif");
		gd.addMessage("Block Properties:");
		gd.addNumericField("width", stepX, 0);
		gd.addNumericField("height", stepY, 0);
		gd.addNumericField("depth", stepZ, 0);
		gd.addMessage("Other Settings:");
		gd.addNumericField("z-offset", z_offset, 0, 4,  "(affects filename only)");
		gd.addCheckbox("save LungJ header", saveProp);
		IJ.showStatus("Waiting for User Input...");
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showProgress(100, 100);
        	return;
        }
		
		IJ.showStatus("Creating blocks...");
		IJ.showProgress(1, 100);
		
		BC_outDirectory = gd.getNextString();
		stepX = (int)gd.getNextNumber();
		stepY = (int)gd.getNextNumber();
		stepZ = (int)gd.getNextNumber();
		z_offset = (int)gd.getNextNumber();
		saveProp = gd.getNextBoolean();
		
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
					IJ.showStatus("Creating blocks...");
					IJ.showProgress(y+maxY*x+maxY*maxX*z, maxY*maxX*maxZ);
					IJ.log("saved "+fileout);
					imgblock = null;
				}
			}
		}
		
		if (saveProp){
			Properties prefs = new Properties();
			prefs.put("maxX", Double.toString(maxX));
			prefs.put("maxY", Double.toString(maxY));
			prefs.put("maxZ", Double.toString(maxZ+z_offset));
			prefs.put("stepX", Double.toString(stepX));
			prefs.put("stepY", Double.toString(stepY));
			prefs.put("stepZ", Double.toString(stepZ));
			prefs.put("minVal", Double.toString(globMin));
			prefs.put("maxVal", Double.toString(globMax));
			try {
				LJPrefs.writeProperties(prefs, BC_outDirectory + "\\properties.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		IJ.showStatus("Blocks have been saved!");
		IJ.showProgress(100, 100);
	}
	
	

}
