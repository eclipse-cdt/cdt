/*******************************************************************************
 * Copyright (c) 2004, 2007, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Red Hat - Copy from org.eclipse.cdt.wizards to here plus rename 
 *******************************************************************************/
package org.eclipse.cdt.internal.autotools.ui.wizards;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @since 2.0
 */
public class AutotoolsWizardMessages {
	// Bundle ID
	private static final String BUNDLE_ID = AutotoolsWizardMessages.class.getName();
	//Resource bundle.
	private static ResourceBundle resourceBundle;

	static {
		try {
			resourceBundle = ResourceBundle.getBundle(BUNDLE_ID);
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new Object[] { arg });
	}

	public static String getFormattedString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[])args);
	}

	public static String getResourceString(String key) {
		try {
			return resourceBundle.getString(key);
		} catch (MissingResourceException e) {
			return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
		} catch (NullPointerException e) {
			return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	private AutotoolsWizardMessages() {
		// No constructor
	}
}
