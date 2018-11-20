/*******************************************************************************
 * Copyright (c) 2011 Jeff Johnston (Red Hat Inc.) and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Jeff Johnston (Red Hat Inc.), Andrew Gvozdev - initial implementation
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.buildconsole;

import java.net.URI;
import java.net.URL;

import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.ui.preferences.BuildConsolePreferencePage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.Preferences;

/**
 * Build console manager managing the global CDT build console.
 * Singleton.
 *
 */
public class GlobalBuildConsoleManager extends BuildConsoleManager {
	private static final String GLOBAL_BUILD_CONSOLE_NODE = "globalBuildConsole"; //$NON-NLS-1$
	private static final String GLOBAL_LOG_FILE = "global-build.log"; //$NON-NLS-1$

	private static final String GLOBAL_CONTEXT_MENU_ID = CUIPlugin.PLUGIN_ID + ".CDTGlobalBuildConsole"; //$NON-NLS-1$

	/** Singleton instance */
	private static GlobalBuildConsoleManager INSTANCE = null;
	private static BuildConsolePartitioner fGlobalConsolePartitioner = null;

	/**
	 * Default constructor is private. The only instance will be created on
	 * access of static methods and assigned to {@link #INSTANCE}.
	 */
	private GlobalBuildConsoleManager() {
		// startup is in the constructor to ensure starting only once
		startup(ConsoleMessages.BuildConsole_GlobalConsole, GLOBAL_CONTEXT_MENU_ID, null);
	}

	/**
	 * @return get instance creating one if necessary.
	 */
	private static GlobalBuildConsoleManager getInstance() {
		if (INSTANCE == null)
			INSTANCE = new GlobalBuildConsoleManager();
		return INSTANCE;
	}

	/**
	 * @return get global console partitioner creating one if necessary.
	 */
	private static BuildConsolePartitioner getConsolePartitioner() {
		if (fGlobalConsolePartitioner == null) {
			fGlobalConsolePartitioner = new BuildConsolePartitioner(getInstance());
		}
		return fGlobalConsolePartitioner;
	}

	/**
	 * Start the console. This will call {@link #startup(String, String, URL)}
	 * to add the global console to the Console view.
	 */
	public static void startup() {
		// instantiate the INSTANCE
		getInstance();
	}

	/**
	 * Stop the console and deallocate resources allocated during {@link #startup()}
	 */
	public static void stop() {
		// avoid initializing INSTANCE needlessly during shutdown
		if (INSTANCE != null)
			INSTANCE.shutdown();
	}

	@Override
	protected BuildConsole createBuildConsole(String name, String contextId, final URL iconUrl) {
		return new GlobalBuildConsole(this, name, contextId, iconUrl);
	}

	/**
	 * @return the global build console.
	 */
	public static IConsole getGlobalConsole() {
		return getConsolePartitioner().getConsole();
	}

	/**
	 * Start the global console; called at the start of the build.
	 * Clears the contents of the console and sets up the log output stream.
	 */
	public static void startGlobalConsole() {
		if (BuildConsolePreferencePage.isClearBuildConsole())
			getConsolePartitioner().appendToDocument("", null, null); //$NON-NLS-1$
		getConsolePartitioner().setStreamOpened();
	}

	/**
	 * Intended to handle event after console output for the whole build including
	 * referenced projects finished. Currently this event is not triggered and
	 * the function does nothing.
	 */
	public static void stopGlobalConsole() {
		// Doesn't do anything currently. This would be a cleaner place to close the global console
		// log, but there is nowhere in CDT that can invoke it at the end of the entire build.
		// Instead, the log is repeatedly closed and opened for append by each project build.
	}

	/**
	 * @return logging preference store for the workspace.
	 */
	public static IPreferenceStore getBuildLogPreferenceStore() {
		return new ScopedPreferenceStore(InstanceScope.INSTANCE, PREF_QUALIFIER + "/" + GLOBAL_BUILD_CONSOLE_NODE); //$NON-NLS-1$
	}

	/**
	 * @return logging preferences for the workspace.
	 */
	public static Preferences getBuildLogPreferences() {
		return InstanceScope.INSTANCE.getNode(PREF_QUALIFIER).node(GLOBAL_BUILD_CONSOLE_NODE);
	}

	/**
	 * @return default location of logs for the workspace.
	 */
	public static String getDefaultConsoleLogLocation() {
		IPath defaultLogLocation = CUIPlugin.getDefault().getStateLocation().append(GLOBAL_LOG_FILE);
		return defaultLogLocation.toOSString();
	}

	@Override
	public IConsole getConsole(IProject project) {
		return getGlobalConsole();
	}

	@Override
	public IConsole getProjectConsole(IProject project) {
		return getGlobalConsole();
	}

	@Override
	public IDocument getConsoleDocument(IProject project) {
		return getConsolePartitioner().getDocument();
	}

	@Override
	public IProject getLastBuiltProject() {
		return null;
	}

	/**
	 * @return {@link URI} of the global build log or {@code null} if not available.
	 */
	@Override
	public URI getLogURI(IProject project) {
		URI logURI = null;

		Preferences prefs = getBuildLogPreferences();
		boolean keepLog = prefs.getBoolean(KEY_KEEP_LOG, CONSOLE_KEEP_LOG_DEFAULT);
		if (keepLog) {
			String strLocation = prefs.get(KEY_LOG_LOCATION, getDefaultConsoleLogLocation());
			if (strLocation.trim().length() > 0) {
				logURI = URIUtil.toURI(strLocation);
			}
			if (logURI == null) {
				IStatus status = new Status(IStatus.ERROR, CUIPlugin.PLUGIN_ID,
						"Can't determine URI for location=[" + strLocation + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				CUIPlugin.log(status);
			}
		}
		return logURI;
	}

}
