/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc Khouzam (Ericsson) - initial API and implementation
 *     William R. Swanson (Tilera Corporation) - added resource support
 *     Marc Dumais (Ericsson) - Bug 460837
 *******************************************************************************/
package org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui;

import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.UIResourceManager;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class MulticoreVisualizerUIPlugin extends AbstractUIPlugin {
	// --- constants ---

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.dsf.gdb.multicorevisualizer.ui"; //$NON-NLS-1$

	// --- static members ---

	/** Singleton instance */
	private static MulticoreVisualizerUIPlugin plugin;

	/** Bundle context */
	private static BundleContext fgBundleContext;

	/** Resource manager */
	protected static UIResourceManager s_resources = null;

	/**
	 * Returns the shared instance
	 */
	public static MulticoreVisualizerUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the bundle context for this plugin.
	 */
	public static BundleContext getBundleContext() {
		return fgBundleContext;
	}

	// --- constructors/destructors ---

	/**
	 * The constructor
	 */
	public MulticoreVisualizerUIPlugin() {
	}

	// --- plugin startup/shutdown methods ---

	@Override
	public void start(BundleContext context) throws Exception {
		fgBundleContext = context;
		super.start(context);
		plugin = this;

		// initialize resource management (strings, images, fonts, colors, etc.)
		getPluginResources();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		// clean up resource management
		cleanupPluginResources();

		plugin = null;
		super.stop(context);
		fgBundleContext = null;
	}

	// --- logging ---

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

	// --- accessors ---

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

	// --- UI plugin support ---

	/**
	 * copied from org.eclipse.cdt.launch.internal.ui.LaunchUIPlugin
	 */
	private static Shell debugDialogShell;

	/**
	 * Returns shell (workbench or dialog) for this plugin.
	 */
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

	/**
	 * Displays an error dialog.
	 */
	public static void errorDialog(String message, IStatus status) {
		log(status);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			ErrorDialog.openError(shell, LaunchMessages.getString("LaunchUIPlugin.Error"), message, status); //$NON-NLS-1$
		}
	}

	/**
	 * Displays an error dialog.
	 */
	public static void errorDialog(String message, Throwable t) {
		log(t);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(), 1, t.getMessage(), null);
			ErrorDialog.openError(shell, LaunchMessages.getString("LaunchUIPlugin.Error"), message, status); //$NON-NLS-1$
		}
	}

	// --- resource management ---

	/** Returns resource manager for this plugin */
	public UIResourceManager getPluginResources() {
		if (s_resources == null) {
			// FindBugs reported that it is unsafe to set s_resources
			// before we finish to initialize the object, because of
			// multi-threading.  This is why we use a temporary variable.
			UIResourceManager resourceManager = new UIResourceManager(this);
			resourceManager.setParentManager(CDTVisualizerUIPlugin.getResources());
			s_resources = resourceManager;
		}

		return s_resources;
	}

	/** Releases resource manager for this plugin. */
	public void cleanupPluginResources() {
		if (s_resources != null)
			s_resources.dispose();
	}

	/** Convenience method for getting plugin resource manager */
	public static UIResourceManager getResources() {
		return getDefault().getPluginResources();
	}

	/** Convenience method for looking up string resources */
	public static String getString(String key) {
		return getDefault().getPluginResources().getString(key);
	}

	/** Convenience method for looking up string resources */
	public static String getString(String key, Object... arguments) {
		return getDefault().getPluginResources().getString(key, arguments);
	}

	/** Convenience method for looking up image resources */
	public static Image getImage(String key) {
		return getDefault().getPluginResources().getImage(key);
	}

	/** Convenience method for looking up image resources */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getPluginResources().getImageDescriptor(key);
	}

	/** Convenience method for looking up font resources */
	public static Font getFont(String fontName, int height) {
		return getDefault().getPluginResources().getFont(fontName, height);
	}

	/** Convenience method for looking up font resources */
	public static Font getFont(String fontName, int height, int style) {
		return getDefault().getPluginResources().getFont(fontName, height, style);
	}

	/** Get the preference store for this Eclipse plug-in */
	public static IEclipsePreferences getEclipsePreferenceStore() {
		return InstanceScope.INSTANCE.getNode(PLUGIN_ID);
	}
}
