package org.eclipse.cdt.utils.pty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class PTY {

	String slave;
	public int master;
	InputStream in;
	OutputStream out;

	private static boolean hasPTY;
	
	public PTY() throws IOException {
		if (hasPTY) {
			slave= forkpty();
		}
		if (slave == null) {
			throw new IOException("Can not create pty");
		}
		in = new PTYInputStream(master);
		out = new PTYOutputStream(master);
	}
	
	public String getSlaveName() {
		return slave;
	}
	
	public OutputStream getOutputStream() {
		return out;
	}
	
	public InputStream getInputStream() {
		return in;
	}
	
	native String forkpty();

	static {
		try {
			System.loadLibrary("pty");
			hasPTY = true;
		} catch (SecurityException e) {
		} catch (UnsatisfiedLinkError e) {
		}			
	}
	
}
