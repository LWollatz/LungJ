import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

import java.io.IOException;
import java.util.Properties;

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

/*** Halo_Exchange
 * exchanges the overlapping halos of all blocks and saves the result
 * in a new directory. This is aimed to be efficient in terms of
 * minimising the memory usage
 * 
 * @author Lasse Wollatz  
 ***/
public class Halo_Exchange implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	private static String BC_inDirectory = LJPrefs.LJ_inpDirectory;
	private static String BC_outDirectory = LJPrefs.LJ_outDirectory;
	
	private static int maxX = 1;
	private static int maxY = 1;
	private static int maxZ = 1;
	private static int stepX = 1;
	private static int stepY = 1;
	private static int stepZ = 1;
	private static int haloX = 0;
	private static int haloY = 0;
	private static int haloZ = 0;
	private static float globMaxIn = -Float.MAX_VALUE;
	private static float globMinIn = Float.MAX_VALUE;
	
	private static int moveX = 1; //current movement x-direction (+1 or -1)
	private static int moveY = 1; //current movement y-direction (+1 or -1)
	private static int moveZ = 1; //current movement z-direction (+1) -> once -1, snake completed
	
	private static int tempX1 = 0;
	private static int tempX2 = 0;
	private static int tempY1 = 0;
	private static int tempY2 = 0;
	private static int tempZ1 = 0;
	private static int tempZ2 = 0;
	
	private static int xs1 = 0;
	private static int xs2 = stepX;
	private static int ys1 = 0;
	private static int ys2 = stepY;
	private static int zs1 = 0;
	private static int zs2 = stepZ;
	
	private static int[] configuration = {0,0,0};
	
	
	private static String filein111 = "";
	private static String fileout111 = "";
	private static ImagePlus img111 = null;
	private static String filein112 = "";
	private static String fileout112 = "";
	private static ImagePlus img112 = null;
	private static String filein121 = "";
	private static String fileout121 = "";
	private static ImagePlus img121 = null;
	private static String filein122 = "";
	private static String fileout122 = "";
	private static ImagePlus img122 = null;
	
	private static String filein211 = "";
	private static String fileout211 = "";
	private static ImagePlus img211 = null;
	private static String filein212 = "";
	private static String fileout212 = "";
	private static ImagePlus img212 = null;
	private static String filein221 = "";
	private static String fileout221 = "";
	private static ImagePlus img221 = null;
	private static String filein222 = "";
	private static String fileout222 = "";
	private static ImagePlus img222 = null;
	
	private static Boolean[] requireClean = {false,false,false};
	
	
	
	public void run(String command){
		
		//create dialog to request values from user:
		GenericDialog gd = new GenericDialog(command+" exchange halos");
		gd.addStringField("Input directory", BC_inDirectory, 100);
		gd.addStringField("Output directory", BC_outDirectory, 100);
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
		if (gd.wasCanceled()){
			return;
		}
		BC_inDirectory = gd.getNextString();
		BC_outDirectory = gd.getNextString();
		//values from user dialog extracted
		LJPrefs.LJ_inpDirectory = BC_inDirectory;
		LJPrefs.LJ_outDirectory = BC_outDirectory;
		//save preferences for after Fiji restart:
		LJPrefs.savePreferences();
		
		Properties prefs = new Properties();
		try {
			prefs = LJPrefs.readProperties(BC_inDirectory + "\\properties.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		maxX = LJPrefs.getPref(prefs, "maxX", maxX);
		maxY = LJPrefs.getPref(prefs, "maxY", maxY);
		maxZ = LJPrefs.getPref(prefs, "maxZ", maxZ);
		stepX = LJPrefs.getPref(prefs, "stepX", stepX);
		stepY = LJPrefs.getPref(prefs, "stepY", stepY);
		stepZ = LJPrefs.getPref(prefs, "stepZ", stepZ);
		haloX = LJPrefs.getPref(prefs, "haloX", haloX);
		haloY = LJPrefs.getPref(prefs, "haloY", haloY);
		haloZ = LJPrefs.getPref(prefs, "haloZ", haloZ);
		globMinIn = (float)LJPrefs.getPref(prefs, "minVal", globMinIn);
		globMaxIn = (float)LJPrefs.getPref(prefs, "maxVal", globMaxIn);
		
		/**initialise coordinates for first load**/
		xs1 = 0;
		xs2 = stepX;
		ys1 = 0;
		ys2 = stepY;
		zs1 = 0;
		zs2 = stepZ;
		
		
		filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys1,xs1);
		fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys1,xs1);
		img111 = IJ.openImage(filein111);
		filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys1,xs2);
		fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys1,xs2);
		img112 = IJ.openImage(filein112);
		filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys2,xs1);
		fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys2,xs1);
		img121 = IJ.openImage(filein121);
		filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys2,xs2);
		fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys2,xs2);
		img122 = IJ.openImage(filein122);
		
		filein211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys1,xs1);
		fileout211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys1,xs1);
		img211 = IJ.openImage(filein211);
		filein212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys1,xs2);
		fileout212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys1,xs2);
		img212 = IJ.openImage(filein212);
		filein221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys2,xs1);
		fileout221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys2,xs1);
		img221 = IJ.openImage(filein221);
		filein222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys2,xs2);
		fileout222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys2,xs2);
		img222 = IJ.openImage(filein222);
		
		//TODO: deal with single block density for either axis.
		//TODO: do appropriate number of halo exchanges to cover everything but not do anything twice.
					
		doexchangetype000();
		
		moveX = 1;
		moveY = 1;
		moveZ = 1;
		
		while(moveZ == 1){
			if (xs1+moveX*stepX<maxX && xs2+moveX*stepX<maxX && xs1+moveX*stepX>=0 && xs2+moveX*stepX>=0){
				docleanexchange();
				move(2);
				doexchange();
				if (xs1+moveX*stepX>=maxX || xs2+moveX*stepX>=maxX){
					requireClean[2] = true;
				}else{
					requireClean[2] = false;
				}
			}else{
				if (ys1+moveY*stepY<maxY && ys2+moveY*stepY<maxY && ys1+moveY*stepY>=0 && ys2+moveY*stepY>=0){
					docleanexchange();
					move(1);
					doexchange();
					if (ys1+moveY*stepY>=maxY || ys2+moveY*stepY>=maxY){
						requireClean[1] = true;
					}else{
						requireClean[1] = false;
					}
				}else{
					if (zs1+stepZ<maxZ && zs2+stepZ<maxZ){
						docleanexchange();
						move(0);
						doexchange();
						if (zs1+stepZ>=maxZ || zs2+stepZ>=maxZ){
							requireClean[0] = true;
						}else{
							requireClean[0] = false;
						}
					}else{
						docleanexchange();
						moveZ *= -1;
					}
					moveY *= -1;
				}
				moveX *= -1;
			}
		}
		
		
		
		/**finally save all**/
		IJ.saveAsTiff(img111,fileout111);
		IJ.saveAsTiff(img112,fileout112);
		IJ.saveAsTiff(img121,fileout121);
		IJ.saveAsTiff(img122,fileout122);
		IJ.saveAsTiff(img211,fileout211);
		IJ.saveAsTiff(img212,fileout212);
		IJ.saveAsTiff(img221,fileout221);
		IJ.saveAsTiff(img222,fileout222);
		
		/**reset all variables**/
		img111 = null;
		img112 = null;
		img121 = null;
		img122 = null;
		img211 = null;
		img212 = null;
		img221 = null;
		img222 = null;
		
		
		
		IJ.showProgress(100, 100);
		
	}
	
	/*** move ***
     * snake moves
     * 
     * @param  axis                int direction in which to move (z=0,y=1,x=2)
     ***/
	private void move(int axis){
		
		int inverted = configuration[axis];
		
		if (axis == 2){
			if ((inverted == 0 && moveX == 1) || (inverted == 1 && moveX == -1)){
				xs1 += moveX*2*stepX;
				IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img211,fileout211);
				IJ.saveAsTiff(img221,fileout221);
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys1,xs1);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys1,xs1);
				img111 = IJ.openImage(filein111);
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys2,xs1);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys2,xs1);
				img121 = IJ.openImage(filein121);
				filein211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys1,xs1);
				fileout211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys1,xs1);
				img211 = IJ.openImage(filein211);
				filein221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys2,xs1);
				fileout221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys2,xs1);
				img221 = IJ.openImage(filein221);
			}else{
				xs2 += moveX*2*stepX;
				IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img122,fileout122);
				IJ.saveAsTiff(img212,fileout212);
				IJ.saveAsTiff(img222,fileout222);
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys1,xs2);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys1,xs2);
				img112 = IJ.openImage(filein112);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys2,xs2);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys2,xs2);
				img122 = IJ.openImage(filein122);
				filein212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys1,xs2);
				fileout212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys1,xs2);
				img212 = IJ.openImage(filein212);
				filein222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys2,xs2);
				fileout222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys2,xs2);
				img222 = IJ.openImage(filein222);
			}
		}else if (axis == 1){
			if ((inverted == 0 && moveY == 1) || (inverted == 1 && moveY == -1)){
				ys1 += moveY*2*stepY;
				IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img211,fileout211);
				IJ.saveAsTiff(img212,fileout212);
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys1,xs1);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys1,xs1);
				img111 = IJ.openImage(filein111);
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys1,xs2);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys1,xs2);
				img112 = IJ.openImage(filein112);
				filein211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys1,xs1);
				fileout211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys1,xs1);
				img211 = IJ.openImage(filein211);
				filein212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys1,xs2);
				fileout212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys1,xs2);
				img212 = IJ.openImage(filein212);
			}else{
				ys2 += moveY*2*stepY;
				IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				IJ.saveAsTiff(img221,fileout221);
				IJ.saveAsTiff(img222,fileout222);
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys2,xs1);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys2,xs1);
				img121 = IJ.openImage(filein121);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys2,xs2);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys2,xs2);
				img122 = IJ.openImage(filein122);
				filein221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys2,xs1);
				fileout221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys2,xs1);
				img221 = IJ.openImage(filein221);
				filein222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys2,xs2);
				fileout222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys2,xs2);
				img222 = IJ.openImage(filein222);
			}
		}else{
			if (inverted == 0){
				zs1 += moveZ*2*stepZ;
				IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys1,xs1);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys1,xs1);
				img111 = IJ.openImage(filein111);
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys1,xs2);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys1,xs2);
				img112 = IJ.openImage(filein112);
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys2,xs1);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys2,xs1);
				img121 = IJ.openImage(filein121);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs1,ys2,xs2);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs1,ys2,xs2);
				img122 = IJ.openImage(filein122);
			}else{
				zs2 += moveZ*2*stepZ;
				IJ.saveAsTiff(img211,fileout211);
				IJ.saveAsTiff(img212,fileout212);
				IJ.saveAsTiff(img221,fileout221);
				IJ.saveAsTiff(img222,fileout222);
				filein211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys1,xs1);
				fileout211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys1,xs1);
				img211 = IJ.openImage(filein211);
				filein212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys1,xs2);
				fileout212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys1,xs2);
				img212 = IJ.openImage(filein212);
				filein221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys2,xs1);
				fileout221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys2,xs1);
				img221 = IJ.openImage(filein221);
				filein222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,zs2,ys2,xs2);
				fileout222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,zs2,ys2,xs2);
				img222 = IJ.openImage(filein222);
			}
		}
		
		configuration[axis] = 1 - inverted;
	}
	
	
	/*** doexchange ***
     * halo exchanges -> calls appropriate "doexchangetype"-function according to current configuration
     * 
     * @see  #doexchangetype000
     * @see  #doexchangetype001
     * @see  #doexchangetype010
     * @see  #doexchangetype011
     * @see  #doexchangetype100
     * @see  #doexchangetype101
     * @see  #doexchangetype110
     * @see  #doexchangetype111
     ***/
	private void doexchange(){
		//TODO: handle boundary case (clean exchange before y or z move)
		IJ.log(String.valueOf(configuration[0])+String.valueOf(configuration[1])+String.valueOf(configuration[2]));
		if (configuration[0] == 0){
			if (configuration[1] == 0){
				if (configuration[2] == 0){
					doexchangetype000();
				}else{
					doexchangetype001();
				}
			}else{
				if (configuration[2] == 0){
					doexchangetype010();
				}else{
					doexchangetype011();
				}
			}
		}else{
			if (configuration[1] == 0){
				if (configuration[2] == 0){
					doexchangetype100();
				}else{
					doexchangetype101();
				}
			}else{
				if (configuration[2] == 0){
					doexchangetype110();
				}else{
					doexchangetype111();
				}
			}
		}
	}
	
	/*** doexchangetype000 ***
     * current arrangement:
     * i111 i112
     * i121 i122
     * 
     * i211 i212
     * i221 i222
     * 
     ***/
	@SuppressWarnings("unused")
	private void doexchangetype000(){
		IJ.log("---000--------------");
		IJ.log(filein111 + "|" + filein112);
		IJ.log(filein121 + "|" + filein122);
		IJ.log("--------------------");
		
		//exchanging img111 and img112 (x-direction change)
		tempX1 = img111.getWidth()-2*haloX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-haloY-stepY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-haloZ-stepZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2 = img112.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc1.set(xb, y, proc2.get(xbo, y));
				}
			}
		}
		/*
		//exchanging img121 and img122 (x-direction change)
		tempX1 = img121.getWidth()-2*haloX;
		tempX2 = img121.getWidth()-haloX;
		tempY1 = img121.getHeight()-haloY-stepY;
		tempY2 = img121.getHeight()-haloY;
		tempZ1 = img121.getStackSize()-haloZ-stepZ;
		tempZ2 = img121.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc21 = img121.getStack().getProcessor(z);
			ImageProcessor proc22 = img122.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc22.set(xao, y, proc21.get(x, y));
					proc21.set(xb, y, proc22.get(xbo, y));
				}
			}
		}
		*/
		//exchanging img111 and img121 (y-direction change)
		tempX1 = img111.getWidth()-haloX-stepX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-2*haloY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-haloZ-stepZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc11 = img111.getStack().getProcessor(z);
			ImageProcessor proc21 = img121.getStack().getProcessor(z);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc21.set(x, yao, proc11.get(x, y));
					proc11.set(x, yb, proc21.get(x, ybo));
				}
			}
		}
		/*
		//exchanging img112 and img122 (y-direction change)
		tempX1 = img112.getWidth()-haloX-stepX;
		tempX2 = img112.getWidth()-haloX;
		tempY1 = img112.getHeight()-2*haloY;
		tempY2 = img112.getHeight()-haloY;
		tempZ1 = img112.getStackSize()-haloZ-stepZ;
		tempZ2 = img112.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc12 = img112.getStack().getProcessor(z);
			ImageProcessor proc22 = img122.getStack().getProcessor(z);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc22.set(x, yao, proc12.get(x, y));
					proc12.set(x, yb, proc22.get(x, ybo));
				}
			}
		}
		*/
		//exchanging img111 and img211 (z-direction change)
		tempX1 = img111.getWidth()-haloX-stepX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-haloY-stepY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-2*haloZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2 = img211.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(zb);
			ImageProcessor proc2 = img211.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		/**edges**/
		//exchanging img111 and img122 and virtual exchange of img112 and img121 (xy-edge change)
		tempX1 = img111.getWidth()-2*haloX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-2*haloY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-haloZ-stepZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2 = img122.getStack().getProcessor(z);
			ImageProcessor proc1v = img112.getStack().getProcessor(z);
			ImageProcessor proc2v = img121.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc2v.set(xb, yao, proc1v.get(xbo, y));
					proc1v.set(xao, yb, proc2v.get(x, ybo));
				}
			}
		}
		
		//exchanging img111 and img212 and virtual exchange of img112 and img211 (xz-edge change)
		tempX1 = img111.getWidth()-2*haloX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-haloY-stepY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-2*haloZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2 = img212.getStack().getProcessor(zao);
			ImageProcessor proc1v = img112.getStack().getProcessor(z);
			ImageProcessor proc2v = img211.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc2v.set(xb, y, proc1v.get(xbo, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(zb);
			ImageProcessor proc2 = img212.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img112.getStack().getProcessor(zb);
			ImageProcessor proc2v = img211.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(xb, y, proc2.get(xbo, y));
					proc1v.set(xao, y, proc2v.get(x, y));
				}
			}
		}
		//exchanging img111 and img221 and virtual exchange of img121 and img211 (yz-edge change)
		tempX1 = img111.getWidth()-haloX-stepX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-2*haloY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-2*haloZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2 = img221.getStack().getProcessor(zao);
			ImageProcessor proc1v = img121.getStack().getProcessor(z);
			ImageProcessor proc2v = img211.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc2v.set(x, yb, proc1v.get(x, ybo));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(zb);
			ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img121.getStack().getProcessor(zb);
			ImageProcessor proc2v = img211.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(x, yb, proc2.get(x, ybo));
					proc1v.set(x, yao, proc2v.get(x, y));
				}
			}
		}
		/**corners**/
		//exchanging img111 and img222 and virtual exchange of img112 and img221 as well as img121 and img212 as well as img211 and img122 (yz-edge change)
		tempX1 = img111.getWidth()-2*haloX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-2*haloY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-2*haloZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2 = img222.getStack().getProcessor(zao);
			ImageProcessor proc1v1 = img112.getStack().getProcessor(z);
			ImageProcessor proc2v1 = img221.getStack().getProcessor(zao);
			ImageProcessor proc1v2 = img121.getStack().getProcessor(z);
			ImageProcessor proc2v2 = img212.getStack().getProcessor(zao);
			ImageProcessor proc1v3 = img122.getStack().getProcessor(z);
			ImageProcessor proc2v3 = img211.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc2v1.set(xb, yao, proc1v1.get(xbo, y));
					proc2v2.set(xao, yb, proc1v2.get(x, ybo));
					proc2v3.set(xb, yb, proc1v3.get(xbo, ybo));
				}
			}
		}
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(zb);
			ImageProcessor proc2 = img222.getStack().getProcessor(zbo);
			ImageProcessor proc1v1 = img112.getStack().getProcessor(zb);
			ImageProcessor proc2v1 = img221.getStack().getProcessor(zbo);
			ImageProcessor proc1v2 = img121.getStack().getProcessor(zb);
			ImageProcessor proc2v2 = img212.getStack().getProcessor(zbo);
			ImageProcessor proc1v3 = img122.getStack().getProcessor(zb);
			ImageProcessor proc2v3 = img211.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc1v1.set(xao, yb, proc2v1.get(x, ybo));
					proc1v2.set(xb, yao, proc2v2.get(xbo, y));
					proc1v3.set(xao, yao, proc2v3.get(x, y));
				}
			}
		}
	}
	
	/*** doexchangetype001 ***
     * current arrangement:
	 * i112 i111
	 * i122 i121
	 * 
	 * i212 i211
	 * i222 i221 
     ***/
	@SuppressWarnings("unused")
	private void doexchangetype001(){
		
		IJ.log("---001--------------");
		IJ.log(filein112 + "|" + filein111);
		IJ.log(filein122 + "|" + filein121);
		IJ.log("--------------------");
		
		//exchanging img112 and img111 (x-direction change)
		tempX1 = img112.getWidth()-2*haloX;
		tempX2 = img112.getWidth()-haloX;
		tempY1 = img112.getHeight()-haloY-stepY;
		tempY2 = img112.getHeight()-haloY;
		tempZ1 = img112.getStackSize()-haloZ-stepZ;
		tempZ2 = img112.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(z);
			ImageProcessor proc2 = img111.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc1.set(xb, y, proc2.get(xbo, y));
				}
			}
		}
		//exchanging img112 and img122 (y-direction change)
		tempX1 = img112.getWidth()-haloX-stepX;
		tempX2 = img112.getWidth()-haloX;
		tempY1 = img112.getHeight()-2*haloY;
		tempY2 = img112.getHeight()-haloY;
		tempZ1 = img112.getStackSize()-haloZ-stepZ;
		tempZ2 = img112.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(z);
			ImageProcessor proc2 = img122.getStack().getProcessor(z);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc1.set(x, yb, proc2.get(x, ybo));
				}
			}
		}
		//exchanging img112 and img212 (z-direction change)
		tempX1 = img112.getWidth()-haloX-stepX;
		tempX2 = img112.getWidth()-haloX;
		tempY1 = img112.getHeight()-haloY-stepY;
		tempY2 = img112.getHeight()-haloY;
		tempZ1 = img112.getStackSize()-2*haloZ;
		tempZ2 = img112.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(z);
			ImageProcessor proc2 = img212.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(zb);
			ImageProcessor proc2 = img212.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		/**edges**/
		//exchanging img112 and img121 and virtual exchange of img111 and img122 (xy-edge change)
		tempX1 = img112.getWidth()-2*haloX;
		tempX2 = img112.getWidth()-haloX;
		tempY1 = img112.getHeight()-2*haloY;
		tempY2 = img112.getHeight()-haloY;
		tempZ1 = img112.getStackSize()-haloZ-stepZ;
		tempZ2 = img112.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(z);
			ImageProcessor proc2 = img121.getStack().getProcessor(z);
			ImageProcessor proc1v = img111.getStack().getProcessor(z);
			ImageProcessor proc2v = img122.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc2v.set(xb, yao, proc1v.get(xbo, y));
					proc1v.set(xao, yb, proc2v.get(x, ybo));
				}
			}
		}
		//exchanging img112 and img211 and virtual exchange of img111 and img212 (xz-edge change)
		tempX1 = img112.getWidth()-2*haloX;
		tempX2 = img112.getWidth()-haloX;
		tempY1 = img112.getHeight()-haloY-stepY;
		tempY2 = img112.getHeight()-haloY;
		tempZ1 = img112.getStackSize()-2*haloZ;
		tempZ2 = img112.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(z);
			ImageProcessor proc2 = img211.getStack().getProcessor(zao);
			ImageProcessor proc1v = img111.getStack().getProcessor(z);
			ImageProcessor proc2v = img212.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc2v.set(xb, y, proc1v.get(xbo, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(zb);
			ImageProcessor proc2 = img211.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img111.getStack().getProcessor(zb);
			ImageProcessor proc2v = img212.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(xb, y, proc2.get(xbo, y));
					proc1v.set(xao, y, proc2v.get(x, y));
				}
			}
		}
		//exchanging img112 and img222 and virtual exchange of img122 and img212 (yz-edge change)
		tempX1 = img112.getWidth()-haloX-stepX;
		tempX2 = img112.getWidth()-haloX;
		tempY1 = img112.getHeight()-2*haloY;
		tempY2 = img112.getHeight()-haloY;
		tempZ1 = img112.getStackSize()-2*haloZ;
		tempZ2 = img112.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(z);
			ImageProcessor proc2 = img222.getStack().getProcessor(zao);
			ImageProcessor proc1v = img122.getStack().getProcessor(z);
			ImageProcessor proc2v = img212.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc2v.set(x, yb, proc1v.get(x, ybo));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(zb);
			ImageProcessor proc2 = img222.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img122.getStack().getProcessor(zb);
			ImageProcessor proc2v = img212.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(x, yb, proc2.get(x, ybo));
					proc1v.set(x, yao, proc2v.get(x, y));
				}
			}
		}
		/**corners**/
		//exchanging img112 and img221 and virtual exchange of img111 and img222 as well as img122 and img211 as well as img212 and img121 (yz-edge change)
		tempX1 = img112.getWidth()-2*haloX;
		tempX2 = img112.getWidth()-haloX;
		tempY1 = img112.getHeight()-2*haloY;
		tempY2 = img112.getHeight()-haloY;
		tempZ1 = img112.getStackSize()-2*haloZ;
		tempZ2 = img112.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(z);
			ImageProcessor proc2 = img221.getStack().getProcessor(zao);
			ImageProcessor proc1v1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2v1 = img222.getStack().getProcessor(zao);
			ImageProcessor proc1v2 = img122.getStack().getProcessor(z);
			ImageProcessor proc2v2 = img211.getStack().getProcessor(zao);
			ImageProcessor proc1v3 = img121.getStack().getProcessor(z);
			ImageProcessor proc2v3 = img212.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc2v1.set(xb, yao, proc1v1.get(xbo, y));
					proc2v2.set(xao, yb, proc1v2.get(x, ybo));
					proc2v3.set(xb, yb, proc1v3.get(xbo, ybo));
				}
			}
		}
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img112.getStack().getProcessor(zb);
			ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
			ImageProcessor proc1v1 = img111.getStack().getProcessor(zb);
			ImageProcessor proc2v1 = img222.getStack().getProcessor(zbo);
			ImageProcessor proc1v2 = img122.getStack().getProcessor(zb);
			ImageProcessor proc2v2 = img211.getStack().getProcessor(zbo);
			ImageProcessor proc1v3 = img121.getStack().getProcessor(zb);
			ImageProcessor proc2v3 = img212.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc1v1.set(xao, yb, proc2v1.get(x, ybo));
					proc1v2.set(xb, yao, proc2v2.get(xbo, y));
					proc1v3.set(xao, yao, proc2v3.get(x, y));
				}
			}
		}
	}
	
	/*** doexchangetype010 ***
     * current arrangement:
	 * i121 i122
	 * i111 i112
	 * 
	 * i221 i222
	 * i211 i212 
     ***/
	@SuppressWarnings("unused")
	private void doexchangetype010(){
		
		IJ.log("---010--------------");
		IJ.log(filein121 + "|" + filein122);
		IJ.log(filein111 + "|" + filein112);
		IJ.log("--------------------");
		
		/**planes**/
		//exchanging img121 and img122 (x-direction change)
		tempX1 = img121.getWidth()-2*haloX;
		tempX2 = img121.getWidth()-haloX;
		tempY1 = img121.getHeight()-haloY-stepY;
		tempY2 = img121.getHeight()-haloY;
		tempZ1 = img121.getStackSize()-haloZ-stepZ;
		tempZ2 = img121.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(z);
			ImageProcessor proc2 = img122.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc1.set(xb, y, proc2.get(xbo, y));
				}
			}
		}
		//exchanging img121 and img111 (y-direction change)
		tempX1 = img121.getWidth()-haloX-stepX;
		tempX2 = img121.getWidth()-haloX;
		tempY1 = img121.getHeight()-2*haloY;
		tempY2 = img121.getHeight()-haloY;
		tempZ1 = img121.getStackSize()-haloZ-stepZ;
		tempZ2 = img121.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(z);
			ImageProcessor proc2 = img111.getStack().getProcessor(z);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc1.set(x, yb, proc2.get(x, ybo));
				}
			}
		}
		//exchanging img121 and img221 (z-direction change)
		tempX1 = img121.getWidth()-haloX-stepX;
		tempX2 = img121.getWidth()-haloX;
		tempY1 = img121.getHeight()-haloY-stepY;
		tempY2 = img121.getHeight()-haloY;
		tempZ1 = img121.getStackSize()-2*haloZ;
		tempZ2 = img121.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(z);
			ImageProcessor proc2 = img221.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(zb);
			ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		/**edges**/
		//exchanging img121 and img112 and virtual exchange of img122 and img111 (xy-edge change)
		tempX1 = img121.getWidth()-2*haloX;
		tempX2 = img121.getWidth()-haloX;
		tempY1 = img121.getHeight()-2*haloY;
		tempY2 = img121.getHeight()-haloY;
		tempZ1 = img121.getStackSize()-haloZ-stepZ;
		tempZ2 = img121.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(z);
			ImageProcessor proc2 = img112.getStack().getProcessor(z);
			ImageProcessor proc1v = img122.getStack().getProcessor(z);
			ImageProcessor proc2v = img111.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc2v.set(xb, yao, proc1v.get(xbo, y));
					proc1v.set(xao, yb, proc2v.get(x, ybo));
				}
			}
		}
		
		//exchanging img121 and img222 and virtual exchange of img122 and img221 (xz-edge change)
		tempX1 = img121.getWidth()-2*haloX;
		tempX2 = img121.getWidth()-haloX;
		tempY1 = img121.getHeight()-haloY-stepY;
		tempY2 = img121.getHeight()-haloY;
		tempZ1 = img121.getStackSize()-2*haloZ;
		tempZ2 = img121.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(z);
			ImageProcessor proc2 = img222.getStack().getProcessor(zao);
			ImageProcessor proc1v = img122.getStack().getProcessor(z);
			ImageProcessor proc2v = img221.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc2v.set(xb, y, proc1v.get(xbo, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(zb);
			ImageProcessor proc2 = img222.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img122.getStack().getProcessor(zb);
			ImageProcessor proc2v = img221.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(xb, y, proc2.get(xbo, y));
					proc1v.set(xao, y, proc2v.get(x, y));
				}
			}
		}
		//exchanging img121 and img211 and virtual exchange of img111 and img221 (yz-edge change)
		tempX1 = img121.getWidth()-haloX-stepX;
		tempX2 = img121.getWidth()-haloX;
		tempY1 = img121.getHeight()-2*haloY;
		tempY2 = img121.getHeight()-haloY;
		tempZ1 = img121.getStackSize()-2*haloZ;
		tempZ2 = img121.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(z);
			ImageProcessor proc2 = img211.getStack().getProcessor(zao);
			ImageProcessor proc1v = img111.getStack().getProcessor(z);
			ImageProcessor proc2v = img221.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc2v.set(x, yb, proc1v.get(x, ybo));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(zb);
			ImageProcessor proc2 = img211.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img111.getStack().getProcessor(zb);
			ImageProcessor proc2v = img221.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(x, yb, proc2.get(x, ybo));
					proc1v.set(x, yao, proc2v.get(x, y));
				}
			}
		}
		/**corners**/
		//exchanging img121 and img212 and virtual exchange of img122 and img211 as well as img111 and img222 as well as img221 and img112 (yz-edge change)
		tempX1 = img121.getWidth()-2*haloX;
		tempX2 = img121.getWidth()-haloX;
		tempY1 = img121.getHeight()-2*haloY;
		tempY2 = img121.getHeight()-haloY;
		tempZ1 = img121.getStackSize()-2*haloZ;
		tempZ2 = img121.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(z);
			ImageProcessor proc2 = img212.getStack().getProcessor(zao);
			ImageProcessor proc1v1 = img122.getStack().getProcessor(z);
			ImageProcessor proc2v1 = img211.getStack().getProcessor(zao);
			ImageProcessor proc1v2 = img111.getStack().getProcessor(z);
			ImageProcessor proc2v2 = img222.getStack().getProcessor(zao);
			ImageProcessor proc1v3 = img112.getStack().getProcessor(z);
			ImageProcessor proc2v3 = img221.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc2v1.set(xb, yao, proc1v1.get(xbo, y));
					proc2v2.set(xao, yb, proc1v2.get(x, ybo));
					proc2v3.set(xb, yb, proc1v3.get(xbo, ybo));
				}
			}
		}
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(zb);
			ImageProcessor proc2 = img212.getStack().getProcessor(zbo);
			ImageProcessor proc1v1 = img122.getStack().getProcessor(zb);
			ImageProcessor proc2v1 = img211.getStack().getProcessor(zbo);
			ImageProcessor proc1v2 = img111.getStack().getProcessor(zb);
			ImageProcessor proc2v2 = img222.getStack().getProcessor(zbo);
			ImageProcessor proc1v3 = img112.getStack().getProcessor(zb);
			ImageProcessor proc2v3 = img221.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc1v1.set(xao, yb, proc2v1.get(x, ybo));
					proc1v2.set(xb, yao, proc2v2.get(xbo, y));
					proc1v3.set(xao, yao, proc2v3.get(x, y));
				}
			}
		}
	}
	
	/*** doexchangetype011 ***
     * current arrangement:
	 * i122 i121
	 * i112 i111
	 * 
	 * i222 i221
	 * i212 i211
     ***/
	@SuppressWarnings("unused")
	private void doexchangetype011(){
		
		IJ.log("---011--------------");
		IJ.log(filein122 + "|" + filein121);
		IJ.log(filein112 + "|" + filein111);
		IJ.log("--------------------");
		
		//exchanging img122 and img121 (x-direction change)
		tempX1 = img122.getWidth()-2*haloX;
		tempX2 = img122.getWidth()-haloX;
		tempY1 = img122.getHeight()-haloY-stepY;
		tempY2 = img122.getHeight()-haloY;
		tempZ1 = img122.getStackSize()-haloZ-stepZ;
		tempZ2 = img122.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(z);
			ImageProcessor proc2 = img121.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc1.set(xb, y, proc2.get(xbo, y));
				}
			}
		}
		//exchanging img122 and img112 (y-direction change)
		tempX1 = img122.getWidth()-haloX-stepX;
		tempX2 = img122.getWidth()-haloX;
		tempY1 = img122.getHeight()-2*haloY;
		tempY2 = img122.getHeight()-haloY;
		tempZ1 = img122.getStackSize()-haloZ-stepZ;
		tempZ2 = img122.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(z);
			ImageProcessor proc2 = img112.getStack().getProcessor(z);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc1.set(x, yb, proc2.get(x, ybo));
				}
			}
		}
		//exchanging img122 and img222 (z-direction change)
		tempX1 = img122.getWidth()-haloX-stepX;
		tempX2 = img122.getWidth()-haloX;
		tempY1 = img122.getHeight()-haloY-stepY;
		tempY2 = img122.getHeight()-haloY;
		tempZ1 = img122.getStackSize()-2*haloZ;
		tempZ2 = img122.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(z);
			ImageProcessor proc2 = img222.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(zb);
			ImageProcessor proc2 = img222.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		/**edges**/
		//exchanging img122 and img111 and virtual exchange of img121 and img112 (xy-edge change)
		tempX1 = img122.getWidth()-2*haloX;
		tempX2 = img122.getWidth()-haloX;
		tempY1 = img122.getHeight()-2*haloY;
		tempY2 = img122.getHeight()-haloY;
		tempZ1 = img122.getStackSize()-haloZ-stepZ;
		tempZ2 = img122.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(z);
			ImageProcessor proc2 = img111.getStack().getProcessor(z);
			ImageProcessor proc1v = img121.getStack().getProcessor(z);
			ImageProcessor proc2v = img112.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc2v.set(xb, yao, proc1v.get(xbo, y));
					proc1v.set(xao, yb, proc2v.get(x, ybo));
				}
			}
		}
		
		//exchanging img122 and img221 and virtual exchange of img121 and img222 (xz-edge change)
		tempX1 = img122.getWidth()-2*haloX;
		tempX2 = img122.getWidth()-haloX;
		tempY1 = img122.getHeight()-haloY-stepY;
		tempY2 = img122.getHeight()-haloY;
		tempZ1 = img122.getStackSize()-2*haloZ;
		tempZ2 = img122.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(z);
			ImageProcessor proc2 = img221.getStack().getProcessor(zao);
			ImageProcessor proc1v = img121.getStack().getProcessor(z);
			ImageProcessor proc2v = img222.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc2v.set(xb, y, proc1v.get(xbo, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(zb);
			ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img121.getStack().getProcessor(zb);
			ImageProcessor proc2v = img222.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(xb, y, proc2.get(xbo, y));
					proc1v.set(xao, y, proc2v.get(x, y));
				}
			}
		}
		//exchanging img122 and img212 and virtual exchange of img112 and img222 (yz-edge change)
		tempX1 = img122.getWidth()-haloX-stepX;
		tempX2 = img122.getWidth()-haloX;
		tempY1 = img122.getHeight()-2*haloY;
		tempY2 = img122.getHeight()-haloY;
		tempZ1 = img122.getStackSize()-2*haloZ;
		tempZ2 = img122.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(z);
			ImageProcessor proc2 = img212.getStack().getProcessor(zao);
			ImageProcessor proc1v = img112.getStack().getProcessor(z);
			ImageProcessor proc2v = img222.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc2v.set(x, yb, proc1v.get(x, ybo));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(zb);
			ImageProcessor proc2 = img212.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img112.getStack().getProcessor(zb);
			ImageProcessor proc2v = img222.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(x, yb, proc2.get(x, ybo));
					proc1v.set(x, yao, proc2v.get(x, y));
				}
			}
		}
		/**corners**/
		//exchanging img122 and img211 and virtual exchange of img121 and img212 as well as img112 and img221 as well as img222 and img111 (yz-edge change)
		tempX1 = img122.getWidth()-2*haloX;
		tempX2 = img122.getWidth()-haloX;
		tempY1 = img122.getHeight()-2*haloY;
		tempY2 = img122.getHeight()-haloY;
		tempZ1 = img122.getStackSize()-2*haloZ;
		tempZ2 = img122.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(z);
			ImageProcessor proc2 = img211.getStack().getProcessor(zao);
			ImageProcessor proc1v1 = img121.getStack().getProcessor(z);
			ImageProcessor proc2v1 = img212.getStack().getProcessor(zao);
			ImageProcessor proc1v2 = img112.getStack().getProcessor(z);
			ImageProcessor proc2v2 = img221.getStack().getProcessor(zao);
			ImageProcessor proc1v3 = img111.getStack().getProcessor(z);
			ImageProcessor proc2v3 = img222.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc2v1.set(xb, yao, proc1v1.get(xbo, y));
					proc2v2.set(xao, yb, proc1v2.get(x, ybo));
					proc2v3.set(xb, yb, proc1v3.get(xbo, ybo));
				}
			}
		}
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(zb);
			ImageProcessor proc2 = img211.getStack().getProcessor(zbo);
			ImageProcessor proc1v1 = img121.getStack().getProcessor(zb);
			ImageProcessor proc2v1 = img212.getStack().getProcessor(zbo);
			ImageProcessor proc1v2 = img112.getStack().getProcessor(zb);
			ImageProcessor proc2v2 = img221.getStack().getProcessor(zbo);
			ImageProcessor proc1v3 = img111.getStack().getProcessor(zb);
			ImageProcessor proc2v3 = img222.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc1v1.set(xao, yb, proc2v1.get(x, ybo));
					proc1v2.set(xb, yao, proc2v2.get(xbo, y));
					proc1v3.set(xao, yao, proc2v3.get(x, y));
				}
			}
		}
	}

	/*** doexchangetype100 ***
     * current arrangement:
	 * i211 i212
	 * i221 i222
	 * 
	 * i111 i112
	 * i121 i122
     ***/
	@SuppressWarnings("unused")
	private void doexchangetype100(){
		
		IJ.log("---100--------------");
		IJ.log(filein211 + "|" + filein212);
		IJ.log(filein221 + "|" + filein222);
		IJ.log("--------------------");
		
		//exchanging img211 and img212 (x-direction change)
		tempX1 = img211.getWidth()-2*haloX;
		tempX2 = img211.getWidth()-haloX;
		tempY1 = img211.getHeight()-haloY-stepY;
		tempY2 = img211.getHeight()-haloY;
		tempZ1 = img211.getStackSize()-haloZ-stepZ;
		tempZ2 = img211.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(z);
			ImageProcessor proc2 = img212.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc1.set(xb, y, proc2.get(xbo, y));
				}
			}
		}
		//exchanging img211 and img221 (y-direction change)
		tempX1 = img211.getWidth()-haloX-stepX;
		tempX2 = img211.getWidth()-haloX;
		tempY1 = img211.getHeight()-2*haloY;
		tempY2 = img211.getHeight()-haloY;
		tempZ1 = img211.getStackSize()-haloZ-stepZ;
		tempZ2 = img211.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(z);
			ImageProcessor proc2 = img221.getStack().getProcessor(z);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc1.set(x, yb, proc2.get(x, ybo));
				}
			}
		}
		//exchanging img211 and img111 (z-direction change)
		tempX1 = img211.getWidth()-haloX-stepX;
		tempX2 = img211.getWidth()-haloX;
		tempY1 = img211.getHeight()-haloY-stepY;
		tempY2 = img211.getHeight()-haloY;
		tempZ1 = img211.getStackSize()-2*haloZ;
		tempZ2 = img211.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(z);
			ImageProcessor proc2 = img111.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
					//proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(zb);
			ImageProcessor proc2 = img111.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					//proc2.set(x, y, proc1.get(x, y));
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		/**edges**/
		//exchanging img211 and img222 and virtual exchange of img212 and img221 (xy-edge change)
		tempX1 = img211.getWidth()-2*haloX;
		tempX2 = img211.getWidth()-haloX;
		tempY1 = img211.getHeight()-2*haloY;
		tempY2 = img211.getHeight()-haloY;
		tempZ1 = img211.getStackSize()-haloZ-stepZ;
		tempZ2 = img211.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(z);
			ImageProcessor proc2 = img222.getStack().getProcessor(z);
			ImageProcessor proc1v = img212.getStack().getProcessor(z);
			ImageProcessor proc2v = img221.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc2v.set(xb, yao, proc1v.get(xbo, y));
					proc1v.set(xao, yb, proc2v.get(x, ybo));
				}
			}
		}
		
		//exchanging img211 and img112 and virtual exchange of img212 and img111 (xz-edge change)
		tempX1 = img211.getWidth()-2*haloX;
		tempX2 = img211.getWidth()-haloX;
		tempY1 = img211.getHeight()-haloY-stepY;
		tempY2 = img211.getHeight()-haloY;
		tempZ1 = img211.getStackSize()-2*haloZ;
		tempZ2 = img211.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(z);
			ImageProcessor proc2 = img112.getStack().getProcessor(zao);
			ImageProcessor proc1v = img212.getStack().getProcessor(z);
			ImageProcessor proc2v = img111.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc2v.set(xb, y, proc1v.get(xbo, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(zb);
			ImageProcessor proc2 = img112.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img212.getStack().getProcessor(zb);
			ImageProcessor proc2v = img111.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(xb, y, proc2.get(xbo, y));
					proc1v.set(xao, y, proc2v.get(x, y));
				}
			}
		}
		//exchanging img211 and img121 and virtual exchange of img221 and img111 (yz-edge change)
		tempX1 = img211.getWidth()-haloX-stepX;
		tempX2 = img211.getWidth()-haloX;
		tempY1 = img211.getHeight()-2*haloY;
		tempY2 = img211.getHeight()-haloY;
		tempZ1 = img211.getStackSize()-2*haloZ;
		tempZ2 = img211.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(z);
			ImageProcessor proc2 = img121.getStack().getProcessor(zao);
			ImageProcessor proc1v = img221.getStack().getProcessor(z);
			ImageProcessor proc2v = img111.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc2v.set(x, yb, proc1v.get(x, ybo));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(zb);
			ImageProcessor proc2 = img121.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img221.getStack().getProcessor(zb);
			ImageProcessor proc2v = img111.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(x, yb, proc2.get(x, ybo));
					proc1v.set(x, yao, proc2v.get(x, y));
				}
			}
		}
		/**corners**/
		//exchanging img211 and img122 and virtual exchange of img212 and img121 as well as img221 and img112 as well as img111 and img222 (yz-edge change)
		tempX1 = img211.getWidth()-2*haloX;
		tempX2 = img211.getWidth()-haloX;
		tempY1 = img211.getHeight()-2*haloY;
		tempY2 = img211.getHeight()-haloY;
		tempZ1 = img211.getStackSize()-2*haloZ;
		tempZ2 = img211.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(z);
			ImageProcessor proc2 = img122.getStack().getProcessor(zao);
			ImageProcessor proc1v1 = img212.getStack().getProcessor(z);
			ImageProcessor proc2v1 = img121.getStack().getProcessor(zao);
			ImageProcessor proc1v2 = img221.getStack().getProcessor(z);
			ImageProcessor proc2v2 = img112.getStack().getProcessor(zao);
			ImageProcessor proc1v3 = img222.getStack().getProcessor(z);
			ImageProcessor proc2v3 = img111.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc2v1.set(xb, yao, proc1v1.get(xbo, y));
					proc2v2.set(xao, yb, proc1v2.get(x, ybo));
					proc2v3.set(xb, yb, proc1v3.get(xbo, ybo));
				}
			}
		}
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img211.getStack().getProcessor(zb);
			ImageProcessor proc2 = img122.getStack().getProcessor(zbo);
			ImageProcessor proc1v1 = img212.getStack().getProcessor(zb);
			ImageProcessor proc2v1 = img121.getStack().getProcessor(zbo);
			ImageProcessor proc1v2 = img221.getStack().getProcessor(zb);
			ImageProcessor proc2v2 = img112.getStack().getProcessor(zbo);
			ImageProcessor proc1v3 = img222.getStack().getProcessor(zb);
			ImageProcessor proc2v3 = img111.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc1v1.set(xao, yb, proc2v1.get(x, ybo));
					proc1v2.set(xb, yao, proc2v2.get(xbo, y));
					proc1v3.set(xao, yao, proc2v3.get(x, y));
				}
			}
		}
	}
	
	/*** doexchangetype101 ***
     * current arrangement:
	 * i212 i211
	 * i222 i221
	 * 
	 * i112 i111
	 * i122 i121
     ***/
	@SuppressWarnings("unused")
	private void doexchangetype101(){
		
		IJ.log("---101--------------");
		IJ.log(filein212 + "|" + filein211);
		IJ.log(filein222 + "|" + filein221);
		IJ.log("--------------------");
		
		//exchanging img212 and img211 (x-direction change)
		tempX1 = img212.getWidth()-2*haloX;
		tempX2 = img212.getWidth()-haloX;
		tempY1 = img212.getHeight()-haloY-stepY;
		tempY2 = img212.getHeight()-haloY;
		tempZ1 = img212.getStackSize()-haloZ-stepZ;
		tempZ2 = img212.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(z);
			ImageProcessor proc2 = img211.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc1.set(xb, y, proc2.get(xbo, y));
				}
			}
		}
		//exchanging img212 and img222 (y-direction change)
		tempX1 = img212.getWidth()-haloX-stepX;
		tempX2 = img212.getWidth()-haloX;
		tempY1 = img212.getHeight()-2*haloY;
		tempY2 = img212.getHeight()-haloY;
		tempZ1 = img212.getStackSize()-haloZ-stepZ;
		tempZ2 = img212.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(z);
			ImageProcessor proc2 = img222.getStack().getProcessor(z);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc1.set(x, yb, proc2.get(x, ybo));
				}
			}
		}
		//exchanging img212 and img112 (z-direction change)
		tempX1 = img212.getWidth()-haloX-stepX;
		tempX2 = img212.getWidth()-haloX;
		tempY1 = img212.getHeight()-haloY-stepY;
		tempY2 = img212.getHeight()-haloY;
		tempZ1 = img212.getStackSize()-2*haloZ;
		tempZ2 = img212.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(z);
			ImageProcessor proc2 = img112.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
					//proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(zb);
			ImageProcessor proc2 = img112.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					//proc2.set(x, y, proc1.get(x, y));
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		/**edges**/
		//exchanging img212 and img221 and virtual exchange of img211 and img222 (xy-edge change)
		tempX1 = img212.getWidth()-2*haloX;
		tempX2 = img212.getWidth()-haloX;
		tempY1 = img212.getHeight()-2*haloY;
		tempY2 = img212.getHeight()-haloY;
		tempZ1 = img212.getStackSize()-haloZ-stepZ;
		tempZ2 = img212.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(z);
			ImageProcessor proc2 = img221.getStack().getProcessor(z);
			ImageProcessor proc1v = img211.getStack().getProcessor(z);
			ImageProcessor proc2v = img222.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc2v.set(xb, yao, proc1v.get(xbo, y));
					proc1v.set(xao, yb, proc2v.get(x, ybo));
				}
			}
		}
		//exchanging img212 and img111 and virtual exchange of img211 and img112 (xz-edge change)
		tempX1 = img212.getWidth()-2*haloX;
		tempX2 = img212.getWidth()-haloX;
		tempY1 = img212.getHeight()-haloY-stepY;
		tempY2 = img212.getHeight()-haloY;
		tempZ1 = img212.getStackSize()-2*haloZ;
		tempZ2 = img212.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(z);
			ImageProcessor proc2 = img111.getStack().getProcessor(zao);
			ImageProcessor proc1v = img211.getStack().getProcessor(z);
			ImageProcessor proc2v = img112.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc2v.set(xb, y, proc1v.get(xbo, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(zb);
			ImageProcessor proc2 = img111.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img211.getStack().getProcessor(zb);
			ImageProcessor proc2v = img112.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(xb, y, proc2.get(xbo, y));
					proc1v.set(xao, y, proc2v.get(x, y));
				}
			}
		}
		//exchanging img212 and img122 and virtual exchange of img222 and img112 (yz-edge change)
		tempX1 = img212.getWidth()-haloX-stepX;
		tempX2 = img212.getWidth()-haloX;
		tempY1 = img212.getHeight()-2*haloY;
		tempY2 = img212.getHeight()-haloY;
		tempZ1 = img212.getStackSize()-2*haloZ;
		tempZ2 = img212.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(z);
			ImageProcessor proc2 = img122.getStack().getProcessor(zao);
			ImageProcessor proc1v = img222.getStack().getProcessor(z);
			ImageProcessor proc2v = img112.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc2v.set(x, yb, proc1v.get(x, ybo));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(zb);
			ImageProcessor proc2 = img122.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img222.getStack().getProcessor(zb);
			ImageProcessor proc2v = img112.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(x, yb, proc2.get(x, ybo));
					proc1v.set(x, yao, proc2v.get(x, y));
				}
			}
		}
		/**corners**/
		//exchanging img212 and img121 and virtual exchange of img211 and img122 as well as img222 and img111 as well as img112 and img221 (yz-edge change)
		tempX1 = img212.getWidth()-2*haloX;
		tempX2 = img212.getWidth()-haloX;
		tempY1 = img212.getHeight()-2*haloY;
		tempY2 = img212.getHeight()-haloY;
		tempZ1 = img212.getStackSize()-2*haloZ;
		tempZ2 = img212.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(z);
			ImageProcessor proc2 = img121.getStack().getProcessor(zao);
			ImageProcessor proc1v1 = img211.getStack().getProcessor(z);
			ImageProcessor proc2v1 = img122.getStack().getProcessor(zao);
			ImageProcessor proc1v2 = img222.getStack().getProcessor(z);
			ImageProcessor proc2v2 = img111.getStack().getProcessor(zao);
			ImageProcessor proc1v3 = img221.getStack().getProcessor(z);
			ImageProcessor proc2v3 = img112.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc2v1.set(xb, yao, proc1v1.get(xbo, y));
					proc2v2.set(xao, yb, proc1v2.get(x, ybo));
					proc2v3.set(xb, yb, proc1v3.get(xbo, ybo));
				}
			}
		}
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img212.getStack().getProcessor(zb);
			ImageProcessor proc2 = img121.getStack().getProcessor(zbo);
			ImageProcessor proc1v1 = img211.getStack().getProcessor(zb);
			ImageProcessor proc2v1 = img122.getStack().getProcessor(zbo);
			ImageProcessor proc1v2 = img222.getStack().getProcessor(zb);
			ImageProcessor proc2v2 = img111.getStack().getProcessor(zbo);
			ImageProcessor proc1v3 = img221.getStack().getProcessor(zb);
			ImageProcessor proc2v3 = img112.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc1v1.set(xao, yb, proc2v1.get(x, ybo));
					proc1v2.set(xb, yao, proc2v2.get(xbo, y));
					proc1v3.set(xao, yao, proc2v3.get(x, y));
				}
			}
		}
	}
	
	/*** doexchangetype110 ***
     * current arrangement:
	 * i221 i222
	 * i211 i212
	 * 
	 * i121 i122
	 * i111 i112
     ***/
	@SuppressWarnings("unused")
	private void doexchangetype110(){
		
		IJ.log("---110--------------");
		IJ.log(filein221 + "|" + filein222);
		IJ.log(filein211 + "|" + filein212);
		IJ.log("--------------------");
		
		//exchanging img221 and img222 (x-direction change)
		tempX1 = img221.getWidth()-2*haloX;
		tempX2 = img221.getWidth()-haloX;
		tempY1 = img221.getHeight()-haloY-stepY;
		tempY2 = img221.getHeight()-haloY;
		tempZ1 = img221.getStackSize()-haloZ-stepZ;
		tempZ2 = img221.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(z);
			ImageProcessor proc2 = img222.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc1.set(xb, y, proc2.get(xbo, y));
				}
			}
		}
		//exchanging img221 and img211 (y-direction change)
		tempX1 = img221.getWidth()-haloX-stepX;
		tempX2 = img221.getWidth()-haloX;
		tempY1 = img221.getHeight()-2*haloY;
		tempY2 = img221.getHeight()-haloY;
		tempZ1 = img221.getStackSize()-haloZ-stepZ;
		tempZ2 = img221.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(z);
			ImageProcessor proc2 = img211.getStack().getProcessor(z);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc1.set(x, yb, proc2.get(x, ybo));
				}
			}
		}
		//exchanging img221 and img121 (z-direction change)
		tempX1 = img221.getWidth()-haloX-stepX;
		tempX2 = img221.getWidth()-haloX;
		tempY1 = img221.getHeight()-haloY-stepY;
		tempY2 = img221.getHeight()-haloY;
		tempZ1 = img221.getStackSize()-2*haloZ;
		tempZ2 = img221.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(z);
			ImageProcessor proc2 = img121.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
					//proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(zb);
			ImageProcessor proc2 = img121.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					//proc2.set(x, y, proc1.get(x, y));
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		/**edges**/
		//exchanging img221 and img212 and virtual exchange of img222 and img211 (xy-edge change)
		tempX1 = img221.getWidth()-2*haloX;
		tempX2 = img221.getWidth()-haloX;
		tempY1 = img221.getHeight()-2*haloY;
		tempY2 = img221.getHeight()-haloY;
		tempZ1 = img221.getStackSize()-haloZ-stepZ;
		tempZ2 = img221.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(z);
			ImageProcessor proc2 = img212.getStack().getProcessor(z);
			ImageProcessor proc1v = img222.getStack().getProcessor(z);
			ImageProcessor proc2v = img211.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc2v.set(xb, yao, proc1v.get(xbo, y));
					proc1v.set(xao, yb, proc2v.get(x, ybo));
				}
			}
		}
		
		//exchanging img221 and img122 and virtual exchange of img222 and img121 (xz-edge change)
		tempX1 = img221.getWidth()-2*haloX;
		tempX2 = img221.getWidth()-haloX;
		tempY1 = img221.getHeight()-haloY-stepY;
		tempY2 = img221.getHeight()-haloY;
		tempZ1 = img221.getStackSize()-2*haloZ;
		tempZ2 = img221.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(z);
			ImageProcessor proc2 = img122.getStack().getProcessor(zao);
			ImageProcessor proc1v = img222.getStack().getProcessor(z);
			ImageProcessor proc2v = img121.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc2v.set(xb, y, proc1v.get(xbo, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(zb);
			ImageProcessor proc2 = img122.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img222.getStack().getProcessor(zb);
			ImageProcessor proc2v = img121.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(xb, y, proc2.get(xbo, y));
					proc1v.set(xao, y, proc2v.get(x, y));
				}
			}
		}
		//exchanging img221 and img111 and virtual exchange of img211 and img121 (yz-edge change)
		tempX1 = img221.getWidth()-haloX-stepX;
		tempX2 = img221.getWidth()-haloX;
		tempY1 = img221.getHeight()-2*haloY;
		tempY2 = img221.getHeight()-haloY;
		tempZ1 = img221.getStackSize()-2*haloZ;
		tempZ2 = img221.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(z);
			ImageProcessor proc2 = img111.getStack().getProcessor(zao);
			ImageProcessor proc1v = img211.getStack().getProcessor(z);
			ImageProcessor proc2v = img121.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc2v.set(x, yb, proc1v.get(x, ybo));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(zb);
			ImageProcessor proc2 = img111.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img211.getStack().getProcessor(zb);
			ImageProcessor proc2v = img121.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(x, yb, proc2.get(x, ybo));
					proc1v.set(x, yao, proc2v.get(x, y));
				}
			}
		}
		/**corners**/
		//exchanging img221 and img112 and virtual exchange of img222 and img111 as well as img211 and img122 as well as img121 and img212 (yz-edge change)
		tempX1 = img221.getWidth()-2*haloX;
		tempX2 = img221.getWidth()-haloX;
		tempY1 = img221.getHeight()-2*haloY;
		tempY2 = img221.getHeight()-haloY;
		tempZ1 = img221.getStackSize()-2*haloZ;
		tempZ2 = img221.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(z);
			ImageProcessor proc2 = img112.getStack().getProcessor(zao);
			ImageProcessor proc1v1 = img222.getStack().getProcessor(z);
			ImageProcessor proc2v1 = img111.getStack().getProcessor(zao);
			ImageProcessor proc1v2 = img211.getStack().getProcessor(z);
			ImageProcessor proc2v2 = img122.getStack().getProcessor(zao);
			ImageProcessor proc1v3 = img212.getStack().getProcessor(z);
			ImageProcessor proc2v3 = img121.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc2v1.set(xb, yao, proc1v1.get(xbo, y));
					proc2v2.set(xao, yb, proc1v2.get(x, ybo));
					proc2v3.set(xb, yb, proc1v3.get(xbo, ybo));
				}
			}
		}
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img221.getStack().getProcessor(zb);
			ImageProcessor proc2 = img112.getStack().getProcessor(zbo);
			ImageProcessor proc1v1 = img222.getStack().getProcessor(zb);
			ImageProcessor proc2v1 = img111.getStack().getProcessor(zbo);
			ImageProcessor proc1v2 = img211.getStack().getProcessor(zb);
			ImageProcessor proc2v2 = img122.getStack().getProcessor(zbo);
			ImageProcessor proc1v3 = img212.getStack().getProcessor(zb);
			ImageProcessor proc2v3 = img121.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc1v1.set(xao, yb, proc2v1.get(x, ybo));
					proc1v2.set(xb, yao, proc2v2.get(xbo, y));
					proc1v3.set(xao, yao, proc2v3.get(x, y));
				}
			}
		}
	}
	
	/*** doexchangetype111 ***
     * current arrangement:
	 * i222 i221
	 * i212 i211
	 * 
	 * i122 i121
	 * i112 i111
     ***/
	@SuppressWarnings("unused")
	private void doexchangetype111(){
		
		IJ.log("---111--------------");
		IJ.log(filein222 + "|" + filein221);
		IJ.log(filein212 + "|" + filein211);
		IJ.log("--------------------");
		
		//exchanging img222 and img221 (x-direction change)
		tempX1 = img222.getWidth()-2*haloX;
		tempX2 = img222.getWidth()-haloX;
		tempY1 = img222.getHeight()-haloY-stepY;
		tempY2 = img222.getHeight()-haloY;
		tempZ1 = img222.getStackSize()-haloZ-stepZ;
		tempZ2 = img222.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(z);
			ImageProcessor proc2 = img221.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc1.set(xb, y, proc2.get(xbo, y));
				}
			}
		}
		//exchanging img222 and img212 (y-direction change)
		tempX1 = img222.getWidth()-haloX-stepX;
		tempX2 = img222.getWidth()-haloX;
		tempY1 = img222.getHeight()-2*haloY;
		tempY2 = img222.getHeight()-haloY;
		tempZ1 = img222.getStackSize()-haloZ-stepZ;
		tempZ2 = img222.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(z);
			ImageProcessor proc2 = img212.getStack().getProcessor(z);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc1.set(x, yb, proc2.get(x, ybo));
				}
			}
		}
		//exchanging img222 and img122 (z-direction change)
		tempX1 = img222.getWidth()-haloX-stepX;
		tempX2 = img222.getWidth()-haloX;
		tempY1 = img222.getHeight()-haloY-stepY;
		tempY2 = img222.getHeight()-haloY;
		tempZ1 = img222.getStackSize()-2*haloZ;
		tempZ2 = img222.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(z);
			ImageProcessor proc2 = img122.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
					//proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(zb);
			ImageProcessor proc2 = img122.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					//proc2.set(x, y, proc1.get(x, y));
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		
		/**edges**/
		//exchanging img222 and img211 and virtual exchange of img221 and img212 (xy-edge change)
		tempX1 = img222.getWidth()-2*haloX;
		tempX2 = img222.getWidth()-haloX;
		tempY1 = img222.getHeight()-2*haloY;
		tempY2 = img222.getHeight()-haloY;
		tempZ1 = img222.getStackSize()-haloZ-stepZ;
		tempZ2 = img222.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(z);
			ImageProcessor proc2 = img211.getStack().getProcessor(z);
			ImageProcessor proc1v = img221.getStack().getProcessor(z);
			ImageProcessor proc2v = img212.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc2v.set(xb, yao, proc1v.get(xbo, y));
					proc1v.set(xao, yb, proc2v.get(x, ybo));
				}
			}
		}
		
		//exchanging img222 and img121 and virtual exchange of img221 and img122 (xz-edge change)
		tempX1 = img222.getWidth()-2*haloX;
		tempX2 = img222.getWidth()-haloX;
		tempY1 = img222.getHeight()-haloY-stepY;
		tempY2 = img222.getHeight()-haloY;
		tempZ1 = img222.getStackSize()-2*haloZ;
		tempZ2 = img222.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(z);
			ImageProcessor proc2 = img121.getStack().getProcessor(zao);
			ImageProcessor proc1v = img221.getStack().getProcessor(z);
			ImageProcessor proc2v = img122.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc2v.set(xb, y, proc1v.get(xbo, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(zb);
			ImageProcessor proc2 = img121.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img221.getStack().getProcessor(zb);
			ImageProcessor proc2v = img122.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(xb, y, proc2.get(xbo, y));
					proc1v.set(xao, y, proc2v.get(x, y));
				}
			}
		}
		//exchanging img222 and img112 and virtual exchange of img212 and img122 (yz-edge change)
		tempX1 = img222.getWidth()-haloX-stepX;
		tempX2 = img222.getWidth()-haloX;
		tempY1 = img222.getHeight()-2*haloY;
		tempY2 = img222.getHeight()-haloY;
		tempZ1 = img222.getStackSize()-2*haloZ;
		tempZ2 = img222.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(z);
			ImageProcessor proc2 = img112.getStack().getProcessor(zao);
			ImageProcessor proc1v = img212.getStack().getProcessor(z);
			ImageProcessor proc2v = img122.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc2v.set(x, yb, proc1v.get(x, ybo));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(zb);
			ImageProcessor proc2 = img112.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img212.getStack().getProcessor(zb);
			ImageProcessor proc2v = img122.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(x, yb, proc2.get(x, ybo));
					proc1v.set(x, yao, proc2v.get(x, y));
				}
			}
		}
		/**corners**/
		//exchanging img222 and img111 and virtual exchange of img221 and img112 as well as img212 and img121 as well as img122 and img211 (yz-edge change)
		tempX1 = img222.getWidth()-2*haloX;
		tempX2 = img222.getWidth()-haloX;
		tempY1 = img222.getHeight()-2*haloY;
		tempY2 = img222.getHeight()-haloY;
		tempZ1 = img222.getStackSize()-2*haloZ;
		tempZ2 = img222.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(z);
			ImageProcessor proc2 = img111.getStack().getProcessor(zao);
			ImageProcessor proc1v1 = img221.getStack().getProcessor(z);
			ImageProcessor proc2v1 = img112.getStack().getProcessor(zao);
			ImageProcessor proc1v2 = img212.getStack().getProcessor(z);
			ImageProcessor proc2v2 = img121.getStack().getProcessor(zao);
			ImageProcessor proc1v3 = img211.getStack().getProcessor(z);
			ImageProcessor proc2v3 = img122.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc2v1.set(xb, yao, proc1v1.get(xbo, y));
					proc2v2.set(xao, yb, proc1v2.get(x, ybo));
					proc2v3.set(xb, yb, proc1v3.get(xbo, ybo));
				}
			}
		}
		for (int z=tempZ1+1, zao = 1, zb=tempZ2+1, zbo = haloZ+1; z<=tempZ2; z++, zao++, zb++, zbo++) {
			ImageProcessor proc1 = img222.getStack().getProcessor(zb);
			ImageProcessor proc2 = img111.getStack().getProcessor(zbo);
			ImageProcessor proc1v1 = img221.getStack().getProcessor(zb);
			ImageProcessor proc2v1 = img112.getStack().getProcessor(zbo);
			ImageProcessor proc1v2 = img212.getStack().getProcessor(zb);
			ImageProcessor proc2v2 = img121.getStack().getProcessor(zbo);
			ImageProcessor proc1v3 = img211.getStack().getProcessor(zb);
			ImageProcessor proc2v3 = img122.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc1v1.set(xao, yb, proc2v1.get(x, ybo));
					proc1v2.set(xb, yao, proc2v2.get(xbo, y));
					proc1v3.set(xao, yao, proc2v3.get(x, y));
				}
			}
		}
		
	}
	
	/*** docleanexchange ***
     * does an exchange of halos
     * 
     * @see  #docleanexchangetype000
     * @see  #docleanexchangetype001
     * @see  #docleanexchangetype010
     * @see  #docleanexchangetype011
     * @see  #docleanexchangetype100
     * @see  #docleanexchangetype101
     * @see  #docleanexchangetype110
     * @see  #docleanexchangetype111
     ***/
	private void docleanexchange(){
		IJ.log("clean"+String.valueOf(configuration[0])+String.valueOf(configuration[1])+String.valueOf(configuration[2]));
		if (!requireClean[0] && !requireClean[1] && !requireClean[2]){
			return;
		}
		if (configuration[0] == 0){
			if (configuration[1] == 0){
				if (configuration[2] == 0){
					docleanexchangetype000();
				}else{
					docleanexchangetype001();
				}
			}else{
				if (configuration[2] == 0){
					docleanexchangetype010();
				}else{
					docleanexchangetype011();
				}
			}
		}else{
			if (configuration[1] == 0){
				if (configuration[2] == 0){
					docleanexchangetype100();
				}else{
					docleanexchangetype101();
				}
			}else{
				if (configuration[2] == 0){
					docleanexchangetype110();
				}else{
					docleanexchangetype111();
				}
			}
		}
		return;
	}
	
	private void docleanexchangetype000(){
		/*
		 * current arrangement:
		 * i111 i112
		 * i121 i122
		 * 
		 * i211 i212
		 * i221 i222
		 */
		
		if (requireClean[1]){
			//exchanging img121 and img122 (x-direction change)
			tempX1 = img121.getWidth()-2*haloX;
			tempX2 = img121.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img121.getHeight();
			tempZ1 = img121.getStackSize()-haloZ-stepZ;
			tempZ2 = img121.getStackSize()-haloZ;
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img121.getStack().getProcessor(z);
				ImageProcessor proc22 = img122.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0]){
			//exchanging img211 and img212 (x-direction change)
			tempX1 = img211.getWidth()-2*haloX;
			tempX2 = img211.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img211.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img211.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img211.getStack().getProcessor(z);
				ImageProcessor proc22 = img212.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0] && requireClean[2]){
			//exchanging img221 and img222 (x-direction change)
			tempX1 = img221.getWidth()-2*haloX;
			tempX2 = img221.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img221.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img221.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img221.getStack().getProcessor(z);
				ImageProcessor proc22 = img222.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}

		
		if (requireClean[2]){
			//exchanging img112 and img122 (y-direction change)
			
			tempX1 = haloX;
			tempX2 = img112.getWidth();
			tempY1 = img112.getHeight()-2*haloY;
			tempY2 = img112.getHeight()-haloY;
			tempZ1 = img112.getStackSize()-haloZ-stepZ;
			tempZ2 = img112.getStackSize()-haloZ;
			IJ.log("X: " + String.valueOf(tempX1) + "<" + String.valueOf(tempX2));
			IJ.log("Y: " + String.valueOf(tempY1) + "<" + String.valueOf(tempY2));
			IJ.log("Yo: " + String.valueOf(0) + "<" + String.valueOf(tempY2-tempY1));
			IJ.log("Z: " + String.valueOf(tempZ1) + "<" + String.valueOf(tempZ2));
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img112.getStack().getProcessor(z);
				ImageProcessor proc2 = img122.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0]){
			//exchanging img211 and img221 (y-direction change)
			tempX1 = haloX;
			tempX2 = img211.getWidth();
			tempY1 = img211.getHeight()-2*haloY;
			tempY2 = img211.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img211.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img211.getStack().getProcessor(z);
				ImageProcessor proc2 = img221.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0] && requireClean[1]){
			//exchanging img212 and img222 (y-direction change)
			tempX1 = haloX;
			tempX2 = img212.getWidth();
			tempY1 = img212.getHeight()-2*haloY;
			tempY2 = img212.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img212.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img212.getStack().getProcessor(z);
				ImageProcessor proc2 = img222.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		
		/*
		 * current arrangement:
		 * i111 i112
		 * i121 i122
		 * 
		 * i211 i212
		 * i221 i222
		 */
		
		if (requireClean[2]){
			//exchanging img112 and img212 (z-direction change)
			tempX1 = haloX;
			tempX2 = img112.getWidth();
			tempY1 = img112.getHeight()-haloY-stepY;
			tempY2 = img112.getHeight()-haloY;
			tempZ1 = img112.getStackSize()-2*haloZ;
			tempZ2 = img112.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img112.getStack().getProcessor(z);
				ImageProcessor proc2 = img212.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img112.getStack().getProcessor(zb);
				ImageProcessor proc2 = img212.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1]){
			//exchanging img121 and img221 (z-direction change)
			tempX1 = img121.getWidth()-haloX-stepX;
			tempX2 = img121.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img121.getHeight();
			tempZ1 = img121.getStackSize()-2*haloZ;
			tempZ2 = img121.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img121.getStack().getProcessor(z);
				ImageProcessor proc2 = img221.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img121.getStack().getProcessor(zb);
				ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[2]){
			//exchanging img122 and img222 (z-direction change)
			tempX1 = haloX;
			tempX2 = img122.getWidth();
			tempY1 = haloY;
			tempY2 = img122.getHeight();
			tempZ1 = img122.getStackSize()-2*haloZ;
			tempZ2 = img122.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img122.getStack().getProcessor(z);
				ImageProcessor proc2 = img222.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img122.getStack().getProcessor(zb);
				ImageProcessor proc2 = img222.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		
		//exchanging img121 and img221 (z-direction change)
		/*
		tempX1 = img121.getWidth()-haloX-stepX;
		tempX2 = img121.getWidth()-haloX;
		tempY1 = img121.getHeight()-haloY-stepY;
		tempY2 = img121.getHeight()-haloY;
		tempZ1 = img121.getStackSize()-2*haloZ;
		tempZ2 = img121.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(z);
			ImageProcessor proc2 = img221.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img121.getStack().getProcessor(zb);
			ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		*/
		
		//exchanging img122 and img222 (z-direction change)
		/*
		tempX1 = img122.getWidth()-haloX-stepX;
		tempX2 = img122.getWidth()-haloX;
		tempY1 = img122.getHeight()-haloY-stepY;
		tempY2 = img122.getHeight()-haloY;
		tempZ1 = img122.getStackSize()-2*haloZ;
		tempZ2 = img122.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(z);
			ImageProcessor proc2 = img222.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(x, y, proc1.get(x, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img122.getStack().getProcessor(zb);
			ImageProcessor proc2 = img222.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(x, y, proc2.get(x, y));
				}
			}
		}
		*/
		
		/**edges**/
		/*
		//exchanging img111 and img122 and virtual exchange of img112 and img121 (xy-edge change)
		tempX1 = img111.getWidth()-2*haloX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-2*haloY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-haloZ-stepZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1; z<=tempZ2; z++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2 = img122.getStack().getProcessor(z);
			ImageProcessor proc1v = img112.getStack().getProcessor(z);
			ImageProcessor proc2v = img121.getStack().getProcessor(z);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(xao, yao, proc1.get(x, y));
					proc1.set(xb, yb, proc2.get(xbo, ybo));
					proc2v.set(xb, yao, proc1v.get(xbo, y));
					proc1v.set(xao, yb, proc2v.get(x, ybo));
				}
			}
		}
		
		//exchanging img111 and img212 and virtual exchange of img112 and img211 (xz-edge change)
		tempX1 = img111.getWidth()-2*haloX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-haloY-stepY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-2*haloZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2 = img212.getStack().getProcessor(zao);
			ImageProcessor proc1v = img112.getStack().getProcessor(z);
			ImageProcessor proc2v = img211.getStack().getProcessor(zao);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc2.set(xao, y, proc1.get(x, y));
					proc2v.set(xb, y, proc1v.get(xbo, y));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(zb);
			ImageProcessor proc2 = img212.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img112.getStack().getProcessor(zb);
			ImageProcessor proc2v = img211.getStack().getProcessor(zbo);
			for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
				for (int y=tempY1; y<tempY2; y++) {
					proc1.set(xb, y, proc2.get(xbo, y));
					proc1v.set(xao, y, proc2v.get(x, y));
				}
			}
		}
		//exchanging img111 and img221 and virtual exchange of img121 and img211 (yz-edge change)
		tempX1 = img111.getWidth()-haloX-stepX;
		tempX2 = img111.getWidth()-haloX;
		tempY1 = img111.getHeight()-2*haloY;
		tempY2 = img111.getHeight()-haloY;
		tempZ1 = img111.getStackSize()-2*haloZ;
		tempZ2 = img111.getStackSize()-haloZ;
		for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(z);
			ImageProcessor proc2 = img221.getStack().getProcessor(zao);
			ImageProcessor proc1v = img121.getStack().getProcessor(z);
			ImageProcessor proc2v = img211.getStack().getProcessor(zao);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc2.set(x, yao, proc1.get(x, y));
					proc2v.set(x, yb, proc1v.get(x, ybo));
				}
			}
		}
		for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
			ImageProcessor proc1 = img111.getStack().getProcessor(zb);
			ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
			ImageProcessor proc1v = img121.getStack().getProcessor(zb);
			ImageProcessor proc2v = img211.getStack().getProcessor(zbo);
			for (int x=tempX1; x<tempX2; x++) {
				for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
					proc1.set(x, yb, proc2.get(x, ybo));
					proc1v.set(x, yao, proc2v.get(x, y));
				}
			}
		}
		*/
		
	}
	
	private void docleanexchangetype001(){
		/*
		 * current arrangement:
		 * img112 img111
		 * img122 img121
		 * 
		 * img212 img211
		 * img222 img221
		 */
		
		if (requireClean[1]){
			//exchanging img122 and img121 (x-direction change)
			tempX1 = img122.getWidth()-2*haloX;
			tempX2 = img122.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img122.getHeight();
			tempZ1 = img122.getStackSize()-haloZ-stepZ;
			tempZ2 = img122.getStackSize()-haloZ;
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img122.getStack().getProcessor(z);
				ImageProcessor proc22 = img121.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0]){
			//exchanging img212 and img211 (x-direction change)
			tempX1 = img212.getWidth()-2*haloX;
			tempX2 = img212.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img212.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img212.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img212.getStack().getProcessor(z);
				ImageProcessor proc22 = img211.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0] && requireClean[2]){
			//exchanging img222 and img221 (x-direction change)
			tempX1 = img222.getWidth()-2*haloX;
			tempX2 = img222.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img222.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img222.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img222.getStack().getProcessor(z);
				ImageProcessor proc22 = img221.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}

		
		if (requireClean[2]){
			//exchanging img111 and img121 (y-direction change)
			
			tempX1 = haloX;
			tempX2 = img111.getWidth();
			tempY1 = img111.getHeight()-2*haloY;
			tempY2 = img111.getHeight()-haloY;
			tempZ1 = img111.getStackSize()-haloZ-stepZ;
			tempZ2 = img111.getStackSize()-haloZ;
			IJ.log("X: " + String.valueOf(tempX1) + "<" + String.valueOf(tempX2));
			IJ.log("Y: " + String.valueOf(tempY1) + "<" + String.valueOf(tempY2));
			IJ.log("Yo: " + String.valueOf(0) + "<" + String.valueOf(tempY2-tempY1));
			IJ.log("Z: " + String.valueOf(tempZ1) + "<" + String.valueOf(tempZ2));
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img111.getStack().getProcessor(z);
				ImageProcessor proc2 = img121.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0]){
			//exchanging img212 and img222 (y-direction change)
			tempX1 = haloX;
			tempX2 = img212.getWidth();
			tempY1 = img212.getHeight()-2*haloY;
			tempY2 = img212.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img212.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img212.getStack().getProcessor(z);
				ImageProcessor proc2 = img222.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0] && requireClean[1]){
			//exchanging img211 and img221 (y-direction change)
			tempX1 = haloX;
			tempX2 = img211.getWidth();
			tempY1 = img211.getHeight()-2*haloY;
			tempY2 = img211.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img211.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img211.getStack().getProcessor(z);
				ImageProcessor proc2 = img221.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		
		/*
		 * current arrangement:
		 * i111 i112
		 * i121 i122
		 * 
		 * i211 i212
		 * i221 i222
		 */
		
		if (requireClean[2]){
			//exchanging img111 and img211 (z-direction change)
			tempX1 = haloX;
			tempX2 = img111.getWidth();
			tempY1 = img111.getHeight()-haloY-stepY;
			tempY2 = img111.getHeight()-haloY;
			tempZ1 = img111.getStackSize()-2*haloZ;
			tempZ2 = img111.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img111.getStack().getProcessor(z);
				ImageProcessor proc2 = img211.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img111.getStack().getProcessor(zb);
				ImageProcessor proc2 = img211.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1]){
			//exchanging img122 and img222 (z-direction change)
			tempX1 = img122.getWidth()-haloX-stepX;
			tempX2 = img122.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img122.getHeight();
			tempZ1 = img122.getStackSize()-2*haloZ;
			tempZ2 = img122.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img122.getStack().getProcessor(z);
				ImageProcessor proc2 = img222.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img122.getStack().getProcessor(zb);
				ImageProcessor proc2 = img222.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[2]){
			//exchanging img121 and img221 (z-direction change)
			tempX1 = haloX;
			tempX2 = img121.getWidth();
			tempY1 = haloY;
			tempY2 = img121.getHeight();
			tempZ1 = img121.getStackSize()-2*haloZ;
			tempZ2 = img121.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img121.getStack().getProcessor(z);
				ImageProcessor proc2 = img221.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img121.getStack().getProcessor(zb);
				ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
	}
	
	private void docleanexchangetype010(){
		/*
		 * current arrangement:
		 * img121 img122
		 * img111 img112
		 * 
		 * img221 img222
		 * img211 img212
		 */
		
		if (requireClean[1]){
			//exchanging img111 and img112 (x-direction change)
			tempX1 = img111.getWidth()-2*haloX;
			tempX2 = img111.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img111.getHeight();
			tempZ1 = img111.getStackSize()-haloZ-stepZ;
			tempZ2 = img111.getStackSize()-haloZ;
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img111.getStack().getProcessor(z);
				ImageProcessor proc22 = img112.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0]){
			//exchanging img221 and img222 (x-direction change)
			tempX1 = img221.getWidth()-2*haloX;
			tempX2 = img221.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img221.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img221.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img221.getStack().getProcessor(z);
				ImageProcessor proc22 = img222.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0] && requireClean[2]){
			//exchanging img211 and img212 (x-direction change)
			tempX1 = img211.getWidth()-2*haloX;
			tempX2 = img211.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img211.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img211.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img211.getStack().getProcessor(z);
				ImageProcessor proc22 = img212.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}

		
		if (requireClean[2]){
			//exchanging img122 and img112 (y-direction change)
			
			tempX1 = haloX;
			tempX2 = img122.getWidth();
			tempY1 = img122.getHeight()-2*haloY;
			tempY2 = img122.getHeight()-haloY;
			tempZ1 = img122.getStackSize()-haloZ-stepZ;
			tempZ2 = img122.getStackSize()-haloZ;
			IJ.log("X: " + String.valueOf(tempX1) + "<" + String.valueOf(tempX2));
			IJ.log("Y: " + String.valueOf(tempY1) + "<" + String.valueOf(tempY2));
			IJ.log("Yo: " + String.valueOf(0) + "<" + String.valueOf(tempY2-tempY1));
			IJ.log("Z: " + String.valueOf(tempZ1) + "<" + String.valueOf(tempZ2));
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img122.getStack().getProcessor(z);
				ImageProcessor proc2 = img112.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0]){
			//exchanging img221 and img211 (y-direction change)
			tempX1 = haloX;
			tempX2 = img221.getWidth();
			tempY1 = img221.getHeight()-2*haloY;
			tempY2 = img221.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img221.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img221.getStack().getProcessor(z);
				ImageProcessor proc2 = img211.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0] && requireClean[1]){
			//exchanging img222 and img212 (y-direction change)
			tempX1 = haloX;
			tempX2 = img222.getWidth();
			tempY1 = img222.getHeight()-2*haloY;
			tempY2 = img222.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img222.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img222.getStack().getProcessor(z);
				ImageProcessor proc2 = img212.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		
		/*
		 * current arrangement:
		 * i111 i112
		 * i121 i122
		 * 
		 * i211 i212
		 * i221 i222
		 */
		
		if (requireClean[2]){
			//exchanging img122 and img222 (z-direction change)
			tempX1 = haloX;
			tempX2 = img122.getWidth();
			tempY1 = img122.getHeight()-haloY-stepY;
			tempY2 = img122.getHeight()-haloY;
			tempZ1 = img122.getStackSize()-2*haloZ;
			tempZ2 = img122.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img122.getStack().getProcessor(z);
				ImageProcessor proc2 = img222.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img122.getStack().getProcessor(zb);
				ImageProcessor proc2 = img222.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1]){
			//exchanging img111 and img211 (z-direction change)
			tempX1 = img111.getWidth()-haloX-stepX;
			tempX2 = img111.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img111.getHeight();
			tempZ1 = img111.getStackSize()-2*haloZ;
			tempZ2 = img111.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img111.getStack().getProcessor(z);
				ImageProcessor proc2 = img211.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img111.getStack().getProcessor(zb);
				ImageProcessor proc2 = img211.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[2]){
			//exchanging img112 and img212 (z-direction change)
			tempX1 = haloX;
			tempX2 = img112.getWidth();
			tempY1 = haloY;
			tempY2 = img112.getHeight();
			tempZ1 = img112.getStackSize()-2*haloZ;
			tempZ2 = img112.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img112.getStack().getProcessor(z);
				ImageProcessor proc2 = img212.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img112.getStack().getProcessor(zb);
				ImageProcessor proc2 = img212.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
	}
	
	private void docleanexchangetype011(){
		/*
		 * current arrangement:
		 * img122 img121
		 * img112 img111
		 * 
		 * img222 img221
		 * img212 img211
		 */
		
		if (requireClean[1]){
			//exchanging img112 and img111 (x-direction change)
			tempX1 = img112.getWidth()-2*haloX;
			tempX2 = img112.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img112.getHeight();
			tempZ1 = img112.getStackSize()-haloZ-stepZ;
			tempZ2 = img112.getStackSize()-haloZ;
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img112.getStack().getProcessor(z);
				ImageProcessor proc22 = img111.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0]){
			//exchanging img222 and img221 (x-direction change)
			tempX1 = img222.getWidth()-2*haloX;
			tempX2 = img222.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img222.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img222.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img222.getStack().getProcessor(z);
				ImageProcessor proc22 = img221.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0] && requireClean[2]){
			//exchanging img212 and img211 (x-direction change)
			tempX1 = img212.getWidth()-2*haloX;
			tempX2 = img212.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img212.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img212.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img212.getStack().getProcessor(z);
				ImageProcessor proc22 = img211.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}

		
		if (requireClean[2]){
			//exchanging img121 and img111 (y-direction change)
			
			tempX1 = haloX;
			tempX2 = img121.getWidth();
			tempY1 = img121.getHeight()-2*haloY;
			tempY2 = img121.getHeight()-haloY;
			tempZ1 = img121.getStackSize()-haloZ-stepZ;
			tempZ2 = img121.getStackSize()-haloZ;
			IJ.log("X: " + String.valueOf(tempX1) + "<" + String.valueOf(tempX2));
			IJ.log("Y: " + String.valueOf(tempY1) + "<" + String.valueOf(tempY2));
			IJ.log("Yo: " + String.valueOf(0) + "<" + String.valueOf(tempY2-tempY1));
			IJ.log("Z: " + String.valueOf(tempZ1) + "<" + String.valueOf(tempZ2));
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img121.getStack().getProcessor(z);
				ImageProcessor proc2 = img111.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0]){
			//exchanging img222 and img212 (y-direction change)
			tempX1 = haloX;
			tempX2 = img222.getWidth();
			tempY1 = img222.getHeight()-2*haloY;
			tempY2 = img222.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img222.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img222.getStack().getProcessor(z);
				ImageProcessor proc2 = img212.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0] && requireClean[1]){
			//exchanging img221 and img211 (y-direction change)
			tempX1 = haloX;
			tempX2 = img221.getWidth();
			tempY1 = img221.getHeight()-2*haloY;
			tempY2 = img221.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img221.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img221.getStack().getProcessor(z);
				ImageProcessor proc2 = img211.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		
		/*
		 * current arrangement:
		 * i111 i112
		 * i121 i122
		 * 
		 * i211 i212
		 * i221 i222
		 */
		
		if (requireClean[2]){
			//exchanging img121 and img221 (z-direction change)
			tempX1 = haloX;
			tempX2 = img121.getWidth();
			tempY1 = img121.getHeight()-haloY-stepY;
			tempY2 = img121.getHeight()-haloY;
			tempZ1 = img121.getStackSize()-2*haloZ;
			tempZ2 = img121.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img121.getStack().getProcessor(z);
				ImageProcessor proc2 = img221.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img121.getStack().getProcessor(zb);
				ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1]){
			//exchanging img112 and img212 (z-direction change)
			tempX1 = img112.getWidth()-haloX-stepX;
			tempX2 = img112.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img112.getHeight();
			tempZ1 = img112.getStackSize()-2*haloZ;
			tempZ2 = img112.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img112.getStack().getProcessor(z);
				ImageProcessor proc2 = img212.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img112.getStack().getProcessor(zb);
				ImageProcessor proc2 = img212.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[2]){
			//exchanging img111 and img211 (z-direction change)
			tempX1 = haloX;
			tempX2 = img111.getWidth();
			tempY1 = haloY;
			tempY2 = img111.getHeight();
			tempZ1 = img111.getStackSize()-2*haloZ;
			tempZ2 = img111.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img111.getStack().getProcessor(z);
				ImageProcessor proc2 = img211.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img111.getStack().getProcessor(zb);
				ImageProcessor proc2 = img211.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
	}
	
	private void docleanexchangetype100(){
		/*
		 * current arrangement:
		 * img211 img212
		 * img221 img222
		 * 
		 * img111 img112
		 * img121 img122
		 */
		
		if (requireClean[1]){
			//exchanging img221 and img222 (x-direction change)
			tempX1 = img221.getWidth()-2*haloX;
			tempX2 = img221.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img221.getHeight();
			tempZ1 = img221.getStackSize()-haloZ-stepZ;
			tempZ2 = img221.getStackSize()-haloZ;
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img221.getStack().getProcessor(z);
				ImageProcessor proc22 = img222.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0]){
			//exchanging img111 and img112 (x-direction change)
			tempX1 = img111.getWidth()-2*haloX;
			tempX2 = img111.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img111.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img111.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img111.getStack().getProcessor(z);
				ImageProcessor proc22 = img112.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0] && requireClean[2]){
			//exchanging img121 and img122 (x-direction change)
			tempX1 = img121.getWidth()-2*haloX;
			tempX2 = img121.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img121.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img121.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img121.getStack().getProcessor(z);
				ImageProcessor proc22 = img122.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}

		
		if (requireClean[2]){
			//exchanging img212 and img222 (y-direction change)
			
			tempX1 = haloX;
			tempX2 = img212.getWidth();
			tempY1 = img212.getHeight()-2*haloY;
			tempY2 = img212.getHeight()-haloY;
			tempZ1 = img212.getStackSize()-haloZ-stepZ;
			tempZ2 = img212.getStackSize()-haloZ;
			IJ.log("X: " + String.valueOf(tempX1) + "<" + String.valueOf(tempX2));
			IJ.log("Y: " + String.valueOf(tempY1) + "<" + String.valueOf(tempY2));
			IJ.log("Yo: " + String.valueOf(0) + "<" + String.valueOf(tempY2-tempY1));
			IJ.log("Z: " + String.valueOf(tempZ1) + "<" + String.valueOf(tempZ2));
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img212.getStack().getProcessor(z);
				ImageProcessor proc2 = img222.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0]){
			//exchanging img111 and img121 (y-direction change)
			tempX1 = haloX;
			tempX2 = img111.getWidth();
			tempY1 = img111.getHeight()-2*haloY;
			tempY2 = img111.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img111.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img111.getStack().getProcessor(z);
				ImageProcessor proc2 = img121.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0] && requireClean[1]){
			//exchanging img112 and img122 (y-direction change)
			tempX1 = haloX;
			tempX2 = img112.getWidth();
			tempY1 = img112.getHeight()-2*haloY;
			tempY2 = img112.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img112.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img112.getStack().getProcessor(z);
				ImageProcessor proc2 = img122.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		
		/*
		 * current arrangement:
		 * i111 i112
		 * i121 i122
		 * 
		 * i211 i212
		 * i221 i222
		 */
		
		if (requireClean[2]){
			//exchanging img212 and img112 (z-direction change)
			tempX1 = haloX;
			tempX2 = img212.getWidth();
			tempY1 = img212.getHeight()-haloY-stepY;
			tempY2 = img212.getHeight()-haloY;
			tempZ1 = img212.getStackSize()-2*haloZ;
			tempZ2 = img212.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img212.getStack().getProcessor(z);
				ImageProcessor proc2 = img112.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img212.getStack().getProcessor(zb);
				ImageProcessor proc2 = img112.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1]){
			//exchanging img221 and img121 (z-direction change)
			tempX1 = img221.getWidth()-haloX-stepX;
			tempX2 = img221.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img221.getHeight();
			tempZ1 = img221.getStackSize()-2*haloZ;
			tempZ2 = img221.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img221.getStack().getProcessor(z);
				ImageProcessor proc2 = img121.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img221.getStack().getProcessor(zb);
				ImageProcessor proc2 = img121.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		
		if (requireClean[1] && requireClean[2]){
			//exchanging img222 and img122 (z-direction change)
			tempX1 = haloX;
			tempX2 = img222.getWidth();
			tempY1 = haloY;
			tempY2 = img222.getHeight();
			tempZ1 = img222.getStackSize()-2*haloZ;
			tempZ2 = img222.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img222.getStack().getProcessor(z);
				ImageProcessor proc2 = img122.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img222.getStack().getProcessor(zb);
				ImageProcessor proc2 = img122.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
	}
	
	private void docleanexchangetype101(){
		/*
		 * current arrangement:
		 * img212 img211
		 * img222 img221
		 * 
		 * img112 img111
		 * img122 img121
		 */
		
		if (requireClean[1]){
			//exchanging img222 and img221 (x-direction change)
			tempX1 = img222.getWidth()-2*haloX;
			tempX2 = img222.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img222.getHeight();
			tempZ1 = img222.getStackSize()-haloZ-stepZ;
			tempZ2 = img222.getStackSize()-haloZ;
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img222.getStack().getProcessor(z);
				ImageProcessor proc22 = img221.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0]){
			//exchanging img112 and img111 (x-direction change)
			tempX1 = img112.getWidth()-2*haloX;
			tempX2 = img112.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img112.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img112.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img112.getStack().getProcessor(z);
				ImageProcessor proc22 = img111.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0] && requireClean[2]){
			//exchanging img122 and img121 (x-direction change)
			tempX1 = img122.getWidth()-2*haloX;
			tempX2 = img122.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img122.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img122.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img122.getStack().getProcessor(z);
				ImageProcessor proc22 = img121.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}

		
		if (requireClean[2]){
			//exchanging img211 and img221 (y-direction change)
			
			tempX1 = haloX;
			tempX2 = img211.getWidth();
			tempY1 = img211.getHeight()-2*haloY;
			tempY2 = img211.getHeight()-haloY;
			tempZ1 = img211.getStackSize()-haloZ-stepZ;
			tempZ2 = img211.getStackSize()-haloZ;
			IJ.log("X: " + String.valueOf(tempX1) + "<" + String.valueOf(tempX2));
			IJ.log("Y: " + String.valueOf(tempY1) + "<" + String.valueOf(tempY2));
			IJ.log("Yo: " + String.valueOf(0) + "<" + String.valueOf(tempY2-tempY1));
			IJ.log("Z: " + String.valueOf(tempZ1) + "<" + String.valueOf(tempZ2));
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img211.getStack().getProcessor(z);
				ImageProcessor proc2 = img221.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0]){
			//exchanging img112 and img122 (y-direction change)
			tempX1 = haloX;
			tempX2 = img112.getWidth();
			tempY1 = img112.getHeight()-2*haloY;
			tempY2 = img112.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img112.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img112.getStack().getProcessor(z);
				ImageProcessor proc2 = img122.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0] && requireClean[1]){
			//exchanging img111 and img121 (y-direction change)
			tempX1 = haloX;
			tempX2 = img111.getWidth();
			tempY1 = img111.getHeight()-2*haloY;
			tempY2 = img111.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img111.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img111.getStack().getProcessor(z);
				ImageProcessor proc2 = img121.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		
		if (requireClean[2]){
			//exchanging img211 and img111 (z-direction change)
			tempX1 = haloX;
			tempX2 = img211.getWidth();
			tempY1 = img211.getHeight()-haloY-stepY;
			tempY2 = img211.getHeight()-haloY;
			tempZ1 = img211.getStackSize()-2*haloZ;
			tempZ2 = img211.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img211.getStack().getProcessor(z);
				ImageProcessor proc2 = img111.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img211.getStack().getProcessor(zb);
				ImageProcessor proc2 = img111.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1]){
			//exchanging img222 and img122 (z-direction change)
			tempX1 = img222.getWidth()-haloX-stepX;
			tempX2 = img222.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img222.getHeight();
			tempZ1 = img222.getStackSize()-2*haloZ;
			tempZ2 = img222.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img222.getStack().getProcessor(z);
				ImageProcessor proc2 = img122.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img222.getStack().getProcessor(zb);
				ImageProcessor proc2 = img122.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[2]){
			//exchanging img221 and img121 (z-direction change)
			tempX1 = haloX;
			tempX2 = img221.getWidth();
			tempY1 = haloY;
			tempY2 = img221.getHeight();
			tempZ1 = img221.getStackSize()-2*haloZ;
			tempZ2 = img221.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img221.getStack().getProcessor(z);
				ImageProcessor proc2 = img121.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img221.getStack().getProcessor(zb);
				ImageProcessor proc2 = img121.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
	}
	
	private void docleanexchangetype110(){
		/*
		 * current arrangement:
		 * img221 img222
		 * img211 img212
		 * 
		 * img121 img122
		 * img111 img112
		 */
		
		if (requireClean[1]){
			//exchanging img211 and img212 (x-direction change)
			tempX1 = img211.getWidth()-2*haloX;
			tempX2 = img211.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img211.getHeight();
			tempZ1 = img211.getStackSize()-haloZ-stepZ;
			tempZ2 = img211.getStackSize()-haloZ;
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img211.getStack().getProcessor(z);
				ImageProcessor proc22 = img212.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0]){
			//exchanging img121 and img122 (x-direction change)
			tempX1 = img121.getWidth()-2*haloX;
			tempX2 = img121.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img121.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img121.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img121.getStack().getProcessor(z);
				ImageProcessor proc22 = img122.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0] && requireClean[2]){
			//exchanging img111 and img112 (x-direction change)
			tempX1 = img111.getWidth()-2*haloX;
			tempX2 = img111.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img111.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img111.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img111.getStack().getProcessor(z);
				ImageProcessor proc22 = img112.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}

		
		if (requireClean[2]){
			//exchanging img222 and img212 (y-direction change)
			
			tempX1 = haloX;
			tempX2 = img222.getWidth();
			tempY1 = img222.getHeight()-2*haloY;
			tempY2 = img222.getHeight()-haloY;
			tempZ1 = img222.getStackSize()-haloZ-stepZ;
			tempZ2 = img222.getStackSize()-haloZ;
			IJ.log("X: " + String.valueOf(tempX1) + "<" + String.valueOf(tempX2));
			IJ.log("Y: " + String.valueOf(tempY1) + "<" + String.valueOf(tempY2));
			IJ.log("Yo: " + String.valueOf(0) + "<" + String.valueOf(tempY2-tempY1));
			IJ.log("Z: " + String.valueOf(tempZ1) + "<" + String.valueOf(tempZ2));
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img222.getStack().getProcessor(z);
				ImageProcessor proc2 = img212.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0]){
			//exchanging img121 and img111 (y-direction change)
			tempX1 = haloX;
			tempX2 = img121.getWidth();
			tempY1 = img121.getHeight()-2*haloY;
			tempY2 = img121.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img121.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img121.getStack().getProcessor(z);
				ImageProcessor proc2 = img111.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0] && requireClean[1]){
			//exchanging img122 and img112 (y-direction change)
			tempX1 = haloX;
			tempX2 = img122.getWidth();
			tempY1 = img122.getHeight()-2*haloY;
			tempY2 = img122.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img122.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img122.getStack().getProcessor(z);
				ImageProcessor proc2 = img112.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		
		if (requireClean[2]){
			//exchanging img222 and img122 (z-direction change)
			tempX1 = haloX;
			tempX2 = img222.getWidth();
			tempY1 = img222.getHeight()-haloY-stepY;
			tempY2 = img222.getHeight()-haloY;
			tempZ1 = img222.getStackSize()-2*haloZ;
			tempZ2 = img222.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img222.getStack().getProcessor(z);
				ImageProcessor proc2 = img122.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img222.getStack().getProcessor(zb);
				ImageProcessor proc2 = img122.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1]){
			//exchanging img211 and img111 (z-direction change)
			tempX1 = img211.getWidth()-haloX-stepX;
			tempX2 = img211.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img211.getHeight();
			tempZ1 = img211.getStackSize()-2*haloZ;
			tempZ2 = img211.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img211.getStack().getProcessor(z);
				ImageProcessor proc2 = img111.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img211.getStack().getProcessor(zb);
				ImageProcessor proc2 = img111.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[2]){
			//exchanging img212 and img112 (z-direction change)
			tempX1 = haloX;
			tempX2 = img212.getWidth();
			tempY1 = haloY;
			tempY2 = img212.getHeight();
			tempZ1 = img212.getStackSize()-2*haloZ;
			tempZ2 = img212.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img212.getStack().getProcessor(z);
				ImageProcessor proc2 = img112.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img212.getStack().getProcessor(zb);
				ImageProcessor proc2 = img112.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
	}
	
	private void docleanexchangetype111(){
		/*
		 * current arrangement:
		 * img222 img221
		 * img212 img211
		 * 
		 * img122 img121
		 * img112 img111
		 */
		
		if (requireClean[1]){
			//exchanging img212 and img211 (x-direction change)
			tempX1 = img212.getWidth()-2*haloX;
			tempX2 = img212.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img212.getHeight();
			tempZ1 = img212.getStackSize()-haloZ-stepZ;
			tempZ2 = img212.getStackSize()-haloZ;
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img212.getStack().getProcessor(z);
				ImageProcessor proc22 = img211.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0]){
			//exchanging img122 and img121 (x-direction change)
			tempX1 = img122.getWidth()-2*haloX;
			tempX2 = img122.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img122.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img122.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img122.getStack().getProcessor(z);
				ImageProcessor proc22 = img121.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[0] && requireClean[2]){
			//exchanging img112 and img111 (x-direction change)
			tempX1 = img112.getWidth()-2*haloX;
			tempX2 = img112.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img112.getHeight();
			tempZ1 = haloZ;
			tempZ2 = img112.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc21 = img112.getStack().getProcessor(z);
				ImageProcessor proc22 = img111.getStack().getProcessor(z);
				for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc22.set(xao, y, proc21.get(x, y));
						proc21.set(xb, y, proc22.get(xbo, y));
					}
				}
			}
		}
		if (requireClean[2]){
			//exchanging img221 and img211 (y-direction change)
			
			tempX1 = haloX;
			tempX2 = img221.getWidth();
			tempY1 = img221.getHeight()-2*haloY;
			tempY2 = img221.getHeight()-haloY;
			tempZ1 = img221.getStackSize()-haloZ-stepZ;
			tempZ2 = img221.getStackSize()-haloZ;
			IJ.log("X: " + String.valueOf(tempX1) + "<" + String.valueOf(tempX2));
			IJ.log("Y: " + String.valueOf(tempY1) + "<" + String.valueOf(tempY2));
			IJ.log("Yo: " + String.valueOf(0) + "<" + String.valueOf(tempY2-tempY1));
			IJ.log("Z: " + String.valueOf(tempZ1) + "<" + String.valueOf(tempZ2));
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img221.getStack().getProcessor(z);
				ImageProcessor proc2 = img211.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0]){
			//exchanging img122 and img112 (y-direction change)
			tempX1 = haloX;
			tempX2 = img122.getWidth();
			tempY1 = img122.getHeight()-2*haloY;
			tempY2 = img122.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img122.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img122.getStack().getProcessor(z);
				ImageProcessor proc2 = img112.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2] && requireClean[0] && requireClean[1]){
			//exchanging img121 and img111 (y-direction change)
			tempX1 = haloX;
			tempX2 = img121.getWidth();
			tempY1 = img121.getHeight()-2*haloY;
			tempY2 = img121.getHeight()-haloY;
			tempZ1 = haloZ;
			tempZ2 = img121.getStackSize();
			for (int z=tempZ1+1; z<=tempZ2; z++) {
				ImageProcessor proc1 = img121.getStack().getProcessor(z);
				ImageProcessor proc2 = img111.getStack().getProcessor(z);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1, yao = 0, ybo = haloY, yb=tempY2; y<tempY2; y++, yb++, yao++, ybo++) {
						proc2.set(x, yao, proc1.get(x, y));
						proc1.set(x, yb, proc2.get(x, ybo));
					}
				}
			}
		}
		if (requireClean[2]){
			//exchanging img221 and img121 (z-direction change)
			tempX1 = haloX;
			tempX2 = img221.getWidth();
			tempY1 = img221.getHeight()-haloY-stepY;
			tempY2 = img221.getHeight()-haloY;
			tempZ1 = img221.getStackSize()-2*haloZ;
			tempZ2 = img221.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img221.getStack().getProcessor(z);
				ImageProcessor proc2 = img121.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img221.getStack().getProcessor(zb);
				ImageProcessor proc2 = img121.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1]){
			//exchanging img212 and img112 (z-direction change)
			tempX1 = img212.getWidth()-haloX-stepX;
			tempX2 = img212.getWidth()-haloX;
			tempY1 = haloY;
			tempY2 = img212.getHeight();
			tempZ1 = img212.getStackSize()-2*haloZ;
			tempZ2 = img212.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img212.getStack().getProcessor(z);
				ImageProcessor proc2 = img112.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img212.getStack().getProcessor(zb);
				ImageProcessor proc2 = img112.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
		
		if (requireClean[1] && requireClean[2]){
			//exchanging img211 and img111 (z-direction change)
			tempX1 = haloX;
			tempX2 = img211.getWidth();
			tempY1 = haloY;
			tempY2 = img211.getHeight();
			tempZ1 = img211.getStackSize()-2*haloZ;
			tempZ2 = img211.getStackSize()-haloZ;
			for (int z=tempZ1+1, zao = 1; z<=tempZ2; z++, zao++) {
				ImageProcessor proc1 = img211.getStack().getProcessor(z);
				ImageProcessor proc2 = img111.getStack().getProcessor(zao);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc2.set(x, y, proc1.get(x, y));
					}
				}
			}
			for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
				ImageProcessor proc1 = img211.getStack().getProcessor(zb);
				ImageProcessor proc2 = img111.getStack().getProcessor(zbo);
				for (int x=tempX1; x<tempX2; x++) {
					for (int y=tempY1; y<tempY2; y++) {
						proc1.set(x, y, proc2.get(x, y));
					}
				}
			}
		}
	}
}


