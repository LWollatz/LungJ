import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import java.io.IOException;
import java.util.Properties;

import lj.LJPrefs;

/*** Run_Macro_3D
 * runs a macro for each 3D block in a directory and saves the resulting image blocks to 
 * a new directory.
 * <code>run("3D Blocks - Run Macro", "input=[C:\\myblocks\\original] output=[C:\\myblocks\\threshold] text1=[var dirin = 'C:\\\\myblocks\\\\original'; open(dirin+'\\\\'+filename); run('Apply Binary Threshold', 'threshold=30 minimum='+globMin+' maximum='+globMax+' stack'); run('8-bit'); ]");</code>
 * 
 * - A directory with image blocks is required as created by Subdivide_3D or by a 
 *   previous run of this function.
 * - Provide an input and an output directory. Global image information will be read from
 *   the input directory and resulting images will be saved into the output directory.
 * - Provide a macro that will be run for each block. The macro will have the filename 
 *   defined as a variable but the image will not be opened automatically, to safe 
 *   memory. At the end of the macro the output image has to be the active image. It is 
 *   advised not to have any other images open.
 * - The function will run the macro for each block, adapting the variable defining the 
 *   image filename for each run and write the active image to the output directory after
 *   each run. It will create a new properties file with the global properties in the 
 *   output directory.
 * - If the macro fails, the process will be continued, but potential error messages have
 *   to be confirmed by the user and can cause the function to pause. There is a report 
 *   in the end, stating the number of files that failed if any and details can be found 
 *   in the log.
 * 
 * @author Lasse Wollatz
 ***/
public class Run_Macro_3D implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	private static String BC_inDirectory = LJPrefs.LJ_inpDirectory;
	private static String BC_outDirectory = LJPrefs.LJ_outDirectory;
	//private static String code = "var dirin = " + BC_inDirectory + ";\n open(dirin+'\\'+filename);\n run('Create Threshold Mask', 'threshold=55 minimum='+globMin+' maximum='+globMax+' stack');";
	private static String code = "var dirin = '" + BC_inDirectory.replace("\\", "\\\\") + "';\n open(dirin+'\\\\'+filename);\n";
	
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
	
	
	/*** run
     * 
     * @param  command        String 
     * 
     * @see    #process
     * @see    LJPrefs#savePreferences
     * @see    LJPrefs#readProperties
     * @see    LJPrefs#getPref
     * @see    LJPrefs#writeProperties
     * @see    ij.gui.GenericDialog
     ***/
	public void run(String command){
		//TODO: fix macro recorder to record escape characters correctly over three levels
		
		/**sample Apply Classifier**/
		//code = "var dirin = '" + BC_inDirectory.replace("\\", "\\\\") + "';\n";
		//code += "run('Apply Weka Classifier',' filepath=['+dirin+'\\\\'+filename+'] classifier=["+LJPrefs.LJ_clsDirectory.replace("\\", "\\\\")+"\\\\vessels.model] class=[2]');\n";
		/**sample Threshold**/
		//code += "run('Apply Binary Threshold', 'threshold=30 minimum='+globMin+' maximum='+globMax+' stack');\n";
		/**sample Colorize**/
		//code += "run('Colorize ',' image=['+filename+'] color1=[#000000]');\n";
		/**sample full run**/
		//code = "var dirin = '" + BC_inDirectory.replace("\\", "\\\\") + "';\n";
		//code += "run('Apply Weka Classifier',' filepath=['+dirin+'\\\\'+filename+'] classifier=["+LJPrefs.LJ_clsDirectory.replace("\\", "\\\\")+"\\\\vessels.model] class=[2]');\n";
		//code += "run('Apply Binary Threshold', 'threshold=30 minimum=0 maximum=1 stack');\n";
		//code += "rename('mask');\n";
		//code += "open(dirin+'\\\\'+filename);\n";
		//code += "run('Apply Mask', 'image='+filename+' mask=mask');";
		//code += "close(filename);\n";
		//code += "close('mask');\n";
		/**sample generation from settings**/
		code = "var dirin = '"+BC_inDirectory.replace("\\","\\\\")+"';\n";
    	if (LJPrefs.LJ_makeMap){
    		code += "var dircls = '"+LJPrefs.LJ_clsDirectory.replace("\\","\\\\")+"';\n";
    		code += "run('Apply Weka Classifier',' filepath=['+dirin+'\\\\'+filename+'] classifier=['+dircls+'] class=[2]');\n";
    	}
    	if (LJPrefs.LJ_makeMask){
    		if(!LJPrefs.LJ_makeMap){
    			code += "open(dirin+'\\\\'+filename);\n";
    			code += "run('Apply Binary Threshold', 'threshold="+(LJPrefs.LJ_threshold*100)+"  minimum='+globMin+' maximum='+globMax+' stack');\n";
    		}else{
    			code += "run('Apply Binary Threshold', 'threshold="+(LJPrefs.LJ_threshold*100)+"  minimum=0 maximum=1 stack');\n";
    		}
    		code += "run('8-bit');\n";
    		code += "run('Multiply...', 'value=255 stack');\n";
    	}
    	
    	/** create dialog to request values from user: **/
		GenericDialog gd = new GenericDialog(command+" Run macro on 3D blocks");
		gd.addStringField("Input directory", BC_inDirectory, 100);
		gd.addStringField("Output directory", BC_outDirectory, 100);
		gd.addMessage("Macrocode (provides variables filename, globMin and globMax)");
		gd.addTextAreas(code, null, 10, 100);
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
        	return;
        }
		BC_inDirectory = gd.getNextString();
		BC_outDirectory = gd.getNextString();
		code = gd.getNextText();
		/** values from user dialog extracted **/
		
		/** save preferences for after Fiji restart: **/
		LJPrefs.LJ_inpDirectory = BC_inDirectory;
		LJPrefs.LJ_outDirectory = BC_outDirectory;
		LJPrefs.savePreferences();
		
		/** read image properties **/
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
					//imgin = IJ.openImage(filein);
					//int bd = imgin.getProcessor().getBitDepth();
					//imgout = IJ.createImage("Result", maxX, maxY, maxZ, bd);
					/*** processing **/
					
					IJ.log(filename);
					String fullcode = " var filename = '" + filename + "';\n var globMin = " + globMinIn + ";\n var globMax = " + globMaxIn + ";\n" + code;
					
					imgout = process(fullcode);
					
					if (imgout == null){
						errCount += 1;
						IJ.log("failed "+filein);
					}else{
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
					}
					//imgin = null;
					imgout = null;
				}
			}
		}
		
		if (errCount > 0){
			IJ.error(String.format("%1$s files failed",errCount));
		}
		IJ.showProgress(99, 100);
		
		/** save image properties **/
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
	
	/*** process
     * executes macro code and returns the top image
     * 
     * @param  code                String with macro to execute
     * @return                     ImagePlus
     ***/
	private ImagePlus process(String code){
		//ImagePlus imgin, String code
		//ImageProcessor ipi = imgin.getProcessor();
		//imgin.show();
		//TODO: how can I intercept macro errors?
		IJ.runMacro(code);
		ImagePlus imgout = WindowManager.getCurrentImage();
		return imgout;
		
	}
	
}
