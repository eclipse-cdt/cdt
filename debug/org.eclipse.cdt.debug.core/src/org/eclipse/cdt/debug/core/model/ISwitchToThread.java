/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IThread;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 20, 2002
 */
public interface ISwitchToThread
{
	IThread getCurrentThread() throws DebugException;	
	void setCurrentThread( IThread thread ) throws DebugException;	
}
