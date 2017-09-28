package org.eclipse.cdt.internal.core.build;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.core.build.Messages"; //$NON-NLS-1$
	public static String CBuildConfigurationtoolchainMissing;
	public static String CBuilder_ExceptionWhileBuilding;
	public static String CBuilder_ExceptionWhileBuilding2;
	public static String CBuilder_NotConfiguredCorrectly;
	public static String CBuilder_NotConfiguredCorrectly2;
	public static String StandardBuildConfiguration_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
