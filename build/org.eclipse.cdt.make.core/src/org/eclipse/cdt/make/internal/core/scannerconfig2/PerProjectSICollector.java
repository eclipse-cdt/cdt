/***********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.MakeProjectNature;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CygpathTranslator;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.w3c.dom.Element;

/**
 * New per project scanner info collector
 * 
 * @since 3.0
 * @author vhirsl
 */
public class PerProjectSICollector implements IScannerInfoCollector2, IScannerInfoCollectorCleaner {
	public static final String COLLECTOR_ID = MakeCorePlugin.getUniqueIdentifier() + ".PerProjectSICollector"; //$NON-NLS-1$

	private IProject project;
	
	private Map discoveredSI;
//    private List discoveredIncludes; 
//	private List discoveredSymbols;
//	private List discoveredTSO;	// target specific options
	// cumulative values
	private List sumDiscoveredIncludes; 
	private Map sumDiscoveredSymbols;
    private boolean scPersisted = false;
	
	public PerProjectSICollector() {
        discoveredSI = new HashMap();
//		discoveredIncludes = new ArrayList();
//		discoveredSymbols = new ArrayList();
//		discoveredTSO = new ArrayList();
//		
		sumDiscoveredIncludes = new ArrayList();
		sumDiscoveredSymbols = new LinkedHashMap();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(java.lang.Object, java.util.Map)
	 */
	public synchronized void contributeToScannerConfig(Object resource, Map scannerInfo) {
		// check the resource
		String errorMessage = null;
		if (resource == null) {
			errorMessage = "resource is null";//$NON-NLS-1$
		} 
		else if (!(resource instanceof IResource)) {
			errorMessage = "resource is not an IResource";//$NON-NLS-1$
		}
		else if (((IResource) resource).getProject() == null) {
			errorMessage = "project is null";//$NON-NLS-1$
		}
		else if (((IResource) resource).getProject() != project) {
			errorMessage = "wrong project";//$NON-NLS-1$
		}
		if (errorMessage != null) {
			TraceUtil.outputError("PerProjectSICollector.contributeToScannerConfig : ", errorMessage); //$NON-NLS-1$
			return;
		}
		
        if (scPersisted) {
            // delete discovered scanner config
            discoveredSI.clear();
            // new collection cycle
            scPersisted = false;
        }
		try {
			if (project.hasNature(MakeProjectNature.NATURE_ID) && // limits to StandardMake projects
					(project.hasNature(CProjectNature.C_NATURE_ID) ||
					 project.hasNature(CCProjectNature.CC_NATURE_ID))) { 

			    for (Iterator I = scannerInfo.keySet().iterator(); I.hasNext(); ) {
			        ScannerInfoTypes siType = (ScannerInfoTypes) I.next();
                    List delta = (List) scannerInfo.get(siType);
                    
                    List discovered = (List) discoveredSI.get(siType);
                    if (discovered == null) {
                        discovered = new ArrayList(delta);
                        discoveredSI.put(siType, discovered);
                    }
                    else {
                        if (siType.equals(ScannerInfoTypes.INCLUDE_PATHS)) {
                            contribute(discovered, delta, true);
                        }
                        else {
                            contribute(discovered, delta, false);
                        }
                    }
                }
			}
		} 
		catch (CoreException e) {
			MakeCorePlugin.log(e);
		}
	}

	/**
	 * @param discovered symbols | includes | targetSpecificOptions
	 * @param delta symbols | includes | targetSpecificOptions
	 * @param ordered - to preserve order or append at the end
	 * @return true if there is a change in discovered symbols | includes | targetSpecificOptions
	 */
	private boolean contribute(List discovered, List delta, boolean ordered) {
		if (delta == null || delta.isEmpty())
			return false;
		return addItemsWithOrder(discovered, delta, ordered);
	}

	/**
	 * Adds new items to the already accumulated ones preserving order
	 *  
     * @param sumIncludes - previously accumulated items
	 * @param includes - items to be added
	 * @param ordered - to preserve order or append at the end
	 * @return boolean - true if added
	 */
	private boolean addItemsWithOrder(List sumIncludes, List includes, boolean ordered) {
		boolean addedIncludes = false;
		int prev = sumIncludes.size() - 1;	// index of previously added/found contribution in already discovered list
		for (Iterator i = includes.iterator(); i.hasNext(); ) {
			String item = (String) i.next();
			if (!sumIncludes.contains(item)) {
				sumIncludes.add(prev + 1, item);
				addedIncludes = true;
			}
			prev = ordered ? sumIncludes.indexOf(item) : sumIncludes.size() - 1;
		}
		return addedIncludes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#updateScannerConfiguration(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public synchronized void updateScannerConfiguration(IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
        IDiscoveredPathInfo pathInfo = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project);
        monitor.beginTask(MakeMessages.getString("ScannerInfoCollector.Processing"), 100); //$NON-NLS-1$
        if (pathInfo != null) {
            monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Processing")); //$NON-NLS-1$
            if (scannerConfigNeedsUpdate(pathInfo)) {
                monitor.worked(50);
                monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Updating") + project.getName()); //$NON-NLS-1$
                try {
                    // update scanner configuration
                    MakeCorePlugin.getDefault().getDiscoveryManager().updateDiscoveredInfo(pathInfo);
                    monitor.worked(50);
                } catch (CoreException e) {
                    MakeCorePlugin.log(e);
                }
            }
        }
        monitor.done();
        scPersisted = true;
	}

	/**
	 * Compare discovered include paths and symbol definitions with the ones from scanInfo.
	 * 
	 * @param scanInfo
	 * @return
	 */
	private boolean scannerConfigNeedsUpdate(IDiscoveredPathInfo discPathInfo) {
		boolean addedIncludes = includePathsNeedUpdate(discPathInfo);
		boolean addedSymbols = definedSymbolsNeedUpdate(discPathInfo);
		
		return (addedIncludes | addedSymbols);
	}

	/**
	 * Compare include paths with already discovered.
	 * 
	 * @param discPathInfo
	 * @param includes
	 * @return
	 */
	private boolean includePathsNeedUpdate(IDiscoveredPathInfo discPathInfo) {
		boolean addedIncludes = false;
        List discoveredIncludes = (List) discoveredSI.get(ScannerInfoTypes.INCLUDE_PATHS);
		if (discoveredIncludes != null) {
			// Step 1. Add discovered scanner config to the existing discovered scanner config 
			// add the includes from the latest discovery
//			if (sumDiscoveredIncludes == null) {
//				sumDiscoveredIncludes = new ArrayList(discoveredIncludes);
//				addedIncludes = true;
//			}
//			else {
//				addedIncludes = addItemsWithOrder(sumDiscoveredIncludes, discoveredIncludes, true);
//			}
// instead
            addedIncludes = addItemsWithOrder(sumDiscoveredIncludes, discoveredIncludes, true);

            // try to translate cygpaths to absolute paths
			List finalSumIncludes = translateIncludePaths(sumDiscoveredIncludes);
			
			// Step 2. Get project's scanner config
			LinkedHashMap persistedIncludes = discPathInfo.getIncludeMap();
	
			// Step 3. Merge scanner config from steps 1 and 2
			// order is important, use list to preserve it
			ArrayList persistedKeyList = new ArrayList(persistedIncludes.keySet());
			addedIncludes = addItemsWithOrder(persistedKeyList, finalSumIncludes, true);
			
			LinkedHashMap newPersistedIncludes;
			if (addedIncludes) {
				newPersistedIncludes = new LinkedHashMap(persistedKeyList.size());
				for (Iterator i = persistedKeyList.iterator(); i.hasNext(); ) {
					String include = (String) i.next();
					if (persistedIncludes.containsKey(include)) {
						newPersistedIncludes.put(include, persistedIncludes.get(include));
					}
					else {
						newPersistedIncludes.put(include, 
								((new Path(include)).toFile().exists()) ? Boolean.FALSE : Boolean.TRUE);
					}
				}
			}
			else {
				newPersistedIncludes = persistedIncludes;
			}
			
			// Step 4. Set resulting scanner config
			discPathInfo.setIncludeMap(newPersistedIncludes);
		}
		return addedIncludes;
	}

	/**
	 * Compare symbol definitions with already discovered.
	 * 
	 * @param discPathInfo
	 * @param symbols
	 * @return
	 */
	private boolean definedSymbolsNeedUpdate(IDiscoveredPathInfo discPathInfo) {
		boolean addedSymbols = false;
        List discoveredSymbols = (List) discoveredSI.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
		if (discoveredSymbols != null) {
			// Step 1. Add discovered scanner config to the existing discovered scanner config 
			// add the symbols from the latest discovery
//			if (sumDiscoveredSymbols == null) {
//				sumDiscoveredSymbols = new LinkedHashMap();
//			}
			addedSymbols = ScannerConfigUtil.scAddSymbolsList2SymbolEntryMap(sumDiscoveredSymbols, discoveredSymbols, false);
			
			// Step 2. Get project's scanner config
			LinkedHashMap persistedSymbols = discPathInfo.getSymbolMap();
			
			// Step 3. Merge scanner config from steps 1 and 2
			LinkedHashMap candidateSymbols = new LinkedHashMap(persistedSymbols);
			addedSymbols |= ScannerConfigUtil.scAddSymbolEntryMap2SymbolEntryMap(candidateSymbols, sumDiscoveredSymbols);
			
			// Step 4. Set resulting scanner config
			discPathInfo.setSymbolMap(candidateSymbols);
		}
		return addedSymbols;
	}

	/**
	 * @param sumIncludes
	 * @return
	 */
	private List translateIncludePaths(List sumIncludes) {
		List translatedIncludePaths = new ArrayList();
		for (Iterator i = sumIncludes.iterator(); i.hasNext(); ) {
			String includePath = (String) i.next();
			IPath realPath = new Path(includePath);
			if (!realPath.toFile().exists()) {
				String translatedPath = includePath;
				if (Platform.getOS().equals(Platform.OS_WIN32)) {
					translatedPath = new CygpathTranslator(project, includePath).run();
				}
				if (translatedPath != null) {
					if (!translatedPath.equals(includePath)) {
						// Check if the translated path exists
						IPath transPath = new Path(translatedPath);
						if (transPath.toFile().exists()) {
							translatedIncludePaths.add(translatedPath);
						}
						else {
							// TODO VMIR for now add even if it does not exist
							translatedIncludePaths.add(translatedPath);
						}
					}
					else {
						// TODO VMIR for now add even if it does not exist
						translatedIncludePaths.add(translatedPath);
					}
				}
				else {
					TraceUtil.outputError("CygpathTranslator unable to translate path: ",//$NON-NLS-1$
							includePath);
				}
			}
			else {
				translatedIncludePaths.add(includePath);
			}
		}
		return translatedIncludePaths;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#getCollectedScannerInfo(java.lang.Object, org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes)
     */
    public List getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
        List rv = null;
        // check the resource
        String errorMessage = null;
        if (resource == null) {
            errorMessage = "resource is null";//$NON-NLS-1$
        } 
        else if (!(resource instanceof IResource)) {
            errorMessage = "resource is not an IResource";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() == null) {
            errorMessage = "project is null";//$NON-NLS-1$
        }
        else if (((IResource) resource).getProject() != project) {
            errorMessage = "wrong project";//$NON-NLS-1$
        }
        
        if (errorMessage != null) {
            TraceUtil.outputError("PerProjectSICollector.getCollectedScannerInfo : ", errorMessage); //$NON-NLS-1$
        }
        else if (project.equals(((IResource)resource).getProject())) {
            rv = (List) discoveredSI.get(type);
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#getDefinedSymbols()
     */
    public Map getDefinedSymbols() {
        Map definedSymbols = ScannerConfigUtil.scSymbolEntryMap2Map(sumDiscoveredSymbols);
        return definedSymbols;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#getIncludePaths()
     */
    public List getIncludePaths() {
        return sumDiscoveredIncludes;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#serialize(org.w3c.dom.Element)
     */
    public void serialize(Element root) {
        // not supported in PerProject collector
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#deserialize(org.w3c.dom.Element)
     */
    public void deserialize(Element root) {
        // not supported in PerProject collector
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#deleteAllPaths(org.eclipse.core.resources.IResource)
     */
    public void deleteAllPaths(IResource resource) {
        IProject project = resource.getProject();
        if (project != null && project.equals(this.project)) {
            sumDiscoveredIncludes.clear();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#deleteAllSymbols(org.eclipse.core.resources.IResource)
     */
    public void deleteAllSymbols(IResource resource) {
        IProject project = resource.getProject();
        if (project != null && project.equals(this.project)) {
            sumDiscoveredSymbols.clear();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#deletePath(org.eclipse.core.resources.IResource, java.lang.String)
     */
    public void deletePath(IResource resource, String path) {
        IProject project = resource.getProject();
        if (project != null && project.equals(this.project)) {
            sumDiscoveredIncludes.remove(path);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorUtil#deleteSymbol(org.eclipse.core.resources.IResource, java.lang.String)
     */
    public void deleteSymbol(IResource resource, String symbol) {
        IProject project = resource.getProject();
        if (project != null && project.equals(this.project)) {
            // remove it from the Map of SymbolEntries 
            ScannerConfigUtil.removeSymbolEntryValue(symbol, sumDiscoveredSymbols);
        }
    }

}
