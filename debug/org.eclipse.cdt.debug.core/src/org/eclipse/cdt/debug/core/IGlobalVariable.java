/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core;

import org.eclipse.core.runtime.IPath;

/**
 * Enter type comment.
 * 
 * @since: Nov 4, 2002
 */
public interface IGlobalVariable
{
	String getName();
	IPath getPath();
}
