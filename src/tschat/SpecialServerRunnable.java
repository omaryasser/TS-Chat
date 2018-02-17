package tschat;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class SpecialServerRunnable implements Runnable {

	private Server server;
	private ObjectOutputStream output;
	private ObjectInputStream input;

	public SpecialServerRunnable(Server server, ObjectOutputStream output, ObjectInputStream input) {
		this.server = server;
		this.output = output;
		this.input = input;
	}

	@Override
	public void run() {
		try {
			while (true) {
				waitForServerRequests();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void waitForServerRequests() throws Exception {
		String request = (String) input.readObject();
		if (request.equals("getYourMembers")) {
			String members = "";
			for (ServerRunnable x : server.getServerRunnables())
				if (!x.getClientName().equals("125395415871"))
					members += " - " + x.getClientName() + "\n";
			output.writeObject(members);
			output.flush();
		} else if (request.length() >= 10 && request.substring(0, 10).equals("&&SendThis")) {
			String encodedMessage = request.substring(10);
			String message = getMessage(encodedMessage);
			String destination = getDestination(encodedMessage);
			String source = getSource(encodedMessage);
			getTTL(encodedMessage);

			ServerRunnable destinationServerRunnable = null;
			boolean found = false;
			for (ServerRunnable serverRunnable : server.getServerRunnables())
				if (serverRunnable.getClientName().equals(destination)) {
					destinationServerRunnable = serverRunnable;
					found = true;
				}

			if (found) {
				String mess = source + " says" + ": " + message;
				output.writeObject("found");
				output.flush();
				destinationServerRunnable.getOutput().writeObject(mess);
			} else {
				output.writeObject("&&Not_There");
				output.flush();
			}
		} else if (request.length() >= 11 && request.substring(0, 11).equals("&&DoYouHave")) {
			String name = request.substring(11);
			boolean found = false;
			for (ServerRunnable serverRunnable : server.getServerRunnables())
				if (serverRunnable.getClientName().equals(name))
					found = true;
			if (found) {
				output.writeObject("don'tAccept");
				output.flush();
			} else {
				output.writeObject("&&GoAhead");
				output.flush();
			}
		}
	}

	private static String getSource(String s) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); ++i)
			if (s.charAt(i) == '$')
				return sb.toString();
			else
				sb.append(s.charAt(i));
		return "";
	}

	private static String getDestination(String s) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (s.charAt(i) != '$')
			i++;
		i++;
		for (; i < s.length(); ++i)
			if (s.charAt(i) == '$')
				return sb.toString();
			else
				sb.append(s.charAt(i));
		return "";
	}

	private static int getTTL(String s) {
		StringBuilder sb = new StringBuilder();
		int i = 0;
		while (s.charAt(i) != '$')
			i++;
		i++;
		while (s.charAt(i) != '$')
			i++;
		i++;
		for (; i < s.length(); ++i)
			if (s.charAt(i) == '$')
				return Integer.parseInt(sb.toString());
			else
				sb.append(s.charAt(i));
		return 1;
	}

	private static String getMessage(String s) {
		int i = 0;
		while (s.charAt(i) != '$')
			i++;
		i++;
		while (s.charAt(i) != '$')
			i++;
		i++;
		while (s.charAt(i) != '$')
			i++;
		i++;
		return s.substring(i);
	}

}
