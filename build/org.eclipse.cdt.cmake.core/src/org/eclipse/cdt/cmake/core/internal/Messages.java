package org.eclipse.cdt.cmake.core.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.cmake.core.internal.messages"; //$NON-NLS-1$
	public static String CMakeBuildConfiguration_Building;
	public static String CMakeBuildConfiguration_BuildingIn;
	public static String CMakeBuildConfiguration_Cleaning;
	public static String CMakeBuildConfiguration_NotFound;
	public static String CMakeBuildConfiguration_ProcCompCmds;
	public static String CMakeBuildConfiguration_ProcCompJson;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
