package org.eclipse.remote.internal.ui.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.remote.internal.ui.views.messages"; //$NON-NLS-1$
	public static String CloseConnectionHandler_0;
	public static String CloseConnectionHandler_1;
	public static String DeleteRemoteConnectionHandler_ConfirmDeleteMessage;
	public static String DeleteRemoteConnectionHandler_DeleteConnectionTitle;
	public static String OpenConnectionHandler_0;
	public static String OpenConnectionHandler_1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
