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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Platform;


/**
 * A remote search result set represents a page in the Remote Search view.
 * A search result set contains multiple search configurations and their results.
 * This allows it to contain results from multiple connections, filters, and folders 
 * (from different systems).
 */
public class HostSearchResultSet implements IHostSearchResultSet, IAdaptable 
{
	
	protected Vector configurations;
	protected String name;

	/**
	 * Constructor to create a result set.
	 */
	public HostSearchResultSet() {
		configurations = new Vector();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#getName()
	 */
	public String getName() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#addSearchConfiguration(org.eclipse.rse.services.search.IHostSearchResultConfiguration)
	 */
	public void addSearchConfiguration(IHostSearchResultConfiguration config) {
		configurations.add(config);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#removeSearchConfiguration(org.eclipse.rse.services.search.IHostSearchResultConfiguration)
	 */
	public void removeSearchConfiguration(IHostSearchResultConfiguration config) {
		configurations.remove(config);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#getSearchConfigurations()
	 */
	public Iterator getSearchConfigurations() {
		return configurations.iterator();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#addResult(org.eclipse.rse.services.search.IHostSearchResultConfiguration, java.lang.Object)
	 */
	public void addResult(IHostSearchResultConfiguration config, Object result) {
		config.addResult(result);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#removeResult(org.eclipse.rse.services.search.IHostSearchResultConfiguration, java.lang.Object)
	 */
	public void removeResult(IHostSearchResultConfiguration config, Object result) {
		config.removeResult(result);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#getResultsForConfiguration(org.eclipse.rse.services.search.IHostSearchResultConfiguration)
	 */
	public Object[] getResultsForConfiguration(IHostSearchResultConfiguration config) {
		return config.getResults();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#getAllResults()
	 */
	public Object[] getAllResults() {
		
		List list = new ArrayList();
		
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) 
		{
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			
			list.addAll(Arrays.asList(config.getResults()));	 
		}
		
		return list.toArray();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#removeAllResults()
	 */
	public void removeAllResults() {

		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			config.removeResults();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#getNumOfResults()
	 */
	public int getNumOfResults() {
		
		int resultSize = 0;
		
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			resultSize += config.getResultsSize();
		}
		
		return resultSize;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#cancel()
	 */
	public void cancel() {
		
		// cancel each config that is running
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			
			if (config.getStatus() == IHostSearchConstants.RUNNING) {
				config.cancel();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#removeResult(java.lang.Object)
	 */
	public void removeResult(Object result) {
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			config.removeResult(result);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#removeAndAddResult(java.lang.Object, java.lang.Object)
	 */
	public void removeAndAddResult(Object oldResult, Object newResult) {
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			config.removeAndAddResult(oldResult, newResult);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#dispose()
	 */
	public void dispose() {
		
		// first cancel all configs that are still running
		cancel();
		
		// now dispose each configuration
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			config.dispose();
		}
		
		// remove all the configurations
		configurations.removeAllElements();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#isCancelled()
	 */
	public boolean isCancelled() {
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			
			// if a config is not cancelled, the search is not cancelled
			if (config.getStatus() != IHostSearchConstants.CANCELLED) {
				return false;
			}
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#isFinished()
	 */
	public boolean isFinished() {
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			
			// if a config is not finished, the search is not finished
			if (config.getStatus() != IHostSearchConstants.FINISHED) {
				return false;
			}
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#isRunning()
	 */
	public boolean isRunning() {
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			
			// if a config is running, the search is running
			if (config.getStatus() == IHostSearchConstants.RUNNING) {
				return true;
			}
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.rse.services.search.IHostSearchResultSet#isDisconnected()
	 */
	public boolean isDisconnected() {
		Iterator iter = getSearchConfigurations();
		
		while (iter.hasNext()) {
			IHostSearchResultConfiguration config = (IHostSearchResultConfiguration)iter.next();
			
			// if a config is not disconnected, the search is not disconnected
			if (config.getStatus() != IHostSearchConstants.DISCONNECTED) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		return Platform.getAdapterManager().getAdapter(this, adapter);
	}
}