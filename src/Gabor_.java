import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

import net.imglib2.img.Img;
import net.imglib2.img.display.imagej.ImageJFunctions;
import net.imglib2.algorithm.fft2.FFTConvolution;
import net.imglib2.img.ImagePlusAdapter;

/*** Gabor_
 * implements the TWS Gabor filter
 * This class has been copied from the TWS and adapted for independent use. (NOT FULLY FUNCTIONAL!)
 * @author Trainable WEKA Segmentation
 ***/
public class Gabor_ implements PlugIn{
	
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
		
		addGabor(originalImage,sigma, gamma, psi, frequency, nAngles);
		
		return;
	}
	
	
	/**
	 * Add Gabor features to current stack
	 * @param originalImage input image
	 * @param sigma size of the Gaussian envelope
	 * @param gamma spatial aspect ratio, it specifies the ellipticity of the support of the Gabor function
	 * @param psi phase offset
	 * @param frequency frequency of the sinusoidal component
	 * @param nAngles number of filter orientations
	 */
	public void addGabor(final ImagePlus originalImage, final double sigma, final double gamma, final double psi, final double frequency, final int nAngles){
		if (Thread.currentThread().isInterrupted()) 
			return;


		final int width = originalImage.getWidth();
		final int height = originalImage.getHeight();

		// Apply aspect ratio to the Gaussian curves
		final double sigma_x = sigma;
		final double sigma_y = sigma / gamma;

		// Decide size of the filters based on the sigma
		int largerSigma = (sigma_x > sigma_y) ? (int) sigma_x : (int) sigma_y;
		if(largerSigma < 1)
			largerSigma = 1;

		// Create set of filters			
		final int filterSizeX = 6 * largerSigma + 1;
		final int filterSizeY = 6 * largerSigma + 1;

		final int middleX = (int) Math.round(filterSizeX / 2);
		final int middleY = (int) Math.round(filterSizeY / 2);

		final ImageStack kernels = new ImageStack(filterSizeX, filterSizeY);

		final double rotationAngle = Math.PI/nAngles;
		final double sigma_x2 = sigma_x * sigma_x;
		final double sigma_y2 = sigma_y * sigma_y;

		// Rotate kernel from 0 to 180 degrees
		for (int i=0; i<nAngles; i++)
		{	
			final double theta = rotationAngle * i;
			final ImageProcessor filter = new FloatProcessor(filterSizeX, filterSizeY);	
			for (int x=-middleX; x<=middleX; x++) //iterate over kernel width, with zero at centre
			{
				for (int y=-middleY; y<=middleY; y++) //iterate over kernel height, with zero at centre
				{	
					//rotation from point to point'
					final double xPrime = x * Math.cos(theta) + y * Math.sin(theta);
					final double yPrime = y * Math.cos(theta) - x * Math.sin(theta);
					
					// exp part = 2D Fourier Transform
					final double a = 1.0 / ( 2* Math.PI * sigma_x * sigma_y ) * Math.exp(-0.5 * (xPrime*xPrime / sigma_x2 + yPrime*yPrime / sigma_y2) );
					// xPrime/filterSizeX = t
					final double c = Math.cos( 2 * Math.PI * (frequency * xPrime) / filterSizeX + psi); 

					filter.setf(x+middleX, y+middleY, (float)(a*c) );
				}
			}
			kernels.addSlice("kernel angle = " + i, filter);
		}

		// Get channel(s) to process
		ImagePlus[] channels = extractChannels(originalImage);
		
		ImagePlus[] results = new ImagePlus[ channels.length ];
		
		for(int ch=0; ch < channels.length; ch++)
		{

			final ImageStack is = new ImageStack(width, height);
			// Apply kernels
			for (int i=0; i<nAngles; i++) //for each angle
			{
				ImagePlus ip2 = channels[ ch ].duplicate();
				Img<FloatType> kernel = ImagePlusAdapter.wrap( new ImagePlus("", kernels.getProcessor(i+1)) );
				Img<FloatType> image2 = ImagePlusAdapter.wrap( ip2 );

				// compute Fourier convolution
				FFTConvolution< FloatType > c = 
						new FFTConvolution< FloatType >( image2, kernel );
				c.convolve();
				
				ip2 = ImageJFunctions.wrap( image2, "" );
				
				is.addSlice( "gabor angle = " + i, ip2.getProcessor() );					
			}			

			// Normalize filtered stack (it seems necessary to have proper results)
			final ImagePlus projectStack = new ImagePlus("filtered stack", Utils.normalize( is ));


			final ImageStack resultStack = new ImageStack(width, height);

			final ZProjector zp = new ZProjector(projectStack);
			zp.setStopSlice(is.getSize());
			for (int i=1;i<=2; i++)
			{
				zp.setMethod(i);
				zp.doProjection();
				resultStack.addSlice("Gabor" + "_" + i 
						+"_"+sigma+"_" + gamma + "_"+ (int) (psi / (Math.PI/4) ) +"_"+frequency, 
						zp.getProjection().getChannelProcessor());
			}
			results[ ch ] = new ImagePlus ("Gabor stack", resultStack);
		}
		
		ImagePlus merged = mergeResultChannels(results);
		
		for(int i=1; i<=merged.getImageStackSize(); i++)
			wholeStack.addSlice(merged.getImageStack().getSliceLabel(i), merged.getImageStack().getPixels(i));
	}
	
	
	/**
	 * Extract channels from input image if it is RGB
	 * @param originalImage input image
	 * @return array of channels
	 */
	ImagePlus[] extractChannels(final ImagePlus originalImage) {
		final int width = originalImage.getWidth();
		final int height = originalImage.getHeight();
		ImagePlus[] channels;
		if( originalImage.getType() == ImagePlus.COLOR_RGB )
		{
			final ByteProcessor redBp = new ByteProcessor(width, height);
			final ByteProcessor greenBp = new ByteProcessor(width, height);
			final ByteProcessor blueBp = new ByteProcessor(width, height);

			final byte[] redPixels = (byte[]) redBp.getPixels();
			final byte[] greenPixels = (byte[]) greenBp.getPixels();
			final byte[] bluePixels = (byte[]) blueBp.getPixels();

			((ColorProcessor)(originalImage.getProcessor().duplicate())).getRGB(redPixels, greenPixels, bluePixels);

			channels = new ImagePlus[]{new ImagePlus("red", redBp.convertToFloat()), 
					new ImagePlus("green", greenBp.convertToFloat()), 
					new ImagePlus("blue", blueBp.convertToFloat() )};
		}
		else
		{
			channels = new ImagePlus[1];
			channels[0] = new ImagePlus(originalImage.getTitle(), originalImage.getProcessor().duplicate().convertToFloat() );
		}
		return channels;
	}
	
	/**
	 * Merge input channels if they are more than 1
	 * @param channels results channels
	 * @return result image 
	 */
	ImagePlus mergeResultChannels(final ImagePlus[] channels) {
		if(channels.length > 1)
		{						
			ImageStack mergedColorStack = mergeStacks(channels[0].getImageStack(), channels[1].getImageStack(), channels[2].getImageStack());
			
			ImagePlus merged = new ImagePlus(channels[0].getTitle(), mergedColorStack); 
			
			for(int n = 1; n <= merged.getImageStackSize(); n++)
				merged.getImageStack().setSliceLabel(channels[0].getImageStack().getSliceLabel(n), n);
			
			return merged;
		}
		else
			return channels[0];
	}
	
	/**
	 * Merge three image stack into a color stack (no scaling)
	 * 
	 * @param redChannel image stack representing the red channel 
	 * @param greenChannel image stack representing the green channel
	 * @param blueChannel image stack representing the blue channel
	 * @return RGB merged stack
	 */
	ImageStack mergeStacks(ImageStack redChannel, ImageStack greenChannel, ImageStack blueChannel){
		final ImageStack colorStack = new ImageStack( redChannel.getWidth(), redChannel.getHeight());
		
		for(int n=1; n<=redChannel.getSize(); n++)
		{
			final ByteProcessor red = (ByteProcessor) redChannel.getProcessor(n).convertToByte(false); 
			final ByteProcessor green = (ByteProcessor) greenChannel.getProcessor(n).convertToByte(false); 
			final ByteProcessor blue = (ByteProcessor) blueChannel.getProcessor(n).convertToByte(false); 
			
			final ColorProcessor cp = new ColorProcessor(redChannel.getWidth(), redChannel.getHeight());
			cp.setRGB((byte[]) red.getPixels(), (byte[]) green.getPixels(), (byte[]) blue.getPixels() );
			
			colorStack.addSlice(redChannel.getSliceLabel(n), cp);
		}
		
		return colorStack;
	}
	
	
}
