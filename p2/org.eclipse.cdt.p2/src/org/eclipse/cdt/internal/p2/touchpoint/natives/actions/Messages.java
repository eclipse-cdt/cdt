package org.eclipse.cdt.internal.p2.touchpoint.natives.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.p2.touchpoint.natives.actions.messages"; //$NON-NLS-1$
	public static String UnpackAction_ParmNotPresent;
	public static String UnpackAction_TargetDirExists;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
