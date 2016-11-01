package lj.process;

import ij.*;
import ij.gui.*;
import ij.measure.*;
import ij.plugin.filter.Analyzer;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

import java.awt.*;
import java.io.IOException;
import java.util.Properties;

import lj.LJPrefs;

/***
 * Statistics, including the histogram, of 3D image blocks. 
 * 
 * @see ij.process.ImageStatistics
 ***/
public class VirtualBlockStatistics extends ImageStatistics {
	
	
	
	/*** VirtualBlockStatistics ***
     * 
     * @param  filepath            String containing the full path to the LungJ block
     * @param  nBins               int containing the number of bins of the histogram requested
     * @param  globMin             double containing the global minimum voxel value
     * @param  globMax             double containing the global maximum voxel value
	 * @see    #doCalculationsFromHistogram
	 * @see    #sum8BitHistograms
     * @see    #sum16BitHistograms 
     * @see    #copyHistogram
     * @see    LJPrefs#readProperties
     * @see    LJPrefs#getPref
     ***/
	public VirtualBlockStatistics(String filepath, int nBins, double globMin, double globMax) {
		stackStatistics = true;
		
		/** read image properties from the properties file **/
		Properties prefs = new Properties();
		try {
			prefs = LJPrefs.readProperties(filepath + "\\properties.txt");
		} catch (IOException e) {
			//TODO Auto-generated catch block if no LJinfo file found
			e.printStackTrace();
		}
		int maxX = LJPrefs.getPref(prefs, "maxX", 0);
		int maxY = LJPrefs.getPref(prefs, "maxY", 0);
		int maxZ = LJPrefs.getPref(prefs, "maxZ", 0);
		int stepX = LJPrefs.getPref(prefs, "stepX", 1);
		int stepY = LJPrefs.getPref(prefs, "stepY", 1);
		int stepZ = LJPrefs.getPref(prefs, "stepZ", 1);
		int haloX = LJPrefs.getPref(prefs, "haloX", 0);
		int haloY = LJPrefs.getPref(prefs, "haloY", 0);
		int haloZ = LJPrefs.getPref(prefs, "haloZ", 0);
		
		histMin = globMin;
		histMax = globMax;
		
		
		IJ.showStatus("Reading blocks...");
		IJ.showProgress(0, 100);
		int prog = 0;
		int diffz = (int)Math.ceil(maxZ/(float)stepZ);
		int diffx = (int)Math.ceil(maxX/(float)stepX);
		int diffy = (int)Math.ceil(maxY/(float)stepY);
		int maxProg = (diffy*diffx*diffz)*100/99;
		
		double[] Xs = new double[nBins];
		long[] Ys = new long[nBins];
		double[] YsG = new double[nBins];
		double[] YsB = new double[nBins];
		
		double measuredMin = Double.MAX_VALUE;
		double measuredMax = -Double.MAX_VALUE;

		
		long plotYmax = 0;
		
		for (int i=0; i<nBins; i++){
			Xs[i] = (float)(i*(max-globMin)/(nBins-1)+globMin);
		}
		
		int testcount = 0;
		
		/** loop over image blocks to create the histogram **/
		for (int z=0; z<maxZ; z+=stepZ) {
			for (int x=0; x<maxX; x+=stepX) {
				for (int y=0; y<maxY; y+=stepY) {
					/** read in each block **/
					String filein = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",filepath,z,y,x);
					ImagePlus imgblock = IJ.openImage(filein);
					
					/** get size values from block **/
					ImageStack stack = imgblock.getStack();
					//int width = imgblock.getWidth();
					//int height = imgblock.getHeight();
					//int n = width*height;
					//int images = imgblock.getStackSize();
					int bWidth = imgblock.getWidth();
					int bHeight = imgblock.getHeight();
					int bDepth = imgblock.getNSlices();
					
					/** get start and end of actual content, excluding the halo **/
					int xs = stepX+2*haloX;
					int ys = stepY+2*haloY;
					int zs = stepZ+2*haloZ;
					int x1 = haloX;
					if (x-haloX < 0){x1 = 0; xs = xs - haloX;}
					int y1 = haloY;
					if (y-haloY < 0){y1 = 0; ys = ys - haloY;}
					int z1 = haloZ;
					if (z-haloZ < 0){z1 = 0; zs = zs - haloZ;}
					int x2 = bWidth - haloX;
					if (x + xs > maxX){x2 = bWidth;}
					int y2 = bHeight - haloY;
					if (y + ys > maxY){y2 = bHeight;}
					int z2 = bDepth - haloZ;
					if (z + zs > maxZ){z2 = bDepth;}
					
					
					//double scale = nBins/(histMax-histMin);
					
					if(imgblock.getBitDepth() == 24){ //RGB image
						/** loop over non-halo voxel of the block**/
						for (int img=z1+1; img<=z2; img++) {
							ImageProcessor ip = stack.getProcessor(img);
							for (int iy=y1; iy<y2; iy++){
					    		for (int ix=x1; ix<x2; ix++){
					    			/** get pixel value **/
					    			int[] pixel = new int[3];
					    			ip.getPixel(ix, iy,pixel);
					    			/** check which bin/box to place it into **/
					    			int[] box = new int[3];
					    			box[0] = (int) Math.round(((double)pixel[0]-globMin)*(nBins-1)/(globMax-globMin));
					    			box[1] = (int) Math.round(((double)pixel[1]-globMin)*(nBins-1)/(globMax-globMin));
					    			box[2] = (int) Math.round(((double)pixel[2]-globMin)*(nBins-1)/(globMax-globMin));
					    			if (box[0] >= nBins){box[0] = nBins-1;}
					    			if (box[1] >= nBins){box[1] = nBins-1;}
					    			if (box[2] >= nBins){box[2] = nBins-1;}
					    			if (box[0] < 0){box[0] = 0;}
					    			if (box[1] < 0){box[1] = 0;}
					    			if (box[2] < 0){box[2] = 0;}
					    			/** increase according counter in the histogram **/
					    			Ys[box[0]] += 1;
					    			YsG[box[1]] += 1;
					    			YsB[box[2]] += 1;
					    			/** check which colour has the biggest counter **/
					    			if (Ys[box[0]] > plotYmax){
										plotYmax = (long) Ys[box[0]];
									}
					    			if (YsG[box[1]] > plotYmax){
										plotYmax = (long) YsG[box[1]];
									}
					    			if (YsB[box[2]] > plotYmax){
										plotYmax = (long) YsB[box[2]];
									}
					    			/** reset variables **/
					    			pixel = null;
					    			box = null;
					    		}
							}
							ip = null;
						}
					}else{
						double v = 0;
						/** loop over non-halo voxel of the block**/
						for (int img=z1+1; img<=z2; img++) {
							ImageProcessor ip = stack.getProcessor(img);
							for (int iy=y1; iy<y2; iy++){
					    		for (int ix=x1; ix<x2; ix++){ //int ix=x1, i=ix+iy*width
					    			/** get pixel value **/
									v = ip.getf(ix,iy);
									testcount += 1;
									/** check which bin/box to place it into **/
									int box = (int) ((v-globMin)*(nBins-1)/(double)(globMax-globMin));
									if (box >= nBins){box = nBins-1;}
					    			if (box < 0){box = 0;}
					    			/** increase according counter in the histogram **/
									Ys[box] += 1;
									/** get extreme values **/
									if (Ys[box] > plotYmax){
										plotYmax = (long) Ys[box];
									}
									if (v < measuredMin){
										measuredMin = v;
									}
									if (v > measuredMax){
										measuredMax = v;
									}
					    		}
							}
							/** reset variables **/
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
				}
			}
		}
		IJ.showProgress(99, 100);
		
		IJ.log(String.valueOf(testcount));
		
		/** open first image block **/
		int x = 0;
		int y = 0;
		int z = 0;
		String filein = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",filepath,z,y,x);
		ImagePlus imp2 = IJ.openImage(filein);
		imp2.show();
		int bits = imp2.getBitDepth();
		/** set statistics parameters **/
		min = measuredMin;
		max = measuredMax;
		stackStatistics = true;
		longHistogram = new long[nBins];
		longHistogram = Ys;
		longPixelCount = (long)maxX*(long)maxY*(long)maxZ;
		
		if ((bits==8||bits==24) && nBins==256 && globMin==0.0 && globMax==256.0)
			sum8BitHistograms(imp2);
		else if (bits==16 && nBins==256 && globMin==0.0 && globMax==0.0 && !imp2.getCalibration().calibrated())
			sum16BitHistograms(imp2);
		else
			doCalculationsFromHistogram(imp2, nBins, globMin, globMax, maxX, maxY, measuredMin, measuredMax);
		/** set statistics parameters **/
		longHistogram = new long[nBins];
		longHistogram = Ys;
		histYMax = (int)plotYmax;
		copyHistogram(nBins);
		longPixelCount = (long)maxX*(long)maxY*(long)maxZ;
		min = measuredMin;
		max = measuredMax;
		
	}
	
	/*** VirtualBlockStatistics
	 * calls default if only image is provided
	 * @param imp   ImagePlus
	 * @see   #VirtualBlockStatistics
	 */
	public VirtualBlockStatistics(ImagePlus imp) {
		this(imp, 256, 0.0, 0.0);
	}

	/*** VirtualBlockStatistics
	 * computes the histogram based on parameters specified
	 * @param imp               ImagePlus
	 * @param nBins             number of histogram bins (segments)
	 * @param histMin           lowest value for histogram range
	 * @param histMax           highest value for histogram range
	 * @see   #sum8BitHistograms
	 * @see   #sum16BitHistograms
	 * @see   #doCalculations
	 ***/
	public VirtualBlockStatistics(ImagePlus imp, int nBins, double histMin, double histMax) {
		int bits = imp.getBitDepth();
		stackStatistics = true;
		if ((bits==8||bits==24) && nBins==256 && histMin==0.0 && histMax==256.0)
			sum8BitHistograms(imp);
		else if (bits==16 && nBins==256 && histMin==0.0 && histMax==0.0 && !imp.getCalibration().calibrated())
			sum16BitHistograms(imp);
		else
			doCalculations(imp, nBins, histMin, histMax);
	}
	
	/*** VirtualBlockStatistics
	 * computes the histogram based on parameters specified
	 * @param imp               ImagePlus
	 * @param nBins             number of histogram bins (segments)
	 * @param histMin           lowest value for histogram range
	 * @param histMax           highest value for histogram range
	 * @param longhist          long[] containing histogram (long instead of int required for large images)
	 * @param pixelcount        long number of pixel
	 * @param imgwidth          int width of the image
	 * @param imgheight         int height of the image
	 * @param imgmin            double minimum pixel value of the image
	 * @param imgmax            double maximum pixel value of the image
	 ***/
	public VirtualBlockStatistics(ImagePlus imp, int nBins, double histMin, double histMax, long[] longhist, long pixelcount, int imgwidth, int imgheight, double imgmin, double imgmax) {
		int bits = imp.getBitDepth();
		stackStatistics = true;
		longHistogram = new long[nBins];
		longHistogram = longhist;
		longPixelCount = pixelcount;
		if ((bits==8||bits==24) && nBins==256 && histMin==0.0 && histMax==256.0)
			sum8BitHistograms(imp);
		else if (bits==16 && nBins==256 && histMin==0.0 && histMax==0.0 && !imp.getCalibration().calibrated())
			sum16BitHistograms(imp);
		else
			//doCalculations(imp, nBins, histMin, histMax);
			doCalculationsFromHistogram(imp, nBins, histMin, histMax, imgwidth, imgheight, imgmin, imgmax);
		longHistogram = new long[nBins];
		longHistogram = longhist;
	}
	
	void doCalculationsFromHistogram(ImagePlus imp, int bins, double histogramMin, double histogramMax, int imgwidth, int imgheight, double imgmin, double imgmax) {
        //ImageProcessor ip = imp.getProcessor();
		//boolean limitToThreshold = (Analyzer.getMeasurements()&LIMIT)!=0;
		//double minThreshold = -Float.MAX_VALUE;
		//double maxThreshold = Float.MAX_VALUE;
        //Calibration cal = imp.getCalibration();
		//if (limitToThreshold && ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
		//	minThreshold=cal.getCValue(ip.getMinThreshold());
		//	maxThreshold=cal.getCValue(ip.getMaxThreshold());
		//}
    	nBins = bins;
    	histMin = histogramMin;
    	histMax = histogramMax;
        //ImageStack stack = imp.getStack();
        //int size = stack.getSize();
        //ip.setRoi(imp.getRoi());
        //byte[] mask = ip.getMaskArray();
        //float[] cTable = imp.getCalibration().getCTable();
        //longHistogram = longhist;
        double v;
        double sum = 0;
        double sum2 = 0;
        int width, height;
        int rx, ry, rw, rh;
        double pw, ph;
        
        width = imgwidth;
        height = imgheight;
        //Rectangle roi = ip.getRoi();
        rx = 0;
        ry = 0;
        rw = width;
        rh = height;
        
        pw = 1.0;
        ph = 1.0;
        roiX = rx*pw;
        roiY = ry*ph;
        roiWidth = rw*pw;
        roiHeight = rh*ph;
        boolean fixedRange = histMin!=0 || histMax!=0.0;
        
        // calculate min and max
		//double roiMin = Double.MAX_VALUE;
		//double roiMax = -Double.MAX_VALUE;
		
		min = imgmin; /**TODO: fix**/
		max = imgmax;
		
		if (fixedRange) {
			if (min<histMin) min = histMin;
			if (max>histMax) max = histMax;
		} else {
			histMin = min; 
			histMax =  max;
		}
       
        // Generate histogram
        //double scale = nBins/( histMax-histMin);
        for(int i=0;i<longHistogram.length;i++){
        	v = ((i/(double)longHistogram.length)*(histMax-histMin)+histMin);
        	sum += v*longHistogram[i];
        	sum2 += v*v*longHistogram[i];
        }
        IJ.log(String.valueOf(sum));
        pixelCount = (int)longPixelCount;
        area = longPixelCount*pw*ph;
        mean = sum/(double)longPixelCount;
        calculateStdDev(longPixelCount, sum, sum2);
        //histMin = cal.getRawValue(histMin); 
        //histMax =  cal.getRawValue(histMax);
        binSize = (histMax-histMin)/nBins;
        int bits = imp.getBitDepth();
        if (histMin==0.0 && histMax==256.0 && (bits==8||bits==24))
        	histMax = 255.0;
        dmode = getMode(cal);
		copyHistogram(nBins);
        IJ.showStatus("");
        IJ.showProgress(1.0);
    }

    void doCalculations(ImagePlus imp,  int bins, double histogramMin, double histogramMax) {
        ImageProcessor ip = imp.getProcessor();
		boolean limitToThreshold = (Analyzer.getMeasurements()&LIMIT)!=0;
		double minThreshold = -Float.MAX_VALUE;
		double maxThreshold = Float.MAX_VALUE;
        Calibration cal = imp.getCalibration();
		if (limitToThreshold && ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
			minThreshold=cal.getCValue(ip.getMinThreshold());
			maxThreshold=cal.getCValue(ip.getMaxThreshold());
		}
    	nBins = bins;
    	histMin = histogramMin;
    	histMax = histogramMax;
        ImageStack stack = imp.getStack();
        int size = stack.getSize();
        ip.setRoi(imp.getRoi());
        byte[] mask = ip.getMaskArray();
        float[] cTable = imp.getCalibration().getCTable();
        longHistogram = new long[nBins];
        double v;
        double sum = 0;
        double sum2 = 0;
        int width, height;
        int rx, ry, rw, rh;
        double pw, ph;
        
        width = ip.getWidth();
        height = ip.getHeight();
        Rectangle roi = ip.getRoi();
        if (roi != null) {
            rx = roi.x;
            ry = roi.y;
            rw = roi.width;
            rh = roi.height;
        } else {
            rx = 0;
            ry = 0;
            rw = width;
            rh = height;
        }
        
        pw = 1.0;
        ph = 1.0;
        roiX = rx*pw;
        roiY = ry*ph;
        roiWidth = rw*pw;
        roiHeight = rh*ph;
        boolean fixedRange = histMin!=0 || histMax!=0.0;
        
        // calculate min and max
		double roiMin = Double.MAX_VALUE;
		double roiMax = -Double.MAX_VALUE;
		for (int slice=1; slice<=size; slice++) {
			IJ.showStatus("Calculating stack histogram...");
			IJ.showProgress(slice/2, size);
			ip = stack.getProcessor(slice);
			//ip.setCalibrationTable(cTable);
			for (int y=ry, my=0; y<(ry+rh); y++, my++) {
				//int i = y * width + rx;
				int mi = my * rw;
				for (int x=rx; x<(rx+rw); x++) {
					if (mask==null || mask[mi++]!=0) {
						v = ip.getPixelValue(x,y);
						if (v>=minThreshold && v<=maxThreshold) {
							if (v<roiMin) roiMin = v;
							if (v>roiMax) roiMax = v;
						}
					}
					//i++;
				}
			}
		 }
		min = roiMin;
		max = roiMax;
		if (fixedRange) {
			if (min<histMin) min = histMin;
			if (max>histMax) max = histMax;
		} else {
			histMin = min; 
			histMax =  max;
		}
       
        // Generate histogram
        double scale = nBins/( histMax-histMin);
        pixelCount = 0;
        int index;
        //boolean first = true;
        for (int slice=1; slice<=size; slice++) {
            IJ.showProgress(size/2+slice/2, size);
            ip = stack.getProcessor(slice);
            ip.setCalibrationTable(cTable);
            for (int y=ry, my=0; y<(ry+rh); y++, my++) {
                //int i = y * width + rx;
                int mi = my * rw;
                for (int x=rx; x<(rx+rw); x++) {
                    if (mask==null || mask[mi++]!=0) {
                        v = ip.getPixelValue(x,y);
						if (v>=minThreshold && v<=maxThreshold && v>=histMin && v<=histMax) {
							longPixelCount++;
							sum += v;
							sum2 += v*v;
							index = (int)(scale*(v-histMin));
							if (index>=nBins)
								index = nBins-1;
							longHistogram[index]++;
						}
                    }
                    //i++;
                }
            }
        }
        pixelCount = (int)longPixelCount;
        area = longPixelCount*pw*ph;
        mean = sum/longPixelCount;
        calculateStdDev(longPixelCount, sum, sum2);
        histMin = cal.getRawValue(histMin); 
        histMax =  cal.getRawValue(histMax);
        binSize = (histMax-histMin)/nBins;
        int bits = imp.getBitDepth();
        if (histMin==0.0 && histMax==256.0 && (bits==8||bits==24))
        	histMax = 255.0;
        dmode = getMode(cal);
		copyHistogram(nBins);
        IJ.showStatus("");
        IJ.showProgress(1.0);
    }
    
	void sum8BitHistograms(ImagePlus imp) {
		//Calibration cal = imp.getCalibration();
		boolean limitToThreshold = (Analyzer.getMeasurements()&LIMIT)!=0;
		int minThreshold = 0;
		int maxThreshold = 255;
		ImageProcessor ip = imp.getProcessor();
		if (limitToThreshold && ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
			minThreshold = (int)ip.getMinThreshold();
			maxThreshold = (int)ip.getMaxThreshold();
		}
		ImageStack stack = imp.getStack();
		Roi roi = imp.getRoi();
		longHistogram = new long[256];
		int n = stack.getSize();
		for (int slice=1; slice<=n; slice++) {
			IJ.showProgress(slice, n);
			ip = stack.getProcessor(slice);
			if (roi!=null) ip.setRoi(roi);
			int[] hist = ip.getHistogram();
			for (int i=0; i<256; i++)
				longHistogram[i] += hist[i];
		}
		pw=1.0; ph=1.0;
		getRawStatistics(longHistogram, minThreshold, maxThreshold);
		getRawMinAndMax(longHistogram, minThreshold, maxThreshold);
		copyHistogram(256);
		IJ.showStatus("");
		IJ.showProgress(1.0);
	}
	
	/*** copyHistogram ***
     * Copies the results from the Long-histogram to the Integer-histogram.
     * 
     * @param  nbins               int representing the number of bins (length of the histogram array)
     ***/
	private void copyHistogram(int nbins) {
        histogram = new int[nbins];
        for (int i=0; i<nbins; i++) {
            long count = longHistogram[i];
            if (count<=Integer.MAX_VALUE){
                histogram[i] = (int)count;
            }else{
                histogram[i] = Integer.MAX_VALUE;
			}
        }
    }

	/*** getRawStatistics ***
     * updates the maximum count in the histogram, the total number of pixel, area, mean pixel value and voxel value with 
	 * the highest voxel count. Also calls an update of the standard deviation and sets minimum and maximum value of the 
	 * histogram.
     * 
     * @param  histogram           long[]
     * @param  minThreshold        int
     * @param  maxThreshold        int
	 * @see    #calculateStdDev
     ***/
	void getRawStatistics(long[] histogram, int minThreshold, int maxThreshold) {
		long count;
		long longMaxCount = 0L;
		double value;
		double sum = 0.0;
		double sum2 = 0.0;
		
		histMin = 0.0;
		histMax = 255.0;
		
		for (int i=minThreshold; i<=maxThreshold; i++) {
			count = histogram[i];
			longPixelCount += count;
			sum += (double)i*count;
			value = i;
			sum2 += (value*value)*count;
			if (count>longMaxCount) {
				longMaxCount = count;
				mode = i;
			}
		}
		
		maxCount = (int)longMaxCount;
		pixelCount = (int)longPixelCount;
		area = longPixelCount*pw*ph;
		mean = sum/longPixelCount;
		umean = mean;
		dmode = mode;
		calculateStdDev(longPixelCount, sum, sum2);
		
	}

	void getRawMinAndMax(long[] histogram, int minThreshold, int maxThreshold) {
		int min = minThreshold;
		while ((histogram[min]==0L) && (min<255))
			min++;
		this.min = min;
		int max = maxThreshold;
		while ((histogram[max]==0L) && (max>0))
			max--;
		this.max = max;
	}

	void sum16BitHistograms(ImagePlus imp) {
		//Calibration cal = imp.getCalibration();
		boolean limitToThreshold = (Analyzer.getMeasurements()&LIMIT)!=0;
		int minThreshold = 0;
		int maxThreshold = 65535;
		ImageProcessor ip = imp.getProcessor();
		if (limitToThreshold && ip.getMinThreshold()!=ImageProcessor.NO_THRESHOLD) {
			minThreshold = (int)ip.getMinThreshold();
			maxThreshold = (int)ip.getMaxThreshold();
		}
		ImageStack stack = imp.getStack();
		Roi roi = imp.getRoi();
		long[] hist16 = new long[65536];
		int n = stack.getSize();
		for (int slice=1; slice<=n; slice++) {
			IJ.showProgress(slice, n);
			IJ.showStatus(slice+"/"+n);
			ip = stack.getProcessor(slice);
			if (roi!=null) ip.setRoi(roi);
			int[] hist = ip.getHistogram();
			for (int i=0; i<65536; i++)
				hist16[i] += hist[i];
		}
		pw=1.0; ph=1.0;
		get16BitMinAndMax(hist16, minThreshold, maxThreshold);
		get16BitStatistics(hist16, (int)min, (int)max);
		histogram16 = new int[65536];
		for (int i=0; i<65536; i++) {
			long count = hist16[i];
			if (count<=Integer.MAX_VALUE)
				histogram16[i] = (int)count;
			else
				histogram16[i] = Integer.MAX_VALUE;
		}
		IJ.showStatus("");
		IJ.showProgress(1.0);
	}
	
	void get16BitMinAndMax(long[] hist, int minThreshold, int maxThreshold) {
		int min = minThreshold;
		while ((hist[min]==0) && (min<65535))
			min++;
		this.min = min;
		int max = maxThreshold;
		while ((hist[max]==0) && (max>0))
			max--;
		this.max = max;
	}

	/*** get16BitStatistics ***
     * Updates the maximum count in the histogram, the total number of pixel, area, mean pixel value and voxel value with 
	 * the highest voxel count. Also calls an update of the standard deviation and sets minimum and maximum value of the 
	 * histogram.
     * 
     * @param  hist                long[]
     * @param  min                 int
     * @param  max                 int
	 * @see    #copyHistogram
	 * @see    #getMode
	 * @see    #calculateStdDev
     ***/
	void get16BitStatistics(long[] hist, int min, int max) {
		long count;
		double value;
		double sum = 0.0;
		double sum2 = 0.0;
		
		histMin = min; 
		histMax = max;
		
		nBins = 256;
		binSize = (histMax-histMin)/nBins;
		double scale = 1.0/binSize;
		int hMin = (int)histMin;
		longHistogram = new long[nBins]; // 256 bin histogram
		int index;
		for (int i=min; i<=max; i++) {
			count = hist[i];
			longPixelCount += count;
			value = i;
			sum += value*count;
			sum2 += (value*value)*count;
			index = (int)(scale*(i-hMin));
			if (index>=nBins)
				index = nBins-1;
			longHistogram[index] += count;
		}
		copyHistogram(nBins);
		
		maxCount = 0;
		pixelCount = (int)longPixelCount;
		area = longPixelCount*pw*ph;
		mean = sum/longPixelCount;
		umean = mean;
		dmode = getMode(null);
		calculateStdDev(longPixelCount, sum, sum2);
	}

    /*** getMode ***
     * looks at the Histogram and returns the voxel value with the highest voxel count
     * 
     * @param  cal                 Calibration
     * @return                     double
     ***/
    double getMode(Calibration cal) {
        long count;
        long longMaxCount = 0L;
		//iterate over the histogram, i=position~pixel_value, count=count_for_that_value
        for (int i=0; i<nBins; i++) { 
            count = longHistogram[i];
            if (count>longMaxCount) {
                longMaxCount = count;
                mode = i; //mode is the position in the histogram with the maximum count
            }
        }
        if (longMaxCount<=Integer.MAX_VALUE)
            maxCount = (int)longMaxCount;
        else
            maxCount = Integer.MAX_VALUE;
		//tmode is the pixel_value with the maximum count
        double tmode = histMin+mode*binSize;
        if (cal!=null) tmode = cal.getCValue(tmode);
            return tmode;
    }
   
    /*** calculateStdDev ***
     * Calculates the standard deviation based on the voxel count, the sum of all voxels and the sum of the square of all voxel.
     * required as calculateStdDev in ImageStatistics is not visible
     * 
     * @param  n                   double representing the total number of voxels
     * @param  sum                 double representing the sum of all voxel values
     * @param  sum2                double representing the sum of the square of all voxel values
     ***/
    void calculateStdDev(double n, double sum, double sum2) {
        if (n>0.0) {
            stdDev = (n*sum2-sum*sum)/n;
            if (stdDev>0.0){
				stdDev = Math.sqrt(stdDev/(n-1.0));
            }else{
				stdDev = 0.0;
			}
        } else {
			stdDev = 0.0;
		}
    }
    
}

