/*******************************************************************************
 * Copyright (c) 2007 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.index.provider;

import java.io.File;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.cdt.internal.core.pdom.PDOM;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/**
 * Internal singleton map maintained for non-project PDOM objects
 */
class PDOMCache {
	private Map/*<File, PDOM>*/ path2pdom; // gives the pdom for a particular path
	private Set/*<File>*/ versionMismatch;
	
	private static PDOMCache singleton;
	private static Object singletonMutex = new Object();

	private PDOMCache() {
		this.path2pdom = new HashMap();
		this.versionMismatch = new HashSet();
	}
	
	/**
	 * Returns the instance of the cache
	 * @return
	 */
	public static PDOMCache getInstance() {
		synchronized(singletonMutex) {
			if(singleton == null) {
				singleton = new PDOMCache();
			}
			return singleton;
		}
	}

	/**
	 * Returns the mapped pdom for the path specified, if such a pdom is not already known about
	 * then one is created using the location converter specified. 
	 * @param path
	 * @param converter
	 * @return a pdom instance or null if the pdom version was too old
	 */
	public PDOM getPDOM(IPath path, IIndexLocationConverter converter) {
		PDOM result= null;
		File file = path.toFile();

		if(!versionMismatch.contains(file)) {
			synchronized(path2pdom) {
				if(path2pdom.containsKey(file)) {
					result = (PDOM) path2pdom.get(file);
				}
				if(result==null) {
					try {
						result = new PDOM(file, converter);

						result.acquireReadLock();
						try {
							if(result.versionMismatch()) {
								versionMismatch.add(file);
								String msg= MessageFormat.format(Messages.PDOMCache_VersionTooOld, new Object[] {file});
								CCorePlugin.log(msg);
								return null;
							} else {
								path2pdom.put(file, result);
							}
						} finally {
							result.releaseReadLock();
						}
					} catch(CoreException ce) {
						CCorePlugin.log(ce);
					} catch(InterruptedException ie) {
						CCorePlugin.log(ie);
					}
				}

			}
		}

		return result;
	}
}
