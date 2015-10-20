import java.awt.Window;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.plugin.ZProjector;
import ij.plugin.filter.Convolver;
import ij.plugin.filter.PlugInFilter;
import ij.process.ByteProcessor;
import ij.process.ColorProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

public class Membrane_Projections implements PlugIn{
	int nAngles = 30;
	int width = 0;
	int height = 0;
	/** original image */
	ImagePlus originalImage = null;
	public static final int MEMBRANE				=  4;
	
	
	public void run(String arg) 
	{
		if (arg.equals("about"))
		{
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
		
		addMembraneFeatures(patchSize,membraneSize);
		
		return;
	}
	
	
	
	/** 
	 * Add membrane features to the stack (single thread version)
	 * @param patchSize size of the filter to be used
	 * @param membraneSize expected membrane thickness
	 */
	public void addMembraneFeatures(int patchSize,int membraneSize){
		//patchSize = 19
		//membraneSize = 1?
	  ImageProcessor membranePatch=new FloatProcessor(patchSize,patchSize); //create kernel as image
	  //put membrane in:
	  int middle=Math.round(patchSize / 2);
	  int startX=middle - (int)Math.floor(membraneSize / 2.0);
	  int endX=middle + (int)Math.ceil(membraneSize / 2.0);
	  for (int x=startX; x <= endX; x++)   for (int y=0; y < patchSize; y++)   membranePatch.setf(x,y,1f);
	  //prepare
	  ImageProcessor rotatedPatch;
	  final double rotationAngle=180 / nAngles;  //6 deg
	  ImagePlus[] channels=extractChannels(originalImage);  //extract individual colour channels
	  ImagePlus[] results=new ImagePlus[channels.length];  //create a result for each channel
	  final Convolver c=new Convolver();  //for matrix on image convolving operation.
	  for (int ch=0; ch < channels.length; ch++) {
	    ImageStack is=new ImageStack(width,height);
	    for (int i=0; i < nAngles; i++) { //for each angle
	      rotatedPatch=membranePatch.duplicate();
	      rotatedPatch.rotate(i * rotationAngle); //rotate kernel
	      float[] kernel=(float[])rotatedPatch.getPixels(); //get kernel as array
	      ImageProcessor ip=channels[ch].getProcessor().duplicate(); //get copy of image
	      c.convolveFloat(ip,kernel,patchSize,patchSize); //convolve kernel with image
	      is.addSlice("Membrane_" + patchSize + "_"+ membraneSize,ip); //collect results in "is" as slices (does this mean I expect originalImage to be a single slice?
	    }
	    ImagePlus projectStack=new ImagePlus("membraneStack",is);
	    ImageStack membraneStack=new ImageStack(width,height); //stack of images, each containing one of 6 z-projections for the channel
	    ZProjector zp=new ZProjector(projectStack);
	    zp.setStopSlice(is.getSize());
	    for (int i=0; i < 6; i++) { //for each projection method
	      zp.setMethod(i);
	      zp.doProjection(); //combine all images from "is" into a single image
	      membraneStack.addSlice("Membrane" + "_" + i+ "_"+ patchSize+ "_"+ membraneSize,zp.getProjection().getChannelProcessor());
	    }
	    results[ch]=new ImagePlus("membrane stack",membraneStack);
	  }
	  ImagePlus merged=mergeResultChannels(results);
	  
	  //for (int i=1; i <= merged.getImageStackSize(); i++)   wholeStack.addSlice(merged.getImageStack().getSliceLabel(i),merged.getImageStack().getPixels(i));
	  
	  merged.show();
	}
	
	
	
	
	
	/**
	 * Display filter information
	 */
	void showAbout() 
	{
		IJ.showMessage("Membrane Projections filter...",
				"modified from Trainable WEKA segmentationn\n");
	}
	
	
	/**
	 * Extract channels from input image if it is RGB
	 * @param originalImage input image
	 * @return array of channels
	 */
	ImagePlus[] extractChannels(final ImagePlus originalImage) 
	{
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
	ImagePlus mergeResultChannels(final ImagePlus[] channels) 
	{
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
	ImageStack mergeStacks(ImageStack redChannel, ImageStack greenChannel, ImageStack blueChannel)
	{
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
