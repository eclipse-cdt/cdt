/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Wind River Systems, Inc. - extended implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.modules.detail;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class ModulesMessages {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.dsf.debug.ui.viewmodel.modules.detail.ModulesMessages";//$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private ModulesMessages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
