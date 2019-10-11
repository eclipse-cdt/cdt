/*******************************************************************************
 * Copyright (c) 2008, 2012 Wind River Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson           - modified to remove dependency on cdt.launch
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.internal.ui;

import org.eclipse.cdt.dsf.gdb.internal.GdbPlugin;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.GdbCliConsoleManager;
import org.eclipse.cdt.dsf.gdb.internal.ui.console.TracingConsoleManager;
import org.eclipse.cdt.dsf.gdb.internal.ui.sync.GdbDebugContextSyncManager;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class GdbUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.dsf.gdb.ui"; //$NON-NLS-1$

	// The shared instance
	private static GdbUIPlugin plugin;

	private static BundleContext fgBundleContext;

	private static TracingConsoleManager fTracingConsoleManager;
	private static GdbCliConsoleManager fGdbConsoleManager;

	private static GdbDebugContextSyncManager fGdbSelectionSyncManager;

	private static IPreferenceStore fCorePreferenceStore;

	/**
	 * The constructor
	 */
	public GdbUIPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		fgBundleContext = context;
		super.start(context);
		plugin = this;

		fTracingConsoleManager = new TracingConsoleManager();
		fTracingConsoleManager.startup();

		fGdbConsoleManager = new GdbCliConsoleManager();
		fGdbConsoleManager.startup();

		fGdbSelectionSyncManager = new GdbDebugContextSyncManager();
		fGdbSelectionSyncManager.startup();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		fTracingConsoleManager.shutdown();
		fGdbConsoleManager.shutdown();
		fGdbSelectionSyncManager.shutdown();

		disposeAdapterSets();
		plugin = null;
		super.stop(context);
		fgBundleContext = null;
	}

	public static GdbCliConsoleManager getCliConsoleManager() {
		return fGdbConsoleManager;
	}

	public static GdbDebugContextSyncManager getGdbSelectionSyncManager() {
		return fGdbSelectionSyncManager;
	}

	/**
	 * Dispose adapter sets for all launches.
	 */
	private void disposeAdapterSets() {
		for (ILaunch launch : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
			if (launch instanceof GdbLaunch) {
				GdbAdapterFactory.disposeAdapterSet(launch);
			}
		}
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GdbUIPlugin getDefault() {
		return plugin;
	}

	public static BundleContext getBundleContext() {
		return fgBundleContext;
	}

	/**
	 * Returns the preference store for this UI plug-in.
	 * It actually uses the preference store of the core plug-in.
	 */
	@Override
	public IPreferenceStore getPreferenceStore() {
		if (fCorePreferenceStore == null) {
			fCorePreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, GdbPlugin.PLUGIN_ID);
		}
		return fCorePreferenceStore;
	}

	/**
	 * copied from org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin
	 */
	private static Shell debugDialogShell;

	public static Shell getShell() {
		if (getActiveWorkbenchShell() != null) {
			return getActiveWorkbenchShell();
		}
		if (debugDialogShell != null) {
			if (!debugDialogShell.isDisposed())
				return debugDialogShell;
			debugDialogShell = null;
		}
		IWorkbenchWindow[] windows = getDefault().getWorkbench().getWorkbenchWindows();
		return windows[0].getShell();
	}

	public static void setDialogShell(Shell shell) {
		debugDialogShell = shell;
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 */
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
	 * Logs the specified status with this plug-in's log.
	 *
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Logs an internal error with the specified message.
	 *
	 * @param message
	 *            the error message to log
	 */
	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 *
	 * @param e
	 *            the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e.getMessage(), e));
	}

	/**
	 * Returns the active workbench window
	 *
	 * @return the active workbench window
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow w = getActiveWorkbenchWindow();
		if (w != null) {
			return w.getActivePage();
		}
		return null;
	}

	/**
	 * Returns the active workbench shell or <code>null</code> if none
	 *
	 * @return the active workbench shell or <code>null</code> if none
	 */
	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	public static void errorDialog(String message, IStatus status) {
		log(status);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			ErrorDialog.openError(shell, LaunchMessages.getString("LaunchUIPlugin.Error"), message, status); //$NON-NLS-1$
		}
	}

	public static void errorDialog(String message, Throwable t) {
		log(t);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), 1, t.getMessage(), null);
			ErrorDialog.openError(shell, LaunchMessages.getString("LaunchUIPlugin.Error"), message, status); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#initializeImageRegistry(org.eclipse.jface.resource.ImageRegistry)
	 */
	@Override
	protected void initializeImageRegistry(ImageRegistry reg) {
		super.initializeImageRegistry(reg);
		declareImages(reg);
	}

	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, path).get();
	}

	public static Image getImage(String key) {
		return getDefault().getImageRegistry().get(key);
	}

	private void declareImages(ImageRegistry reg) {
		reg.put(IGdbUIConstants.IMG_WIZBAN_ADVANCED_TIMEOUT_SETTINGS,
				getImageDescriptor("icons/full/wizban/advtosettings_wiz.png")); //$NON-NLS-1$
	}

	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

}
