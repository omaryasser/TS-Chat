package tschat;

import javax.swing.*;
import java.awt.event.*;

public class WindowDestroyer extends WindowAdapter {

	public void windowClosing(WindowEvent e) {
		int confirmed = JOptionPane.showConfirmDialog(null, "Leaving?", "Exit", JOptionPane.YES_NO_OPTION);
		if (confirmed == JOptionPane.YES_OPTION) {
			System.exit(0);
		}
	}
}