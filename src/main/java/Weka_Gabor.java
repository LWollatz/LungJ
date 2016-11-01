import trainableSegmentation.FeatureStack;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/*** Gabor_
 * implements the TWS Gabor filter
 * This class has been copied from the TWS and adapted for independent use. (NOT FULLY FUNCTIONAL!)
 * @author Trainable WEKA Segmentation
 ***/
public class Weka_Gabor implements PlugIn{
	
	ImagePlus originalImage = null;
	int width = 0;
	int height = 0;
	
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
		
		gd.addNumericField("sigma", 1, 2);
		gd.addNumericField("gamma", 8, 2);
		gd.addNumericField("psi", 8, 2);
		gd.addNumericField("frequency", 8, 2);
		gd.addNumericField("nAngles", 8, 2);

		gd.showDialog();
		if ( gd.wasCanceled() )
			return;
		
		
		double sigma = (double) gd.getNextNumber();
		double gamma = (double) gd.getNextNumber();
		double psi = (double) gd.getNextNumber();
		double frequency = (double) gd.getNextNumber();
		int nAngles = (int) gd.getNextNumber();
		

		FeatureStack WekaFeature = new FeatureStack(imp);
		WekaFeature.addGabor(originalImage,sigma, gamma, psi, frequency, nAngles);
		WekaFeature.show();
		
		imp = WindowManager.getCurrentImage();
		imp.setTitle("Gabor");
		
		return;
	}
	
	
}
