import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Color;

import lj.LJPrefs;


/*** Colorize_
 * Combines a set of segmented images into a colour image.
 * <code>run("Colour by Segment"," image=[sample.tif]
 * color1=[#000000] color2=[#FFFFFF] color3=[#009999]
 * color4=[#FF6666]");</code>
 * 
 * - The original image should be a hyperstack with each feature
 *   segmentation appearing in a separate frame.
 * - Colorize_ allows to choose a colour for each frame.
 * - Once colours have been chosen, a new image is produced,
 *   overlaying each frame with the specified colour and combining
 *   them into a single RGB stack, ignoring black pixels as
 *   background.
 * 
 * @author Lasse Wollatz  
 ***/
public class Colorize_ implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	static Color[] LJColors = LJPrefs.LJ_Colors;
	
	
	/*** run
	 * 
	 * @param  command        String
	 * 
	 * @see   LJPrefs#retrieveOption
	 * @see   LJPrefs#getColor
	 * @see   LJPrefs#savePreferences
	 * @see   LJPrefs#recordRun
	 ***/
	public void run(String command){
		/** generate error message for older versions: **/
		if (IJ.versionLessThan("1.49s")) {
			IJ.showStatus("Plug-In unable to run.");
			return;
		}
		
		IJ.showStatus("Colorizing Image...");
		//IJ.register(this.getClass()); //check if required
		
		//TODO: make recordable
		String arguments = "";
		if (IJ.isMacro() && Macro.getOptions() != null && !Macro.getOptions().trim().isEmpty()) { 
			arguments = Macro.getOptions().trim();
			System.out.println(arguments);
			IJ.log(arguments);
		}
		//Thread initThread = Thread.currentThread();
		
		//TODO: have a look at the macro recording again and see what is used and what isn't
		//String options = "";
		
		ImagePlus image;
		if (WindowManager.getImageCount() <= 0){
			IJ.error("no image opened to apply filter to");
			IJ.showStatus("Plug-In unable to run.");
			return;
		}
		if (IJ.isMacro()){
			/** get active image: **/
			String title = null;
			title = LJPrefs.retrieveOption(arguments, "image", title);
			image = WindowManager.getImage(title);
		}else{
			/** get active image: **/
			image = WindowManager.getCurrentImage();
		}
		//options += " image="+image.getTitle();
		
		//int tChannels = image.getNChannels();
		
		
		int tFrames = image.getNFrames();
		Color[] userColor = new Color[tFrames];
		String[] keys = new String[tFrames+1];
		String[] values = new String[tFrames+1];
		
		for (int f=1; f<=tFrames; f++){
			int index = image.getStackIndex(1, 1, f);
			String label = image.getStack().getShortSliceLabel(index);
			if (IJ.isMacro()){
				System.out.println("I'm a macro\n");
				userColor[f-1] = LJPrefs.retrieveOption(arguments, "color"+f, userColor[f-1]);
			}
            if (f< 5 && userColor[f-1] == null){
				userColor[f-1] = LJPrefs.getColor("Choose color for "+label+" ( frame "+f+")",LJColors[f-1]);
				LJColors[f-1] = userColor[f-1];
			}else if(userColor[f-1] == null){
				userColor[f-1] = LJPrefs.getColor("Choose color for "+label+" ( frame "+f+")",LJColors[4]);
			}
			//options += " color"+f+"="+userColor[f-1];
			keys[f] = "color"+f;
			values[f] = ""+Tools.c2hex(userColor[f-1]);
		}
		
		LJPrefs.LJ_Colors = LJColors;
		LJPrefs.savePreferences();
		
		keys[0] = "image";
		values[0] = image.getTitle();
		
		int tHeight = image.getHeight();
		int tWidth = image.getWidth();
		int tDepth = image.getNSlices();
		
		
		//TODO: make background black
		ImagePlus imgout = IJ.createImage("Result", "RGB", tWidth, tHeight, tDepth);
		
		for (int f=1; f<=tFrames; f++){
			for (int z=1; z<=tDepth; z++){
				int index = image.getStackIndex(1, z, f);
				ImageProcessor imageIP = image.getStack().getProcessor(index).convertToByte(true);
				ImageProcessor outputIP = imgout.getStack().getProcessor(z);
			
				byte[] iPixels = (byte[])imageIP.getPixels();
				int[] oPixels = (int[])outputIP.getPixels();
				
				for (int y=0; y<tHeight; y++){
		    		for (int x=0, p=x+y*tWidth; x<tWidth; x++,p++){
		    			/*
		    			if (iPixels[p] == (byte)0){
							oPixels[p] = (int)0;
		    			}else{*/
		    			if (iPixels[p] != (byte)0){
		    				int red  = (int)((iPixels[p] & 0xff)/2+userColor[f-1].getRed()/2);
		    				int green = (int)((iPixels[p] & 0xff)/2+userColor[f-1].getGreen()/2);
		    				int blue = (int)((iPixels[p] & 0xff)/2+userColor[f-1].getBlue()/2);
							oPixels[p] = ((red & 0xff)<<16)+((green & 0xff)<<8) + (blue & 0xff);
		    			}
		    		}
				}
			}
		}
		imgout.show();
		
		//Macro.setOptions(initThread, options);
		LJPrefs.recordRun("Colour by Segment ", keys, values);
		
		IJ.showStatus("Image Colorized.");
	}
}
