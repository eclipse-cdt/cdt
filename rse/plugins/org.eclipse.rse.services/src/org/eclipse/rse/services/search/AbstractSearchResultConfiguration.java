/********************************************************************************
 * Copyright (c) 2006 IBM Corporation. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is 
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * {Name} (company) - description of contribution.
 ********************************************************************************/

package org.eclipse.rse.services.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.rse.services.clientserver.SystemSearchString;


/**
 * This class represents a search result configuration. A configuration
 * consists of a search target and a search string, and belongs to a search
 * result set. Once a search has been started via a subsystem, the results
 * are stored in the configuration itself. The status of the search can also be queried from
 * the configuration.
 */
public abstract class AbstractSearchResultConfiguration implements IHostSearchResultConfiguration 
{
	
	protected IHostSearchResultSet parentResultSet;
	protected Object searchTarget;
	protected SystemSearchString searchString;
	protected Vector results;
	protected Map _containedResults;
	protected int status;
	protected ISearchService _searchService;
	protected ISearchHandler _searchHandler;

	
	/**
	 * Constructor for a result set configuration. Sets status to <code>RUNNING</code>.
	 * @param resultSet the parent result set.
	 * @param searchObject the target of the search.
	 * @param searchString the search string.
	 */
	public AbstractSearchResultConfiguration(IHostSearchResultSet resultSet, Object searchObject, SystemSearchString string, ISearchService searchService) 
	{
		results = new Vector();
		setParentResultSet(resultSet);
		setSearchTarget(searchObject);
		setSearchString(string);
		setStatus(RUNNING);
		_searchService = searchService;
		_containedResults = new HashMap();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#setParentResultSet(org.eclipse.rse.services.search.IHostSearchResultSet)
	 */
	public void setParentResultSet(IHostSearchResultSet resultSet) 
	{
		this.parentResultSet = resultSet;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#getParentResultSet()
	 */
	public IHostSearchResultSet getParentResultSet() {
		return parentResultSet;
	}

	public ISearchService getSearchService()
	{
		return _searchService;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#setSearchTarget(java.lang.Object)
	 */
	public void setSearchTarget(Object searchObject) {
		this.searchTarget = searchObject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#getSearchTarget()
	 */
	public Object getSearchTarget() {
		return searchTarget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#setSearchString(org.eclipse.rse.services.clientserver.SystemSearchString)
	 */
	public void setSearchString(SystemSearchString string) {
		this.searchString = string;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#getSearchString()
	 */
	public SystemSearchString getSearchString() 
	{
		return searchString;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#addResult(java.lang.Object)
	 */
	public void addResult(Object result) 
	{
		results.add(result);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#removeResult(java.lang.Object)
	 */
	public void removeResult(Object result) 
	{
		results.remove(result);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#removeAndAddResult(java.lang.Object, java.lang.Object)
	 */
	public void removeAndAddResult(Object oldResult, Object newResult) 
	{
		results.remove(oldResult);
		results.add(newResult);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#getResults()
	 */
	public Object[] getResults() 
	{
		return results.toArray();
	}
	
	public IHostSearchResult[] getContainedResults(Object resultContainer)
	{
		return (IHostSearchResult[])_containedResults.get(resultContainer);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#getResultsSize()
	 */
	public int getResultsSize() 
	{
		return results.size();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#removeResults()
	 */
	public void removeResults() 
	{
		results.removeAllElements();
		_containedResults.clear();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#setStatus(int)
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#getStatus()
	 */
	public int getStatus() {
		return status;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#cancel()
	 */
	public void cancel() {
		
		// if not running, return
		if (getStatus() != RUNNING) {
			return;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultConfiguration#dispose()
	 */
	public void dispose() {
		
		// cancel search first
		cancel();
		
		// remove results
		removeResults();
	}
	
	public void addResults(Object container, IHostSearchResult[] results)
	{
		_containedResults.put(container, results);
	}
	
	public void setSearchHandler(ISearchHandler searchHandler)
	{
		_searchHandler = searchHandler;
	}
	
	
	/**
	 * Gets the search handler.
	 * @return the search handler.
	 */
	public ISearchHandler getSearchHandler() 
	{
		return _searchHandler;	
	}
}