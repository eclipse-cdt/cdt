/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Enter type comment.
 * 
 * @since: Jan 7, 2003
 */
public interface ICBreakpointManager extends IAdaptable
{
	long getBreakpointAddress( IBreakpoint breakpoint );
}
