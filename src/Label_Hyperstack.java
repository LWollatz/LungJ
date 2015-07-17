import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;

/** 
 * Consistently labels the slices of a hyperstack.
 * 
 * - Provide names for each frame. leave the name blank if slices should not be 
 *   distinguished by their frame.
 * - Provide names for each channel. leave the name blank if slices should not be 
 *   distinguished by their channel.
 * - Provide a separating string (default is ` - ’)
 * - Choose if the slice number (z-value) should be included in the slice name.
 *   
 * @author Lasse Wollatz
 *   
 **/

public class Label_Hyperstack implements PlugIn{
	//TODO: handle case of too many frames (e.g. for more than 10 just allow to include frame number with prefix like t=1 etc.)
	//TODO: allow to offset slice number
	//TODO: future work: detect patterns in current image labeling to pre-fill textboxes, or just store user preferences
	
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
