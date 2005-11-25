package org.eclipse.cdt.pdom.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

/**
 * The main plugin class to be used in the desktop.
 */
public class PDOMCorePlugin extends Plugin {

	public static final String ID = "org.eclipse.cdt.pdom.core"; //$NON-NLS-1$
	
	//The shared instance.
	private static PDOMCorePlugin plugin;
	
	/**
	 * The constructor.
	 */
	public PDOMCorePlugin() {
		plugin = this;
	}

	/**
	 * This method is called upon plug-in activation
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
	}

	/**
	 * This method is called when the plug-in is stopped
	 */
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
	}

	/**
	 * Returns the shared instance.
	 */
	public static PDOMCorePlugin getDefault() {
		return plugin;
	}

	public static void log(CoreException e) {
		plugin.getLog().log(e.getStatus());
	}
	
}
