import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

import trainableSegmentation.FeatureStack;

/*** Neighbors_
 * implements the TWS Neighbors_ filter
 * This class has been copied from the TWS and adapted for independent use.
 * 
 * @author Trainable WEKA Segmentation
 * @author iarganda
 * @author ctrueden
 * @author m-ezzat
 * @author dscho
 * 
 * @author Lasse Wollatz
 ***/
public class Weka_Neighbors implements PlugIn{
	
	ImagePlus originalImage = null;
	int width = 0;
	int height = 0;
	
	/*** run
	* @see trainableSegmentation.FeatureStack#addNeighbors
	***/
	public void run(String arg){
		if (arg.equals("about"))
		{
			//TODO: showAbout(); 
			return;
		}
		
		ImagePlus imp = WindowManager.getCurrentImage();
		
		this.originalImage = imp; // Get a reference to the image.
		width = originalImage.getWidth();
		height = originalImage.getHeight();
		
		if (imp == null)
			return;

		GenericDialog gd = new GenericDialog("Matrix Operation");
		
		gd.addNumericField("minSigma", 1, 2);
		gd.addNumericField("maxSigma", 8, 2);

		gd.showDialog();
		if ( gd.wasCanceled() )
			return;
		
		int minSigma = (int) gd.getNextNumber();
		int maxSigma = (int) gd.getNextNumber();
		
		FeatureStack WekaFeature = new FeatureStack(imp);
		WekaFeature.addNeighbors(minSigma,maxSigma);
		WekaFeature.show();
		
		imp = WindowManager.getCurrentImage();
		imp.setTitle("Neighbors");
		
		return;
	}
	
}
