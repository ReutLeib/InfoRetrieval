package view;

import annotations.A;
import controller.AppController;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static javax.swing.ListSelectionModel.*;

public class MainGui {

    private AppController   appCtrl;

    private JPanel mainPanel;
    private JTable tableIndexDocResults;
    private JComboBox jcbDocNameResults;
    private JTextField tfSearchLine;
    private JButton btnSearch;
    private JComboBox jcbAddDoc;
    private JComboBox jcbRemoveDoc;
    private JButton btnAddDoc;
    private JButton btnRemoveDoc;
    private JPasswordField pfAdminPassword;
    private JButton btnLoginAsAdmin;
    private JLabel lblAppName;
    private JLabel lblLoggedAs;
    private JLabel lblSystemMsg;
    private JScrollPane jspDocResTable;
    private JTextArea taFullDocContent;
    private JTextArea taDocSummery;
    private JButton btnResetAppDb;
    private JList listAddDocs;
    private JLabel lblDocList;
    private JFrame mainFrame;
    private DefaultTableModel modelIndexDocResults;
    private ArrayList<String[]> records;
    private DefaultListModel listModel = new DefaultListModel();


    public static final String LOG_OUT = "Log Out";
    public static final String LOG_IN  = "Log In";
    public static final String WRONG_PASSWORD = "Wrong password.";
    public static final String LOGGED_AS_VISITOR = "Logged as: Visitor";
    public static final String LOGGED_AS_ADMIN = "Logged as: Admin";

    private HelpWindow helpWindow = new HelpWindow();

    public MainGui() throws FileNotFoundException {
        initMainFrame();
        initButtons();
        initButtonListeners();
        initComboxBoxes();
        initLists();

        initComboxBoxesListeners();
        initTables();
        initTextArea();

    }

    private void initLists() {
        listAddDocs.setModel(listModel);
        listAddDocs.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        listAddDocs.setEnabled(false);


    }

    private void initTextArea() {
        taDocSummery.setEditable(false);
        taFullDocContent.setEditable(false);
    }

    private void initTables() {
        final String[] tbIndexDocResColumns = {"Word","Doc Id","Appearances"};
        modelIndexDocResults = new DefaultTableModel(null,tbIndexDocResColumns);
        tableIndexDocResults.setBackground(new Color(255,255,255));
        tableIndexDocResults.setModel(modelIndexDocResults);
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment( JLabel.CENTER );

        tableIndexDocResults.getColumnModel().getColumn(0).setCellRenderer(centerRenderer);
        tableIndexDocResults.getColumnModel().getColumn(1).setCellRenderer(centerRenderer);
        tableIndexDocResults.getColumnModel().getColumn(2).setCellRenderer(centerRenderer);

    }

    private void resetTableRecords(){
        modelIndexDocResults.setRowCount(0);
    }

    private void initMainFrame() {
        mainFrame = new JFrame("IR System - by team_pwnz (c) ");
        //mainFrame.setSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize()));
        mainFrame.setSize(1200,800);
        mainFrame.setContentPane(this.getMainPanel());
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setVisible(true);

        //center window
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        int x = (int) ( 150 + (dimension.getWidth() - mainFrame.getWidth()) / 2);
        int y = (int) ((dimension.getHeight() - mainFrame.getHeight()) / 2);
        mainFrame.setLocation(x, y);

        //set enter as search button default stroke
        mainFrame.getRootPane().setDefaultButton(btnSearch);

        //changeTheme(mainFrame);


    }

    private void changeTheme(JFrame mainFrame) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (ClassNotFoundException e) {
            //e.printStackTrace();
        } catch (InstantiationException e) {
            //e.printStackTrace();
        } catch (IllegalAccessException e) {
            //e.printStackTrace();
        } catch (UnsupportedLookAndFeelException e) {
            //e.printStackTrace();
        }
        SwingUtilities.updateComponentTreeUI(mainFrame);
    }


    public void initButtons(){

        btnLoginAsAdmin.setText(LOG_IN);
        btnAddDoc.setEnabled(false);
        btnRemoveDoc.setEnabled(false);
        btnResetAppDb.setEnabled(false);
    }

    public void initComboxBoxes(){

        jcbDocNameResults.setEnabled(true);
        jcbAddDoc.setEnabled(false);
        jcbRemoveDoc.setEnabled(false);

        defaultComboBoxHeader(jcbDocNameResults,"Result");
        defaultComboBoxHeader(jcbAddDoc,"Document");
        defaultComboBoxHeader(jcbRemoveDoc,"Document");

    }

    public void initComboxBoxesListeners() throws FileNotFoundException{

        jcbDocNameResults.addActionListener(e -> {
            if(jcbDocNameResults.getSelectedIndex() > 1){
                String path = fetchFilePath(jcbDocNameResults.getSelectedItem().toString());
                try {
                    //clear any content displayed before
                    taFullDocContent.setText("");
                    displayFileContent(path);

                } catch (FileNotFoundException e1) {
                    e1.printStackTrace();
                }
            }
        });

        jcbAddDoc.addActionListener(e -> {
//            if(jcbAddDoc.getSelectedIndex() > 1){
//                btnAddDoc.setEnabled(true);
//            }
//            else
//                btnAddDoc.setEnabled(false);
        });

        jcbRemoveDoc.addActionListener(e -> {
            if(jcbRemoveDoc.getSelectedIndex() > 1){
                btnRemoveDoc.setEnabled(true);
            }
            else
                btnRemoveDoc.setEnabled(false);
        });
    }

    private void displayFileContent(String path) throws FileNotFoundException {
        System.out.println("displayFileContent():\t\tcalled.");
        System.out.println("displayFileContent():\t\tpath="+path);

        Scanner itr = new Scanner(new File(path));
        String line,word;
        StringBuilder fullFileContent = new StringBuilder();
        ArrayList<Integer> pos = new ArrayList<>();


        taFullDocContent.setText("");
        while(itr.hasNextLine()){
            line = itr.nextLine();
            fullFileContent.append(line.toLowerCase() + "\n");
            taFullDocContent.setText(taFullDocContent.getText() + "\n" + line);

            //get all word positions in line to later be highlighted:


        }

        //TODO: highlight is going over all words in table, even when its not needed in certain docs. (union results)
        for (int i = 0; i < tableIndexDocResults.getRowCount() ; i++) {
            word = tableIndexDocResults.getValueAt(i,0).toString();
            pos = getWordPositions(pos,word,fullFileContent);
            highlightInTextArea(taFullDocContent,word,pos);
        }


    }

    private String removePunctuation(String word) {
        return word.replaceAll("(?=[^a-zA-Z0-9])([^'])", "");
    }

    private void displayDocSummery(ArrayList<String[]> records) {

        System.out.println("displayDocSummery: called.");
        taDocSummery.setText("");
        for(String[] record : records){
            try {
                System.out.println("displayDocSummery: attempting to open: " + record[4] );
                Scanner itr = new Scanner(new File(record[4]));

                String fileNameWithoutExtension = record[3].substring(0,record[3].length()-4);

                //if this doc is already summarized, do not display again.
                if (!taDocSummery.getText().contains(fileNameWithoutExtension)) {
                    taDocSummery.setText(taDocSummery.getText() + fileNameWithoutExtension + " : \n" );
                    taDocSummery.setText(taDocSummery.getText() + "===============================\n");

                    for(int i = 0 ; i < 3 && itr.hasNextLine() ; i++){
                        String line = itr.nextLine();
                        if(line.equals("") || line.equals('\n')) {
                            --i;
                            continue;
                        }
                        if(i == 2)
                            line += " .....";
                        taDocSummery.setText(taDocSummery.getText() + line + "\n");
                    }
                    taDocSummery.setText(taDocSummery.getText() + "\n");
                }
            } catch (FileNotFoundException e) {
                System.out.println("displayDocSummery. failed to open file");
                e.printStackTrace();
            }
        }
    }

    private void highlightInTextArea(JTextArea taFullDocContent, String phrase, ArrayList<Integer> pos) {
        Highlighter highlighter = taFullDocContent.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.yellow);

        for(Integer posStart : pos) {
            //System.out.println("phrase:"+phrase + "," + "line:"+line + ":" + posStart+","+posEnd);
            try {
                highlighter.addHighlight(posStart+1, posStart+phrase.length()+1, painter);

            } catch (BadLocationException e) {
                System.out.println("highlightInTextArea called. bad location exception occurred.");
                e.printStackTrace();
            }
        }

    }

    private ArrayList<Integer> getWordPositions(ArrayList<Integer> pos, String word, StringBuilder line) {

        //regex to match exact phrase/word in line
        Matcher m = Pattern.compile("(?=(\\b"+ word + "\\b))").matcher(line);   // the \b is for exact word boundaries

        while (m.find())
            pos.add(m.start());

        return pos;
    }

    private String fetchFilePath(String s) {
        System.out.println("fetchFilePath: called. fileName="+s);
        String path = null;
        String id = s.substring(s.indexOf("(")+1,s.indexOf(")"));

        for(String[] record : records){
            if(record[1].equals(id))
                path = record[4];
        }

        return path;
    }


    private void initButtonListeners() {

        //Login/Logout button:
        btnLoginAsAdmin.addActionListener(e -> {

            if(appCtrl.isLoggedAsAdmin()){
                //already logged as admin, downgrade to visitor access
                setLblSystemMsg("");
                btnLoginAsAdmin.setText(LOG_IN);
                appCtrl.setAdminAccess(false);
                getPfAdminPassword().setEnabled(true);
                getPfAdminPassword().setBackground(Color.WHITE);
                setLblLoggedAs(LOGGED_AS_VISITOR);

                jcbAddDoc.setEnabled(false);
                jcbRemoveDoc.setEnabled(false);
                btnResetAppDb.setEnabled(false);
                listAddDocs.setEnabled(false);
                btnAddDoc.setEnabled(false);

            }
            else {
                //logged as visitor
                String pass = getPfAdminPassword().getText().toString();
                if(appCtrl.verifyAdminPass(pass)){
                    //password verification succeeds, allowing admin access.
                    setLblLoggedAs(LOGGED_AS_ADMIN);
                    btnLoginAsAdmin.setText(LOG_OUT);
                    appCtrl.setAdminAccess(true);
                    getPfAdminPassword().setText("");
                    getPfAdminPassword().setEnabled(false);
                    getPfAdminPassword().setBackground(new Color(193, 196, 201));
                    setLblSystemMsg("Hello");
                    getLblSystemMsg().setForeground(new Color(13, 163, 8));

                    jcbAddDoc.setEnabled(true);
                    jcbRemoveDoc.setEnabled(true);
                    btnResetAppDb.setEnabled(true);
                    btnAddDoc.setEnabled(true);
                    listAddDocs.setEnabled(true);

                    try {
                        loadDbToApp();
                    } catch (SQLException e1) {
                        System.out.println("loadDbToApp called. Could not load db.");
                        e1.printStackTrace();
                    }

                }
                else {
                    //wrong password
                    setLblSystemMsg(WRONG_PASSWORD);
                    getLblSystemMsg().setForeground(Color.RED);
                }

            }
            return;
        });




        btnAddDoc.addActionListener(e -> {

            //make sure some document is chosen
            if(jcbAddDoc.getSelectedIndex() > 1) {
                String fileName = jcbAddDoc.getSelectedItem().toString();
                try {
                    addFileToStorage(fileName);
                    loadDbToApp();
                    jcbAddDoc.setSelectedIndex(0);
                } catch (FileNotFoundException e1) {
                    System.out.println("addFileToStorage called. File not found");
                    e1.printStackTrace();
                } catch (SQLException e1) {
                    e1.printStackTrace();
                }

            }
            else if(listAddDocs.getSelectedIndices().length != 0){      //selected from group
                int[] items = listAddDocs.getSelectedIndices();
                try {
                    for(int i : items){
                        addFileToStorage(listAddDocs.getModel().getElementAt(i).toString());
                    }
                    loadDbToApp();
                } catch (FileNotFoundException e1) {
                    System.out.println("addFileToStorage called. File not found");
                    e1.printStackTrace();
                } catch (SQLException e1) {

                    e1.printStackTrace();
                }
            }
            else{
                return;
            }
        });


        btnRemoveDoc.addActionListener(e -> {
            //make sure some document is chosen
            if(jcbRemoveDoc.getSelectedIndex() > 1){
                String fileAndId = jcbRemoveDoc.getSelectedItem().toString();
                try {
                    //exclude file on search results
                    removeFileFromStorage(fileAndId);
                    loadDbToApp();
                    jcbRemoveDoc.setSelectedIndex(0);

                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });

        //Search button:
        btnSearch.addActionListener(e -> {


            if(/* some validation on string */ true){


                //clear any content displayed before
                defaultComboBoxHeader(jcbDocNameResults,"Result");
                clearFullDocHighlights(taFullDocContent);
                taDocSummery.setText("");
                taFullDocContent.setText("");


                String searchQuery = getTfSearchLine().getText().toString();
                resetTableRecords();
                try {
                    records = appCtrl.search(searchQuery);
                    if(null != records && !records.isEmpty()){
                        loadRecordsIntoTable(records);
                        displayDocSummery(records);
                        displayDocsInComboBox(records);

                    }
                    else{
                        System.out.println("no records available.");
                        return;
                    }


//                    System.out.println("FROM GUI: ");
//                    for(String[] str : records){
//                        for(String s : str){
//                            System.out.println(s);
//                        }
//                    }


                } catch (SQLException e1) {
                    e1.printStackTrace();
                }
            }
        });




        btnResetAppDb.addActionListener(e -> {
            try {
                appCtrl.reset();
            } catch (SQLException e1) {
                e1.printStackTrace();
            }
        });
    }



    public void clearFullDocHighlights(JTextArea textArea) {
        Highlighter highlighter = textArea.getHighlighter();
        Highlighter.HighlightPainter painter = new DefaultHighlighter.DefaultHighlightPainter(Color.white);
        highlighter.removeAllHighlights();
    }

    private void displayDocsInComboBox(ArrayList<String[]> records) {
        //get id and name

        for(String[] record : records){
            String name = record[3];
            String nameAndId = record[3] + " (" + record[1] +")";

            if(((DefaultComboBoxModel)jcbDocNameResults.getModel()).getIndexOf(nameAndId) == -1) {
                jcbDocNameResults.addItem(nameAndId);
            }
        }
    }

    private void loadRecordsIntoTable(ArrayList<String[]> records) {
        if(records != null){
            for(String[] record : records){
                if(record != null)
                    modelIndexDocResults.addRow(record);
            }
        }
    }

    public void defaultComboBoxHeader(JComboBox jBox, String type){
        if(null != jBox){
            jBox.removeAllItems();
            jBox.addItem("Select " + type);
            jBox.addItem("");
        }
    }


    @A.DBOperation
    public void loadDbToApp() throws SQLException {

        String[] docList;
        String fixedSourceString = null;
        String delim = appCtrl.getDb().DELIM;

        //get all available source files and display them on associated combo box:
        docList = appCtrl.getAvailableSourceFiles();
        defaultComboBoxHeader(jcbAddDoc,"Document");
        clearDocList(listModel);

        for(String doc : docList){
            //doc = doc.substring(0,doc.length()-4);
            jcbAddDoc.addItem(doc);
            listModel.addElement(doc);
        }

        //get removed docs back into add area
        docList = appCtrl.getAvailableStorageFiles();
        for(String doc : docList){
            //doc = doc.substring(0,doc.length()-4);
            jcbAddDoc.addItem(doc);
            listModel.addElement(doc);
        }

        //get all storage files and display them on associated combo box:
        docList = appCtrl.getLocalStorageFiles();
        defaultComboBoxHeader(jcbRemoveDoc,"Document");
        for(String s: docList)
            jcbRemoveDoc.addItem(s);

    }

    private void clearDocList(DefaultListModel model) {
        model.setSize(0);
    }


    @A.AdminOperation
    @A.DBOperation
    public void removeFileFromStorage(String fileAndId) throws SQLException {
        appCtrl.removeFileFromStorage(fileAndId);
    }


    @A.AdminOperation
    @A.DBOperation
    public void addFileToStorage(String fileName) throws FileNotFoundException, SQLException {

        appCtrl.addFileToStorage(fileName);
    }



    public JPasswordField getPfAdminPassword() {
        return pfAdminPassword;
    }

    public JPanel getMainPanel() {
        return mainPanel;
    }

    public void setAppCtrl(AppController guiCtrl) {
        this.appCtrl = guiCtrl;
    }

    public JLabel getLblLoggedAs() {
        return lblLoggedAs;
    }

    public void setLblLoggedAs(String lblLoggedAs) {
        this.lblLoggedAs.setText(lblLoggedAs);
    }

    public JLabel getLblSystemMsg() {
        return lblSystemMsg;
    }

    public void setLblSystemMsg(String lblSystemMsg) {
        this.lblSystemMsg.setText(lblSystemMsg);
    }

    public JTextField getTfSearchLine() {
        return tfSearchLine;
    }

    public DefaultListModel getListModel() {
        return listModel;
    }
}
