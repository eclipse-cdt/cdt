/*******************************************************************************
 * Copyright (c) 2009, 2013 Andrew Gvozdev and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Gvozdev - initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.managedbuilder.internal.language.settings.providers;

import java.net.URI;

import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.cdt.internal.core.Cygwin;
import org.eclipse.cdt.managedbuilder.core.ManagedBuilderCorePlugin;
import org.eclipse.cdt.managedbuilder.language.settings.providers.GCCBuiltinSpecsDetector;

/**
 * Class to detect built-in compiler settings for Cygwin toolchain.
 * The paths are converted to cygwin "file-system" representation.
 */
public class GCCBuiltinSpecsDetectorCygwin extends GCCBuiltinSpecsDetector {
	// ID must match the tool-chain definition in org.eclipse.cdt.managedbuilder.core.buildDefinitions extension point
	private static final String GCC_TOOLCHAIN_ID_CYGWIN = "cdt.managedbuild.toolchain.gnu.cygwin.base";  //$NON-NLS-1$
	private static final String ENV_PATH = "PATH"; //$NON-NLS-1$

	/**
	 * EFSExtensionProvider for Cygwin translations
	 */
	private class CygwinEFSExtensionProvider extends EFSExtensionProvider {
		private String envPathValue;

		/**
		 * Constructor.
		 * @param envPathValue - Value of environment variable $PATH.
		 */
		public CygwinEFSExtensionProvider(String envPathValue) {
			this.envPathValue = envPathValue;
		}

		@Override
		public String getMappedPath(URI locationURI) {
			String windowsPath = null;
			try {
				String cygwinPath = getPathFromURI(locationURI);
				windowsPath = Cygwin.cygwinToWindowsPath(cygwinPath, envPathValue);
			} catch (Exception e) {
				ManagedBuilderCorePlugin.log(e);
			}
			if (windowsPath != null) {
				return windowsPath;
			}

			return super.getMappedPath(locationURI);
		}
	}

	@Override
	public String getToolchainId() {
		return GCC_TOOLCHAIN_ID_CYGWIN;
	}

	@Override
	protected EFSExtensionProvider getEFSProvider() {
		String envPathValue = environmentMap != null ? environmentMap.get(ENV_PATH) : null;
		return new CygwinEFSExtensionProvider(envPathValue);
	}

	@Override
	public GCCBuiltinSpecsDetectorCygwin cloneShallow() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetectorCygwin) super.cloneShallow();
	}

	@Override
	public GCCBuiltinSpecsDetectorCygwin clone() throws CloneNotSupportedException {
		return (GCCBuiltinSpecsDetectorCygwin) super.clone();
	}

}
