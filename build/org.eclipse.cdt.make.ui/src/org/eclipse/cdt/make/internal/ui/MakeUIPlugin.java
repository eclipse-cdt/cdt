/*******************************************************************************
 * Copyright (c) 2000, 2013 QNX Software Systems and others.
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
 *     Markus Schorn (Wind River Systems)
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.make.internal.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.ui.editor.IMakefileDocumentProvider;
import org.eclipse.cdt.make.internal.ui.editor.MakefileDocumentProvider;
import org.eclipse.cdt.make.internal.ui.editor.WorkingCopyManager;
import org.eclipse.cdt.make.internal.ui.text.ColorManager;
import org.eclipse.cdt.make.ui.IWorkingCopyManager;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class MakeUIPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "org.eclipse.cdt.make.ui"; //$NON-NLS-1$
	//The shared instance.
	private static MakeUIPlugin plugin;
	//Resource bundle.
	private ResourceBundle resourceBundle;

	private IWorkingCopyManager fWorkingCopyManager;
	private IMakefileDocumentProvider fMakefileDocumentProvider;
	private ScopedPreferenceStore fCorePreferenceStore;

	/**
	 * The constructor.
	 */
	public MakeUIPlugin() {
		plugin = this;
		try {
			resourceBundle = ResourceBundle.getBundle("org.eclipse.cdt.make.internal.ui.MakeResources"); //$NON-NLS-1$
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
	}

	/**
	 * Returns the shared instance.
	 */
	public static MakeUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the Unique identifier for this plugin.
	 */
	public static String getPluginId() {
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	public static Shell getActiveWorkbenchShell() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getShell();
		}
		return null;
	}

	/**
	 * Returns the active workbench window or <code>null</code> if none
	 */
	public static IWorkbenchWindow getActiveWorkbenchWindow() {
		return getDefault().getWorkbench().getActiveWorkbenchWindow();
	}

	/**
	 * Returns the active workbench page or <code>null</code> if none.
	 */
	public static IWorkbenchPage getActivePage() {
		IWorkbenchWindow window = getActiveWorkbenchWindow();
		if (window != null) {
			return window.getActivePage();
		}
		return null;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = MakeUIPlugin.getDefault().getResourceBundle();
		try {
			return bundle.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		return resourceBundle;
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
	 * Returns the preference color, identified by the given preference.
	 */
	public static Color getPreferenceColor(String key) {
		return ColorManager.getDefault().getColor(PreferenceConverter.getColor(getDefault().getPreferenceStore(), key));
	}

	public static void log(IStatus status) {
		ResourcesPlugin.getPlugin().getLog().log(status);
	}

	public static void logErrorMessage(String message) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, null));
	}

	public static void logException(Throwable e, final String title, String message) {
		if (e instanceof InvocationTargetException) {
			e = ((InvocationTargetException) e).getTargetException();
		}
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else {
			if (message == null)
				message = e.getMessage();
			if (message == null)
				message = e.toString();
			status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, e);
		}
		ResourcesPlugin.getPlugin().getLog().log(status);
		Display display;
		display = Display.getCurrent();
		if (display == null)
			display = Display.getDefault();
		final IStatus fstatus = status;
		display.asyncExec(() -> ErrorDialog.openError(null, title, null, fstatus));
	}

	public static void logException(Throwable e) {
		logException(e, null, null);
	}

	public static void log(Throwable e) {
		if (e instanceof InvocationTargetException)
			e = ((InvocationTargetException) e).getTargetException();
		IStatus status = null;
		if (e instanceof CoreException)
			status = ((CoreException) e).getStatus();
		else
			status = new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, e.getMessage(), e);
		log(status);
	}

	/**
	* Utility method with conventions
	*/
	public static void errorDialog(Shell shell, String title, String message, IStatus s) {
		log(s);
		// if the 'message' resource string and the IStatus' message are the same,
		// don't show both in the dialog
		if (s != null && message.equals(s.getMessage())) {
			message = null;
		}
		ErrorDialog.openError(shell, title, message, s);
	}

	/**
	* Utility method with conventions
	*/
	public static void errorDialog(Shell shell, String title, String message, Throwable t) {
		log(t);
		IStatus status;
		if (t instanceof CoreException) {
			status = ((CoreException) t).getStatus();
			// if the 'message' resource string and the IStatus' message are the same,
			// don't show both in the dialog
			if (status != null && message.equals(status.getMessage())) {
				message = null;
			}
		} else {
			status = new Status(IStatus.ERROR, MakeUIPlugin.PLUGIN_ID, -1, "Internal Error: ", t); //$NON-NLS-1$
		}
		ErrorDialog.openError(shell, title, message, status);
	}

	public Shell getShell() {
		if (getActiveWorkbenchShell() != null) {
			return getActiveWorkbenchShell();
		}
		IWorkbenchWindow[] windows = getDefault().getWorkbench().getWorkbenchWindows();
		return windows[0].getShell();
	}

	public synchronized IMakefileDocumentProvider getMakefileDocumentProvider() {
		if (fMakefileDocumentProvider == null) {
			fMakefileDocumentProvider = new MakefileDocumentProvider();
		}
		return fMakefileDocumentProvider;
	}

	public synchronized IWorkingCopyManager getWorkingCopyManager() {
		if (fWorkingCopyManager == null) {
			IMakefileDocumentProvider provider = getMakefileDocumentProvider();
			fWorkingCopyManager = new WorkingCopyManager(provider);
		}
		return fWorkingCopyManager;
	}

	/**
	 * Returns a combined preference store, this store is read-only.
	 *
	 * @return the combined preference store
	 */
	public IPreferenceStore getCombinedPreferenceStore() {
		IPreferenceStore[] stores = new IPreferenceStore[2];
		stores[0] = getPreferenceStore();
		stores[1] = EditorsUI.getPreferenceStore();
		ChainedPreferenceStore chainedStore = new ChainedPreferenceStore(stores);
		return chainedStore;
	}

	/**
	 * Returns a preference store for org.eclipse.cdt.make.core preferences
	 * @return the preference store
	 */
	public IPreferenceStore getCorePreferenceStore() {
		if (fCorePreferenceStore == null) {
			fCorePreferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, MakeCorePlugin.PLUGIN_ID);
		}
		return fCorePreferenceStore;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		new MakeStartup().schedule();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		if (fWorkingCopyManager != null) {
			fWorkingCopyManager.shutdown();
			fWorkingCopyManager = null;
		}
		super.stop(context);
	}

}
