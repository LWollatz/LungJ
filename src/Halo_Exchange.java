
	import java.io.IOException;
	import java.util.Properties;

	import lj.LJPrefs;
	import ij.IJ;
	import ij.ImagePlus;
	import ij.gui.GenericDialog;
	import ij.plugin.PlugIn;
	import ij.process.ImageProcessor;


	public class Halo_Exchange implements PlugIn{
			/** plugin's name */
			public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
			/** plugin's current version */
			public static final String PLUGIN_VERSION = LJPrefs.VERSION;
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
			private static int errCount = 0;
			
			
			private static int moveX = 1; //current movement x-direction (+1 or -1)
			private static int moveY = 1; //current movement y-direction (+1 or -1)
			private static int moveZ = 1; //current movement z-direction (+1) -> once -1, snake completed
			
			private static int tempX1 = 0;
			private static int tempX2 = 0;
			private static int tempY1 = 0;
			private static int tempY2 = 0;
			private static int tempZ1 = 0;
			private static int tempZ2 = 0;
			
			
			private static int x11 = 0;		//current position of block 111 and block 211
			private static int y11 = 0;
			private static int z11 = 0;
			
			private static int x12 = stepX;
			private static int y12 = 0;
			private static int z12 = 0;
			
			private static int x21 = 0;
			private static int y21 = stepY;
			private static int z21 = 0;
			
			private static int x22 = stepX;
			private static int y22 = stepY;
			private static int z22 = 0;
			
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
			
			
			
			public void run(String command){
				
				
				GenericDialog gd = new GenericDialog(command+" Run macro on 3D blocks");
				gd.addStringField("Input directory", BC_inDirectory, 100);
				gd.addStringField("Output directory", BC_outDirectory, 100);
				gd.showDialog();
				if (gd.wasCanceled()){
		        	return;
		        }
				BC_inDirectory = gd.getNextString();
				LJPrefs.LJ_inpDirectory = BC_inDirectory;
				BC_outDirectory = gd.getNextString();
				LJPrefs.LJ_outDirectory = BC_outDirectory;
				
				LJPrefs.savePreferences(); //save preferences for after Fiji restart.
				
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
				
				//ImagePlus imgin = null;
				ImagePlus imgout = null;
				float globMax = -Float.MAX_VALUE;
				float globMin = Float.MAX_VALUE;
				errCount = 0;
				
				/**initialise coordinates for first load**/
				x11 = 0;
				y11 = 0;
				z11 = 0;
				
				x12 = stepX;
				y12 = 0;
				z12 = 0;
				
				x21 = 0;
				y21 = stepY;
				z21 = 0;
				
				x22 = stepX;
				y22 = stepY;
				z22 = 0;
				
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
				
				//TODO: exchange halos
				
				//TODO: move to next 2x2
				
				
				
				
				
				
				doexchangetype000();
				
				while(moveZ == 1){
					if(moveX == 1){
						if (xs1+stepX<maxX && xs2+stepX<maxX){
							if (configuration[2] == 0){
								/**change 000 to 001**/
								move(2);
								/**exchange 001 (inverse x)**/
								doexchangetype001();
							}else if (configuration[2] == 1){
								/**change 001 to 000**/
								move(2);
								/**exchange 000**/
								doexchangetype000();
							}
						}else{
							//need x-sign inverse and y shift!
							moveX = -1;
							if (ys1+stepY<maxY && ys2+stepY<maxY){
								if (configuration[2] == 0){
									/**change 000 to 010**/
									move(1);
									/**exchange 001 (inverse x)**/
									doexchangetype010();
								}else if (configuration[2] == 1){
									/**change 001 to 011**/
									move(1);
									/**exchange 000**/
									doexchangetype011();
								}
							}else{
								//need y-sign inverse and z shift!
								/////////////////////////////////////////TODO:!!!!!!!!!!
								moveY = -1;
								if (zs1+stepZ<maxZ && zs2+2*stepZ<maxZ){
									if (configuration[1] == 0 && configuration[2] == 0){
										/**change 000 to 100**/
										move(0);
										/**exchange 100 (inverse z)**/
										doexchangetype100();
									}else if (configuration[1] == 0 && configuration[2] == 1){
										/**change 001 to 101**/
										move(0);
										/**exchange 101**/
										doexchangetype101();
									}else if (configuration[1] == 1 && configuration[2] == 0){
										/**change 010 to 110**/
										move(0);
										/**exchange 110**/
										doexchangetype110();
									}else if (configuration[1] == 1 && configuration[2] == 1){
										/**change 011 to 111**/
										move(0);
										/**exchange 111**/
										doexchangetype111();
									}
								}else{
									//need z-sign inverse and end!
									moveZ = -1;
								}
							}
						}
					}else{
						if (xs1-stepX>=0 && xs2-stepX>=0){
							if (configuration[2] == 0){
								/**change 010 to 011**/
								move(2);
								/**exchange 001 (inverse x)**/
								doexchangetype011();
							}
							if (configuration[2] == 1){
								/**change 011 to 010**/
								move(2);
								/**exchange 000**/
								doexchangetype010();
							}
						}else{
							//need x-sign inverse and y shift!
							moveX = 1;
							if (ys1+stepY<maxY && ys2+stepY<maxY){
								if (configuration[2] == 0){
									/**change 010 to 000**/
									move(1);
									/**exchange 000**/
									doexchangetype000();
								}
								if (configuration[2] == 1){
									/**change 011 to 001**/
									move(1);
									/**exchange 001**/
									doexchangetype001();
								}
							}else{
								//need y-sign inverse and z shift!
								moveY = -1;
								moveZ = -1;
							}
						}
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
			
			//TODO: snake moves can be united by axis and direction rather than configuration2configuration
			/**snake moves**/
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
			
			
			//positive x movement from normal to inverted
			private void move000to001(){
				
				
				IJ.saveAsTiff(img111,fileout111);
				//IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img121,fileout121);
				//IJ.saveAsTiff(img122,fileout122);
				IJ.saveAsTiff(img211,fileout211);
				//IJ.saveAsTiff(img212,fileout212);
				IJ.saveAsTiff(img221,fileout221);
				//IJ.saveAsTiff(img222,fileout222);
				
				
				x11 += 2*stepX;
				x21 += 2*stepX;
				
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11,y11,x11);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11,y11,x11);
				img111 = IJ.openImage(filein111);
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21,y21,x21);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21,y21,x21);
				img121 = IJ.openImage(filein121);
				filein211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11+stepZ,y11,x11);
				fileout211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11+stepZ,y11,x11);
				img211 = IJ.openImage(filein211);
				filein221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21+stepZ,y21,x21);
				fileout221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21+stepZ,y21,x21);
				img221 = IJ.openImage(filein221);
				
				configuration[2] = 1;
			}
			
			//positive x movement from inverted to normal
			private void move001to000(){
				
				
				//IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				//IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				//IJ.saveAsTiff(img211,fileout211);
				IJ.saveAsTiff(img212,fileout212);
				//IJ.saveAsTiff(img221,fileout221);
				IJ.saveAsTiff(img222,fileout222);
				
				
				/**move in x direction**/
				x12 += 2*stepX;
				x22 += 2*stepX;
				
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12,y12,x12);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12,y12,x12);
				img112 = IJ.openImage(filein112);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22,y22,x22);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22,y22,x22);
				img122 = IJ.openImage(filein122);
				filein212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12+stepZ,y12,x12);
				fileout212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12+stepZ,y12,x12);
				img212 = IJ.openImage(filein212);
				filein222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22+stepZ,y22,x22);
				fileout222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22+stepZ,y22,x22);
				img222 = IJ.openImage(filein222);
				
				configuration[2] = 0;
			}
			
			//positive y movement from normal to inverted
			private void move000to010(){
				
				
				IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				//IJ.saveAsTiff(img121,fileout121);
				//IJ.saveAsTiff(img122,fileout122);
				IJ.saveAsTiff(img211,fileout211);
				IJ.saveAsTiff(img212,fileout212);
				//IJ.saveAsTiff(img221,fileout221);
				//IJ.saveAsTiff(img222,fileout222);
				
				y11 += 2*stepY;
				y12 += 2*stepY;
				
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11,y11,x11);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11,y11,x11);
				img111 = IJ.openImage(filein111);
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12,y12,x12);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12,y12,x12);
				img112 = IJ.openImage(filein112);
				filein211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11+stepZ,y11,x11);
				fileout211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11+stepZ,y11,x11);
				img211 = IJ.openImage(filein211);
				filein212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12+stepZ,y12,x12);
				fileout212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12+stepZ,y12,x12);
				img212 = IJ.openImage(filein212);
				
				configuration[1] = 1;
			}
			
			//positive y movement from normal to inverted
			private void move001to011(){
				
				
				IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				//IJ.saveAsTiff(img121,fileout121);
				//IJ.saveAsTiff(img122,fileout122);
				IJ.saveAsTiff(img211,fileout211);
				IJ.saveAsTiff(img212,fileout212);
				//IJ.saveAsTiff(img221,fileout221);
				//IJ.saveAsTiff(img222,fileout222);
				
				
				/**move in y direction**/
				y11 += 2*stepY;
				y12 += 2*stepY;
				
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11,y11,x11);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11,y11,x11);
				img111 = IJ.openImage(filein111);
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12,y12,x12);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12,y12,x12);
				img112 = IJ.openImage(filein112);
				filein211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11+stepZ,y11,x11);
				fileout211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11+stepZ,y11,x11);
				img211 = IJ.openImage(filein211);
				filein212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12+stepZ,y12,x12);
				fileout212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12+stepZ,y12,x12);
				img212 = IJ.openImage(filein212);
				
				configuration[2] = 0;
			}
			
			//negative x movement from inverted to normal
			private void move011to010(){
				
				
				IJ.saveAsTiff(img111,fileout111);
				//IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img121,fileout121);
				//IJ.saveAsTiff(img122,fileout122);
				IJ.saveAsTiff(img211,fileout211);
				//IJ.saveAsTiff(img212,fileout212);
				IJ.saveAsTiff(img221,fileout221);
				//IJ.saveAsTiff(img222,fileout222);
				
				x11 -= 2*stepX;
				x21 -= 2*stepX;
				
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11,y11,x11);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11,y11,x11);
				img111 = IJ.openImage(filein111);
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21,y21,x21);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21,y21,x21);
				img121 = IJ.openImage(filein121);
				filein211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11+stepZ,y11,x11);
				fileout211 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11+stepZ,y11,x11);
				img211 = IJ.openImage(filein211);
				filein221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21+stepZ,y21,x21);
				fileout221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21+stepZ,y21,x21);
				img221 = IJ.openImage(filein221);
				
				configuration[2] = 0;
			}
			
			//negative x movement from normal to inverted
			private void move010to011(){
				//IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				//IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				//IJ.saveAsTiff(img211,fileout211);
				IJ.saveAsTiff(img212,fileout212);
				//IJ.saveAsTiff(img221,fileout221);
				IJ.saveAsTiff(img222,fileout222);
				
				
				/**move in x direction**/
				x12 -= 2*stepX;
				x22 -= 2*stepX;
				
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12,y12,x12);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12,y12,x12);
				img112 = IJ.openImage(filein112);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22,y22,x22);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22,y22,x22);
				img122 = IJ.openImage(filein122);
				filein212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12+stepZ,y12,x12);
				fileout212 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12+stepZ,y12,x12);
				img212 = IJ.openImage(filein212);
				filein222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22+stepZ,y22,x22);
				fileout222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22+stepZ,y22,x22);
				img222 = IJ.openImage(filein222);
				
				configuration[2] = 1;
			}
			
			//positive y movement from inverted to normal
			private void move010to000(){
				
				//IJ.saveAsTiff(img111,fileout111);
				//IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				//IJ.saveAsTiff(img211,fileout211);
				//IJ.saveAsTiff(img212,fileout212);
				IJ.saveAsTiff(img221,fileout221);
				IJ.saveAsTiff(img222,fileout222);
				
				y21 += 2*stepY;
				y22 += 2*stepY;
				
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21,y21,x21);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21,y21,x21);
				img121 = IJ.openImage(filein121);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22,y22,x22);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22,y22,x22);
				img122 = IJ.openImage(filein122);
				filein221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21+stepZ,y21,x21);
				fileout221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21+stepZ,y21,x21);
				img221 = IJ.openImage(filein221);
				filein222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22+stepZ,y22,x22);
				fileout222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22+stepZ,y22,x22);
				img222 = IJ.openImage(filein222);
				
				configuration[1] = 0;
			}
			
			//positive y movement from inverted to normal
			private void move011to001(){
				
				//IJ.saveAsTiff(img111,fileout111);
				//IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				//IJ.saveAsTiff(img211,fileout211);
				//IJ.saveAsTiff(img212,fileout212);
				IJ.saveAsTiff(img221,fileout221);
				IJ.saveAsTiff(img222,fileout222);
				
				y11 += 2*stepY;
				y12 += 2*stepY;
				
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21,y21,x21);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21,y21,x21);
				img121 = IJ.openImage(filein121);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22,y22,x22);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22,y22,x22);
				img122 = IJ.openImage(filein122);
				filein221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21+stepZ,y21,x21);
				fileout221 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21+stepZ,y21,x21);
				img221 = IJ.openImage(filein221);
				filein222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22+stepZ,y22,x22);
				fileout222 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22+stepZ,y22,x22);
				img222 = IJ.openImage(filein222);
				
				configuration[1] = 0;
			}
			
			/**dummy moves**/
			private void move000to100(){
				
				IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				//IJ.saveAsTiff(img211,fileout211);
				//IJ.saveAsTiff(img212,fileout212);
				//IJ.saveAsTiff(img221,fileout221);
				//IJ.saveAsTiff(img222,fileout222);
				
				z11 += stepZ;
				z12 += stepZ;
				z21 += stepZ;
				z22 += stepZ;
				
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11+stepZ,y11,x11);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11+stepZ,y11,x11);
				img111 = IJ.openImage(filein111);
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12+stepZ,y12,x12);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12+stepZ,y12,x12);
				img112 = IJ.openImage(filein112);
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21+stepZ,y21,x21);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21+stepZ,y21,x21);
				img121 = IJ.openImage(filein121);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22+stepZ,y22,x22);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22+stepZ,y22,x22);
				img122 = IJ.openImage(filein122);
				
				configuration[0] = 1;
			}
			private void move001to101(){
				IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				//IJ.saveAsTiff(img211,fileout211);
				//IJ.saveAsTiff(img212,fileout212);
				//IJ.saveAsTiff(img221,fileout221);
				//IJ.saveAsTiff(img222,fileout222);
				
				z11 += stepZ;
				z12 += stepZ;
				z21 += stepZ;
				z22 += stepZ;
				
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11+stepZ,y11,x11);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11+stepZ,y11,x11);
				img111 = IJ.openImage(filein111);
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12+stepZ,y12,x12);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12+stepZ,y12,x12);
				img112 = IJ.openImage(filein112);
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21+stepZ,y21,x21);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21+stepZ,y21,x21);
				img121 = IJ.openImage(filein121);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22+stepZ,y22,x22);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22+stepZ,y22,x22);
				img122 = IJ.openImage(filein122);
				
				configuration[0] = 1;
			}
			private void move010to110(){
				IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				//IJ.saveAsTiff(img211,fileout211);
				//IJ.saveAsTiff(img212,fileout212);
				//IJ.saveAsTiff(img221,fileout221);
				//IJ.saveAsTiff(img222,fileout222);
				
				z11 += stepZ;
				z12 += stepZ;
				z21 += stepZ;
				z22 += stepZ;
				
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11+stepZ,y11,x11);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11+stepZ,y11,x11);
				img111 = IJ.openImage(filein111);
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12+stepZ,y12,x12);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12+stepZ,y12,x12);
				img112 = IJ.openImage(filein112);
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21+stepZ,y21,x21);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21+stepZ,y21,x21);
				img121 = IJ.openImage(filein121);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22+stepZ,y22,x22);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22+stepZ,y22,x22);
				img122 = IJ.openImage(filein122);
				
				configuration[0] = 1;
			}
			private void move011to111(){
				IJ.saveAsTiff(img111,fileout111);
				IJ.saveAsTiff(img112,fileout112);
				IJ.saveAsTiff(img121,fileout121);
				IJ.saveAsTiff(img122,fileout122);
				//IJ.saveAsTiff(img211,fileout211);
				//IJ.saveAsTiff(img212,fileout212);
				//IJ.saveAsTiff(img221,fileout221);
				//IJ.saveAsTiff(img222,fileout222);
				
				z11 += stepZ;
				z12 += stepZ;
				z21 += stepZ;
				z22 += stepZ;
				
				filein111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z11+stepZ,y11,x11);
				fileout111 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z11+stepZ,y11,x11);
				img111 = IJ.openImage(filein111);
				filein112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z12+stepZ,y12,x12);
				fileout112 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z12+stepZ,y12,x12);
				img112 = IJ.openImage(filein112);
				filein121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z21+stepZ,y21,x21);
				fileout121 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z21+stepZ,y21,x21);
				img121 = IJ.openImage(filein121);
				filein122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_inDirectory,z22+stepZ,y22,x22);
				fileout122 = String.format("%1$s\\%2$04d_%3$04d_%4$04d.tif",BC_outDirectory,z22+stepZ,y22,x22);
				img122 = IJ.openImage(filein122);
				
				configuration[0] = 1;
			}
			
			/**halo exchanges**/
			private void doexchange(){
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
			
			private void doexchangetype000(){
				/*
				 * current arrangement:
				 * i111 i112
				 * i121 i122
				 * 
				 * i211 i212
				 * i221 i222
				 */
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
					ImageProcessor proc11 = img111.getStack().getProcessor(z);
					ImageProcessor proc12 = img112.getStack().getProcessor(z);
					for (int x=tempX1, xao = 0, xbo = haloX, xb=tempX2; x<tempX2; x++, xb++, xao++, xbo++) {
						for (int y=tempY1; y<tempY2; y++) {
							proc12.set(xao, y, proc11.get(x, y));
							proc11.set(xb, y, proc12.get(xbo, y));
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
							proc1.set(x, y, proc2.get(x, y));
						}
					}
				}
				for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
					ImageProcessor proc1 = img111.getStack().getProcessor(zb);
					ImageProcessor proc2 = img211.getStack().getProcessor(zbo);
					for (int x=tempX1; x<tempX2; x++) {
						for (int y=tempY1; y<tempY2; y++) {
							proc2.set(x, y, proc1.get(x, y));
							proc1.set(x, y, proc2.get(x, y));
						}
					}
				}
			}
			
			private void doexchangetype001(){
				/*
				 * current arrangement:
				 * i112 i111
				 * i122 i121
				 * 
				 * i212 i211
				 * i222 i221
				 */
				
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
							proc1.set(x, y, proc2.get(x, y));
						}
					}
				}
				for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
					ImageProcessor proc1 = img112.getStack().getProcessor(zb);
					ImageProcessor proc2 = img212.getStack().getProcessor(zbo);
					for (int x=tempX1; x<tempX2; x++) {
						for (int y=tempY1; y<tempY2; y++) {
							proc2.set(x, y, proc1.get(x, y));
							proc1.set(x, y, proc2.get(x, y));
						}
					}
				}
			}
			
			private void doexchangetype010(){
				/*
				 * current arrangement:
				 * i121 i122
				 * i111 i112
				 * 
				 * i221 i222
				 * i211 i212
				 */
				
				IJ.log("---010--------------");
				IJ.log(filein121 + "|" + filein122);
				IJ.log(filein111 + "|" + filein112);
				IJ.log("--------------------");
				
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
							proc1.set(x, y, proc2.get(x, y));
						}
					}
				}
				for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
					ImageProcessor proc1 = img121.getStack().getProcessor(zb);
					ImageProcessor proc2 = img221.getStack().getProcessor(zbo);
					for (int x=tempX1; x<tempX2; x++) {
						for (int y=tempY1; y<tempY2; y++) {
							proc2.set(x, y, proc1.get(x, y));
							proc1.set(x, y, proc2.get(x, y));
						}
					}
				}
			}
			
			private void doexchangetype011(){
				/*
				 * current arrangement:
				 * i122 i121
				 * i112 i111
				 * 
				 * i222 i221
				 * i212 i211
				 */
				
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
							proc1.set(x, y, proc2.get(x, y));
						}
					}
				}
				for (int zb=tempZ2+1, zbo = haloZ+1; zb<=tempZ2+haloZ; zb++, zbo++) {
					ImageProcessor proc1 = img122.getStack().getProcessor(zb);
					ImageProcessor proc2 = img222.getStack().getProcessor(zbo);
					for (int x=tempX1; x<tempX2; x++) {
						for (int y=tempY1; y<tempY2; y++) {
							proc2.set(x, y, proc1.get(x, y));
							proc1.set(x, y, proc2.get(x, y));
						}
					}
				}
			}
	
			
			/**dummy exchanges**/
			private void doexchangetype100(){}
			private void doexchangetype101(){}
			private void doexchangetype110(){}
			private void doexchangetype111(){}
	
	}


