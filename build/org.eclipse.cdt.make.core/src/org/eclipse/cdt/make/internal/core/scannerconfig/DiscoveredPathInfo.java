/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others. All rights reserved. This
 * program and the accompanying materials are made available under the terms of
 * the Common Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: QNX Software Systems - initial API and implementation
 ******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;


public class DiscoveredPathInfo implements IDiscoveredPathInfo {
	final private IProject project;
	private LinkedHashMap discoveredPaths;
	private LinkedHashMap discoveredSymbols;

	private List activePaths;
	private Map activeSymbols;

	public DiscoveredPathInfo(IProject project) {
		this.project = project;
	}

	public IProject getProject() {
		return project;
	}

	public synchronized Map getSymbols() {
		if (activeSymbols == null) {
			createSymbolsMap();
		}
		Map dSymbols = ScannerConfigUtil.scSymbolEntryMap2Map(discoveredSymbols);
		return dSymbols;
	}

	public synchronized IPath[] getIncludePaths() {
		if ( activePaths == null) {
			createPathLists();
		}
		return (IPath[])activePaths.toArray(new IPath[activePaths.size()]);
	}

	public LinkedHashMap getIncludeMap() {
		if (discoveredPaths == null) {
			return new LinkedHashMap();
		}
		return new LinkedHashMap(discoveredPaths);
	}

	public synchronized void setIncludeMap(LinkedHashMap paths) {
		discoveredPaths = new LinkedHashMap(paths);
		activePaths = null;
	}
	
	/**
	 * Populates active and removed include path lists
	 */
	private void createPathLists() {
		List aPaths = getActivePathList();
		aPaths.clear();
		
		for (Iterator i = discoveredPaths.keySet().iterator(); i.hasNext(); ) {
			String path = (String) i.next();
			Boolean removed = (Boolean) discoveredPaths.get(path);
			if (removed == null || removed.booleanValue() == false) {
				aPaths.add(new Path(path));
			}
		}
	}

	public LinkedHashMap getSymbolMap() {
		if (discoveredSymbols == null) {
			return new LinkedHashMap();
		}
		return new LinkedHashMap(discoveredSymbols);
	}
	
	public synchronized void setSymbolMap(LinkedHashMap symbols) {
		discoveredSymbols = new LinkedHashMap(symbols);
		activeSymbols = null;
	}
	
	/**
	 * Populates active symbols sets
	 */
	private void createSymbolsMap() {
		Map aSymbols = getActiveSymbolsMap();
		aSymbols.clear();
		
		aSymbols.putAll(ScannerConfigUtil.scSymbolEntryMap2Map(discoveredSymbols));
	}

	private List getActivePathList() {
		if (activePaths == null) {
			activePaths = new ArrayList();
		}
		return activePaths;
	}

	private Map getActiveSymbolsMap() {
		if (activeSymbols == null) {
			activeSymbols = new HashMap();
		}
		return activeSymbols;
	}
}
