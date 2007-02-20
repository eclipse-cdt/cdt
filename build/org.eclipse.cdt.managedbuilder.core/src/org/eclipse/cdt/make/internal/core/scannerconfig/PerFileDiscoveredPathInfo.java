package org.eclipse.cdt.make.internal.core.scannerconfig;


public class PerFileDiscoveredPathInfo /*implements IPerFileDiscoveredPathInfo, IDiscoveredScannerInfoSerializable*/{
//	public static final String INCLUDE_PATH = "includePath"; //$NON-NLS-1$
//	public static final String PATH = "path"; //$NON-NLS-1$
//	public static final String DEFINED_SYMBOL = "definedSymbol"; //$NON-NLS-1$
//	public static final String SYMBOL = "symbol"; //$NON-NLS-1$
//	public static final String REMOVED = "removed"; //$NON-NLS-1$
//
////	final private IProject project;
//	private final IConfiguration config;
////	private LinkedHashMap discoveredPaths;
////	private LinkedHashMap discoveredSymbols;
//
//	private List activePaths;
//	private Map activeSymbols;
//
//	public PerFileDiscoveredPathInfo(IConfiguration cfg) {
//		this.config = cfg;
////		discoveredPaths = new LinkedHashMap();
////		discoveredSymbols = new LinkedHashMap();
//	}
//
//	public IProject getProject() {
//		return config.getOwner().getProject();
//	}
//
//	public synchronized Map getSymbols() {
//		if (activeSymbols == null) {
//			createSymbolsMap();
//		}
//		Map dSymbols = ScannerConfigUtil.scSymbolEntryMap2Map(discoveredSymbols);
//		return dSymbols;
//	}
//
//	public synchronized IPath[] getIncludePaths() {
//		if ( activePaths == null) {
//			createPathLists();
//		}
//		return (IPath[])activePaths.toArray(new IPath[activePaths.size()]);
//	}
//
//	public LinkedHashMap getIncludeMap() {
//		return new LinkedHashMap(discoveredPaths);
//	}
//
//	public synchronized void setIncludeMap(LinkedHashMap paths) {
//		discoveredPaths = new LinkedHashMap(paths);
//		activePaths = null;
//	}
//	
//	/**
//	 * Populates active and removed include path lists
//	 */
//	private void createPathLists() {
//		List aPaths = getActivePathList();
//		aPaths.clear();
//		
//		for (Iterator i = discoveredPaths.keySet().iterator(); i.hasNext(); ) {
//			String path = (String) i.next();
//			Boolean removed = (Boolean) discoveredPaths.get(path);
//			if (removed == null || removed.booleanValue() == false) {
//				aPaths.add(new Path(path));
//			}
//		}
//	}
//
//	public LinkedHashMap getSymbolMap() {
//		return new LinkedHashMap(discoveredSymbols);
//	}
//	
//	public synchronized void setSymbolMap(LinkedHashMap symbols) {
//		discoveredSymbols = new LinkedHashMap(symbols);
//		activeSymbols = null;
//	}
//	
//	/**
//	 * Populates active symbols sets
//	 */
//	private void createSymbolsMap() {
//		Map aSymbols = getActiveSymbolsMap();
//		aSymbols.clear();
//		
//		aSymbols.putAll(ScannerConfigUtil.scSymbolEntryMap2Map(discoveredSymbols));
//	}
//
//	private List getActivePathList() {
//		if (activePaths == null) {
//			activePaths = new ArrayList();
//		}
//		return activePaths;
//	}
//
//	private Map getActiveSymbolsMap() {
//		if (activeSymbols == null) {
//			activeSymbols = new HashMap();
//		}
//		return activeSymbols;
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#serialize(org.w3c.dom.Element)
//	 */
//	public void serialize(Element collectorElem) {
//		Document doc = collectorElem.getOwnerDocument(); 
//		
//		Map includes = getIncludeMap();
//		Iterator iter = includes.keySet().iterator();
//		while (iter.hasNext()) {
//			Element pathElement = doc.createElement(INCLUDE_PATH);
//			String include = (String)iter.next();
//			pathElement.setAttribute(PATH, include);
//			Boolean removed = (Boolean)includes.get(include);
//			if (removed != null && removed.booleanValue() == true) {
//				pathElement.setAttribute(REMOVED, "true"); //$NON-NLS-1$
//			}
//			collectorElem.appendChild(pathElement);
//		}
//		// Now do the same for the symbols
//		Map symbols = getSymbolMap();
//		iter = symbols.keySet().iterator();
//		while (iter.hasNext()) {
//			String symbol = (String)iter.next();
//			SymbolEntry se = (SymbolEntry)symbols.get(symbol);
//			for (Iterator i = se.getActiveRaw().iterator(); i.hasNext();) {
//				String value = (String)i.next();
//				Element symbolElement = doc.createElement(DEFINED_SYMBOL);
//				symbolElement.setAttribute(SYMBOL, value);
//				collectorElem.appendChild(symbolElement);
//			}
//			for (Iterator i = se.getRemovedRaw().iterator(); i.hasNext();) {
//				String value = (String)i.next();
//				Element symbolElement = doc.createElement(DEFINED_SYMBOL);
//				symbolElement.setAttribute(SYMBOL, value);
//				symbolElement.setAttribute(REMOVED, "true"); //$NON-NLS-1$
//				collectorElem.appendChild(symbolElement);
//			}
//		}
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#deserialize(org.w3c.dom.Element)
//	 */
//	public void deserialize(Element collectorElem) {
//		LinkedHashMap includes = getIncludeMap();
//		LinkedHashMap symbols = getSymbolMap();
//
//		Node child = collectorElem.getFirstChild();
//		while (child != null) {
//			if (child.getNodeName().equals(INCLUDE_PATH)) {
//				// Add the path to the property list
//				includes.put( ((Element)child).getAttribute(PATH), Boolean.valueOf( ((Element)child).getAttribute(REMOVED)));
//			} else if (child.getNodeName().equals(DEFINED_SYMBOL)) {
//				// Add the symbol to the symbol list
//				String symbol = ((Element)child).getAttribute(SYMBOL);
//				String removed = ((Element)child).getAttribute(REMOVED);
//				boolean bRemoved = (removed != null && removed.equals("true")); //$NON-NLS-1$
//				ScannerConfigUtil.scAddSymbolString2SymbolEntryMap(symbols, symbol, !bRemoved);
//			}
//			child = child.getNextSibling();
//		}
//		
//		setIncludeMap(includes);
//		setSymbolMap(symbols);
//	}
//
//	/* (non-Javadoc)
//	 * @see org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore.IDiscoveredScannerInfoSerializable#getCollectorId()
//	 */
//	public String getCollectorId() {
//		return PerProjectSICollector.COLLECTOR_ID;
//	}
//
//    /* (non-Javadoc)
//     * @see org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo#getSerializable()
//     */
//    public IDiscoveredScannerInfoSerializable getSerializable() {
//        return this;
//    }
//
//
}
