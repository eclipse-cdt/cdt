/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v0.5 
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors:
 *     IBM Corp. - Rational Software - initial implementation
 ******************************************************************************/

package org.eclipse.cdt.internal.ui.search;

import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.IMatch;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.search.ui.text.Match;

public class NewSearchResultCollector extends BasicSearchResultCollector {
	private CSearchResult fSearch;
	private IProgressMonitor fProgressMonitor;
	private int fMatchCount;

	public NewSearchResultCollector(CSearchResult search, IProgressMonitor monitor) {
		super();
		fSearch= search;
		fProgressMonitor= monitor;
		fMatchCount = 0;
	}

	
	public void accept(IResource resource, int start, int end, ICElement enclosingElement, int accuracy) {
		fMatchCount++;
		fSearch.addMatch(new Match(enclosingElement, start, end-start));
	}


	public void done() {
	}

	public IProgressMonitor getProgressMonitor() {
		return fProgressMonitor;
	}
	
	public int getMatchCount() {
		return fMatchCount;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.search.ICSearchResultCollector#acceptMatch(org.eclipse.cdt.core.search.IMatch)
	 */
	public boolean acceptMatch(IMatch match) throws CoreException {
		
      if (super.acceptMatch(match)){
		fMatchCount++;
		int start = match.getStartOffset();
		int end = match.getEndOffset();
		fSearch.addMatch(new Match(match,start,end-start));
		return true;
      }
      
      return false;
	}


}
