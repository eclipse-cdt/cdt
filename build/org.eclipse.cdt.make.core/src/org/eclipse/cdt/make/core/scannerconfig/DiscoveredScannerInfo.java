/*******************************************************************************
 * Copyright (c) 2004, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core.scannerconfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Discovered portion of scanner configuration
 * @deprecated
 * @author vhirsl
 */
public class DiscoveredScannerInfo implements IScannerInfo {
	private IProject project;
	private LinkedHashMap discoveredPaths;
	private LinkedHashMap discoveredSymbols;

	private ArrayList activePaths;
	private ArrayList removedPaths;
	
	private ArrayList activeSymbols;
	private ArrayList removedSymbols;

	private  org.eclipse.cdt.make.core.MakeScannerInfo userInfo;
	
	/**
	 * @param project
	 */
	public DiscoveredScannerInfo(IProject project) {
		this.project = project;
	}
	
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfo#getDefinedSymbols()
	 */
	public synchronized Map getDefinedSymbols() {
		Map dSymbols = ScannerConfigUtil.scSymbolEntryMap2Map(discoveredSymbols);
		dSymbols.putAll(userInfo.getDefinedSymbols());
		return dSymbols;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfo#getIncludePaths()
	 */
	public synchronized String[] getIncludePaths() {
		String[] iPaths = new String[getUserIncludePaths().length + getActiveIncludePaths().length];
		System.arraycopy(getUserIncludePaths(), 0, iPaths, 0, getUserIncludePaths().length);
		System.arraycopy(getActiveIncludePaths(), 0, iPaths, getUserIncludePaths().length, getActiveIncludePaths().length);
		return iPaths;
	}

	public  org.eclipse.cdt.make.core.MakeScannerInfo getUserScannerInfo() {
		return userInfo;
	}

	public synchronized void setUserScannerInfo( org.eclipse.cdt.make.core.MakeScannerInfo info) {
		userInfo = info;
	}
	
	public LinkedHashMap getDiscoveredIncludePaths() {
		if (discoveredPaths == null) {
			return new LinkedHashMap();
		}
		return new LinkedHashMap(discoveredPaths);
	}
	public synchronized void setDiscoveredIncludePaths(LinkedHashMap paths) {
		discoveredPaths = new LinkedHashMap(paths);
		createPathLists();
	}
	
	/**
	 * Populates active and removed include path lists
	 */
	private void createPathLists() {
		List aPaths = getActivePathList();
		aPaths.clear();
		List rPaths = getRemovedPathList();
		rPaths.clear();
		
		for (Iterator i = discoveredPaths.keySet().iterator(); i.hasNext(); ) {
			String path = (String) i.next();
			Boolean removed = (Boolean) discoveredPaths.get(path);
			if (removed == null || removed.booleanValue() == false) {
				aPaths.add(path);
			}
			else {
				rPaths.add(path);
			}
		}
	}

	public LinkedHashMap getDiscoveredSymbolDefinitions() {
		if (discoveredSymbols == null) {
			return new LinkedHashMap();
		}
		return new LinkedHashMap(discoveredSymbols);
	}
	public synchronized void setDiscoveredSymbolDefinitions(LinkedHashMap symbols) {
		discoveredSymbols = new LinkedHashMap(symbols);
		createSymbolsLists();
	}
	
	/**
	 * Populates active and removed defined symbols sets
	 */
	private void createSymbolsLists() {
		List aSymbols = getActiveSymbolsList();
		aSymbols.clear();
		List rSymbols = getRemovedSymbolsList();
		rSymbols.clear();
		
		aSymbols.addAll(ScannerConfigUtil.scSymbolsSymbolEntryMap2List(discoveredSymbols, true));
		rSymbols.addAll(ScannerConfigUtil.scSymbolsSymbolEntryMap2List(discoveredSymbols, false));
	}

	public String[] getUserIncludePaths() {
		if (userInfo == null) {
			return new String[0];
		}
		return userInfo.getIncludePaths();
	}
	public String[] getActiveIncludePaths() {
		return (String[]) getActivePathList().toArray(new String[getActivePathList().size()]); 
	}
	public String[] getRemovedIncludePaths() {
		return (String[])getRemovedPathList().toArray(new String[getRemovedPathList().size()]);
	}
	
	public String[] getUserSymbolDefinitions() {
		if (userInfo == null) {
			return new String[0];
		}
		return userInfo.getPreprocessorSymbols();
	}
	public String[] getActiveSymbolDefinitions() {
		return (String[]) getActiveSymbolsList().toArray(new String[getActiveSymbolsList().size()]); 
	}
	public String[] getRemovedSymbolDefinitions() {
		return (String[]) getRemovedSymbolsList().toArray(new String[getRemovedSymbolsList().size()]); 
	}
	public String[] getPreprocessorSymbols() {
		// user specified + active
		String[] userSymbols = getUserSymbolDefinitions();
		String[] discActiveSymbols = getActiveSymbolDefinitions();
		String[] rv = new String[userSymbols.length + discActiveSymbols.length];
		System.arraycopy(userSymbols, 0, rv, 0, userSymbols.length);
		System.arraycopy(discActiveSymbols, 0, rv, userSymbols.length, discActiveSymbols.length);
		return rv;
	}
	
	private List getActivePathList() {
		if (activePaths == null) {
			activePaths = new ArrayList();
		}
		return activePaths;
	}

	private List getRemovedPathList() {
		if (removedPaths == null) {
			removedPaths = new ArrayList();
		}
		return removedPaths;
	}

	private List getActiveSymbolsList() {
		if (activeSymbols == null) {
			activeSymbols = new ArrayList();
		}
		return activeSymbols;
	}
	
	private List getRemovedSymbolsList() {
		if (removedSymbols == null) {
			removedSymbols = new ArrayList();
		}
		return removedSymbols;
	}
	
	public void update() throws CoreException {
		DiscoveredScannerInfoProvider.updateScannerInfo(this);
	}

	/**
	 * @param userPaths
	 */
	public void setUserIncludePaths(List userPaths) {
		userInfo.setIncludePaths((String[]) userPaths.toArray(new String[userPaths.size()]));
	}

	/**
	 * @param userSymbols
	 */
	public void setUserDefinedSymbols(List userSymbols) {
		userInfo.setPreprocessorSymbols((String[]) userSymbols.toArray(new String[userSymbols.size()]));
	}

}
