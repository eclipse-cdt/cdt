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

package org.eclipse.cdt.managedbuilder.internal.scannerconfig;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IMacroEntry;
import org.eclipse.cdt.core.model.IPathEntry;
import org.eclipse.cdt.core.model.IPathEntryContainer;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITarget;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;

/**
 * Implements a specialized path container for managed build projects. It will 
 * either start the dynamic path collector specified for a target in the tool 
 * manifest, or it will attempt to discover the built-in values specified in 
 * the manifest.
 * 
 * @since 2.0
 */
public class ManagedBuildCPathEntryContainer implements IPathEntryContainer {
	private static final String BUILDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".ScannerConfigBuilder"; //$NON-NLS-1$
	private static final String NEWLINE = System.getProperty("line.separator");	//$NON-NLS-1$
	private static final String ERROR_HEADER = "PathEntryContainer error [";	//$NON-NLS-1$
	private static final String TRACE_FOOTER = "]: ";	//$NON-NLS-1$
	private static final String TRACE_HEADER = "PathEntryContainer trace [";	//$NON-NLS-1$
	
	private ITarget defaultTarget;
	private Vector entries;
	private IProject project;
	private ManagedBuildInfo info;
	public static boolean VERBOSE = false;
	
	public static void outputTrace(String resourceName, String message) {
		System.out.println(TRACE_HEADER + resourceName + TRACE_FOOTER + message + NEWLINE);
	}

	public static void outputError(String resourceName, String message) {
		System.err.println(ERROR_HEADER + resourceName + TRACE_FOOTER + message + NEWLINE);
	}

	/**
	 * Creates a new path container for the managed buildd project.
	 * 
	 * @param info the build information associated with the project
	 */
	public ManagedBuildCPathEntryContainer(IProject project) {
		super();
		this.project = project;
		entries = new Vector();
	}
	
	protected void addDefinedSymbols(Map definedSymbols) {
		// Add a new macro entry for each defined symbol
		Iterator keyIter = definedSymbols.keySet().iterator();
		while (keyIter.hasNext()) {
			boolean add = true;
			String macro = (String) keyIter.next();
			String value = (String) definedSymbols.get(macro);
			// Make sure the current entries do not contain a duplicate
			Iterator entryIter = entries.listIterator();
			while (entryIter.hasNext()) {
				IPathEntry entry = (IPathEntry) entryIter.next();
				if (entry.getEntryKind() == IPathEntry.CDT_MACRO) {	
					if (((IMacroEntry)entry).getMacroName().equals(macro) && 
							((IMacroEntry)entry).getMacroValue().equals(value)) {
						add = false;
						break;
					}
				}
			}
			
			if (add) {
				entries.add(CoreModel.newMacroEntry(Path.EMPTY, macro, value));
			}
		}
		
	}
	
	protected void addIncludePaths(List paths) {
		// A little checking is needed to avoid adding duplicates
		Iterator pathIter = paths.listIterator();
		while (pathIter.hasNext()) {
			String path = (String) pathIter.next();
			IPathEntry entry = CoreModel.newIncludeEntry(Path.EMPTY, Path.EMPTY, new Path(path), true);
			if (!entries.contains(entry)) {
				entries.add(entry);
			}
		}
	}

	protected void calculateBuiltIns(ITarget defaultTarget, IConfiguration config) {
		ITool[] tools = config.getFilteredTools(info.getOwner().getProject());

		// Iterate over the list
		for (int toolIndex = 0; toolIndex < tools.length; ++toolIndex) {
			ITool tool = tools[toolIndex];
			// Check its options
			IOption[] options = tool.getOptions();
			for (int optIndex = 0; optIndex < options.length; ++optIndex) {
				IOption option = options[optIndex];
				if (option.getValueType() == IOption.PREPROCESSOR_SYMBOLS) {
					String[] builtIns = option.getBuiltIns();
					Map macroMap = new HashMap();
					for (int biIndex = 0; biIndex < builtIns.length; ++biIndex) {
						String  symbol = builtIns[biIndex];
						String[] tokens = symbol.split("=");	//$NON-NLS-1$
						String macro = tokens[0].trim();
						String value = (tokens.length > 1) ? tokens[1] : new String();
						macroMap.put(macro, value);
					}
					addDefinedSymbols(macroMap);
				} else if (option.getValueType() == IOption.INCLUDE_PATH) {
					// Make sure it is a built-in, not a user-defined path
					String[] values = option.getBuiltIns();
					if (values.length > 0) {
						addIncludePaths(Arrays.asList(values));
					}
				}
			}
		}

	}

	protected void calculateEntriesDynamically(final IProject project, final IScannerInfoCollector collector) {
		final IScannerConfigBuilderInfo buildInfo;
		buildInfo = MakeCorePlugin.createScannerConfigBuildInfo(
												MakeCorePlugin.getDefault().getPluginPreferences(), 
												BUILDER_ID, 
												false);

		// TODO Get the provider from the toolchain specification
		final IExternalScannerInfoProvider esiProvider;
		esiProvider = MakeCorePlugin.getDefault().getExternalScannerInfoProvider(MakeCorePlugin.DEFAULT_EXTERNAL_SI_PROVIDER_ID);
		
		// Set the arguments for the provider
		Vector compilerArgs = new Vector();
		String args = buildInfo.getESIProviderArguments();
		IPath command = buildInfo.getESIProviderCommand();
		final Vector buildArgs = compilerArgs;
		
		ISafeRunnable runnable = new ISafeRunnable() {
			public void run() {
				IProgressMonitor monitor = new NullProgressMonitor();
				esiProvider.invokeProvider(monitor, project, buildInfo, buildArgs, collector);
			}

			public void handleException(Throwable exception) {
				if (exception instanceof OperationCanceledException) {
					throw (OperationCanceledException) exception;
				}
			}
		};
		Platform.run(runnable);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IPathEntryContainer#getPathEntries()
	 */
	public IPathEntry[] getPathEntries() {
		info = (ManagedBuildInfo) ManagedBuildManager.getBuildInfo(project);

		// Load the toolchain-spec'd collector
		defaultTarget = info.getDefaultTarget();
		if (defaultTarget == null) {
			// The build information has not been loaded yet
			return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
		}
		ITarget parent = defaultTarget.getParent();
		if (parent == null) {
			// The build information has not been loaded yet
			ManagedBuildCPathEntryContainer.outputError(project.getName(), "Build information has not been loaded yet");	//$NON-NLS-1$
			return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
		}
		// See if we can load a dynamic resolver
		String baseTargetId = parent.getId();
		IManagedScannerInfoCollector collector = ManagedBuildManager.getScannerInfoCollector(baseTargetId); 
		if (collector != null) {
			ManagedBuildCPathEntryContainer.outputTrace(project.getName(), "Path entries collected dynamically");	//$NON-NLS-1$
			collector.setProject(info.getOwner().getProject());
			calculateEntriesDynamically((IProject)info.getOwner(), collector);
			addIncludePaths(collector.getIncludePaths());
			addDefinedSymbols(collector.getDefinedSymbols());
		} else {
			// If none supplied, use the built-ins
			IConfiguration config = info.getDefaultConfiguration(defaultTarget);
			if (config != null) {
				calculateBuiltIns(defaultTarget, config);
				ManagedBuildCPathEntryContainer.outputTrace(project.getName(), "Path entries set using built-in definitions from " + config.getName());	//$NON-NLS-1$
			} else {
				ManagedBuildCPathEntryContainer.outputError(project.getName(), "Configuration is null");	//$NON-NLS-1$
				return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
			}
		}
		return (IPathEntry[])entries.toArray(new IPathEntry[entries.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IPathEntryContainer#getDescription()
	 */
	public String getDescription() {
		return "CDT Managed Build Project";	//$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.IPathEntryContainer#getPath()
	 */
	public IPath getPath() {
		return new Path("org.eclipse.cdt.managedbuilder.MANAGED_CONTAINER");	//$NON-NLS-1$
	}
	
}
