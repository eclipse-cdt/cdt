/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Base class for different types of location breakponts.
 */
public abstract class AbstractLineBreakpoint extends CBreakpoint implements ICLineBreakpoint {

	/**
	 * Constructor for AbstractLineBreakpoint.
	 */
	public AbstractLineBreakpoint() {
		super();
	}

	/**
	 * Constructor for AbstractLineBreakpoint.
	 *
	 * @param resource
	 * @param markerType
	 * @param attributes
	 * @param add
	 * @throws CoreException
	 */
	public AbstractLineBreakpoint( IResource resource, String markerType, Map attributes, boolean add ) throws CoreException {
		super( resource, markerType, attributes, add );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute( IMarker.LINE_NUMBER, -1 );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_START, -1 );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_END, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getAddress()
	 */
	public String getAddress() throws CoreException {
		return ensureMarker().getAttribute( ICLineBreakpoint.ADDRESS, "" ); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getFileName()
	 */
	public String getFileName() throws CoreException {
		String fileName = ensureMarker().getAttribute( ICBreakpoint.SOURCE_HANDLE, "" ); //$NON-NLS-1$
		IPath path = new Path( fileName );
		return ( path.isValidPath( fileName ) ) ? path.lastSegment() : null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getFunction()
	 */
	public String getFunction() throws CoreException {
		return ensureMarker().getAttribute( ICLineBreakpoint.FUNCTION, "" ); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#setAddress(java.lang.String)
	 */
	public void setAddress( String address ) throws CoreException {
		setAttribute( ICLineBreakpoint.ADDRESS, address );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#setFunction(java.lang.String)
	 */
	public void setFunction( String function ) throws CoreException {
		setAttribute( ICLineBreakpoint.FUNCTION, function );
	}
}
