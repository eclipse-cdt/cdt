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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.core.parser.IScannerInfo;
import org.eclipse.cdt.core.parser.IScannerInfoProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.cdt.make.core.MakeScannerInfo;
import org.eclipse.cdt.make.core.MakeProjectNature;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.CygpathTranslator;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.ScannerConfigUtil;


/**
 * Singleton object that collects scanner config updates from ScannerInfoParser
 * and updates scanner config when the project's build is done.
 *
 * @author vhirsl
 */
public class ScannerInfoCollector { 

	// Singleton
	private static ScannerInfoCollector instance = new ScannerInfoCollector();
	private Map discoveredIncludes; 
	private Map discoveredSymbols;
	private Map discoveredTSO;	// target specific options
	// cumulative values
	private Map sumDiscoveredIncludes; 
	private Map sumDiscoveredSymbols;
	private Map sumDiscoveredTSO;	// target specific options
	
	private IProject currentProject;	// project being built
	
	private ScannerInfoCollector() {
		discoveredIncludes = new HashMap();
		discoveredSymbols = new HashMap();
		discoveredTSO = new HashMap();
		
		sumDiscoveredIncludes = new HashMap();
		sumDiscoveredSymbols = new HashMap();
		sumDiscoveredTSO = new HashMap();
	}
	
	public static ScannerInfoCollector getInstance() {
		return instance;
	}

	/**
	 * Published method to receive per file contributions to ScannerInfo
	 * 
	 * @param resource
	 * @param includes
	 * @param symbols
	 * @param targetSpecificOptions
	 */
	public synchronized void contributeToScannerConfig(IResource resource, List includes, List symbols, List targetSpecificOptions) {
		IProject project;
		if (resource == null || (project = resource.getProject()) == null) {
			// TODO VMIR create a log
			return;
		}
		try {
			if (project.hasNature(MakeProjectNature.NATURE_ID) && // limits to StandardMake projects
					(project.hasNature(CProjectNature.C_NATURE_ID) ||
					 project.hasNature(CCProjectNature.CC_NATURE_ID))) { 

				String projectName = project.getName();
				contribute(projectName, discoveredIncludes, includes);
				contribute(projectName, discoveredSymbols, symbols);
				contribute(projectName, discoveredTSO, targetSpecificOptions);
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
	 * @return true if there is a change in discovered symbols | includes | targetSpecificOptions
	 */
	private boolean contribute(String projectName, Map discovered, List delta) {
		if (delta == null || delta.isEmpty())
			return false;
		List projectDiscovered = (List) discovered.get(projectName);
		if (projectDiscovered == null) {
			projectDiscovered = new ArrayList(delta);
			discovered.put(projectName, projectDiscovered);
			return true;
		}
		boolean added = false;
		for (Iterator i = delta.iterator(); i.hasNext(); ) {
			String item = (String) i.next();
			if (!projectDiscovered.contains(item)) {
				added |= projectDiscovered.add(item);
			}
		}
		return added;
	}

	/**
	 * @param project
	 * @param monitor
	 */
	private void updateScannerConfig(IProject project, IProgressMonitor monitor) {
		IScannerInfoProvider provider = CCorePlugin.getDefault().getScannerInfoProvider(project);
		monitor.beginTask(MakeCorePlugin.getResourceString("ScannerInfoCollector.Processing"), 100); //$NON-NLS-1$
		if (provider != null) {
			IScannerInfo scanInfo = provider.getScannerInformation(project);
			if (scanInfo != null) {
				if (scanInfo instanceof MakeScannerInfo) {
					MakeScannerInfo makeScanInfo = (MakeScannerInfo)scanInfo;
					String projectName = project.getName();
					
					monitor.subTask(MakeCorePlugin.getResourceString("ScannerInfoCollector.Processing")); //$NON-NLS-1$
					if (scannerConfigNeedsUpdate(makeScanInfo, projectName)) {
						monitor.worked(50);
						monitor.subTask(MakeCorePlugin.getResourceString("ScannerInfoCollector.Updating") + projectName); //$NON-NLS-1$
						
						try {
							// update scanner configuration
							makeScanInfo.update();
							monitor.worked(50);
						} catch (CoreException e) {
							// TODO : VMIR create a marker?
							MakeCorePlugin.log(e);
						}
					}
				}
			}
		}
		monitor.done();
	}

	/**
	 * Compare discovered include paths and symbol definitions with the ones from scanInfo.
	 * @param scanInfo
	 * @param projectName
	 * @return
	 */
	private boolean scannerConfigNeedsUpdate(MakeScannerInfo makeScanInfo, String projectName) {
		// TODO : VMIR to implement other variants
		List includes = (List) discoveredIncludes.get(projectName);
		List dSymbols = (List) discoveredSymbols.get(projectName);
		if (includes == null && dSymbols == null)
			return false;
		Set symbols = new HashSet(dSymbols);
		
		// Step 1. Add discovered scanner config to the existing discovered scanner config 
		// add the includes from the latest discovery
		boolean addedIncludes = false;
		List sumIncludes = (List) sumDiscoveredIncludes.get(projectName);
		if (sumIncludes == null) {
			sumIncludes = new ArrayList(includes);
			sumDiscoveredIncludes.put(projectName, sumIncludes);
			addedIncludes = true;
		}
		else {
			for (Iterator i = includes.iterator(); i.hasNext(); ) {
				String include = (String) i.next();
				if (!sumIncludes.contains(include)) {
					addedIncludes |= sumIncludes.add(include);
				}
			}
		}
		// try to translate cygpaths to absolute paths
		List finalSumIncludes = translateIncludePaths(sumIncludes);
		
		// add the symbols from the latest discovery
		boolean addedSymbols = false;
		Map sumSymbols = (Map) sumDiscoveredSymbols.get(projectName);
		if (sumSymbols == null) {
			sumSymbols = new HashMap();
			sumDiscoveredSymbols.put(projectName, sumSymbols);
		}
		addedSymbols = ScannerConfigUtil.scAddSymbolsSet2SymbolEntryMap(sumSymbols, symbols, false);
		
		// Step 2. Get project's scanner config
		String[] persistedIncludes = makeScanInfo.getIncludePaths();
		Map persistedSymbols = makeScanInfo.getDefinedSymbols();
		
		// TODO VMIR this is likely to change when new UI is introduced
		// Step 3. Merge scanner config from steps 1 and 2
		List candidateIncludes = new ArrayList(Arrays.asList(persistedIncludes));
		for (Iterator i = finalSumIncludes.iterator(); i.hasNext(); ) {
			String include = (String) i.next();
			if (!candidateIncludes.contains(include)) {
				addedIncludes |= candidateIncludes.add(include);
			}
		}
		Map candidateSymbols = new HashMap(sumSymbols);
		Set persistedSymbolsSet = ScannerConfigUtil.scSymbolsMap2Set(persistedSymbols);
		addedSymbols |= ScannerConfigUtil.scAddSymbolsSet2SymbolEntryMap(candidateSymbols, persistedSymbolsSet, true);
		
		// Step 4. Set resulting scanner config
		makeScanInfo.setIncludePaths((String[])candidateIncludes.toArray(new String[candidateIncludes.size()]));
		makeScanInfo.setPreprocessorSymbols((String[])ScannerConfigUtil.
			scSymbolsSymbolEntryMap2Set(candidateSymbols).toArray(new String[candidateSymbols.size()]));
		
		// invalidate discovered include paths and symbol definitions
		discoveredIncludes.put(projectName, null);
		discoveredSymbols.put(projectName, null);
		
		return (addedIncludes | addedSymbols);
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
				String translatedPath = new CygpathTranslator(currentProject, includePath).run();
				if (!translatedPath.equals(includePath)) {
					// Check if the translated path exists
					IPath transPath = new Path(translatedPath);
					if (transPath.toFile().exists()) {
						translatedIncludePaths.add(translatedPath);
					}
					else {
						// TODO VMIR create problem marker
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
			MakeCorePlugin.log(e);
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
						String[] tsoArray;
						if (tso == null) {
							tsoArray = new String[0];
						}
						else {
							tsoArray = (String[])tso.toArray(new String[tso.size()]);
						}
						esiProvider.invokeProvider(monitor, project, buildInfo, tsoArray);
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
	public synchronized void updateScannerConfiguration(IProject project, IProgressMonitor monitor) {
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

}
