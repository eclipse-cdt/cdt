/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.core.browser.cache;

import java.util.ArrayList;

import org.eclipse.cdt.core.browser.TypeInfo;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class TypeMatchCollector {
	private ArrayList results= new ArrayList();
	private TypeCache typeCache;
	private IProgressMonitor progressMonitor;

	public TypeMatchCollector(TypeCache cache, IProgressMonitor monitor) {
		progressMonitor= monitor;
		typeCache= cache;
	}
	
	public IProgressMonitor getProgressMonitor() {
		return progressMonitor;
	}

	public boolean beginParsing(IPath path) {
		// check if path is in the cache already
		if (typeCache.contains(path))
			return false;

		results.clear();
		return true;
	}

	public void doneParsing(IPath path) {
		if (!results.isEmpty()) {
			// add types to cache
			typeCache.insert(path, results);
		}
	}

	public void acceptType(String name, int type, String[] enclosingNames, IResource resource, IPath path, int startOffset, int endOffset) {
		// create new type info
		TypeInfo info= new TypeInfo(name, type, enclosingNames, resource, path, startOffset, endOffset);
		results.add(info);
	}
}
