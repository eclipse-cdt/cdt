/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.mi.core.command;

import org.eclipse.cdt.debug.mi.core.MIException;

/**
 * 
 * Allows clients to communicate with the debug engine by posting 
 * MI requests.
 * 
 * @author Mikhail Khodjaiants
 * @since Jul 11, 2002
 */
public interface MIRequestManager
{
	/**
	 * Posts request to the debug engine.
	 * 
	 * @param request - the request to post
	 * @throws MIException if this method fails.  Reasons include:
	 */
	void postRequest (Command request) throws MIException;
}
