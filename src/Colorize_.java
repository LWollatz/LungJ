import ij.IJ;
import ij.ImagePlus;
import ij.Macro;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.ImageCalculator;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.util.Tools;

import java.awt.Color;

public class Colorize_ implements PlugIn{
	
	static Color[] LJColors = new Color[5];
	
	public void run(String command){
		if (IJ.versionLessThan("1.48n"))        // generates an error message for older versions
			return;
		IJ.showStatus("Colorizing Image...");
		IJ.register(this.getClass());
		//TODO: make recordable
		
		String arguments = "";
		if (IJ.isMacro() && Macro.getOptions() != null && !Macro.getOptions().trim().isEmpty()) { 
			System.out.println("hello\n");
			arguments = Macro.getOptions().trim();
			System.out.println(arguments);
		}
		Thread initThread = Thread.currentThread();
		
		
		String options = "";
		
		ImagePlus image;
		if (IJ.isMacro()){
			//get active image:
			String title = null;
			title = LJPrefs.retrieveOption(arguments, "image", title);
			image = WindowManager.getImage(title);
		}else{
			//get active image:
			image = WindowManager.getCurrentImage();
		}
		options += " image="+image.getTitle();
		
		int tChannels = image.getNChannels();
		
		
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
				userColor[f-1] = Set_Up.getColor("Choose color for "+label+" ( frame "+f+")",LJColors[f-1]);
				LJColors[f-1] = userColor[f-1];
			}else if(userColor[f-1] == null){
				userColor[f-1] = Set_Up.getColor("Choose color for "+label+" ( frame "+f+")",LJColors[4]);
			}
			options += " color"+f+"="+userColor[f-1];
			keys[f] = "color"+f;
			values[f] = ""+Tools.c2hex(userColor[f-1]);
		}
		
		keys[0] = "filename";
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
		LJPrefs.recordRun("Colorize ", keys, values);
		
		IJ.showStatus("Image Colorized.");
	}
}
