import ij.plugin.PlugIn; 
import ij.plugin.BrowserLauncher; 

 

import java.awt.BorderLayout; 

 
import java.io.IOException; 

 

import javax.swing.JEditorPane; 
import javax.swing.JFrame; 
import javax.swing.JPanel; 
 
 
import javax.swing.event.HyperlinkEvent; 
import javax.swing.event.HyperlinkListener; 


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
						+ "<p><b>LungJ version "
						+ PLUGIN_VERSION
						+ "</b>	</p>"
						+ "<p>LungJ is an ImageJ plugin designed for (but not limited to) micro-CT lung tissue image analysis.</p>"
						+ "<p>If you have problems running LungJ, make sure your ImageJ uses Java 1.8.</p>"
						+ "<p>User and developer documentation can be found at <a href=http://bonej.org/>bonej.org</a></p>"
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
