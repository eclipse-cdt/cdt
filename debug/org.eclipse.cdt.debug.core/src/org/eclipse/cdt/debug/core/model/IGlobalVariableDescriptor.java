/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.IPath;

/**
 * Enter type comment.
 * 
 * @since: Nov 4, 2002
 */
public interface IGlobalVariableDescriptor
{
	String getName();
	IPath getPath();
}
