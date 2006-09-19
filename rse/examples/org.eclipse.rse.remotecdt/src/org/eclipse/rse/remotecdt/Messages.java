package org.eclipse.rse.remotecdt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.rse.remotecdt.messages"; //$NON-NLS-1$

	public static String RemoteCMainTab_Program;
	public static String RemoteCMainTab_SkipDownload;
	public static String RemoteCMainTab_ErrorNoProgram;
	public static String RemoteCMainTab_ErrorNoConnection;
	public static String RemoteCMainTab_Connection;
	public static String RemoteCMainTab_New;

	public static String RemoteRunLaunchDelegate_RemoteShell;
	public static String RemoteRunLaunchDelegate_1;
	public static String RemoteRunLaunchDelegate_3;
	public static String RemoteRunLaunchDelegate_4;
	public static String RemoteRunLaunchDelegate_5;
	public static String RemoteRunLaunchDelegate_6;
	public static String RemoteRunLaunchDelegate_7;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
