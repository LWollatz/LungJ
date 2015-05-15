import java.io.IOException;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class Concatenate_3D implements PlugIn{
	
	private static String BC_inDirectory = "J:\\Biomedical Imaging Unit\\Research\\Research temporary\\3D IfLS Lung Project\\temp\\20150324_IfLS_Segmentation\\output";
	private static int maxX = 1;
	private static int maxY = 1;
	private static int maxZ = 1;
	private static int stepX = 1;
	private static int stepY = 1;
	private static int stepZ = 1;
	private static double globMax = 1;
	private static double globMin = 0;
	
	
	public void run(String command){
		IJ.showStatus("Getting data...");
		IJ.showProgress(0, 100);
		GenericDialog gd = new GenericDialog(command+" Subdivide image and save into directory");
		gd.addStringField("Input directory", BC_inDirectory, 100);
		IJ.showStatus("Waiting for user input...");
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showStatus("Plug In cancelled...");
			IJ.showProgress(100, 100);
        	return;
        }
		IJ.showStatus("Getting data...");
		BC_inDirectory = gd.getNextString();
		
		Properties prefs = new Properties();
		try {
			prefs = LJPrefs.readProperties(BC_inDirectory + "\\properties.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		maxX = LJPrefs.getPref(prefs, "maxX", maxX);
		maxY = LJPrefs.getPref(prefs, "maxY", maxY);
		maxZ = LJPrefs.getPref(prefs, "maxZ", maxZ);
		stepX = LJPrefs.getPref(prefs, "stepX", stepX);
		stepY = LJPrefs.getPref(prefs, "stepY", stepY);
		stepZ = LJPrefs.getPref(prefs, "stepZ", stepZ);
		globMin = (float)LJPrefs.getPref(prefs, "minVal", globMin);
		globMax = (float)LJPrefs.getPref(prefs, "maxVal", globMax);
		
		ImagePlus imgout = null;
		IJ.showStatus("Concatenating blocks...");
		IJ.showProgress(0, 100);
		int prog = 0;
		int diffz = (int)Math.round(maxZ/stepZ+0.5);
		int diffx = (int)Math.round(maxX/stepX+0.5);
		int diffy = (int)Math.round(maxY/stepY+0.5);
		int maxProg = (diffy*diffx*diffz)*100/99;
		
		for (int z=0; z<maxZ; z+=stepZ) {
			for (int x=0; x<maxX; x+=stepX) {
				for (int y=0; y<maxY; y+=stepY) {
					String filein = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z,y,x);
					ImagePlus imgblock = IJ.openImage(filein);
					if (imgout == null){
						int bd = imgblock.getProcessor().getBitDepth();
						imgout = IJ.createImage("Result", maxX, maxY, maxZ, bd);
					}
					imgout = paste(imgblock, imgout, x, y, z);
					IJ.log("added "+filein);
					IJ.showStatus("Concatenating blocks...");
					prog += 1;
					IJ.showProgress(prog, maxProg);
					IJ.log(prog+"/"+maxProg);
					imgblock = null;
				}
			}
		}
		
		
		IJ.showProgress(90, 100);
    	//IJ.setThreshold(globMin,globMax,"BLACK_AND_WHITE_LUT");
		imgout.show();
		IJ.setMinAndMax(globMin, globMax);
		IJ.showStatus("Concatenated blocks");
		IJ.showProgress(100, 100);
		
	}
	
	private ImagePlus paste(ImagePlus img, ImagePlus destimg, int px, int py, int pz){
		ImageProcessor ipo = destimg.getProcessor();
		
		int oWidth = destimg.getWidth();
		
		int iHeight = img.getHeight();
		int iWidth = img.getWidth();
		int iDepth = img.getNSlices();
		
		if (ipo.getBitDepth() == 32) {
			for (int z=pz; z<pz+iDepth; z++) {
				float[] oPixels = (float[])destimg.getStack().getProcessor(z+1).getPixels();
				float[] iPixels = (float[])img.getStack().getProcessor(z-pz+1).getPixels();
				for (int y=py; y<py+iHeight; y++){
					for (int x=px; x<px+iWidth; x++){
						int po=x+y*oWidth;
						int pi=(x-px)+(y-py)*iWidth; 
	    				oPixels[po] = iPixels[pi];
					}
	    		}
			}
		}else if (ipo.getBitDepth() == 16) {
			for (int z=pz; z<pz+iDepth; z++) {
				short[] oPixels = (short[])destimg.getStack().getProcessor(z+1).getPixels();
				short[] iPixels = (short[])img.getStack().getProcessor(z-pz+1).getPixels();
				for (int y=py; y<py+iHeight; y++){
					for (int x=px; x<px+iWidth; x++){
						int po=x+y*oWidth;
						int pi=(x-px)+(y-py)*iWidth; 
	    				oPixels[po] = iPixels[pi];
					}
	    		}
			}
		}else  if (ipo.getBitDepth() == 8) {
			for (int z=pz; z<pz+iDepth; z++) {
				byte[] oPixels = (byte[])destimg.getStack().getProcessor(z+1).getPixels();
				byte[] iPixels = (byte[])img.getStack().getProcessor(z-pz+1).getPixels();
				for (int y=py; y<py+iHeight; y++){
					for (int x=px; x<px+iWidth; x++){
						int po=x+y*oWidth;
						int pi=(x-px)+(y-py)*iWidth; 
	    				oPixels[po] = iPixels[pi];
					}
	    		}
			}
		}
		return destimg;
		
	}
	
}
