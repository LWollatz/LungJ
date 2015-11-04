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

	public void run(String arg) {
		if (arg.equals("about")) {
			showAbout();
			return;
		}
	}

	private void showAbout() {
		JEditorPane htmlPane = new JEditorPane(
				"text/html",
				"<html>\n"
						+ "  <body>\n"
						+ "<h1>LungJ version "
						+ PLUGIN_VERSION
						+ "</h1>"
						+ "<p>LungJ is an ImageJ plugin designed for (but not limited to) micro-CT lung tissue image analysis.</p>"
						+ "<p>If you have problems running LungJ, make sure your ImageJ uses Java 1.8.</p>"
						+ "<p>User and developer documentation can be found at our <a href=https://bitbucket.org/lwollatz/lungj/wiki/Home>Wiki</a></p>"
						+ "<h2>Installation Requirements</h2>"
						+ "<ul>"
						+ "<li>Windows 7 - 64bit recommended (not yet tested for other platforms)</li>"
						+ "<li>ImageJ 1.49s</li>"
						+ "<li>Java 1.8 (note that ImageJ comes with its own Java!)</li>"
						+ "<li>Trainable Weka Segmentation 2.2.1 (comes with Fiji)</li>"
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
