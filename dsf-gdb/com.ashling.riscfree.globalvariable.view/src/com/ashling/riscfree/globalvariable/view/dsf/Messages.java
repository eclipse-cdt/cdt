package com.ashling.riscfree.globalvariable.view.dsf;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "com.ashling.riscfree.globalvariable.view.dsf.messages"; //$NON-NLS-1$
	private static ResourceBundle RESOURCE_BUNDLE;
	static {
		try {
			RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch (MissingResourceException e) {
			System.console().printf("%s", e);
		}
	}
	public static String GlobalVariableService_0;
	public static String GlobalVariableService_1;
	public static String GlobalVariableService_2;
	public static String GlobalVariableService_3;
	public static String GlobalVariableService_4;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
	
	private Messages() {
	}
}
