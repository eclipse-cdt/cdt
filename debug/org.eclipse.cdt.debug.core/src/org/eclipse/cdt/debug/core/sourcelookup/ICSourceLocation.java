/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;

/**
 * 
 * A source location defines the location of a repository
 * of source code. A source location is capable of retrieving
 * source elements.
 * <p>
 * For example, a source location could be a project, zip/archive
 * file, or a directory in the file system.
 * </p>
 * 
 * @since Sep 23, 2002
 */
public interface ICSourceLocation extends IAdaptable
{
	/**
	 * Returns an object representing the source code
	 * for a type with the specified name, or <code>null</code>
	 * if none could be found. The source element 
	 * returned is implementation specific - for example, a
	 * resource, a local file, a zip file entry, etc.
	 * 
	 * @param name the name of the object for which source is being searched for
	 * 
	 * @return source element
	 * @exception CoreException if an exception occurs while searching for the specified source element
	 */
	Object findSourceElement( String name ) throws CoreException;
	
	/**
	 * Returns the paths associated with this location.
	 * 
	 * @return the paths associated with this location
	 */
	IPath[] getPaths(); 
}
