package org.eclipse.remote.internal.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.remote.internal.core.messages.messages"; //$NON-NLS-1$
	public static String LocalConnection_1;
	public static String LocalConnection_2;
	public static String RemoteServicesProxy_0;
	public static String RemoteServicesProxy_1;
	public static String Unable_to_create_new_local_connections;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
