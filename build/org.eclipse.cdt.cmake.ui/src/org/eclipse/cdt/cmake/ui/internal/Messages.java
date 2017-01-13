package org.eclipse.cdt.cmake.ui.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

	public static String CMakeBuildTab_BuildCommand;
	public static String CMakeBuildTab_CleanCommand;
	public static String CMakeBuildTab_Cmake;
	public static String CMakeBuildTab_CMakeArgs;
	public static String CMakeBuildTab_Generator;
	public static String CMakeBuildTab_Ninja;
	public static String CMakeBuildTab_UnixMakefiles;
	public static String CMakePropertyPage_FailedToStartCMakeGui_Body;
	public static String CMakePropertyPage_FailedToStartCMakeGui_Title;
	public static String CMakePropertyPage_LaunchCMakeGui;

	static {
		// initialize resource bundle
		NLS.initializeMessages(Messages.class.getName(), Messages.class);
	}

	private Messages() {
	}
}
