/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core;



/**
 * 
 * Provides utility methods for creating debug sessions, targets and 
 * breakpoints specific to the CDI debug model.
 * 
 * @since Aug 1, 2002
 */
public class CDebugModel
{
	/**
	 * Constructor for CDebugModel.
	 */
	public CDebugModel()
	{
		super();
	}

	/**
	 * Returns the identifier for the CDI debug model plug-in
	 *
	 * @return plugin identifier
	 */
	public static String getPluginIdentifier()
	{
		return CDebugCorePlugin.getUniqueIdentifier();
	}
}
