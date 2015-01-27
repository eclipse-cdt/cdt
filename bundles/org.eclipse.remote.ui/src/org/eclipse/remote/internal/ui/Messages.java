package org.eclipse.remote.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.remote.internal.ui.messages"; //$NON-NLS-1$
	public static String NewRemoteConnectionTypePage_LaunchTargetType;
	public static String NewRemoteConnectionTypePage_SelectTargetType;
	public static String NewRemoteConnectionWizard_0;
	public static String OpenTerminalHandler_OpenTerminalDesc;
	public static String OpenTerminalHandler_OpenTerminalTitle;
	public static String RemoteConnectionPropertyPage_ConnectionName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
