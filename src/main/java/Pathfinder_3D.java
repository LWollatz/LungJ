import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
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

/*** Pathfinder_3D
 * attempts to find a path between two points using the pixel values like a forcefield.
 * 
 * @author Lasse Wollatz
 ***/
public class Pathfinder_3D implements PlugIn {
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	
	public void run(String command){
		//generate error message for older versions:
		if (IJ.versionLessThan("1.48n"))
			return;
		IJ.showStatus("Finding Path");
		
		
		//get start point
		//create dialog to request values from user:
		GenericDialog gd = new GenericDialog(command+" Define Start and End");
		gd.addNumericField("x0", 145, 0);
		gd.addNumericField("y0", 97, 0);
		gd.addNumericField("z0", 5, 0);
		gd.addNumericField("x2", 142, 0); //199
		gd.addNumericField("y2", 224, 0); //50
		gd.addNumericField("z2", 244, 0); //180
		gd.addNumericField("Fm", 5, 2);
		gd.addNumericField("Fb", 0.1, 2);
		gd.addNumericField("rb", 5, 0);
		gd.showDialog();
		if (gd.wasCanceled()){
        	return;
        }
		double x0 = (double)(int)gd.getNextNumber();
		double y0 = (double)(int)gd.getNextNumber();
		double z0 = (double)(int)gd.getNextNumber();
		double x2 = (double)(int)gd.getNextNumber();
		double y2 = (double)(int)gd.getNextNumber();
		double z2 = (double)(int)gd.getNextNumber();
		double Fm = (double)gd.getNextNumber();
		double Fb = (double)gd.getNextNumber();
		int r = (int)gd.getNextNumber();
		//values from user dialog extracted
		//get image
		ImagePlus image = WindowManager.getCurrentImage();
		int tWidth = image.getWidth();
		int tHeight = image.getHeight();
		int tDepth = image.getNSlices();
		//ImageProcessor imageIP = image.getStack().getProcessor(1).convertToByte(true);
		//byte[] Pixels = (byte[])imageIP.getPixels();
		
		//initialize variables
		double dist0 = Math.sqrt((x2-x0)*(x2-x0)+(y2-y0)*(y2-y0)+(z2-z0)*(z2-z0));
		double x1 = x0;
		double y1 = y0;
		double z1 = z0;
		double dist = dist0;
		double vx = 0;
		double vy = 0;
		double vz = 0;
		double Fx = 0;
		double Fy = 0;
		double Fz = 0;
		int p = 0;
		
		int xp = (int)x1;
		int yp = (int)y1;
		int zp = (int)z1;
		
		int t=1;
		while (t<100000){
		//for (int t=1; t<=2000; t++){
			//reset values
			vx = 0;
			vy = 0;
			vz = 0;
			Fx = 0;
			Fy = 0;
			Fz = 0;
			xp = (int)x1;
			yp = (int)y1;
			zp = (int)z1;
			if (xp == x2 && yp == y2 && zp == z2){
				IJ.showStatus("Path Completed.");
				break;
			}
			//calculate major flow force:
			dist = Math.sqrt((x2-x0)*(x2-x0)+(y2-y0)*(y2-y0)+(z2-z0)*(z2-z0));
			vx += (x2-x1)*Fm/dist;
			vy += (y2-y1)*Fm/dist;
			vz += (z2-z1)*Fm/dist;
			//calculate effect from nearby points:
			for (int nz=Math.max((int)z1-r,1); nz<=Math.min(z1+r, tDepth); nz++){
				ImageProcessor imageIP = image.getStack().getProcessor(nz).convertToByte(true);
				byte[] Pixels = (byte[])imageIP.getPixels();
				for (int nx=Math.max((int)x1-r,0); nx<=Math.min(x1+r,tWidth-1); nx++){
					for (int ny=Math.max((int)y1-r,0); ny<=Math.min(y1+r,tHeight-1); ny++){
						p=nx+ny*tWidth;
						if (Pixels[p] == (byte)255) {
							dist = Math.sqrt((nx-x1)*(nx-x1)+(ny-y1)*(ny-y1)+(nz-z1)*(nz-z1));
							Fx -= Fb*(1/dist)*(nx-x1)/dist;
							Fy -= Fb*(1/dist)*(ny-y1)/dist;
							Fz -= Fb*(1/dist)*(nz-z1)/dist;
						}
					}
				}
			}
			//converting force to velocity
			vx += Fx;
			vy += Fy;
			vz += Fz;
			//making sure that I am moving:
			double vmax = Math.max(Math.max(Math.abs(vx),Math.abs(vy)),Math.abs(vz));
			if (vmax < 0.1){
				//abort!
				IJ.showStatus("Dead End.");
				IJ.log("t="+String.valueOf(vmax)+"v="+String.valueOf(vmax));
				break;
			}else{
				vx /= vmax;
				vy /= vmax;
				vz /= vmax;
			}
			//converting velocity to position
			x1 += vx;
			y1 += vy;
			z1 += vz;
			//draw absolute pixel as 180:
			
			if((int)x1 >= 0 && (int)x1 < tWidth && (int)y1 >= 0 && (int)y1 < tHeight && (int)z1 > 0 && (int)z1 <= tDepth){
				p=(int)x1+(int)y1*tWidth;
				ImageProcessor imageIP = image.getStack().getProcessor((int)z1).convertToByte(true);
				byte[] Pixels = (byte[])imageIP.getPixels();
				Pixels[p] = (byte)190;
				
			}else{
				IJ.log("["+String.valueOf(x1)+";"+String.valueOf(y1)+";"+String.valueOf(z1)+"]");
			}
		}
		
		IJ.showStatus("Out of Time.");
		
		image.show();
		
		
	}
}


