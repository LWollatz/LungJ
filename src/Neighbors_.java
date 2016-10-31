import java.util.concurrent.Callable;

import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

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
public class Neighbors_ implements PlugIn{
	
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
		
		gd.addNumericField("minSigma", 1, 2);
		gd.addNumericField("maxSigma", 8, 2);

		gd.showDialog();
		if ( gd.wasCanceled() )
			return;
		
		
		int minSigma = (int) gd.getNextNumber();
		int maxSigma = (int) gd.getNextNumber();
		
		addNeighbors(minSigma,maxSigma);
		
		return;
	}
	
	/**
	 * Add 8 neighbours of the original image as features
	 * @param minSigma    int specifying the minimum neighbour offset
	 * @param maxSigma    int specifying the maximum neighbour offset
	 * @see trainableSegmentation.FeatureStack#addNeighbors
	 */
	public void addNeighbors(final int minSigma,final int maxSigma){
		// Test: add neighbors of original image
				
		ImagePlus[] channels = extractChannels(originalImage);

		ImagePlus[] results = new ImagePlus[ channels.length ];

		for(int ch=0; ch < channels.length; ch++)
		{
			ImageStack result = new ImageStack( originalImage.getWidth(), originalImage.getHeight() );
			
			for(int sigma = minSigma; sigma <=maxSigma; sigma *= 2) //iterate over all requested surroundings
			{
				double[][] neighborhood = new double[8][originalImage.getWidth() * originalImage.getHeight()];		

				for(int y=0, n=0; y<originalImage.getHeight(); y++)
					for(int x=0; x<originalImage.getWidth(); x++, n++)		//iterate over all pixel of the original slice			
					{
						for(int i = -1 * sigma, k=0;  i < (sigma+1); i += sigma)
							for(int j = -1 * sigma; j < (sigma+1); j += sigma)  //iterate over surrounding of pixel
							{
								if(i==0 && j==0)
									continue;				
								neighborhood[k][n] = getPixelMirrorConditions(channels[ ch ].getProcessor(), x+i, y+j);
								k++;
							}
					}


				for(int i=0; i<8; i++)
					result.addSlice("Neighbours" + "_" + sigma +"_" +  i, new FloatProcessor( originalImage.getWidth(), originalImage.getHeight(), neighborhood[ i ]));						
			}
			results[ ch ] = new ImagePlus("Neighbors", result);
		}
		ImagePlus merged = mergeResultChannels(results);
		
		//for(int i=1; i<=merged.getImageStackSize(); i++)
		//	wholeStack.addSlice(merged.getImageStack().getSliceLabel(i), merged.getImageStack().getPixels(i));
		
		merged.show();
	}
	
	/**
	 * Calculate 8 neighbours  concurrently
	 * @param originalImage original input image
	 * @param minSigma    int specifying the minimum neighbour offset
	 * @param maxSigma    int specifying the maximum neighbour offset
	 * @return result image
	 * @see trainableSegmentation.FeatureStack#getNeighbors
	 */
	public Callable<ImagePlus> getNeighbors(final ImagePlus originalImage,final int minSigma,final int maxSigma){
		if (Thread.currentThread().isInterrupted()) 
			return null;
		
		return new Callable<ImagePlus>(){
			public ImagePlus call(){
		
				// Test: add neighbours of original image
				ImagePlus[] channels = extractChannels(originalImage);

				ImagePlus[] results = new ImagePlus[ channels.length ];

				for(int ch=0; ch < channels.length; ch++)
				{
					ImageStack result = new ImageStack( originalImage.getWidth(), originalImage.getHeight() );
					for(int sigma = minSigma; sigma <=maxSigma; sigma *= 2)
					{
						double[][] neighborhood = new double[8][originalImage.getWidth() * originalImage.getHeight()];		

						for(int y=0, n=0; y<originalImage.getHeight(); y++)
							for(int x=0; x<originalImage.getWidth(); x++, n++)
							{
								for(int i=-1 * sigma, k=0;  i < (sigma+1); i+=sigma)
									for(int j = -1 * sigma; j < (sigma+1); j+=sigma)
									{
										if(i==0 && j==0)
											continue;				
										neighborhood[k][n] = getPixelMirrorConditions(channels[ ch ].getProcessor(), x+i, y+j);
										k++;
									}
							}

						
						for(int i=0; i<8; i++)
							result.addSlice("Neighbours" + "_" + sigma +"_" +  i, new FloatProcessor( originalImage.getWidth(), originalImage.getHeight(), neighborhood[ i ]));						
					}
					results[ ch ] = new ImagePlus("Neighbors", result);
				}
				return mergeResultChannels(results);
			}
		};
	}
	
	/**
	 * Extract channels from input image if it is RGB
	 * @param originalImage input image
	 * @return array of channels
	 * @see trainableSegmentation.FeatureStack#extractChannels
	 */
	ImagePlus[] extractChannels(final ImagePlus originalImage){
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
	 * @see trainableSegmentation.FeatureStack#mergeResultChannels
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
	 * @see trainableSegmentation.FeatureStack#mergeStacks
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
	
	/**
	 * Get pixel value from an ImageProcessor with mirror boundary conditions
	 * @param ip input image
	 * @param x x- pixel coordinate
	 * @param y y- pixel coordinate
	 * @return pixel value
	 * @see trainableSegmentation.FeatureStack#getPixelMirrorConditions
	 */
	double getPixelMirrorConditions(ImageProcessor ip, int x, int y){
		int x2 = x < 0 ? -x : x;
		int y2 = y < 0 ? -y : y;
		
		if(x2 >= ip.getWidth())
			x2 = 2 * (ip.getWidth() - 1) - x2;
		
		if(y2 >= ip.getHeight())
			y2 = 2 * (ip.getHeight() - 1) - y2;
		
		return ip.getPixelValue(x2, y2);
	}
}
