package org.eclipse.cdt.launch.serial.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.launch.serial.internal.messages"; //$NON-NLS-1$
	public static String SerialFlashLaunch_Pause;
	public static String SerialFlashLaunch_Resume;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
