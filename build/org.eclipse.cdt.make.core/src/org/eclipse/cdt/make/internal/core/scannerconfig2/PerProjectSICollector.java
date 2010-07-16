/*******************************************************************************
 *  Copyright (c) 2004, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *    IBM - Initial API and implementation
 *    Anton Leherbauer (Wind River Systems)
 *    Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig2;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IPerProjectDiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathContainer;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CygpathTranslator;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.SymbolEntry;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SafeRunner;
import org.w3c.dom.Element;

/**
 * New per project scanner info collector
 * 
 * @since 3.0
 * @author vhirsl
 */
public class PerProjectSICollector implements IScannerInfoCollector3, IScannerInfoCollectorCleaner {
	public static final String COLLECTOR_ID = MakeCorePlugin.getUniqueIdentifier() + ".PerProjectSICollector"; //$NON-NLS-1$

	private IProject project;
	private InfoContext context;
	private boolean isBuiltinConfig= false;
	
	private Map<ScannerInfoTypes, List<String>> discoveredSI;
//    private List discoveredIncludes; 
//	private List discoveredSymbols;
//	private List discoveredTSO;	// target specific options
	// cumulative values
	private List<String> sumDiscoveredIncludes; 
	private Map<String, SymbolEntry> sumDiscoveredSymbols;
    private boolean scPersisted = false;
	
	public PerProjectSICollector() {
        discoveredSI = new HashMap<ScannerInfoTypes, List<String>>();
//		discoveredIncludes = new ArrayList();
//		discoveredSymbols = new ArrayList();
//		discoveredTSO = new ArrayList();
//		
		sumDiscoveredIncludes = new ArrayList<String>();
		sumDiscoveredSymbols = new LinkedHashMap<String, SymbolEntry>();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		this.project = project;
		this.context = new InfoContext(project);
	}

	public synchronized void contributeToScannerConfig(Object resource, @SuppressWarnings("rawtypes") Map scannerInfo, boolean isBuiltinConfig) {
		this.isBuiltinConfig= isBuiltinConfig;
		try {
			contributeToScannerConfig(resource, scannerInfo);
		}
		finally {
			this.isBuiltinConfig= false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(java.lang.Object, java.util.Map)
	 */
	public synchronized void contributeToScannerConfig(Object resource, @SuppressWarnings("rawtypes") Map scannerInfo) {
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
		else if (!((IResource) resource).getProject().equals(project)) {
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
			if (/*project.hasNature(MakeProjectNature.NATURE_ID) && */// limits to StandardMake projects
					(project.hasNature(CProjectNature.C_NATURE_ID) ||
					 project.hasNature(CCProjectNature.CC_NATURE_ID))) { 

			    for (Object name : scannerInfo.keySet()) {
			        ScannerInfoTypes siType = (ScannerInfoTypes) name;
			        @SuppressWarnings("unchecked")
                    List<String> delta = (List<String>) scannerInfo.get(siType);
                    
                    List<String> discovered = discoveredSI.get(siType);
                    if (discovered == null) {
                        discovered = new ArrayList<String>(delta);
                        discoveredSI.put(siType, discovered);
                    }
                    else {
                    	final boolean addSorted= !isBuiltinConfig && siType.equals(ScannerInfoTypes.INCLUDE_PATHS);
                    	contribute(discovered, delta, addSorted);
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
	private boolean contribute(List<String> discovered, List<String> delta, boolean ordered) {
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
	private boolean addItemsWithOrder(List<String> sumIncludes, List<String> includes, boolean ordered) {
		if (includes.isEmpty()) 
			return false;
		
		boolean addedIncludes = false;
		int insertionPoint= ordered ? 0 : sumIncludes.size(); 
		for (String item : includes) {
			int pos= sumIncludes.indexOf(item);
			if (pos >= 0) {
				if (ordered) {
					insertionPoint= pos+1;
				}
			} else {
				sumIncludes.add(insertionPoint++, item);
				addedIncludes = true;
			} 
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
        IDiscoveredPathInfo pathInfo = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project, context);
        if (pathInfo instanceof IPerProjectDiscoveredPathInfo) {
            IPerProjectDiscoveredPathInfo projectPathInfo = (IPerProjectDiscoveredPathInfo) pathInfo;
            
            monitor.beginTask(MakeMessages.getString("ScannerInfoCollector.Processing"), 100); //$NON-NLS-1$
            monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Processing")); //$NON-NLS-1$
            if (scannerConfigNeedsUpdate(projectPathInfo)) {
                monitor.worked(50);
                monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Updating") + project.getName()); //$NON-NLS-1$
                try {
                    // update scanner configuration
					List<IResource> resourceDelta = new ArrayList<IResource>(1);
					resourceDelta.add(project);
                    MakeCorePlugin.getDefault().getDiscoveryManager().updateDiscoveredInfo(context, pathInfo, context.isDefaultContext(), resourceDelta);
                    monitor.worked(50);
                } catch (CoreException e) {
                    MakeCorePlugin.log(e);
                }
            }
            monitor.done();
            scPersisted = true;
        }
	}

	/**
	 * Compare discovered include paths and symbol definitions with the ones from scanInfo.
	 */
	private boolean scannerConfigNeedsUpdate(IPerProjectDiscoveredPathInfo discPathInfo) {
		boolean addedIncludes = includePathsNeedUpdate(discPathInfo);
		boolean addedSymbols = definedSymbolsNeedUpdate(discPathInfo);
		
		return (addedIncludes | addedSymbols);
	}

	/**
	 * Compare include paths with already discovered.
	 */
	private boolean includePathsNeedUpdate(IPerProjectDiscoveredPathInfo discPathInfo) {
		boolean addedIncludes = false;
        List<String> discoveredIncludes = discoveredSI.get(ScannerInfoTypes.INCLUDE_PATHS);
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
			List<String> finalSumIncludes = CygpathTranslator.translateIncludePaths(project, sumDiscoveredIncludes);
			
			// Step 2. Get project's scanner config
			LinkedHashMap<String, Boolean> persistedIncludes = discPathInfo.getIncludeMap();
	
			// Step 3. Merge scanner config from steps 1 and 2
			// order is important, use list to preserve it
			ArrayList<String> persistedKeyList = new ArrayList<String>(persistedIncludes.keySet());
			addedIncludes = addItemsWithOrder(persistedKeyList, finalSumIncludes, true);
			
			LinkedHashMap<String, Boolean> newPersistedIncludes;
			if (addedIncludes) {
				newPersistedIncludes = new LinkedHashMap<String, Boolean>(persistedKeyList.size());
				for (String include : persistedKeyList) {
					if (persistedIncludes.containsKey(include)) {
						newPersistedIncludes.put(include, persistedIncludes.get(include));
					}
					else {
						// the paths may be on EFS resources, not local
						Boolean includePathExists = true;
						URI projectLocationURI = discPathInfo.getProject().getLocationURI();
						
						// use the project's location... create a URI that uses the same provider but that points to the include path
						URI includeURI = EFSExtensionManager.getDefault().createNewURIFromPath(projectLocationURI, include);
						
						// ask EFS if the path exists
						try {
							IFileStore fileStore = EFS.getStore(includeURI);
							IFileInfo info = fileStore.fetchInfo();
							if(!info.exists()) {
								includePathExists = false;
							}
						} catch (CoreException e) {
							MakeCorePlugin.log(e);
						}
						
						// if the include path doesn't exist, then we tell the scanner config system that the folder
						// has been "removed", and thus it won't show up in the UI
						newPersistedIncludes.put(include, !includePathExists);
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
	 */
	private boolean definedSymbolsNeedUpdate(IPerProjectDiscoveredPathInfo discPathInfo) {
		boolean addedSymbols = false;
        List<String> discoveredSymbols = discoveredSI.get(ScannerInfoTypes.SYMBOL_DEFINITIONS);
		if (discoveredSymbols != null) {
			// Step 1. Add discovered scanner config to the existing discovered scanner config 
			// add the symbols from the latest discovery
//			if (sumDiscoveredSymbols == null) {
//				sumDiscoveredSymbols = new LinkedHashMap();
//			}
			addedSymbols = ScannerConfigUtil.scAddSymbolsList2SymbolEntryMap(sumDiscoveredSymbols, discoveredSymbols, true);
			
			// Step 2. Get project's scanner config
			LinkedHashMap<String, SymbolEntry> persistedSymbols = discPathInfo.getSymbolMap();
			
			// Step 3. Merge scanner config from steps 1 and 2
			LinkedHashMap<String, SymbolEntry> candidateSymbols = new LinkedHashMap<String, SymbolEntry>(persistedSymbols);
			addedSymbols |= ScannerConfigUtil.scAddSymbolEntryMap2SymbolEntryMap(candidateSymbols, sumDiscoveredSymbols);
			
			// Step 4. Set resulting scanner config
			discPathInfo.setSymbolMap(candidateSymbols);
		}
		return addedSymbols;
	}

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#getCollectedScannerInfo(java.lang.Object, org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes)
     */
    public List<String> getCollectedScannerInfo(Object resource, ScannerInfoTypes type) {
        List<String> rv = null;
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
        else if (resource!=null && project.equals(((IResource)resource).getProject())) {
            rv = discoveredSI.get(type);
        }
        return rv;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#getDefinedSymbols()
     */
    public Map<String, String> getDefinedSymbols() {
        Map<String, String> definedSymbols = ScannerConfigUtil.scSymbolEntryMap2Map(sumDiscoveredSymbols);
        return definedSymbols;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#getIncludePaths()
     */
    public List<String> getIncludePaths() {
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

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner#deleteAll(org.eclipse.core.resources.IResource)
     */
    public void deleteAll(IResource resource) {
        deleteAllPaths(resource);
        deleteAllSymbols(resource);
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2#createPathInfoObject()
     */
    public IDiscoveredPathInfo createPathInfoObject() {
        DiscoveredPathInfo pathInfo = new DiscoveredPathInfo(project);
        try {
            DiscoveredScannerInfoStore.getInstance().loadDiscoveredScannerInfoFromState(project, context, pathInfo);
        }
        catch (CoreException e) {
            MakeCorePlugin.log(e);
        }
        return pathInfo; 
    }

	/**
	 * Static method to return compiler built-in scanner info.
	 * Preconditions: resource has to be contained by a project that has following natures:
	 *     C nature, CC nature (for C++ projects), Make nature and ScannerConfig nature  
	 */
	public static void calculateCompilerBuiltins(final IProject project) throws CModelException {
		createDiscoveredPathContainer(project, new NullProgressMonitor());
		String scdProfileId = ScannerConfigProfileManager.PER_PROJECT_PROFILE_ID;
        SCProfileInstance profileInstance = ScannerConfigProfileManager.getInstance().
        		getSCProfileInstance(project, scdProfileId);
        final IScannerConfigBuilderInfo2 buildInfo = ScannerConfigProfileManager.
        		createScannerConfigBuildInfo2(MakeCorePlugin.getDefault().getPluginPreferences(),
						scdProfileId, true);
		final IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
		if (collector instanceof IScannerInfoCollectorCleaner) {
			((IScannerInfoCollectorCleaner) collector).deleteAll(project);
		}
		final IExternalScannerInfoProvider esiProvider = profileInstance.createExternalScannerInfoProvider("specsFile");//$NON-NLS-1$
		
		// Set the arguments for the provider
		
		ISafeRunnable runnable = new ISafeRunnable() {
			public void run() throws CoreException {
				IProgressMonitor monitor = new NullProgressMonitor();
				esiProvider.invokeProvider(monitor, project, "specsFile", buildInfo, collector);//$NON-NLS-1$
				if (collector instanceof IScannerInfoCollector2) {
					IScannerInfoCollector2 collector2 = (IScannerInfoCollector2) collector;
					collector2.updateScannerConfiguration(monitor);
				}
			}
		
			public void handleException(Throwable exception) {
				if (exception instanceof OperationCanceledException) {
					throw (OperationCanceledException) exception;
				}
			}
		};
		SafeRunner.run(runnable);
	}
	
    private static void createDiscoveredPathContainer(IProject project, IProgressMonitor monitor) throws CModelException {
        IPathEntry container = CoreModel.newContainerEntry(DiscoveredPathContainer.CONTAINER_ID);
        ICProject cProject = CoreModel.getDefault().create(project);
        if (cProject != null) {
            IPathEntry[] entries = cProject.getRawPathEntries();
            List<IPathEntry> newEntries = new ArrayList<IPathEntry>(Arrays.asList(entries));
            if (!newEntries.contains(container)) {
                newEntries.add(container);
                cProject.setRawPathEntries(newEntries.toArray(new IPathEntry[newEntries.size()]), monitor);
            }
        }
        // create a new discovered scanner config store
        MakeCorePlugin.getDefault().getDiscoveryManager().removeDiscoveredInfo(project);
    }

	public void setInfoContext(InfoContext context) {
		this.context = context;
		this.project = context.getProject();
	}
	
	public InfoContext getContext(){
		return this.context;
	}
	
}
