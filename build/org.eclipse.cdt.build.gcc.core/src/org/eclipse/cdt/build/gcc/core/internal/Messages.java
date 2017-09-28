package org.eclipse.cdt.build.gcc.core.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.build.gcc.core.internal.messages"; //$NON-NLS-1$
	public static String GCCUserToolChainProvider_Loading;
	public static String GCCUserToolChainProvider_NotOurs;
	public static String GCCUserToolChainProvider_Saving;
	public static String GCCUserToolChainProvider_Saving1;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
