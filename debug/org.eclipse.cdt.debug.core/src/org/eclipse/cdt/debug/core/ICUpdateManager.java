/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since: Feb 10, 2003
 */
public interface ICUpdateManager
{
	void setAutoModeEnabled( boolean enable );

	boolean getAutoModeEnabled();

	void update() throws DebugException;
	
	boolean canUpdate();
}
