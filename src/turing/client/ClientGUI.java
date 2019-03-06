package turing.client;

import turing.ClientNotificationManagerAPI;
import turing.ServerNotificationManagerAPI;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

/**
 * Implements the turing client Graphical User Interface
 */
public class ClientGUI extends JFrame {
	private Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

	private Connection connection;

	private ArrayList<Document> documents;
	private DefaultTableModel documentsTableModel, sectionsTableModel;
	private Document lastSelectedDocument = null;
	private int lastSelectedSection = -1;

	/**
	 * Creates the Graphical User Interface
	 */
	public ClientGUI() {
		super("Turing");

		// TEST ********************************************************************************************************
		try {
			Registry registry = LocateRegistry.getRegistry(Client.HOST);
			ServerNotificationManagerAPI aaa = (ServerNotificationManagerAPI) registry.lookup(Client.NOTIFICATION_OBJECT);
			ClientNotificationManager listener = new ClientNotificationManager();
			ClientNotificationManagerAPI stub = (ClientNotificationManagerAPI) UnicastRemoteObject.exportObject(listener, 0);
			aaa.registerForCallback(stub);
		} catch (Exception e) {
			e.printStackTrace();
		}
		// TEST ********************************************************************************************************

		// try to connect with the server
		try {
			connection = new Connection(Client.DEFAULT_ADDRESS);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Client.frame, "Can't connect to the server");
			System.exit(0);
		}

		Operation.setConnection(connection);
		documents = new ArrayList<>();

		int width = (int) (screen.width * 0.3);
		int height = (int) (screen.height * 0.25);
		setBounds(screen.width / 2 - width / 2, screen.height / 2 - height, width, height);

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
		signupButton.addActionListener(event -> connection.signUp(usernameField.getText().trim(), passwordField.getText()));
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

		// prompt to confirm program closing
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				if (JOptionPane.showConfirmDialog(null, "Close turing?", "Quit",
						JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.YES_OPTION)
					System.exit(0);
			}
		});
	}

	/**
	 * Creates the workspace window
	 */
	public void createWorkspace() {
		setVisible(false);
		getContentPane().removeAll();
		int width = (int) (screen.width * 0.8);
		int height = (int) (screen.height * 0.8);
		setBounds(screen.width/2 - width/2, screen.height/2 - height/2, width, height);
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
		JButton showDocumentButton = new JButton("Show document");
		JButton showSectionButton = new JButton("Show section");
		JButton editSectionButton = new JButton("Edit section");
		JButton inviteButton = new JButton("Invite");
		JButton refreshButton = new JButton("Refresh");
		createDocumentButton.addActionListener(event -> createDocument());
		//showDocumentButton.addActionListener(event -> showDocument());
		//showSectionButton.addActionListener(event -> showSection());
		editSectionButton.addActionListener(event -> Operation.editSection(lastSelectedDocument, lastSelectedSection));
		//endEditButton.addActionListener(event -> foo());
		//inviteButton.addActionListener(event -> bar());
		refreshButton.addActionListener(event -> Operation.list());
		buttonsPanel.add(createDocumentButton);
		buttonsPanel.add(showDocumentButton);
		buttonsPanel.add(showSectionButton);
		buttonsPanel.add(editSectionButton);
		buttonsPanel.add(inviteButton);
		buttonsPanel.add(refreshButton);

		// sectionsPanel
		String[] sectionsTableColumns = new String[] {"N°", "Field 2", "Field 3"};
		sectionsTableModel = new DefaultTableModel(sectionsTableColumns, 0);
		JTable sectionsTable = new JTable(sectionsTableModel);
		sectionsTable.setDefaultEditor(Object.class, null); // set table not editalbe
		sectionsTable.getTableHeader().setReorderingAllowed(false); // block column ordering
		sectionsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // force to select only one row
		sectionsTable.getSelectionModel().addListSelectionListener(event -> {
			if (event.getValueIsAdjusting())
				lastSelectedSection = sectionsTable.getSelectedRow();
		});
		sectionsPanel.add(new JScrollPane(sectionsTable));

		// documentsPanel
		String[] documentsTableColumns = new String[] {"Name", "Creator", "Active users", "Shared"};
		documentsTableModel = new DefaultTableModel(documentsTableColumns, 0);
		JTable documentsTable = new JTable(documentsTableModel);
		documentsTable.setDefaultEditor(Object.class, null); // set table not editalbe
		documentsTable.getTableHeader().setReorderingAllowed(false); // block column ordering
		documentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // force to select only one row
		documentsTable.getSelectionModel().addListSelectionListener(event -> {
			if (event.getValueIsAdjusting())
				updateSectionsTable(documentsTable.getSelectedRow());
			sectionsTable.clearSelection();
		});
		documentsPanel.add(new JScrollPane(documentsTable));

		// centerPanel
		centerPanel.add(documentsPanel);
		centerPanel.add(sectionsPanel);

		// workspace
		add(buttonsPanel, BorderLayout.WEST);
		add(centerPanel, BorderLayout.CENTER);
		setVisible(true);
	}

	/**
	 * Creates the document editing window
	 */
	public void createEditingSpace(String documentText) {
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
		buttonsPanel.add(endEditButton);
		buttonsPanel.add(discardChangesButton);

		// editingPanel
		editingArea.setWrapStyleWord(true);
		JScrollPane editingScroll = new JScrollPane (editingArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		editingPanel.add(editingScroll, BorderLayout.CENTER);

		// chatPanel
		JTextArea chatArea = new JTextArea("No messages yet", 32, 24);
		JTextField chatField = new JTextField("Message");
		JButton sendButton = new JButton("Send");
		chatArea.setEditable(false);
		JScrollPane chatScroll = new JScrollPane (chatArea,
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		sendButton.addActionListener(event -> Operation.sendMessage(chatField.getText()));
		chatPanel.add(chatScroll, BorderLayout.NORTH);
		chatPanel.add(chatField, BorderLayout.CENTER);
		chatPanel.add(sendButton, BorderLayout.SOUTH);

		add(buttonsPanel, BorderLayout.WEST);
		add(editingPanel, BorderLayout.CENTER);
		add(chatPanel, BorderLayout.EAST);
		setVisible(true);
	}

	/**
	 * Creates the document creation window
	 */
	private void createDocument() {
		JPanel window = new JPanel();
		JLabel documentNameLabel = new JLabel("Document name");
		JLabel sectionsLabel = new JLabel("Number of sections");
		JTextField documentNameField = new JTextField(10);

		// force number digits
		NumberFormatter numberFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		numberFormatter.setValueClass(Integer.class);
		numberFormatter.setAllowsInvalid(false);
		numberFormatter.setMinimum(1);
		numberFormatter.setMaximum(1000);
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
			JOptionPane.showMessageDialog(Client.frame, "You must compile all fields");
			return;
		}

		// creating document
		Operation.createDocument(documentName, sections);
	}

	/**
	 * Adds a document in the documents table and in the documents list
	 */
	public void addDocument(Document document) {
		Object[] tableData = {document.getName(), document.getCreator(), "n.d.", document.isShared()}; // TODO: unused fields
		documentsTableModel.addRow(tableData);
		documents.add(document);
	}

	/**
	 * Updates the sections table with the selected document's sections
	 */
	private void updateSectionsTable(int documentIndex) {
		sectionsTableModel.setRowCount(0);
		lastSelectedDocument = documents.get(documentIndex);
		lastSelectedSection = -1;
		for (int i = 0; i < documents.get(documentIndex).getSections(); i++) {
			Object[] data = {documents.get(documentIndex).getName() + " - section " + (i + 1), "n.d."}; // TODO: unused field
			sectionsTableModel.addRow(data);
		}
	}

	/**
	 * Shows an error window with an error message and description
	 */
	public void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(Client.frame, message,"Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Clears the tables and the documents list
	 */
	public void clearTables() {
		documentsTableModel.setRowCount(0);
		sectionsTableModel.setRowCount(0);
		lastSelectedDocument = null;
		documents.clear();
		lastSelectedSection = -1;
	}
}
