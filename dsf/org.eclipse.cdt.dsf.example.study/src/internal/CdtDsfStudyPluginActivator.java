package internal;

import org.eclipse.jface.resource.ResourceLocator;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class CdtDsfStudyPluginActivator extends AbstractUIPlugin {
	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.dsf.study.id"; //$NON-NLS-1$

	public static final String IMG_EMPLOYEE = "icons/employee.png"; //$NON-NLS-1$

	// The shared instance
	private static CdtDsfStudyPluginActivator fgPlugin;

	private static BundleContext fgBundleContext;

	/**
	 * The constructor
	 */
	public CdtDsfStudyPluginActivator() {
		fgPlugin = this;
	}

	@Override
	public void start(BundleContext context) throws Exception {
		fgBundleContext = context;
		super.start(context);
		getImageRegistry().put(IMG_EMPLOYEE, ResourceLocator.imageDescriptorFromBundle(PLUGIN_ID, IMG_EMPLOYEE).get());
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		fgPlugin = null;
		fgBundleContext = null;
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static CdtDsfStudyPluginActivator getDefault() {
		return fgPlugin;
	}

	public static BundleContext getBundleContext() {
		return fgBundleContext;
	}

}
