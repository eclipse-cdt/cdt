package org.eclipse.dd.debug.memory.renderings.traditional;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class TraditionalRenderingPlugin extends AbstractUIPlugin 
{

	private static TraditionalRenderingPlugin plugin;
	
	public TraditionalRenderingPlugin() 
	{
		super();
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static TraditionalRenderingPlugin getDefault() {
		return plugin;
	}
}
