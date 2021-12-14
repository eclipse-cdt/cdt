package org.eclipse.remote.internal.core.services.local;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.remote.internal.core.services.local.messages"; //$NON-NLS-1$
	public static String LocalConnectionProviderService_LocalConnectionName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
