/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ken Ryall (Nokia) - Support for breakpoint actions (bug 118308)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.cdt.debug.core;

import java.util.HashSet;

import org.eclipse.cdt.debug.core.breakpointactions.BreakpointActionManager;
import org.eclipse.cdt.debug.core.command.CCommandAdapterFactory;
import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextService;
import org.eclipse.cdt.debug.core.launch.CoreBuildLaunchBarTracker;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.IRestart;
import org.eclipse.cdt.debug.core.sourcelookup.AbsolutePathSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.CProjectSourceContainer;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocation;
import org.eclipse.cdt.debug.core.sourcelookup.ProgramRelativePathSourceContainer;
import org.eclipse.cdt.debug.internal.core.DebugModelProvider;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.core.ListenerList;
import org.eclipse.cdt.debug.internal.core.Trace;
import org.eclipse.cdt.debug.internal.core.disassembly.DisassemblyContextService;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CommonSourceLookupDirector;
import org.eclipse.cdt.debug.internal.core.sourcelookup.SourceUtils;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchDelegate;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.launchbar.core.ILaunchBarManager;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The plugin class for C/C++ debug core.
 */
public class CDebugCorePlugin extends Plugin {
	/**
	 * The plug-in identifier (value <code>"org.eclipse.cdt.debug.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.core"; //$NON-NLS-1$

	/**
	 * Status code indicating an unexpected internal error.
	 */
	public static final int INTERNAL_ERROR = 1000;

	/**
	 * The shared instance.
	 */
	private static CDebugCorePlugin plugin;

	/**
	 * Breakpoint listener list.
	 */
	private ListenerList fBreakpointListeners;

	/**
	 * Breakpoint action manager.
	 */
	private BreakpointActionManager breakpointActionManager;

	private DisassemblyContextService fDisassemblyContextService;

	public static final String CDEBUGGER_EXTENSION_POINT_ID = "CDebugger"; //$NON-NLS-1$
	public static final String DEBUGGER_ELEMENT = "debugger"; //$NON-NLS-1$

	public static final String BREAKPOINT_ACTION_EXTENSION_POINT_ID = "BreakpointActionType"; //$NON-NLS-1$
	public static final String ACTION_TYPE_ELEMENT = "actionType"; //$NON-NLS-1$

	public static final String BREAKPOINT_EXTENSION_EXTENSION_POINT_ID = "BreakpointExtension"; //$NON-NLS-1$
	public static final String BREAKPOINT_EXTENSION_ELEMENT = "breakpointExtension"; //$NON-NLS-1$

	/**
	 * Dummy source lookup director needed to manage common source containers.
	 */
	private CommonSourceLookupDirector fCommonSourceLookupDirector;

	private CoreBuildLaunchBarTracker coreBuildLaunchBarTracker;

	/**
	 * The constructor.
	 */
	public CDebugCorePlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 *
	 * @return the shared instance
	 */
	public static CDebugCorePlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 *
	 * @return the workspace instance
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 *
	 * @return the unique identifier of this plugin
	 */
	public static String getUniqueIdentifier() {
		return PLUGIN_ID;
	}

	/**
	 * Logs the specified throwable with this plug-in's log.
	 *
	 * @param t throwable to log
	 */
	public static void log(Throwable t) {
		Throwable top = t;
		if (t instanceof DebugException) {
			DebugException de = (DebugException) t;
			IStatus status = de.getStatus();
			if (status.getException() != null) {
				top = status.getException();
			}
		}
		// this message is intentionally not internationalized, as an exception may
		// be due to the resource bundle itself
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), INTERNAL_ERROR, "Internal error logged from CDI Debug: ", //$NON-NLS-1$
				top));
	}

	/**
	 * Logs the specified status with this plug-in's log.
	 *
	 * @param status status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs the specified message with this plug-in's log.
	 *
	 * @param status status to log
	 */
	public static void log(String message) {
		getDefault().getLog()
				.log(new Status(IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), INTERNAL_ERROR, message, null));
	}

	public void saveCommonSourceLocations(ICSourceLocation[] locations) {
		CDebugCorePlugin.getDefault().getPluginPreferences().setValue(ICDebugConstants.PREF_SOURCE_LOCATIONS,
				SourceUtils.getCommonSourceLocationsMemento(locations));
	}

	public ICSourceLocation[] getCommonSourceLocations() {
		return SourceUtils.getCommonSourceLocationsFromMemento(
				CDebugCorePlugin.getDefault().getPluginPreferences().getString(ICDebugConstants.PREF_SOURCE_LOCATIONS));
	}

	/**
	 * Adds the given breakpoint listener to the debug model.
	 *
	 * @param listener breakpoint listener
	 */
	public void addCBreakpointListener(ICBreakpointListener listener) {
		fBreakpointListeners.add(listener);
	}

	/**
	 * Removes the given breakpoint listener from the debug model.
	 *
	 * @param listener breakpoint listener
	 */
	public void removeCBreakpointListener(ICBreakpointListener listener) {
		fBreakpointListeners.remove(listener);
	}

	/**
	 * Returns the list of breakpoint listeners registered with this plugin.
	 *
	 * @return the list of breakpoint listeners registered with this plugin
	 */
	public Object[] getCBreakpointListeners() {
		if (fBreakpointListeners == null)
			return new Object[0];
		return fBreakpointListeners.getListeners();
	}

	private void createBreakpointListenersList() {
		fBreakpointListeners = new ListenerList(1);
	}

	private void disposeBreakpointListenersList() {
		fBreakpointListeners.removeAll();
		fBreakpointListeners = null;
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		Trace.init();
		initializeCommonSourceLookupDirector();
		createCommandAdapterFactory();
		createBreakpointListenersList();
		createDisassemblyContextService();
		setDefaultLaunchDelegates();

		Platform.getAdapterManager().registerAdapters(new DebugModelProvider(), ICDebugElement.class);

		// Add core build launch bar listener
		ILaunchBarManager launchBarManager = getService(ILaunchBarManager.class);
		coreBuildLaunchBarTracker = new CoreBuildLaunchBarTracker();
		launchBarManager.addListener(coreBuildLaunchBarTracker);
	}

	/* (non-Javadoc)
	 * @see org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		ILaunchBarManager launchBarManager = getService(ILaunchBarManager.class);
		launchBarManager.removeListener(coreBuildLaunchBarTracker);
		coreBuildLaunchBarTracker = null;

		disposeDisassemblyContextService();
		disposeBreakpointListenersList();
		disposeCommonSourceLookupDirector();
		super.stop(context);
	}

	/**
	 * @since 8.1
	 */
	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	private void createCommandAdapterFactory() {
		IAdapterManager manager = Platform.getAdapterManager();
		CCommandAdapterFactory actionFactory = new CCommandAdapterFactory();
		manager.registerAdapters(actionFactory, IRestart.class);
	}

	private void initializeCommonSourceLookupDirector() {
		if (fCommonSourceLookupDirector == null) {
			fCommonSourceLookupDirector = new CommonSourceLookupDirector();
			boolean convertingFromLegacyFormat = false;
			String newMemento = CDebugCorePlugin.getDefault().getPluginPreferences()
					.getString(ICDebugInternalConstants.PREF_DEFAULT_SOURCE_CONTAINERS);
			if (newMemento.isEmpty()) {
				newMemento = CDebugCorePlugin.getDefault().getPluginPreferences()
						.getString(ICDebugInternalConstants.PREF_COMMON_SOURCE_CONTAINERS);
				convertingFromLegacyFormat = true;
			}
			if (newMemento.isEmpty()) {
				// Add the participant(s). This happens as part of
				// initializeFromMemento(), but since we're not calling it, we
				// need to do this explicitly. See 299583.
				fCommonSourceLookupDirector.initializeParticipants();

				// Convert source locations to source containers
				convertSourceLocations(fCommonSourceLookupDirector);
			} else {
				try {
					fCommonSourceLookupDirector.initializeFromMemento(newMemento);
				} catch (CoreException e) {
					log(e.getStatus());
				}
			}
			if (convertingFromLegacyFormat) {
				// Add three source containers that used to be present implicitly.
				ISourceContainer[] oldContainers = fCommonSourceLookupDirector.getSourceContainers();
				ISourceContainer[] containers = new ISourceContainer[oldContainers.length + 3];
				int i = 0;
				containers[i++] = new AbsolutePathSourceContainer();
				containers[i++] = new ProgramRelativePathSourceContainer();
				containers[i++] = new CProjectSourceContainer(null, true);
				System.arraycopy(oldContainers, 0, containers, i, oldContainers.length);
				fCommonSourceLookupDirector.setSourceContainers(containers);
			}
		}
	}

	private void disposeCommonSourceLookupDirector() {
		if (fCommonSourceLookupDirector != null)
			fCommonSourceLookupDirector.dispose();
	}

	public CSourceLookupDirector getCommonSourceLookupDirector() {
		return fCommonSourceLookupDirector;
	}

	private void convertSourceLocations(CommonSourceLookupDirector director) {
		director.setSourceContainers(SourceUtils.convertSourceLocations(getCommonSourceLocations()));
	}

	public BreakpointActionManager getBreakpointActionManager() {
		if (breakpointActionManager == null)
			breakpointActionManager = new BreakpointActionManager();
		return breakpointActionManager;
	}

	private void createDisassemblyContextService() {
		fDisassemblyContextService = new DisassemblyContextService();
	}

	public IDisassemblyContextService getDisassemblyContextService() {
		return fDisassemblyContextService;
	}

	private void disposeDisassemblyContextService() {
		if (fDisassemblyContextService != null)
			fDisassemblyContextService.dispose();
	}

	private void setDefaultLaunchDelegates() {
		// Set the default launch delegates as early as possible, and do it only once (Bug 312997)
		ILaunchManager launchMgr = DebugPlugin.getDefault().getLaunchManager();

		HashSet<String> debugSet = new HashSet<>();
		debugSet.add(ILaunchManager.DEBUG_MODE);

		ILaunchConfigurationType localCfg = launchMgr
				.getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_APP);
		try {
			if (localCfg.getPreferredDelegate(debugSet) == null) {
				ILaunchDelegate[] delegates = localCfg.getDelegates(debugSet);
				for (ILaunchDelegate delegate : delegates) {
					if (ICDTLaunchConfigurationConstants.PREFERRED_DEBUG_LOCAL_LAUNCH_DELEGATE
							.equals(delegate.getId())) {
						localCfg.setPreferredDelegate(debugSet, delegate);
						break;
					}
				}
			}
		} catch (CoreException e) {
		}

		ILaunchConfigurationType remoteCfg = launchMgr
				.getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_REMOTE_APP);
		try {
			if (remoteCfg.getPreferredDelegate(debugSet) == null) {
				ILaunchDelegate[] delegates = remoteCfg.getDelegates(debugSet);
				for (ILaunchDelegate delegate : delegates) {
					if (ICDTLaunchConfigurationConstants.PREFERRED_DEBUG_REMOTE_LAUNCH_DELEGATE
							.equals(delegate.getId())) {
						remoteCfg.setPreferredDelegate(debugSet, delegate);
						break;
					}
				}
			}
		} catch (CoreException e) {
		}

		ILaunchConfigurationType attachCfg = launchMgr
				.getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_ATTACH);
		try {
			if (attachCfg.getPreferredDelegate(debugSet) == null) {
				ILaunchDelegate[] delegates = attachCfg.getDelegates(debugSet);
				for (ILaunchDelegate delegate : delegates) {
					if (ICDTLaunchConfigurationConstants.PREFERRED_DEBUG_ATTACH_LAUNCH_DELEGATE
							.equals(delegate.getId())) {
						attachCfg.setPreferredDelegate(debugSet, delegate);
						break;
					}
				}
			}
		} catch (CoreException e) {
		}

		ILaunchConfigurationType postMortemCfg = launchMgr
				.getLaunchConfigurationType(ICDTLaunchConfigurationConstants.ID_LAUNCH_C_POST_MORTEM);
		try {
			if (postMortemCfg.getPreferredDelegate(debugSet) == null) {
				ILaunchDelegate[] delegates = postMortemCfg.getDelegates(debugSet);
				for (ILaunchDelegate delegate : delegates) {
					if (ICDTLaunchConfigurationConstants.PREFERRED_DEBUG_POSTMORTEM_LAUNCH_DELEGATE
							.equals(delegate.getId())) {
						postMortemCfg.setPreferredDelegate(debugSet, delegate);
						break;
					}
				}
			}
		} catch (CoreException e) {
		}

		HashSet<String> runSet = new HashSet<>();
		runSet.add(ILaunchManager.RUN_MODE);

		try {
			if (localCfg.getPreferredDelegate(runSet) == null) {
				ILaunchDelegate[] delegates = localCfg.getDelegates(runSet);
				for (ILaunchDelegate delegate : delegates) {
					if (ICDTLaunchConfigurationConstants.PREFERRED_RUN_LAUNCH_DELEGATE.equals(delegate.getId())) {
						localCfg.setPreferredDelegate(runSet, delegate);
						break;
					}
				}
			}
		} catch (CoreException e) {
		}
	}
}
