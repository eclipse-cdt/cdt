package org.eclipse.rse.examples.dstore;

import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.rse.examples.dstore.subsystems.RemoteSampleObject;
import org.eclipse.rse.examples.dstore.ui.RemoteSampleObjectAdapterFactory;
import org.eclipse.rse.examples.dstore.ui.SampleSubSystemConfigurationAdapterFactory;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends Plugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.rse.examples.dstore";

	// The shared instance
	private static Activator plugin;
	
	/**
	 * The constructor
	 */
	public Activator() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugins#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		
		IAdapterManager manager = Platform.getAdapterManager();
		RemoteSampleObjectAdapterFactory factory = new RemoteSampleObjectAdapterFactory();
		manager.registerAdapters(factory, RemoteSampleObject.class);
	
	    SampleSubSystemConfigurationAdapterFactory sscaf = new SampleSubSystemConfigurationAdapterFactory();
	    sscaf.registerWithManager(manager);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static Activator getDefault() {
		return plugin;
	}

}
