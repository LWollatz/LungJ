import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.*;
import ij.measure.Calibration;
import ij.plugin.*;
import ij.plugin.frame.Recorder;
import ij.process.*;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.*;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

import lj.LJPrefs;

/**
 * Divides a 3D image into 3D blocks and saves them into a directory along with header 
 * information in a txt file.
 * run("3D Blocks - Create", "directory=[C:\myblocks\original] width=250 height=250 depth=250 z-offset=0 save");
 * 
 * - Open a large image in ImageJ. If the image is too large to load, load as many slices
 *   as possible.
 * - Choose the size of the output blocks. It is advised to make the blocks cubic if they
 *   are intended for use with 3D algorithms but to make them mere subsets of the 
 *   original stack, if used with 2D algorithms. This will reduce the number of artefacts
 *   from boundary assumptions.
 * - Adjust the z-offset to deal with images that are too large to load into memory at 
 *   once.
 * - Provide an output directory for the blocks.
 * TODO: set calibration of image, specifically the X,Y,Z offset
 * 
 * @author Lasse Wollatz
 * 
 **/

public class Subdivide_3D implements PlugIn, ActionListener{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	private static String BC_outDirectory = LJPrefs.LJ_inpDirectory;
	private static int stepX = 250;
	private static int stepY = 250;
	private static int stepZ = 250;
	private static int haloX = 0;
	private static int haloY = 0;
	private static int haloZ = 0;
	private static int z_offset = 0;
	private double globMin = 0;
    private double globMax = 1;
	private static boolean saveProp = true;
	
	JButton filebtn;
	JTextField outdirtxt;
	GenericDialog gd;
	
	/*** run
     * 
     * @param  command        String 
     * 
     * @see    LJPrefs#getMinMax
     * @see    LJPrefs#savePreferences
     * @see    LJPrefs#writeProperties
     * @see    ij.gui.GenericDialog
     ***/
	public void run(String command){
		IJ.showStatus("Creating blocks...");
		IJ.showProgress(0, 100);
		ImagePlus image = WindowManager.getCurrentImage();
		int[] properties = image.getDimensions(); //width, height, nChannels, nSlices, nFrames
		int maxX = properties[0];
		int maxY = properties[1];
		int maxZ = properties[3];
		//int bits = image.getBitDepth();
		float[] minmax = new float[2];
    	minmax = LJPrefs.getMinMax(image);
		globMin = (double)minmax[0];
    	globMax = (double)minmax[1];
    	
    	/** create dialog to request values from user: **/
		gd = new GenericDialog(command+" Subdivide image and save into directory");
		Font gdFont = gd.getFont();
		//gd.addStringField("Output directory", BC_outDirectory, 100);
		JLabel outdirlbl = new JLabel ("Output directory  ", JLabel.RIGHT);
		outdirlbl.setFont(gdFont);
		outdirtxt = new JTextField(BC_outDirectory,80);
		outdirtxt.setFont(gdFont);
		filebtn = new JButton("...");
		filebtn.addActionListener(this);
		
		gd.add(outdirlbl,-1);
		gd.add(outdirtxt,-1);
		gd.add(filebtn,-1);
		
		gd.addMessage("     ");
		gd.addMessage("filename will be 'z'_'y'_'x'.tif");
		gd.addMessage("Block Properties:");
		gd.addNumericField("width", stepX, 0);
		gd.addNumericField("height", stepY, 0);
		gd.addNumericField("depth", stepZ, 0);
		gd.addMessage("Halo Size:");
		gd.addNumericField("halo_width", haloX, 0);
		gd.addNumericField("halo_height", haloY, 0);
		gd.addNumericField("halo_depth", haloZ, 0);
		gd.addMessage("Other Settings:");
		gd.addNumericField("z-offset", z_offset, 0, 4,  "(affects filename only)");
		gd.addCheckbox("save LungJ header", saveProp);
		IJ.showStatus("Waiting for User Input...");
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showStatus("Plug-In aborted...");
			IJ.showProgress(100, 100);
        	return;
        }
		
		IJ.showStatus("Creating blocks...");
		IJ.showProgress(1, 100);
		
		BC_outDirectory = outdirtxt.getText();
		Recorder.recordOption("directory", BC_outDirectory);
		stepX = (int)gd.getNextNumber();
		stepY = (int)gd.getNextNumber();
		stepZ = (int)gd.getNextNumber();
		haloX = (int)gd.getNextNumber();
		haloY = (int)gd.getNextNumber();
		haloZ = (int)gd.getNextNumber();
		z_offset = (int)gd.getNextNumber();
		saveProp = gd.getNextBoolean();
		/** values from user dialog extracted **/
		
		LJPrefs.LJ_inpDirectory = BC_outDirectory;
		/** save preferences for after Fiji restart: **/
		LJPrefs.savePreferences();
		
		ImageProcessor ip = image.getProcessor();
		Calibration cal = image.getCalibration(); 
		
		int x1 = 0;
		int y1 = 0;
		int z1 = 0;
		
		int xs = stepX+2*haloX;
		int ys = stepY+2*haloY;
		int zs = stepZ+2*haloZ;
		
		int x2 = x1+xs;
		int y2 = y1+ys;
		int z2 = z1+zs;
		
		
		for (int z=0; z<maxZ; z+=stepZ) {
			ip.setSliceNumber(z);
			IJ.showProgress(z, maxZ);
			//run("Image Sequence...", "open=["+stackdirectory+"] number=250 file=DigiSens_ sort");
			for (int y=0; y<maxY; y+=stepY) {
				for (int x=0; x<maxX; x+=stepX) {
					
					xs = stepX+2*haloX;
					ys = stepY+2*haloY;
					zs = stepZ+2*haloZ;
					
					x1 = x-haloX;
					if (x1 < 0){x1 = 0; xs = xs - haloX;}
					y1 = y-haloY;
					if (y1 < 0){y1 = 0; ys = ys - haloY;}
					z1 = z-haloZ;
					if (z1 < 0){z1 = 0; zs = zs - haloZ;}
					
					x2 = x1+xs;
					if (x2 > maxX){xs = maxX - x1;}
					y2 = y1+ys;
					if (y2 > maxY){ys = maxY - y1;}
					z2 = z1+zs;
					if (z2 > maxZ){zs = maxZ - z1;}
					
					//Roi tempRoi = new Roi(x, y, stepX, stepY);
					Roi tempRoi = new Roi(x1, y1, xs, ys);
					image.setRoi(tempRoi);
					ip.setRoi(tempRoi);
					//int lastSlice = (z+stepZ < maxZ) ? z+stepZ : maxZ;
					//ImagePlus imgblock = new Duplicator().run(image,z+1,lastSlice);
					ImagePlus imgblock = new Duplicator().run(image,z1+1,z1+zs);
					Calibration tempcal = cal;
					//TODO: Origin doesn't get saved with the image as TIFF doesn't support it by default and ImageJ doesn't offer a workaround using the info string
					tempcal.xOrigin = -1*x1;
					tempcal.yOrigin = -1*y1;
					tempcal.zOrigin = -1*z1;
					imgblock.setCalibration(tempcal);
					String fileout = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,(z+z_offset),y,x);
					IJ.saveAsTiff(imgblock,fileout);
					IJ.showStatus("Creating blocks...");
					IJ.showProgress(y+maxY*x+maxY*maxX*z, maxY*maxX*maxZ);
					IJ.log("saved "+fileout);
					imgblock = null;
				}
			}
		}
		
		/** save block properties if requested **/
		if (saveProp){
			Properties prefs = new Properties();
			prefs.put("maxX", Double.toString(maxX));
			prefs.put("maxY", Double.toString(maxY));
			prefs.put("maxZ", Double.toString(maxZ+z_offset));
			prefs.put("stepX", Double.toString(stepX));
			prefs.put("stepY", Double.toString(stepY));
			prefs.put("stepZ", Double.toString(stepZ));
			prefs.put("haloX", Double.toString(haloX));
			prefs.put("haloY", Double.toString(haloY));
			prefs.put("haloZ", Double.toString(haloZ));
			prefs.put("minVal", Double.toString(globMin));
			prefs.put("maxVal", Double.toString(globMax));
			try {
				LJPrefs.writeProperties(prefs, BC_outDirectory + "\\properties.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		IJ.showStatus("Blocks have been saved!");
		IJ.showProgress(100, 100);
	}

	/*** actionPerformed
     * triggered if button is pressed. Displays a file open dialog to 
     * choose the output directory
     * 
     * @param  arg0           ActionEvent 
     ***/
	@Override
	public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource() == this.filebtn ){
			JFileChooser chooser = new JFileChooser(LJPrefs.LJ_clsDirectory);
			//FileFilter filter = new FileNameExtensionFilter("Directory", "*.*");
			//chooser.addChoosableFileFilter(filter);
			chooser.setFileSelectionMode(1);
			chooser.setSelectedFile(new File(BC_outDirectory));
			int returnVal = chooser.showDialog(gd,"Choose Output");
			if(returnVal == JFileChooser.APPROVE_OPTION)
	        {
				this.outdirtxt.setText(chooser.getSelectedFile().getPath());
	        }
        }
	}
	
	

}
