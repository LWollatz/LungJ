import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.io.IOException;
import java.util.Properties;

import lj.LJPrefs;

/*** Concatenate_3D
 * combines 3D blocks in a directory into a single image.
 * <code>run("3D Blocks - Concatenate", 
 * "input=[C:\\myblocks\\threshold]");</code>
 * 
 * - Ensure that all the image-blocks are inside the directory and
 *   that the `properties.txt’ file is available.
 * - Call Concatenate_3D and provide the full path to the directory.
 * - The function will then load the blocks one after the other,
 *   stitch them together into one image and display the output.
 * 
 * @author Lasse Wollatz  
 ***/

public class Concatenate_3D implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = Concatenate_3D.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	private static String BC_inDirectory = LJPrefs.LJ_outDirectory;
	private static int maxX = 1;
	private static int maxY = 1;
	private static int maxZ = 1;
	private static int stepX = 1;
	private static int stepY = 1;
	private static int stepZ = 1;
	private static int haloX = 0;
	private static int haloY = 0;
	private static int haloZ = 0;
	private static double globMax = 1;
	private static double globMin = 0;
	
	/*** run
     * 
     * @param  command        String 
     * 
     * @see    #paste
     * @see    LJPrefs#readProperties
     * @see    LJPrefs#getPref
     * @see    ij.gui.GenericDialog
     ***/
	public void run(String command){
		IJ.showStatus("Getting data...");
		IJ.showProgress(0, 100);
		
		/** create dialog box **/
		GenericDialog gd = new GenericDialog(command+" Concatenate imageblocks created by subdivide");
		gd.addStringField("Input directory", BC_inDirectory, 100);
		IJ.showStatus("Waiting for user input...");
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showStatus("Plug In cancelled...");
			IJ.showProgress(100, 100);
        	return;
        }
		
		/** getting user input and reading image information from preference file **/
		IJ.showStatus("Getting data...");
		BC_inDirectory = gd.getNextString();
		Properties prefs = new Properties();
		try {
			prefs = LJPrefs.readProperties(BC_inDirectory + "\\properties.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block if no LJinfo file found
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
		
		/** loop over image blocks **/
		for (int z=0; z<maxZ; z+=stepZ) {
			for (int x=0; x<maxX; x+=stepX) {
				for (int y=0; y<maxY; y+=stepY) {
					/** read in each block **/
					String filein = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z,y,x);
					ImagePlus imgblock = IJ.openImage(filein);
					
					int bWidth = imgblock.getWidth();
					int bHeight = imgblock.getHeight();
					int bDepth = imgblock.getNSlices();
					
					
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

					
					
					if (imgout == null){
						/** create image for output **/
						int bd = imgblock.getProcessor().getBitDepth();
						imgout = IJ.createImage("Result", maxX, maxY, maxZ, bd);
					}
					if (imgout == null){
						IJ.log("ERROR "+filein);
					}else{
						/** paste block into full image **/
						imgout = paste(imgblock, imgout, x, y, z, x1, x2, y1, y2, z1, z2);
						IJ.log("added "+filein);
					}
					/** show progress **/
					IJ.showStatus("Concatenating blocks...");
					prog += 1;
					IJ.showProgress(prog, maxProg);
					IJ.log(prog+"/"+maxProg);
					imgblock = null;
				}
			}
		}
		
		
		IJ.showProgress(90, 100);
		imgout.show();
		IJ.setMinAndMax(globMin, globMax);
		IJ.showStatus("Concatenated blocks");
		IJ.showProgress(100, 100);
		
	}
	
	/*** paste
     * takes an image stack and pastes it into another image stack at the
     * given top left position
     * 
     * @param  img            ImagePlus image block to paste into canvas
     *                        image
     * @param  destimg        ImagePlus destination image or canvas to
     *                        past image block into.
     * @param  px             int x coordinate of destimg where x=0 of img
     *                        is
     * @param  py             int y coordinate of destimg where y=0 of img
     *                        is
     * @param  pz             int z coordinate of destimg where z=0 of img
     *                        is
     * 
     * @return                ImagePlus image canvas with img pasted into
     ***/
	private ImagePlus paste(ImagePlus img, ImagePlus destimg, int px, int py, int pz){
		ImageProcessor ipo = destimg.getProcessor();
		
		int oWidth = destimg.getWidth();
		
		int iHeight = img.getHeight();
		int iWidth = img.getWidth();
		int iDepth = img.getNSlices();
		
		
		/** different handler depending on bitdepth required... **/
		if (ipo.getBitDepth() == 32) {
			for (int z=pz; z<pz+iDepth; z++) {
				/** get pixel handler **/
				float[] oPixels = (float[])destimg.getStack().getProcessor(z+1).getPixels();
				float[] iPixels = (float[])img.getStack().getProcessor(z-pz+1).getPixels();
				/** copy img into destimg pixel by pixel **/
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
	
	/*** paste
     * takes an image stack with halos and pastes it into another image stack
     * at the given top left position, ignoring the halos.
     * 
     * @param  img            ImagePlus image block to paste into <code>destimg</code>
     * @param  destimg        ImagePlus image canvas
     * @param  px             int x coordinate of destimg where x=0 of img is
     * @param  py             int y coordinate of destimg where y=0 of img is
     * @param  pz             int z coordinate of destimg where z=0 of img is
     * @param  sx1            int x coordinate of destimg where halo starts
     * @param  sx2            int x coordinate of destimg where halo ends
     * @param  sy1            int y coordinate of destimg where halo starts
     * @param  sy2            int y coordinate of destimg where halo ends
     * @param  sz1            int z coordinate of destimg where halo starts
     * @param  sz2            int z coordinate of destimg where halo ends
     * 
     * @return                ImagePlus image canvas with img pasted into
     ***/
	private ImagePlus paste(ImagePlus img, ImagePlus destimg, int px, int py, int pz, int sx1, int sx2, int sy1, int sy2, int sz1, int sz2){
		ImageProcessor ipo = destimg.getProcessor();
		
		int oWidth = destimg.getWidth();
		
		int iHeight = sy2-sy1;
		int iWidth = sx2-sx1;
		int iDepth = sz2-sz1;
		int bWidth = img.getWidth();
		
		/** different handler depending on bitdepth required... **/
		if (ipo.getBitDepth() == 32) {
			for (int z=pz; z<pz+iDepth; z++) {
				/** get pixel handler **/
				float[] oPixels = (float[])destimg.getStack().getProcessor(z+1).getPixels();
				float[] iPixels = (float[])img.getStack().getProcessor(z-pz+sz1+1).getPixels();
				/** copy img into destimg pixel by pixel **/
				for (int y=py; y<py+iHeight; y++){
					for (int x=px; x<px+iWidth; x++){
						int po=x+y*oWidth;
						int pi=(x+sx1-px)+(y+sy1-py)*bWidth; 
	    				oPixels[po] = iPixels[pi];
					}
	    		}
			}
		}else if (ipo.getBitDepth() == 16) {
			for (int z=pz; z<pz+iDepth; z++) {
				short[] oPixels = (short[])destimg.getStack().getProcessor(z+1).getPixels();
				short[] iPixels = (short[])img.getStack().getProcessor(z-pz+sz1+1).getPixels();
				for (int y=py; y<py+iHeight; y++){
					for (int x=px; x<px+iWidth; x++){
						int po=x+y*oWidth;
						int pi=(x+sx1-px)+(y+sy1-py)*bWidth; 
	    				oPixels[po] = iPixels[pi];
					}
	    		}
			}
		}else  if (ipo.getBitDepth() == 8) {
			for (int z=pz; z<pz+iDepth; z++) {
				byte[] oPixels = (byte[])destimg.getStack().getProcessor(z+1).getPixels();
				byte[] iPixels = (byte[])img.getStack().getProcessor(z-pz+sz1+1).getPixels();
				for (int y=py; y<py+iHeight; y++){
					for (int x=px; x<px+iWidth; x++){
						int po=x+y*oWidth;
						int pi=(x+sx1-px)+(y+sy1-py)*bWidth;  
	    				oPixels[po] = iPixels[pi];
					}
	    		}
			}
		}
		return destimg;
		
	}

}
