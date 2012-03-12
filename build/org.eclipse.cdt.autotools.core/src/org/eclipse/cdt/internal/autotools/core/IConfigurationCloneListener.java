package org.eclipse.cdt.internal.autotools.core;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;

public interface IConfigurationCloneListener {
	/**
	 * Notified when a configuration gets cloned.
	 * @param cloneName - name of the cloned configuration
	 * @param c - the clone
	 */
	public void cloneCfg(String cloneName, IConfiguration c);
}
