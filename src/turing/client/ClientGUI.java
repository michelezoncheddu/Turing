package turing.client;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

/**
 * Implements the turing client Graphical User Interface.
 */
public class ClientGUI extends JFrame {
	private Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();

	private Connection connection;

	public ArrayList<Document> documents;
	private DefaultTableModel documentsTableModel, sectionsTableModel;
	private Document lastSelectedDocument = null;
	private int lastSelectedSection = -1;

	/**
	 * Creates the Graphical User Interface.
	 */
	public ClientGUI() {
		super("Turing");

		// try to connect with the server
		try {
			connection = new Connection(Client.DEFAULT_ADDRESS, Client.BACKGROUND_ADDRESS);
		} catch (IOException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(Client.frame, "Can't connect to the server");
			System.exit(0);
		}
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
		signupButton.addActionListener(event -> connection.signUp(usernameField.getText(), passwordField.getText()));
		loginButton.addActionListener(event -> connection.logIn(usernameField.getText(), passwordField.getText()));
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
	 * TO DO
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
		JPanel chatPanel = new JPanel();
		buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.Y_AXIS));
		centerPanel.setLayout(new GridLayout(2, 1, 0, 10));
		documentsPanel.setLayout(new BorderLayout());
		sectionsPanel.setLayout(new BorderLayout());
		chatPanel.setLayout(new BorderLayout());

		// buttonsPanel
		JButton createDocumentButton = new JButton("Create document");
		JButton showDocumentButton = new JButton("Show document");
		JButton showSectionButton = new JButton("Show section");
		JButton editSectionButton = new JButton("Edit section");
		JButton endEditButton = new JButton("End edit");
		JButton inviteButton = new JButton("Invite");
		JButton refreshButton = new JButton("Refresh");
		buttonsPanel.add(createDocumentButton);
		buttonsPanel.add(showDocumentButton);
		buttonsPanel.add(showSectionButton);
		buttonsPanel.add(editSectionButton);
		buttonsPanel.add(endEditButton);
		buttonsPanel.add(inviteButton);
		buttonsPanel.add(refreshButton);
		createDocumentButton.addActionListener(event -> createDocument());
		//showDocumentButton.addActionListener(event -> showDocument());
		//showSectionButton.addActionListener(event -> showSection());
		editSectionButton.addActionListener(event -> connection.editSection(lastSelectedSection));

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
				updateSectionTable(documentsTable.getSelectedRow());
			sectionsTable.clearSelection();
		});
		documentsPanel.add(new JScrollPane(documentsTable));

		// centerPanel
		centerPanel.add(documentsPanel);
		centerPanel.add(sectionsPanel);

		// chatPanel
		JTextArea chatArea = new JTextArea("No messages", 30, 24);
		JTextField chatField = new JTextField("Write here");
		JButton sendButton = new JButton("Send");
		chatArea.setEditable(false);
		chatPanel.add(chatArea, BorderLayout.NORTH);
		chatPanel.add(chatField, BorderLayout.CENTER);
		chatPanel.add(sendButton, BorderLayout.SOUTH);

		// workspace
		add(buttonsPanel, BorderLayout.WEST);
		add(centerPanel, BorderLayout.CENTER);
		add(chatPanel, BorderLayout.EAST);
		setVisible(true);
	}

	/**
	 * TO DO
	 */
	private void createDocument() {
		JPanel panel = new JPanel();
		JLabel documentNameLabel = new JLabel("Document name");
		JLabel sectionsLabel = new JLabel("Number of sections");
		JTextField documentNameField = new JTextField(10);
		NumberFormatter numberFormatter = new NumberFormatter(NumberFormat.getIntegerInstance());
		numberFormatter.setValueClass(Integer.class);
		numberFormatter.setAllowsInvalid(false);
		numberFormatter.setMinimum(1);
		numberFormatter.setMaximum(1000);
		JFormattedTextField sectionsField = new JFormattedTextField(numberFormatter);
		sectionsField.setColumns(10);
		panel.add(documentNameLabel);
		panel.add(documentNameField);
		panel.add(sectionsLabel);
		panel.add(sectionsField);
		int code = JOptionPane.showConfirmDialog(Client.frame, panel, "Create document",
				JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE);

		if (code != JOptionPane.OK_OPTION)
			return;
		String documentName = documentNameField.getText();
		Integer sections = (Integer) sectionsField.getValue();

		if (documentName.isBlank() || sections == null) // TODO: errorDialog
			return;

		connection.createDocument(documentName, sections);
	}

	/**
	 * TO DO
	 */
	public void addDocument(Document document) {
		Object[] data = {document.getName(), document.getCreator(), "n.d.", "n.d."};
		documentsTableModel.addRow(data);
		documents.add(document);
	}

	/**
	 * TO DO
	 */
	private void updateSectionTable(int index) {
		sectionsTableModel.setRowCount(0);
		lastSelectedDocument = documents.get(index);
		lastSelectedSection = -1;
		for (int i = 0; i < documents.get(index).getSections(); i++) {
			Object[] data = {documents.get(index).getName() + "_" + i, "TODO"};
			sectionsTableModel.addRow(data);
		}
	}

	/**
	 * TO DO
	 */
	public void showErrorDialog(String message, Exception e) {
		JOptionPane.showMessageDialog(Client.frame, message + e.getMessage(),
				"Error", JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * TO DO
	 */
	public Document getLastSelectedDocument() { return lastSelectedDocument; }
}
