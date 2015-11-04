import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;
import javax.swing.border.MatteBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import lj.LJPrefs;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;


public class LungJ_Settings implements PlugIn, ActionListener{
	/** plugin's name */
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version */
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	
	private static String clsDir = LJPrefs.LJ_clsDirectory;
	private final Border ColPrevBorder = new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0));
	
	private static int Ncolours = 5;
	private static int rowInp = 0;
	private static int rowCol = 5;
	GridBagConstraints cCol = new GridBagConstraints();
	GenericDialog gd;
	JButton filebtn, clsbtn;
	JComboBox cmbClassifier1;
	JTextField inpdirtxt;
	
	

	
	public void run(String arg) {
		
		showDialog();
        
		

	}
	
	// Called by ImageJ after setup.
    public int showDialog() {
    	// The dialog
        GenericDialog gd = new GenericDialog("LungJ Settings...");
        GridBagConstraints c = new GridBagConstraints();
        Font gdFont = gd.getFont();
        
        cCol.fill = GridBagConstraints.CENTER;
    	cCol.insets = new Insets(0, 5, 5, 0);
    	cCol.gridy = rowCol;
        
        /**MAIN DIRECTORY**/
		JLabel inpdirlbl = new JLabel ("Input directory ", JLabel.RIGHT);
		inpdirlbl.setFont(gdFont);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = rowInp;
		gd.add(inpdirlbl,c);
		inpdirtxt = new JTextField(LJPrefs.LJ_inpDirectory,70);
		inpdirtxt.setFont(gdFont);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = Ncolours + 2;
		c.gridx = 1;
		c.gridy = rowInp;
		gd.add(inpdirtxt,c);
        filebtn = new JButton("...");
        filebtn.addActionListener(this);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = 1;
		c.gridx = Ncolours + 3;
		c.gridy = rowInp;
		gd.add(filebtn,c);
		gd.addMessage("");
		
		/**APPLY WEKA CLASSIFIER**/
        gd.addCheckbox("Use WEKA", LJPrefs.LJ_makeMap);
        LJPrefs.loadClassifier();
		String[] classifiers = new String[LJPrefs.LJ_classifiers.size()];
		classifiers = LJPrefs.LJ_classifiers.toArray(classifiers);
		File model = new File(LJPrefs.LJ_clsDirectory);
        cmbClassifier1 = new JComboBox();
        cmbClassifier1.setModel(new DefaultComboBoxModel(classifiers));
        cmbClassifier1.setSelectedItem(model.getName());
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridwidth = Ncolours + 2;
        c.gridx = 1;
        c.gridy = 2;
        gd.add(cmbClassifier1, c);
        clsbtn = new JButton("...");
        clsbtn.addActionListener(this);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.gridx = Ncolours + 3;
		c.gridy = 2;
		gd.add(clsbtn,c);
		gd.addMessage("");
        
		/**CREATE MASK**/
        gd.addCheckbox("Create Mask", LJPrefs.LJ_makeMask);
        gd.addNumericField("Threshold", LJPrefs.LJ_threshold*100, 1);
        
        
        /**COLORIZE_**/
        gd.addMessage("Colours for Colour by Segment");
        JPanel pnlcol0 = new JPanel();
		pnlcol0.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent arg0) {
				LJPrefs.LJ_Colors[0] = LJPrefs.getColor("Choose Color", LJPrefs.LJ_Colors[0]);
				pnlcol0.setBackground(LJPrefs.LJ_Colors[0]);
			}
		});
		pnlcol0.setBorder(ColPrevBorder);
		pnlcol0.setBackground(LJPrefs.LJ_Colors[0]);
		cCol.gridx = 2;
		gd.add(pnlcol0, cCol);
		JPanel pnlcol1 = new JPanel();
		pnlcol1.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent arg0) {
				LJPrefs.LJ_Colors[1] = LJPrefs.getColor("Choose Color", LJPrefs.LJ_Colors[1]);
				pnlcol1.setBackground(LJPrefs.LJ_Colors[1]);
			}
		});
		pnlcol1.setBorder(ColPrevBorder);
		pnlcol1.setBackground(LJPrefs.LJ_Colors[1]);
		cCol.gridx = 3;
		gd.add(pnlcol1, cCol);
		JPanel pnlcol2 = new JPanel();
		pnlcol2.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent arg0) {
				LJPrefs.LJ_Colors[2] = LJPrefs.getColor("Choose Color", LJPrefs.LJ_Colors[2]);
				pnlcol2.setBackground(LJPrefs.LJ_Colors[2]);
			}
		});
		pnlcol2.setBorder(ColPrevBorder);
		pnlcol2.setBackground(LJPrefs.LJ_Colors[2]);
		cCol.gridx = 4;
		gd.add(pnlcol2, cCol);
		JPanel pnlcol3 = new JPanel();
		pnlcol3.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent arg0) {
				LJPrefs.LJ_Colors[3] = LJPrefs.getColor("Choose Color", LJPrefs.LJ_Colors[3]);
				pnlcol3.setBackground(LJPrefs.LJ_Colors[3]);
			}
		});
		pnlcol3.setBorder(ColPrevBorder);
		pnlcol3.setBackground(LJPrefs.LJ_Colors[3]);
		cCol.gridx = 5;
		gd.add(pnlcol3, cCol);
		JPanel pnlcol4 = new JPanel();
		pnlcol4.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent arg0) {
				LJPrefs.LJ_Colors[4] = LJPrefs.getColor("Choose Color", LJPrefs.LJ_Colors[4]);
				pnlcol4.setBackground(LJPrefs.LJ_Colors[4]);
			}
		});
		pnlcol4.setBorder(ColPrevBorder);
		pnlcol4.setBackground(LJPrefs.LJ_Colors[4]);
		cCol.gridx = 6;
		gd.add(pnlcol4, cCol);
		gd.addMessage("");
        
		
		/**FINAL PREPERATION OF GD**/
        gd.doLayout();
        gd.addHelp("https://bitbucket.org/lwollatz/lungj/wiki/Home");
        gd.hideCancelButton();
        gd.showDialog();
        
     
        if (gd.wasCanceled()){
        	return 1;
        }
        
        /**GET VALUES**/
        //MAIN DIRECTORY
        LJPrefs.LJ_inpDirectory = inpdirtxt.getText();
        //APPLY WEKA CLASSIFIER
		LJPrefs.LJ_makeMap = gd.getNextBoolean();
        File file = new File(LJPrefs.LJ_clsDirectory);
		File folder = file.getParentFile();
		LJPrefs.LJ_clsDirectory = folder.getPath() + "\\" + cmbClassifier1.getItemAt(cmbClassifier1.getSelectedIndex());
		//BINARY THRESHOLD
    	LJPrefs.LJ_makeMask = gd.getNextBoolean();
    	LJPrefs.LJ_threshold = gd.getNextNumber()/100;
    	//note that colours and classifier directory have already been copied...
    	LJPrefs.savePreferences();
        
        return 0;  
    }
    
    
    public void actionPerformed(ActionEvent arg0) {
		if(arg0.getSource() == this.clsbtn ){
			JFileChooser chooser = new JFileChooser(LJPrefs.LJ_clsDirectory);
			FileFilter modelfilter = new FileNameExtensionFilter("WEKA model (*.model)", "model");
			chooser.addChoosableFileFilter(modelfilter);
			chooser.setFileSelectionMode(0);
			chooser.setSelectedFile(new File(LJPrefs.LJ_clsDirectory));
			int returnVal = chooser.showDialog(gd,"Choose WEKA Classifier");
			if(returnVal == JFileChooser.APPROVE_OPTION)
	        {
				clsDir = chooser.getSelectedFile().getPath();
				LJPrefs.LJ_clsDirectory = clsDir;
				LJPrefs.loadClassifier();
				String[] classifiers = new String[LJPrefs.LJ_classifiers.size()];
				classifiers = LJPrefs.LJ_classifiers.toArray(classifiers);
				File model = new File(LJPrefs.LJ_clsDirectory);
				cmbClassifier1.setModel(new DefaultComboBoxModel(classifiers));
				cmbClassifier1.setSelectedItem(model.getName());
				
	        }
        }
		if(arg0.getSource() == this.filebtn ){
			JFileChooser chooser = new JFileChooser(LJPrefs.LJ_inpDirectory);
			chooser.setFileSelectionMode(1);
			chooser.setSelectedFile(new File(LJPrefs.LJ_inpDirectory));
			int returnVal = chooser.showDialog(gd,"Choose Folder");
			if(returnVal == JFileChooser.APPROVE_OPTION)
	        {
				this.inpdirtxt.setText(chooser.getSelectedFile().getPath());
				
	        }
        }
	}
	

}
