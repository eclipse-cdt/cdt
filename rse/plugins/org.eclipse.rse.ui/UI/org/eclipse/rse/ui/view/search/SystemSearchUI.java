/********************************************************************************
 * Copyright (c) 2003, 2006 IBM Corporation. All rights reserved.
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

package org.eclipse.rse.ui.view.search;

import org.eclipse.rse.core.SystemBasePlugin;
import org.eclipse.ui.PartInitException;


/**
 * A singleton class for dealing with Remote Search view
 */
public class SystemSearchUI {



	// singleton instance
	private static SystemSearchUI instance;
	
	// search view id
	public static final String SEARCH_RESULT_VIEW_ID = "org.eclipse.rse.ui.view.SystemSearchView";
	
	/**
	 * Constructor for SystemSearchUI.
	 */
	private SystemSearchUI() {
		super();
	}
	
	/**
	 * Get the singleton instance.
	 * @return the singleton object of this type
	 */
	public static SystemSearchUI getInstance() {
		
		if (instance == null) {
			instance = new SystemSearchUI();
		}
		
		return instance;
	}
	
	/**
	 * Activate search result view.
	 * @return <code>true</code> if successful, <code>false</false> otherwise
	 */
	public SystemSearchViewPart activateSearchResultView() {
		
		SystemSearchViewPart searchView = null;
		
		try {
			searchView = (SystemSearchViewPart)(SystemBasePlugin.getActiveWorkbenchWindow().getActivePage().showView(SystemSearchUI.SEARCH_RESULT_VIEW_ID));
		}
		catch (PartInitException e) {
			SystemBasePlugin.logError("Can not open search result view", e);
		}
		
		return searchView;
	}
}