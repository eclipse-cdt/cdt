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

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.search.ICSearchConstants;
import org.eclipse.cdt.core.search.OrPattern;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;


public final class TypeSearchPattern extends OrPattern {
	public TypeSearchPattern() {
		super();
		addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.NAMESPACE, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
		addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.CLASS, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
		addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.STRUCT, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
		addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.UNION, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
		addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.ENUM, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
		addPattern(SearchEngine.createSearchPattern("*", ICSearchConstants.TYPEDEF, ICSearchConstants.DECLARATIONS, false)); //$NON-NLS-1$
	}
	
	public void addDependencySearch(IPath path) {
		// convert path to absolute or the search will fail
		IResource res= CCorePlugin.getWorkspace().getRoot().findMember(path);
		if (res != null)
			path= res.getRawLocation();
		if (path != null)
			addPattern(createPattern(path.toOSString(), ICSearchConstants.INCLUDE, ICSearchConstants.REFERENCES, ICSearchConstants.EXACT_MATCH, true));
	}
}
