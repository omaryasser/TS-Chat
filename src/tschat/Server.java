package tschat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Server {

	private ServerSocket server;
	private ArrayList<ServerRunnable> serverRunnables;

	private ArrayList<Socket> connections;
	private ArrayList<ObjectInputStream> inputs;
	private ArrayList<ObjectOutputStream> outputs;

	private int port;
	private String serverIP;

	public Server(String serverIP, int port) throws Exception {
		this.port = port;
		this.serverIP = serverIP;
		server = new ServerSocket(port);

		serverRunnables = new ArrayList<>();
		connections = new ArrayList<>();
		inputs = new ArrayList<>();
		outputs = new ArrayList<>();

		if (this.port == 9000) {
			connectToServer(9001);
			waitForServerConnection(9002);
			waitForServerConnection(9003);
		} else if (port == 9001) {
			waitForServerConnection(9000);
			waitForServerConnection(9002);
			waitForServerConnection(9003);
		} else if (port == 9002) {
			connectToServer(9000);
			connectToServer(9001);
			waitForServerConnection(9003);
		} else {
			connectToServer(9000);
			connectToServer(9001);
			connectToServer(9002);
		}

		new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						waitForConnection();
					} catch (Exception e) {
					}
				}
			}
		}).start();

		new Thread(new SpecialServerRunnable(this, outputs.get(1), inputs.get(0))).start();
		new Thread(new SpecialServerRunnable(this, outputs.get(3), inputs.get(2))).start();
		new Thread(new SpecialServerRunnable(this, outputs.get(5), inputs.get(4))).start();
	}

	private void connectToServer(int portTo) throws IOException {
		connections.add(new Socket(InetAddress.getByName(serverIP), portTo));
		connections.add(new Socket(InetAddress.getByName(serverIP), portTo));
		setupStreams();
	}

	private void waitForServerConnection(int portFrom) throws IOException {
		connections.add(server.accept());
		connections.add(server.accept());
		setupStreams();
	}

	private void setupStreams() throws IOException {
		outputs.add(new ObjectOutputStream(connections.get(connections.size() - 2).getOutputStream()));
		inputs.add(new ObjectInputStream(connections.get(connections.size() - 2).getInputStream()));

		outputs.add(new ObjectOutputStream(connections.get(connections.size() - 1).getOutputStream()));
		inputs.add(new ObjectInputStream(connections.get(connections.size() - 1).getInputStream()));
	}

	private void waitForConnection() throws IOException {
		ArrayList<ObjectOutputStream> x = new ArrayList<>();
		ArrayList<ObjectInputStream> y = new ArrayList<>();

		x.add(outputs.get(0));
		x.add(outputs.get(2));
		x.add(outputs.get(4));

		y.add(inputs.get(1));
		y.add(inputs.get(3));
		y.add(inputs.get(5));

		ServerRunnable serverRunnable = new ServerRunnable(this, server.accept(), x, y);
		serverRunnables.add(serverRunnable);
		new Thread(serverRunnable).start();
	}

	public ArrayList<ServerRunnable> getServerRunnables() {
		return serverRunnables;
	}

	public static void main(String[] args) throws Exception {
		Scanner sc = new Scanner(System.in);
		System.out.println("Enter port number");
		int port = sc.nextInt();
		sc.close();
		// server1: 9001
		// server2: 9000
		// server3: 9002
		// server4: 9003
		new Server("127.0.0.1", port);
	}
}