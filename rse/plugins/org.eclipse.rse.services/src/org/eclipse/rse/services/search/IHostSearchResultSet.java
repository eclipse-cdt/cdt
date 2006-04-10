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

import java.util.Iterator;

/**
 * A remote search result set represents a page in the Remote Search view.
 * A search result set contains multiple search configurations and their results.
 * This allows it to contain results from multiple connections, filters, and folders 
 * (from different systems).
 */
public interface IHostSearchResultSet {
	
	/**
	 * Sets the name of the search.
	 * @param name the name of the search.
	 */
	public void setName(String name);
	
	/**
	 * Gets the name of the search that is being run.
	 * @return the name of the search.
	 */
	public String getName();
	
	/**
	 * Add a search configuration.
	 * @param config a search configuration.
	 */
	public void addSearchConfiguration(IHostSearchResultConfiguration config);
	
	/**
	 * Removes a search configuration.
	 * @param config a search configuration.
	 */
	public void removeSearchConfiguration(IHostSearchResultConfiguration config);
	
	/**
	 * Returns an iterator over search configurations.
	 * @return an iterator over search configurations.
	 */
	public Iterator getSearchConfigurations();
	
	/**
	 * Add a result for a search configuration.
	 * @param config a search configuration that was previously added.
	 * @param result a search result.
	 */
	public void addResult(IHostSearchResultConfiguration config, Object result);
	
	/**
	 * Removes a result from a search configuration.
	 * @param config a search configuration that was previously added.
	 * @param result a search result.
	 */
	public void removeResult(IHostSearchResultConfiguration config, Object result);
	
	/**
	 * Removes a result from all search configurations where it exists.
	 * @param result a search result.
	 */
	public void removeResult(Object result);
	
	/**
	 * Removes the old result from configurations where it is found, and 
	 * add the new result to those configurations. 
	 * @param oldResult the old result.
	 * @param newResult the new result.
	 */
	public void removeAndAddResult(Object oldResult, Object newResult);
	
	/**
	 * Returns the results of a particular search configuration.
	 * @param config a search configuration.
	 * @return the results for the given search configuration.
	 */
	public Object[] getResultsForConfiguration(IHostSearchResultConfiguration config);
	
	/**
	 * Returns all results of the search.
	 * @return all results of the search.
	 */
	public Object[] getAllResults();
	
	/**
	 * Removes all results of the search.
	 */
	public void removeAllResults();
	
	/**
	 * Gets the number of results.
	 * @return the number of results.
	 */
	public int getNumOfResults();
	
	/**
	 * Cancels the search. Cancel those configurations that are still running.
	 */
	public void cancel();
	
	/**
	 * Removes all configurations. First cancels the search, then calls the <code>dispose</code> method of
	 * the configurations before removing them.
	 */
	public void dispose();
	
	/**
	 * Returns whether search is running. A search is running if any of the configurations is running. 
	 * @return <code>true</code> if the search is running, <code>false</code> otherwise.
	 */
	public boolean isRunning();
	
	/**
	 * Returns whether the search is cancelled. A search is cancelled if all of the configurations are
	 * cancelled.
	 * @return <code>true</code> if the search is cancelled, <code>false</code> otherwise.
	 */
	public boolean isCancelled();
	
	/**
	 * Returns whether the search is finished. A search is finished if all the configurations are finished.
	 * @return <code>true</code> if the search is finished, <code>false</code> otherwise.
	 */
	public boolean isFinished();
	
	/**
	 * Returns whether the search is disconnected. A search is disconnected if all the configurations are
	 * disconnected.
	 * @return <code>true</code> if the search is disconnected, <code>false</code> otherwise.
	 */
	public boolean isDisconnected();
}