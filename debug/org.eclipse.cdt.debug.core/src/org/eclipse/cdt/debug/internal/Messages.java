package org.eclipse.cdt.debug.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.debug.internal.messages"; //$NON-NLS-1$
	public static String CoreBuildGenericLaunchConfigDelegate_CommandNotValid;
	public static String CoreBuildGenericLaunchConfigDelegate_NoAction;
	public static String CoreBuildGenericLaunchConfigDelegate_SubstitutionFailed;
	public static String CoreBuildGenericLaunchConfigDelegate_WorkingDirNotExists;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
