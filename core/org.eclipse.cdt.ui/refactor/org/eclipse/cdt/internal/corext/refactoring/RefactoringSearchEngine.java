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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.search.BasicSearchMatch;
import org.eclipse.cdt.core.search.BasicSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchPattern;
import org.eclipse.cdt.core.search.ICSearchResultCollector;
import org.eclipse.cdt.core.search.ICSearchScope;
import org.eclipse.cdt.core.search.SearchEngine;
import org.eclipse.cdt.internal.corext.Assert;
import org.eclipse.cdt.internal.corext.util.CModelUtil;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;


/**
 * Convenience wrapper for <code>SearchEngine</code> - performs searching and sorts the results.
 */
public class RefactoringSearchEngine {

	//no instances
	private RefactoringSearchEngine(){
	}
	
	public static ITranslationUnit[] findAffectedTranslationUnits(final IProgressMonitor pm, ICSearchScope scope, ICSearchPattern pattern) throws CModelException {
		final Set matches= new HashSet(5);
		ICSearchResultCollector collector = new BasicSearchResultCollector();
		try {
		new SearchEngine().search(ResourcesPlugin.getWorkspace(), pattern, scope, collector, false);
		} catch (InterruptedException e){
			
		}

		List result= new ArrayList(matches.size());
		for (Iterator iter= matches.iterator(); iter.hasNext(); ) {
			IResource resource= (IResource)iter.next();
			ICElement element= CoreModel.getDefault().create(resource);
			if (element instanceof ITranslationUnit) {
				ITranslationUnit original= (ITranslationUnit)element;
				result.add(CModelUtil.toWorkingCopy(original)); // take working copy is there is one
			}
		}
		return (ITranslationUnit[])result.toArray(new ITranslationUnit[result.size()]);
	}
			
	/**
	 * Performs searching for a given <code>SearchPattern</code>.
	 * Returns SearchResultGroup[] 
	 * In each of SearchResultGroups all SearchResults are
	 * sorted backwards by <code>SearchResult#getStart()</code> 
	 * @see SearchResult
	 */			
	public static SearchResultGroup[] search(IProgressMonitor pm, ICSearchScope scope, ICSearchPattern pattern) throws CModelException {
		return search(scope, pattern, new BasicSearchResultCollector(pm));
	}
	
	public static SearchResultGroup[] search(ICSearchScope scope, ICSearchPattern pattern, BasicSearchResultCollector collector) throws CModelException {
		return search(scope, pattern, collector, null);
	}
	
	public static SearchResultGroup[] search(IProgressMonitor pm, ICSearchScope scope, ICSearchPattern pattern, ITranslationUnit[] workingCopies) throws CModelException {
		return search(scope, pattern, new BasicSearchResultCollector(pm), workingCopies);
	}
	
	public static SearchResultGroup[] search(ICSearchScope scope, ICSearchPattern pattern, BasicSearchResultCollector collector, ITranslationUnit[] workingCopies) throws CModelException {
		internalSearch(scope, pattern, collector, workingCopies);
		Set results = collector.getSearchResults();
		List resultList = new ArrayList(results);
		return groupByResource(createSearchResultArray(resultList));
	}
	
	public static SearchResultGroup[] groupByResource(BasicSearchMatch[] results){
		Map grouped= groupByResource(Arrays.asList(results));
		
		SearchResultGroup[] result= new SearchResultGroup[grouped.keySet().size()];
		int i= 0;
		for (Iterator iter= grouped.keySet().iterator(); iter.hasNext();) {
			IResource resource= (IResource)iter.next();
			List searchResults= (List)grouped.get(resource);
			result[i]= new SearchResultGroup(resource, createSearchResultArray(searchResults));
			i++;
		}
		return result;		
	}
	
	private static BasicSearchMatch[] createSearchResultArray(List searchResults){
		return (BasicSearchMatch[])searchResults.toArray(new BasicSearchMatch[searchResults.size()]);
	}
	
	private static Map groupByResource(List searchResults){
		Map grouped= new HashMap(); //IResource -> List of SearchResults
		for (Iterator iter= searchResults.iterator(); iter.hasNext();) {
			BasicSearchMatch searchResult= (BasicSearchMatch) iter.next();
			if (! grouped.containsKey(searchResult.getResource()))
				grouped.put(searchResult.getResource(), new ArrayList(1));
			((List)grouped.get(searchResult.getResource())).add(searchResult);
		}
		return grouped;
	}
	
	private static void internalSearch(ICSearchScope scope, ICSearchPattern pattern, ICSearchResultCollector collector, ITranslationUnit[] workingCopies) throws CModelException {
		if (pattern == null)
			return;
		Assert.isNotNull(scope, "scope"); //$NON-NLS-1$
		try {
			createSearchEngine(workingCopies).search(ResourcesPlugin.getWorkspace(), pattern, scope, collector, false);
		}catch (InterruptedException e){
			
		}
	}
	
	private static SearchEngine createSearchEngine(ITranslationUnit[] workingCopies){
//		if (workingCopies == null)
			return new SearchEngine();
//		else 	
//			return  new SearchEngine(workingCopies);
	}
	
//	public static ICSearchPattern createSearchPattern(ICElement[] elements, int limitTo) {
//		if (elements == null || elements.length == 0)
//			return null;
//		Set set= new HashSet(Arrays.asList(elements));
//		Iterator iter= set.iterator();
//		ICElement first= (ICElement)iter.next();
//		ICSearchPattern pattern= createSearchPattern(first, limitTo);
//		while(iter.hasNext()){
//			ICElement each= (ICElement)iter.next();
//			pattern= SearchEngine.createOrSearchPattern(pattern, createSearchPattern(each, limitTo));
//		}
//		return pattern;
//	}

//	private static ICSearchPattern createSearchPattern(ICElement element, int limitTo) {
//		return SearchEngine.createSearchPattern(element, limitTo, true);
//	}
	
}
