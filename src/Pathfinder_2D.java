import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

/*** Pathfinder_2D
 * attempts to find a path between two points using the pixel values like a forcefield.
 * 
 * @author Lasse Wollatz
 ***/
public class Pathfinder_2D implements PlugIn {
	
	public void run(String command){
		//generate error message for older versions:
		if (IJ.versionLessThan("1.48n"))
			return;
		IJ.showStatus("Applying Mask...");
		
		
		//get start point
		//create dialog to request values from user:
		GenericDialog gd = new GenericDialog(command+" Define Start and End");
		gd.addNumericField("x0", 80, 0);
		gd.addNumericField("y0", 80, 0);
		gd.addNumericField("x2", 200, 0);
		gd.addNumericField("y2", 200, 0);
		gd.addNumericField("Fm", 1, 2);
		gd.addNumericField("Fb", 1, 2);
		gd.addNumericField("rb", 10, 0);
		gd.showDialog();
		if (gd.wasCanceled()){
        	return;
        }
		double x0 = (double)(int)gd.getNextNumber();
		double y0 = (double)(int)gd.getNextNumber();
		double x2 = (double)(int)gd.getNextNumber();
		double y2 = (double)(int)gd.getNextNumber();
		double Fm = (double)gd.getNextNumber();
		double Fb = (double)gd.getNextNumber();
		int r = (int)gd.getNextNumber();
		//values from user dialog extracted
		//get image
		ImagePlus image = WindowManager.getCurrentImage();
		int tWidth = image.getWidth();
		int tHeight = image.getHeight();
		ImageProcessor imageIP = image.getStack().getProcessor(1).convertToByte(true);
		byte[] Pixels = (byte[])imageIP.getPixels();
		
		//initialize variables
		double dist0 = Math.sqrt((x2-x0)*(x2-x0)+(y2-y0)*(y2-y0));
		double x1 = x0;
		double y1 = y0;
		double dist = dist0;
		double vx = 0;
		double vy = 0;
		double Fx = 0;
		double Fy = 0;
		int p = 0;
		
		//int xp, yp;
		
		//int xp = (int)x1;
		//int yp = (int)y1;
		
		for (int t=1; t<=1000; t++){
			//reset values
			vx = 0;
			vy = 0;
			Fx = 0;
			Fy = 0;
			//xp = (int)x1;
			//yp = (int)y1;
			//calculate major flow force:
			dist = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
			vx += (x2-x1)*Fm/dist;
			vy += (y2-y1)*Fm/dist;
			//calculate effect from nearby points:
			for (int nx=(int)x1-r; nx<=x1+r; nx++){
				for (int ny=(int)y1-r; ny<=y1+r; ny++){
					p=nx+ny*tWidth;
					if (Pixels[p] == (byte)255) {
						dist = Math.sqrt((nx-x1)*(nx-x1)+(ny-y1)*(ny-y1));
						Fx -= Fb*(1/dist)*(nx-x1)/dist;
						Fy -= Fb*(1/dist)*(ny-y1)/dist;
					}
				}
			}
			//converting force to velocity
			vx += Fx;
			vy += Fy;
			//converting velocity to position
			x1 += vx;
			y1 += vy;
			//draw absolute pixel as 180:
			
			if((int)x1 >= 0 && (int)x1 < tWidth && (int)y1 >= 0 && (int)y1 < tHeight){
				p=(int)x1+(int)y1*tWidth;
				Pixels[p] = (byte)220;
			}
		}
		
		
		
		image.show();
		
		IJ.showStatus("Path Completed.");
	}
}


