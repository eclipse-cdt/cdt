package org.eclipse.cdt.internal.qt.core.build;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.qt.core.build.messages"; //$NON-NLS-1$
	public static String QtBuildConfigurationProvider_NoQtInstall;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
