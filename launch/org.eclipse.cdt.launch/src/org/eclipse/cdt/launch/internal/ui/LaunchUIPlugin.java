/**********************************************************************
 * Copyright (c) 2002 - 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class LaunchUIPlugin extends AbstractUIPlugin
		implements
			IDebugEventSetListener {
	public static final String PLUGIN_ID = "org.eclipse.cdt.launch"; //$NON-NLS-1$

	private static final String BUNDLE_NAME = "org.eclipse.cdt.launch.internal.ui.LaunchUIPluginResources";//$NON-NLS-1$
	private static ResourceBundle resourceBundle = null;

	// -------- static methods --------

	static {
		if (resourceBundle == null) {
			// Acquire a reference to the .properties file for this plug-in
			try {
				resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
			} catch (MissingResourceException e) {
				resourceBundle = null;
			}
		}
	}

	/**
	 * Launch UI plug-in instance
	 */
	private static LaunchUIPlugin fgPlugin;
	private static Shell debugDialogShell;

	/**
	 * Constructor for LaunchUIPlugin.
	 * 
	 * @param descriptor
	 */
	public LaunchUIPlugin() {
		super();
		setDefault(this);
	}

	/**
	 * Sets the Java Debug UI plug-in instance
	 * 
	 * @param plugin
	 *            the plugin instance
	 */
	private static void setDefault(LaunchUIPlugin plugin) {
		fgPlugin = plugin;
	}

	/**
	 * Returns the Java Debug UI plug-in instance
	 * 
	 * @return the Java Debug UI plug-in instance
	 */
	public static LaunchUIPlugin getDefault() {
		return fgPlugin;
	}

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
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR,
				message, null));
	}

	/**
	 * Logs an internal error with the specified throwable
	 * 
	 * @param e
	 *            the exception to be logged
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getUniqueIdentifier(), IStatus.ERROR, e
				.getMessage(), e)); //$NON-NLS-1$
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
			ErrorDialog
					.openError(
							shell,
							LaunchUIPlugin
									.getResourceString("LaunchUIPlugin.Error"), message, status); //$NON-NLS-1$
		}
	}

	public static void errorDialog(String message, Throwable t) {
		log(t);
		Shell shell = getActiveWorkbenchShell();
		if (shell != null) {
			IStatus status = new Status(IStatus.ERROR, getUniqueIdentifier(),
					1, t.getMessage(), null); //$NON-NLS-1$	
			ErrorDialog
					.openError(
							shell,
							LaunchUIPlugin
									.getResourceString("LaunchUIPlugin.Error"), message, status); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		super.stop(context);
	}

	/**
	 * Notifies this listener of the given debug events. All of the events in
	 * the given event collection occurred at the same location the program be
	 * run or debugged.
	 * 
	 * @param events
	 *            the debug events
	 */
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			if (events[i].getKind() == DebugEvent.TERMINATE) {
				Object o = events[i].getSource();
				if (o instanceof IProcess) {
					IProcess proc = (IProcess) o;
					ICProject cproject = null;
					try {
						cproject = AbstractCLaunchDelegate.getCProject(proc
								.getLaunch().getLaunchConfiguration());
					} catch (CoreException e) {
					}
					if (cproject != null) {
						try {
							cproject.getProject().refreshLocal(
									IResource.DEPTH_INFINITE, null);
						} catch (CoreException e) {
						}
					}
				}
			}
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
	}

	public static String getResourceString(String key) {
		ResourceBundle bundle = LaunchUIPlugin.getDefault().getResourceBundle();

		// No point trying if bundle is null as exceptions are costly
		if (bundle != null) {
			try {
				return bundle.getString(key);
			} catch (MissingResourceException e) {
				return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
			} catch (NullPointerException e) {
				return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// If we get here, then bundle is null.
		return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getFormattedResourceString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new String[]{arg});
	}

	public static String getFormattedResourceString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), args);
	}

}