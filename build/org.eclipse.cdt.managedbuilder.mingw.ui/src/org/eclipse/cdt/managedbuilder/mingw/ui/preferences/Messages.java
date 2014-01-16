package org.eclipse.cdt.managedbuilder.mingw.ui.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.managedbuilder.mingw.ui.preferences.messages"; //$NON-NLS-1$

	public static String MinGWPrefPage_Description;
	public static String MinGWPrefPage_MingwLocation;
	public static String MinGWPrefPage_MsysLocation;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
