package org.eclipse.cdt.debug.mi.core;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IPluginDescriptor;
import org.eclipse.core.runtime.Plugin;

/**
 *
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
	 * Returns the singleton.
	 */
	public static MIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Create a MI Session.
	 */
	public MISession createMISession(InputStream in, OutputStream out) {
		return new MISession(in, out);
	}
}
