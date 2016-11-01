import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import trainableSegmentation.FeatureStack;

/*** Membrane_Projections
 * implements the TWS Membrane_Projections filter
 * This filter is merely a front end for the filter from the TWS.
 * 
 * @author Lasse Wollatz
 * @see trainableSegmentation.FeatureStack#addMembraneFeatures
 ***/
public class Weka_Membrane_Projections implements PlugIn{
	int nAngles = 30;
	int width = 0;
	int height = 0;
	/** original image */
	ImagePlus originalImage = null;
	
	
	public void run(String arg){
		if (arg.equals("about")){
			showAbout(); 
			return;
		}
		
		ImagePlus imp = WindowManager.getCurrentImage();
		
		this.originalImage = imp; // Get a reference to the image.
		width = originalImage.getWidth();
		height = originalImage.getHeight();
		
		if (imp == null)
			return;

		GenericDialog gd = new GenericDialog("Matrix Operation");
		
		gd.addNumericField("membrane size", 1, 2);
		gd.addNumericField("patch size", 19, 2);

		gd.showDialog();
		if ( gd.wasCanceled() )
			return;
		
		
		int membraneSize = (int) gd.getNextNumber();
		int patchSize = (int) gd.getNextNumber();
		
		
		FeatureStack WekaFeature = new FeatureStack(imp);
		
		WekaFeature.addMembraneFeatures(patchSize,membraneSize);
		
		WekaFeature.show();
		
		imp = WindowManager.getCurrentImage();
		
		imp.setTitle("membrane stack");
		
		return;
	}
	
	

	/*** showAbout ***
     * Display filter information
     * 
     ***/
	void showAbout(){
		IJ.showMessage("Membrane Projections filter...",
				"modified from Trainable WEKA segmentationn\n");
	}

}
