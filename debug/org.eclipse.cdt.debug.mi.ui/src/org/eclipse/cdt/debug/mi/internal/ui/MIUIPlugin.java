package org.eclipse.cdt.debug.mi.internal.ui;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.plugin.AbstractUIPlugin;

/**
 * The main plugin class to be used in the desktop.
 */
public class MIUIPlugin extends AbstractUIPlugin {

	/**
	 * The plug-in identifier (value <code>"org.eclipse.cdt.debug.mi.ui"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.cdt.debug.mi.ui" ; //$NON-NLS-1$

	//The shared instance.
	private static MIUIPlugin plugin;

	/**
	 * The constructor.
	 */
	public MIUIPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static MIUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns the workspace instance.
	 */
	public static IWorkspace getWorkspace() {
		return ResourcesPlugin.getWorkspace();
	}

	/**
	 * Convenience method which returns the unique identifier of this plugin.
	 * 
	 * @return the unique identifier of this plugin
	 */
	public static String getUniqueIdentifier() {
		if ( getDefault() == null ) {
			// If the default instance is not yet initialized,
			// return a static identifier. This identifier must
			// match the plugin id defined in plugin.xml
			return PLUGIN_ID; //$NON-NLS-1$
		}
		return getDefault().getBundle().getSymbolicName();
	}
}
