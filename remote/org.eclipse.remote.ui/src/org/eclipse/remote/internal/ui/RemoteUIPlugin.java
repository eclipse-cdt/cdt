package org.eclipse.remote.internal.ui;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.remote.internal.ui.messages.Messages;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

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
		log(new Status(IStatus.ERROR, getDefault().getBundle().getSymbolicName(), IStatus.ERROR,
				Messages.PTPRemoteUIPlugin_3, e));
	}

	/**
	 * Return the OSGi service with the given service interface.
	 *
	 * @param service service interface
	 * @return the specified service or null if it's not registered
	 */
	public static <T> T getService(Class<T> service) {
		BundleContext context = plugin.getBundle().getBundleContext();
		ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}

	/**
	 * The constructor
	 */
	public RemoteUIPlugin() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}
}
