
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

public class Label_Hyperstack implements PlugIn{
	
	
	public void run(String command){
		IJ.showStatus("Labeling Hyperstack");
		//IJ.register(this.getClass());
		ImagePlus image = WindowManager.getCurrentImage();
		
		int tChannels = image.getNChannels();
		int tFrames = image.getNFrames();
		int tSlices = image.getNSlices();
		
		String[] ChannelNames = new String[tChannels];
		String[] FrameNames = new String[tFrames];
		String seperator = " - ";
		boolean doSliceN = true;
		
		GenericDialog gd = new GenericDialog(command+" Relabel Image Slices");
		gd.addMessage("Frame Labels:");
		for (int f=1; f<=tFrames; f++){
			gd.addStringField("Frame-"+f, "", 40);
		}
		gd.addMessage("Channel Labels:");
		for (int c=1; c<=tChannels; c++){
			gd.addStringField("Channel-"+c, "", 40);
		}
		gd.addMessage("Settings:");
		gd.addStringField("Seperator", seperator, 5);
		gd.addCheckbox("include Slice Number", doSliceN);
		IJ.showStatus("Waiting for User Input...");
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showProgress(100, 100);
        	return;
        }
		IJ.showStatus("Labeling Hyperstack");
		
		for (int f=0; f<tFrames; f++){
			FrameNames[f] = gd.getNextString();
		}
		for (int c=0; c<tChannels; c++){
			ChannelNames[c] = gd.getNextString();
		}
		seperator = gd.getNextString();
		doSliceN = gd.getNextBoolean();
		
		for (int f=1; f<=tFrames; f++){
			for (int c=1; c<=tChannels; c++){
				for (int s=1; s<=tSlices; s++){
					int index = image.getStackIndex(c, s, f);
					String label = "";
					if (!FrameNames[f-1].equals("")){
						if (!label.equals("")){
							label += seperator;
						}
						label += FrameNames[f-1];
					}
					if (!ChannelNames[c-1].equals("")){
						if (!label.equals("")){
							label += seperator;
						}
						label += ChannelNames[c-1];
					}
					if (doSliceN){
						if (!label.equals("")){
							label += seperator;
						}
						label += "slice " + s;
					}
					image.getStack().setSliceLabel(label, index);
				}
			}
		}
		
		
		IJ.showStatus("Labeled Hyperstack.");
	}
}
