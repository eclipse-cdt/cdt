/**********************************************************************
 * Copyright (c) 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * IBM - Initial API and implementation
 **********************************************************************/
package org.eclipse.cdt.make.internal.core.scannerconfig;

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
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigUtil;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager.IDiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CygpathTranslator;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;


/**
 * Singleton object that collects scanner config updates from ScannerInfoParser
 * and updates scanner config when the project's build is done.
 *
 * @author vhirsl
 */
public class ScannerInfoCollector implements IScannerInfoCollector { 

	// Singleton
	private static ScannerInfoCollector instance = new ScannerInfoCollector();
	private Map discoveredIncludes; 
	private Map discoveredSymbols;
	private Map discoveredTSO;	// target specific options
	// cumulative values
	private Map sumDiscoveredIncludes; 
	private Map sumDiscoveredSymbols;
//	private Map sumDiscoveredTSO;	// target specific options
	
	private IProject currentProject;	// project being built
	
	private ScannerInfoCollector() {
		discoveredIncludes = new HashMap();
		discoveredSymbols = new HashMap();
		discoveredTSO = new HashMap();
		
		sumDiscoveredIncludes = new HashMap();
		sumDiscoveredSymbols = new HashMap();
//		sumDiscoveredTSO = new HashMap();
	}
	
	public static ScannerInfoCollector getInstance() {
		return instance;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector#contributeToScannerConfig(org.eclipse.core.resources.IResource, java.util.List, java.util.List, java.util.Map)
	 */
	public void contributeToScannerConfig(IResource resource, List includes, List symbols, Map extraInfo) {
		IProject project;
		if (resource == null || (project = resource.getProject()) == null) {
			TraceUtil.outputError("IScannerInfoCollector.contributeToScannerConfig : ", "resource or project is null"); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		try {
			if (project.hasNature(MakeProjectNature.NATURE_ID) && // limits to StandardMake projects
					(project.hasNature(CProjectNature.C_NATURE_ID) ||
					 project.hasNature(CCProjectNature.CC_NATURE_ID))) { 

				String projectName = project.getName();
				contribute(projectName, discoveredIncludes, includes, true);
				contribute(projectName, discoveredSymbols, symbols, false);
				contribute(projectName, 
						   discoveredTSO, 
						   (extraInfo == null) ? null : (List) extraInfo.get(IScannerInfoCollector.TARGET_SPECIFIC_OPTION),
						   false);
			}
		} 
		catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param project
	 * @param discovered symbols | includes | targetSpecificOptions
	 * @param delta symbols | includes | targetSpecificOptions
	 * @param ordered - to preserve order or append at the end
	 * @return true if there is a change in discovered symbols | includes | targetSpecificOptions
	 */
	private boolean contribute(String projectName, Map discovered, List delta, boolean ordered) {
		if (delta == null || delta.isEmpty())
			return false;
		List projectDiscovered = (List) discovered.get(projectName);
		if (projectDiscovered == null) {
			projectDiscovered = new ArrayList(delta);
			discovered.put(projectName, projectDiscovered);
			return true;
		}
		return addItemsWithOrder(delta, projectDiscovered, ordered);
	}

	/**
	 * Adds new items to the already accumulated ones preserving order
	 *  
	 * @param includes - items to be added
	 * @param sumIncludes - previously accumulated items
	 * @param ordered - to preserve order or append at the end
	 * @return boolean - true if added
	 */
	private boolean addItemsWithOrder(List includes, List sumIncludes, boolean ordered) {
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

	/**
	 * @param project
	 * @param monitor
	 */
	private void updateScannerConfig(IProject project, IProgressMonitor monitor) throws CoreException {
		IDiscoveredPathInfo pathInfo = MakeCorePlugin.getDefault().getDiscoveryManager().getDiscoveredInfo(project);
		monitor.beginTask(MakeMessages.getString("ScannerInfoCollector.Processing"), 100); //$NON-NLS-1$
		if (pathInfo != null) {
			String projectName = project.getName();
			monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Processing")); //$NON-NLS-1$
			if (scannerConfigNeedsUpdate(pathInfo)) {
				monitor.worked(50);
				monitor.subTask(MakeMessages.getString("ScannerInfoCollector.Updating") + projectName); //$NON-NLS-1$
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
	}

	/**
	 * Compare discovered include paths and symbol definitions with the ones from scanInfo.
	 * 
	 * @param scanInfo
	 * @param projectName
	 * @return
	 */
	private boolean scannerConfigNeedsUpdate(IDiscoveredPathInfo discPathInfo) {
		List includes = (List) discoveredIncludes.get(discPathInfo.getProject().getName());
		List symbols = (List) discoveredSymbols.get(discPathInfo.getProject().getName());
		
		boolean addedIncludes = includePathsNeedUpdate(discPathInfo, includes);
		boolean addedSymbols = definedSymbolsNeedUpdate(discPathInfo, symbols);
		
		return (addedIncludes | addedSymbols);
	}

	/**
	 * Compare include paths with already discovered.
	 * 
	 * @param discPathInfo
	 * @param projectName
	 * @param includes
	 * @return
	 */
	private boolean includePathsNeedUpdate(IDiscoveredPathInfo discPathInfo, List includes) {
		boolean addedIncludes = false;
		String projectName = discPathInfo.getProject().getName();
		if (includes != null) {
			// Step 1. Add discovered scanner config to the existing discovered scanner config 
			// add the includes from the latest discovery
			List sumIncludes = (List) sumDiscoveredIncludes.get(projectName);
			if (sumIncludes == null) {
				sumIncludes = new ArrayList(includes);
				sumDiscoveredIncludes.put(projectName, sumIncludes);
				addedIncludes = true;
			}
			else {
				addedIncludes = addItemsWithOrder(includes, sumIncludes, true);
			}
			// try to translate cygpaths to absolute paths
			List finalSumIncludes = translateIncludePaths(sumIncludes);
			
			// Step 2. Get project's scanner config
			LinkedHashMap persistedIncludes = discPathInfo.getIncludeMap();
	
			// Step 3. Merge scanner config from steps 1 and 2
			// order is important, use list to preserve it
			ArrayList persistedKeyList = new ArrayList(persistedIncludes.keySet());
			addedIncludes = addItemsWithOrder(finalSumIncludes, persistedKeyList, true);
			
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
			
			// Step 5. Invalidate discovered include paths
			discoveredIncludes.put(projectName, null);
		}
		return addedIncludes;
	}

	/**
	 * Compare symbol definitions with already discovered.
	 * 
	 * @param discPathInfo
	 * @param projectName
	 * @param symbols
	 * @return
	 */
	private boolean definedSymbolsNeedUpdate(IDiscoveredPathInfo discPathInfo, List symbols) {
		boolean addedSymbols = false;
		String projectName = discPathInfo.getProject().getName();
		if (symbols != null) {
			// Step 1. Add discovered scanner config to the existing discovered scanner config 
			// add the symbols from the latest discovery
			Map sumSymbols = (Map) sumDiscoveredSymbols.get(projectName);
			if (sumSymbols == null) {
				sumSymbols = new LinkedHashMap();
				sumDiscoveredSymbols.put(projectName, sumSymbols);
			}
			addedSymbols = ScannerConfigUtil.scAddSymbolsList2SymbolEntryMap(sumSymbols, symbols, false);
			
			// Step 2. Get project's scanner config
			LinkedHashMap persistedSymbols = discPathInfo.getSymbolMap();
			
			// Step 3. Merge scanner config from steps 1 and 2
			LinkedHashMap candidateSymbols = new LinkedHashMap(persistedSymbols);
			addedSymbols |= ScannerConfigUtil.scAddSymbolEntryMap2SymbolEntryMap(candidateSymbols, sumSymbols);
			
			// Step 4. Set resulting scanner config
			discPathInfo.setSymbolMap(candidateSymbols);
			
			// Step 5. Invalidate discovered symbol definitions
			discoveredSymbols.put(projectName, null);
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
					translatedPath = new CygpathTranslator(currentProject, includePath).run();
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

	/**
	 * Call ESI provider to get scanner info
	 *
	 * @param project
	 * @param tso
	 * @param monitor
	 */
	private void getProviderScannerInfo(final IProject project, 
										final List tso, 
										final IProgressMonitor monitor) {
		// get IScannerConfigBuilderInfo
		IScannerConfigBuilderInfo info;
		try {
			info = MakeCorePlugin.createScannerConfigBuildInfo(
					project, ScannerConfigBuilder.BUILDER_ID);
		}
		catch (CoreException e) {
			info = MakeCorePlugin.createScannerConfigBuildInfo(
					MakeCorePlugin.getDefault().getPluginPreferences(), 
					ScannerConfigBuilder.BUILDER_ID, false);
		}
		if (info.isESIProviderCommandEnabled()) {
			final IScannerConfigBuilderInfo buildInfo = info;
			final IExternalScannerInfoProvider esiProvider = MakeCorePlugin.getDefault().
				getExternalScannerInfoProvider(MakeCorePlugin.DEFAULT_EXTERNAL_SI_PROVIDER_ID);
			if (esiProvider != null) {
				ISafeRunnable runnable = new ISafeRunnable() {
					public void run() {
						esiProvider.invokeProvider(monitor, project, buildInfo, tso, ScannerInfoCollector.getInstance());
					}
		
					public void handleException(Throwable exception) {
						MakeCorePlugin.log(exception);
					}
				};
				Platform.run(runnable);
			}
		}
	}

	/**
	 * @param project
	 * @param monitor
	 */
	public synchronized void updateScannerConfiguration(IProject project, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		currentProject = project;
		String projectName = project.getName();
		// check TSO for the project
		monitor.beginTask("", 100); //$NON-NLS-1$
		getProviderScannerInfo(project, (List) discoveredTSO.get(projectName), new SubProgressMonitor(monitor, 60));
		updateScannerConfig(project, new SubProgressMonitor(monitor, 40));

		// delete discovered scanner config
		discoveredIncludes.put(projectName, null);
		discoveredSymbols.put(projectName, null);
		discoveredTSO.put(projectName, null);
	}

	/**
	 * Delete all discovered paths for the project
	 * 
	 * @param project
	 */
	public void deleteAllPaths(IProject project) {
		if (project != null) {
			sumDiscoveredIncludes.put(project.getName(), null);
		}
	}

	/**
	 * Delete all discovered symbols for the project
	 * 
	 * @param project
	 */
	public void deleteAllSymbols(IProject project) {
		if (project != null) {
			sumDiscoveredSymbols.put(project.getName(), null);
		}
	}

	/**
	 * Delete a specific include path
	 * 
	 * @param project
	 * @param path
	 */
	public void deletePath(IProject project, String path) {
		if (project != null) {
			List sumIncludes = (List) sumDiscoveredIncludes.get(project.getName());
			if (sumIncludes != null) {
				sumIncludes.remove(path);
			}
		}
	}

	/**
	 * Delete a specific symbol definition
	 * 
	 * @param project
	 * @param path
	 */
	public void deleteSymbol(IProject project, String symbol) {
		if (project != null) {
			Map sumSymbols = (Map) sumDiscoveredSymbols.get(project.getName());
			if (sumSymbols != null) {
				// remove it from the Map of SymbolEntries 
				ScannerConfigUtil.removeSymbolEntryValue(symbol, sumSymbols);
			}
		}
	}
}
