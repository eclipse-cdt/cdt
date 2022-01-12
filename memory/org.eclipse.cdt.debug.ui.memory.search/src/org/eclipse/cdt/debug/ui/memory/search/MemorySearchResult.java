/*******************************************************************************
 * Copyright (c) 2007-2012 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Ted R Williams (Wind River Systems, Inc.) - initial implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.ui.memory.search;

import java.util.Enumeration;
import java.util.Vector;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.search.ui.ISearchQuery;
import org.eclipse.search.ui.ISearchResult;
import org.eclipse.search.ui.ISearchResultListener;
import org.eclipse.search.ui.SearchResultEvent;

public class MemorySearchResult implements ISearchResult {
	private ISearchQuery fQuery;
	private String fLabel;

	private Vector<ISearchResultListener> listeners = new Vector<>();

	private Vector<MemoryMatch> fMatches = new Vector<>();

	public MemorySearchResult(ISearchQuery query, String label) {
		fQuery = query;
		fLabel = label;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {

		return null;
	}

	@Override
	public String getLabel() {
		return fLabel;
	}

	@Override
	public ISearchQuery getQuery() {
		return fQuery;
	}

	@Override
	public String getTooltip() {

		return fLabel;
	}

	public MemoryMatch[] getMatches() {
		MemoryMatch matches[] = new MemoryMatch[fMatches.size()];
		for (int i = 0; i < matches.length; i++)
			matches[i] = fMatches.elementAt(i);
		return matches;
	}

	public void addMatch(MemoryMatch address) {
		fMatches.addElement(address);
		fireChange();
	}

	private void fireChange() {
		Enumeration<ISearchResultListener> en = listeners.elements();
		while (en.hasMoreElements()) {
			en.nextElement().searchResultChanged(new SearchResultEvent(this) {
				private static final long serialVersionUID = -1435449002760145835L;
			});
		}
	}

	@Override
	public void addListener(ISearchResultListener l) {
		listeners.addElement(l);

	}

	@Override
	public void removeListener(ISearchResultListener l) {
		listeners.removeElement(l);
	}

}
