package org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany;

//import org.eclipse.cdt.dsf.gdb.multicorevisualizer.internal.ui.MulticoreVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.plugin.CDTVisualizerUIPlugin;
import org.eclipse.cdt.visualizer.ui.util.UIResourceManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EpiphanyVisualizerUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "org.eclipse.cdt.dsf.gdb.multicorevisualizer.epiphany.ui"; //$NON-NLS-1$

	/** Singleton instance */
	private static EpiphanyVisualizerUIPlugin plugin;
	
	/** Bundle context */
	private static BundleContext fgBundleContext;
	
	/** Resource manager */
	protected static UIResourceManager s_resources = null;
	
	/**
	 * Returns the shared instance
	 */
	public static EpiphanyVisualizerUIPlugin getDefault() {
		return plugin;
	}
	
	/**
	 * Returns the bundle context for this plugin.
	 */
    public static BundleContext getBundleContext() {
        return fgBundleContext;
    }
	
	/**
	 * The constructor
	 */
	public EpiphanyVisualizerUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	public void start(BundleContext context) throws Exception {
		fgBundleContext = context;
		super.start(context);
		plugin = this;
		
		// initialize resource management (strings, images, fonts, colors, etc.)
		getPluginResources();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
		// clean up resource management
		cleanupPluginResources();
		
		plugin = null;
		super.stop(context);
		fgBundleContext = null;
	}
	
	
	// --- resource management ---
	
	/** Returns resource manager for this plugin */
	public UIResourceManager getPluginResources() {
		if (s_resources == null) {
			// FindBugs reported that it is unsafe to set s_resources
			// before we finish to initialize the object, because of
			// multi-threading.  This is why we use a temporary variable.
			UIResourceManager resourceManager = new UIResourceManager(this);
			resourceManager.setParentManager(CDTVisualizerUIPlugin.getResources());
			s_resources = resourceManager;
		}
		
		return s_resources;
	}
	
	/** Releases resource manager for this plugin. */
	public void cleanupPluginResources() {
		if (s_resources != null) s_resources.dispose();
	}

	public static UIResourceManager getResources() {
		return getDefault().getPluginResources();
	}
	/** Convenience method for looking up string resources */
	public static String getString(String key) {
		return getDefault().getPluginResources().getString(key);
	}
	/** Convenience method for looking up string resources */
	public static String getString(String key, Object... arguments) {
		return getDefault().getPluginResources().getString(key, arguments);
	}
	
	/** Convenience method for looking up image resources */
	public static Image getImage(String key) {
		return getDefault().getPluginResources().getImage(key);
	}
	/** Convenience method for looking up image resources */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getDefault().getPluginResources().getImageDescriptor(key);
	}
	
	/** Convenience method for looking up font resources */
	public static Font getFont(String fontName, int height) {
		return getDefault().getPluginResources().getFont(fontName, height);
	}
	/** Convenience method for looking up font resources */
	public static Font getFont(String fontName, int height, int style) {
		return getDefault().getPluginResources().getFont(fontName, height, style);
	}

}
