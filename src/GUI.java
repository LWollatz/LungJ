import ij.WindowManager;
import ij.gui.*;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.GridBagLayout;

import javax.swing.JLabel;

import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Font;

import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.SwingConstants;
import javax.swing.SpringLayout;
import javax.swing.JCheckBox;
import javax.swing.JButton;
import javax.swing.JTextField;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JSeparator;

import java.awt.Color;
import java.awt.FlowLayout;

import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.border.SoftBevelBorder;
import javax.swing.border.BevelBorder;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.border.MatteBorder;
import javax.swing.border.LineBorder;

public class GUI extends JFrame {

	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private JTextField textField_3;
	private JTextField textField_4;
	private final Action action = new SwingAction();
	
	private final Font fntNormal = new Font("Tahoma", Font.PLAIN, 11);
	private final Font fntTitle = new Font("Tahoma", Font.BOLD, 16);
	private final Font fntHeader = new Font("Tahoma", Font.BOLD, 11);
	private final Border ColPrevBorder = new MatteBorder(1, 1, 1, 1, (Color) new Color(0, 0, 0));
	//private final Border ColPrevBorder = new SoftBevelBorder(BevelBorder.RAISED, null, null, null, null);
	//private final Border ColPrevBorder = new EtchedBorder(EtchedBorder.RAISED, null, new Color(0, 0, 0));
	private final String GUI_Font = "Tahoma";
	private final Action action_1 = new SwingAction_1();

	/**
	 * Launch the application.
	 */
	public static void main(String args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					//LJPrefs.loadPreferences();
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setTitle("LungJ");
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 943, 380);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{60, 60, 60, 20, 60, 30, 30, 60, 30, 60, 30, 10, 10, 0};
		gbl_contentPane.rowHeights = new int[] {15, 15, 15, 15, 15, 15, 15, 15, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		JLabel lblLungj = new JLabel("LungJ");
		lblLungj.setFont(fntTitle);
		lblLungj.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblLungj = new GridBagConstraints();
		gbc_lblLungj.anchor = GridBagConstraints.EAST;
		gbc_lblLungj.insets = new Insets(0, 0, 5, 5);
		gbc_lblLungj.gridx = 0;
		gbc_lblLungj.gridy = 0;
		contentPane.add(lblLungj, gbc_lblLungj);
		
		JLabel lblV = new JLabel("v"+LJPrefs.LJ_version );
		lblV.setFont(fntTitle);
		lblV.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblV = new GridBagConstraints();
		gbc_lblV.anchor = GridBagConstraints.WEST;
		gbc_lblV.gridwidth = 2;
		gbc_lblV.insets = new Insets(0, 0, 5, 5);
		gbc_lblV.gridx = 1;
		gbc_lblV.gridy = 0;
		contentPane.add(lblV, gbc_lblV);
		
		JLabel lblClass0 = new JLabel("Original Image");
		GridBagConstraints gbc_lblClass0 = new GridBagConstraints();
		gbc_lblClass0.anchor = GridBagConstraints.WEST;
		gbc_lblClass0.insets = new Insets(0, 0, 5, 5);
		gbc_lblClass0.gridx = 0;
		gbc_lblClass0.gridy = 1;
		contentPane.add(lblClass0, gbc_lblClass0);
		
		
		
		JComboBox cmbimgSegment0 = new JComboBox();
		GridBagConstraints gbc_cmbimgSegment0 = new GridBagConstraints();
		gbc_cmbimgSegment0.anchor = GridBagConstraints.SOUTH;
		gbc_cmbimgSegment0.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgSegment0.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgSegment0.gridwidth = 2;
		gbc_cmbimgSegment0.gridx = 1;
		gbc_cmbimgSegment0.gridy = 1;
		contentPane.add(cmbimgSegment0, gbc_cmbimgSegment0);
		
		JLabel lblCreateFrom = new JLabel("Create From");
		lblCreateFrom.setFont(fntHeader);
		GridBagConstraints gbc_lblCreateFrom = new GridBagConstraints();
		gbc_lblCreateFrom.anchor = GridBagConstraints.SOUTH;
		gbc_lblCreateFrom.fill = GridBagConstraints.HORIZONTAL;
		gbc_lblCreateFrom.insets = new Insets(0, 0, 5, 5);
		gbc_lblCreateFrom.gridx = 1;
		gbc_lblCreateFrom.gridy = 2;
		contentPane.add(lblCreateFrom, gbc_lblCreateFrom);
		
		JLabel lblNewLabel = new JLabel("Classifier");
		lblNewLabel.setFont(fntHeader);
		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
		gbc_lblNewLabel.anchor = GridBagConstraints.NORTHWEST;
		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblNewLabel.gridx = 2;
		gbc_lblNewLabel.gridy = 2;
		contentPane.add(lblNewLabel, gbc_lblNewLabel);
		
		JLabel lblInvert = new JLabel("Invert");
		lblInvert.setFont(fntHeader);
		GridBagConstraints gbc_lblInvert = new GridBagConstraints();
		gbc_lblInvert.anchor = GridBagConstraints.SOUTHEAST;
		gbc_lblInvert.insets = new Insets(0, 0, 5, 5);
		gbc_lblInvert.gridx = 3;
		gbc_lblInvert.gridy = 2;
		contentPane.add(lblInvert, gbc_lblInvert);
		
		JLabel lblProbabilityMap = new JLabel("Probability Map");
		lblProbabilityMap.setFont(fntHeader);
		GridBagConstraints gbc_lblProbabilityMap = new GridBagConstraints();
		gbc_lblProbabilityMap.gridwidth = 2;
		gbc_lblProbabilityMap.anchor = GridBagConstraints.NORTH;
		gbc_lblProbabilityMap.insets = new Insets(0, 0, 5, 5);
		gbc_lblProbabilityMap.gridx = 4;
		gbc_lblProbabilityMap.gridy = 2;
		contentPane.add(lblProbabilityMap, gbc_lblProbabilityMap);
		
		JLabel lblThreshold = new JLabel("threshold");
		lblThreshold.setFont(fntHeader);
		GridBagConstraints gbc_lblThreshold = new GridBagConstraints();
		gbc_lblThreshold.anchor = GridBagConstraints.NORTH;
		gbc_lblThreshold.insets = new Insets(0, 0, 5, 5);
		gbc_lblThreshold.gridx = 6;
		gbc_lblThreshold.gridy = 2;
		contentPane.add(lblThreshold, gbc_lblThreshold);
		
		JLabel lblMask = new JLabel("Mask");
		lblMask.setFont(fntHeader);
		GridBagConstraints gbc_lblMask = new GridBagConstraints();
		gbc_lblMask.gridwidth = 2;
		gbc_lblMask.anchor = GridBagConstraints.NORTH;
		gbc_lblMask.insets = new Insets(0, 0, 5, 5);
		gbc_lblMask.gridx = 7;
		gbc_lblMask.gridy = 2;
		contentPane.add(lblMask, gbc_lblMask);
		
		JLabel lblSegmentation = new JLabel("Segmentation");
		lblSegmentation.setFont(fntHeader);
		GridBagConstraints gbc_lblSegmentation = new GridBagConstraints();
		gbc_lblSegmentation.gridwidth = 2;
		gbc_lblSegmentation.insets = new Insets(0, 0, 5, 5);
		gbc_lblSegmentation.gridx = 9;
		gbc_lblSegmentation.gridy = 2;
		contentPane.add(lblSegmentation, gbc_lblSegmentation);
		
		JCheckBox chckbxNewCheckBox = new JCheckBox("");
		GridBagConstraints gbc_chckbxNewCheckBox = new GridBagConstraints();
		gbc_chckbxNewCheckBox.anchor = GridBagConstraints.NORTH;
		gbc_chckbxNewCheckBox.insets = new Insets(0, 0, 5, 5);
		gbc_chckbxNewCheckBox.gridx = 3;
		gbc_chckbxNewCheckBox.gridy = 3;
		contentPane.add(chckbxNewCheckBox, gbc_chckbxNewCheckBox);
		
		JLabel lblClass1 = new JLabel(LJPrefs.LJ_segname1);
		GridBagConstraints gbc_lblClass1 = new GridBagConstraints();
		gbc_lblClass1.anchor = GridBagConstraints.WEST;
		gbc_lblClass1.insets = new Insets(0, 0, 5, 5);
		gbc_lblClass1.gridx = 0;
		gbc_lblClass1.gridy = 3;
		contentPane.add(lblClass1, gbc_lblClass1);
		
		JComboBox comboBox_1 = new JComboBox();
		comboBox_1.setModel(new DefaultComboBoxModel(new String[] {"Original", LJPrefs.LJ_segname1, LJPrefs.LJ_segname2, LJPrefs.LJ_segname3, LJPrefs.LJ_segname4}));
		comboBox_1.setSelectedIndex(0);
		GridBagConstraints gbc_comboBox_1 = new GridBagConstraints();
		gbc_comboBox_1.anchor = GridBagConstraints.NORTHWEST;
		gbc_comboBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_1.gridx = 1;
		gbc_comboBox_1.gridy = 3;
		contentPane.add(comboBox_1, gbc_comboBox_1);
		
		JComboBox cmbClassifier1 = new JComboBox();
		GridBagConstraints gbc_cmbClassifier1 = new GridBagConstraints();
		gbc_cmbClassifier1.anchor = GridBagConstraints.NORTH;
		gbc_cmbClassifier1.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbClassifier1.insets = new Insets(0, 0, 5, 5);
		gbc_cmbClassifier1.gridx = 2;
		gbc_cmbClassifier1.gridy = 3;
		contentPane.add(cmbClassifier1, gbc_cmbClassifier1);
		
		JComboBox cmbimgProbability1 = new JComboBox();
		GridBagConstraints gbc_cmbimgProbability1 = new GridBagConstraints();
		gbc_cmbimgProbability1.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgProbability1.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgProbability1.gridx = 4;
		gbc_cmbimgProbability1.gridy = 3;
		contentPane.add(cmbimgProbability1, gbc_cmbimgProbability1);
		
		JButton btnCreate = new JButton("Create");
		GridBagConstraints gbc_btnCreate = new GridBagConstraints();
		gbc_btnCreate.insets = new Insets(0, 0, 5, 5);
		gbc_btnCreate.gridx = 5;
		gbc_btnCreate.gridy = 3;
		contentPane.add(btnCreate, gbc_btnCreate);
		
		textField = new JTextField();
		textField.setToolTipText("between 0 and 1");
		textField.setText("0.2");
		GridBagConstraints gbc_textField = new GridBagConstraints();
		gbc_textField.insets = new Insets(0, 0, 5, 5);
		gbc_textField.gridx = 6;
		gbc_textField.gridy = 3;
		contentPane.add(textField, gbc_textField);
		textField.setColumns(4);
		
		JComboBox cmbimgMask1 = new JComboBox();
		GridBagConstraints gbc_cmbimgMask1 = new GridBagConstraints();
		gbc_cmbimgMask1.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgMask1.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgMask1.gridx = 7;
		gbc_cmbimgMask1.gridy = 3;
		contentPane.add(cmbimgMask1, gbc_cmbimgMask1);
		
		JButton button_4 = new JButton("Create");
		GridBagConstraints gbc_button_4 = new GridBagConstraints();
		gbc_button_4.insets = new Insets(0, 0, 5, 5);
		gbc_button_4.gridx = 8;
		gbc_button_4.gridy = 3;
		contentPane.add(button_4, gbc_button_4);
		
		JComboBox cmbimgSegment1 = new JComboBox();
		GridBagConstraints gbc_cmbimgSegment1 = new GridBagConstraints();
		gbc_cmbimgSegment1.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgSegment1.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgSegment1.gridx = 9;
		gbc_cmbimgSegment1.gridy = 3;
		contentPane.add(cmbimgSegment1, gbc_cmbimgSegment1);
		
		JButton button_9 = new JButton("Create");
		GridBagConstraints gbc_button_9 = new GridBagConstraints();
		gbc_button_9.insets = new Insets(0, 0, 5, 5);
		gbc_button_9.gridx = 10;
		gbc_button_9.gridy = 3;
		contentPane.add(button_9, gbc_button_9);
		
		JPanel pnlcol1 = new JPanel();
		pnlcol1.setBorder(ColPrevBorder);
		pnlcol1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				LJPrefs.LJ_Color1 = Set_Up.getColor("Choose Color", LJPrefs.LJ_Color1);
				pnlcol1.setBackground(LJPrefs.LJ_Color1);
			}
		});
		pnlcol1.setBackground(LJPrefs.LJ_Color1);
		FlowLayout flowLayout = (FlowLayout) pnlcol1.getLayout();
		GridBagConstraints gbc_pnlcol1 = new GridBagConstraints();
		gbc_pnlcol1.anchor = GridBagConstraints.WEST;
		gbc_pnlcol1.insets = new Insets(0, 5, 5, 0);
		gbc_pnlcol1.gridx = 12;
		gbc_pnlcol1.gridy = 3;
		contentPane.add(pnlcol1, gbc_pnlcol1);
		
		
		
		JLabel lblClass2 = new JLabel(LJPrefs.LJ_segname2);
		GridBagConstraints gbc_lblClass2 = new GridBagConstraints();
		gbc_lblClass2.anchor = GridBagConstraints.WEST;
		gbc_lblClass2.insets = new Insets(0, 0, 5, 5);
		gbc_lblClass2.gridx = 0;
		gbc_lblClass2.gridy = 4;
		contentPane.add(lblClass2, gbc_lblClass2);
		
		JComboBox comboBox = new JComboBox();
		comboBox.setModel(new DefaultComboBoxModel(new String[] {"Original", "Tissue", "Fibre", "Vessels"}));
		comboBox.setSelectedIndex(1);
		GridBagConstraints gbc_comboBox = new GridBagConstraints();
		gbc_comboBox.anchor = GridBagConstraints.SOUTHWEST;
		gbc_comboBox.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox.gridx = 1;
		gbc_comboBox.gridy = 4;
		contentPane.add(comboBox, gbc_comboBox);
		
		JComboBox cmbClassifier2 = new JComboBox();
		GridBagConstraints gbc_cmbClassifier2 = new GridBagConstraints();
		gbc_cmbClassifier2.anchor = GridBagConstraints.SOUTH;
		gbc_cmbClassifier2.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbClassifier2.insets = new Insets(0, 0, 5, 5);
		gbc_cmbClassifier2.gridx = 2;
		gbc_cmbClassifier2.gridy = 4;
		contentPane.add(cmbClassifier2, gbc_cmbClassifier2);
		
		JCheckBox checkBox = new JCheckBox("");
		GridBagConstraints gbc_checkBox = new GridBagConstraints();
		gbc_checkBox.anchor = GridBagConstraints.NORTH;
		gbc_checkBox.insets = new Insets(0, 0, 5, 5);
		gbc_checkBox.gridx = 3;
		gbc_checkBox.gridy = 4;
		contentPane.add(checkBox, gbc_checkBox);
		
		JComboBox cmbimgProbability2 = new JComboBox();
		GridBagConstraints gbc_cmbimgProbability2 = new GridBagConstraints();
		gbc_cmbimgProbability2.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgProbability2.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgProbability2.gridx = 4;
		gbc_cmbimgProbability2.gridy = 4;
		contentPane.add(cmbimgProbability2, gbc_cmbimgProbability2);
		
		JButton button = new JButton("Create");
		GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.insets = new Insets(0, 0, 5, 5);
		gbc_button.gridx = 5;
		gbc_button.gridy = 4;
		contentPane.add(button, gbc_button);
		
		textField_1 = new JTextField();
		textField_1.setToolTipText("between 0 and 1");
		textField_1.setText("0.2");
		textField_1.setColumns(4);
		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 5);
		gbc_textField_1.gridx = 6;
		gbc_textField_1.gridy = 4;
		contentPane.add(textField_1, gbc_textField_1);
		
		JComboBox cmbimgMask2 = new JComboBox();
		GridBagConstraints gbc_cmbimgMask2 = new GridBagConstraints();
		gbc_cmbimgMask2.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgMask2.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgMask2.gridx = 7;
		gbc_cmbimgMask2.gridy = 4;
		contentPane.add(cmbimgMask2, gbc_cmbimgMask2);
		
		JButton button_5 = new JButton("Create");
		GridBagConstraints gbc_button_5 = new GridBagConstraints();
		gbc_button_5.insets = new Insets(0, 0, 5, 5);
		gbc_button_5.gridx = 8;
		gbc_button_5.gridy = 4;
		contentPane.add(button_5, gbc_button_5);
		
		JComboBox cmbimgSegment2 = new JComboBox();
		GridBagConstraints gbc_cmbimgSegment2 = new GridBagConstraints();
		gbc_cmbimgSegment2.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgSegment2.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgSegment2.gridx = 9;
		gbc_cmbimgSegment2.gridy = 4;
		contentPane.add(cmbimgSegment2, gbc_cmbimgSegment2);
		
		JButton button_10 = new JButton("Create");
		GridBagConstraints gbc_button_10 = new GridBagConstraints();
		gbc_button_10.insets = new Insets(0, 0, 5, 5);
		gbc_button_10.gridx = 10;
		gbc_button_10.gridy = 4;
		contentPane.add(button_10, gbc_button_10);
		
		JPanel pnlcol2 = new JPanel();
		pnlcol2.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				LJPrefs.LJ_Color2 = Set_Up.getColor("Choose Color", LJPrefs.LJ_Color2);
				pnlcol2.setBackground(LJPrefs.LJ_Color2);
			}
		});
		pnlcol2.setBorder(ColPrevBorder);
		pnlcol2.setBackground(LJPrefs.LJ_Color2);
		GridBagConstraints gbc_pnlcol2 = new GridBagConstraints();
		gbc_pnlcol2.anchor = GridBagConstraints.WEST;
		gbc_pnlcol2.insets = new Insets(0, 5, 5, 0);
		gbc_pnlcol2.gridx = 12;
		gbc_pnlcol2.gridy = 4;
		contentPane.add(pnlcol2, gbc_pnlcol2);
		
		JLabel lblClass3 = new JLabel(LJPrefs.LJ_segname3);
		GridBagConstraints gbc_lblClass3 = new GridBagConstraints();
		gbc_lblClass3.anchor = GridBagConstraints.WEST;
		gbc_lblClass3.insets = new Insets(0, 0, 5, 5);
		gbc_lblClass3.gridx = 0;
		gbc_lblClass3.gridy = 5;
		contentPane.add(lblClass3, gbc_lblClass3);
		
		JComboBox comboBox_4 = new JComboBox();
		comboBox_4.setModel(new DefaultComboBoxModel(new String[] {"Original", "Tissue", "Fibre", "Vessels"}));
		comboBox_4.setSelectedIndex(1);
		GridBagConstraints gbc_comboBox_4 = new GridBagConstraints();
		gbc_comboBox_4.anchor = GridBagConstraints.SOUTHWEST;
		gbc_comboBox_4.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_4.gridx = 1;
		gbc_comboBox_4.gridy = 5;
		contentPane.add(comboBox_4, gbc_comboBox_4);
		
		JComboBox cmbClassifier3 = new JComboBox();
		GridBagConstraints gbc_cmbClassifier3 = new GridBagConstraints();
		gbc_cmbClassifier3.anchor = GridBagConstraints.SOUTH;
		gbc_cmbClassifier3.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbClassifier3.insets = new Insets(0, 0, 5, 5);
		gbc_cmbClassifier3.gridx = 2;
		gbc_cmbClassifier3.gridy = 5;
		contentPane.add(cmbClassifier3, gbc_cmbClassifier3);
		
		JCheckBox checkBox_1 = new JCheckBox("");
		GridBagConstraints gbc_checkBox_1 = new GridBagConstraints();
		gbc_checkBox_1.anchor = GridBagConstraints.NORTH;
		gbc_checkBox_1.insets = new Insets(0, 0, 5, 5);
		gbc_checkBox_1.gridx = 3;
		gbc_checkBox_1.gridy = 5;
		contentPane.add(checkBox_1, gbc_checkBox_1);
		
		JComboBox cmbimgProbability3 = new JComboBox();
		GridBagConstraints gbc_cmbimgProbability3 = new GridBagConstraints();
		gbc_cmbimgProbability3.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgProbability3.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgProbability3.gridx = 4;
		gbc_cmbimgProbability3.gridy = 5;
		contentPane.add(cmbimgProbability3, gbc_cmbimgProbability3);
		
		JButton button_1 = new JButton("Create");
		GridBagConstraints gbc_button_1 = new GridBagConstraints();
		gbc_button_1.insets = new Insets(0, 0, 5, 5);
		gbc_button_1.gridx = 5;
		gbc_button_1.gridy = 5;
		contentPane.add(button_1, gbc_button_1);
		
		textField_2 = new JTextField();
		textField_2.setToolTipText("between 0 and 1");
		textField_2.setText("0.2");
		textField_2.setColumns(4);
		GridBagConstraints gbc_textField_2 = new GridBagConstraints();
		gbc_textField_2.insets = new Insets(0, 0, 5, 5);
		gbc_textField_2.gridx = 6;
		gbc_textField_2.gridy = 5;
		contentPane.add(textField_2, gbc_textField_2);
		
		JComboBox cmbimgMask3 = new JComboBox();
		GridBagConstraints gbc_cmbimgMask3 = new GridBagConstraints();
		gbc_cmbimgMask3.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgMask3.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgMask3.gridx = 7;
		gbc_cmbimgMask3.gridy = 5;
		contentPane.add(cmbimgMask3, gbc_cmbimgMask3);
		
		JButton button_6 = new JButton("Create");
		GridBagConstraints gbc_button_6 = new GridBagConstraints();
		gbc_button_6.insets = new Insets(0, 0, 5, 5);
		gbc_button_6.gridx = 8;
		gbc_button_6.gridy = 5;
		contentPane.add(button_6, gbc_button_6);
		
		JComboBox cmbimgSegment3 = new JComboBox();
		GridBagConstraints gbc_cmbimgSegment3 = new GridBagConstraints();
		gbc_cmbimgSegment3.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgSegment3.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgSegment3.gridx = 9;
		gbc_cmbimgSegment3.gridy = 5;
		contentPane.add(cmbimgSegment3, gbc_cmbimgSegment3);
		
		JButton button_11 = new JButton("Create");
		GridBagConstraints gbc_button_11 = new GridBagConstraints();
		gbc_button_11.insets = new Insets(0, 0, 5, 5);
		gbc_button_11.gridx = 10;
		gbc_button_11.gridy = 5;
		contentPane.add(button_11, gbc_button_11);
		
		JPanel pnlcol3 = new JPanel();
		pnlcol3.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				LJPrefs.LJ_Color3 = Set_Up.getColor("Choose Color", LJPrefs.LJ_Color3);
				pnlcol3.setBackground(LJPrefs.LJ_Color3);
			}
		});
		pnlcol3.setBorder(ColPrevBorder);
		pnlcol3.setBackground(LJPrefs.LJ_Color3);
		GridBagConstraints gbc_pnlcol3 = new GridBagConstraints();
		gbc_pnlcol3.anchor = GridBagConstraints.WEST;
		gbc_pnlcol3.insets = new Insets(0, 5, 5, 0);
		gbc_pnlcol3.gridx = 12;
		gbc_pnlcol3.gridy = 5;
		contentPane.add(pnlcol3, gbc_pnlcol3);
		
		JLabel lblClass4 = new JLabel(LJPrefs.LJ_segname4);
		lblClass4.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblClass4 = new GridBagConstraints();
		gbc_lblClass4.anchor = GridBagConstraints.WEST;
		gbc_lblClass4.insets = new Insets(0, 0, 5, 5);
		gbc_lblClass4.gridx = 0;
		gbc_lblClass4.gridy = 6;
		contentPane.add(lblClass4, gbc_lblClass4);
		
		JComboBox comboBox_5 = new JComboBox();
		comboBox_5.setModel(new DefaultComboBoxModel(new String[] {"Original", "Tissue", "Fibre", "Vessels"}));
		comboBox_5.setSelectedIndex(3);
		GridBagConstraints gbc_comboBox_5 = new GridBagConstraints();
		gbc_comboBox_5.anchor = GridBagConstraints.NORTHWEST;
		gbc_comboBox_5.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_5.gridx = 1;
		gbc_comboBox_5.gridy = 6;
		contentPane.add(comboBox_5, gbc_comboBox_5);
		
		JComboBox cmbClassifier4 = new JComboBox();
		GridBagConstraints gbc_cmbClassifier4 = new GridBagConstraints();
		gbc_cmbClassifier4.anchor = GridBagConstraints.NORTH;
		gbc_cmbClassifier4.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbClassifier4.insets = new Insets(0, 0, 5, 5);
		gbc_cmbClassifier4.gridx = 2;
		gbc_cmbClassifier4.gridy = 6;
		contentPane.add(cmbClassifier4, gbc_cmbClassifier4);
		
		JCheckBox checkBox_2 = new JCheckBox("");
		GridBagConstraints gbc_checkBox_2 = new GridBagConstraints();
		gbc_checkBox_2.anchor = GridBagConstraints.NORTH;
		gbc_checkBox_2.insets = new Insets(0, 0, 5, 5);
		gbc_checkBox_2.gridx = 3;
		gbc_checkBox_2.gridy = 6;
		contentPane.add(checkBox_2, gbc_checkBox_2);
		
		JComboBox cmbimgProbability4 = new JComboBox();
		GridBagConstraints gbc_cmbimgProbability4 = new GridBagConstraints();
		gbc_cmbimgProbability4.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgProbability4.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgProbability4.gridx = 4;
		gbc_cmbimgProbability4.gridy = 6;
		contentPane.add(cmbimgProbability4, gbc_cmbimgProbability4);
		
		JButton button_2 = new JButton("Create");
		GridBagConstraints gbc_button_2 = new GridBagConstraints();
		gbc_button_2.insets = new Insets(0, 0, 5, 5);
		gbc_button_2.gridx = 5;
		gbc_button_2.gridy = 6;
		contentPane.add(button_2, gbc_button_2);
		
		textField_3 = new JTextField();
		textField_3.setToolTipText("between 0 and 1");
		textField_3.setText("0.2");
		textField_3.setColumns(4);
		GridBagConstraints gbc_textField_3 = new GridBagConstraints();
		gbc_textField_3.insets = new Insets(0, 0, 5, 5);
		gbc_textField_3.gridx = 6;
		gbc_textField_3.gridy = 6;
		contentPane.add(textField_3, gbc_textField_3);
		
		JComboBox cmbimgMask4 = new JComboBox();
		GridBagConstraints gbc_cmbimgMask4 = new GridBagConstraints();
		gbc_cmbimgMask4.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgMask4.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgMask4.gridx = 7;
		gbc_cmbimgMask4.gridy = 6;
		contentPane.add(cmbimgMask4, gbc_cmbimgMask4);
		
		JButton button_7 = new JButton("Create");
		GridBagConstraints gbc_button_7 = new GridBagConstraints();
		gbc_button_7.insets = new Insets(0, 0, 5, 5);
		gbc_button_7.gridx = 8;
		gbc_button_7.gridy = 6;
		contentPane.add(button_7, gbc_button_7);
		
		JComboBox cmbimgSegment4 = new JComboBox();
		GridBagConstraints gbc_cmbimgSegment4 = new GridBagConstraints();
		gbc_cmbimgSegment4.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgSegment4.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgSegment4.gridx = 9;
		gbc_cmbimgSegment4.gridy = 6;
		contentPane.add(cmbimgSegment4, gbc_cmbimgSegment4);
		
		JButton button_12 = new JButton("Create");
		GridBagConstraints gbc_button_12 = new GridBagConstraints();
		gbc_button_12.insets = new Insets(0, 0, 5, 5);
		gbc_button_12.gridx = 10;
		gbc_button_12.gridy = 6;
		contentPane.add(button_12, gbc_button_12);
		
		JPanel pnlcol4 = new JPanel();
		pnlcol4.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				LJPrefs.LJ_Color4 = Set_Up.getColor("Choose Color", LJPrefs.LJ_Color4);
				pnlcol4.setBackground(LJPrefs.LJ_Color4);
			}
		});
		pnlcol4.setBorder(ColPrevBorder);
		pnlcol4.setBackground(LJPrefs.LJ_Color4);
		GridBagConstraints gbc_pnlcol4 = new GridBagConstraints();
		gbc_pnlcol4.anchor = GridBagConstraints.WEST;
		gbc_pnlcol4.insets = new Insets(0, 5, 5, 0);
		gbc_pnlcol4.gridx = 12;
		gbc_pnlcol4.gridy = 6;
		contentPane.add(pnlcol4, gbc_pnlcol4);
		
		JLabel lblClass5 = new JLabel(LJPrefs.LJ_segname5);
		lblClass5.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblClass5 = new GridBagConstraints();
		gbc_lblClass5.anchor = GridBagConstraints.WEST;
		gbc_lblClass5.insets = new Insets(0, 0, 5, 5);
		gbc_lblClass5.gridx = 0;
		gbc_lblClass5.gridy = 7;
		contentPane.add(lblClass5, gbc_lblClass5);
		
		JComboBox comboBox_6 = new JComboBox();
		comboBox_6.setModel(new DefaultComboBoxModel(new String[] {"Original", "Tissue", "Fibre", "Vessels"}));
		comboBox_6.setSelectedIndex(3);
		GridBagConstraints gbc_comboBox_6 = new GridBagConstraints();
		gbc_comboBox_6.anchor = GridBagConstraints.NORTHWEST;
		gbc_comboBox_6.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_6.gridx = 1;
		gbc_comboBox_6.gridy = 7;
		contentPane.add(comboBox_6, gbc_comboBox_6);
		
		JComboBox cmbClassifier5 = new JComboBox();
		GridBagConstraints gbc_cmbClassifier5 = new GridBagConstraints();
		gbc_cmbClassifier5.anchor = GridBagConstraints.NORTH;
		gbc_cmbClassifier5.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbClassifier5.insets = new Insets(0, 0, 5, 5);
		gbc_cmbClassifier5.gridx = 2;
		gbc_cmbClassifier5.gridy = 7;
		contentPane.add(cmbClassifier5, gbc_cmbClassifier5);
		
		JCheckBox checkBox_3 = new JCheckBox("");
		GridBagConstraints gbc_checkBox_3 = new GridBagConstraints();
		gbc_checkBox_3.anchor = GridBagConstraints.NORTH;
		gbc_checkBox_3.insets = new Insets(0, 0, 5, 5);
		gbc_checkBox_3.gridx = 3;
		gbc_checkBox_3.gridy = 7;
		contentPane.add(checkBox_3, gbc_checkBox_3);
		
		JComboBox cmbimgProbability5 = new JComboBox();
		GridBagConstraints gbc_cmbimgProbability5 = new GridBagConstraints();
		gbc_cmbimgProbability5.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgProbability5.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgProbability5.gridx = 4;
		gbc_cmbimgProbability5.gridy = 7;
		contentPane.add(cmbimgProbability5, gbc_cmbimgProbability5);
		
		JButton button_3 = new JButton("Create");
		GridBagConstraints gbc_button_3 = new GridBagConstraints();
		gbc_button_3.insets = new Insets(0, 0, 5, 5);
		gbc_button_3.gridx = 5;
		gbc_button_3.gridy = 7;
		contentPane.add(button_3, gbc_button_3);
		
		textField_4 = new JTextField();
		textField_4.setToolTipText("between 0 and 1");
		textField_4.setText("0.2");
		textField_4.setColumns(4);
		GridBagConstraints gbc_textField_4 = new GridBagConstraints();
		gbc_textField_4.insets = new Insets(0, 0, 5, 5);
		gbc_textField_4.gridx = 6;
		gbc_textField_4.gridy = 7;
		contentPane.add(textField_4, gbc_textField_4);
		
		JComboBox cmbimgMask5 = new JComboBox();
		GridBagConstraints gbc_cmbimgMask5 = new GridBagConstraints();
		gbc_cmbimgMask5.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgMask5.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgMask5.gridx = 7;
		gbc_cmbimgMask5.gridy = 7;
		contentPane.add(cmbimgMask5, gbc_cmbimgMask5);
		
		JButton button_8 = new JButton("Create");
		GridBagConstraints gbc_button_8 = new GridBagConstraints();
		gbc_button_8.insets = new Insets(0, 0, 5, 5);
		gbc_button_8.gridx = 8;
		gbc_button_8.gridy = 7;
		contentPane.add(button_8, gbc_button_8);
		
		JComboBox cmbimgSegment5 = new JComboBox();
		GridBagConstraints gbc_cmbimgSegment5 = new GridBagConstraints();
		gbc_cmbimgSegment5.insets = new Insets(0, 0, 5, 5);
		gbc_cmbimgSegment5.fill = GridBagConstraints.HORIZONTAL;
		gbc_cmbimgSegment5.gridx = 9;
		gbc_cmbimgSegment5.gridy = 7;
		contentPane.add(cmbimgSegment5, gbc_cmbimgSegment5);
		
		JButton button_13 = new JButton("Create");
		GridBagConstraints gbc_button_13 = new GridBagConstraints();
		gbc_button_13.insets = new Insets(0, 0, 5, 5);
		gbc_button_13.gridx = 10;
		gbc_button_13.gridy = 7;
		contentPane.add(button_13, gbc_button_13);
		
		JPanel pnlcol5 = new JPanel();
		pnlcol5.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				LJPrefs.LJ_Color5 = Set_Up.getColor("Choose Color", LJPrefs.LJ_Color5);
				pnlcol5.setBackground(LJPrefs.LJ_Color5);
			}
		});
		pnlcol5.setBorder(ColPrevBorder);
		pnlcol5.setBackground(LJPrefs.LJ_Color5);
		GridBagConstraints gbc_pnlcol5 = new GridBagConstraints();
		gbc_pnlcol5.anchor = GridBagConstraints.WEST;
		gbc_pnlcol5.insets = new Insets(0, 5, 5, 0);
		gbc_pnlcol5.gridx = 12;
		gbc_pnlcol5.gridy = 7;
		contentPane.add(pnlcol5, gbc_pnlcol5);
		
		JComboBox comboBox_21 = new JComboBox();
		GridBagConstraints gbc_comboBox_21 = new GridBagConstraints();
		gbc_comboBox_21.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_21.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_21.gridx = 7;
		gbc_comboBox_21.gridy = 8;
		contentPane.add(comboBox_21, gbc_comboBox_21);
		
		JButton btnCombine = new JButton("Combine");
		GridBagConstraints gbc_btnCombine = new GridBagConstraints();
		gbc_btnCombine.insets = new Insets(0, 0, 5, 5);
		gbc_btnCombine.gridx = 8;
		gbc_btnCombine.gridy = 8;
		contentPane.add(btnCombine, gbc_btnCombine);
		
		
		
		JComboBox comboBox_27 = new JComboBox();
		GridBagConstraints gbc_comboBox_27 = new GridBagConstraints();
		gbc_comboBox_27.insets = new Insets(0, 0, 5, 5);
		gbc_comboBox_27.fill = GridBagConstraints.HORIZONTAL;
		gbc_comboBox_27.gridx = 9;
		gbc_comboBox_27.gridy = 8;
		contentPane.add(comboBox_27, gbc_comboBox_27);
		
		JButton btnOverlay = new JButton("Overlay");
		GridBagConstraints gbc_btnOverlay = new GridBagConstraints();
		gbc_btnOverlay.insets = new Insets(0, 0, 5, 5);
		gbc_btnOverlay.gridx = 10;
		gbc_btnOverlay.gridy = 8;
		contentPane.add(btnOverlay, gbc_btnOverlay);
		
		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 11;
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 9;
		contentPane.add(separator, gbc_separator);
		
		JLabel lblClassifier = new JLabel("Classifier");
		lblClassifier.setHorizontalAlignment(SwingConstants.LEFT);
		GridBagConstraints gbc_lblClassifier = new GridBagConstraints();
		gbc_lblClassifier.anchor = GridBagConstraints.WEST;
		gbc_lblClassifier.insets = new Insets(0, 0, 5, 5);
		gbc_lblClassifier.gridx = 0;
		gbc_lblClassifier.gridy = 10;
		contentPane.add(lblClassifier, gbc_lblClassifier);
		
		JLabel lblClsDirectory = new JLabel(LJPrefs.LJ_clsDirectory);
		GridBagConstraints gbc_lblClsDirectory = new GridBagConstraints();
		gbc_lblClsDirectory.anchor = GridBagConstraints.WEST;
		gbc_lblClsDirectory.insets = new Insets(0, 0, 5, 5);
		gbc_lblClsDirectory.gridwidth = 4;
		gbc_lblClsDirectory.gridx = 1;
		gbc_lblClsDirectory.gridy = 10;
		contentPane.add(lblClsDirectory, gbc_lblClsDirectory);
		
		LJPrefs.loadClassifier();
		String[] classifiers = new String[LJPrefs.LJ_classifiers.size()];
		classifiers = LJPrefs.LJ_classifiers.toArray(classifiers);
		cmbClassifier1.setModel(new DefaultComboBoxModel(classifiers));
		cmbClassifier2.setModel(new DefaultComboBoxModel(classifiers));
		cmbClassifier3.setModel(new DefaultComboBoxModel(classifiers));
		cmbClassifier4.setModel(new DefaultComboBoxModel(classifiers));
		cmbClassifier5.setModel(new DefaultComboBoxModel(classifiers));
		
		String[] imagenames = WindowManager.getImageTitles();
		cmbimgProbability1.setModel(new DefaultComboBoxModel(imagenames));
		cmbimgProbability2.setModel(new DefaultComboBoxModel(imagenames));
		cmbimgProbability3.setModel(new DefaultComboBoxModel(imagenames));
		cmbimgProbability4.setModel(new DefaultComboBoxModel(imagenames));
		cmbimgProbability5.setModel(new DefaultComboBoxModel(imagenames));
		cmbimgMask1.setModel(new DefaultComboBoxModel(imagenames));
		cmbimgMask2.setModel(new DefaultComboBoxModel(imagenames));
		cmbimgMask3.setModel(new DefaultComboBoxModel(imagenames));
		cmbimgMask4.setModel(new DefaultComboBoxModel(imagenames));
		cmbimgMask5.setModel(new DefaultComboBoxModel(imagenames));
		
		JButton btnNewButton = new JButton("...");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent arg0) {
				//open file dialog...
				LJPrefs.LJ_clsDirectory = Set_Up.getClassifier();
				lblClsDirectory.setText(LJPrefs.LJ_clsDirectory);
				String[] classifiers = new String[LJPrefs.LJ_classifiers.size()];
				classifiers = LJPrefs.LJ_classifiers.toArray(classifiers);
				cmbClassifier1.setModel(new DefaultComboBoxModel(classifiers));
				cmbClassifier2.setModel(new DefaultComboBoxModel(classifiers));
				cmbClassifier3.setModel(new DefaultComboBoxModel(classifiers));
				cmbClassifier4.setModel(new DefaultComboBoxModel(classifiers));
				cmbClassifier5.setModel(new DefaultComboBoxModel(classifiers));
			}
		});
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.anchor = GridBagConstraints.NORTHEAST;
		gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
		gbc_btnNewButton.gridx = 5;
		gbc_btnNewButton.gridy = 10;
		contentPane.add(btnNewButton, gbc_btnNewButton);
		
		JButton btnSavePreferences = new JButton("Save Preferences");
		btnSavePreferences.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				LJPrefs.savePreferences();
			}
		});
		GridBagConstraints gbc_btnSavePreferences = new GridBagConstraints();
		gbc_btnSavePreferences.gridwidth = 2;
		gbc_btnSavePreferences.insets = new Insets(0, 0, 0, 5);
		gbc_btnSavePreferences.gridx = 9;
		gbc_btnSavePreferences.gridy = 11;
		contentPane.add(btnSavePreferences, gbc_btnSavePreferences);
	}
	private class SwingAction extends AbstractAction {
		public SwingAction() {
			putValue(NAME, "SwingAction");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
	private class SwingAction_1 extends AbstractAction {
		public SwingAction_1() {
			putValue(NAME, "SwingAction_1");
			putValue(SHORT_DESCRIPTION, "Some short description");
		}
		public void actionPerformed(ActionEvent e) {
		}
	}
}
