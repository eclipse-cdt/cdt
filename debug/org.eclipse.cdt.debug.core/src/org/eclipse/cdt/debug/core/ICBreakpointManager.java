/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.core.runtime.IAdaptable;

/**
 * Enter type comment.
 * 
 * @since: Jan 7, 2003
 */
public interface ICBreakpointManager extends IAdaptable
{
	long getBreakpointAddress( ICBreakpoint breakpoint );
}
