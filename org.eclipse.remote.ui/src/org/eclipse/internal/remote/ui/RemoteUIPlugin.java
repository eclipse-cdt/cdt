package org.eclipse.internal.remote.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.internal.remote.ui.messages.Messages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class RemoteUIPlugin extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.remote.ui"; //$NON-NLS-1$

	// The shared instance
	private static RemoteUIPlugin plugin;

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static RemoteUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Get unique identifier for this plugin
	 * 
	 * @since 7.0
	 */
	public static String getUniqueIdentifier() {
		if (getDefault() == null) {
			return PLUGIN_ID;
		}
		return getDefault().getBundle().getSymbolicName();
	}

	/**
	 * Generate a log message given an IStatus object
	 * 
	 * @param status
	 *            IStatus object
	 * @since 5.0
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Generate a log message
	 * 
	 * @param msg
	 *            message to log
	 * @since 5.0
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, msg, null));
	}

	/**
	 * Generate a log message for an exception
	 * 
	 * @param e
	 *            exception used to generate message
	 * @since 5.0
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR, Messages.PTPRemoteUIPlugin_3, e));
	}

	/**
	 * The constructor
	 */
	public RemoteUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
}
