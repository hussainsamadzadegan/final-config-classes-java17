package finalconfigclasses.cfg.engine.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import finalconfigclasses.cfg.engine.ConfigClassGenerator;
import finalconfigclasses.cfg.engine.ConfigClassGenerator.Options;

/**
 * Small Swing front-end for {@link ConfigClassGenerator}: lets you pick a
 * config-classes description XML file and a destination source directory,
 * then generates the config bean (and DiffHelper) classes with a couple of
 * clicks - the Java 17 replacement for the old Ant "codegen" target.
 */
public class ConfigGeneratorFrame extends JFrame {

	private static final long serialVersionUID = 1L;

	private final JTextField xmlFileField = new JTextField(40);
	private final JTextField destDirField = new JTextField(40);
	private final JTextField templatesDirField = new JTextField(40);
	private final JCheckBox diffHelpersCheck = new JCheckBox("Generate DiffHelper classes", true);
	private final JCheckBox flexibleCheck = new JCheckBox(
			"Auto-create ZooKeeper znode path if missing (\"flexible\")", true);
	private final JTextArea logArea = new JTextArea(16, 60);
	private final JButton generateButton = new JButton("Generate");

	public ConfigGeneratorFrame() {
		super("FinalConfigClasses - Config Class Generator");
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		buildUI();
		pack();
		setLocationRelativeTo(null);
	}

	private void buildUI() {
		JPanel form = new JPanel(new GridBagLayout());
		form.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(4, 4, 4, 4);
		c.fill = GridBagConstraints.HORIZONTAL;

		int row = 0;
		addRow(form, c, row++, "Description XML:", xmlFileField, e -> browseFile(xmlFileField));
		addRow(form, c, row++, "Destination dir:", destDirField, e -> browseDir(destDirField));
		addRow(form, c, row++, "Custom templates dir (optional):", templatesDirField, e -> browseDir(templatesDirField));

		c.gridx = 1;
		c.gridy = row++;
		c.gridwidth = 2;
		form.add(diffHelpersCheck, c);

		c.gridx = 1;
		c.gridy = row++;
		form.add(flexibleCheck, c);

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		generateButton.addActionListener(e -> onGenerate());
		buttonPanel.add(generateButton);

		logArea.setEditable(false);
		JScrollPane logScroll = new JScrollPane(logArea);
		logScroll.setBorder(BorderFactory.createTitledBorder("Log"));

		JPanel content = new JPanel(new BorderLayout(8, 8));
		content.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));
		content.add(form, BorderLayout.NORTH);
		content.add(logScroll, BorderLayout.CENTER);
		content.add(buttonPanel, BorderLayout.SOUTH);
		content.setPreferredSize(new Dimension(720, 520));

		setContentPane(content);
	}

	private interface RowAction {
		void run(java.awt.event.ActionEvent e);
	}

	private void addRow(JPanel form, GridBagConstraints c, int row, String label, JTextField field,
			RowAction browseAction) {
		c.gridx = 0;
		c.gridy = row;
		c.gridwidth = 1;
		c.weightx = 0;
		form.add(new JLabel(label), c);

		c.gridx = 1;
		c.weightx = 1;
		form.add(field, c);

		c.gridx = 2;
		c.weightx = 0;
		JButton browse = new JButton("Browse...");
		browse.addActionListener(browseAction::run);
		form.add(browse, c);
	}

	private void browseFile(JTextField target) {
		JFileChooser chooser = new JFileChooser();
		if (target.getText() != null && target.getText().length() > 0) {
			chooser.setSelectedFile(new java.io.File(target.getText()));
		}
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			target.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	private void browseDir(JTextField target) {
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (target.getText() != null && target.getText().length() > 0) {
			chooser.setCurrentDirectory(new java.io.File(target.getText()));
		}
		int result = chooser.showOpenDialog(this);
		if (result == JFileChooser.APPROVE_OPTION) {
			target.setText(chooser.getSelectedFile().getAbsolutePath());
		}
	}

	/** Optionally pre-fill the description XML field, e.g. from a .cmd argument. */
	public void setInitialXmlFile(String xmlFile) {
		xmlFileField.setText(xmlFile);
	}

	private void onGenerate() {
		String xmlFile = xmlFileField.getText().trim();
		String destDir = destDirField.getText().trim();
		String templatesDir = templatesDirField.getText().trim();

		if (xmlFile.length() == 0 || destDir.length() == 0) {
			JOptionPane.showMessageDialog(this, "Please choose both a description XML file and a destination directory.",
					"Missing input", JOptionPane.WARNING_MESSAGE);
			return;
		}

		Options opts = new Options(xmlFile, destDir);
		opts.generateDiffHelpers = diffHelpersCheck.isSelected();
		opts.manageConfigXml = flexibleCheck.isSelected();
		opts.templatesDir = templatesDir.length() > 0 ? templatesDir : null;

		generateButton.setEnabled(false);
		logArea.setText("");

		new SwingWorker<Void, Void>() {
			private ByteArrayOutputStream buffer;
			private Exception failure;

			@Override
			protected Void doInBackground() {
				buffer = new ByteArrayOutputStream();
				PrintStream log = new PrintStream(buffer);
				try {
					new ConfigClassGenerator(log).generate(opts);
				} catch (Exception e) {
					failure = e;
					e.printStackTrace(log);
				}
				return null;
			}

			@Override
			protected void done() {
				logArea.setText(buffer.toString());
				generateButton.setEnabled(true);
				if (failure == null) {
					JOptionPane.showMessageDialog(ConfigGeneratorFrame.this,
							"Config classes generated successfully into:\n" + destDir, "Success",
							JOptionPane.INFORMATION_MESSAGE);
				} else {
					JOptionPane.showMessageDialog(ConfigGeneratorFrame.this,
							"Generation failed: " + failure.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
				}
			}
		}.execute();
	}

	public static void launch(String initialXmlFile) {
		SwingUtilities.invokeLater(() -> {
			ConfigGeneratorFrame frame = new ConfigGeneratorFrame();
			if (initialXmlFile != null) {
				frame.setInitialXmlFile(initialXmlFile);
			}
			frame.setVisible(true);
		});
	}
}
