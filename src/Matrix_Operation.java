
import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.gui.OvalRoi;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * This class implements a circular entropy filter
 * 
 */
public class Matrix_Operation implements PlugInFilter
{
	/** original image */
	ImagePlus origImg = null;
	/** radius to use (in pixels) */
	int radius = 2;
	/** number of bins to use in the histogram */
	int numBins = 256;
	
	double[][] matrix;
	
	private int flags = DOES_8G|DOES_16|DOES_32|DOES_STACKS|PARALLELIZE_STACKS;
	
	/**
	 * Main method when called as a plugin filter
	 * @param ip input image
	 */
	
	/*
	public void run(ImageProcessor ip) 
	{
		applyEntropy(ip, matrix);			
	}
	 */
	
	/**
	 * Setup method
	 * @param arg filter arguments
	 * @param imp input image
	 */
	public int setup(String arg, ImagePlus imp) 
	{
		if (arg.equals("about"))
		{
			showAbout(); 
			return DONE;
		}

		this.origImg = imp; // Get a reference to the image.
		if (imp == null)
			return DONE;

		GenericDialog gd = new GenericDialog("Matrix Operation");
		/*
		gd.addNumericField("a11", 0, 0);
		gd.addNumericField("a12", 0, 0);
		gd.addNumericField("a13", 0, 0);
		
		gd.addNumericField("a21", 0, 0);
		gd.addNumericField("a22", 1, 0);
		gd.addNumericField("a23", 0, 0);
		
		gd.addNumericField("a31", 0, 0);
		gd.addNumericField("a32", 0, 0);
		gd.addNumericField("a33", 0, 0);
		*/
		
		//gd.addNumericField("dimensions", 2, 1);
		gd.addTextAreas("[[-1.0,0.0,1.0],[-2.0,0.0,2.0],[-1.0,0.0,1.0]]", null, 10, 40);

		gd.showDialog();
		if ( gd.wasCanceled() )
			return DONE;
		
		/*
		matrix = new double[3][3];
		matrix[0][0] = (int) gd.getNextNumber();
		matrix[0][1] = (int) gd.getNextNumber();
		matrix[0][2] = (int) gd.getNextNumber();
		
		matrix[1][0] = (int) gd.getNextNumber();
		matrix[1][1] = (int) gd.getNextNumber();
		matrix[1][2] = (int) gd.getNextNumber();
		
		matrix[2][0] = (int) gd.getNextNumber();
		matrix[2][1] = (int) gd.getNextNumber();
		matrix[2][2] = (int) gd.getNextNumber();
		*/
		
		String tempstr = gd.getNextText();
		IJ.log(tempstr);
		String[] items = tempstr.replaceAll("\\[", "").replaceAll("\\]", "").split(",");
		int size  = (int) Math.sqrt(items.length);
		
		matrix = new double[size][size];
		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				try {
					IJ.log(items[i*size+j]);
					matrix[j][i] = Float.parseFloat(items[i*size+j]);
				} catch (NumberFormatException nfe) {};
			}
		}
		
		return flags;
	}

	
	
	/**
	 * Get the entropy filter version of an image
	 * @param ip input image
	 * @param radius radius to use (in pixels)
	 * @param numBins number of bins to use in the histogram
	 * @return entropy image (32-bit)
	 */
	public void run(ImageProcessor ip)
	{
		//final double[][] matrix = {{-1.0,0.0,1.0},{-2.0,0.0,2.0},{-1.0,0.0,1.0}};
		IJ.log(String.valueOf(matrix[0][0]) + " " + String.valueOf(matrix[1][0]) + " " + String.valueOf(matrix[2][0]));
		IJ.log(String.valueOf(matrix[0][1]) + " " + String.valueOf(matrix[1][1]) + " " + String.valueOf(matrix[2][1]));
		IJ.log(String.valueOf(matrix[0][2]) + " " + String.valueOf(matrix[1][2]) + " " + String.valueOf(matrix[2][2]));
		
		
		final int matsize = matrix.length;
		final int halfsize = (matsize-1)/2;
		final double matsum = 0;
		

		
		double tempsum = 0;
		
		final double log2=Math.log(2.0);
		ip.resetMinAndMax();
		//final FloatProcessor bp = (FloatProcessor) ip.convertToFloat();
		//float[] bPixels = (float[])bp.getPixels();
		//bp.setHistogramRange( 0, 255 );
		//bp.setHistogramSize( numBins );
		final int width = ip.getWidth();
		
		final FloatProcessor bp = new FloatProcessor(ip.getWidth(), ip.getHeight(), (float[])ip.convertToFloat().getPixels());
		float[] bPixels = (float[])bp.getPixels();
		
		for(int i=halfsize; i<bp.getWidth()-halfsize; i++)
		{			
			for(int j=halfsize; j<bp.getHeight()-halfsize; j++)
			{
				//curval = bp.getPixelValue(i, j);
				tempsum = 0;
				for(int x=i-halfsize, xm = 0; x<=i+halfsize; x++, xm++)
				{
					for(int y=j-halfsize, ym = 0; y<=j+halfsize; y++, ym++)
					{
						tempsum += bPixels[x+y*width]*matrix[ym][xm]; //bp.getPixelValue(x, y)*
						//IJ.log(String.valueOf(matrix[ym][xm]));
						
					}
				}
				/*final OvalRoi roi = new OvalRoi(i-radius, j-radius, size, size);				
				bp.setRoi( roi );
				final int[] histogram = bp.getHistogram(); // Get histogram from the ROI
				
				double total = 0;
				for (int k = 0 ; k < numBins ; k++ )
					total +=histogram[ k ];

				double entropy = 0;
				for (int k = 0 ; k < numBins ; k++ )
				{
					if (histogram[k]>0)
					{   
						double p = histogram[k]/total; // calculate p
		  				entropy += -p * Math.log(p)/log2;						
					}
				}*/
				//bp.getPixelValue(i, j);
				ip.putPixelValue(i, j, tempsum );
			}
		}
		
		return;
	}
	
	/**
	 * Apply entropy filter to an image
	 * @param ip input image
	 * @param radius radius to use (in pixels)
	 * @param numBins number of bins to use in the histogram
	 */
	public void applyEntropy(
			ImageProcessor ip, 
			double[][] matrix)
	{
		///final FloatProcessor fp = getEntropy(ip, matrix);
		
		ImageProcessor ip2;
		
		// rescale to the corresponding number of bits
		if (ip instanceof FloatProcessor == false) 
		{
			if (ip instanceof ByteProcessor)	
			{
				//IJ.log("Float2Byte");
				///ip2 = fp.convertToByte(true);
			}
			else
			{
				//IJ.log("Float2Short");
				///ip2 = fp.convertToShort(true);
			}
			///ip.setPixels(ip2.getPixels());
		}
		else
		{
			IJ.log("keeping output");
			///ip.setPixels( fp.getPixels() );
		}
		
		//IJ.log("resetminmax");
		ip.resetMinAndMax();
		
	}

	/**
	 * Display filter information
	 */
	void showAbout() 
	{
		IJ.showMessage("Matrix Operations...",
				"Convolves a matrix with an image\n"+
				"matrix has to be 2D\n");
	}
	
}
