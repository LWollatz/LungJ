
import lj.LJPrefs;
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/*** Matrix_Operation
 * implements a filter that applies a matrix onto an image
 * 
 * @author Lasse Wollatz
 ***/
public class Matrix_Operation implements PlugInFilter{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = Compare_Masks.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;

	/** original image **/
	ImagePlus origImg = null;
	/** radius to use (in pixels) **/
	int radius = 2;
	/** number of bins to use in the histogram **/
	int numBins = 256;
	/** matrix to apply to the image**/
	double[][] matrix;

	private int flags = DOES_8G|DOES_16|DOES_STACKS|PARALLELIZE_STACKS;

	/***
	 * Setup method
	 * @param arg filter arguments
	 * @param imp input image
	 ***/
	public int setup(String arg, ImagePlus imp){
		if (arg.equals("about"))
		{
			showAbout(); 
			return DONE;
		}

		this.origImg = imp; // Get a reference to the image.
		if (imp == null)
			return DONE;

		/** create dialog to request values from user: **/
		GenericDialog gd = new GenericDialog("Matrix Operation");
		//gd.addNumericField("dimensions", 2, 1);
		gd.addTextAreas("[[-1.0,0.0,1.0],[-2.0,0.0,2.0],[-1.0,0.0,1.0]]", null, 10, 40);

		gd.showDialog();
		if ( gd.wasCanceled() )
			return DONE;

		String tempstr = gd.getNextText();
		IJ.log(tempstr);
		String[] items = tempstr.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
		int size  = (int) Math.sqrt(items.length);

		matrix = new double[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				try {
					//IJ.log(items[i*size+j]);
					matrix[j][i] = Float.parseFloat(items[i*size+j]);
				} catch (NumberFormatException nfe) {};
			}
		}
		IJ.log(String.valueOf(matrix[0][0]) + " " + String.valueOf(matrix[1][0]) + " " + String.valueOf(matrix[2][0]));
		IJ.log(String.valueOf(matrix[0][1]) + " " + String.valueOf(matrix[1][1]) + " " + String.valueOf(matrix[2][1]));
		IJ.log(String.valueOf(matrix[0][2]) + " " + String.valueOf(matrix[1][2]) + " " + String.valueOf(matrix[2][2]));
		return flags;
	}


	public void run(ImageProcessor ip){
		//final double[][] matrix = {{-1.0,0.0,1.0},{-2.0,0.0,2.0},{-1.0,0.0,1.0}};
		


		final int matsize = matrix.length;
		final int halfsize = (matsize-1)/2;
		double tempsum = 0;
		ip.resetMinAndMax();
		final int width = ip.getWidth();

		final FloatProcessor bp = new FloatProcessor(ip.getWidth(), ip.getHeight(), (float[])ip.convertToFloat().getPixels());
		float[] bPixels = (float[])bp.getPixels();

		for(int i=halfsize; i<bp.getWidth()-halfsize; i++)
		{			
			for(int j=halfsize; j<bp.getHeight()-halfsize; j++)
			{
				//curval = bp.getPixelValue(i, j);
				//IJ.log("---");
				tempsum = 0;
				for(int x=i-halfsize, xm = 0; x<=i+halfsize; x++, xm++)
				{
					for(int y=j-halfsize, ym = 0; y<=j+halfsize; y++, ym++)
					{
						tempsum += ((double)bPixels[x+y*width])*matrix[ym][xm]; //bp.getPixelValue(x, y)*
						//IJ.log(String.valueOf(tempsum));
					}
				}
				ip.putPixelValue(i, j, tempsum );
			}
			IJ.showProgress(i-halfsize, bp.getWidth()-halfsize);
		}

		return;
	}

	/**
	 * Display filter information
	 */
	void showAbout() {
		IJ.showMessage("Matrix Operations...",
				"Convolves a matrix with an image\n"+
				"matrix has to be 2D\n");
	}

}
