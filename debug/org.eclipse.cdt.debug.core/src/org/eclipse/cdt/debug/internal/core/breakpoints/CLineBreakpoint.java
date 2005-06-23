/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * A breakpoint that suspends the execution when a particular line of code is
 * reached.
 */
public class CLineBreakpoint extends AbstractLineBreakpoint {

	private static final String C_LINE_BREAKPOINT = "org.eclipse.cdt.debug.core.cLineBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CLineBreakpoint.
	 */
	public CLineBreakpoint() {
	}

	/**
	 * Constructor for CLineBreakpoint.
	 */
	public CLineBreakpoint( IResource resource, Map attributes, boolean add ) throws CoreException {
		super( resource, getMarkerType(), attributes, add );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType() {
		return C_LINE_BREAKPOINT;
	}

	/*(non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException {
		StringBuffer sb = new StringBuffer( BreakpointMessages.getString( "CLineBreakpoint.1" ) ); //$NON-NLS-1$
		String fileName = ensureMarker().getResource().getName();
		if ( fileName != null && fileName.length() > 0 ) {
			sb.append( ' ' );
			sb.append( fileName );
		}
		int lineNumber = getLineNumber();
		if ( lineNumber > 0 ) {
			sb.append( MessageFormat.format( BreakpointMessages.getString( "CLineBreakpoint.2" ), new Integer[] { new Integer( lineNumber ) } ) ); //$NON-NLS-1$
		}
		sb.append( getConditionText() );
		return sb.toString();
	}
}
