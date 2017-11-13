package org.eclipse.cdt.launch.internal.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.launch.internal.ui.messages"; //$NON-NLS-1$
	public static String GenericMainTab_Arguments;
	public static String GenericMainTab_BrowseFileSystem;
	public static String GenericMainTab_BrowseWorkspace;
	public static String GenericMainTab_Location;
	public static String GenericMainTab_LocationNotAFile;
	public static String GenericMainTab_LocationNotExists;
	public static String GenericMainTab_Main;
	public static String GenericMainTab_Quotes;
	public static String GenericMainTab_SelectResource;
	public static String GenericMainTab_SelectWorkingDir;
	public static String GenericMainTab_SpecifyLocation;
	public static String GenericMainTab_Variables;
	public static String GenericMainTab_WorkingDirectory;
	public static String GenericMainTab_WorkingDirNotADir;
	public static String GenericMainTab_WorkingDirNotExists;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
