/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 20, 2002
 */
public interface ISwitchToFrame
{
	void switchToFrame( IStackFrame frame ) throws DebugException;
}
