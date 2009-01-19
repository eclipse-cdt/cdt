/********************************************************************************
 * Copyright (c) 2006, 2009 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Kushal Munir (IBM) - Initial API and implementation.
 * Martin Oberhuber (Wind River) - [cleanup] Add API "since" Javadoc tags
 * Martin Oberhuber (Wind River) - [261486][api][cleanup] Mark @noimplement interfaces as @noextend
 ********************************************************************************/
package org.eclipse.rse.core;

/**
 * These constants define the set of properties that the UI expects to be
 * available via <code>IRSESystemType.getProperty(String)</code>.
 *
 * @see org.eclipse.rse.core.IRSESystemType#getProperty(String)
 * @noimplement This interface is not intended to be implemented by clients.
 * @noextend This interface is not intended to be extended by clients.
 */
public interface IRSESystemTypeConstants {

	public static final String ICON = "icon"; //$NON-NLS-1$
	public static final String ICON_LIVE = "iconLive"; //$NON-NLS-1$
	public static final String ENABLE_OFFLINE = "enableOffline"; //$NON-NLS-1$
}
