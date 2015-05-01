import javax.swing.JFrame;
import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;


public class Status_  implements PlugIn {
	public void run(String arg) {
		//String LJ_srcDirectory;
		//String LJ_srcFilename;
		//LJ_srcDirectory = "J:\\Biomedical Imaging Unit\\Research\\Research temporary\\3D IfLS Lung Project\\temp\\20150324_IfLS_Segmentation\\tests\\";
		//LJ_srcFilename = "250x250x250x16bit";
		
		IJ.error(LJPrefs.LJ_srcDirectory+LJPrefs.LJ_srcFilename);
		IJ.run("Trainable Weka Segmentation", "open=["+LJPrefs.LJ_srcDirectory+LJPrefs.LJ_srcFilename+"]");
		IJ.error("Hello world!");
	}
}