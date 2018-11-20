/*******************************************************************************
 * Copyright (c) 2007, 2011 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.provider;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Internal singleton map maintained for non-project PDOM objects
 */
class PDOMCache {
	private Map<File, PDOM> path2pdom; // gives the PDOM for a particular path

	private static PDOMCache singleton;
	private static Object singletonMutex = new Object();

	private PDOMCache() {
		this.path2pdom = new HashMap<>();
	}

	/**
	 * Returns the instance of the cache
	 */
	public static PDOMCache getInstance() {
		synchronized (singletonMutex) {
			if (singleton == null) {
				singleton = new PDOMCache();
			}
			return singleton;
		}
	}

	/**
	 * Returns the mapped PDOM for the path specified, if such a pdom is not already known about
	 * then one is created using the location converter specified.
	 * @param path
	 * @param converter
	 * @return a PDOM instance or null if the PDOM version was too old
	 */
	public PDOM getPDOM(IPath path, IIndexLocationConverter converter) {
		if (path == null) {
			return null;
		}

		PDOM result = null;
		File file = path.toFile();

		synchronized (path2pdom) {
			if (path2pdom.containsKey(file)) {
				result = path2pdom.get(file);
			}
			if (result == null) {
				try {
					result = new PDOM(file, converter, LanguageManager.getInstance().getPDOMLinkageFactoryMappings());
					path2pdom.put(file, result);
				} catch (CoreException ce) {
					CCorePlugin.log(ce);
				}
			}
		}

		return result;
	}
}