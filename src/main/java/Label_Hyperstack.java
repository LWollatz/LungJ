import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.measure.Calibration;
import ij.plugin.PlugIn;

import lj.LJPrefs;

/** License Statement
 * Copyright 2016 Lasse Wollatz
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/

/*** Label_Hyperstack
 * consistently labels the slices of a hyperstack.
 * 
 * - Provide names for each frame. leave the name blank if slices
 *   should not be distinguished by their frame.
 * - Provide names for each channel. leave the name blank if slices
 *   should not be distinguished by their channel.
 * - Provide a separating string (default is ` - `)
 * - Choose if the slice number (z-value) should be included in the
 *   slice name. If so, provide a starting value and an increment
 *   value as well as a unit if useful.
 * 
 * @author Lasse Wollatz  
 ***/
public class Label_Hyperstack implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	//TODO: future work: detect patterns in current image labeling to pre-fill textboxes, or just store user preferences
	//TODO:              handle case of too many frames more elegant 
	
	public void run(String command){
		IJ.showStatus("Labeling Hyperstack");
		ImagePlus image = WindowManager.getCurrentImage();
		
		int tChannels = image.getNChannels();
		int tFrames = image.getNFrames();
		int tSlices = image.getNSlices();
		int tWidth = image.getWidth();
		int tHeight = image.getHeight();
		
		Calibration cal = image.getCalibration(); 
		Double xres = cal.pixelWidth; //xres contains the voxel width in units 
		Double yres = cal.pixelHeight; //yres contains the voxel height in units 
		Double zres = cal.pixelDepth; //zres contains the voxel depth in units 
		//String unit = ij.measure.getUnit();
		
		
		int a = 0;
		int b = 0;
		//Double xres = 1.0;
		//Double yres = 1.0;
		//Double zres = 1.0;
		String unit = "";
		String dimstr = "";
		//Properties imgprops = image.getProperties();
		Object prop = image.getProperty("Info");
		String imginfo = (String)prop;
		a = imginfo.indexOf("\nXResolution ");
		/*if(a>0){
			a = imginfo.indexOf("=",a)+1;
			b = imginfo.indexOf("\n",a);
			xres = 1/Double.parseDouble(imginfo.substring(a, b));
			if(xres<0){
				xres=1.0;
			}
		}
		a = imginfo.indexOf("\nYResolution ");
		if(a>0){
			a = imginfo.indexOf("=",a)+1;
			b = imginfo.indexOf("\n",a);
			yres = 1/Double.parseDouble(imginfo.substring(a, b));
			if(yres<0){
				yres=1.0;
			}
		}
		a = imginfo.indexOf("\nSpacing ");
		if(a>0){
			a = imginfo.indexOf("=",a)+1;
			b = imginfo.indexOf("\n",a);
			zres = Double.parseDouble(imginfo.substring(a, b));
			if(zres<0){
				zres=1.0;
			}
		}*/
		a = imginfo.indexOf("\nUnit ");
		if(a>0){
			a = imginfo.indexOf("=",a)+2;
			b = imginfo.indexOf("\n",a);
			unit = imginfo.substring(a, b);
			dimstr = " ("+(tWidth*xres)+"x"+(tHeight*yres)+"x"+(tSlices*zres)+" "+unit+")";
		}
		
		
		
		String[] ChannelNames = new String[tChannels];
		String[] FrameNames = new String[tFrames];
		Double FramelabelStart = 0.0;
		Double FramelabelInc = 1.0;
		String FramelabelEnd = "";
		Double SlicelabelStart = 0.0;
		Double SlicelabelInc = zres;
		String SlicelabelEnd = unit;
		String seperator = " - ";
		boolean doSliceN = true;
		boolean doFrameN = true;
		
		Double tempnum = 0.0;
		
		GenericDialog gd = new GenericDialog(command+" Relabel Image Slices");
		gd.addMessage("Dimensions: "+tWidth+"x"+tHeight+"x"+tSlices+" px"+dimstr);
		if(tFrames > 10){
			gd.addMessage("Frame Labels:");
			gd.addCheckbox("frame number included", doFrameN);
			gd.addStringField("frame_starting_string", "t = ", 40);
			gd.addNumericField("frame_starting_number", FramelabelStart, 5);
			gd.addNumericField("frame_increment_number", FramelabelInc, 5);
			gd.addStringField("frame_ending_string", FramelabelEnd, 5);
		}else if (tFrames > 1){
			gd.addMessage("Frame Labels:");
			for (int f=1; f<=tFrames; f++){
				gd.addStringField("Frame-"+f, "", 40);
			}
		}else {
			gd.addMessage("(only single frame found)");
		}
		if (tChannels > 1){
			gd.addMessage("Channel Labels:");
			for (int c=1; c<=tChannels; c++){
				gd.addStringField("Channel-"+c, "", 40);
			}
		}else {
			gd.addMessage("(only single channel found)");
		}
		gd.addMessage("Slice Labels:");
		gd.addCheckbox("slice number included", doSliceN);
		gd.addNumericField("slice_starting_number", SlicelabelStart, 5);
		gd.addNumericField("slice_increment_number", SlicelabelInc, 5);
		gd.addStringField("slice_ending_string", SlicelabelEnd, 5);
		
		//gd.addTextAreas(imginfo, "", 20, 20);
		gd.addMessage("Settings:");
		gd.addStringField("Seperator", seperator, 5);
		IJ.showStatus("Waiting for User Input...");
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showProgress(100, 100);
        	return;
        }
		IJ.showStatus("Labeling Hyperstack");
		
		if(tFrames > 10){
			doFrameN = gd.getNextBoolean();
			FrameNames[0] = gd.getNextString();
			FramelabelStart = gd.getNextNumber();
			FramelabelInc = gd.getNextNumber();
			FramelabelEnd = gd.getNextString();
		}else if (tFrames > 1){
			for (int f=0; f<tFrames; f++){
				FrameNames[f] = gd.getNextString();
			}
		}
		if (tChannels > 1){
			for (int c=0; c<tChannels; c++){
				ChannelNames[c] = gd.getNextString();
			}
		}
		//get slices
		doSliceN = gd.getNextBoolean();
		SlicelabelStart = gd.getNextNumber();
		SlicelabelInc = gd.getNextNumber();
		SlicelabelEnd = gd.getNextString();
		//get settings
		seperator = gd.getNextString();
		
		for (int f=1; f<=tFrames; f++){
			for (int c=1; c<=tChannels; c++){
				for (int s=1; s<=tSlices; s++){
					int index = image.getStackIndex(c, s, f);
					String label = "";
					if(tFrames > 10){
						if (doFrameN){
							if (!label.equals("")){
								label += seperator;
							}
							tempnum = (FramelabelStart+((double)f-1.0)*FramelabelInc);
							label += FrameNames[0] + tempnum.toString() + FramelabelEnd;
						}
					}else if (tFrames > 1){
						if (!FrameNames[f-1].equals("")){
							if (!label.equals("")){
								label += seperator;
							}
							label += FrameNames[f-1];
						}
					}
					if (tChannels > 1){
						if (!ChannelNames[c-1].equals("")){
							if (!label.equals("")){
								label += seperator;
							}
							label += ChannelNames[c-1];
						}
					}
					if (doSliceN){
						if (!label.equals("")){
							label += seperator;
						}
						tempnum = SlicelabelStart+((double)s-1.0)*SlicelabelInc;
						label += tempnum.toString() + ""; 
						label += SlicelabelEnd;
					}
					IJ.log(label);
					image.getStack().setSliceLabel(label+" ", index); //for some reason the unit vanishes and numbers are rounded to integers
				}
			}
		}
		
		
		IJ.showStatus("Labeled Hyperstack.");
	}
}
