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
import org.eclipse.cdt.debug.core.model.ICWatchpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * A watchpoint specific to the C/C++ debug model.
 */
public class CWatchpoint extends CBreakpoint implements ICWatchpoint {

	private static final String C_WATCHPOINT = "org.eclipse.cdt.debug.core.cWatchpointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CWatchpoint.
	 */
	public CWatchpoint() {
	}

	/**
	 * Constructor for CWatchpoint.
	 */
	public CWatchpoint( IResource resource, Map attributes, boolean add ) throws CoreException {
		super( resource, getMarkerType(), attributes, add );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICWatchpoint#isWriteType()
	 */
	public boolean isWriteType() throws CoreException {
		return ensureMarker().getAttribute( WRITE, true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICWatchpoint#isReadType()
	 */
	public boolean isReadType() throws CoreException {
		return ensureMarker().getAttribute( READ, false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICWatchpoint#getExpression()
	 */
	public String getExpression() throws CoreException {
		return ensureMarker().getAttribute( EXPRESSION, "" ); //$NON-NLS-1$
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType() {
		return C_WATCHPOINT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException {
		StringBuffer sb = new StringBuffer();
		if ( isWriteType() && !isReadType() )
			sb.append( BreakpointMessages.getString( "CWatchpoint.1" ) ); //$NON-NLS-1$
		else if ( !isWriteType() && isReadType() )
			sb.append( BreakpointMessages.getString( "CWatchpoint.2" ) ); //$NON-NLS-1$
		else if ( isWriteType() && isReadType() )
			sb.append( BreakpointMessages.getString( "CWatchpoint.3" ) ); //$NON-NLS-1$
		else
			sb.append( BreakpointMessages.getString( "CWatchpoint.4" ) ); //$NON-NLS-1$
		sb.append( ' ' );
		String fileName = ensureMarker().getResource().getName();
		if ( fileName != null && fileName.length() > 0 ) {
			sb.append( ' ' );
			sb.append( fileName );
		}
		String expression = getExpression();
		if ( expression != null && expression.length() > 0 ) {
			sb.append( MessageFormat.format( BreakpointMessages.getString( "CWatchpoint.5" ), new String[]{ expression } ) ); //$NON-NLS-1$
		}
		sb.append( getConditionText() );
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute( IMarker.LINE_NUMBER, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_START, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_END, -1 );
	}
}