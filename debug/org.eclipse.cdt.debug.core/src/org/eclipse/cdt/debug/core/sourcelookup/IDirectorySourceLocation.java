/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.core.runtime.IPath;

/**
 * 
 * Enter type comment.
 * 
 * @since Dec 24, 2002
 */
public interface IDirectorySourceLocation extends ICSourceLocation
{
	IPath getDirectory();
	IPath getAssociation();
}
