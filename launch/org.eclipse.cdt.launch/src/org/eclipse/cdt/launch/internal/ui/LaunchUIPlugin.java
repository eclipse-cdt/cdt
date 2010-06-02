/*******************************************************************************
 * Copyright (c) 2005, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.launch.internal.ui;

import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate.CLaunch;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class LaunchUIPlugin extends AbstractUIPlugin implements ILaunchesListener2 {

	public static final String PLUGIN_ID = "org.eclipse.cdt.launch"; //$NON-NLS-1$

	// -------- static methods --------

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		LaunchUIPlugin.getDefault().getPluginPreferences().getString( ICDebugConstants.PREF_FILTERED_DEBUGGERS );
		DebugPlugin.getDefault().getLaunchManager().addLaunchListener( this );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener( this );
		super.stop(context);
	}

	/* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchesListener#launchesAdded(org.eclipse.debug.core.ILaunch[])
     */
    public void launchesAdded( ILaunch[] launches ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchesListener#launchesChanged(org.eclipse.debug.core.ILaunch[])
     */
    public void launchesChanged( ILaunch[] launches ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchesListener#launchesRemoved(org.eclipse.debug.core.ILaunch[])
     */
    public void launchesRemoved( ILaunch[] launches ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.core.ILaunchesListener2#launchesTerminated(org.eclipse.debug.core.ILaunch[])
     */
    public void launchesTerminated( ILaunch[] launches ) {
        for ( ILaunch l : launches ) {
            if ( l instanceof CLaunch ) {
                ((CLaunch)l).refresh();
            }
        }
    }
}
