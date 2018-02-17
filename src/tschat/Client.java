package tschat;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Scanner;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class Client extends JFrame {

	private JTextField destinationName;
	private JTextField userText;
	private JTextArea chatWindow;
	private ObjectOutputStream output;
	private ObjectInputStream input;
	private String message = "";
	private String serverIP;
	private Socket connection;
	private Chat chat;
	private int toPort;

	/**
	 * 
	 * @param host
	 *            IP address of the host
	 * @param toPort
	 *            port of the server
	 */
	public Client(String host, int toPort) {
		super("Chat");
		this.toPort = toPort;
		serverIP = host;
		chat = new Chat();

		userText = new JTextField();
		destinationName = new JTextField();

		Color bgBlue = new Color(19, 38, 57);
		Color fgRed = new Color(204, 51, 0);

		JLabel to = new JLabel("To: ");
		to.setForeground(fgRed);

		JLabel urMessage = new JLabel("Message: ");
		urMessage.setForeground(fgRed);

		userText.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				message = event.getActionCommand();
				chat.message = message;
				chat.destination = destinationName.getText();
				try {
					String s = encode(chat);
					output.writeObject(s);
					userText.setText("");
					
					String tmpS = chat.source;
					chat = new Chat();
					chat.source = tmpS;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		
		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
		addWindowListener(new WindowDestroyer());

		JButton btn = new JButton("Get all users");
		btn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getMemberList("getAllMembers");
			}
		});

		JButton btn2 = new JButton("Get users on my server");
		btn2.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getMemberList("getMyServerMembers");
			}
		});

		JButton btn3 = new JButton("Get users on server 2");
		btn3.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getMemberList("getOtherServerMembers0");
			}
		});

		JButton btn4 = new JButton("Get users on server 3");
		btn4.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getMemberList("getOtherServerMembers1");
			}
		});

		JButton btn5 = new JButton("Get users on server 4");
		btn5.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				getMemberList("getOtherServerMembers2");
			}
		});
		btn.setBackground(bgBlue);
		btn.setForeground(fgRed);
		btn2.setBackground(bgBlue);
		btn2.setForeground(fgRed);
		btn3.setBackground(bgBlue);
		btn3.setForeground(fgRed);
		btn4.setBackground(bgBlue);
		btn4.setForeground(fgRed);
		btn5.setBackground(bgBlue);
		btn5.setForeground(fgRed);

		this.setBackground(new Color(51, 102, 153));
		JPanel x = new JPanel(new GridLayout(3, 0));
		x.add(to);
		x.add(urMessage);
		JPanel y = new JPanel(new BorderLayout());
		userText.setBackground(new Color(230, 230, 250));
		destinationName.setBackground(new Color(188, 210, 238));
		y.add(destinationName, BorderLayout.NORTH);
		y.add(userText, BorderLayout.CENTER);
		JPanel z = new JPanel(new BorderLayout());
		z.add(x, BorderLayout.WEST);
		z.add(y, BorderLayout.CENTER);

		JPanel buttons = new JPanel(new GridLayout(5, 0));
		buttons.add(btn);
		buttons.add(btn2);
		buttons.add(btn3);
		buttons.add(btn4);
		buttons.add(btn5);

		z.add(buttons, BorderLayout.SOUTH);

		chatWindow = new JTextArea();
		chatWindow.setBackground(bgBlue);
		chatWindow.setForeground(new Color(236, 242, 248));

		add(z, BorderLayout.SOUTH);
		add(new JScrollPane(chatWindow), BorderLayout.CENTER);

		setBounds(50, 50, 500, 600);
		setVisible(true);
		startRunning();
	}

	public void startRunning() {
		try {
			connectToServer();
			setupStreams();
			setupUsername();
			whileChatting();
		} catch (EOFException e) {
			showMessage("Client terminated connection\n");
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			close();
		}
	}

	private static String encode(Chat c) {
		return c.source + "$" + c.destination + "$" + c.TTL + "$" + c.message;
	}

	public void setupUsername() {
		String ob = "";
		try {
			do {
				String s = JOptionPane.showInputDialog("Choose a username");

				if (s == null)
					System.exit(0);

				if (!valid(s)) {
					showMessage("You can use only letters[A - Z].\n");
					continue;
				}
				chat.source = s;
				output.writeObject(s);
				ob = (String) input.readObject();
				showMessage(ob);
			} while (!ob.equals("Username accepted !!\n"));
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
		this.setTitle("Chat | " + chat.source);
	}

	private boolean valid(String userName) {
		for (int i = 0; i < userName.length(); ++i)
			if (!Character.isAlphabetic(userName.charAt(i)))
				return false;
		return true;
	}

	private void connectToServer() throws IOException {
		showMessage("Attempting connection...\n");
		connection = new Socket(InetAddress.getByName(serverIP), toPort);
		showMessage("Connected to " + connection.getInetAddress().getHostName() + "\n");
	}

	private void setupStreams() throws IOException {
		output = new ObjectOutputStream(connection.getOutputStream());
		output.flush();
		input = new ObjectInputStream(connection.getInputStream());
		showMessage("Streams are now setup!\n");
		showMessage("Please choose a username!!\n");
	}

	private void whileChatting() throws IOException {
		do
			try {
				message = (String) input.readObject();
				showMessage(message + "\n");
			} catch (ClassNotFoundException e) {
				showMessage("There is a problem with the message\n");
			}
		while (true);
	}

	private void close() {
		showMessage("Closing connections...\n---\n");
		try {
			output.close();
			input.close();
			connection.close();
			System.exit(0);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void showMessage(final String message) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				chatWindow.append(message);
			}
		});
	}

	private void getMemberList(String message) {
		try {
			output.writeObject(message);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public static void main(String[] args) {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter port number");
		int port = sc.nextInt();
		sc.close();
		// server1: 9001
		// server2: 9000
		// server3: 9002
		// server4: 9003
		new Client("127.0.0.1", port);
	}
}