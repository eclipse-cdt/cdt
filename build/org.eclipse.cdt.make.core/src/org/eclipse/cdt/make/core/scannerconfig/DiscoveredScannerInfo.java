/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SymbolEntry;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

/**
 * Discovered portion of scanner configuration
 * @deprecated as of CDT 4.0.
 * @author vhirsl
 * 
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
@Deprecated
public class DiscoveredScannerInfo implements IScannerInfo {
	private IProject project;
	private LinkedHashMap<String, Boolean> discoveredPaths;
	private LinkedHashMap<String, SymbolEntry> discoveredSymbols;

	private ArrayList<String> activePaths;
	private ArrayList<String> removedPaths;
	
	private ArrayList<String> activeSymbols;
	private ArrayList<String> removedSymbols;

	private  org.eclipse.cdt.make.core.MakeScannerInfo userInfo;
	
	public DiscoveredScannerInfo(IProject project) {
		this.project = project;
	}
	
	public IProject getProject() {
		return project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.IScannerInfo#getDefinedSymbols()
	 */
	public synchronized Map<String, String> getDefinedSymbols() {
		Map<String, String> dSymbols = ScannerConfigUtil.scSymbolEntryMap2Map(discoveredSymbols);
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
	
	public LinkedHashMap<String, Boolean> getDiscoveredIncludePaths() {
		if (discoveredPaths == null) {
			return new LinkedHashMap<String, Boolean>();
		}
		return new LinkedHashMap<String, Boolean>(discoveredPaths);
	}
	public synchronized void setDiscoveredIncludePaths(LinkedHashMap<String, Boolean> paths) {
		discoveredPaths = new LinkedHashMap<String, Boolean>(paths);
		createPathLists();
	}
	
	/**
	 * Populates active and removed include path lists
	 */
	private void createPathLists() {
		List<String> aPaths = getActivePathList();
		aPaths.clear();
		List<String> rPaths = getRemovedPathList();
		rPaths.clear();
		
		Set<String> paths = discoveredPaths.keySet();
		for (String path : paths) {
			Boolean removed = discoveredPaths.get(path);
			if (removed == null || removed.booleanValue() == false) {
				aPaths.add(path);
			}
			else {
				rPaths.add(path);
			}
		}
	}

	public LinkedHashMap<String, SymbolEntry> getDiscoveredSymbolDefinitions() {
		if (discoveredSymbols == null) {
			return new LinkedHashMap<String, SymbolEntry>();
		}
		return new LinkedHashMap<String, SymbolEntry>(discoveredSymbols);
	}
	public synchronized void setDiscoveredSymbolDefinitions(LinkedHashMap<String, SymbolEntry> symbols) {
		discoveredSymbols = new LinkedHashMap<String, SymbolEntry>(symbols);
		createSymbolsLists();
	}
	
	/**
	 * Populates active and removed defined symbols sets
	 */
	private void createSymbolsLists() {
		List<String> aSymbols = getActiveSymbolsList();
		aSymbols.clear();
		List<String> rSymbols = getRemovedSymbolsList();
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
		return getActivePathList().toArray(new String[getActivePathList().size()]); 
	}
	public String[] getRemovedIncludePaths() {
		return getRemovedPathList().toArray(new String[getRemovedPathList().size()]);
	}
	
	public String[] getUserSymbolDefinitions() {
		if (userInfo == null) {
			return new String[0];
		}
		return userInfo.getPreprocessorSymbols();
	}
	public String[] getActiveSymbolDefinitions() {
		return getActiveSymbolsList().toArray(new String[getActiveSymbolsList().size()]); 
	}
	public String[] getRemovedSymbolDefinitions() {
		return getRemovedSymbolsList().toArray(new String[getRemovedSymbolsList().size()]); 
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
	
	private List<String> getActivePathList() {
		if (activePaths == null) {
			activePaths = new ArrayList<String>();
		}
		return activePaths;
	}

	private List<String> getRemovedPathList() {
		if (removedPaths == null) {
			removedPaths = new ArrayList<String>();
		}
		return removedPaths;
	}

	private List<String> getActiveSymbolsList() {
		if (activeSymbols == null) {
			activeSymbols = new ArrayList<String>();
		}
		return activeSymbols;
	}
	
	private List<String> getRemovedSymbolsList() {
		if (removedSymbols == null) {
			removedSymbols = new ArrayList<String>();
		}
		return removedSymbols;
	}
	
	public void update() throws CoreException {
		DiscoveredScannerInfoProvider.updateScannerInfo(this);
	}

	public void setUserIncludePaths(List<String> userPaths) {
		userInfo.setIncludePaths(userPaths.toArray(new String[userPaths.size()]));
	}

	public void setUserDefinedSymbols(List<String> userSymbols) {
		userInfo.setPreprocessorSymbols(userSymbols.toArray(new String[userSymbols.size()]));
	}

}
