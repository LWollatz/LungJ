import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.StackConverter;

import ij3d.Content;
import ij3d.Image3DUniverse;

import isosurface.MeshExporter;

import java.io.File;

import lj.LJPrefs;

/*** mask_to_stl
 * converts a mask into an stl file using the 3D Viewer.
 * Good for 3D printing...
 * 
 * @author Lasse Wollatz
 * 
 * @see    <a href="https://imagej.net/3D_Viewer">3D Viewer</a>
 * @see    <a href="https://github.com/fiji/3D_Viewer">3D Viewer (code)</a>
 ***/

public class mask_to_stl implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = Compare_Masks.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	private static String BC_outDirectory = LJPrefs.LJ_srcDirectory;

	public void run(String command){
		//generate error message for older versions:
		if (IJ.versionLessThan("1.48n"))
			return;
		IJ.showStatus("Select Mask...");

		//get available images:
		int Nimg = WindowManager.getImageCount();
		/*
		if (Nimg < 1){
			IJ.error("At least one image is required - the original and the mask.");
			return;
		}*/
		String[] lstImages = WindowManager.getImageTitles();
		int[] lstImageIds = WindowManager.getIDList();
		String[] lstMasks = new String[Nimg];
		int Nmasks = Nimg;
		for (int i = 0; i < Nimg; i++){
			ImageProcessor temp = WindowManager.getImage(lstImageIds[i]).getProcessor();
			if((temp.getMin() == 0 && temp.getMax() == 1) || temp.isBinary()){
				lstMasks[i] = lstImages[i];
			}else{
				lstMasks[i] = "--not a mask--";
				Nmasks--;
			}
		}
		if (Nmasks < 1){
			IJ.error("Could not find valid mask. (A mask needs to be binary, or only contain values between 0 and 1)");
			return;
		}
		/** create dialog to request values from user: **/
		GenericDialog gd = new GenericDialog(command+" Save mask as STL");
		gd.addChoice("Mask", lstMasks, lstMasks[0]);
		gd.addStringField("Output directory", BC_outDirectory);
		if (IJ.getVersion().compareTo("1.42p")>=0)
			gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
			return;
		}
		int AM_maskID = lstImageIds[gd.getNextChoiceIndex()];
		String fileoutname = gd.getNextString();
		/** values from user dialog extracted **/
		ImagePlus mask = WindowManager.getImage(AM_maskID);
		new StackConverter(mask).convertToGray8();


		/** open mask in 3D Viewer **/
		Image3DUniverse univ = new Image3DUniverse();
		univ.show();
		//univ.addMesh(mask);
		//univ.addMesh(mask, null, "somename", 50, new boolean[] {true, true, true}, 2);
		Content c = univ.addVoltex(mask);

		/** convert volume to surface **/
		c.displayAs(Content.SURFACE);
		IJ.showStatus("Mask converted.");

		/** save as STL **/
		//call("ij3d.ImageJ3DViewer.exportContent", "STL Binary", "filepath\\testtemp.stl");
		//ImageJ3DViewer.exportContent("STL Binary", fileoutname);
		MeshExporter.saveAsSTL(univ.getContents(), new File(fileoutname),MeshExporter.BINARY);
		IJ.showStatus("STL saved.");
	}
}
