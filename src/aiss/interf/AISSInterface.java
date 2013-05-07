package aiss.interf;

import java.awt.BorderLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/*
 * Cifra: O utilizador escolhe a pasta pelo bot‹o Escolhe as opcoes que quer usar (sign,
 * cipher, timestamp) Clica cifrar e abre popup para escolher o ficheiro onde vai guardar
 * (.aiss)
 * 
 * Decifra: O utilizador escolhe o ficheiro que vai decifrar (.aiss) O programa decifra
 * autom‡ticamente para uma pasta no mesmo directorio e apaga o original. Mostrar o
 * resultado da decifra (bem cifrado, bem assinado, timestamp e o nome do sender se
 * existe)
 */
public class AISSInterface extends
        JPanel
        implements ActionListener {
    static private final String newline = "\n";
    JButton openButton, saveButton;
    JTextArea log;
    JFileChooser fileChooser;
    JFileChooser folderChooser;

    public AISSInterface() {
        super(new BorderLayout());

        // Create the log first, because the action listeners
        // need to refer to it.
        log = new JTextArea(20, 50);
        log.setMargin(new Insets(5, 5, 5, 5));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);

        // Create a file chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);


        // Create the open button. We use the image from the JLF
        // Graphics Repository (but we extracted it from the jar).
        openButton = new JButton("Open Received Email",
                createImageIcon("images/Open16.gif"));
        openButton.addActionListener(this);

        // Create the save button. We use the image from the JLF
        // Graphics Repository (but we extracted it from the jar).
        saveButton = new JButton("Open Folder to Send",
                createImageIcon("images/Save16.gif"));
        saveButton.addActionListener(this);

        // For layout purposes, put the buttons in a separate panel
        JPanel buttonPanel = new JPanel(); // use FlowLayout
        buttonPanel.add(openButton);
        buttonPanel.add(saveButton);

        // Add the buttons and the log to this panel.
        add(buttonPanel, BorderLayout.PAGE_START);
        add(logScrollPane, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        // Handle open button action.
        if (e.getSource() == openButton) {
            int returnVal = fileChooser.showOpenDialog(AISSInterface.this);

            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // This is where a real application would open the file.
                log.append("Opening: " + file.getName() + "." + newline);
            } else {
                log.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());

            // Handle save button action.
        } else if (e.getSource() == saveButton) {
            int returnVal = fileChooser.showSaveDialog(AISSInterface.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                // This is where a real application would save the file.
                log.append("Saving: " + file.getName() + "." + newline);
            } else {
                log.append("Save command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        }
    }

    /** Returns an ImageIcon, or null if the path was invalid. */
    protected static ImageIcon createImageIcon(String path) {
        java.net.URL imgURL = AISSInterface.class.getResource(path);
        if (imgURL != null) {
            return new ImageIcon(imgURL);
        } else {
            System.err.println("Couldn't find file: " + path);
            return null;
        }
    }

    /**
     * Create the GUI and show it. For thread safety, this method should be invoked from
     * the event dispatch thread.
     */
    private static void createAndShowGUI() {
        // Create and set up the window.
        JFrame frame = new JFrame("FileChooserDemo");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Add content to the window.
        frame.add(new AISSInterface());

        // Display the window.
        frame.pack();
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        // Schedule a job for the event dispatch thread:
        // creating and showing this application's GUI.
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                // Turn off metal's use of bold fonts
                UIManager.put("swing.boldMetal", Boolean.FALSE);
                createAndShowGUI();
            }
        });
    }
}
