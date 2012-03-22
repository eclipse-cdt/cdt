/*******************************************************************************
 * Copyright (c) 2004, 2009 QNX Software Systems and others.
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
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * Base class for different types of location tracepoints.
 */
public abstract class AbstractTracepoint extends CBreakpoint implements ICTracepoint {

	/**
	 * Constructor for AbstractTracepoint.
	 */
	public AbstractTracepoint() {
		super();
	}

	/**
	 * Constructor for AbstractTracepoint.
	 *
	 * @param resource
	 * @param markerType
	 * @param attributes
	 * @param add
	 * @throws CoreException
	 */
	public AbstractTracepoint( IResource resource, Map<String, Object> attributes, boolean add ) throws CoreException {
		super( resource, attributes, add );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	@Override
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute( IMarker.LINE_NUMBER, -1 );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	@Override
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_START, -1 );
	}

	/*(non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	@Override
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_END, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getAddress()
	 */
	@Override
	public String getAddress() throws CoreException {
		return ensureMarker().getAttribute( ICLineBreakpoint.ADDRESS, "" ); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getFileName()
	 */
	@Override
	public String getFileName() throws CoreException {
		String fileName = ensureMarker().getAttribute( ICBreakpoint.SOURCE_HANDLE, "" ); //$NON-NLS-1$
		IPath path = new Path( fileName );
		return ( path.isValidPath( fileName ) ) ? path.lastSegment() : null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#getFunction()
	 */
	@Override
	public String getFunction() throws CoreException {
		return ensureMarker().getAttribute( ICLineBreakpoint.FUNCTION, "" ); //$NON-NLS-1$
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICBreakpoint#isConditional()
	 */
	@Override
	public boolean isConditional() throws CoreException {
		return (super.isConditional() || getPassCount() > 0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#setAddress(java.lang.String)
	 */
	@Override
	public void setAddress( String address ) throws CoreException {
		setAttribute( ICLineBreakpoint.ADDRESS, address );
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICLineBreakpoint#setFunction(java.lang.String)
	 */
	@Override
	public void setFunction( String function ) throws CoreException {
		setAttribute( ICLineBreakpoint.FUNCTION, function );
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICTracepoint#getPassCount()
	 */
	@Override
	public int getPassCount() throws CoreException {
		return ensureMarker().getAttribute( PASS_COUNT, 0 );
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICTracepoint#setPassCount(int)
	 */
	@Override
	public void setPassCount( int passCount ) throws CoreException {
		setAttribute( PASS_COUNT, passCount );
		setAttribute( IMarker.MESSAGE, getMarkerMessage() );
	}
}
