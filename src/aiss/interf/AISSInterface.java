package aiss.interf;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import aiss.receiver.Receiver;
import aiss.sender.Sender;

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
    JButton openButton, saveButton; //// template - to del
    JButton openToSendButton, openReceivedButton, saveSenderButton, saveReceiverButton;
    JTextArea log;
    JPanel topPanel;
    JPanel senderPanel;
    JPanel receiverPanel;
    JPanel optionSenderPanel;
    JPanel homePanel;
    JCheckBox signCB , cipherCB, timestampCB;
    JTextArea maininfo;
    JTextArea logsender;
    JTextArea logreceiver;
    JFileChooser fileChooser;
    JFileChooser folderChooser;
    JTabbedPane tabbedPane;
    
    // variables to pass to the service
    private boolean sign = false;
    private boolean cipher = false;
    private boolean timestamp = false;
    private String emailDir = "";
    

    public AISSInterface() {
        super(new BorderLayout());
  
        ////////// LOGS & MAIN INFO
        // creating the log for the sender
        logreceiver = new JTextArea(10,50);
        logreceiver.setMargin(new Insets(5, 5, 5, 5));
        logreceiver.setEditable(false);
        logreceiver.append("Receiver log:\n");
        JScrollPane logReceiverScrollPane = new JScrollPane(logreceiver);
        
        // creating the log for the receiver
        logsender = new JTextArea(10,50);
        logsender.setMargin(new Insets(5, 5, 5, 5));
        logsender.setEditable(false);
        logsender.append("Sender log:\n");
        JScrollPane logSenderScrollPane = new JScrollPane(logsender);
        
        // main info on the top of page
        maininfo = new JTextArea(4, 50);
        maininfo.setMargin(new Insets(10, 10, 10, 10));
        maininfo.setEditable(false);
        maininfo.setText("Welcome to AISS Security Email Service.\nPlease select your action below.");
        JScrollPane mainScrollPane = new JScrollPane(maininfo);
        ///////////////
        
        ///////// FILE/FOLDER CHOOSERS
        // Create a file chooser
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // Create folder chooser
        folderChooser = new JFileChooser();
        folderChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        
        ///////// BUTTONS
        // Create the openToSendButton. 
        openToSendButton = new JButton("Open Folder to Send");
        openToSendButton.addActionListener(this);
        // Create the openReceivedButton
        openReceivedButton = new JButton("Open Received Email");
        openReceivedButton.addActionListener(this);
        openReceivedButton.setPreferredSize(new Dimension(120,60));
        // save button for sender
        saveSenderButton = new JButton("Save");
        saveSenderButton.addActionListener(this);
        saveSenderButton.setPreferredSize(new Dimension(120,60));
        // save button for sender
        saveReceiverButton = new JButton("Save");
        saveReceiverButton.addActionListener(this);

        ////// TOP PANEL CONTAINS MAIN INFO TEXT
        // top panel where it is the main info
        topPanel = new JPanel();
        // add main info to the top panel
        topPanel.add(mainScrollPane);
        ////////////////
        
        /////////// HOME PANEL
        // homePanel is the welcome page
        homePanel = new JPanel();
        ////////////
        
        /////// SENDER PANEL CONTAINS.....
        // optionsenderPanel contains...
        signCB = new JCheckBox("Sign");
        signCB.addActionListener(this);
        cipherCB = new JCheckBox("Cipher");
        cipherCB.addActionListener(this);
        timestampCB = new JCheckBox("TimeStamp");
        timestampCB.addActionListener(this);
        // optionsenderPanel
        optionSenderPanel = new JPanel();
        optionSenderPanel.setLayout(new BoxLayout(optionSenderPanel, BoxLayout.PAGE_AXIS));
        //Titled borders
        TitledBorder title;
        title = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.black), "Options:");
        optionSenderPanel.setBorder(title);
        optionSenderPanel.add(signCB);
        optionSenderPanel.add(cipherCB);
        optionSenderPanel.add(timestampCB);
        // Box with the 2 buttons and the options
        JPanel senderBox = new JPanel();
        senderBox.setLayout(new BoxLayout(senderBox, BoxLayout.LINE_AXIS));
        senderBox.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
        senderBox.add(openToSendButton);
        senderBox.add(Box.createRigidArea(new Dimension(60,0)));
        senderBox.add(optionSenderPanel);
        senderBox.add(Box.createRigidArea(new Dimension(60,0)));
        senderBox.add(saveSenderButton);
        //senderBox.setBackground(new Color(255,255,204));
        senderPanel = new JPanel(new BorderLayout());
        senderPanel.add(logSenderScrollPane, BorderLayout.PAGE_END);
        senderPanel.add(senderBox, BorderLayout.CENTER);
        ///////////////
        
        
        /////// RECEIVER PANEL CONTAINS......
        JPanel receiverBox = new JPanel();
        receiverBox.setLayout(new BoxLayout(receiverBox, BoxLayout.LINE_AXIS));
        receiverBox.setBorder(BorderFactory.createEmptyBorder(10, 60, 10, 10));
        receiverBox.add(openReceivedButton);
        receiverBox.add(Box.createRigidArea(new Dimension(60,0)));
        //receiverBox.add(optionSenderPanel);
        receiverBox.add(Box.createRigidArea(new Dimension(60,0)));
        receiverBox.add(saveReceiverButton);
        receiverPanel = new JPanel(new BorderLayout());
        receiverPanel.add(logReceiverScrollPane, BorderLayout.PAGE_END);
        receiverPanel.add(receiverBox, BorderLayout.CENTER);
        //////////////
        
        // ChangeListener for tabs
        ChangeListener changeListener = new ChangeListener() {
            public void stateChanged(ChangeEvent changeEvent) {
              JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
              int index = sourceTabbedPane.getSelectedIndex();
              if(index == 0){
            	  maininfo.setText("Welcome to AISS Security Email Service.\nPlease select your action below.");
              } else if (index == 1){
            	  maininfo.setText("SENDER\nClick on Open to load the directory where you have your mail files.\nChoose your options.\nClick on Save to save the protect file to disk.");
              } else {
            	  maininfo.setText("RECEIVER\nClick on Open to load the protected file.\nCheck output information.\nClick on Save button to save original mail files to disk.");
              }
              //maininfo.setText("Tab changed to: " + sourceTabbedPane.getTitleAt(index));
            }
         };
        
         
        // create tabs
        JTabbedPane tabbedPane = new JTabbedPane();
        
        tabbedPane.addTab("Home", homePanel);
        tabbedPane.addTab("Sender", senderPanel);
        tabbedPane.addTab("Receiver", receiverPanel);
        tabbedPane.addChangeListener(changeListener); // listening tabchanges
       
        // add to main Panel
        add(topPanel, BorderLayout.NORTH);
        add(tabbedPane, BorderLayout.CENTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
    	
        // Handle template button action
    	
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

            
        ////////// SENDER SIDE    
        // Handle (open folder to send) button action.
    	} else if (e.getSource() == openToSendButton) {
            int returnVal = folderChooser.showSaveDialog(AISSInterface.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	logsender.append("Opening..." + newline);
            	File file = folderChooser.getCurrentDirectory();
            	emailDir = file.getPath();
                logsender.append("Opened: " + emailDir + "." + newline);
            } else {
                logsender.append("Open command cancelled by user." + newline);
            }      	
        // Handle sign checkbox
        } else if (e.getSource() == signCB){
        	sign = (sign == false) ? true: false;
        	String state = (sign == false) ? "disabled" : "enabled";
        	logsender.append("Sign is: " + state + newline);
        	
        // Handle cipher checkbox
        } else if (e.getSource() == cipherCB){
        	cipher = (cipher == false) ? true: false;
        	String state = (cipher == false) ? "disabled" : "enabled";
        	logsender.append("Cipher is: " + state + newline);
        
        // Handle timestamp checkbox
        } else if (e.getSource() == timestampCB){
        	timestamp = (timestamp == false) ? true: false;
        	String state = (timestamp == false) ? "disabled" : "enabled";
        	logsender.append("TimeStamp is: " + state + newline);
        
        // Handle SAVE sender - HERE IS THE REAL ACTION IN SENDER
        } else if (e.getSource() == saveSenderButton){
        	// save the file
        	int returnVal = fileChooser.showSaveDialog(AISSInterface.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fileChooser.getSelectedFile();
                String path = file.getPath();
                if(path.toLowerCase().endsWith(".aiss")){
                	// Do the job
                	try {
						Sender.begin(sign, cipher, timestamp, emailDir, path);
					} catch (Exception e1) {
						// erro
						logsender.append("SAVE - ERROR: operation was not concluded and file was not saved: " + e1);
					}
                	// log
                	logsender.append("SAVE: the file " + path + " was sucessefully saved." + newline);
                }else{
                	logsender.append("SAVE: File not saved. Please choose an .aiss extension name, for example, mail.aiss");
                }
            } else {
                logsender.append("Save command cancelled by user." + newline);
            }
            
        /////////// RECEIVER SIDE    
        // Handle (open receiver) button action.
    	} else if (e.getSource() == openReceivedButton) {
            int returnVal = fileChooser.showSaveDialog(this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	logreceiver.append("Opening..." + newline);
            	File file = folderChooser.getCurrentDirectory();
            	emailDir = file.getPath();
                logreceiver.append("Opened: " + emailDir + "." + newline);
                // begin of cipher or assigned or verify TS
            } else {
                logreceiver.append("Open command cancelled by user." + newline);
            }
            log.setCaretPosition(log.getDocument().getLength());
        	
        // Handle SAVE receiver
        } else if (e.getSource() == saveReceiverButton){
        	// save the file
        	int returnVal = folderChooser.showSaveDialog(AISSInterface.this);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
            	File file = folderChooser.getCurrentDirectory();
            	emailDir = file.getPath();
            	// receiver
            	// Receiver.begin(, emailDir);
            	logsender.append("SAVE: content was saved to " + emailDir + "with success." + newline);
                }
            } else {
                logsender.append("Save command cancelled by user." + newline);
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
        JFrame frame = new JFrame("AISS Security Email Service");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        AISSInterface aissinterf = new AISSInterface();
        aissinterf.setPreferredSize(new Dimension(640, 480));
        // Add content to the window.
        frame.add(aissinterf);

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
