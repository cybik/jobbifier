package com.cybikbase.yafot;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.Executors;

/**
 * Created by cybik on 16-08-28.
 */
public class YafotUI {

    private JFrame frmJObbifier;
    private File inputDir;
    private File outputDir;
    private JLabel lblOutputFileName;
    private JTextField txtPackageName;
    private JTextPane txtpnOutput;
    private JRadioButton rdbtnMain;
    private JSpinner spinner;
    private JPasswordField pwdPassword;
    private JTextField txtOutputFolder;
    private JTextField txtInputFolder;
    private JLabel lblInputFolder;
    private JLabel lblOutputFolder;
    private JLabel lblPackageName;
    private JLabel lblPackageVersion;
    private JRadioButton rdbtnPatch;
    private JButton btnInputSelection;
    private JButton btnOutputSelection;
    private JButton btnCreateObb;
    private JScrollPane scrollPane;
    private JMenuItem mntmExit;


    private void updateTextPane(final String text)
    {
        SwingUtilities.invokeLater(() -> {
            Document doc = txtpnOutput.getDocument();
            try
            {
                doc.insertString(doc.getLength(), text, null);
            }
            catch (BadLocationException e)
            {
                throw new RuntimeException(e);
            }
            txtpnOutput.setCaretPosition(doc.getLength() - 1);
        });
    }

    private void redirectSystemStreams()
    {
        OutputStream out = new OutputStream()
        {
            @Override
            public void write(final int b) throws IOException
            {
                updateTextPane(String.valueOf((char)b));
            }

            @Override
            public void write(byte[] b, int off, int len) throws IOException
            {
                updateTextPane(new String(b, off, len));
            }

            @Override
            public void write(byte[] b) throws IOException
            {
                write(b, 0, b.length);
            }
        };
        System.setOut(new PrintStream(out, true));
        System.setErr(new PrintStream(out, true));
    }

    /**
     * Create the application.
     * @param main
     */
    public YafotUI(Main main) {
        initialize(main);
    }
    /**
     * Initialize the contents of the frame.
     * @param main
     */
    private void initialize(Main main) {
        frmJObbifier = new JFrame();
        frmJObbifier.setResizable(false);
        frmJObbifier.setBounds(100, 100, 619, 488);
        frmJObbifier.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmJObbifier.setTitle("Yet Another Friendly Obbifier Tool");
        inputDir = null;
        outputDir = null;

        JMenuBar menuBar = new JMenuBar();
        frmJObbifier.setJMenuBar(menuBar);

        JMenu mnFile = new JMenu("File");
        mnFile.setMnemonic('F');
        menuBar.add(mnFile);

        mntmExit = new JMenuItem("Exit");
        mntmExit.setMnemonic('x');
        mntmExit.addActionListener(e -> frmJObbifier.dispose());
        mnFile.add(mntmExit);

        lblInputFolder = new JLabel("Input folder:");

        btnInputSelection = new JButton("...");
        btnInputSelection.addActionListener(e -> {
            inputDir = SanitizePath(inputDir, txtInputFolder);
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(inputDir == null ? "." : inputDir.getPath()));
            chooser.setDialogTitle("Select input folder...");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(frmJObbifier) == JFileChooser.APPROVE_OPTION) {
                inputDir = chooser.getSelectedFile();
                txtInputFolder.setText(inputDir.getAbsolutePath());
            }
        });

        lblOutputFolder = new JLabel("Output location:");

        btnOutputSelection = new JButton("...");
        btnOutputSelection.addActionListener(e -> {
            outputDir = SanitizePath(outputDir, txtOutputFolder);
            JFileChooser chooser = new JFileChooser();
            chooser.setCurrentDirectory(new File(outputDir == null ? "." : outputDir.getPath()));
            chooser.setDialogTitle("Select output folder...");
            chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            chooser.setAcceptAllFileFilterUsed(false);
            if (chooser.showOpenDialog(frmJObbifier) == JFileChooser.APPROVE_OPTION)
            {
                outputDir = chooser.getSelectedFile();
                txtOutputFolder.setText(outputDir.getAbsolutePath());
            }
        });

        lblPackageName = new JLabel("Package name:");

        txtPackageName = new JTextField();
        txtPackageName.setText("com.example.sample");
        txtPackageName.setColumns(35);
        txtPackageName.getDocument().addDocumentListener(new DocumentListener()
        {
            @Override
            public void changedUpdate(DocumentEvent e)
            {
                updateOutputFileNameLabel();
            }

            @Override
            public void insertUpdate(DocumentEvent e)
            {
                updateOutputFileNameLabel();
            }

            @Override
            public void removeUpdate(DocumentEvent e)
            {
                updateOutputFileNameLabel();
            }
        });

        lblPackageVersion = new JLabel("Package version:");

        spinner = new JSpinner();
        spinner.addChangeListener(e -> updateOutputFileNameLabel());
        spinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));

        rdbtnMain = new JRadioButton("Main");
        rdbtnMain.setSelected(true);
        rdbtnMain.addItemListener(e -> updateOutputFileNameLabel());

        rdbtnPatch = new JRadioButton("Patch");
        rdbtnPatch.addItemListener(e -> updateOutputFileNameLabel());

        lblOutputFileName = new JLabel();
        updateOutputFileNameLabel();

        JButton btnShowPassword = new JButton("Show");
        btnShowPassword.setVisible(false);
        btnShowPassword.addMouseListener(new MouseAdapter() {
            char passwordEchoChar;

            @Override
            public void mousePressed(MouseEvent e) {
                passwordEchoChar = pwdPassword.getEchoChar();
                pwdPassword.setEchoChar((char)0);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                pwdPassword.setEchoChar(passwordEchoChar);
            }
        });

        JCheckBox chkUsePassword = new JCheckBox("Use password?");
        chkUsePassword.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                boolean enabled = e.getStateChange() == ItemEvent.SELECTED;
                if (enabled)
                {
                    int result = JOptionPane.showConfirmDialog(frmJObbifier,
                            "Warning! Encrypted OBB files may not work with all versions of Android. Do you wish to continue?",
                            "Use password?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                    if (result != JOptionPane.YES_OPTION)
                    {
                        enabled = false;
                    }
                    // TODO: why is this being unchecked when the dialog is shown??
                    chkUsePassword.removeItemListener(this);
                    chkUsePassword.setSelected(enabled);
                    chkUsePassword.addItemListener(this);
                }
                chkUsePassword.setText("Use password" + (enabled ? ":" : "?"));
                pwdPassword.setVisible(enabled);
                btnShowPassword.setVisible(enabled);
            }
        });

        btnCreateObb = new JButton("Create OBB");
        btnCreateObb.addActionListener(e -> {
            if ((inputDir = SanitizePath(inputDir, txtInputFolder, true)) == null) return;
            if ((outputDir = SanitizePath(outputDir, txtOutputFolder, true)) == null) return;
            if (inputDir == outputDir)
            {
                JOptionPane.showMessageDialog(frmJObbifier,
                        "Error: Input and output directories cannot be the same.",
                        "Error!", JOptionPane.ERROR_MESSAGE);
                return;
            }
            java.util.List<String> args1 = main.prepareArguments(
                    inputDir.getPath(),
                    outputDir.getPath() + File.separator + getOutputFileName(),
                    txtPackageName.getText(),
                    spinner.getValue().toString(),
                    (chkUsePassword.isSelected()
                            ? new String(pwdPassword.getPassword())
                            : null
                    )
            );
			/*
            List<String> args = new ArrayList<>(asList(
            new String[]
            {
                "-d", inputDir.getPath(),
                "-o", outputDir.getPath() + File.separator + getOutputFileName(),
                "-pn", txtPackageName.getText(),
                "-pv", spinner.getValue().toString(),
                "-v"
            }));
            if (chkUsePassword.isSelected())
            {
                char[] pwdChars = pwdPassword.getPassword();
                if (pwdChars != null)
                {
                    if (pwdChars.length != 0)
                    {
                        args.add("-k");
                        args.add(new String(pwdChars));
                    }
                    for (int i = 0; i < pwdChars.length; ++i)
                    {
                        pwdChars[i] = (char)0;
                    }
                }
            }*/
            Executors.newSingleThreadExecutor().execute(() -> {
                // run the jobb code in a separate thread so program doesn't hang
                main.invoke(args1);
                //com.android.jobb.Main.main(args.toArray(new String[0]));
            });
        });

        scrollPane = new JScrollPane();
        scrollPane.setAutoscrolls(true);

        pwdPassword = new JPasswordField();
        pwdPassword.setVisible(false);
        pwdPassword.setColumns(10);

        txtOutputFolder = new JTextField();
        txtOutputFolder.setColumns(10);

        txtInputFolder = new JTextField();
        txtInputFolder.setColumns(10);

        GroupLayout groupLayout = new GroupLayout(frmJObbifier.getContentPane());
        groupLayout.setHorizontalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                        .addComponent(scrollPane, GroupLayout.Alignment.LEADING)
                                        .addComponent(lblOutputFileName, GroupLayout.Alignment.LEADING)
                                        .addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
                                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                .addGroup(groupLayout.createSequentialGroup()
                                                                        .addComponent(lblOutputFolder)
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                .addComponent(txtInputFolder, GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                                                                                .addComponent(txtOutputFolder)))
                                                                .addGroup(groupLayout.createSequentialGroup()
                                                                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING, false)
                                                                                .addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
                                                                                        .addComponent(chkUsePassword)
                                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                        .addComponent(pwdPassword))
                                                                                .addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
                                                                                        .addComponent(btnCreateObb)
                                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                        .addComponent(rdbtnMain)
                                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                        .addComponent(rdbtnPatch))
                                                                                .addGroup(GroupLayout.Alignment.LEADING, groupLayout.createSequentialGroup()
                                                                                        .addComponent(lblPackageName)
                                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                                                                        .addComponent(txtPackageName, GroupLayout.PREFERRED_SIZE, 251, GroupLayout.PREFERRED_SIZE)))
                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                        .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                                                .addGroup(groupLayout.createSequentialGroup()
                                                                                        .addComponent(lblPackageVersion)
                                                                                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                                                        .addComponent(spinner, GroupLayout.DEFAULT_SIZE, 73, Short.MAX_VALUE))
                                                                                .addComponent(btnShowPassword))))
                                                        .addComponent(lblInputFolder))
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                        .addComponent(btnOutputSelection)
                                                        .addComponent(btnInputSelection))))
                                .addContainerGap(27, Short.MAX_VALUE))
        );
        groupLayout.setVerticalGroup(
                groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(groupLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblInputFolder)
                                        .addComponent(txtInputFolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnInputSelection))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblOutputFolder)
                                        .addComponent(txtOutputFolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnOutputSelection))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblPackageName)
                                        .addComponent(txtPackageName, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(spinner, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(lblPackageVersion))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(chkUsePassword)
                                        .addComponent(pwdPassword, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                        .addComponent(btnShowPassword))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnCreateObb)
                                        .addComponent(rdbtnMain)
                                        .addComponent(rdbtnPatch))
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblOutputFileName)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 223, Short.MAX_VALUE)
                                .addContainerGap())
        );

        txtpnOutput = new JTextPane();
        scrollPane.setViewportView(txtpnOutput);
        txtpnOutput.setEditable(false);
        ButtonGroup mainPatchGroup = new ButtonGroup();
        mainPatchGroup.add(rdbtnMain);
        mainPatchGroup.add(rdbtnPatch);
        frmJObbifier.getContentPane().setLayout(groupLayout);
        redirectSystemStreams();
    }


    private String getOutputFileNamePrefix()
    {
        return "Output file name: ";
    }

    private String getOutputFileName()
    {
        return (rdbtnMain.isSelected() ? "main" : "patch") + "." + spinner.getValue().toString() + "." + txtPackageName.getText() + ".obb";
    }

    private void updateOutputFileNameLabel()
    {
        lblOutputFileName.setText(getOutputFileNamePrefix() + getOutputFileName());
    }

    public void execute() {
        // Present the UI
        EventQueue.invokeLater(() -> {
            try {
                frmJObbifier.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }


    private File SanitizePath(File dir, JTextField textBox, boolean warn)
    {
        String path = textBox.getText();
        if ((path != null) && (!path.equals("")))
        {
            File pathDir = new File(path);
            if (pathDir.exists() && pathDir.isDirectory())
            {
                return pathDir;
            }
        }
        if (dir == null)
        {
            dir = new File(".");
        }
        if (warn)
        {
            int result = JOptionPane.showConfirmDialog(frmJObbifier,
                    "Entered path ('" + path + "') is not a valid directory! Defaulting to '" +
                            dir.getAbsolutePath() + "' instead.", "Invalid path entered!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.WARNING_MESSAGE);
            if (result == JOptionPane.CANCEL_OPTION)
            {
                return null;
            }
        }
        textBox.setText(dir.getAbsolutePath());
        return dir;
    }

    private File SanitizePath(File dir, JTextField textBox)
    {
        return SanitizePath(dir, textBox, false);
    }
}
