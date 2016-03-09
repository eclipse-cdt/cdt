package org.eclipse.cdt.cmake.ui.properties;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.cmake.ui.properties.messages"; //$NON-NLS-1$
	public static String CMakePropertyPage_FailedToStartCMakeGui_Body;
	public static String CMakePropertyPage_FailedToStartCMakeGui_Title;
	public static String CMakePropertyPage_LaunchCMakeGui;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
