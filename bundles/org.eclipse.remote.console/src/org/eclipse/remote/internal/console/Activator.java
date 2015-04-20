package org.eclipse.remote.internal.console;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.remote.console"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	@Override
	protected void initializeImageRegistry(ImageRegistry imageRegistry) {
		HashMap<String, String> map = new HashMap<>();

		try {
			// Local toolbars
			map.put(ImageConsts.IMAGE_NEW_TERMINAL, "newterminal.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_CONNECT, "connect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_DISCONNECT, "disconnect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_SETTINGS, "properties_tsk.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_COMMAND_INPUT_FIELD, "command_input_field.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_CLCL_SCROLL_LOCK, "lock_co.gif"); //$NON-NLS-1$

			loadImageRegistry(imageRegistry, ImageConsts.IMAGE_DIR_LOCALTOOL, map);

			map.clear();

			// Enabled local toolbars
			map.put(ImageConsts.IMAGE_NEW_TERMINAL, "newterminal.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_CONNECT, "connect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_DISCONNECT, "disconnect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_SETTINGS, "properties_tsk.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_COMMAND_INPUT_FIELD, "command_input_field.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_SCROLL_LOCK, "lock_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_ELCL_REMOVE, "rem_co.gif"); //$NON-NLS-1$

			loadImageRegistry(imageRegistry, ImageConsts.IMAGE_DIR_ELCL, map);

			map.clear();

			// Disabled local toolbars
			map.put(ImageConsts.IMAGE_NEW_TERMINAL, "newterminal.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_CONNECT, "connect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_DISCONNECT, "disconnect_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_SETTINGS, "properties_tsk.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_COMMAND_INPUT_FIELD, "command_input_field.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_SCROLL_LOCK, "lock_co.gif"); //$NON-NLS-1$
			map.put(ImageConsts.IMAGE_DLCL_REMOVE, "rem_co.gif"); //$NON-NLS-1$

			loadImageRegistry(imageRegistry, ImageConsts.IMAGE_DIR_DLCL, map);

			map.clear();

			map.put(ImageConsts.IMAGE_TERMINAL_VIEW, "terminal_view.gif"); //$NON-NLS-1$

			loadImageRegistry(imageRegistry, ImageConsts.IMAGE_DIR_EVIEW, map);

			map.clear();

		} catch (MalformedURLException malformedURLException) {
			malformedURLException.printStackTrace();
		}
	}

	protected void loadImageRegistry(ImageRegistry imageRegistry, String strDir, HashMap<String, String> map)
			throws MalformedURLException {
		ImageDescriptor imageDescriptor;

		for (Entry<String, String> entry : map.entrySet()) {
			URL url = plugin.getBundle().getEntry(ImageConsts.IMAGE_DIR_ROOT + strDir + entry.getValue());
			imageDescriptor = ImageDescriptor.createFromURL(url);
			imageRegistry.put(entry.getKey(), imageDescriptor);
		}
	}
	
	/**
	 * Create log entry from an IStatus
	 * 
	 * @param status
	 *            status to log
	 */
	public static void log(IStatus status) {
		getDefault().getLog().log(status);
	}

	/**
	 * Create log entry from a string
	 * 
	 * @param msg
	 *            message to log
	 */
	public static void log(String msg) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, msg, null));
	}

	/**
	 * Create log entry from a Throwable
	 * 
	 * @param e
	 *            throwable to log
	 */
	public static void log(Throwable e) {
		log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, e.getMessage(), e));
	}

	/**
	 * Return the OSGi service with the given service interface.
	 *
	 * @param service
	 *            service interface
	 * @return the specified service or null if it's not registered
	 */
	public static <T> T getService(Class<T> service) {
		final BundleContext context = plugin.getBundle().getBundleContext();
		final ServiceReference<T> ref = context.getServiceReference(service);
		return ref != null ? context.getService(ref) : null;
	}
}
