/*******************************************************************************
 * Copyright (c) 2000, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.core.sourcelookup;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IStackFrame;

/**
 * 
 * A C/C++ extension of <code>ISourceLocator</code>. 
 * Provides constants and methods to manage different source modes.
 * 
 * @since Aug 19, 2002
 */
public interface ICSourceLocator extends ISourceLocator
{
	/**
	 * Returns the project this source locator is associated with or <code>null</code>.
	 * 
	 * @return project this source locator is associated with or <code>null</code>
	 */
	IProject getProject();

	/**
	 * Returns the line number of the instruction pointer in the specified 
	 * stack frame that corresponds to a line in an associated source element, 
	 * or -1 if line number information is unavailable.
	 * 
	 * @param frameInfo the frame data
	 * @return line number of instruction pointer in this stack frame, 
	 * 		   or -1 if line number information is unavailable
	 */
	int getLineNumber( IStackFrame stackFrame );

	/**
	 * Returns the source locations of this locator.
	 * 
	 * @return the source locations of this locator
	 */
	ICSourceLocation[] getSourceLocations();
	
	/**
	 * Sets the source locations of this locator.
	 * 
	 * @param location - an array of source locations
	 */
	void setSourceLocations( ICSourceLocation[] locations );

	/**
	 * Returns whether this locator is able to locate the given resource.
	 * 
	 * @param resource the resource to locate
	 * @return whether this locator is able to locate the given resource
	 */
	boolean contains( IResource resource );
	
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
	 */
	Object findSourceElement( String fileName );

	/**
	 * Returns whether to search for all source elements, or just the first match.
	 *  
	 * @return whether to search for all source elements, or just the first match
	 */
	boolean searchForDuplicateFiles();

	/**
	 * Sets the value of the 'search for duplicate source files' flag.
	 * 
	 * @param search - a value to set
	 */
	void setSearchForDuplicateFiles( boolean search );
}
