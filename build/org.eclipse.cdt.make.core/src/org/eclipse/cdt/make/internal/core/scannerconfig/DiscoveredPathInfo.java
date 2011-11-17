/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *     IBM Corporation
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.internal.core.SafeStringInterner;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredScannerInfoSerializable;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerProjectDiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SymbolEntry;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class DiscoveredPathInfo implements IPerProjectDiscoveredPathInfo, IDiscoveredScannerInfoSerializable {
	public static final String INCLUDE_PATH = "includePath"; //$NON-NLS-1$
	public static final String PATH = "path"; //$NON-NLS-1$
	public static final String DEFINED_SYMBOL = "definedSymbol"; //$NON-NLS-1$
	public static final String SYMBOL = "symbol"; //$NON-NLS-1$
	public static final String REMOVED = "removed"; //$NON-NLS-1$

	final private IProject project;
	private LinkedHashMap<String, Boolean> discoveredPaths;
	private LinkedHashMap<String, SymbolEntry> discoveredSymbols;

	private List<Path> activePaths;
	private Map<String, String> activeSymbols;

	public DiscoveredPathInfo(IProject project) {
		this.project = project;
		discoveredPaths = new LinkedHashMap<String, Boolean>();
		discoveredSymbols = new LinkedHashMap<String, SymbolEntry>();
	}

	@Override
	public IProject getProject() {
		return project;
	}

	@Override
	public synchronized Map<String, String> getSymbols() {
		if (activeSymbols == null) {
			createSymbolsMap();
		}
		Map<String, String> dSymbols = ScannerConfigUtil.scSymbolEntryMap2Map(discoveredSymbols);
		return dSymbols;
	}

	@Override
	public synchronized IPath[] getIncludePaths() {
		if ( activePaths == null) {
			createPathLists();
		}
		return activePaths.toArray(new IPath[activePaths.size()]);
	}

	@Override
	public LinkedHashMap<String, Boolean> getIncludeMap() {
		return new LinkedHashMap<String, Boolean>(discoveredPaths);
	}

	@Override
	public synchronized void setIncludeMap(LinkedHashMap<String, Boolean> paths) {
		discoveredPaths = SafeStringInterner.safeIntern(new LinkedHashMap<String, Boolean>(paths));
		activePaths = null;
	}

	/**
	 * Populates active and removed include path lists
	 */
	private void createPathLists() {
		List<Path> aPaths = getActivePathList();
		aPaths.clear();

		Set<String> paths = discoveredPaths.keySet();
		for (String path : paths) {
			Boolean removed = discoveredPaths.get(path);
			if (removed == null || removed.booleanValue() == false) {
				aPaths.add(new Path(path));
			}
		}
	}

	@Override
	public LinkedHashMap<String, SymbolEntry> getSymbolMap() {
		return new LinkedHashMap<String, SymbolEntry>(discoveredSymbols);
	}

	@Override
	public synchronized void setSymbolMap(LinkedHashMap<String, SymbolEntry> symbols) {
		discoveredSymbols = SafeStringInterner.safeIntern(new LinkedHashMap<String, SymbolEntry>(symbols));
		activeSymbols = null;
	}

	/**
	 * Populates active symbols sets
	 */
	private void createSymbolsMap() {
		Map<String, String> aSymbols = getActiveSymbolsMap();
		aSymbols.clear();

		aSymbols.putAll(SafeStringInterner.safeIntern(ScannerConfigUtil.scSymbolEntryMap2Map(discoveredSymbols)));
	}

	private List<Path> getActivePathList() {
		if (activePaths == null) {
			activePaths = new ArrayList<Path>();
		}
		return activePaths;
	}

	private Map<String, String> getActiveSymbolsMap() {
		if (activeSymbols == null) {
			activeSymbols = new HashMap<String, String>();
		}
		return activeSymbols;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#serialize(org.w3c.dom.Element)
	 */
	@Override
	public void serialize(Element collectorElem) {
		Document doc = collectorElem.getOwnerDocument();

		Map<String, Boolean> includes = getIncludeMap();
		Set<String> includesSet = includes.keySet();
		for (String include : includesSet) {
			Element pathElement = doc.createElement(INCLUDE_PATH);
			pathElement.setAttribute(PATH, include);
			Boolean removed = includes.get(include);
			if (removed != null && removed.booleanValue() == true) {
				pathElement.setAttribute(REMOVED, "true"); //$NON-NLS-1$
			}
			collectorElem.appendChild(pathElement);
		}
		// Now do the same for the symbols
		Map<String, SymbolEntry> symbols = getSymbolMap();
		Set<String> symbolsSet = symbols.keySet();
		for (String symbol : symbolsSet) {
			SymbolEntry se = symbols.get(symbol);
			if (se != null)
			{
				List<String> activeValues = se.getActiveRaw();
				for (String value : activeValues) {
					Element symbolElement = doc.createElement(DEFINED_SYMBOL);
					symbolElement.setAttribute(SYMBOL, value);
					collectorElem.appendChild(symbolElement);
				}
				List<String> removedValues = se.getRemovedRaw();
				for (String value : removedValues) {
					Element symbolElement = doc.createElement(DEFINED_SYMBOL);
					symbolElement.setAttribute(SYMBOL, value);
					symbolElement.setAttribute(REMOVED, "true"); //$NON-NLS-1$
					collectorElem.appendChild(symbolElement);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#deserialize(org.w3c.dom.Element)
	 */
	@Override
	public void deserialize(Element collectorElem) {
		LinkedHashMap<String, Boolean> includes = getIncludeMap();
		LinkedHashMap<String, SymbolEntry> symbols = getSymbolMap();

		Node child = collectorElem.getFirstChild();
		while (child != null) {
			if (child.getNodeName().equals(INCLUDE_PATH)) {
				// Add the path to the property list
				includes.put( SafeStringInterner.safeIntern(((Element)child).getAttribute(PATH)), Boolean.valueOf( ((Element)child).getAttribute(REMOVED)));
			} else if (child.getNodeName().equals(DEFINED_SYMBOL)) {
				// Add the symbol to the symbol list
				String symbol = SafeStringInterner.safeIntern(((Element)child).getAttribute(SYMBOL));
				String removed = ((Element)child).getAttribute(REMOVED);
				boolean bRemoved = (removed != null && removed.equals("true")); //$NON-NLS-1$
				ScannerConfigUtil.scAddSymbolString2SymbolEntryMap(symbols, symbol, !bRemoved);
			}
			child = child.getNextSibling();
		}

		setIncludeMap(includes);
		setSymbolMap(symbols);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#getCollectorId()
	 */
	@Override
	public String getCollectorId() {
		return PerProjectSICollector.COLLECTOR_ID;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSerializable()
     */
    @Override
	public IDiscoveredScannerInfoSerializable getSerializable() {
        return this;
    }

}
