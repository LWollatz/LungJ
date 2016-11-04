import ij.IJ;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
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

/*** Test_WEKA_Filter
 * implements a plug-in to run all the various filters of the Trainable WEKA Segmentation against one image.
 * useful to compare the effect of different filters.
 * 
 * @author Lasse Wollatz
 * 
 * @see    <a href="http://imagej.net/Trainable_Weka_Segmentation">Trainable Weka Segmentation</a>
 ***/
public class Test_WEKA_Filter implements PlugIn{
	/** plugin's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** URL linking to documentation **/
	public static final String PLUGIN_HELP_URL = LJPrefs.PLUGIN_HELP_URL;
	boolean doSave = true;
	boolean doShades = false;
	boolean doEnhance = true;
	int membranesize = 20;
	
	public void run(String arg) {
		String[] labels = {"Gaussian blur","Sobel","Hessian","Difference of gaussians","Membrane projections","Variance","Mean","Minimum","Maximum","Median","Anisotropic diffusion","Bilateral","(Lipschitz)","Kuwahara","(Gabor)","Derivatives","Laplacian","Structure","Entropy","Neighbors"};
		boolean[] defaultValues = {true,true,true,true,true,true,true,true,true,true,true,true,false,true,false,true,true,true,true,true};
		/** create dialog to request values from user: **/
		GenericDialog gd = new GenericDialog("WEKA Test Settings...");
		gd.addStringField("Output Directory", LJPrefs.LJ_srcDirectory, 100);
		gd.addCheckbox("save images", true);
		gd.addCheckbox("enhance contrast", true);
		gd.addCheckbox("apply 6 shades", false);
		gd.addCheckboxGroup(10, 2, labels, defaultValues);
		if (IJ.getVersion().compareTo("1.42p")>=0)
        	gd.addHelp(PLUGIN_HELP_URL);
		gd.showDialog();
        if (gd.wasCanceled()){
        	return;
        }
		
        String outputdirectory = gd.getNextString().replace("\\", "\\\\");
        doSave = gd.getNextBoolean();
        doEnhance = gd.getNextBoolean();
        doShades = gd.getNextBoolean();
        /** main values from user dialog extracted **/
        
		
		String macro = "";
		macro += "var outdir = '"+outputdirectory.replace("\\", "\\\\")+"'; //name of directory and file-prefix to save results to\n";
		macro += "rename('original');\n\n";
		
		/**Gaussian**/
		if(gd.getNextBoolean()){
			macro += "\n/****GAUSSIAN BLUR****/\n";
			macro += GaussMacro(1);
			macro += GaussMacro(2);
			macro += GaussMacro(4);
			macro += GaussMacro(8);
		}

		/**Sobel**/
		if(gd.getNextBoolean()){
			macro += "\n/****SOBEL****/\n";
			macro += SobelMacro(0);
			macro += SobelMacro(1);
			macro += SobelMacro(2);
			macro += SobelMacro(4);
			macro += SobelMacro(8);
		}

		/**Hessian**/
		if(gd.getNextBoolean()){
			macro += "\n/****HESSIAN****/\n";
			//macro += HesseMacro(0);
			macro += HesseMacro(1);
			macro += HesseMacro(2);
			macro += HesseMacro(4);
			macro += HesseMacro(8);
		}

		/**Gaussian Difference**/
		if(gd.getNextBoolean()){
			macro += "\n/****DIFFERENCE OF GAUSSIAN****/\n";
			macro += GaussDifMacro(2, 1);

			macro += GaussDifMacro(4, 2);
			macro += GaussDifMacro(4, 1);

			macro += GaussDifMacro(8, 1);
			macro += GaussDifMacro(8, 2);
			macro += GaussDifMacro(8, 4);
		}

		/**Membrane projections**/
		if(gd.getNextBoolean()){
			macro += "\n/****MEMBRANE PROJECTIONS****/\n";
			macro += MembraneMacro(1,15);
			macro += MembraneMacro(5,15);
			macro += MembraneMacro(9,15);
			macro += MembraneMacro(13,15);
			
			macro += MembraneMacro(1,19);
			macro += MembraneMacro(5,19);
			macro += MembraneMacro(9,19);
			macro += MembraneMacro(13,19);
			
			macro += MembraneMacro(1,27);
			macro += MembraneMacro(5,27);
			macro += MembraneMacro(9,27);
			macro += MembraneMacro(13,27);
			
			macro += MembraneMacro(1,43);
			macro += MembraneMacro(5,43);
			macro += MembraneMacro(9,43);
			macro += MembraneMacro(13,43);
		}

		/**Variance**/
		if(gd.getNextBoolean()){
			macro += "\n/****VARIANCE****/\n";
			macro += VarianceMacro(1);
			macro += VarianceMacro(2);
			macro += VarianceMacro(4);
			macro += VarianceMacro(8);
		}

		/**Mean**/
		if(gd.getNextBoolean()){
			macro += "\n/****MEAN****/\n";
			macro += MeanMacro(1);
			macro += MeanMacro(2);
			macro += MeanMacro(4);
			macro += MeanMacro(8);
		}

		/**Minimum**/
		if(gd.getNextBoolean()){
			macro += "\n/****MINIMUM****/\n";
			macro += MinimumMacro(1);
			macro += MinimumMacro(2);
			macro += MinimumMacro(4);
			macro += MinimumMacro(8);
		}

		/**Maximum**/
		if(gd.getNextBoolean()){
			macro += "\n/****MAXIMUM****/\n";
			macro += MaximumMacro(1);
			macro += MaximumMacro(2);
			macro += MaximumMacro(4);
			macro += MaximumMacro(8);
		}

		/**Median**/
		if(gd.getNextBoolean()){
			macro += "\n/****MEDIAN****/\n";
			macro += MedianMacro(1);
			macro += MedianMacro(2);
			macro += MedianMacro(4);
			macro += MedianMacro(8);
		}

		/**Anisotropic Diffusion**/
		if(gd.getNextBoolean()){
			macro += "\n/****ANISOTROPIC DIFFUSION****/\n";
			macro += AnisotropicDiffusionMacro(20, 1, 0.1, 0.9, 20, membranesize);
			macro += AnisotropicDiffusionMacro(20, 1, 0.35, 0.9, 20, membranesize);

			macro += AnisotropicDiffusionMacro(20, 2, 0.1, 0.9, 20, membranesize);
			macro += AnisotropicDiffusionMacro(20, 2, 0.35, 0.9, 20, membranesize);

			macro += AnisotropicDiffusionMacro(20, 4, 0.1, 0.9, 20, membranesize);
			macro += AnisotropicDiffusionMacro(20, 4, 0.35, 0.9, 20, membranesize);

			macro += AnisotropicDiffusionMacro(20, 8, 0.1, 0.9, 20, membranesize);
			macro += AnisotropicDiffusionMacro(20, 8, 0.35, 0.9, 20, membranesize);
		}

		/**Bilateral**/
		if(gd.getNextBoolean()){
			macro += "\n/****BILATERAL****/\n";
			macro += Bilateral(5, 50);
			macro += Bilateral(5, 100);

			macro += Bilateral(10, 50);
			macro += Bilateral(10, 100);

			/*macro += Bilateral(20, 50);
			macro += Bilateral(20, 100);*/
		}

		/**Lipschitz**/
		if(gd.getNextBoolean()){
			macro += "\n/****LIPSCHITZ****/\n";
			macro += "\n/*(currently broken)*/\n";
			/*macro += LipschitzMacro(5, true, false);
			macro += LipschitzMacro(10, true, false);
			macro += LipschitzMacro(15, true, false);
			macro += LipschitzMacro(20, true, false);
			macro += LipschitzMacro(25, true, false);

			macro += LipschitzMacro(5, false, true);
			macro += LipschitzMacro(10, false, true);
			macro += LipschitzMacro(15, false, true);
			macro += LipschitzMacro(20, false, true);
			macro += LipschitzMacro(25, false, true);

			macro += LipschitzMacro(5, true, true);
			macro += LipschitzMacro(10, true, true);
			macro += LipschitzMacro(15, true, true);
			macro += LipschitzMacro(20, true, true);
			macro += LipschitzMacro(25, true, true);*/
		}

		/**Kuwahara**/
		if(gd.getNextBoolean()){
			macro += "\n/****KUWAHARA****/\n";
			macro += KuwaharaMacro("Variance");
			macro += KuwaharaMacro("Variance / Mean");
			macro += KuwaharaMacro("Variance / Mean^2");
		}
		
		/**Gabor**/
		//TODO: check parameters
		if(gd.getNextBoolean()){
			macro += "\n/****GABOR****/\n";
			macro += "\n/*(currently broken)*/\n";
			/*macro += GaborMacro(1, 8, 8, 8, 8);
			macro += GaborMacro(2, 8, 8, 8, 8);
			macro += GaborMacro(4, 8, 8, 8, 8);
			macro += GaborMacro(8, 8, 8, 8, 8);*/
		}

		/**Derivatives**/
		if(gd.getNextBoolean()){
			macro += "\n/****DERIVATIVES****/\n";
			macro += Derivatives(2, 2, 0, 1);
			macro += Derivatives(3, 3, 0, 1);
			macro += Derivatives(4, 4, 0, 1);
			macro += Derivatives(5, 5, 0, 1);
			
			macro += Derivatives(2, 2, 0, 2);
			macro += Derivatives(3, 3, 0, 2);
			macro += Derivatives(4, 4, 0, 2);
			macro += Derivatives(5, 5, 0, 2);
			
			macro += Derivatives(2, 2, 0, 4);
			macro += Derivatives(3, 3, 0, 4);
			macro += Derivatives(4, 4, 0, 4);
			macro += Derivatives(5, 5, 0, 4);
			
			macro += Derivatives(2, 2, 0, 8);
			macro += Derivatives(3, 3, 0, 8);
			macro += Derivatives(4, 4, 0, 8);
			macro += Derivatives(5, 5, 0, 8);
			/*macro += Derivatives(2, 0, 0, 1);
			macro += Derivatives(1, 1, 0, 1);
			macro += Derivatives(0, 2, 0, 1);

			macro += Derivatives(3, 0, 0, 1);
			macro += Derivatives(2, 1, 0, 1);
			macro += Derivatives(1, 2, 0, 1);
			macro += Derivatives(0, 3, 0, 1);

			macro += Derivatives(4, 0, 0, 1);
			macro += Derivatives(3, 1, 0, 1);
			macro += Derivatives(2, 2, 0, 1);
			macro += Derivatives(1, 3, 0, 1);
			macro += Derivatives(0, 4, 0, 1);

			macro += Derivatives(5, 0, 0, 1);
			macro += Derivatives(4, 1, 0, 1);
			macro += Derivatives(3, 2, 0, 1);
			macro += Derivatives(2, 3, 0, 1);
			macro += Derivatives(1, 4, 0, 1);
			macro += Derivatives(0, 5, 0, 1);*/
		}

		/**Laplacian**/
		if(gd.getNextBoolean()){
			macro += "\n/****LAPLACIAN****/\n";
			macro += LaplaceMacro(1);
			macro += LaplaceMacro(2);
			macro += LaplaceMacro(4);
			macro += LaplaceMacro(8);
		}

		/**Structure**/
		if(gd.getNextBoolean()){
			macro += "\n/****STRUCTURE****/\n";
			macro += Structure(1, 1);
			macro += Structure(2, 1);
			macro += Structure(4, 1);
			macro += Structure(8, 1);

			macro += Structure(1, 3);
			macro += Structure(2, 3);
			macro += Structure(4, 3);
			macro += Structure(8, 3);
		}

		/**Entropy**/
		if(gd.getNextBoolean()){
			macro += "\n/****ENTROPY****/\n";
			macro += EntropyMacro(1,32);
			macro += EntropyMacro(2,32);
			macro += EntropyMacro(4,32);
			macro += EntropyMacro(8,32);
			
			macro += EntropyMacro(1,64);
			macro += EntropyMacro(2,64);
			macro += EntropyMacro(4,64);
			macro += EntropyMacro(8,64);
			
			macro += EntropyMacro(1,128);
			macro += EntropyMacro(2,128);
			macro += EntropyMacro(4,128);
			macro += EntropyMacro(8,128);
			
			macro += EntropyMacro(1,256);
			macro += EntropyMacro(2,256);
			macro += EntropyMacro(4,256);
			macro += EntropyMacro(8,256);
		}
		
		/**Neighbors**/
		macro += "\n/****NEIGHBORS****/\n";
		if(gd.getNextBoolean()){
			macro += NeighborMacro(1, 8);
		}
		
		IJ.log(macro);
		IJ.runMacro(macro);
	}
	
	/*** LipschitzMacro ***
     * creates a string of an ImageJ macro that executes Lipschitz filter on the image 'original'
     * 
     * @param  slope               int
     * @param  topdown             boolean
     * @param  tophat              boolean
     * @return                     String
     ***/
	private String LipschitzMacro(int slope, boolean topdown, boolean tophat){
		String options = "";
		if(topdown){
			options += " topdown";
		}
		if(topdown){
			options += " tophat";
		}
		String macro = "/***LIPSCHITZ "+slope+options+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Lipschitz', 'slope="+slope+options+" stack');\n";
		String fname = "lipschitz_";
		if(topdown){
			fname += "down";
		}
		if(topdown){
			fname += "tophat";
		}
		macro += FinalMacro(fname+"_"+slope);
		macro += "rename('Lipschitz "+slope+", "+options+"');\n";
		
		return macro;
	}
	
	/*** GaborMacro ***
     * creates a string of an ImageJ macro that executes the Weka Gabor filter on the image 'original'
     * 
     * @param  sigma               int
     * @return                     String
     ***/
	private String GaborMacro(int sigma, int gamma, int psi, int frequency, int nangles){
		String macro = "/***GABOR FILTER "+sigma+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Gabor duplicate');\n";
		macro += "selectWindow('Gabor');\n";
		macro += "run('Gabor', 'sigma="+sigma+" gamma="+gamma+" psi="+psi+" frequency="+frequency+" nangles="+nangles+"');\n";
		//run("Gabor", "sigma=1 gamma=8 psi=8 frequency=8 nangles=8");
		
		macro += FinalMacro("gabor"+sigma);
		macro += "rename('Gabor "+sigma+"');\n";
		
		return macro;
	}
	
	/*** EntropyMacro ***
     * creates a string of an ImageJ macro that executes the Entropy filter on the image 'original'
     * 
     * @param  radius              int
     * @param  number              int
     * @return                     String
     ***/
	private String EntropyMacro(int radius, int number){
		String macro = "/***ENTROPY "+radius+", "+number+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Entropy duplicate');\n";
		macro += "run('32-bit');";
		macro += "run('Entropy', 'radius="+radius+" number="+number+" stack');\n";
		
		macro += FinalMacro("entropy_r"+radius+"_bins"+number);
		macro += "rename('Entropy "+radius+", "+number+"');\n";
		
		return macro;
	}
	
	/*** KuwaharaMacro ***
     * creates a string of an ImageJ macro that executes the Linear Kuwahara filter on the image 'original'
     * 
     * @param  criterion           String
     * @return                     String
     ***/
	private String KuwaharaMacro(String criterion){
		String macro = "/***KUWAHARA "+criterion+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Kuwahara duplicate');\n";
		macro += "selectWindow('Kuwahara');\n";
		macro += "run('Linear Kuwahara', 'number_of_angles=30 line_length=11 criterion=["+criterion+"] stack');\n";
		
		macro += FinalMacro("kuwahara_"+criterion.replace(" / ", "by"));
		macro += "rename('Kuwahara "+criterion+"');\n";
		
		return macro;
	}
	
	/*** LaplaceMacro ***
     * creates a string of an ImageJ macro that executes the FeatureJ Laplacian filter on the image 'original'
     * 
     * @param  smoothing           int
     * @return                     String
     ***/
	private String LaplaceMacro(int smoothing){
		String macro = "/***LAPLACIAN "+smoothing+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('FeatureJ Laplacian', 'compute smoothing="+smoothing+"');\n";
		macro += "selectWindow('original Laplacian');\n";
		
		macro += FinalMacro("laplace"+smoothing);
		macro += "rename('Laplacian "+smoothing+"');\n";
		
		return macro;
	}
	
	/*** SobelMacro ***
     * creates a string of an ImageJ macro that executes the "Find Edges" Sobel filter on the image 'original'
     * 
     * @param  sigma               int
     * @return                     String
     ***/
	private String SobelMacro(int sigma){
		String macro = "/***SOBEL "+sigma+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Sobel duplicate');\n";
		macro += "selectWindow('Sobel');\n";
		macro += "run('Gaussian Blur 3D...', 'x="+sigma+" y="+sigma+" z="+sigma+"');\n";
		macro += "run('Find Edges');\n";
		
		macro += FinalMacro("sobel"+sigma);
		macro += "rename('Sobel "+sigma+"');\n";
		
		return macro;
	}
	
	/*** GaussMacro ***
     * creates a string of an ImageJ macro that executes the Gaussian Blur filter on the image 'original'
     * 
     * @param  sigma               int
     * @return                     String
     ***/
	private String GaussMacro(int sigma){
		String macro = "/***GAUSSIAN BLUR "+sigma+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Gaussian duplicate');\n";
		macro += "selectWindow('Gaussian');\n";
		macro += "run('Gaussian Blur 3D...', 'x="+sigma+" y="+sigma+" z="+sigma+"');\n";
		
		macro += FinalMacro("gauss"+sigma);
		macro += "rename('Gaussian Blur "+sigma+"');\n";
		
		return macro;
	}
	
	/*** GaussDifMacro ***
     * creates a string of an ImageJ macro that executes the Gaussian Difference filter on the image 'original'
     * 
     * @param  sigma1              int
     * @param  sigma2              int
     * @return                     String
     ***/
	private String GaussDifMacro(int sigma1, int sigma2){
		String macro = "/***DIFFERENCE OF GAUSSIANS "+sigma1+"-"+sigma2+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=GaussianDif1 duplicate');\n";
		macro += "selectWindow('GaussianDif1');\n";
		macro += "run('Gaussian Blur 3D...', 'x="+sigma1+" y="+sigma1+" z="+sigma1+"');\n";
		
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=GaussianDif2 duplicate');\n";
		macro += "selectWindow('GaussianDif2');\n";
		macro += "run('Gaussian Blur 3D...', 'x="+sigma2+" y="+sigma2+" z="+sigma2+"');\n";
		
		macro += "imageCalculator('Subtract', 'GaussianDif1','GaussianDif2');\n";
		macro += FinalMacro("diffgauss"+sigma1+"_"+sigma2);
		macro += "rename('Difference of Gaussian Blur "+sigma1+" - "+sigma2+"');\n";
		
		macro += "selectWindow('GaussianDif2');\n";
		macro += "close();\n";
		
		return macro;
	}
	
	/*** HesseMacro ***
     * creates a string of an ImageJ macro that executes the Hessian filters on the image 'original'
     * 
     * @param  sigma               int
     * @return                     String
     ***/
	private String HesseMacro(int sigma){
		String macro = "/***HESSIAN "+sigma+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('FeatureJ Derivatives', 'x-order=2 y-order=0 z-order=0 smoothing="+sigma+"');\n";
		macro += FinalMacro("hess"+sigma+"_a");
		macro += "rename('Hessian a');\n";
		macro += "selectWindow('original');\n";
		macro += "run('FeatureJ Derivatives', 'x-order=1 y-order=1 z-order=0 smoothing="+sigma+"');\n";
		macro += FinalMacro("hess"+sigma+"_b");
		macro += "rename('Hessian b');\n";
		macro += "selectWindow('original');\n";
		macro += "run('FeatureJ Derivatives', 'x-order=1 y-order=1 z-order=0 smoothing="+sigma+"');\n";
		macro += FinalMacro("hess"+sigma+"_c");
		macro += "rename('Hessian c');\n";
		macro += "selectWindow('original');\n";
		macro += "run('FeatureJ Derivatives', 'x-order=0 y-order=2 z-order=0 smoothing="+sigma+"');\n";
		macro += FinalMacro("hess"+sigma+"_d");
		macro += "rename('Hessian d');\n";
		
		macro += "imageCalculator('Add create', 'Hessian a','Hessian d');\n";
		macro += FinalMacro("hess"+sigma+"_trace");
		macro += "run('Divide...', 'value=2');\n";
		macro += "rename('Hessian (a+d)/2');\n";
		
		macro += "imageCalculator('Multiply create', 'Hessian a','Hessian a');\n";
		macro += "rename('Hessian a^2');\n";
		macro += "imageCalculator('Multiply create', 'Hessian b','Hessian c');\n";
		macro += "rename('Hessian bc');\n";
		macro += "imageCalculator('Multiply create', 'Hessian d','Hessian d');\n";
		macro += "rename('Hessian d^2');\n";
		macro += "imageCalculator('Multiply create', 'Hessian b','Hessian b');\n";
		macro += "rename('Hessian b^2');\n";
		macro += "imageCalculator('Multiply create', 'Hessian a','Hessian d');\n";
		macro += "rename('Hessian ad');\n";
		
		macro += "imageCalculator('Subtract create', 'Hessian ad','Hessian bc');\n";
		macro += FinalMacro("hess"+sigma+"_determinant");
		macro += "rename('Hessian Determinant "+sigma+"');\n";
		
		macro += "imageCalculator('Add create', 'Hessian a^2','Hessian bc');\n";
		macro += "rename('Hessian module');\n";
		macro += "imageCalculator('Add', 'Hessian module','Hessian d^2');\n";
		macro += "run('Square Root');\n";
		macro += FinalMacro("hess"+sigma+"_module");
		macro += "rename('Hessian module "+sigma+"');\n";
		
		macro += "selectWindow('Hessian b^2');\n";
		macro += "run('Multiply...', 'value=4');\n";
		macro += "rename('Hessian 4b^2');\n";
		macro += "imageCalculator('Subtract create', 'Hessian a','Hessian d');\n";
		macro += "rename('Hessian (a-d)');\n";
		macro += "imageCalculator('Multiply create', 'Hessian (a-d)','Hessian (a-d)');\n";
		macro += "rename('Hessian (a-d)^2');\n";
		macro += "imageCalculator('Add create', 'Hessian 4b^2','Hessian (a-d)^2');\n";
		macro += "rename('Hessian 4b^2 + (a-d)^2');\n";
		
		macro += "selectWindow('Hessian 4b^2');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian 4b^2 + (a-d)^2');\n";
		macro += "run('Duplicate...', 'title=hessevdif duplicate');\n";
		macro += "selectWindow('hessevdif');\n";
		macro += "run('Divide...', 'value=2');\n";
		macro += "run('Square Root');\n";
		macro += "imageCalculator('Add create', 'Hessian (a+d)/2','hessevdif');\n";
		macro += FinalMacro("hess"+sigma+"_eig_first");
		macro += "rename('Hessian First Eigenvalue "+sigma+"');\n";
		macro += "imageCalculator('Subtract create', 'Hessian (a+d)/2','hessevdif');\n";
		macro += FinalMacro("hess"+sigma+"_eig_second");
		macro += "rename('Hessian Second Eigenvalue "+sigma+"');\n";
		macro += "selectWindow('hessevdif');\n";
		macro += "close();\n";
		
		macro += "selectWindow('Hessian 4b^2 + (a-d)^2');\n";
		macro += "run('Duplicate...', 'title=orientation duplicate');\n";
		macro += "run('Macro...', 'code=v=0.5*acos(v)');\n";
		macro += FinalMacro("hess"+sigma+"_orientation");
		macro += "rename('Hessian Orientation "+sigma+"');\n";
		
		macro += "selectWindow('Hessian 4b^2 + (a-d)^2');\n";
		macro += "rename('Hessian Square of Gamma-normalized square eigenvalue difference??? "+sigma+"');\n";
		macro += "imageCalculator('Multiply create', 'Hessian Square of Gamma-normalized square eigenvalue difference??? "+sigma+"','Hessian (a-d)^2');\n";
		macro += "rename('Hessian Gamma-normalized square eigenvalue difference??? "+sigma+"');\n";
		macro += FinalMacro("hess"+sigma+"_gnsqeigdif");
		macro += "selectWindow('Hessian Square of Gamma-normalized square eigenvalue difference??? "+sigma+"');\n";
		macro += FinalMacro("hess"+sigma+"_gnsqeigdif_sq");
		
		
		macro += "selectWindow('Hessian a^2');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian d^2');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian bc');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian ad');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian (a-d)');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian (a-d)^2');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian (a+d)/2');\n";
		macro += "close();\n";
		
		macro += "selectWindow('Hessian a');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian b');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian c');\n";
		macro += "close();\n";
		macro += "selectWindow('Hessian d');\n";
		macro += "close();\n";
		
		
		
		return macro;
	}
	
	/*** AnisotropicDiffusionMacro ***
     * creates a string of an ImageJ macro that executes the Anisotropic Diffusion filter on the image 'original'
     * 
     * @param  number              int
     * @param  smoothings          int
     * @param  a1                  double
     * @param  a2                  double
     * @param  dt                  int
     * @param  edge                int
     * @return                     String
     ***/
	private String AnisotropicDiffusionMacro(int number, int smoothings, double a1, double a2, int dt, int edge){
		String macro = "/***ANISOTROPIC DIFFUSION "+smoothings+", "+a1+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=temp duplicate');\n";
		macro += "run('8-bit');";
		macro += "run('Anisotropic Diffusion 2D', 'number="+number+" smoothings="+smoothings+" a1="+a1+" a2="+a2+" dt="+dt+" edge="+edge+"');";
				
		macro += FinalMacro("anisodiffusion_"+smoothings+"_"+a1);
		macro += "rename('Anisotropic Diffusion "+smoothings+", "+a1+"');\n";
		
		macro += "selectWindow('temp');\n";
		macro += "close();\n";
		
		return macro;
	}
	
	/*** Bilateral ***
     * creates a string of an ImageJ macro that executes the Bilateral filter on the image 'original'
     * 
     * @param  spatial             int
     * @param  range               int
     * @return                     String
     ***/
	private String Bilateral(int spatial, int range){
		String macro = "/***BILATERAL FILTER "+spatial+", "+range+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=temp duplicate');\n";
		macro += "run('8-bit');";
		macro += "run('Bilateral Filter', 'spatial="+spatial+" range="+range+"');";
				
		macro += FinalMacro("bilat"+spatial+"-"+range);
		macro += "rename('Bilateral Filter "+spatial+", "+range+"');\n";
		
		macro += "selectWindow('temp');\n";
		macro += "close();\n";
		
		return macro;
	}
	
	/*** Derivatives ***
     * creates a string of an ImageJ macro that executes the FeatureJ Derivatives filter on the image 'original'
     * 
     * @param  dx                  int
     * @param  dy                  int
     * @param  dz                  int
     * @param  sigma               int
     * @return                     String
     ***/
	private String Derivatives(int dx, int dy, int dz, int sigma){
		String macro = "/***DERIVATIVES dx"+dx+", dy"+dy+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('FeatureJ Derivatives', 'x-order="+dx+" y-order="+dy+" z-order="+dz+" smoothing="+sigma+"');\n";
		if (dx==0){
			macro += FinalMacro("diff"+sigma+"_y"+dy);
		}else if (dy==0){
			macro += FinalMacro("diff"+sigma+"_x"+dx);
		}else if (dx==1 && dy ==1){
			macro += FinalMacro("diff"+sigma+"_xy");
		}else if (dx==1){
			macro += FinalMacro("diff"+sigma+"_xy"+dy);
		}else if (dy==1){
			macro += FinalMacro("diff"+sigma+"_x"+dx+"y");
		}else{
			macro += FinalMacro("diff"+sigma+"_x"+dx+"y"+dy);
		}
		
		macro += "rename('Derivative dx"+dx+", dy"+dy+"');\n";
		return macro;
	}
	
	/*** Structure ***
     * creates a string of an ImageJ macro that executes the FeatureJ Structure filter on the image 'original'
     * 
     * @param  smoothing           int
     * @param  integration         int
     * @return                     String
     ***/
	private String Structure(int smoothing, int integration){
		String macro = "/***STRUCTURE "+smoothing+", "+integration+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('FeatureJ Structure', 'largest smallest smoothing="+smoothing+" integration="+integration+"');\n";
		
		macro += "selectWindow('original smallest structure eigenvalues');\n";
		macro += FinalMacro("struct_eig_smallest_i"+integration+"_s"+smoothing);
		macro += "rename('Structure eigenvalues "+smoothing+", "+integration+" (smallest)');\n";
		
		macro += "selectWindow('original largest structure eigenvalues');\n";
		macro += FinalMacro("struct_eig_largest_i"+integration+"_s"+smoothing);
		macro += "rename('Structure eigenvalues "+smoothing+", "+integration+" (largest)');\n";
		
		return macro;
	}
	
	/*** MembraneMacro ***
     * creates a string of an ImageJ macro that executes the Membrane Projections of the image 'original'
     * 
     * @param  membrane            int
     * @param  patchsize           int
     * @return                     String
     ***/
	private String MembraneMacro(int membrane, int patchsize){
		String macro = "/***MEMBRANE PROJECTION "+membrane+","+patchsize+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Membrane Projections', 'membrane="+membrane+" patch="+patchsize+"');\n";
		
		macro += "selectWindow('membrane stack');\n";
		macro += "run('Duplicate...', 'title=mem0');\n";
		macro += "selectWindow('mem0');\n";
		macro += FinalMacro("membrane_"+membrane+"_"+patchsize+"_av");
		macro += "rename('Membrane Projection average thickness="+membrane+" kernelsize="+patchsize+"');\n";
		
		macro += "selectWindow('membrane stack');\n";
		macro += "run('Next Slice [>]');\n";
		macro += "run('Duplicate...', 'title=mem1');\n";
		macro += "selectWindow('mem1');\n";
		macro += FinalMacro("membrane_"+membrane+"_"+patchsize+"_max");
		macro += "rename('Membrane Projection maximum thickness="+membrane+" kernelsize="+patchsize+"');\n";
		
		macro += "selectWindow('membrane stack');\n";
		macro += "run('Next Slice [>]');\n";
		macro += "run('Duplicate...', 'title=mem2');\n";
		macro += "selectWindow('mem2');\n";
		macro += FinalMacro("membrane_"+membrane+"_"+patchsize+"_min");
		macro += "rename('Membrane Projection minimum thickness="+membrane+" kernelsize="+patchsize+"');\n";
		
		macro += "selectWindow('membrane stack');\n";
		macro += "run('Next Slice [>]');\n";
		macro += "run('Duplicate...', 'title=mem3');\n";
		macro += "selectWindow('mem3');\n";
		macro += FinalMacro("membrane_"+membrane+"_"+patchsize+"_sum");
		macro += "rename('Membrane Projection sum thickness="+membrane+" kernelsize="+patchsize+"');\n";
		
		macro += "selectWindow('membrane stack');\n";
		macro += "run('Next Slice [>]');\n";
		macro += "run('Duplicate...', 'title=mem4');\n";
		macro += "selectWindow('mem4');\n";
		macro += FinalMacro("membrane_"+membrane+"_"+patchsize+"_sd");
		macro += "rename('Membrane Projection standard deviation thickness="+membrane+" kernelsize="+patchsize+"');\n";
		
		macro += "selectWindow('membrane stack');\n";
		macro += "run('Next Slice [>]');\n";
		macro += "run('Duplicate...', 'title=mem5');\n";
		macro += "selectWindow('mem5');\n";
		macro += FinalMacro("membrane_"+membrane+"_"+patchsize+"_med");
		macro += "rename('Membrane Projection median thickness="+membrane+" kernelsize="+patchsize+"');\n";
		
		macro += "selectWindow('membrane stack');\n";
		macro += "close();\n";
		
		return macro;
	}
	
	/*** NeighborMacro ***
     * creates a string of an ImageJ macro that executes the Neighbors filter on the image 'original'
     * 
     * @param  minsig              int
     * @param  maxsig              int
     * @return                     String
     ***/
	private String NeighborMacro(int minsig, int maxsig){
		String macro = "/***NEIGHBORS "+minsig+" to "+maxsig+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Neighbors', 'minsigma="+minsig+" maxsigma="+maxsig+"');\n";
		
		for (int s=minsig, i=1; s<=maxsig; s*=2){
			for (int k=0; k<8; k++, i++){
				macro += "selectWindow('Neighbors');\n";
				macro += "setSlice("+i+");\n";
				macro += "run('Duplicate...', 'title=n"+s+"_"+k+"');\n";
				macro += "selectWindow('n"+s+"_"+k+"');\n";
				macro += FinalMacro("neighbors_"+s+"_"+k);
				macro += "rename('Neighbors sigma="+s+" shift="+k+"');\n";
			}
		}
		
		macro += "selectWindow('Neighbors');\n";
		macro += "close();\n";
		
		return macro;
	}
	
	/*** MinimumMacro ***
     * creates a string of an ImageJ macro that executes the Minimum filter on the image 'original'
     * 
     * @param  radius              int
     * @return                     String
     ***/
	private String MinimumMacro(int radius){
		String macro = "/***MINIMUM "+radius+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Minimum duplicate');\n";
		macro += "selectWindow('Minimum');\n";
		macro += "run('Minimum...', 'radius="+radius+" stack');\n";
		
		macro += FinalMacro("min"+radius);
		macro += "rename('Minimum "+radius+"');\n";
		
		return macro;
	}
	
	/*** MaximumMacro ***
     * creates a string of an ImageJ macro that executes the Maximum filter on the image 'original'
     * 
     * @param  radius              int
     * @return                     String
     ***/
	private String MaximumMacro(int radius){
		String macro = "/***MAXIMUM "+radius+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Maximum duplicate');\n";
		macro += "selectWindow('Maximum');\n";
		macro += "run('Maximum...', 'radius="+radius+" stack');\n";
		
		macro += FinalMacro("max"+radius);
		macro += "rename('Maximum "+radius+"');\n";
		
		return macro;
	}
	
	/*** VarianceMacro ***
     * creates a string of an ImageJ macro that executes the Variance filter on the image 'original'
     * 
     * @param  radius              int
     * @return                     String
     ***/
	private String VarianceMacro(int radius){
		String macro = "/***VARIANCE "+radius+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Variance duplicate');\n";
		macro += "selectWindow('Variance');\n";
		macro += "run('Variance...', 'radius="+radius+" stack');\n";
		
		macro += FinalMacro("variance"+radius);
		macro += "rename('Variance "+radius+"');\n";
		
		return macro;
	}
	
	/*** MeanMacro ***
     * creates a string of an ImageJ macro that executes the Mean filter on the image 'original'
     * 
     * @param  radius              int
     * @return                     String
     ***/
	private String MeanMacro(int radius){
		String macro = "/***MEAN "+radius+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Mean duplicate');\n";
		macro += "selectWindow('Mean');\n";
		macro += "run('Mean...', 'radius="+radius+" stack');\n";
		
		macro += FinalMacro("mean"+radius);
		macro += "rename('Mean "+radius+"');\n";
		
		return macro;
	}
	
	/*** MedianMacro ***
     * creates a string of an ImageJ macro that executes the Median filter on the image 'original'
     * 
     * @param  radius              int
     * @return                     String
     ***/
	private String MedianMacro(int radius){
		String macro = "/***MEDIAN "+radius+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('Duplicate...', 'title=Median duplicate');\n";
		macro += "selectWindow('Median');\n";
		macro += "run('Median...', 'radius="+radius+" stack');\n";
		
		macro += FinalMacro("median"+radius);
		macro += "rename('Median "+radius+"');\n";
		
		return macro;
	}
	
	/*** FinalMacro ***
     * creates a string for a macro that applies final filters and saves the image as requested by the user
     * 
     * @param  filename            String
     * @return                     String
     ***/
	private String FinalMacro(String filename){
		String macro = "";
		if (doShades){
			macro += "run('6 shades');\n";
		}
		if (doEnhance){
			macro += "run('Enhance Contrast', 'saturated=0.35');\n";
		}
		if (doSave){
			macro += "saveAs('PNG', outdir+'"+filename+".png');\n";
			//macro += "wait(2000);\n";
		}
		return macro;
	}

}
