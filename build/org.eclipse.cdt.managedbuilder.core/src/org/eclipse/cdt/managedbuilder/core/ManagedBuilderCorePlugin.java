package org.eclipse.cdt.managedbuilder.core;

/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
 * **********************************************************************/

import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.managedbuilder.internal.core.GeneratedMakefileBuilder;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.ManagedBuildCPathEntryContainer;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.ManagedBuildPathEntryContainerInitializer;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;


public class ManagedBuilderCorePlugin extends Plugin {
	private static final String PLUGIN_ID = "org.eclipse.cdt.managedbuilder.core"; //$NON-NLS-1$
	// The attribute name for the dependency calculator
	public static final String DEP_CALC_ID ="dependencyCalculator"; //$NON-NLS-1$
	//The shared instance.
	private static ManagedBuilderCorePlugin plugin;
	// The attribute name for the scanner info collector
	public static final String SCANNER_INFO_ID = "scannerInfoCollector"; //$NON-NLS-1$
	// The attribute name for the makefile generator
	public static final String MAKEGEN_ID ="makefileGenerator"; //$NON-NLS-1$
	
	/**
	 * @param descriptor
	 */
	public ManagedBuilderCorePlugin() {
		super();
		plugin = this;
	}

	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static ManagedBuilderCorePlugin getDefault() {
		return plugin;
	}

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.OK, e.getMessage(), e);
		log(status);
	}

	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		// Turn on logging for plugin when debugging
		super.start(context);
		configurePluginDebugOptions();
	}
	
	private static final String PATH_ENTRY = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/pathEntry"; //$NON-NLS-1$
	private static final String PATH_ENTRY_INIT = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/pathEntryInit"; //$NON-NLS-1$
	private static final String BUILDER = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/builder"; //$NON-NLS-1$
	
	/**
	 * 
	 */
	private void configurePluginDebugOptions() {
		if (isDebugging()) {
			String pathInit = Platform.getDebugOption(PATH_ENTRY_INIT);
			if (pathInit != null) {
				ManagedBuildPathEntryContainerInitializer.VERBOSE = pathInit.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			String pathCalc = Platform.getDebugOption(PATH_ENTRY);
			if (pathCalc != null) {
				ManagedBuildCPathEntryContainer.VERBOSE = pathCalc.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
			String builder = Platform.getDebugOption(BUILDER);
			if (builder != null) {
				GeneratedMakefileBuilder.VERBOSE = builder.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
		}
	}
}
