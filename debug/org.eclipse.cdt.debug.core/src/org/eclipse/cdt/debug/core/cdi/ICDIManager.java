/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.cdi;

/**
 */
public interface ICDIManager extends ICDISessionObject {

	/**
	 * When the target is suspended the manager will check
	 * for any updates. The default behaviour (on/off) depend on the manager.
	 */
	void setAutoUpdate(boolean update);

	/**
	 * Returns true is the manager is set to autoupdate.
	 */
	boolean isAutoUpdate();

	/**
	 * Force the manager to update its state.
	 */
	void update() throws CDIException;

}
