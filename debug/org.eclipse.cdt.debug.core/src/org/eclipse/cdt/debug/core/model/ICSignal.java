/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;

/**
 * Enter type comment.
 * 
 * @since: Jan 31, 2003
 */
public interface ICSignal extends IDebugElement
{
	String getName();
	
	String getDescription();
	
	boolean isPassEnabled();
	
	boolean isStopEnabled();
	
	void setPassEnabled( boolean enable ) throws DebugException;
	
	void setStopEnabled( boolean enable ) throws DebugException;

	void signal() throws DebugException;
	
	void dispose();
}
