/*******************************************************************************
 * Copyright (c) 2004, 2009-7 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Freescale Semiconductor - Address watchpoints, https://bugs.eclipse.org/bugs/show_bug.cgi?id=118299
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.math.BigInteger;
import com.ibm.icu.text.MessageFormat;
import java.util.Map;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICWatchpoint2;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

/**
 * A watchpoint specific to the C/C++ debug model.
 */
public class CWatchpoint extends CBreakpoint implements ICWatchpoint2 {

	/**
	 * Constructor for CWatchpoint.
	 */
	public CWatchpoint() {
	}

	/**
	 * Constructor for CWatchpoint.
	 */
	public CWatchpoint( IResource resource, Map<String, Object> attributes, boolean add ) throws CoreException {
		super( resource, attributes, add );
	}

	@Override
	public String getMarkerType() {
	    return C_WATCHPOINT_MARKER;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICWatchpoint#isWriteType()
	 */
	@Override
	public boolean isWriteType() throws CoreException {
		return ensureMarker().getAttribute( WRITE, true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICWatchpoint#isReadType()
	 */
	@Override
	public boolean isReadType() throws CoreException {
		return ensureMarker().getAttribute( READ, false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICWatchpoint#getExpression()
	 */
	@Override
	public String getExpression() throws CoreException {
		return ensureMarker().getAttribute( EXPRESSION, "" ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	@Override
	protected String getMarkerMessage() throws CoreException {
		String format = BreakpointMessages.getString( "CWatchpoint.3" ); //$NON-NLS-1$
		if ( isWriteType() && !isReadType() )
			format = BreakpointMessages.getString( "CWatchpoint.0" ); //$NON-NLS-1$
		else if ( !isWriteType() && isReadType() )
			format = BreakpointMessages.getString( "CWatchpoint.1" ); //$NON-NLS-1$
		else if ( isWriteType() && isReadType() )
			format = BreakpointMessages.getString( "CWatchpoint.2" ); //$NON-NLS-1$
		return MessageFormat.format( format, new Object[] { CDebugUtils.getBreakpointText( this, false ) } );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	@Override
	public int getLineNumber() throws CoreException {
		return ensureMarker().getAttribute( IMarker.LINE_NUMBER, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	@Override
	public int getCharStart() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_START, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	@Override
	public int getCharEnd() throws CoreException {
		return ensureMarker().getAttribute( IMarker.CHAR_END, -1 );
	}

	@Override
	public String getMemorySpace() throws CoreException {
		return ensureMarker().getAttribute( MEMORYSPACE, "" ); //$NON-NLS-1$
	}

	@Override
	public BigInteger getRange() throws CoreException {
		String attr = ensureMarker().getAttribute( RANGE, "0" ); //$NON-NLS-1$
		return new BigInteger( attr.length() > 0 ? attr : "0" ); //$NON-NLS-1$
	}

}
