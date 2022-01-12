package org.eclipse.cdt.internal.qt.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.qt.core.messages"; //$NON-NLS-1$
	public static String QtBuildConfiguration_ConfigNotFound;
	public static String QtBuildConfiguration_MakeNotFound;
	public static String QtBuilder_0;
	public static String QtBuildTab_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
