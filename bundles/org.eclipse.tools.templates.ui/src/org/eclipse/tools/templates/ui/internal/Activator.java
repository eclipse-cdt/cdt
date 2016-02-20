package org.eclipse.tools.templates.ui.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	private static Activator plugin;

	private TemplateExtension templateExtension;

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;

		templateExtension = new TemplateExtension();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static String getId() {
		return plugin.getBundle().getSymbolicName();
	}

	public static TemplateExtension getTemplateExtension() {
		return plugin.templateExtension;
	}

	public static void log(Exception e) {
		if (e instanceof CoreException) {
			plugin.getLog().log(((CoreException) e).getStatus());
		} else {
			plugin.getLog().log(new Status(IStatus.ERROR, getId(), e.getLocalizedMessage(), e));
		}
	}

}
