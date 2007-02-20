/*******************************************************************************
 * Copyright (c) 2003, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.managedbuilder.core;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IDiscoveredPathManager;
import org.eclipse.cdt.make.core.scannerconfig.IExternalScannerInfoProvider;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathManager;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerConfigInfoFactory;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig.util.TraceUtil;
import org.eclipse.cdt.managedbuilder.internal.buildmodel.DbgUtil;
import org.eclipse.cdt.managedbuilder.internal.core.BuilderFactory;
import org.eclipse.cdt.managedbuilder.internal.core.GeneratedMakefileBuilder;
import org.eclipse.cdt.managedbuilder.internal.core.ManagedMakeMessages;
import org.eclipse.cdt.managedbuilder.internal.core.ResourceChangeHandler;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.ManagedBuildCPathEntryContainer;
import org.eclipse.cdt.managedbuilder.internal.scannerconfig.ManagedBuildPathEntryContainerInitializer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.ISavedState;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.BundleContext;


public class ManagedBuilderCorePlugin extends Plugin {
	private static final String PLUGIN_ID = "org.eclipse.cdt.managedbuilder.core"; //$NON-NLS-1$
	// The shared instance
	private static ManagedBuilderCorePlugin plugin;
	// The attribute name for the makefile generator
	public static final String MAKEGEN_ID ="makefileGenerator"; //$NON-NLS-1$
	public static final String COMMANDLINEGEN_ID = "commandlineGenerator"; //$NON-NLS-1$
	// The unique id for all managed make projects 
	public static final String MANAGED_MAKE_PROJECT_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".managedMake"; //$NON-NLS-1$
	//  NOTE: The code below is for tracking resource renaming and deleting.  This is needed to keep
	//  ResourceConfiguration elements up to date.  It may also be needed by AdditionalInput
	//  elements
	
	private static final String SCANNER_CONFIG = getUniqueIdentifier() + "/debug/scdiscovery"; //$NON-NLS-1$
	public static final String EXTERNAL_SI_PROVIDER_SIMPLE_ID = "ExternalScannerInfoProvider"; //$NON-NLS-1$
	public static final String SI_CONSOLE_PARSER_SIMPLE_ID = "ScannerInfoConsoleParser";	//$NON-NLS-1$
	public static final String DEFAULT_EXTERNAL_SI_PROVIDER_ID = getUniqueIdentifier() + ".DefaultExternalScannerInfoProvider"; //$NON-NLS-1$

	public static final String GCC_SPECS_CONSOLE_PARSER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".GCCSpecsConsoleParser"; //$NON-NLS-1$
	public static final String GCC_SCANNER_INFO_CONSOLE_PARSER_ID = ManagedBuilderCorePlugin.getUniqueIdentifier() + ".GCCScannerInfoConsoleParser"; //$NON-NLS-1$



	private static ResourceChangeHandler listener;

	private DiscoveredPathManager fDiscoveryPathManager;

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

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		// Turn on logging for plugin when debugging
		super.start(context);
		configurePluginDebugOptions();
		
        GCCScannerConfigUtil.createSpecs();

		
		//	  NOTE: The code below is for tracking resource renaming and deleting.  This is needed to keep
		//      ResourceConfiguration elements up to date.  It may also be needed by AdditionalInput
		//      elements
		
//		IJobManager jobManager = Platform.getJobManager();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
/*		try {
			jobManager.beginRule(root, null);

			startResourceChangeHandling();
		} catch (Throwable e) {
			//either an IllegalArgumentException is thrown by the jobManager.beginRule
			//or core exception is thrown by the startResourceChangeHandling()
			//in any case, schedule a job with the root rule
			//that will perform the resource change handling initialization
*/			
		//The startResourceChangeHandling() might result in throwing an error
		//see bug# 132001
		//Always schedule a job
			Job rcJob = new Job(ManagedMakeMessages.getResourceString("ManagedBuilderCorePlugin.resourceChangeHandlingInitializationJob")){ 	//$NON-NLS-1$
				protected IStatus run(IProgressMonitor monitor) {
					try{
						startResourceChangeHandling();
					} catch (CoreException e){
						CCorePlugin.log(e);
						return e.getStatus();
					}
					return new Status(
							IStatus.OK,
							ManagedBuilderCorePlugin.getUniqueIdentifier(),
							IStatus.OK,
							new String(),
							null);
				}
			};
			
			rcJob.setRule(root);
			rcJob.setPriority(Job.INTERACTIVE);
			rcJob.setSystem(true);
			rcJob.schedule();
/*
		} finally {
			jobManager.endRule(root);
		}
*/
	}
	
	/*
	 * This method adds a save participant and resource change listener
	 * Throws CoreException if the methods fails to add a save participant.
	 * The resource change listener in not added in this case either.
	 */
	private void startResourceChangeHandling() throws CoreException{
		// Set up a listener for resource change events
		listener = new ResourceChangeHandler();
		ISavedState lastState =
			ResourcesPlugin.getWorkspace().addSaveParticipant(ManagedBuilderCorePlugin.this, listener);

		ResourcesPlugin.getWorkspace().addResourceChangeListener(
				listener, 
				IResourceChangeEvent.POST_CHANGE 
				| IResourceChangeEvent.PRE_DELETE
				| IResourceChangeEvent.PRE_CLOSE
				/*| IResourceChangeEvent.POST_BUILD*/);

		if (lastState != null) {
			lastState.processResourceChangeEvents(listener);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#start(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {

		if (fDiscoveryPathManager != null) {
			fDiscoveryPathManager.shutdown();
			fDiscoveryPathManager = null;
		}


		
		//	  NOTE: The code below is for tracking resource renaming and deleting.  This is needed to keep
		//      ResourceConfiguration elements up to date.  It may also be needed by AdditionalInput
		//      elements
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(listener);
		IProject projects[] = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for(int i = 0; i < projects.length; i++){
			listener.sendClose(projects[i]);
		}
		listener = null;
		super.stop(context);
	}
	
	private static final String PATH_ENTRY = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/pathEntry"; //$NON-NLS-1$
	private static final String PATH_ENTRY_INIT = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/pathEntryInit"; //$NON-NLS-1$
	private static final String BUILDER = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/builder"; //$NON-NLS-1$
	private static final String BUILD_MODEL = ManagedBuilderCorePlugin.getUniqueIdentifier() + "/debug/buildModel"; //$NON-NLS-1$

	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
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
			String buildModel = Platform.getDebugOption(BUILD_MODEL);
			if(buildModel != null){
				DbgUtil.DEBUG = buildModel.equalsIgnoreCase("true"); //$NON-NLS-1$
			}
			String option = Platform.getDebugOption(SCANNER_CONFIG);
			if (option != null) {
				TraceUtil.SCANNER_CONFIG = option.equalsIgnoreCase("true") ; //$NON-NLS-1$
			}

		}
	}
	
	public static IBuilder[] createBuilders(IProject project, Map args){
		return BuilderFactory.createBuilders(project, args);
	}
	
	public static IBuilder createCustomBuilder(IConfiguration cfg, String builderId) throws CoreException{
		return BuilderFactory.createCustomBuilder(cfg, builderId);
	}
	
	public static IBuilder createCustomBuilder(IConfiguration cfg, IBuilder base){
		return BuilderFactory.createCustomBuilder(cfg, base);
	}
	
	public static IBuilder createBuilderForEclipseBuilder(IConfiguration cfg, String eclipseBuilderID) throws CoreException {
		return BuilderFactory.createBuilderForEclipseBuilder(cfg, eclipseBuilderID);
	}
	
	public IDiscoveredPathManager getDiscoveryManager() {
		if ( fDiscoveryPathManager == null) {
			fDiscoveryPathManager = new DiscoveredPathManager();
			fDiscoveryPathManager.startup();
		}
		return fDiscoveryPathManager;
	}

	public static IScannerConfigBuilderInfo createScannerConfigBuildInfo(
			Preferences prefs, String builderID, boolean useDefaults) {
		return ScannerConfigInfoFactory.create(prefs, builderID, useDefaults);
	}
	
	public static IPath getWorkingDirectory() {
		return ManagedBuilderCorePlugin.getDefault().getStateLocation();
	}

	public static IScannerConfigBuilderInfo createScannerConfigBuildInfo(
			IProject project, String builderID) throws CoreException {
		return ScannerConfigInfoFactory.create(project, builderID);
	}

	public static IScannerConfigBuilderInfo createScannerConfigBuildInfo(
			Map args, String builderID) {
		return ScannerConfigInfoFactory.create(args, builderID);
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

	
/*
	public static IMakeBuilderInfo createBuildInfo(Preferences prefs, String builderID, boolean useDefaults) {
		return BuildInfoFactory.create(prefs, builderID, useDefaults);
	}

	public static IMakeBuilderInfo createBuildInfo(IProject project, String builderID) throws CoreException {
		return BuildInfoFactory.create(project, builderID);
	}

	public static IMakeBuilderInfo createBuildInfo(Map args, String builderID) {
		return BuildInfoFactory.create(args, builderID);
	}
*/
	
}
