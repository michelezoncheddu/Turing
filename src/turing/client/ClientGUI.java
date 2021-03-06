package turing.client;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.InetAddress;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

/**
 * Implements the turing client Graphical User Interface
 */
public class ClientGUI extends JFrame {
	private Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize(); // screen size

	private Connection   connection = null;   // connection with the server
	private ChatListener chatListener = null; // chat background thread

	private ArrayList<Document> documents = new ArrayList<>(); // document list

	private DefaultTableModel documentsTableModel, sectionsTableModel; // table models

	private Document lastSelectedDocument = null; // last selected document in the document table
	private int      lastSelectedSection  = -1;   // index of the last selected section in the section table

	private ImageIcon sharedIcon, notSharedIcon; // table icons

	String username = null; // current username

	/**
	 * Creates the Graphical User Interface
	 */
	public ClientGUI() {
		// load shared icon
		sharedIcon = new ImageIcon("icons/shared_icon.png");
		notSharedIcon = new ImageIcon("icons/not_shared_icon.png");
		sharedIcon = new ImageIcon(sharedIcon.getImage().getScaledInstance(
				16, 16, java.awt.Image.SCALE_SMOOTH));
		notSharedIcon = new ImageIcon(notSharedIcon.getImage().getScaledInstance(
				16, 16, java.awt.Image.SCALE_SMOOTH));

		// prompt to confirm program closing
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (JOptionPane.showConfirmDialog(Client.frame, "Close turing?", "Quit",
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) == JOptionPane.YES_OPTION) {
					if (connection != null)
						connection.close(); // close the connection with the server
					System.exit(0);
				}
			}
		});

		startConnection();
		showLoginWindow();
	}

	/**
	 * Connects the client with the server
	 */
	private void startConnection() {
		// try to connect with the server
		try {
			connection = new Connection(Client.DEFAULT_ADDRESS);
		} catch (IOException e) {
			showErrorDialog("Can't connect to the server");
			System.exit(0);
		}

		// sets statically the connection to the operations stubs
		Operation.setConnection(connection);
	}

	/**
	 * Creates the login window
	 */
	public void showLoginWindow() {
		setTitle("Turing - Login");
		setVisible(false);
		getContentPane().removeAll();
		int width = 350;
		int height = 200;
		setBounds(screenSize.width / 2 - width / 2, screenSize.height / 2 - height, width, height);

		// panels
		JPanel headPanel = new JPanel();
		JPanel usernamePanel = new JPanel();
		JPanel passwordPanel = new JPanel();
		JPanel buttonsPanel = new JPanel();
		headPanel.setLayout(new BorderLayout());

		// headPanel
		JLabel headLabel = new JLabel("A distributed collaborative editing software", SwingConstants.CENTER);
		headPanel.add(headLabel, BorderLayout.CENTER);

		// usernamePanel
		JLabel usernameLabel = new JLabel("Username", SwingConstants.RIGHT);
		usernameLabel.setPreferredSize(new Dimension(70, 20));
		JTextField usernameField = new JTextField(16);
		usernamePanel.add(usernameLabel);
		usernamePanel.add(usernameField);

		// passwordPanel
		JLabel passwordLabel = new JLabel("Password", SwingConstants.RIGHT);
		passwordLabel.setPreferredSize(new Dimension(70, 20));
		JTextField passwordField = new JPasswordField(16);
		passwordPanel.add(passwordLabel);
		passwordPanel.add(passwordField);

		// buttonsPanel
		JButton signupButton = new JButton("Sign up");
		JButton loginButton = new JButton("Log in");
		signupButton.addActionListener(event -> Operation.signUp(usernameField.getText().trim(), passwordField.getText()));
		loginButton.addActionListener(event -> Operation.logIn(usernameField.getText().trim(), passwordField.getText()));
		buttonsPanel.add(signupButton);
		buttonsPanel.add(loginButton);

		// login frame
		setLayout(new GridLayout(4, 1));
		add(headPanel);
		add(usernamePanel);
		add(passwordPanel);
		add(buttonsPanel);
		setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		setVisible(true);
	}

	/**
	 * Creates the workspace window
	 */
	public void showWorkspace() {
		setTitle("Turing - " + username);

		// terminates the chat thread, if any
		if (chatListener != null) {
			chatListener.shutdown();
			chatListener = null;
		}

		setVisible(false);
		getContentPane().removeAll();
		int width = (int) (screenSize.width * 0.8);
		int height = (int) (screenSize.height * 0.8);
		setBounds(screenSize.width/2 - width/2, screenSize.height/2 - height/2, width, height);
		setLayout(new BorderLayout());

		// panels
		JPanel buttonsPanel = new JPanel();
		JPanel centerPanel = new JPanel();
		JPanel documentsPanel = new JPanel();
		JPanel sectionsPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		centerPanel.setLayout(new GridLayout(2, 1, 0, 10));
		documentsPanel.setLayout(new BorderLayout());
		sectionsPanel.setLayout(new BorderLayout());

		// buttonsPanel
		JButton createDocumentButton = new JButton("Create document");
		JButton showDocumentButton   = new JButton("Show document");
		JButton showSectionButton    = new JButton("Show section");
		JButton editSectionButton    = new JButton("Edit section");
		JButton inviteButton         = new JButton("Invite");
		JButton refreshButton        = new JButton("Refresh");
		JButton logoutbutton         = new JButton("Logout");
		createDocumentButton.addActionListener(event -> showDocumentDialog());
		showDocumentButton.addActionListener(event ->   Operation.showDocument(lastSelectedDocument));
		showSectionButton.addActionListener(event ->    Operation.showSection(lastSelectedDocument, lastSelectedSection));
		editSectionButton.addActionListener(event ->    Operation.editSection(lastSelectedDocument, lastSelectedSection));
		inviteButton.addActionListener(event ->         showInviteWindow());
		refreshButton.addActionListener(event ->        Operation.list());
		logoutbutton.addActionListener(event ->         Operation.logout());
		createDocumentButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		showDocumentButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		showSectionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		editSectionButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		inviteButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		refreshButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		logoutbutton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(createDocumentButton);
		buttonsPanel.add(showDocumentButton);
		buttonsPanel.add(showSectionButton);
		buttonsPanel.add(editSectionButton);
		buttonsPanel.add(inviteButton);
		buttonsPanel.add(refreshButton);
		buttonsPanel.add(logoutbutton);

		// sectionsPanel
		String[] sectionsTableColumns = new String[] {"Section"};
		sectionsTableModel = new DefaultTableModel(sectionsTableColumns, 0);
		JTable sectionsTable = new JTable(sectionsTableModel);
		sectionsTable.setDefaultEditor(Object.class, null);            // set table not editalbe
		sectionsTable.getTableHeader().setReorderingAllowed(false);           // block column ordering
		sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // force to select only one row
		sectionsTable.getSelectionModel().addListSelectionListener(event -> { // detects the click
			if (event.getValueIsAdjusting())
				lastSelectedSection = sectionsTable.getSelectedRow();
		});
		sectionsPanel.add(new JScrollPane(sectionsTable));

		// documentsPanel
		String[] documentsTableColumns = new String[] {"Name", "Creator", "Shared"};
		documentsTableModel = new DefaultTableModel(documentsTableColumns, 0);
		JTable documentsTable = new JTable(documentsTableModel) {
			public Class getColumnClass(int column) {
				return getValueAt(0, column).getClass(); // to render table icons
			}
		};
		documentsTable.setDefaultEditor(Object.class, null);            // set table not editalbe
		documentsTable.getTableHeader().setReorderingAllowed(false);           // lock column ordering
		documentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);  // force to select only one row
		documentsTable.getSelectionModel().addListSelectionListener(event -> { // detects the click
			if (event.getValueIsAdjusting())
				updateSectionsTable(documentsTable.getSelectedRow());
			sectionsTable.clearSelection();
		});
		documentsPanel.add(new JScrollPane(documentsTable));

		// centerPanel
		centerPanel.add(documentsPanel);
		centerPanel.add(sectionsPanel);

		// to fill the document table with the updated documents
		updateDocumentsTable();

		// workspace
		add(buttonsPanel, BorderLayout.WEST);
		add(centerPanel, BorderLayout.CENTER);
		setVisible(true);
	}

	/**
	 * Creates the document editing window
	 *
	 * @param documentText the document content
	 * @param chatAddress  the chat IP address
	 */
	public void showEditingWindow(String documentText, InetAddress chatAddress) {
		setTitle("Turing - editing " + lastSelectedDocument.getName() + ", section " + (lastSelectedSection + 1));
		setVisible(false);
		getContentPane().removeAll();
		setLayout(new BorderLayout());
		JTextArea editingArea = new JTextArea(documentText);

		// panels
		JPanel buttonsPanel = new JPanel();
		JPanel editingPanel = new JPanel();
		JPanel chatPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		editingPanel.setLayout(new BorderLayout());
		chatPanel.setLayout(new BorderLayout());

		// buttonsPanel
		JButton endEditButton = new JButton("End edit");
		JButton discardChangesButton = new JButton("Discard changes");
		endEditButton.addActionListener(event -> Operation.endEdit(editingArea.getText()));
		discardChangesButton.addActionListener(event -> Operation.endEdit(null));
		endEditButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		discardChangesButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(endEditButton);
		buttonsPanel.add(discardChangesButton);

		// editingPanel
		editingArea.setWrapStyleWord(true);
		JScrollPane editingScroll = new JScrollPane (
				editingArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		editingPanel.add(editingScroll, BorderLayout.CENTER);

		// chatPanel
		JTextArea chatArea = new JTextArea(32, 24);
		JTextField chatField = new JTextField("Message");
		JButton sendButton = new JButton("Send");
		chatArea.setEditable(false);
		JScrollPane chatScroll = new JScrollPane (
				chatArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sendButton.addActionListener(event -> Operation.sendMessage(chatField));
		chatPanel.add(chatScroll, BorderLayout.NORTH);
		chatPanel.add(chatField, BorderLayout.CENTER);
		chatPanel.add(sendButton, BorderLayout.SOUTH);

		// start chat background thread
		chatListener = new ChatListener(chatAddress, chatArea);
		new Thread(chatListener).start();

		// editing window
		add(buttonsPanel, BorderLayout.WEST);
		add(editingPanel, BorderLayout.CENTER);
		add(chatPanel, BorderLayout.EAST);
		setVisible(true);
	}

	/**
	 * Creates the document/section showing window
	 *
	 * @param documentText the document content
	 */
	public void showDocumentWindow(String documentText) {
		if (lastSelectedSection < 0)
			setTitle("Turing - showing " + lastSelectedDocument.getName());
		else
			setTitle("Turing - showing " + lastSelectedDocument.getName() + ", section " + (lastSelectedSection + 1));
		setVisible(false);
		getContentPane().removeAll();
		setLayout(new BorderLayout());
		JTextArea editingArea = new JTextArea(documentText);

		// panels
		JPanel buttonsPanel = new JPanel();
		JPanel documentPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		documentPanel.setLayout(new BorderLayout());

		// buttonsPanel
		JButton backButton = new JButton("Back");
		backButton.addActionListener(event -> showWorkspace());
		backButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonsPanel.add(backButton);

		// documentPanel
		editingArea.setWrapStyleWord(true);
		editingArea.setEditable(false);
		JScrollPane editingScroll = new JScrollPane (
				editingArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		documentPanel.add(editingScroll, BorderLayout.CENTER);

		// document window
		add(buttonsPanel, BorderLayout.WEST);
		add(documentPanel, BorderLayout.CENTER);
		setVisible(true);
	}

	/**
	 * Creates the document creation dialog window
	 */
	private void showDocumentDialog() {
		// components
		JPanel window = new JPanel();
		JLabel documentNameLabel = new JLabel("Document name");
		JLabel sectionsLabel = new JLabel("Number of sections");
		JTextField documentNameField = new JTextField(10);

		// force number digits
		NumberFormatter numberFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		numberFormatter.setValueClass(Integer.class);
		numberFormatter.setAllowsInvalid(false);
		numberFormatter.setMinimum(1);
		numberFormatter.setMaximum(100);
		JFormattedTextField sectionsField = new JFormattedTextField(numberFormatter);
		sectionsField.setColumns(10);

		// creating dialog window
		window.add(documentNameLabel);
		window.add(documentNameField);
		window.add(sectionsLabel);
		window.add(sectionsField);
		int code = JOptionPane.showConfirmDialog(Client.frame, window, "Create document",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (code != JOptionPane.OK_OPTION) // operation aborted
			return;

		// checking fields
		String documentName = documentNameField.getText();
		Integer sections = (Integer) sectionsField.getValue();
		if (documentName.isBlank() || sections == null) {
			showErrorDialog("You must compile all fields");
			return;
		}

		// creating document
		Operation.createDocument(documentName, sections);
	}

	/**
	 * Creates the invite window
	 */
	private void showInviteWindow() {
		if (lastSelectedDocument == null) {
			showErrorDialog("You must select a document to share");
			return;
		}

		JPanel window = new JPanel();
		JLabel usernameLabel = new JLabel("Username");
		JTextField usernameField = new JTextField(10);

		// creating dialog window
		window.add(usernameLabel);
		window.add(usernameField);
		int code = JOptionPane.showConfirmDialog(Client.frame, window, "Invite user",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (code != JOptionPane.OK_OPTION) // operation aborted
			return;

		// checking fields
		String username = usernameField.getText();
		if (username.isBlank()) {
			showErrorDialog("You must write an username");
			return;
		}

		// inviting user
		Operation.invite(username, lastSelectedDocument);
	}

	/**
	 * Shows an error window with an information message and description
	 *
	 * @param message the info message to show
	 */
	public void showInfoDialog(String message) {
		JOptionPane.showMessageDialog(Client.frame, message,"Info", JOptionPane.INFORMATION_MESSAGE);
	}

	/**
	 * Shows an error window with an error message and description
	 *
	 * @param message the error message to show
	 */
	public void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(Client.frame, message,"Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Adds a document in the documents table and in the documents list
	 *
	 * @param document the document to add
	 */
	public void addDocument(Document document) {
		if (documentsTableModel == null)
			return;

		addDocumentToTable(document);
		documents.add(document);
	}

	/**
	 * Adds a document to the documents table
	 *
	 * @param document the document to add
	 */
	private void addDocumentToTable(Document document) {
		Object[] tableData = {document.getName(), document.getCreator(), document.isShared() ? sharedIcon : notSharedIcon};
		documentsTableModel.addRow(tableData);
	}

	/**
	 * Updates the table with the new documents
	 */
	public void updateDocumentsTable() {
		clearTables();
		for (Document document : documents)
			addDocumentToTable(document);
	}

	/**
	 * Updates the sections table with the selected document sections
	 *
	 * @param documentIndex the document table index
	 */
	private void updateSectionsTable(int documentIndex) {
		sectionsTableModel.setRowCount(0);
		lastSelectedDocument = documents.get(documentIndex);
		lastSelectedSection = -1;
		for (int i = 0; i < documents.get(documentIndex).getSections(); i++) {
			Object[] data = {documents.get(documentIndex).getName() + " - section " + (i + 1)};
			sectionsTableModel.addRow(data);
		}
	}

	/**
	 * Clears the workspace tables and the document list
	 */
	public void clearWorkspace() {
		clearTables();
		documents.clear();
	}

	/**
	 * Clears the tables and the documents list
	 */
	private void clearTables() {
		documentsTableModel.setRowCount(0);
		sectionsTableModel.setRowCount(0);
		lastSelectedDocument = null;
		lastSelectedSection = -1;
	}
}
