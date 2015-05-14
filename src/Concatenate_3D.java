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
	
	public void run(String command){
		GenericDialog gd = new GenericDialog(command+" Subdivide image and save into directory");
		gd.addStringField("Input directory", BC_inDirectory, 100);
		gd.showDialog();
		if (gd.wasCanceled()){
        	return;
        }
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
		
		
		ImagePlus imgout = null;
		//ImageProcessor ip = imgout.getProcessor();
		
		for (int z=0; z<maxZ; z+=stepZ) {
			//ip.setSliceNumber(z);
			IJ.showProgress(z, maxZ);
			//run("Image Sequence...", "open=["+stackdirectory+"] number=250 file=DigiSens_ sort");
			for (int x=0; x<maxX; x+=stepX) {
				for (int y=0; y<maxY; y+=stepY) {
					//Roi tempRoi = new Roi(x, y, stepX, stepY);
					//imgout.setRoi(tempRoi);
					//ip.setRoi(tempRoi);
					//int lastSlice = (z+stepZ < maxZ) ? z+stepZ : maxZ;
					//ImagePlus imgblock = new Duplicator().run(image,z+1,lastSlice);
					String filein = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z,y,x);
					IJ.log("opening "+filein);
					ImagePlus imgblock = IJ.openImage(filein);
					
					if (imgout == null){
						int bd = imgblock.getProcessor().getBitDepth();
						imgout = IJ.createImage("Result", maxX, maxY, maxZ, bd);
					}
					
					imgout = paste(imgblock, imgout, x, y, z);
					IJ.log("added "+filein);
					imgblock = null;
				}
			}
		}
		
		
		IJ.showProgress(99, 100);
		imgout.show();
		IJ.showProgress(100, 100);
		
	}
	
	private ImagePlus paste(ImagePlus img, ImagePlus destimg, int px, int py, int pz){
		ImageProcessor ipo = destimg.getProcessor();
		ImageProcessor ipi = img.getProcessor();
		
		IJ.log("placing at "+px+","+py+","+pz);
		int oHeight = destimg.getHeight();
		int oWidth = destimg.getWidth();
		int oDepth = destimg.getNSlices();
		
		int tHeight = img.getHeight();
		int tWidth = img.getWidth();
		int tDepth = img.getNSlices();
		
		if (ipo.getBitDepth() == 32) {
			for (int z=pz; z<pz+tDepth; z++) {
				ipo.setSliceNumber(z+1);
				IJ.log(""+(z-pz+1));
				float[] oPixels = (float[])destimg.getStack().getProcessor(z+1).getPixels();
				float[] iPixels = (float[])img.getStack().getProcessor(z-pz+1).getPixels();
				for (int y=py; y<py+tHeight; y++){
					for (int x=px; x<px+tWidth; x++){
						int po=x+y*oWidth;
						int pi=(x-px)+(y-py)*tWidth; 
	    				oPixels[po] = iPixels[pi];
					}
	    		}
			}
		}else if (ipo.getBitDepth() == 16) {
			for (int z=pz; z<pz+tDepth; z++) {
				ipo.setSliceNumber(z+1);
				IJ.log(""+(z-pz+1));
				short[] oPixels = (short[])destimg.getStack().getProcessor(z+1).getPixels();
				short[] iPixels = (short[])img.getStack().getProcessor(z-pz+1).getPixels();
				for (int y=py; y<py+tHeight; y++){
					for (int x=px; x<px+tWidth; x++){
						int po=x+y*oWidth;
						int pi=(x-px)+(y-py)*tWidth; 
	    				oPixels[po] = iPixels[pi];
					}
	    		}
			}
		}else  if (ipo.getBitDepth() == 8) {
			for (int z=pz; z<pz+tDepth; z++) {
				ipo.setSliceNumber(z+1);
				IJ.log(""+(z-pz+1));
				byte[] oPixels = (byte[])destimg.getStack().getProcessor(z+1).getPixels();
				byte[] iPixels = (byte[])img.getStack().getProcessor(z-pz+1).getPixels();
				for (int y=py; y<py+tHeight; y++){
					for (int x=px; x<px+tWidth; x++){
						int po=x+y*oWidth;
						int pi=(x-px)+(y-py)*tWidth; 
	    				oPixels[po] = iPixels[pi];
					}
	    		}
			}
		}
		return destimg;
		
	}
	
}
