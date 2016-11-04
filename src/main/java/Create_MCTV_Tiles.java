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
import java.io.FileNotFoundException;
import java.io.PrintWriter;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextField;

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

/*** Create_MCTV_Tiles
 * creates tiles and metadata for the Multiresolution CT Viewer. This
 * should become a stand alone PlugIn in the future.
 * 
 * @author Lasse Wollatz  
 * 
 * @see    <a href="http://dx.doi.org/10.5258/SOTON/400332">Multiresolution CT Viewer</a>
 ***/
public class Create_MCTV_Tiles implements PlugIn, ActionListener{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = "http://dx.doi.org/10.5258/SOTON/400332";
	private static String BC_outDirectory = LJPrefs.LJ_inpDirectory;
	private static int stepX = 256;
	private static int stepY = 256;
	private static int stepZ = 1;
	private static int z_offset = 0;
	private static boolean saveProp = true;
	
	JButton filebtn;
	JTextField outdirtxt;
	GenericDialog gd;
	
	/*** getDICOMtag
     * getDICOMtag extracts the value of a DICOM tag from the image info
     * from ij.ImagePlus#getProperty as a String
     * 
     * @param  tagname        String describing a DICOMtag. E.g.
     *                        "0054,1001" or "Pixel Spacing"
     * @param  imginfo        String
     *                        (String)imageplus.getProperty("Info");
     * @param  ans            String default value
     * 
     * @return                String value of that DICOM tag or ans if not
     *                        found
     * 
     * @see    ij.ImagePlus#getInfoProperty
     ***/
	private String getDICOMtag(String tagname, String imginfo, String ans){
		int a = imginfo.indexOf(tagname);
		int b = a;
		if(a>0){
			a = imginfo.indexOf("=",a)+2;
			b = imginfo.indexOf("\n",a);
			ans = imginfo.substring(a, b);
			return ans;
		}else{
			return ans;
		}
	}
	
	/*** getDICOMtag
     * getDICOMtag extracts the value of a DICOM tag from the image info
     * from ij.ImagePlus#getProperty as a Double
     * 
     * @param  tagname        String describing a DICOMtag. E.g.
     *                        "0054,1001" or "Pixel Spacing"
     * @param  imginfo        String
     *                        (String)imageplus.getProperty("Info");
     * @param  ans            Double Double default value
     * 
     * @return                Double Double value of that DICOM tag or ans
     *                        if not found
     * 
     * @see    ij.ImagePlus#getInfoProperty
     ***/
	private Double getDICOMtag(String tagname, String imginfo, Double ans){
		int a = imginfo.indexOf(tagname);
		int b = a;
		if(a>0){
			a = imginfo.indexOf("=",a)+2;
			b = imginfo.indexOf("\n",a);
			ans = Double.parseDouble(imginfo.substring(a, b));
			return ans;
		}else{
			return ans;
		}
	}
	
	/*** run
     * is the main function, extracting and calculating all the values and
     * then creating the image tiles and the JSON description.
     * 
     * @param  command        String 
     * 
     * @see    #getDICOMtag   
     * @see    LJPrefs#getMinMax
     * @see    ij.gui.GenericDialog
     * @see    ij.plugin.frame.Recorder#recordOption
     ***/
	public void run(String command){
		IJ.showStatus("Creating tiles...");
		IJ.showProgress(0, 100);
		ImagePlus image = WindowManager.getCurrentImage();
		int[] properties = image.getDimensions(); //width, height, nChannels, nSlices, nFrames
		int maxX = properties[0];
		int maxY = properties[1];
		int maxZ = properties[3];
		
		Calibration cal = image.getCalibration(); 
		Double xres = cal.pixelWidth; //xres contains the voxel width in units 
		Double yres = cal.pixelHeight; //yres contains the voxel height in units 
		Double zres = cal.pixelDepth; //zres contains the voxel depth in units 
		//String unit = ij.measure.getUnit();
		
		int tWidth = maxX;
		int tHeight = maxY;
		int tSlices = maxZ;
		
		Double rescaleM = 1.0;
		Double rescaleB = 0.0;
		
		/** extract image information **/
		int a = 0;
		int b = 0;
		String unit = "";
		String dimstr = "";
		Object prop = image.getProperty("Info");
		String imginfo = (String)prop;
		a = imginfo.indexOf("DICOM");
		if(a>0){
			/** there is DICOM information **/
			IJ.log("DICOM info found");
			String temp = null;
			temp = getDICOMtag("Pixel Spacing",imginfo,temp);
			if(temp != null){
				a = 1;
				b = temp.indexOf("\\",a);
				xres = Double.parseDouble(temp.substring(a, b));
				a = b+1;
				b = temp.length();
				yres = Double.parseDouble(temp.substring(a, b));
			}
			zres = getDICOMtag("Spacing Between Slices",imginfo,zres);
			unit = getDICOMtag("0054,1001",imginfo,unit);
			if(unit == ""){
				unit = "mm";
			}
			dimstr = " ("+(tWidth*xres)+"x"+(tHeight*yres)+"x"+(tSlices*zres)+" "+unit+")";
		}else if(imginfo.indexOf("0028,0030")>0){
			/** major tag found but no "DICOM" header **/
			IJ.log("This is not a DICOM-file but DICOM tags were found.");
			String temp = null;
			temp = getDICOMtag("Pixel Spacing",imginfo,temp);
			if(temp != null){
				a = 1;
				b = temp.indexOf("\\",a);
				xres = Double.parseDouble(temp.substring(a, b));
				a = b+1;
				b = temp.length();
				yres = Double.parseDouble(temp.substring(a, b));
			}
			zres = getDICOMtag("Spacing Between Slices",imginfo,zres);
			rescaleM = getDICOMtag("0028,1053",imginfo,rescaleM); //rescale slope
			rescaleB = getDICOMtag("0028,1052",imginfo,rescaleB); //rescale intercept
			unit = getDICOMtag("0054,1001",imginfo,unit);
			if(unit == ""){
				unit = "mm";
			}
			dimstr = " ("+(tWidth*xres)+"x"+(tHeight*yres)+"x"+(tSlices*zres)+" "+unit+")";
		}else{
			/** normal image - no information available **/
			IJ.log("not a DICOM");
			IJ.log(imginfo);
			a = imginfo.indexOf("\nUnit ");
			if(a>0){
				a = imginfo.indexOf("=",a)+2;
				b = imginfo.indexOf("\n",a);
				unit = imginfo.substring(a, b);
				dimstr = " ("+(tWidth*xres)+"x"+(tHeight*yres)+"x"+(tSlices*zres)+" "+unit+")";
			}
		}
		dimstr = "  Physical dimensions:"+dimstr;
		float[] minmax = new float[2];
    	minmax = LJPrefs.getMinMax(image);
    	float globMin = minmax[0];
		float globMax = minmax[1];
    	double densmin = (double)globMin*rescaleM + rescaleB;
		double densmax = (double)globMax*rescaleM + rescaleB;
		String HUrange = "  Density in ["+(densmin)+","+(densmax)+"] HU";
    	
    	/**create dialog to request values from user: **/
		gd = new GenericDialog(command+" Create image tiles compatible with Multiresolution CT Viewer");
		Font gdFont = gd.getFont();
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
		gd.addMessage("Block Properties:");
		gd.addMessage(dimstr);
		gd.addMessage(HUrange);
		gd.addMessage("Settings:");
		gd.addNumericField("z-offset", z_offset, 0, 4,  "(affects filename only)");
		gd.addCheckbox("save MCTV header", saveProp);
		IJ.showStatus("Waiting for User Input...");
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showStatus("Plug-In aborted...");
			IJ.showProgress(100, 100);
        	return;
        }
		IJ.showStatus("Creating tiles...");
		IJ.showProgress(1, 100);
		BC_outDirectory = outdirtxt.getText();
		Recorder.recordOption("directory", BC_outDirectory);
		z_offset = (int)gd.getNextNumber();
		saveProp = gd.getNextBoolean();
		/** values from user dialog extracted **/
		
		int x1 = 0;
		int y1 = 0;
		int z1 = 0;
		
		int xs = stepX;
		int ys = stepY;
		int zs = stepZ;
		
		int x2 = x1+xs;
		int y2 = y1+ys;
		int z2 = z1+zs;
		
		
		
		
		
		int maxZoomlevel = (int) Math.ceil(Math.log(Math.max((double)maxX/xs,(double)maxY/ys))/Math.log(2));
		ImagePlus scaledimage = new Duplicator().run(image,z1+1,maxZ);
		ImageProcessor ip_scaledimage = scaledimage.getProcessor(); 
		scaledimage.show();
		
		
		
		String JSONStr = "{\n";
		
		//height
		JSONStr += String.format("\"%1$s\":%2$d,\n","height",tHeight);
		//width
		JSONStr += String.format("\"%1$s\":%2$d,\n","width",tWidth);
		//depth
		JSONStr += String.format("\"%1$s\":%2$d,\n","Slices",tSlices);
		//Units
		JSONStr += String.format("\"%1$s\":\"%2$s\",\n","Units",unit);
		//PixelSpacing
		JSONStr += String.format("\"%1$s\":%2$g,\n","PixelSpacing",xres);
		//SpacingBetweenSlices
		JSONStr += String.format("\"%1$s\":%2$g,\n","SpacingBetweenSlices",zres);
		//densmin
		JSONStr += String.format("\"%1$s\":%2$g,\n","densmin",densmin);
		//densmax
		JSONStr += String.format("\"%1$s\":%2$g,\n","densmax",densmax);
		
		JSONStr += "\"slides\": [\n";
		
		ImageProcessor ip_crossimg = ip_scaledimage.resize(maxX, maxZ); 
		ImagePlus crossimg = new ImagePlus("cross-section", ip_crossimg); 
		ImageProcessor proc1;
		
		for (int zoomlevel=maxZoomlevel; zoomlevel>=0; zoomlevel--){
			IJ.showProgress(maxZoomlevel-zoomlevel+1, maxZoomlevel+3);
			ip_scaledimage.resetRoi();
			scaledimage.setRoi(1, 1, maxX, maxY);
			if (zoomlevel != maxZoomlevel){
				//scale image by 0.5
				//ip_scaledimage = ip_scaledimage.resize((int)(maxX/2), (int)(maxY/2)); 
				IJ.run(scaledimage, "Size...", "width="+((int)(maxX/2))+" height="+((int)(maxY/2))+" depth="+maxZ+" constrain average interpolation=Bilinear"); 
				//scaledimage = new ImagePlus("scaled image", ip_scaledimage); 
			}
			properties = scaledimage.getDimensions(); //width, height, nChannels, nSlices, nFrames
			maxX = properties[0];
			maxY = properties[1];
			maxZ = properties[3];
			for (int z=0; z<maxZ; z+=stepZ) {
				ip_scaledimage.setSliceNumber(z);
				if (zoomlevel == maxZoomlevel){
					JSONStr += "{\"path\": \"";
					JSONStr += String.format("./%1$d/",(z+z_offset));
					JSONStr += "\", \"height\": ";
					JSONStr += String.format("%1$d",maxY);
					JSONStr += ", \"width\": ";
					JSONStr += String.format("%1$d",maxX);
					JSONStr += "},\n";
					//create cross-sectional view
					IJ.log(String.format("SliceNumber=%1$d, z=%2d",ip_scaledimage.getSliceNumber(),z));
					for (int cx=0; cx<maxX; cx+=1) {
						//ip_scaledimage.setSliceNumber(z);
						proc1 = scaledimage.getStack().getProcessor(z+1);
						ip_crossimg.setf(cx, z, proc1.getf(cx, (int)(maxY/2)));
					}
				}
				for (int x=0; x<maxX; x+=stepX) {
					for (int y=0; y<maxY; y+=stepY) {
						
						xs = stepX;
						ys = stepY;
						zs = stepZ;
						
						x1 = x;//-haloX;
						//if (x1 < 0){x1 = 0; xs = xs - haloX;}
						y1 = y;//-haloY;
						//if (y1 < 0){y1 = 0; ys = ys - haloY;}
						z1 = z;//-haloZ;
						//if (z1 < 0){z1 = 0; zs = zs - haloZ;}
						
						x2 = x1+xs;
						if (x2 > maxX){xs = maxX - x1;}
						y2 = y1+ys;
						if (y2 > maxY){ys = maxY - y1;}
						z2 = z1+zs;
						if (z2 > maxZ){zs = maxZ - z1;}
						
						Roi tempRoi = new Roi(x1, y1, xs, ys);
						scaledimage.setRoi(tempRoi);
						ip_scaledimage.setRoi(tempRoi);
						ImagePlus imgblock = new Duplicator().run(scaledimage,z1+1,z1+zs);
						String directoryout = String.format("%1$s\\%2$d",BC_outDirectory,(z+z_offset));
						String fileout = String.format("%1$s\\%2$d\\%3$d-%5$d-%4$d.jpg",BC_outDirectory,(z+z_offset),zoomlevel,y/ys,x/xs);
						
						File file = new File(directoryout);
				        if (!file.exists()) {
				            if (!file.mkdirs()) {
				                System.out.println("Failed to create directory!");
				            }
				        }
				        
						IJ.saveAs(imgblock, "jpg", fileout);
						IJ.showStatus("Creating tiles...");
						IJ.showProgress(maxZoomlevel-zoomlevel+1, maxZoomlevel+3);
						IJ.log("saved "+fileout);
						imgblock = null;
					}
				}
			}
		}
		scaledimage.changes = false;
		scaledimage.close();
		JSONStr += "]\n";
		
		ip_crossimg = ip_crossimg.resize((int)(tWidth/Math.pow(2,maxZoomlevel)), (int)(maxZ/Math.pow(2,maxZoomlevel))); 
		crossimg = new ImagePlus("cross-section", ip_crossimg); 
		IJ.saveAs(crossimg, "jpg", BC_outDirectory+"\\tc.jpg");
		
		if (saveProp){
			JSONStr += "}";
			try {
				PrintWriter out = new PrintWriter(BC_outDirectory + "\\infoJSON.txt");
				out.println(JSONStr);
				out.close();
			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		IJ.showStatus("Blocks have been saved!");
		IJ.showProgress(100, 100);
	}

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
