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
package org.eclipse.cdt.internal.ui.opentype;

import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class TypeSearchResultCollector extends BasicSearchResultCollector {

	private IProgressMonitor monitor;
	
	public TypeSearchResultCollector() {
		super();
	}
	
	public IProgressMonitor getProgressMonitor() {
		return monitor;
	}

	public void setProgressMonitor(IProgressMonitor monitor) {
		this.monitor = monitor;
	}
	
	public IMatch createMatch(Object fileResource, int start, int end, ISourceElementCallbackDelegate node ) throws CoreException 
	{
		TypeSearchMatch result = new TypeSearchMatch();
		return super.createMatch( result, fileResource, start, end, node );
	}

	public boolean acceptMatch(IMatch match) throws CoreException {
		// filter out unnamed structs
		TypeSearchMatch result = (TypeSearchMatch) match;
		if (result.getName().length() == 0)
			return false;
		else
			return super.acceptMatch(match);
	}
}
