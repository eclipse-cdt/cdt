/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since Mar 7, 2003
 */
public interface ICastToType extends IAdaptable
{
	boolean supportsCasting();
	
	String getCurrentType();
	
	void cast( String type ) throws DebugException;
	
	void restoreDefault() throws DebugException;
	
	boolean isCasted();
}
