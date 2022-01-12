/*******************************************************************************
 *  Copyright (c) 2005, 2016 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
 *
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     Bjorn Freeman-Benson - initial API and implementation
 *     Wind River Systems - adopted to use with DSF
 *******************************************************************************/
package org.eclipse.cdt.examples.dsf.pda;

import java.io.File;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.Query;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.examples.dsf.pda.launch.PDALaunch;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PDAPlugin extends Plugin {

	public static String PLUGIN_ID = "org.eclipse.cdt.examples.dsf.pda";

	// Debugging flag
	public static boolean DEBUG = false;

	//The shared instance.
	private static PDAPlugin plugin;

	//Resource bundle.
	private ResourceBundle resourceBundle;

	// Bundle context used in registering and retrieving DSF (OSGi) services.
	private static BundleContext fContext;

	/**
	 * Unique identifier for the PDA debug model (value
	 * <code>pda.debugModel</code>).
	 */
	public static final String ID_PDA_DEBUG_MODEL = "org.eclipse.cdt.examples.dsf.pda.debugModel";

	/**
	 * Name of the string substitution variable that resolves to the
	 * location of a local Perl executable (value <code>perlExecutable</code>).
	 */
	public static final String VARIALBE_PERL_EXECUTABLE = "dsfPerlExecutable";

	/**
	 * Launch configuration attribute key. Value is a path to a perl
	 * program. The path is a string representing a full path
	 * to a perl program in the workspace.
	 */
	public static final String ATTR_PDA_PROGRAM = ID_PDA_DEBUG_MODEL + ".ATTR_PDA_PROGRAM";

	/**
	 * Identifier for the PDA launch configuration type
	 * (value <code>pda.launchType</code>)
	 */
	public static final String ID_PDA_LAUNCH_CONFIGURATION_TYPE = "org.eclipse.cdt.examples.dsf.pda.launchType";

	/**
	 * The constructor.
	 */
	public PDAPlugin() {
		super();
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		fContext = context;
		DEBUG = Boolean.parseBoolean(Platform.getDebugOption(PLUGIN_ID + "/debug")); //$NON-NLS-1$
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		shutdownActiveLaunches();
		super.stop(context);
		plugin = null;
		resourceBundle = null;
		fContext = context;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PDAPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the string from the plugin's resource bundle,
	 * or 'key' if not found.
	 */
	public static String getResourceString(String key) {
		ResourceBundle bundle = PDAPlugin.getDefault().getResourceBundle();
		try {
			return (bundle != null) ? bundle.getString(key) : key;
		} catch (MissingResourceException e) {
			return key;
		}
	}

	/**
	 * Returns the plugin's resource bundle,
	 */
	public ResourceBundle getResourceBundle() {
		try {
			if (resourceBundle == null)
				resourceBundle = ResourceBundle
						.getBundle("org.eclipse.debug.examples.core.pda.DebugCorePluginResources");
		} catch (MissingResourceException x) {
			resourceBundle = null;
		}
		return resourceBundle;
	}

	public static BundleContext getBundleContext() {
		return fContext;
	}

	/**
	 * Return a <code>java.io.File</code> object that corresponds to the specified
	 * <code>IPath</code> in the plugin directory, or <code>null</code> if none.
	 */
	public static File getFileInPlugin(IPath path) {
		if (true) {
			throw new RuntimeException(
					"The commented out code below has not worked in many years with a NullPointerException. Now the API that was returning null has been removed, see Bug 475944");
		}
		return null;
		//		try {
		//			URL installURL = new URL(getDefault().getDescriptor().getInstallURL(), path.toString());
		//			URL localURL = Platform.asLocalURL(installURL);
		//			return new File(localURL.getFile());
		//		} catch (IOException ioe) {
		//			return null;
		//		}
	}

	/**
	 * Shuts down any active launches.  We must shutdown any active sessions
	 * and services associated with this plugin before this plugin is stopped.
	 * Any attempts to use the plugins {@link BundleContext} after the plugin
	 * is shut down will result in exceptions.
	 */
	private void shutdownActiveLaunches() {
		for (ILaunch launch : DebugPlugin.getDefault().getLaunchManager().getLaunches()) {
			if (launch instanceof PDALaunch && !((PDALaunch) launch).isShutDown()) {
				final PDALaunch pdaLaunch = (PDALaunch) launch;

				Query<Object> launchShutdownQuery = new Query<Object>() {
					@Override
					protected void execute(DataRequestMonitor<Object> rm) {
						pdaLaunch.shutdownServices(rm);
					}
				};

				try {
					pdaLaunch.getSession().getExecutor().execute(launchShutdownQuery);
				} catch (RejectedExecutionException e) {
					// We can get this exception if the session is shutdown concurrently
					// to this method running.
					break;
				}

				// The Query.get() method is a synchronous call which blocks until the
				// query completes.
				try {
					launchShutdownQuery.get();
				} catch (InterruptedException e) {
					getLog().log(new Status(IStatus.ERROR, PLUGIN_ID,
							"InterruptedException while shutting down PDA debugger launch " + pdaLaunch, e.getCause()));
				} catch (ExecutionException e) {
					getLog().log(new Status(IStatus.ERROR, PLUGIN_ID,
							"Exception while shutting down PDA debugger launch " + pdaLaunch, e.getCause()));
				}
			}
		}
	}

	public static void failRequest(RequestMonitor rm, int code, String message) {
		rm.setStatus(new Status(IStatus.ERROR, PLUGIN_ID, code, message, null));
		rm.done();
	}

	public static void debug(String debugString) {
		if (DEBUG) {
			System.out.println(debugString);
		}
	}
}
