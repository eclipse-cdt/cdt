/*******************************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.corext.refactoring;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.core.resources.IResource;

public class SearchResultGroup {

	private final IResource fResouce;
	private final List fSearchResults;
	
	public SearchResultGroup(IResource res, BasicSearchMatch[] results){
		Assert.isNotNull(results);
		fResouce= res;
		fSearchResults= new ArrayList(Arrays.asList(results));//have to is this way to allow adding
	}

	public void add(BasicSearchMatch result) {
		Assert.isNotNull(result);
		fSearchResults.add(result);		
	}
	
	public IResource getResource() {
		return fResouce;
	}
	
	public BasicSearchMatch[] getSearchResults() {
		return (BasicSearchMatch[]) fSearchResults.toArray(new BasicSearchMatch[fSearchResults.size()]);
	}
	
	public static IResource[] getResources(SearchResultGroup[] searchResultGroups){
		Set resourceSet= new HashSet(searchResultGroups.length);
		for (int i= 0; i < searchResultGroups.length; i++) {
			resourceSet.add(searchResultGroups[i].getResource());
		}
		return (IResource[]) resourceSet.toArray(new IResource[resourceSet.size()]);
	}
	
	public IResource getResultGroupResource(){
		if (getSearchResults() == null || getSearchResults().length == 0)
			return null;
		return getSearchResults()[0].getResource();
	}
}
