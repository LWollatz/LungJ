import ij.IJ;
import ij.plugin.PlugIn;


public class Test_WEKA_Filter implements PlugIn{
	boolean doSave = true;
	boolean doShades = false;
	boolean doEnhance = true;
	
	int membranesize = 20;
	
	public void run(String arg) {
		//TODO: add GUI to request options
		
		String macro = "";
		
		macro += "var outdir = '\\\\\\\\soton.ac.uk\\\\ude\\\\PersonalFiles\\\\Users\\\\lw6g10\\\\mydocuments\\\\PhD\\\\LungJ\\\\wiki\\\\weka_lung_'; //name of directory and file-prefix to save results to\n";
		macro += "rename('original');\n\n";
		
		/**Gaussian**/
		macro += "\n/****GAUSSIAN BLUR****/\n";
		macro += GaussMacro(1);
		macro += GaussMacro(2);
	    macro += GaussMacro(4);
		macro += GaussMacro(8);
		
		/**Sobel**/
		macro += "\n/****SOBEL****/\n";
		macro += SobelMacro(1);
		macro += SobelMacro(2);
		macro += SobelMacro(4);
		macro += SobelMacro(8);
		
		/**Hessian**/
		macro += "\n/****HESSIAN****/\n";
		macro += HesseMacro(1);
		macro += HesseMacro(2);
		macro += HesseMacro(4);
		macro += HesseMacro(8);
		
		/**Gaussian Difference**/
		macro += "\n/****DIFFERENCE OF GAUSSIAN****/\n";
		macro += GaussDifMacro(2, 1);
		
		macro += GaussDifMacro(4, 2);
		macro += GaussDifMacro(4, 1);
		
		macro += GaussDifMacro(8, 1);
		macro += GaussDifMacro(8, 2);
		macro += GaussDifMacro(8, 4);
		
		//TODO: Membrane projections
		
		/**Variance**/
		macro += "\n/****VARIANCE****/\n";
		macro += VarianceMacro(1);
		macro += VarianceMacro(2);
		macro += VarianceMacro(4);
		macro += VarianceMacro(8);
		
		/**Mean**/
		macro += "\n/****MEAN****/\n";
		macro += MeanMacro(1);
		macro += MeanMacro(2);
		macro += MeanMacro(4);
		macro += MeanMacro(8);
		
		/**Minimum**/
		macro += "\n/****MINIMUM****/\n";
		macro += MinimumMacro(1);
		macro += MinimumMacro(2);
		macro += MinimumMacro(4);
		macro += MinimumMacro(8);
		
		/**Maximum**/
		macro += "\n/****MAXIMUM****/\n";
		macro += MaximumMacro(1);
		macro += MaximumMacro(2);
		macro += MaximumMacro(4);
		macro += MaximumMacro(8);
		
		/**Median**/
		macro += "\n/****MEDIAN****/\n";
		macro += MedianMacro(1);
		macro += MedianMacro(2);
		macro += MedianMacro(4);
		macro += MedianMacro(8);
		
		/**Anisotropic Diffusion**/
		macro += "\n/****ANISOTROPIC DIFFUSION****/\n";
		macro += AnisotropicDiffusionMacro(20, 1, 0.1, 0.9, 20, membranesize);
		macro += AnisotropicDiffusionMacro(20, 1, 0.35, 0.9, 20, membranesize);
		
		macro += AnisotropicDiffusionMacro(20, 2, 0.1, 0.9, 20, membranesize);
		macro += AnisotropicDiffusionMacro(20, 2, 0.35, 0.9, 20, membranesize);
		
		macro += AnisotropicDiffusionMacro(20, 4, 0.1, 0.9, 20, membranesize);
		macro += AnisotropicDiffusionMacro(20, 4, 0.35, 0.9, 20, membranesize);
		
		macro += AnisotropicDiffusionMacro(20, 8, 0.1, 0.9, 20, membranesize);
		macro += AnisotropicDiffusionMacro(20, 8, 0.35, 0.9, 20, membranesize);
		
		/**Bilateral**/
		macro += "\n/****BILATERAL****/\n";
		macro += Bilateral(5, 50);
		macro += Bilateral(5, 100);
		
		macro += Bilateral(10, 50);
		macro += Bilateral(10, 100);
		
		macro += Bilateral(20, 50);
		macro += Bilateral(20, 100);
		
		//TODO: Lipschitz
		
		//TODO: Kuwahara
		
		//TODO: Gabor
		
		/**Derivatives**/
		macro += "\n/****DERIVATIVES****/\n";
		macro += Derivatives(2, 0, 0, 1);
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
		macro += Derivatives(0, 5, 0, 1);
		
		//TODO: Laplacian
		
		/**Structure**/
		macro += "\n/****STRUCTURE****/\n";
		macro += Structure(1, 1);
		macro += Structure(2, 1);
		macro += Structure(4, 1);
		macro += Structure(8, 1);
		
		macro += Structure(1, 3);
		macro += Structure(2, 3);
		macro += Structure(4, 3);
		macro += Structure(8, 3);
		
		//TODO: Entropy
		
		//TODO: Neighbors
		
		IJ.log(macro);
		IJ.runMacro(macro);
	}
	
	
	
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
		macro += "selectWindow('Hessian (a-d)^2');\n";
		macro += "close();\n";
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
		macro += "rename('Hessian Square of Gamma-normalized eigenvalue difference??? "+sigma+"');\n";
		
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
	
	private String Derivatives(int dx, int dy, int dz, int sigma){
		String macro = "/***DERIVATIVES dx"+dx+", dy"+dy+"***/\n";
		macro += "selectWindow('original');\n";
		macro += "run('FeatureJ Derivatives', 'x-order="+dx+" y-order="+dy+" z-order="+dz+" smoothing="+sigma+"');\n";
		if (dx==0){
			macro += FinalMacro("diff_y"+dy);
		}else if (dy==0){
			macro += FinalMacro("diff_x"+dx);
		}else if (dx==1 && dy ==1){
			macro += FinalMacro("diff_xy");
		}else if (dx==1){
			macro += FinalMacro("diff_xy"+dy);
		}else if (dy==1){
			macro += FinalMacro("diff_x"+dx+"y");
		}else{
			macro += FinalMacro("diff_x"+dx+"y"+dy);
		}
		
		macro += "rename('Derivative dx"+dx+", dy"+dy+"');\n";
		return macro;
	}
	
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
