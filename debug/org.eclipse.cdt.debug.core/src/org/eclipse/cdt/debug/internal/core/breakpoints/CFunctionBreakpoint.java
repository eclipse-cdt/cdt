/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * A breakpoint that suspends the execution when a function is entered.
 */
public class CFunctionBreakpoint extends CBreakpoint implements ICFunctionBreakpoint {

	private static final String C_FUNCTION_BREAKPOINT = "org.eclipse.cdt.debug.core.cFunctionBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Breakpoint attribute storing the function this breakpoint suspends
	 * execution in (value <code>"org.eclipse.cdt.debug.core.function"</code>).
	 * This attribute is a <code>String</code>.
	 */
	protected static final String FUNCTION = "org.eclipse.cdt.debug.core.function"; //$NON-NLS-1$	

	/**
	 * Constructor for CFunctionBreakpoint.
	 */
	public CFunctionBreakpoint() {
	}

	/**
	 * Constructor for CFunctionBreakpoint.
	 */
	public CFunctionBreakpoint( IResource resource, Map attributes, boolean add ) throws CoreException {
		super( resource, getMarkerType(), attributes, add );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICFunctionBreakpoint#getFunction()
	 */
	public String getFunction() throws CoreException {
		return ensureMarker().getAttribute( FUNCTION, null );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.ICFunctionBreakpoint#setFunction(String)
	 */
	public void setFunction( String function ) throws CoreException {
		setAttribute( FUNCTION, function );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute( IMarker.LINE_NUMBER, -1 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_START, -1 );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_END, -1 );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType() {
		return C_FUNCTION_BREAKPOINT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint#getFileName()
	 */
	public String getFileName() throws CoreException {
		IResource resource = ensureMarker().getResource();
		if ( resource instanceof IFile ) {
			return ((IFile)resource).getLocation().lastSegment();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException {
		StringBuffer sb = new StringBuffer( BreakpointMessages.getString( "CFunctionBreakpoint.2" ) ); //$NON-NLS-1$
		String name = ensureMarker().getResource().getName();
		if ( name != null && name.length() > 0 ) {
			sb.append( ' ' );
			sb.append( name );
		}
		String function = getFunction();
		if ( function != null && function.trim().length() > 0 ) {
			sb.append( MessageFormat.format( BreakpointMessages.getString( "CFunctionBreakpoint.3" ), new String[] { function.trim() } ) ); //$NON-NLS-1$
		}
		sb.append( getConditionText() );
		return sb.toString();
	}
}