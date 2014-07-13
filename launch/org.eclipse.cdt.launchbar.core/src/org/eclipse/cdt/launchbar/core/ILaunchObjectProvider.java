package org.eclipse.cdt.launchbar.core;

/**
 * An extension that serves up objects to feed launch descriptors.
 * 
 */
public interface ILaunchObjectProvider {

	/**
	 * Add initial launch descriptors and set up listeners.
	 * 
	 * @param launchbar manager
	 */
	void init(ILaunchBarManager manager);

	/**
	 * Shutting down, remove any listeners.
	 */
	void dispose();
	
}
