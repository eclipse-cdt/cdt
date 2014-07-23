package org.eclipse.cdt.examples.dsf.gdb;

import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class GDBExamplePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ExtendCDTTest"; //$NON-NLS-1$

	// The shared instance
	private static GDBExamplePlugin plugin;
	
    private static BundleContext fgBundleContext;

	/**
	 * The constructor
	 */
	public GDBExamplePlugin() {
	}

	@Override
    public void start(BundleContext context) throws Exception {
        fgBundleContext = context;
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
        fgBundleContext = null;
	}


	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static GDBExamplePlugin getDefault() {
		return plugin;
	}

    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }

}
