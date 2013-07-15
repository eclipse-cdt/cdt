package org.eclipse.internal.remote.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.internal.remote.core.messages.messages"; //$NON-NLS-1$
	public static String LocalConnection_1;
	public static String LocalConnection_2;
	public static String RemoteServicesProxy_0;
	public static String RemoteServicesProxy_1;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
