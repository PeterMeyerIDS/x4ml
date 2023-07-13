package x4ml;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.prefs.BackingStoreException;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;

public class X4MLGUI {

	private JFrame frmX4ml;
	private JButton launchInBrowserButton;
	private JTextArea txtrPleaseWait;
	
	
	/**
	 * Create the application.
	 */
	public X4MLGUI() {
		initialize();
		updateGUIAndSetProjectDir();
		
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmX4ml = new JFrame();
		frmX4ml.setResizable(false);
		frmX4ml.setTitle("x4ml Launcher (version " + X4MLMain.VERSION + ")");
		frmX4ml.setBounds(100, 100, 575, 340);
		frmX4ml.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel panel = new JPanel();
		panel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null), "x4ml mission control", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		GroupLayout groupLayout = new GroupLayout(frmX4ml.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 563, Short.MAX_VALUE)
					.addContainerGap())
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel, GroupLayout.DEFAULT_SIZE, 306, Short.MAX_VALUE)
					.addContainerGap())
		);
		
		JButton setWorkDirButton = new JButton("set/change your x4ml base directory");
		setWorkDirButton.setFocusable(false);
		
		launchInBrowserButton = new JButton("open a new x4ml application window in browser");
		launchInBrowserButton.setFont(new Font("Lucida Grande", Font.BOLD, 13));
		launchInBrowserButton.setFocusable(false);
		launchInBrowserButton.setForeground(new Color(0, 153, 102));
		launchInBrowserButton.setEnabled(false);
		launchInBrowserButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				try {
					java.awt.Desktop.getDesktop().browse(X4MLMain.getDesktopModeURL(X4MLMain.DESKTOPUSER));
				} catch (IOException exc) {
					exc.printStackTrace();
				}
			}
		});
		
		JButton endAppButton = new JButton("end x4ml");
		endAppButton.setFocusable(false);
		endAppButton.setForeground(Color.RED);
		endAppButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		txtrPleaseWait = new JTextArea();
		txtrPleaseWait.setText("Please wait…");
		txtrPleaseWait.setLineWrap(true);
		txtrPleaseWait.setFocusable(false);
		txtrPleaseWait.setOpaque(false);
		txtrPleaseWait.setRequestFocusEnabled(false);
		txtrPleaseWait.setEditable(false);
		GroupLayout gl_panel = new GroupLayout(panel);
		gl_panel.setHorizontalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(Alignment.TRAILING, gl_panel.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel.createParallelGroup(Alignment.TRAILING)
						.addComponent(launchInBrowserButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
						.addComponent(setWorkDirButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE)
						.addGroup(Alignment.LEADING, gl_panel.createSequentialGroup()
							.addGap(6)
							.addComponent(txtrPleaseWait, 0, 0, Short.MAX_VALUE))
						.addComponent(endAppButton, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 551, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panel.setVerticalGroup(
			gl_panel.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel.createSequentialGroup()
					.addContainerGap()
					.addComponent(setWorkDirButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(launchInBrowserButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(endAppButton)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(txtrPleaseWait, GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
					.addContainerGap())
		);
		panel.setLayout(gl_panel);
		setWorkDirButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				File previousProjectDir = X4MLPreferences4DesktopMode.getProjectDirOrNull();
				if (previousProjectDir == null) {
					fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				} else {
					fileChooser.setCurrentDirectory(previousProjectDir.getParentFile());
					
				}
				
				int result = fileChooser.showOpenDialog(frmX4ml);
				if (result == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = fileChooser.getSelectedFile();
				    try {
				    	X4MLPreferences4DesktopMode.setProjectDir(selectedFile);
				    	//X4MLMain.initNewDesktopSession();
					} catch (BackingStoreException e1) {
						JOptionPane.showMessageDialog(frmX4ml, "Unknown error upon selecting base directory.");
						e1.printStackTrace();
					}
				    updateGUIAndSetProjectDir();
				} 
//				// testing
//				else {
//					X4MLPreferences.removePrefsForDebugAndTest();
//					updateGUI();
//				}
			}
		});
		frmX4ml.getContentPane().setLayout(groupLayout);
	}
	
	private void updateGUIAndSetProjectDir() {
		File projectDir = X4MLPreferences4DesktopMode.getProjectDirOrNull();
		if (projectDir != null) {
			X4MLMain.desktopModeProjectDir = projectDir.getAbsolutePath();
			launchInBrowserButton.setEnabled(true);
			txtrPleaseWait.setText("Current x4ml base directory:\n\n" + projectDir.getAbsolutePath());
		} else {
			launchInBrowserButton.setEnabled(false);
			txtrPleaseWait.setText("No x4ml base directory selected yet.\n\nYou must specify a base directory "
					+ "before you can launch the application window.\n\nThe base directory is the folder where all your data are located.");
		}
	}
}
