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

import org.eclipse.rse.services.clientserver.SystemSearchString;

/**
 * An interface representing a search result configuration. 
 */
public interface IHostSearchResultConfiguration extends IHostSearchConstants {
	
	/**
	 * Sets the parent result set.
	 * @param resultSet the parent result set.
	 */
	public void setParentResultSet(IHostSearchResultSet resultSet);
	
	/**
	 * Gets the parent result set.
	 * @return the parent result set.
	 */
	public IHostSearchResultSet getParentResultSet();
	
	/**
	 * Gets the results contained within the resultContainer
	 * @param resultContainer
	 * @return
	 */
	public IHostSearchResult[] getContainedResults(Object resultContainer);

	/**
	 * Sets the object to be searched.
	 * @param searchObject the object to be searched.
	 */
	public void setSearchTarget(Object searchObject);
	
	/**
	 * Gets the object to be searched.
	 * @return the object to be searched.
	 */
	public Object getSearchTarget();
	
	/**
	 * Sets the search string.
	 * @param string the search string.
	 */
	public void setSearchString(SystemSearchString string);
	
	/**
	 * Gets the search string.
	 * @return string the search string.
	 */
	public SystemSearchString getSearchString();
	
	/**
	 * Adds a search result.
	 * @param result a search result.
	 */
	public void addResult(Object result);
	
	/**
	 * Adds a set of search results along their associated container
	 * @param container
	 * @param results
	 */
	public void addResults(Object container, IHostSearchResult[] results);
	
	/**
	 * Removes a search result.
	 * @param result a search result.
	 */
	public void removeResult(Object result);
	
	/**
	 * Removes the old result and adds a new result.
	 * @param oldResult the old result.
	 * @param newResult the new result.
	 */
	public void removeAndAddResult(Object oldResult, Object newResult);
	
	/**
	 * Gets search results.
	 * @return search results.
	 */
	public Object[] getResults();
	
	/**
	 * Gets the size of the results.
	 * @return the size of the results.
	 */
	public int getResultsSize();
	
	/**
	 * Removes all search results.
	 */
	public void removeResults();
	
	/**
	 * Sets the status of the search. One of <code>RUNNING</code>, <code>FINISHED</code>,
	 * <code>CANCELLED</code>, or <code>DISCONNECTED</code>.
	 * @param the status.
	 */
	public void setStatus(int status);
	
	/**
	 * Gets the status of the search. One of <code>RUNNING</code>, <code>FINISHED</code>,
	 * <code>CANCELLED</code>, or <code>DISCONNECTED</code>.
	 * @return the status of the search.
	 */
	public int getStatus();
	
	/**
	 * Cancels the search if it is running.
	 */
	public void cancel();
	
	/**
	 * Cancels the search and then removes the search results. Implementors should call <code>super</code> first.
	 */
	public void dispose();
	
	/**
	 * Sets the search handler
	 * @param handler
	 */
	public void setSearchHandler(ISearchHandler handler);	
}