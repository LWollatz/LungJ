package lj.testing;

import java.io.IOException;
import java.util.Properties;

import lj.LJPrefs;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;


public class Test_Halo_Highlight implements PlugIn{
		/** plugin's name */
		public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
		/** plugin's current version */
		public static final String PLUGIN_VERSION = LJPrefs.VERSION;
		private static String BC_inDirectory = LJPrefs.LJ_inpDirectory;
		private static String BC_outDirectory = LJPrefs.LJ_outDirectory;
		
		private static int maxX = 1;
		private static int maxY = 1;
		private static int maxZ = 1;
		private static int stepX = 1;
		private static int stepY = 1;
		private static int stepZ = 1;
		private static int haloX = 0;
		private static int haloY = 0;
		private static int haloZ = 0;
		private static float globMaxIn = -Float.MAX_VALUE;
		private static float globMinIn = Float.MAX_VALUE;
		private static int errCount = 0;
		
		
		
		public void run(String command){
			
			
			GenericDialog gd = new GenericDialog(command+" Run macro on 3D blocks");
			gd.addStringField("Input directory", BC_inDirectory, 100);
			gd.addStringField("Output directory", BC_outDirectory, 100);
			gd.showDialog();
			if (gd.wasCanceled()){
	        	return;
	        }
			BC_inDirectory = gd.getNextString();
			LJPrefs.LJ_inpDirectory = BC_inDirectory;
			BC_outDirectory = gd.getNextString();
			LJPrefs.LJ_outDirectory = BC_outDirectory;
			
			LJPrefs.savePreferences(); //save preferences for after Fiji restart.
			
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
			haloX = LJPrefs.getPref(prefs, "haloX", haloX);
			haloY = LJPrefs.getPref(prefs, "haloY", haloY);
			haloZ = LJPrefs.getPref(prefs, "haloZ", haloZ);
			globMinIn = (float)LJPrefs.getPref(prefs, "minVal", globMinIn);
			globMaxIn = (float)LJPrefs.getPref(prefs, "maxVal", globMaxIn);
			
			//ImagePlus imgin = null;
			ImagePlus imgout = null;
			float globMax = -Float.MAX_VALUE;
			float globMin = Float.MAX_VALUE;
			errCount = 0;
			
			for (int z=0; z<maxZ; z+=stepZ) {
				IJ.showProgress(z, maxZ);
				for (int x=0; x<maxX; x+=stepX) {
					for (int y=0; y<maxY; y+=stepY) {
						/*** open input **/
						String filein = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z,y,x);
						String filename = String.format("%1$04d_%2$04d_%3$04d.tif",z,y,x);
						/*** processing **/
						IJ.log(filename);
						imgout = IJ.openImage(filein);
						int bWidth = imgout.getWidth();
						int bHeight = imgout.getHeight();
						int bDepth = imgout.getNSlices();
						int xs = stepX+2*haloX;
						int ys = stepY+2*haloY;
						int zs = stepZ+2*haloZ;
						int x1 = haloX;
						if (x-haloX < 0){x1 = 0; xs = xs - haloX;}
						int y1 = haloY;
						if (y-haloY < 0){y1 = 0; ys = ys - haloY;}
						int z1 = haloZ;
						if (z-haloZ < 0){z1 = 0; zs = zs - haloZ;}
						int x2 = bWidth - haloX;
						if (x + xs > maxX){x2 = bWidth;}
						int y2 = bHeight - haloY;
						if (y + ys > maxY){y2 = bHeight;}
						int z2 = bDepth - haloZ;
						if (z + zs > maxZ){z2 = bDepth;}

						for (int zb=1; zb<=bDepth; zb++){
							ImageProcessor outputIP = imgout.getStack().getProcessor(zb);
							short[] oPixels = (short[])outputIP.getPixels();
							
							for (int yb=0; yb<bHeight; yb++){
					    		for (int xb=0, p=xb+yb*bWidth; xb<bWidth; xb++,p++){
					    			if (yb<y1 ||  yb>=y2 || xb<x1 ||  xb>=x2 || zb<=z1 ||  zb>z2){
										oPixels[p] = (short)(60000);
					    			}
					    		}
							}
							outputIP.setPixels(oPixels);
						}
						
						
						
						
						
						
						
							float[] minmax = LJPrefs.getMinMax(imgout);
							float curMin = (float)minmax[0];
							float curMax = (float)minmax[1];
							if (curMax>globMax) {
								globMax = curMax; 
							}
							if (curMin<globMin) {
								globMin = curMin;
							}
							/*** saving output **/
							String fileout = String.format("%1$s\\%2$s",BC_outDirectory,filename);
							IJ.saveAsTiff(imgout,fileout);
							IJ.log("processed "+filein);
							imgout.close();
						
						imgout = null;
					}
				}
			}
			
			if (errCount > 0){
				IJ.error(String.format("%1$s files failed",errCount));
			}
			
			
			IJ.showProgress(99, 100);
			prefs = new Properties();
			prefs.put("maxX", Double.toString(maxX));
			prefs.put("maxY", Double.toString(maxY));
			prefs.put("maxZ", Double.toString(maxZ));
			prefs.put("stepX", Double.toString(stepX));
			prefs.put("stepY", Double.toString(stepY));
			prefs.put("stepZ", Double.toString(stepZ));
			prefs.put("haloX", Double.toString(haloX));
			prefs.put("haloY", Double.toString(haloY));
			prefs.put("haloZ", Double.toString(haloZ));
			prefs.put("minVal", Double.toString(globMin));
			prefs.put("maxVal", Double.toString(globMax));
			try {
				LJPrefs.writeProperties(prefs, BC_outDirectory + "\\properties.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			

			IJ.showProgress(100, 100);
			
		}
		
		
		
}

