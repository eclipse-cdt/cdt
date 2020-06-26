/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Martin Oberhuber (Wind River) - [303083] Split out from CCorePlugin
 *******************************************************************************/
package org.eclipse.cdt.internal.core.natives;

import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * CNativePlugin is the life-cycle owner of the plug-in, and also holds
 * utility methods for logging.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CNativePlugin extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.core.native"; //$NON-NLS-1$

	private static CNativePlugin fgPlugin;

	// NON-API

	/**
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public CNativePlugin() {
		super();
		fgPlugin = this;
	}

	public static CNativePlugin getDefault() {
		return fgPlugin;
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String e) {
		log(createStatus(e));
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(Throwable e) {
		String msg = e.getMessage();
		if (msg == null) {
			log("Error", e); //$NON-NLS-1$
		} else {
			log("Error: " + msg, e); //$NON-NLS-1$
		}
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(String message, Throwable e) {
		log(createStatus(message, e));
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static IStatus createStatus(String msg) {
		return createStatus(msg, null);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static IStatus createStatus(String msg, Throwable e) {
		return new Status(IStatus.ERROR, PLUGIN_ID, msg, e);
	}

	/**
	 * @noreference This method is not intended to be referenced by clients.
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Get the WindowsRegistry contributed class for the platform.
	 */
	public WindowsRegistry getWindowsRegistry() throws CoreException {
		IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, "WindowsRegistry"); //$NON-NLS-1$
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			IConfigurationElement defaultContributor = null;
			for (IExtension extension2 : extensions) {
				IConfigurationElement[] configElements = extension2.getConfigurationElements();
				for (IConfigurationElement configElement : configElements) {
					if (configElement.getName().equals("windowsRegistry")) { //$NON-NLS-1$
						String platform = configElement.getAttribute("platform"); //$NON-NLS-1$
						if (platform == null) { // first contributor found with
												// not platform will be default.
							if (defaultContributor == null) {
								defaultContributor = configElement;
							}
						} else if (platform.equals(Platform.getOS())) {
							// found explicit contributor for this platform.
							return (WindowsRegistry) configElement.createExecutableExtension("class"); //$NON-NLS-1$
						}
					}
				}
			}
			if (defaultContributor != null) {
				return (WindowsRegistry) defaultContributor.createExecutableExtension("class"); //$NON-NLS-1$
			}
		}
		return null;
	}
}
