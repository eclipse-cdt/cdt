/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.core.model;

import org.eclipse.debug.core.DebugException;

/**
 * Enter type comment.
 * 
 * @since Mar 10, 2003
 */
public interface ICastToArray extends ICastToType
{
	boolean supportsCastToArray();

	void castToArray( String type, int startIndex, int endIndex ) throws DebugException;
}
