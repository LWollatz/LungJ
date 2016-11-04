import ij.IJ;
import ij.plugin.BrowserLauncher; 
import ij.plugin.PlugIn; 

import java.awt.BorderLayout; 
import java.io.IOException; 

import javax.swing.JEditorPane; 
import javax.swing.JFrame; 
import javax.swing.JPanel; 
import javax.swing.event.HyperlinkEvent; 
import javax.swing.event.HyperlinkListener; 

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

/*** Help
 * displays a help dialog with information about the plugin
 * 
 * @author Lasse Wollatz
 ***/
public class Help implements PlugIn {
	/** plug-in's name **/
	public static final String PLUGIN_NAME = LJPrefs.PLUGIN_NAME;
	/** plug-in's current version **/
	public static final String PLUGIN_VERSION = LJPrefs.VERSION;
	//public static final String IMPLEMENTATION_VERSION = LungJ_.class.getPackage().getImplementationVersion();
	/** author of plug-in publication **/
	public static final String PLUGIN_AUTHOR = "L. Wollatz, S. J. Johnston, P. M. Lackie, and S. J. Cox";
	/** author of plug-in publication for BibTeX **/
	public static final String PLUGIN_AUTHOR_Bib = "Wollatz, Lasse and Johnston, Steven J. and Lackie, Peter M. and Cox, Simon J.";
	/** year of publication **/
	public static final String PLUGIN_PubYear = "2016";
	/** URL where to obtain the plug-in **/
	public static final String PLUGIN_URL = LJPrefs.PLUGIN_DOWNLOAD_URL;
	/** URL where plug-in was published **/
	public static final String PLUGIN_PubURL = LJPrefs.PLUGIN_PUBLICATION_URL;
	/** URL where to find help/documentation **/
	public static final String PLUGIN_Help = LJPrefs.PLUGIN_HELP_URL;
	/** plugin's Java version **/
	public static final String PLUGIN_JAVA = LJPrefs.PLUGIN_JAVA_VERSION;
	/** plugin's Trainable Weka Segmentation version **/
	public static final String PLUGIN_TWS = LJPrefs.PLUGIN_TWS_VERSION;
	/** plugin's ImageJ version **/
	public static final String PLUGIN_IJ = LJPrefs.PLUGIN_IJ_VERSION;
	/** version of ImageJ this is compiled with. **/
	public static final String IJ_VERSION = LJPrefs.IJ_VERSION;
	
	/*** run
     * Displays Info about LungJ on execution when requested
     * 
     * @param  arg            String containing arguments passed
     ***/
	public void run(String arg) {
		if (arg.equals("about")) {
			showAbout();
			return;
		}
	}
	
	/*** showAbout
     * Displays Info about LungJ as html pane
     * 
     ***/
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
						+ " is an ImageJ plugin designed for (but not limited to) &mu;-CT lung tissue image analysis.</p>"
						+ "<p>User and developer documentation can be found on our <a href="+PLUGIN_Help+">ImageJ Page</a></p>"
						+ "<h2>Citing &amp; Referencing</h2>"
						+ "<p>When you use this plugin for any of your academic work, please cite "+PLUGIN_NAME+" like<br/>"
						+ PLUGIN_AUTHOR+" ("+PLUGIN_PubYear+"). <i>"+PLUGIN_NAME+"</i> [Computer software]. DOI:<a href="+PLUGIN_PubURL+">"+PLUGIN_PubURL.replace("http://dx.doi.org/", "")+"</a>.<br/>"
						/*+ "<br/>in BibTeX:<br/>"
						+ "@Misc{lungj,<br/>"
						+ "	&nbsp;&nbsp;author =   {"+PLUGIN_AUTHOR_Bib+"},<br/>"
						+ "	&nbsp;&nbsp;title =    {{"+PLUGIN_NAME+"}},<br/>"
						+ "	&nbsp;&nbsp;year = {"+PLUGIN_PubYear+"},<br/>"
						+ "	&nbsp;&nbsp;publisher =    {{University of Southampton}},<br/>"
						+ "	&nbsp;&nbsp;howpublished = {[Computer software] \\url{"+PLUGIN_PubURL+"}}<br/>"
						+ "	}<br/>"*/
						+ "</p>"
						+ "<h2>Requirements</h2>"
						+ "<ul>"
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
