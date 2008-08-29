/*******************************************************************************
 * Copyright (c) 2007-2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.dd.debug.ui.memory.search;

import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;

public class MemorySearchResult implements ISearchResult 
{
	private ISearchQuery fQuery;
	private String fLabel;
	
	private Vector listeners = new Vector();
	
	private Vector fMatches = new Vector();
	
	public MemorySearchResult(ISearchQuery query, String label)
	{
		fQuery = query;
		fLabel = label;
	}
	
	public ImageDescriptor getImageDescriptor() {
		
		return null;
	}

	public String getLabel() {
		return fLabel;
	}

	public ISearchQuery getQuery() {
		return fQuery;
	}

	public String getTooltip() {
		
		return fLabel;
	}
	
	public MemoryMatch[] getMatches()
	{
		MemoryMatch matches[] = new MemoryMatch[fMatches.size()];
		for(int i = 0; i < matches.length; i++)
			matches[i] = (MemoryMatch) fMatches.elementAt(i);
		return matches;
	}
	
	public void addMatch(MemoryMatch address)
	{
		fMatches.addElement(address);
		fireChange();
	}
	
	private void fireChange()
	{
		Enumeration en = listeners.elements();
		while(en.hasMoreElements())
			((ISearchResultListener) en.nextElement()).searchResultChanged(new SearchResultEvent(this) {} );
	}

	public void addListener(ISearchResultListener l) {
		listeners.addElement(l);
		
	}

	public void removeListener(ISearchResultListener l) {
		listeners.removeElement(l);
	}

}
