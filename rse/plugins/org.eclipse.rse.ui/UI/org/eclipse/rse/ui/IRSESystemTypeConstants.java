/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kushal Munir (IBM) - Initial API and implementation.
 ********************************************************************************/
package org.eclipse.rse.ui;

/**
 * These constants define the set of properties that the UI expects to
 * be available via <code>IRSESystemType.getProperty(String)</code>.
 *
 * @see org.eclipse.core.runtime.IRSESystemType#getProperty(String)
 */
public interface IRSESystemTypeConstants {

	public static final String ICON = "icon";
	public static final String ICON_LIVE = "iconLive";
	public static final String ENABLE_OFFLINE = "enableOffline";
}
