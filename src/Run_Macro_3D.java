import java.io.IOException;
import java.util.Properties;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.gui.Roi;
import ij.plugin.Duplicator;
import ij.plugin.PlugIn;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;


public class Run_Macro_3D implements PlugIn{
	
	private static String BC_inDirectory = "J:\\Biomedical Imaging Unit\\Research\\Research temporary\\3D IfLS Lung Project\\temp\\20150324_IfLS_Segmentation\\input";
	private static String BC_outDirectory = "J:\\Biomedical Imaging Unit\\Research\\Research temporary\\3D IfLS Lung Project\\temp\\20150324_IfLS_Segmentation\\output";
	//private static String code = "var dirin = " + BC_inDirectory + ";\n open(dirin+'\\'+filename);\n run('Create Threshold Mask', 'threshold=55 minimum='+globMin+' maximum='+globMax+' stack');";
	private static String code = "var dirin = '" + BC_inDirectory.replace("\\", "\\\\") + "';\n open(dirin+'\\\\'+filename);\n";
	
	private static int maxX = 1;
	private static int maxY = 1;
	private static int maxZ = 1;
	private static int stepX = 1;
	private static int stepY = 1;
	private static int stepZ = 1;
	private static float globMaxIn = -Float.MAX_VALUE;
	private static float globMinIn = Float.MAX_VALUE;
	private static int errCount = 0;
	
	
	
	public void run(String command){
		//sample Apply Classifier
		code = "var dirin = '" + BC_inDirectory.replace("\\", "\\\\") + "';\n";
		code += "run('Apply Weka Classifier',' filepath=['+dirin+'\\\\'+filename+'] classifier=[J:\\\\Biomedical Imaging Unit\\\\Research\\\\Research Temporary\\\\3D IfLS Lung Project\\\\temp\\\\20150324_IfLS_Segmentation\\\\run02\\\\vessels.model] class=[2]');\n";
		code += "run('Create Threshold Mask', 'threshold=30 minimum=0 maximum=1 stack');\n";
		code += "rename('mask');\n";
		code += "open(dirin+'\\\\'+filename);\n";
		code += "run('Apply Mask', 'image='+filename+' mask=mask');";
		code += "close(filename);\n";
		code += "close('mask');\n";
		
		//sample Threshold
		//code += "run('Create Threshold Mask', 'threshold=30 minimum='+globMin+' maximum='+globMax+' stack');\n";
		//sample Colorize
		//code += "run('Colorize ',' image=['+filename+'] color1=[#000000]');\n";
		
		GenericDialog gd = new GenericDialog(command+" Subdivide image and save into directory");
		gd.addStringField("Input directory", BC_inDirectory, 100);
		gd.addStringField("Output directory", BC_outDirectory, 100);
		gd.addMessage("Macrocode (provides variables filename, globMin and globMax)");
		gd.addTextAreas(code, null, 10, 100);
		gd.showDialog();
		if (gd.wasCanceled()){
        	return;
        }
		BC_inDirectory = gd.getNextString();
		BC_outDirectory = gd.getNextString();
		code = gd.getNextText();
		
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
		globMinIn = (float)LJPrefs.getPref(prefs, "minVal", globMinIn);
		globMaxIn = (float)LJPrefs.getPref(prefs, "maxVal", globMaxIn);
		
		ImagePlus imgin = null;
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
					imgin = null;
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
	
	private ImagePlus process(String code){
		//ImagePlus imgin, String code
		//ImageProcessor ipi = imgin.getProcessor();
		
		//imgin.show();
		
		
		
		IJ.runMacro(code);
		
		ImagePlus imgout = WindowManager.getCurrentImage();
		
		
		
		return imgout;
		
	}
	
}
