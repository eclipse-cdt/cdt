package org.eclipse.cdt.make.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.make.internal.ui.messages"; //$NON-NLS-1$
	public static String MakeBuildSettingsTab_Build;
	public static String MakeBuildSettingsTab_BuildCommands;
	public static String MakeBuildSettingsTab_BuildInConfigDir;
	public static String MakeBuildSettingsTab_BuildInProjectDir;
	public static String MakeBuildSettingsTab_BuildOutputLocation;
	public static String MakeBuildSettingsTab_Clean;
	public static String MakeBuildSettingsTab_Makefile;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
