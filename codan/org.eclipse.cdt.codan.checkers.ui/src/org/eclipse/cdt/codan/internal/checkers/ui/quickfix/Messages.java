package org.eclipse.cdt.codan.internal.checkers.ui.quickfix;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.cdt.codan.internal.checkers.ui.quickfix.messages"; //$NON-NLS-1$
	public static String QuickFixCreateField_0;
	public static String QuickFixCreateLocalVariable_0;
	public static String QuickFixCreateParameter_0;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
