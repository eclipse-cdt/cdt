/**********************************************************************
 * Copyright (c) 2002,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.make.core;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.core.makefile.IMakefile;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.core.scannerconfig.ScannerConfigBuilder;
import org.eclipse.cdt.make.internal.core.BuildInfoFactory;
import org.eclipse.cdt.make.internal.core.MakeTargetManager;
import org.eclipse.cdt.make.internal.core.makefile.gnu.GNUMakefile;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigInfoFactory;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;

/**
 * The main plugin class to be used in the desktop.
 */
public class MakeCorePlugin extends Plugin {
	public static final String MAKE_PROJECT_ID = MakeCorePlugin.getUniqueIdentifier() + ".make"; //$NON-NLS-1$
	private MakeTargetManager fTargetManager;
	public static final String OLD_BUILDER_ID = "org.eclipse.cdt.core.cbuilder"; //$NON-NLS-1$

	public static final String EXTERNAL_SI_PROVIDER_SIMPLE_ID = "ExternalScannerInfoProvider"; //$NON-NLS-1$
	public static final String SI_CONSOLE_PARSER_SIMPLE_ID = "ScannerInfoConsoleParser";	//$NON-NLS-1$
	public static final String DEFAULT_EXTERNAL_SI_PROVIDER_ID = MakeCorePlugin.getUniqueIdentifier() + ".DefaultExternalScannerInfoProvider"; //$NON-NLS-1$
	public static final String GCC_SPECS_CONSOLE_PARSER_ID = MakeCorePlugin.getUniqueIdentifier() + ".GCCSpecsConsoleParser"; //$NON-NLS-1$
	public static final String GCC_SCANNER_INFO_CONSOLE_PARSER_ID = MakeCorePlugin.getUniqueIdentifier() + ".GCCScannerInfoConsoleParser"; //$NON-NLS-1$
	
	//The shared instance.
	private static MakeCorePlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	/**
	 * The constructor.
	 */
	public MakeCorePlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.make.core.PluginResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
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

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = MakeCorePlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	public static String getFormattedString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[] { arg });
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return "org.eclipse.cdt.make.core"; //$NON-NLS-1$
		}
		return getDefault().getDescriptor().getUniqueIdentifier();
	}

	protected void initializeDefaultPluginPreferences() {
		IMakeBuilderInfo info = createBuildInfo(getPluginPreferences(), MakeBuilder.BUILDER_ID, true);
		try {
			info.setBuildCommand(new Path("make")); //$NON-NLS-1$
			info.setBuildLocation(new Path("")); //$NON-NLS-1$
			info.setStopOnError(false);
			info.setUseDefaultBuildCmd(true);
			info.setAutoBuildEnable(false);
			info.setAutoBuildTarget("all"); //$NON-NLS-1$
			info.setIncrementalBuildEnable(true);
			info.setIncrementalBuildTarget("all"); //$NON-NLS-1$
			info.setFullBuildEnable(true);
			info.setFullBuildTarget("clean all"); //$NON-NLS-1$
			info.setCleanBuildEnable(true);
			info.setCleanBuildTarget("clean"); //$NON-NLS-1$
			info.setErrorParsers(CCorePlugin.getDefault().getAllErrorParsersIDs());
		} catch (CoreException e) {
		}
		getPluginPreferences().setDefault(CCorePlugin.PREF_BINARY_PARSER, CCorePlugin.PLUGIN_ID + ".ELF"); //$NON-NLS-1$

		// default plugin preferences for scanner configuration discovery
		IScannerConfigBuilderInfo scInfo = createScannerConfigBuildInfo(getPluginPreferences(), ScannerConfigBuilder.BUILDER_ID, true);
		try {
			scInfo.setAutoDiscoveryEnabled(false);
			scInfo.setMakeBuilderConsoleParserEnabled(true);
			scInfo.setESIProviderCommandEnabled(true);
			scInfo.setUseDefaultESIProviderCmd(true);
			scInfo.setESIProviderCommand(new Path("gcc")); //$NON-NLS-1$
			scInfo.setESIProviderArguments("-c -v");	//$NON-NLS-1$
			scInfo.setESIProviderConsoleParserId(GCC_SPECS_CONSOLE_PARSER_ID);
			scInfo.setMakeBuilderConsoleParserId(GCC_SCANNER_INFO_CONSOLE_PARSER_ID);
		} catch (CoreException e) {
		}
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

	public IMakefile createMakefile(IFile file) {
		GNUMakefile gnu = new GNUMakefile();
		try {
			gnu.parse(file.getLocation().toOSString());
			String[] dirs = gnu.getIncludeDirectories();
			String[] includes = new String[dirs.length + 1];
			System.arraycopy(dirs, 0, includes, 0, dirs.length);
			String cwd = file.getLocation().removeLastSegments(1).toOSString();
			includes[dirs.length] = cwd;
			gnu.setIncludeDirectories(includes);
		} catch (IOException e) {
		}
		return gnu;
		//
		// base on a preference to chose GNU vs Posix 
		//return PosixMakefile(file.getLocation);
	}

	public void shutdown() throws CoreException {
		if ( fTargetManager != null) {
			fTargetManager.shutdown();
			fTargetManager = null;
		}
		super.shutdown();
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

	/**
	 * @param id - id specifying external scanner info provider
	 * @return provider - new instance of an external scanner info provider
	 */
	public IExternalScannerInfoProvider getExternalScannerInfoProvider(String id) {
		try {
			IExtensionPoint extension = getDescriptor().getExtensionPoint(EXTERNAL_SI_PROVIDER_SIMPLE_ID);
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
		IExtensionPoint extension = getDescriptor().getExtensionPoint(SI_CONSOLE_PARSER_SIMPLE_ID);
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
			IExtensionPoint extension = getDescriptor().getExtensionPoint(SI_CONSOLE_PARSER_SIMPLE_ID);
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
	public void startup() throws CoreException {
		super.startup();
		
		//Set debug tracing options
		configurePluginDebugOptions();
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
