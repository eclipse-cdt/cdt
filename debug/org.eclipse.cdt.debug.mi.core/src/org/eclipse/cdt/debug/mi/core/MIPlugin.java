package org.eclipse.cdt.debug.mi.core;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;

/**
 * @author alain
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MIPlugin extends Plugin {

	//The shared instance.
	private static MIPlugin plugin;

	/**
	* The constructor.
	*/
	public MIPlugin(IPluginDescriptor descriptor) {
		super(descriptor);
		plugin = this;
	}

	/**
	* Returns the shared instance.
	*/
	public static MIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Create a MI Session.
	 */
	public MISession createSession(Process proc) {
		return new MISession(proc);
	}
}
