/*******************************************************************************
 * Copyright (c) 2012, 2012 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.language.settings.providers;

import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;

/**
 * Class to detect built-in compiler settings for MinGW toolchain.
 */
public class GCCBuiltinSpecsDetectorMinGW extends GCCBuiltinSpecsDetector {
	// ID must match the tool-chain definition in org.eclipse.cdt.managedbuilder.core.buildDefinitions extension point
	private static final String GCC_TOOLCHAIN_ID_MINGW = "cdt.managedbuild.toolchain.gnu.mingw.base";  //$NON-NLS-1$

	@Override
	public String getToolchainId() {
		return GCC_TOOLCHAIN_ID_MINGW;
	}

	@Override
	public GCCBuiltinSpecsDetectorMinGW cloneShallow() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetectorMinGW) super.cloneShallow();
	}

	@Override
	public GCCBuiltinSpecsDetectorMinGW clone() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetectorMinGW) super.clone();
	}

}
