/*******************************************************************************
 * Copyright (c) 2000, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Martin Oberhuber (Wind River) - [303083] Split out from CCorePlugin
 *******************************************************************************/
package org.eclipse.cdt.internal.core.spawner;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;

/**
 * CSpawnerPlugin is the life-cycle owner of the plug-in.
 *
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public class CSpawnerPlugin extends Plugin {

	public static final String PLUGIN_ID = "org.eclipse.cdt.core.spawner"; //$NON-NLS-1$

	private static CSpawnerPlugin fgPlugin;

	// NON-API

	/**
	 * @noreference This constructor is not intended to be referenced by clients.
	 */
	public CSpawnerPlugin() {
		super();
		fgPlugin = this;
	}

	public static CSpawnerPlugin getDefault() {
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
		String msg= e.getMessage();
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
