import java.awt.Color;
import java.awt.Rectangle;
import java.util.Hashtable;

import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.WindowManager;
import ij.gui.Plot;
import ij.gui.PlotWindow;
import ij.gui.PointRoi;
import ij.gui.PolygonRoi;
import ij.gui.Roi;
import ij.measure.ResultsTable;
import ij.plugin.PlugIn;
import ij.plugin.filter.Analyzer;
import ij.plugin.frame.RoiManager;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/** 
 * Finds the average value or RGB of active ROIS. The expected colour range is overlaid 
 * on the histogram of the active image.
 * 
 * - Open an image. 
 * - Select Rectangular and Point ROIs which contain only the region you want the average
 *   colour of. (Other ROIs are not supported)
 * - Make sure the ROIs are added to the ROI Manager (press Ctrl + T after each 
 *   selection)
 * - Make sure that the selected regions are representative of the whole area.
 * - with the ROI Manager open, run LungJ>Average ROI Colour.
 * - The output will be a results table giving you information about the mean value, the 
 *   standard deviation and the likelihood that this is the average value of your region.
 *   As well a plot illustrates the result.
 *   
 * @author Lasse Wollatz
 *   
 **/


public class Average_ROI_Colour implements PlugIn{
	/** plugin's name */
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version */
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	
	public static double plotYmax = 0;
	
	
	public void run(String arg) {
		
		//TODO: integrate directly into ROI Manager
		/**use ROI Manager**/
		
		RoiManager manager = RoiManager.getInstance();
		
		if (manager != null) {
		    //Hashtable<String, Roi> table = (Hashtable<String, Roi>)manager.getROIs();
			int[] table = manager.getIndexes();
		    /** mean calculation run **/
		    double[] mean = {0,0,0,0};
		    IJ.log("manager not null");
		    if (table != null) {
		    	IJ.log("table not null");
		    for (int label : table) { //for (String label : table.keySet()) {
		    	IJ.log("in loop");
		        //int slice = manager.getSliceNumber(label);
		        Roi roi = manager.getRoi(label); //Roi roi = table.get(label);
		        ImagePlus img = roi.getImage();
		        if (img == null){
		        	img = WindowManager.getCurrentImage();
		        }
		        int sn = roi.getPosition();
		        if (sn != 0){
		        	img.setSlice(sn);
		        }
		        ImageProcessor ip = img.getProcessor();
		        
		        
		        if (roi != null && roi.getType() == Roi.POINT){
		            mean = updateMean(ip,(PointRoi)roi, mean);
		        }else if (roi != null && roi.getType() == Roi.POLYGON){
		        	IJ.log("Can't include polygon ROI in analysis!");
		        }else if (roi != null && roi.getType() == Roi.RECTANGLE){
		        	mean = updateMean(ip,roi, mean);
		        }else if (roi != null){
		            IJ.log("Can't include unknown ROI type "+roi.getType()+"in analysis!");
		        }
		        
		    }
		    //IJ.log("mean = " + String.valueOf(mean[1]) + ";" + String.valueOf(mean[2])+ ";" + String.valueOf(mean[3]));
		    /** std dev calculation run **/
		    double[] std = {0,0,0};
		    for (int label : table) { //for (String label : table.keySet()) {
		        //int slice = manager.getSliceNumber(label);
		        Roi roi = manager.getRoi(label);
		        ImagePlus img = roi.getImage();
		        if (img == null){
		        	img = WindowManager.getCurrentImage();
		        }
		        int sn = roi.getPosition();
		        if (sn != 0){
		        	img.setSlice(sn);
		        }
		        ImageProcessor ip = img.getProcessor();
		        
		        if (roi != null && roi.getType() == Roi.RECTANGLE){
		        	std = updateStd(ip, roi, std, mean);
		        }else if (roi != null && roi.getType() == Roi.POINT){
		            std = updateStd(ip,(PointRoi)roi, std, mean);
		        }
		        
		    }
		    
		    
		    /** calculating p from t **/
		    /**
		     * adapted from http://psydok.sulb.uni-saarland.de/volltexte/2004/268/html/surfstat/t.htm
		     *              (Students T-Verteilung)
		     *         by   Hans-Jürgen Andreß 10. September 2001
		     *         last accessed: 22. July 2015
		     * which is 
		     * adapted from http://surfstat.newcastle.edu.au/
		     *              (An Online Text in Introductory Statistics from the University of Newcastle, Australia)
		     *         by   Keith Dear 1999
		     * 
		     */
		    int n = (int)mean[0];
		    int df = n-1;
		    std[0] = Math.sqrt(std[0]); // /sqrt(n)
		    std[1] = Math.sqrt(std[1]); // /sqrt(n)
		    std[2] = Math.sqrt(std[2]); // /sqrt(n)
		    //IJ.log("stdR = " + String.valueOf(std[0] / Math.sqrt(n)));
		    //IJ.log("stdG = " + String.valueOf(std[1] / Math.sqrt(n)));
		    //IJ.log("stdB = " + String.valueOf(std[2] / Math.sqrt(n)));
		    
		    double t, tG, tB;
		    double p, pG, pB;
		    double t1, t1G, t1B;
		    double p1, p1G, p1B;
		    pG = 0;
		    pB = 0;
		    p1G = 0;
		    p1B = 0;
		    
		    
		    t = 0.5 * n / std[0];
		    //IJ.log("t = " + String.valueOf(t));
		    p = tToP(t,df);
		    //IJ.log("p = " + String.valueOf(p*100) + "%");
		    if(mean[2] != 0){
		    	tG = 0.5 * n / std[1];
		    	//IJ.log("tG = " + String.valueOf(tG));
		    	pG = tToP(tG,df);
		    	//IJ.log("pG = " + String.valueOf(pG*100) + "%");
		    	tB = 0.5 * n / std[2];
		    	//IJ.log("tB = " + String.valueOf(tB));
		    	pB = tToP(tB,df);
		    	//IJ.log("pB = " + String.valueOf(pB*100) + "%");
		    }
		    
		    t1 = mean[1]*0.01 * n / std[0];
		    //IJ.log("t1 = " + String.valueOf(t1));
		    p1 = tToP(t1,df);
		    //IJ.log("p1 = " + String.valueOf(p1*100) + "%");
		    if(mean[2] != 0){
		    	t1G = mean[2]*0.01 * n / std[1];
		    	//IJ.log("t1G = " + String.valueOf(t1G));
		    	p1G = tToP(t1G,df);
		    	//IJ.log("p1G = " + String.valueOf(p1G*100) + "%");
		    	t1B = mean[3]*0.01 * n / std[2];
		    	//IJ.log("t1B = " + String.valueOf(t1B));
		    	p1B = tToP(t1B,df);
		    	//IJ.log("p1B = " + String.valueOf(p1B*100) + "%");
		    }
		    
		    
		    std[0] = std[0] / Math.sqrt(n);
		    std[1] = std[1] / Math.sqrt(n);
		    std[2] = std[2] / Math.sqrt(n);
		    
		    /**output to results table**/
		    ResultsTable rt = Analyzer.getResultsTable();
			if (rt == null) {
			        rt = new ResultsTable();
			        Analyzer.setResultsTable(rt);
			}
			
			if(mean[2] != 0){
				rt.incrementCounter();
				rt.addLabel("points");
				rt.addValue("value R", n);
				rt.addValue("value G", n);
				rt.addValue("value B", n);
				rt.addValue("unit", "pixel");
				rt.incrementCounter();
				rt.addLabel("mean");
				rt.addValue("value R", mean[1]);
				rt.addValue("value G", mean[2]);
				rt.addValue("value B", mean[3]);
				rt.addValue("unit", "");
				rt.incrementCounter();
				rt.addLabel("std");
				rt.addValue("value R", std[0]);
				rt.addValue("value G", std[1]);
				rt.addValue("value B", std[2]);
				rt.addValue("unit", "");
				rt.incrementCounter();
				rt.addLabel("+-0.5 reliability");
				rt.addValue("value R", p*100);
				rt.addValue("value G", pG*100);
				rt.addValue("value B", pB*100);
				rt.addValue("unit", "%");
				rt.incrementCounter();
				rt.addLabel("+-1% reliability");
				rt.addValue("value R", p1*100);
				rt.addValue("value G", p1G*100);
				rt.addValue("value B", p1B*100);
				rt.addValue("unit", "%");
				rt.incrementCounter();
				rt.addValue("value R", "");
				rt.addValue("value G", "");
				rt.addValue("value B", "");
				rt.addValue("unit", "");
			}else{
				rt.incrementCounter();
				rt.addLabel("points");
				rt.addValue("value", n);
				rt.addValue("unit", "pixel");
				rt.incrementCounter();
				rt.addLabel("mean");
				rt.addValue("value", mean[1]);
				rt.addValue("unit", "");
				rt.incrementCounter();
				rt.addLabel("std");
				rt.addValue("value", std[0]);
				rt.addValue("unit", "");
				rt.incrementCounter();
				rt.addLabel("+-0.5 reliability");
				rt.addValue("value", p*100);
				rt.addValue("unit", "%");
				rt.incrementCounter();
				rt.addLabel("+-1% reliability");
				rt.addValue("value", p1*100);
				rt.addValue("unit", "%");
				rt.incrementCounter();
				rt.addValue("value", "");
				rt.addValue("unit", "");
			}
			rt.showRowNumbers(false);
			rt.show("Results");
		    
			
			/**output to Histogram**/
			plotYmax = 0;
			ImagePlus img = WindowManager.getCurrentImage();
			float[] minmax = LJPrefs.getMinMax(img);
			float max = minmax[1];
			float min = minmax[0];
			//IJ.log(String.valueOf(min) + "<" + String.valueOf(max));
			
			Plot hist = plotHistogram(img, min, max);
			//PlotWindow window = hist.show();
			

			int Nvox = img.getWidth()*img.getHeight()*img.getStackSize();
			double[] tmean = {mean[1],mean[2],mean[3]};
			Plot gauss = plotGauss(hist, tmean, std, Nvox, min, max);
			
			/*add square with average colour*/
			ImagePlus ansimg = gauss.getImagePlus();
			ImageProcessor ansplot = ansimg.getProcessor();
			double width = ansplot.getWidth();
			//double height = ansplot.getHeight();
			int avColor = (int)mean[1];
			avColor = (int)(avColor*255/max);
			avColor = ((avColor & 0xff)<<16)+((avColor & 0xff)<<8) + (avColor & 0xff);
			if(mean[2] != 0){
				avColor = (((int)mean[1] & 0xff)<<16)+(((int)mean[2] & 0xff)<<8) + ((int)mean[3] & 0xff);
			}
			for (int i=(int)(width*0.9); i < width; i++){
				for (int j=0; j < (int)(width*0.1); j++){
					ansplot.set(i, j, avColor);
				}
			}
			
			ansimg.show();
			
			//window.drawPlot(gauss);

			
			
		    } 
		}
		
	}
	
	Plot plotHistogram(ImagePlus imp, float min, float max){
		//float[] minmax = LJPrefs.getMinMax(imp);
		float[] Xs = new float[256];
		float[] Ys = new float[256];
		float[] YsG = new float[256];
		float[] YsB = new float[256];

		ImageStack stack = imp.getStack();
		int width = imp.getWidth();
		int height = imp.getHeight();
		int n = width*height;
		int images = imp.getStackSize();
		
		for (int i=0; i<=255; i++){
			Xs[i] = i*(max-min)/255+min;
		}
		
		Plot plot = new Plot("Histogram", "pixel value", "pixel count");
		plot.setLimits(min, max, 0, n);
		if(imp.getBitDepth() == 24){
			for (int img=1; img<=images; img++) {
				ImageProcessor ip = stack.getProcessor(img);
				for (int y=0; y<height; y++){
		    		for (int x=0; x<width; x++){
		    			int[] pixel = new int[3];
		    			ip.getPixel(x, y,pixel);
		    			int[] box = new int[3];
		    			//IJ.log(String.valueOf(pixel[0])+"|"+String.valueOf(pixel[1])+"|"+String.valueOf(pixel[2]));
		    			box[0] = Math.round(((float)pixel[0]-min)*255/(max-min));
		    			box[1] = Math.round(((float)pixel[1]-min)*255/(max-min));
		    			box[2] = Math.round(((float)pixel[2]-min)*255/(max-min));
		    			Ys[box[0]] += 1;
		    			YsG[box[1]] += 1;
		    			YsB[box[2]] += 1;
		    			if (Ys[box[0]] > plotYmax){
							plotYmax = Ys[box[0]];
						}
		    			if (YsG[box[1]] > plotYmax){
							plotYmax = YsG[box[1]];
						}
		    			if (YsB[box[2]] > plotYmax){
							plotYmax = YsB[box[2]];
						}
		    		}
				}
			}
			plot.setColor(Color.RED);
			plot.addPoints(Xs, Ys, Plot.LINE);
			plot.draw();
			plot.setColor(Color.GREEN);
			plot.addPoints(Xs, YsG, Plot.LINE);
			plot.draw();
			plot.setColor(Color.BLUE);
			plot.addPoints(Xs, YsB, Plot.LINE);
			plot.draw();
		}else{
			for (int img=1; img<=images; img++) {
				ImageProcessor ip = stack.getProcessor(img);
				for (int i=0; i<n; i++) {
					float v = ip.getf(i);
					int box = Math.round((v-min)*255/(max-min));
					Ys[box] += 1;
					if (Ys[box] > plotYmax){
						plotYmax = Ys[box];
					}
					//Xs[box] = box*(max-min)/255+min; //preassign automatically at start?
				}
			}
			plot.addPoints(Xs, Ys, Plot.LINE);
			plot.setColor(Color.BLACK);
			plot.draw();
		}
		
		
		
		return plot;
	}
	
	Plot plotGauss(Plot plot, double[] mean, double[] std, int n, float min, float max){
		float[] Xs = new float[256];
		float[] Ys = new float[256];
		float[] YsG = new float[256];
		float[] YsB = new float[256];
		
		float yMax = 0;
		float yMaxG = 0;
		float yMaxB = 0;
		
		
		
		float step = (max-min)/256;
		float F1 = (float)(1/(std[0]*Math.sqrt(2*Math.PI)));
		float D1 = (float)(2*Math.pow(std[0],2));
		float F2 = (float)(1/(std[1]*Math.sqrt(2*Math.PI)));
		float D2 = (float)(2*Math.pow(std[1],2));
		float F3 = (float)(1/(std[2]*Math.sqrt(2*Math.PI)));
		float D3 = (float)(2*Math.pow(std[2],2));
		//IJ.log(String.valueOf(n));
		float x = min;
		
		if(mean[2] != 0){
			for (int i = 0; i < 256; i++){
				Xs[i] = x;
				Ys[i] = (float)(0.66*step*n*F1*Math.exp(-1*Math.pow(x-mean[0],2)/D1));
				YsG[i] = (float)(0.66*step*n*F2*Math.exp(-1*Math.pow(x-mean[1],2)/D2));
				YsB[i] = (float)(0.66*step*n*F3*Math.exp(-1*Math.pow(x-mean[2],2)/D3));
				if (Ys[i] > yMax){
					yMax = Ys[i];
				}
				if (YsG[i] > yMaxG){
					yMaxG = YsG[i];
				}
				if (YsB[i] > yMaxB){
					yMaxB = YsB[i];
				}
				x += step;
			}
			float[] mR = {(float)mean[0]};
			float[] yR = {(float)yMax};
			float[] mG = {(float)mean[1]};
			float[] yG = {(float)yMaxG};
			float[] mB = {(float)mean[2]};
			float[] yB = {(float)yMaxB};
			plot.setColor(Color.RED);
			plot.addPoints(Xs, Ys, Plot.DOT);
			plot.addPoints(mR, yR, Plot.CIRCLE);
			plot.setColor(Color.GREEN);
			plot.addPoints(Xs, YsG, Plot.DOT);
			plot.addPoints(mG, yG, Plot.CIRCLE);
			plot.setColor(Color.BLUE);
			plot.addPoints(Xs, YsB, Plot.DOT);
			plot.addPoints(mB, yB, Plot.CIRCLE);
		}else{
			for (int i = 0; i < 256; i++){
				Xs[i] = x;
				Ys[i] = (float)(0.66*step*n*F1*Math.exp(-1*Math.pow(x-mean[0],2)/D1));
				if (Ys[i] > yMax){
					yMax = Ys[i];
				}
				x += step;
			}
			plot.setColor(Color.BLACK);
			//if (plot == null){
			//	plot = new Plot("Gaussian", "pixel value", "occurence", Xs, Ys);
			//}else{
				plot.addPoints(Xs, Ys, Plot.DOT);
			//}
		    float[] mR = {(float)mean[0]};
			float[] yR = {(float)yMax};
			plot.addPoints(mR, yR, Plot.TRIANGLE);
		}
		
		plotYmax = Math.max(plotYmax,  yMax);
		plotYmax = Math.max(plotYmax,  yMaxG);
		plotYmax = Math.max(plotYmax,  yMaxB);
		//yMax = Math.max(yMax,  (float)(n/3));
		
		plot.setLimits(min, max, 0, plotYmax);
		plot.draw();
		return plot;
	}
	
	
	double tToP(double t,int df){
		double A9 = df-0.5;
	    double B9 = 48*A9*A9;
	    double T9 = t*t/df;
	    double Z8 = 0;
	    if (T9 > 0.04){
	    	Z8 = A9*Math.log(1+T9);
	    }else{
	    	Z8 = A9*(((1-T9*0.75)*T9/3-0.5)*T9+1)*T9;
	    }
	    double P7 = ((0.4*Z8+3.3)*Z8+24)*Z8+85.5;
	    double B7 = 0.8*Z8*Z8+100+B9;
	    double z = (1+(-P7/B7+Z8+3)/B9)*Math.sqrt(Z8);
	    
	    double a1 = 0.000005383;
	    double a2 = 0.0000488906;
	    double a3 = 0.0000380036;
	    double a4 = 0.0032776263;
	    double a5 = 0.0211410061;
	    double a6 = 0.049867347;
	    double p_2t = Math.pow((((((a1*z+a2)*z+a3)*z+a4)*z+a5)*z+a6)*z+1,-16);
	    double p = Math.abs(2-p_2t) - 1;
	    return p;
	}
	
	
	void showCoordinates(PolygonRoi roi) {
	    PolygonRoi polygon = (PolygonRoi)roi;
	    int[] x = polygon.getXCoordinates();
	    int[] y = polygon.getYCoordinates();
	    Rectangle bounds = polygon.getBounds();
	    for (int i = 0; i < x.length; i++)
	        // x, y are relative to the bounds' origin
	        IJ.log("point " + i + ": " + (x[i] + bounds.x) + "|" + (y[i] + bounds.y));
	}
	
	double[] updateMean(ImageProcessor ip, PointRoi roi, double[] inp) {
		double mean = inp[1];
		double meanG = inp[2];
		double meanB = inp[3];
		int n = (int)inp[0];
	    Rectangle bounds = roi.getBounds();
	    
	    mean = mean * n;
		meanG = meanG * n;
		meanB = meanB * n;
	    if(ip.getBitDepth() == 24){
	    	int[] pixel = new int[3];
			ip.getPixel(bounds.x, bounds.y,pixel);
			n = n + 1;
			mean = mean + pixel[0];
			meanG = meanG + pixel[1];
			meanB = meanB + pixel[2];
	    }else{
	    	int pixel = ip.get(bounds.x, bounds.y);
		    n = n + 1;
		    mean = mean + pixel;
	    }
	    mean = mean / n;
	    meanG = meanG / n;
		meanB = meanB / n;
	    
	    double[] ans = {(double)n,mean,meanG,meanB}; 
	    
	    return ans;
	}
	
	double[] updateMean(ImageProcessor ip, Roi roi, double[] inp) {
		int n = (int)inp[0];
		double mean = inp[1];
		double meanG = inp[2];
		double meanB = inp[3];
	    Rectangle roiRect = roi.getBounds();
		int width = ip.getWidth();
		
		mean = mean * n;
		meanG = meanG * n;
		meanB = meanB * n;
		if(ip.getBitDepth() == 24){
			for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
	    		for (int x=roiRect.x; x<roiRect.x+roiRect.width; x++){
	    			int[] pixel = new int[3];
	    			ip.getPixel(x, y,pixel);
	    			mean = mean + pixel[0];
	    			meanG = meanG + pixel[1];
	    			meanB = meanB + pixel[2];
	    			n = n + 1;
	    		}
	    	}
		}else{
			for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
				for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++){
					double pixel = (double)ip.getf(p);
					mean = mean + pixel;
					n = n + 1;
				}
			}
		}
	    mean = mean / n;
	    meanG = meanG / n;
		meanB = meanB / n;
	    
	    double[] ans = {(double)n,mean,meanG,meanB}; 
	    
	    return ans;
	}
	
	double[] updateStd(ImageProcessor ip, PointRoi roi, double[] cstd, double[] mn) {
		double[] mean = {mn[1],mn[2],mn[3]};
		//int n = (int)mn[0];
	    Rectangle bounds = roi.getBounds();
	    
	    if(ip.getBitDepth() == 24){
	    	int[] pixel = new int[3];
	    	ip.getPixel(bounds.x, bounds.y,pixel);
	    	cstd[0] = cstd[0] + (mean[0] - pixel[0])*(mean[0] - pixel[0]);
	    	cstd[1] = cstd[1] + (mean[1] - pixel[1])*(mean[1] - pixel[1]);
	    	cstd[2] = cstd[2] + (mean[2] - pixel[2])*(mean[2] - pixel[2]);
		}else{
			int pixel = ip.get(bounds.x, bounds.y);
			cstd[0] = cstd[0] + (mean[0] - pixel)*(mean[0] - pixel);
		}
	    
	    
	    return cstd; 
	}
	
	double[] updateStd(ImageProcessor ip, Roi roi, double[] cstd, double[] mn) {
		double[] mean = {mn[1],mn[2],mn[3]};
		//int n = (int)mn[0];
	    Rectangle roiRect = roi.getBounds();
		int width = ip.getWidth();
		
		if(ip.getBitDepth() == 24){
			for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
	    		for (int x=roiRect.x; x<roiRect.x+roiRect.width; x++){
	    			int[] pixel = new int[3];
	    	    	ip.getPixel(x, y,pixel);
	    			cstd[0] = cstd[0] + (mean[0] - pixel[0])*(mean[0] - pixel[0]);
	    			cstd[1] = cstd[1] + (mean[1] - pixel[1])*(mean[1] - pixel[1]);
	    			cstd[2] = cstd[2] + (mean[2] - pixel[2])*(mean[2] - pixel[2]);
	    		}
	    	}
		}else{		
			for (int y=roiRect.y; y<roiRect.y+roiRect.height; y++){
				for (int x=roiRect.x, p=x+y*width; x<roiRect.x+roiRect.width; x++,p++){
					double pixel = (double)ip.getf(p);
					cstd[0] = cstd[0] + (mean[0] - pixel)*(mean[0] - pixel);
				}
			}
		}
	    
	    return cstd;
	}

	
}
