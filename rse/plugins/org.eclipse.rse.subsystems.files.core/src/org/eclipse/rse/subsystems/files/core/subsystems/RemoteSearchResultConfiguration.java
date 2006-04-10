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

package org.eclipse.rse.subsystems.files.core.subsystems;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import org.eclipse.rse.services.clientserver.SystemSearchString;
import org.eclipse.rse.services.search.IHostSearchResult;
import org.eclipse.rse.services.search.IHostSearchResultConfiguration;
import org.eclipse.rse.services.search.IHostSearchResultSet;
import org.eclipse.rse.services.search.ISearchHandler;


/**
 * This class represents a search result configuration. A configuration
 * consists of a search target and a search string, and belongs to a search
 * result set. Once a search has been started via a subsystem, the results
 * are stored in the configuration itself. The status of the search can also be queried from
 * the configuration.
 */
public class RemoteSearchResultConfiguration implements IHostSearchResultConfiguration {
	
	protected IHostSearchResultSet parentResultSet;
	protected Object searchTarget;
	protected SystemSearchString searchString;
	protected Vector results;
	protected int status;
	protected Map _containedResults;
	protected ISearchHandler _searchHandler;
	
	/**
	 * Constructor for a result set configuration. Sets status to <code>RUNNING</code>.
	 * @param resultSet the parent result set.
	 * @param searchObject the target of the search.
	 * @param searchString the search string.
	 */
	public RemoteSearchResultConfiguration(IHostSearchResultSet resultSet, Object searchObject, SystemSearchString string) {
		results = new Vector();
		setParentResultSet(resultSet);
		setSearchTarget(searchObject);
		setSearchString(string);
		setStatus(RUNNING);
		_containedResults = new HashMap();
	}
	
	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#setParentResultSet(org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultSet)
	 */
	public void setParentResultSet(IHostSearchResultSet resultSet) {
		this.parentResultSet = resultSet;
	}
	
	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#getParentResultSet()
	 */
	public IHostSearchResultSet getParentResultSet() {
		return parentResultSet;
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#setSearchTarget(java.lang.Object)
	 */
	public void setSearchTarget(Object searchObject) {
		this.searchTarget = searchObject;
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#getSearchTarget()
	 */
	public Object getSearchTarget() {
		return searchTarget;
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#setSearchString(org.eclipse.rse.services.clientserver.SystemSearchString)
	 */
	public void setSearchString(SystemSearchString string) {
		this.searchString = string;
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#getSearchString()
	 */
	public SystemSearchString getSearchString() {
		return searchString;
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#addResult(java.lang.Object)
	 */
	public void addResult(Object result) {
		results.add(result);
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#removeResult(java.lang.Object)
	 */
	public void removeResult(Object result) {
		results.remove(result);
	}
	
	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#removeAndAddResult(java.lang.Object, java.lang.Object)
	 */
	public void removeAndAddResult(Object oldResult, Object newResult) {
		results.remove(oldResult);
		results.add(newResult);
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#getResults()
	 */
	public Object[] getResults() {
		return results.toArray();
	}
	
	public IHostSearchResult[] getContainedResults(Object resultContainer)
	{
		return (IHostSearchResult[])_containedResults.get(resultContainer);
	}
	
	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#getResultsSize()
	 */
	public int getResultsSize() {
		return results.size();
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#removeResults()
	 */
	public void removeResults() {
		results.removeAllElements();
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#setStatus(int)
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#getStatus()
	 */
	public int getStatus() {
		return status;
	}
	
	/**
	 * Does nothing.
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#cancel()
	 */
	public void cancel() {
		
		// if not running, return
		if (getStatus() != RUNNING) {
			return;
		}
	}

	/**
	 * @see org.eclipse.rse.core.subsystems.files.core.subsystems.IHostSearchResultConfiguration#dispose()
	 */
	public void dispose() {
		
		// cancel search first
		cancel();
		
		// remove results
		removeResults();
	}

	public void addResults(Object container, IHostSearchResult[] results)
	{
		addResult(container);
		_containedResults.put(container, results);
		
	}

	public void setSearchHandler(ISearchHandler handler)
	{
		_searchHandler = handler;
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