/**********************************************************************
 * Copyright (c) 2002,2003 Timesys Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * Timesys - Initial API and implementation
 **********************************************************************/

package org.eclipse.cdt.gnu.tools;

import org.eclipse.cdt.core.builder.ACToolchainProvider;

/**
 * Standard GNU toolchain provider.
 */
public class CGnuToolchainProvider extends ACToolchainProvider {

	/**
	 * Prefix used to construct toolchain identifiers for this
	 * provider.
	 */
	public final static String PROVIDER_ID = "org.eclipse.cdt.gnu.tools";

	/**
	 * This toolchain provider.
	 */
	public final static String NATIVE_ID = PROVIDER_ID + ".native";

	/**
	 * @see org.eclipse.cdt.core.builder.ACToolchainProvider#doRefresh()
	 */
	public void doRefresh() {
		addToolchain(NATIVE_ID, new CGnuToolchain());
	}
}
