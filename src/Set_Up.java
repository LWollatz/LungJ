//import javax.swing.JFrame;
//import javax.swing.JPanel;
//import javax.swing.JLabel;
//import javax.swing.JColorChooser;
//import javax.swing.JFileChooser;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.event.*;

import java.awt.Color;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.PlugIn;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;
import ij.WindowManager;


public class Set_Up  implements PlugIn {
	public static final String PLUGIN_NAME = "LungJ";
	/** plugin's current version */
	public static final String PLUGIN_VERSION = "v" + LJPrefs.VERSION;
	public void run(String arg) {
		GUI.main(arg);
		/*
		//public String LJ_srcFilename = getTitle;
		JFrame SetUpframe = new JFrame("LungJ Options");
		//SetUpframe.setSize(500,500);
		
		JPanel srcPanel = new JPanel();
		srcPanel.setBackground(Color.red);
		
		
		JButton btnSrcColor = new JButton("C");
		//btnSrcColor.addActionListener(SetUpframe);
		srcPanel.add(new JLabel("Source Directory: "+LJPrefs.LJ_srcDirectory));
		srcPanel.add(new JLabel("Source Filename: "+LJPrefs.LJ_srcFilename));
		srcPanel.add(btnSrcColor);
		
		SetUpframe.add(srcPanel);
		SetUpframe.pack();
		SetUpframe.setVisible(true);
		
		ImagePlus img = WindowManager.getCurrentImage();  // current image
		LJPrefs.LJ_srcFilename = img.getTitle();
		IJ.error("Hello world!");
		*/
	}
	
	public static Color getColor(String label, Color defaultCol){
		Color selColor = JColorChooser.showDialog(null, label, defaultCol);
		System.out.println(selColor);
		return selColor;
	}
	
	public static String getClassifier(){
		FileFilter filter = new FileNameExtensionFilter("Weka Classifier", "model");
		JFileChooser chooser = new JFileChooser(LJPrefs.LJ_clsDirectory);
		chooser.addChoosableFileFilter(filter);
		chooser.showDialog(null, "Select");
		//String cls = chooser.getSelectedFile().getAbsolutePath();
		String cls = chooser.getSelectedFile().getParent();
		LJPrefs.LJ_clsDirectory = cls;
		LJPrefs.loadClassifier();
		return cls;
	}
	
	
	
}


