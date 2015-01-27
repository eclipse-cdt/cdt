package org.eclipse.remote.internal.ui.views;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.remote.internal.ui.views.messages"; //$NON-NLS-1$
	public static String DeleteRemoteConnectionHandler_ConfirmDeleteMessage;
	public static String DeleteRemoteConnectionHandler_DeleteConnectionTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
