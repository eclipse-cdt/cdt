package org.eclipse.dd.dsf.debug.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class DsfDebugUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.dd.dsf.debug.ui"; //$NON-NLS-1$

	
	// The shared instance
	private static DsfDebugUIPlugin plugin;
    
    // BundleContext of this plugin
    private static BundleContext fgBundleContext; 
	
	/**
	 * The constructor
	 */
	public DsfDebugUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
    public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
        fgBundleContext = context;

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
    public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static DsfDebugUIPlugin getDefault() {
		return plugin;
	}

    /**
     * Returns the bundle context
     *
     * @return the bundle context
     */
    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }
    
	/**
	 * Returns an image descriptor for the image file at the given
	 * plug-in relative path
	 *
	 * @param path the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
}
