package org.eclipse.cdt.launchbar.core;

import org.eclipse.core.runtime.CoreException;

/**
 * An extension that serves up objects to feed launch descriptors.
 * 
 */
public interface ILaunchObjectProvider {

	/**
	 * Add initial launch descriptors and set up for new ones.
	 * 
	 * @param launchbar manager
	 * @throws CoreException 
	 */
	void init(ILaunchBarManager manager) throws CoreException;

	/**
	 * Shutting down, remove any listeners.
	 */
	void dispose();
	
}
