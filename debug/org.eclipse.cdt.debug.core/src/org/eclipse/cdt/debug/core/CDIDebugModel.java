/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;

/**
 * Provides utility methods for creating debug sessions, targets and 
 * breakpoints specific to the CDI debug model.
 *
 * @since: Feb 23, 2004
 */
public class CDIDebugModel {
	/**
	 * Returns the identifier for the CDI debug model plug-in
	 *
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier() {
		return CDebugCorePlugin.getUniqueIdentifier();
	}	
}
