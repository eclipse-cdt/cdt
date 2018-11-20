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

import org.eclipse.core.runtime.IStatus;
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

}
