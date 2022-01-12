/*******************************************************************************
 * Copyright (c) 2004, 2013 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext;

import org.eclipse.osgi.util.NLS;

public final class CorextMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.cdt.internal.corext.CorextMessages";//$NON-NLS-1$

	private CorextMessages() {
		// Do not instantiate
	}

	public static String Resources_outOfSyncResources;
	public static String Resources_outOfSync;
	public static String Resources_modifiedResources;
	public static String Resources_fileModified;

	static {
		NLS.initializeMessages(BUNDLE_NAME, CorextMessages.class);
	}
}