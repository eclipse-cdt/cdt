/*******************************************************************************
 * Copyright (c) 2002, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.make.core;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.BuildInfoFactory;
import org.eclipse.cdt.make.internal.core.MakeTargetManager;
import org.eclipse.cdt.make.internal.core.makefile.gnu.GNUMakefile;
import org.eclipse.cdt.make.internal.core.makefile.posix.PosixMakefile;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathManager;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigInfoFactory;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class MakeCorePlugin extends Plugin {
	public static final String PLUGIN_ID = "org.eclipse.cdt.make.core"; //$NON-NLS-1$
	public static final String MAKE_PROJECT_ID = MakeCorePlugin.getUniqueIdentifier() + ".make"; //$NON-NLS-1$
	public static final String OLD_BUILDER_ID = "org.eclipse.cdt.core.cbuilder"; //$NON-NLS-1$

	public static final String EXTERNAL_SI_PROVIDER_SIMPLE_ID = "ExternalScannerInfoProvider"; //$NON-NLS-1$
	public static final String SI_CONSOLE_PARSER_SIMPLE_ID = "ScannerInfoConsoleParser";	//$NON-NLS-1$
	public static final String DEFAULT_EXTERNAL_SI_PROVIDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".DefaultExternalScannerInfoProvider"; //$NON-NLS-1$

	public static final String GCC_SPECS_CONSOLE_PARSER_ID = MakeCorePlugin.getUniqueIdentifier() + ".GCCSpecsConsoleParser"; //$NON-NLS-1$
	public static final String GCC_SCANNER_INFO_CONSOLE_PARSER_ID = MakeCorePlugin.getUniqueIdentifier() + ".GCCScannerInfoConsoleParser"; //$NON-NLS-1$

	public static final String MAKEFILE_STYLE = PLUGIN_ID + "editor_makefile_style"; //$NON-NLS-1$
	public static final String MAKEFILE_DIRS = PLUGIN_ID + "editor_makefile_dirs"; //$NON-NLS-1$


	private MakeTargetManager fTargetManager;
	private DiscoveredPathManager fDiscoveryPathManager;
	//The shared instance.
	private static MakeCorePlugin plugin;

	/**
	 * The constructor.
	 */
	public MakeCorePlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static MakeCorePlugin getDefault() {
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

	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	public static IMakeBuilderInfo createBuildInfo(Preferences prefs, String builderID, boolean useDefaults) {
		return BuildInfoFactory.create(prefs, builderID, useDefaults);
	}

	public static IMakeBuilderInfo createBuildInfo(IProject project, String builderID) throws CoreException {
		return BuildInfoFactory.create(project, builderID);
	}

	public static IMakeBuilderInfo createBuildInfo(Map args, String builderID) {
		return BuildInfoFactory.create(args, builderID);
	}

	public IMakeTargetManager getTargetManager() {
		if ( fTargetManager == null) {
			fTargetManager = new MakeTargetManager();
			fTargetManager.startup();
		}
		return fTargetManager;
	}

	public boolean isMakefileGNUStyle() {
		String style = getPluginPreferences().getString(MAKEFILE_STYLE);
		return (style != null && style.equalsIgnoreCase("GNU")); //$NON-NLS-1$
	}

	public String[] getMakefileDirs() {
		String stringList = getPluginPreferences().getString(MAKEFILE_DIRS);
		StringTokenizer st = new StringTokenizer(stringList, File.pathSeparator + "\n\r");//$NON-NLS-1$
		ArrayList v = new ArrayList();
		while (st.hasMoreElements()) {
			v.add(st.nextElement());
		}
		return (String[])v.toArray(new String[v.size()]);		
	}

	public IMakefile createMakefile(IFile file) {
		//
		// base on a preference to chose GNU vs Posix 
		IMakefile makefile;
		if (isMakefileGNUStyle()) {
			GNUMakefile gnu = new GNUMakefile();
			ArrayList includeList = new ArrayList();
			includeList.addAll(Arrays.asList(gnu.getIncludeDirectories()));
			includeList.addAll(Arrays.asList(getMakefileDirs()));
			includeList.add(file.getLocation().removeLastSegments(1).toOSString());
			String[] includes = (String[]) includeList.toArray(new String[includeList.size()]);
			gnu.setIncludeDirectories(includes);
			try {
				gnu.parse(file.getLocation().toOSString());
			} catch (IOException e) {
			}
			makefile = gnu;
		} else {
			PosixMakefile posix = new PosixMakefile();
			try {
				posix.parse(file.getLocation().toOSString());
			} catch (IOException e) {
			}
			makefile = posix;
		}
		return makefile;
	}

	public void stop(BundleContext context) throws Exception {
		try {
			if ( fTargetManager != null) {
				fTargetManager.shutdown();
				fTargetManager = null;
			}
			if (fDiscoveryPathManager != null) {
				fDiscoveryPathManager.shutdown();
				fDiscoveryPathManager = null;
			}
			savePluginPreferences();
		} finally {
			super.stop(context);
		}
	}

	/*
	 * Following methods create IScannerConfigBuilderInfo
	 * Delegating requests to ScannerConfigInfoFactory
	 */
	public static IScannerConfigBuilderInfo createScannerConfigBuildInfo(
			Preferences prefs, String builderID, boolean useDefaults) {
		return ScannerConfigInfoFactory.create(prefs, builderID, useDefaults);
	}

	public static IScannerConfigBuilderInfo createScannerConfigBuildInfo(
			IProject project, String builderID) throws CoreException {
		return ScannerConfigInfoFactory.create(project, builderID);
	}

	public static IScannerConfigBuilderInfo createScannerConfigBuildInfo(
			Map args, String builderID) {
		return ScannerConfigInfoFactory.create(args, builderID);
	}
	
	public static IPath getWorkingDirectory() {
		return MakeCorePlugin.getDefault().getStateLocation();
	}

	public IDiscoveredPathManager getDiscoveryManager() {
		if ( fDiscoveryPathManager == null) {
			fDiscoveryPathManager = new DiscoveredPathManager();
			fDiscoveryPathManager.startup();
		}
		return fDiscoveryPathManager;
	}

	/**
	 * @param id - id specifying external scanner info provider
	 * @return provider - new instance of an external scanner info provider
	 */
	public IExternalScannerInfoProvider getExternalScannerInfoProvider(String id) {
		try {
	        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, EXTERNAL_SI_PROVIDER_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					String tool = extensions[i].getUniqueIdentifier();
					if (tool != null && tool.equals(id)) {
						IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
						for (int j = 0; j < configElements.length; j++) {
							IConfigurationElement[] runElement = configElements[j].getChildren("run"); //$NON-NLS-1$
							if (runElement.length > 0) { 
								IExternalScannerInfoProvider builder = (IExternalScannerInfoProvider) runElement[0].createExecutableExtension("class"); //$NON-NLS-1$
								return builder;
							}
						}
					}
				}
			}
		} 
		catch (CoreException e) {
			log(e);
		}
		return null;
	}

	/**
	 * @param commandId
	 * @return String[] - array of parserIds associated with the commandId or 'all'
	 */
	public String[] getScannerInfoConsoleParserIds(String commandId) {
		String[] empty = new String[0];
		if (commandId == null || commandId.length() == 0) {
			commandId = "all";	//$NON-NLS-1$
		}
        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, SI_CONSOLE_PARSER_SIMPLE_ID);
		if (extension != null) {
			IExtension[] extensions = extension.getExtensions();
			List parserIds = new ArrayList(extensions.length);
			for (int i = 0; i < extensions.length; i++) {
				String parserId = extensions[i].getUniqueIdentifier();
				if (parserId != null) {
					IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
					String id = configElements[0].getAttribute("commandId");//$NON-NLS-1$
					if (id != null && (id.equals(commandId) || id.equals("all"))) {	//$NON-NLS-1$
						parserIds.add(parserId);
					}
				}							
			}
			return (String[])parserIds.toArray(empty);
		}
		return empty;
	}
	
	/**
	 * @param parserId
	 * @return parser - parser object identified by the parserId
	 */
	public IScannerInfoConsoleParser getScannerInfoConsoleParser(String parserId) {
		try {
	        IExtensionPoint extension = Platform.getExtensionRegistry().getExtensionPoint(PLUGIN_ID, SI_CONSOLE_PARSER_SIMPLE_ID);
			if (extension != null) {
				IExtension[] extensions = extension.getExtensions();
				for (int i = 0; i < extensions.length; i++) {
					String id = extensions[i].getUniqueIdentifier();
					if (id != null && id.equals(parserId)) {
						IConfigurationElement[] configElements = extensions[i].getConfigurationElements();
						IScannerInfoConsoleParser parser = (IScannerInfoConsoleParser)configElements[0].createExecutableExtension("class");//$NON-NLS-1$
						return parser;
					}
				}
			}
		}
		catch (CoreException e) {
			log(e);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		
		//Set debug tracing options
		configurePluginDebugOptions();
        // Scanner config discovery setup
        GCCScannerConfigUtil.createSpecs();
	}

	private static final String SCANNER_CONFIG = MakeCorePlugin.getUniqueIdentifier() + "/debug/scdiscovery"; //$NON-NLS-1$
	/**
	 * 
	 */
	private void configurePluginDebugOptions() {
		if (isDebugging()) {
			String option = Platform.getDebugOption(SCANNER_CONFIG);
			if (option != null) {
				TraceUtil.SCANNER_CONFIG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}
		}
	}
}
