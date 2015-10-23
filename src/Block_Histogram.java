import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.gui.HistogramWindow;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.StackStatistics;
import lj.process.VirtualBlockStatistics;

import java.awt.Color;
import java.awt.Graphics;
import java.io.IOException;
import java.util.Properties;




public class Block_Histogram implements PlugIn{
	/** plugin's name */
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version */
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	private static String BC_inDirectory = LJPrefs.LJ_outDirectory;
	private static int maxX = 1;
	private static int maxY = 1;
	private static int maxZ = 1;
	private static int stepX = 1;
	private static int stepY = 1;
	private static int stepZ = 1;
	private static double globMax = 255;
	private static double globMin = 0;
	private boolean usePrefs = true;
	private boolean doUpdate = true;
	private int nBins = 256;
	private int[] histogram = null;
	private long[] longHistogram = null;
	
	public void run(String command){
		double min = globMin;
		double max = globMax;
		
		IJ.showStatus("Getting data...");
		IJ.showProgress(0, 100);
		
		/** create dialog box **/
		GenericDialog gd = new GenericDialog(command+" Histogram of 3D Blocks");
		gd.addStringField("Input directory", BC_inDirectory, 100);
		gd.addNumericField("Bins", nBins, 4);
		gd.addCheckbox("Use pixel value range", usePrefs);
		gd.addMessage("or use:");
		gd.addNumericField("X_min", min, 4);
		gd.addNumericField("X_max", max, 4);
		gd.addCheckbox("Update Properties.txt", doUpdate);
		
		IJ.showStatus("Waiting for user input...");
		gd.showDialog();
		if (gd.wasCanceled()){
			IJ.showStatus("Plug In cancelled...");
			IJ.showProgress(100, 100);
        	return;
        }
		
		/** getting user input and reading image information from preference file **/
		IJ.showStatus("Getting data...");
		BC_inDirectory = gd.getNextString();
		nBins = (int) gd.getNextNumber();
		usePrefs = gd.getNextBoolean();
		min = gd.getNextNumber();
		max = gd.getNextNumber();
		doUpdate = gd.getNextBoolean();
		Properties prefs = new Properties();
		try {
			prefs = LJPrefs.readProperties(BC_inDirectory + "\\properties.txt");
		} catch (IOException e) {
			// TODO Auto-generated catch block if no LJinfo file found
			e.printStackTrace();
		}
		
		maxX = LJPrefs.getPref(prefs, "maxX", maxX);
		maxY = LJPrefs.getPref(prefs, "maxY", maxY);
		maxZ = LJPrefs.getPref(prefs, "maxZ", maxZ);
		stepX = LJPrefs.getPref(prefs, "stepX", stepX);
		stepY = LJPrefs.getPref(prefs, "stepY", stepY);
		stepZ = LJPrefs.getPref(prefs, "stepZ", stepZ);
		
		globMin = (float)LJPrefs.getPref(prefs, "minVal", globMin);
		globMax = (float)LJPrefs.getPref(prefs, "maxVal", globMax);
		if (usePrefs){
			min = globMin;
			max = globMax;
		}
		
		
		//ImagePlus imgout = null;
		IJ.showStatus("Reading blocks...");
		IJ.showProgress(0, 100);
		int prog = 0;
		int diffz = (int)Math.ceil(maxZ/(float)stepZ);
		//IJ.log(String.valueOf(diffz)+"="+String.valueOf(maxZ)+"/"+String.valueOf(stepZ));
		int diffx = (int)Math.ceil(maxX/(float)stepX);
		//IJ.log(String.valueOf(diffx)+"="+String.valueOf(maxX)+"/"+String.valueOf(stepX));
		int diffy = (int)Math.ceil(maxY/(float)stepY);
		//IJ.log(String.valueOf(diffy)+"="+String.valueOf(maxY)+"/"+String.valueOf(stepY));
		//IJ.log(String.valueOf(diffx)+"*"+String.valueOf(diffy)+"*"+String.valueOf(diffz));
		int maxProg = (diffy*diffx*diffz)*100/99;
		
		double[] Xs = new double[nBins];
		long[] Ys = new long[nBins];
		double[] YsG = new double[nBins];
		double[] YsB = new double[nBins];
		
		double measuredMin = Double.MAX_VALUE;
		double measuredMax = -Double.MAX_VALUE;

		
		long plotYmax = 0;
		
		for (int i=0; i<nBins; i++){
			Xs[i] = (float)(i*(max-min)/(nBins-1)+min);
		}
		
		/** loop over image blocks **/
		for (int z=0; z<maxZ; z+=stepZ) {
			for (int x=0; x<maxX; x+=stepX) {
				for (int y=0; y<maxY; y+=stepY) {
					/** read in each block **/
					String filein = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z,y,x);
					ImagePlus imgblock = IJ.openImage(filein);
					
					/** get values from block **/
					ImageStack stack = imgblock.getStack();
					int width = imgblock.getWidth();
					int height = imgblock.getHeight();
					int n = width*height;
					int images = imgblock.getStackSize();
					
					if(imgblock.getBitDepth() == 24){
						for (int img=1; img<=images; img++) {
							ImageProcessor ip = stack.getProcessor(img);
							for (int iy=0; iy<height; iy++){
					    		for (int ix=0; ix<width; ix++){
					    			int[] pixel = new int[3];
					    			ip.getPixel(ix, iy,pixel);
					    			int[] box = new int[3];
					    			//IJ.log(String.valueOf(pixel[0])+"|"+String.valueOf(pixel[1])+"|"+String.valueOf(pixel[2]));
					    			box[0] = (int) Math.round(((double)pixel[0]-min)*(nBins-1)/(max-min));
					    			box[1] = (int) Math.round(((double)pixel[1]-min)*(nBins-1)/(max-min));
					    			box[2] = (int) Math.round(((double)pixel[2]-min)*(nBins-1)/(max-min));
					    			if (box[0] >= nBins){box[0] = nBins-1;}
					    			if (box[1] >= nBins){box[1] = nBins-1;}
					    			if (box[2] >= nBins){box[2] = nBins-1;}
					    			if (box[0] < 0){box[0] = 0;}
					    			if (box[1] < 0){box[1] = 0;}
					    			if (box[2] < 0){box[2] = 0;}
					    			Ys[box[0]] += 1;
					    			YsG[box[1]] += 1;
					    			YsB[box[2]] += 1;
					    			if (Ys[box[0]] > plotYmax){
										plotYmax = (long) Ys[box[0]];
									}
					    			if (YsG[box[1]] > plotYmax){
										plotYmax = (long) YsG[box[1]];
									}
					    			if (YsB[box[2]] > plotYmax){
										plotYmax = (long) YsB[box[2]];
									}
					    			pixel = null;
					    			box = null;
					    		}
							}
						}
					}else{
						double v = 0;
						for (int img=1; img<=images; img++) {
							ImageProcessor ip = stack.getProcessor(img);
							for (int i=0; i<n; i++) {
								v = ip.getf(i);
								int box = (int) Math.round((v-min)*(nBins-1)/(max-min));
								if (box >= nBins){box = nBins-1;}
				    			if (box < 0){box = 0;}
								Ys[box] += 1;
								if (Ys[box] > plotYmax){
									plotYmax = (long) Ys[box];
								}
								if (v < measuredMin){
									measuredMin = v;
								}
								if (v > measuredMax){
									measuredMax = v;
								}
								//Xs[box] = box*(max-min)/255+min; //preassign automatically at start?
							}
							ip = null;
						}
					}
					/** done with block! try to free memory again**/
					imgblock = null;
					stack = null;
					filein = null;
					System.gc();
					
					/** show progress **/
					IJ.showStatus("Reading blocks...");
					prog += 1;
					IJ.showProgress(prog, maxProg);
					//IJ.log(prog+"/"+maxProg);
					
				}
			}
		}
		IJ.showProgress(99, 100);
		
		IJ.log(String.valueOf(plotYmax));
		
		/*
		Plot plot = LJPrefs.plotHistogram(Xs, Ys, (long)(plotYmax*1.1));
		
		plot.addLabel(0, 1.2, "Count: " + String.valueOf(maxX*maxY*maxZ));
		plot.addLabel(0, 1.25, "Mean: " + "");
		plot.addLabel(0, 1.3, "StdDev: " + "");
		plot.addLabel(0, 1.35, "Bins: " + String.valueOf(nBins));
		
		plot.addLabel(0.5, 1.2, "Min: " + min + " (" + String.valueOf(measuredMin)+ ")");
		plot.addLabel(0.5, 1.25, "Max: " + max + " (" + String.valueOf(measuredMax)+ ")");
		plot.addLabel(0.5, 1.3, "Mode: " + "");
		plot.addLabel(0.5, 1.35, "Bin Width: " + "");
		
		plot.setAxisYLog(true);
		
		plot.show();
		*/
		
		int x = 0;
		int y = 0;
		int z = 0;
		String filein = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z,y,x);
		ImagePlus imp2 = IJ.openImage(filein);
		imp2.show();
		//ImagePlus imp2 = imp; //sample image
		//if (customHistogram && !stackHistogram && imp.getStackSize()>1)
		//imp2 = new ImagePlus("Temp", imp.getProcessor());
		VirtualBlockStatistics stats = new VirtualBlockStatistics(imp2, nBins, min, max, Ys, (long)maxX*(long)maxY*(long)maxZ, maxX, maxY, measuredMin, measuredMax);
		stats.histYMax = (int)plotYmax;
		longHistogram = Ys;
		copyHistogram(nBins);
		stats.histogram = histogram;
		stats.longPixelCount = (long)maxX*(long)maxY*(long)maxZ;
		
		
		if (doUpdate){
			prefs = new Properties();
			prefs.put("maxX", Double.toString(maxX));
			prefs.put("maxY", Double.toString(maxY));
			prefs.put("maxZ", Double.toString(maxZ));
			prefs.put("stepX", Double.toString(stepX));
			prefs.put("stepY", Double.toString(stepY));
			prefs.put("stepZ", Double.toString(stepZ));
			prefs.put("minVal", Double.toString(measuredMin));
			prefs.put("maxVal", Double.toString(measuredMax));
			prefs.put("meanVal", Double.toString(stats.mean));
			prefs.put("StdDev", Double.toString(stats.stdDev));
			try {
				LJPrefs.writeProperties(prefs, BC_inDirectory + "\\properties.txt");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		
		new HistogramWindow("Histogram of "+BC_inDirectory, imp2, stats);
		
		
		imp2.close();
		
    	//IJ.setThreshold(globMin,globMax,"BLACK_AND_WHITE_LUT");
		//imgout.show();
		//IJ.setMinAndMax(globMin, globMax);
		
		IJ.showStatus("Histogram created");
		IJ.showProgress(100, 100);
		
	}
	
	private void copyHistogram(int nbins) {
		histogram = new int[nbins];
		for (int i=0; i<nbins; i++) {
			long count = longHistogram[i];
			if (count<=Integer.MAX_VALUE)
				histogram[i] = (int)count;
			else
				histogram[i] = Integer.MAX_VALUE;
		}
	}
}
