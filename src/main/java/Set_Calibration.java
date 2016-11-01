import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;
import lj.LJPrefs;

/*** Set_Calibration
 * sets the calibration parameters of an image
 * 
 * @author Lasse Wollatz  
 ***/
public class Set_Calibration implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	//TODO: future work: detect patterns in current image labeling to pre-fill textboxes, or just store user preferences
	//TODO:              handle case of too many frames more elegant 
	
	public void run(String command){
		IJ.showStatus("Labeling Hyperstack");
		ImagePlus image = WindowManager.getCurrentImage();
		
		//int tChannels = image.getNChannels();
		//int tFrames = image.getNFrames();
		int tSlices = image.getNSlices();
		int tWidth = image.getWidth();
		int tHeight = image.getHeight();
		float[] minmax = new float[2];
    	minmax = LJPrefs.getMinMax(image);
    	float GlobalMin, GlobalMax;
    	if(image.getProcessor().getBitDepth() == 8){
    		GlobalMin = 0;
    		GlobalMax = 255;
		}else if(image.getProcessor().getBitDepth() == 16){
			GlobalMin = Short.MIN_VALUE;
    		GlobalMax = Short.MAX_VALUE;
		}else if(image.getProcessor().getBitDepth() == 24){
			GlobalMin = 0;
    		GlobalMax = 255;
		}else{
			GlobalMin = Float.MIN_VALUE;
    		GlobalMax = Float.MAX_VALUE;
		}
		
		Calibration cal = image.getCalibration(); 
		boolean isCalibrated = cal.calibrated();
		
		float[] calTable = cal.getCTable();
		float cTb = 0;
		float cTm = 1;
		float cTvs = 0;
		float cTve = 1;
		if(calTable != null){
			cTvs = calTable[0];
			cTve = calTable[calTable.length-1];
			cTm = (cTvs-cTve)/(GlobalMin-GlobalMax);
			cTb = cTvs - GlobalMin*cTm;
		}
		
		/** create user dialog **/
		GenericDialog gd = new GenericDialog(command+" Set Image Calibration");
		/**  outer dimensions  **/
		if(tSlices > 1){
			gd.addMessage("Dimensions: "+tWidth+"x"+tHeight+"x"+tSlices+" px");
			gd.addNumericField("Physical_width_of_voxel", cal.pixelWidth, 5);
			gd.addNumericField("Physical_height_of_voxel", cal.pixelHeight, 5);
			gd.addNumericField("Physical_depth_of_voxel", cal.pixelDepth, 5);
			gd.addStringField("Unit_of_voxel_width", cal.getXUnit(), 5);
			gd.addStringField("Unit_of_voxel_height", cal.getYUnit(), 5);
			gd.addStringField("Unit_of_voxel_depth", cal.getZUnit(), 5);
			gd.addNumericField("X_Origin in voxel", cal.xOrigin, 5);
			gd.addNumericField("Y_Origin in voxel", cal.yOrigin, 5);
			gd.addNumericField("Z_Origin in voxel", cal.zOrigin, 5);
		}else{
			gd.addMessage("Dimensions: "+tWidth+"x"+tHeight+" px");
			gd.addNumericField("Physical_width_of_voxel", cal.pixelWidth, 5);
			gd.addNumericField("Physical_height_of_voxel", cal.pixelHeight, 5);
			gd.addStringField("Unit_of_voxel_width", cal.getXUnit(), 5);
			gd.addStringField("Unit_of_voxel_height", cal.getYUnit(), 5);
			gd.addNumericField("X_Origin in voxel", cal.xOrigin, 5);
			gd.addNumericField("Y_Origin in voxel", cal.yOrigin, 5);
		}
		/**  pixel values  **/
		gd.addMessage("Values: ["+minmax[0]+","+minmax[1]+"]");
		gd.addCheckbox("calibrated", cal.calibrated());
		gd.addNumericField("m (rescale slope)", cTm, 5);
		gd.addNumericField("b (rescale intercept)", cTb, 5);
		gd.addStringField("Unit_of_values", cal.getValueUnit(), 5);
		/**  basics  **/
		IJ.showStatus("Waiting for User Input...");
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showProgress(100, 100);
        	return;
        }
		IJ.showStatus("Setting Values");
		
		/** getting values **/
		/**  outer dimensions  **/
		if(tSlices > 1){
			cal.pixelWidth = gd.getNextNumber();
			cal.pixelHeight = gd.getNextNumber();
			cal.pixelDepth = gd.getNextNumber();
			cal.setXUnit(gd.getNextString());
			cal.setYUnit(gd.getNextString());
			cal.setZUnit(gd.getNextString());
			cal.xOrigin = gd.getNextNumber();
			cal.yOrigin = gd.getNextNumber();
			cal.zOrigin = gd.getNextNumber();
		}else{
			cal.pixelWidth = gd.getNextNumber();
			cal.pixelHeight = gd.getNextNumber();
			cal.setXUnit(gd.getNextString());
			cal.setYUnit(gd.getNextString());
			cal.xOrigin = gd.getNextNumber();
			cal.yOrigin = gd.getNextNumber();
		}
		/**  pixel values  **/
		isCalibrated = gd.getNextBoolean();
		if(isCalibrated){
			cTm = (float)gd.getNextNumber();
			cTb = (float)gd.getNextNumber();
		}
		
		if(image.getProcessor().getBitDepth() == 8){
			calTable = new float[256];
		}else if(image.getProcessor().getBitDepth() == 16){
			calTable = new float[65536];
		}else if(image.getProcessor().getBitDepth() == 24){
			calTable = new float[256];
		}
		for (int p=0; p < calTable.length; p++){
			calTable[p] = p*cTm+cTb;
		}
		cal.setCTable(calTable, gd.getNextString());
		
		
		
		
		IJ.showStatus("Labeled Hyperstack.");
	}
}
