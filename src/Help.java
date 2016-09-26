
import ij.IJ;
import ij.plugin.PlugIn; 
import ij.plugin.BrowserLauncher; 

 


import java.awt.BorderLayout; 

 
import java.io.IOException; 

 


import javax.swing.JEditorPane; 
import javax.swing.JFrame; 
import javax.swing.JPanel; 
 
 
import javax.swing.event.HyperlinkEvent; 
import javax.swing.event.HyperlinkListener; 
import lj.LJPrefs;



public class Help implements PlugIn {
	
	/** plugin's name */
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plugin's current version */
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	public static final String PLUGIN_AUTHOR = "Wollatz, L.";
	public static final String PLUGIN_PubYear = "2016";
	public static final String PLUGIN_URL = "http://sites.imagej.net/LungJ";
	/** plugin's Java version */
	public static final String PLUGIN_JAVA = LJPrefs.PLUGIN_JAVA_VERSION;
	/** plugin's Trainable Weka Segmentation version */
	public static final String PLUGIN_TWS = LJPrefs.PLUGIN_TWS_VERSION;
	/** plugin's ImageJ version */
	public static final String PLUGIN_IJ = LJPrefs.PLUGIN_IJ_VERSION;
	public static final String IJ_VERSION = LJPrefs.IJ_VERSION;

	public void run(String arg) {
		if (arg.equals("about")) {
			showAbout();
			return;
		}
	}

	private void showAbout() {
		String JAVA_VERSION = "1.4";
		if (IJ.isJava15()) {JAVA_VERSION = "1.5";}
		if (IJ.isJava16()) {JAVA_VERSION = "1.6";}
		if (IJ.isJava17()) {JAVA_VERSION = "1.7";}
		if (IJ.isJava18()) {JAVA_VERSION = "1.8 or higher";}
		
		JEditorPane htmlPane = new JEditorPane(
				"text/html",
				"<html>\n"
						+ "  <body>\n"
						+ "<h1>LungJ version "
						+ PLUGIN_VERSION
						+ "</h1>"
						+ "<p>"
						+ PLUGIN_NAME
						+ " is an ImageJ plugin designed for (but not limited to) micro-CT lung tissue image analysis.</p>"
						+ "<p>If you have problems running "+PLUGIN_NAME+", make sure your ImageJ uses Java compatible with "
						+ PLUGIN_JAVA 
						+ ".</p>"
						+ "<p>User and developer documentation can be found at our <a href=https://bitbucket.org/lwollatz/lungj/wiki/Home>Wiki</a></p>"
						+ "<h2>Citing &amp; Referencing</h2>"
						+ "<p>When you use this plugin for any of your academic work, please cite "+PLUGIN_NAME+" like<br/>"
						+ PLUGIN_AUTHOR+" ("+PLUGIN_PubYear+"). "+PLUGIN_NAME+" [Computer software]. Retrieved from "+PLUGIN_URL+".<br/>"
						+ "in BibTeX:<br/>"
						+ "@Misc{wollatz16,<br/>"
						+ "	author =   {"+PLUGIN_AUTHOR+"},<br/>"
						+ "	title =    {{"+PLUGIN_NAME+"}},<br/>"
						+ "	howpublished = {\\url{"+PLUGIN_URL+"}},<br/>"
						+ "	year = {"+PLUGIN_PubYear+"}<br/>"
						+ "	}<br/>"
						+ "</p>"
						+ "<h2>Installation Requirements</h2>"
						+ "<ul>"
						+ "<li>Windows 7 - 64bit recommended (not yet tested for other platforms)</li>"
						+ "<li>ImageJ "+PLUGIN_IJ+" (currently "+IJ_VERSION+")</li>"
						+ "<li>Java "+PLUGIN_JAVA+" (currently "+JAVA_VERSION+") Note that ImageJ comes with its own Java!</li>"
						+ "<li>Trainable Weka Segmentation "+PLUGIN_TWS+" (comes with Fiji)</li>"
						+ "<li>Flood Fill(3D) (comes with Fiji)</li>"
						+ "</ul>"
						+ "\n" + "  </body>\n" + "</html>");
		htmlPane.setEditable(false);
		htmlPane.setOpaque(false);
		htmlPane.addHyperlinkListener(new HyperlinkListener() {
			public void hyperlinkUpdate(HyperlinkEvent e) {
				if (HyperlinkEvent.EventType.ACTIVATED.equals(e.getEventType()))
					try {
						BrowserLauncher.openURL(e.getURL().toString());
					} catch (IOException exception) {
						// ignore
					}
			}
		});

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(htmlPane, BorderLayout.CENTER);

		final JFrame frame = new JFrame("About LungJ...");
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().add(panel, BorderLayout.CENTER);
		frame.pack();
		frame.setVisible(true);
	}

}
