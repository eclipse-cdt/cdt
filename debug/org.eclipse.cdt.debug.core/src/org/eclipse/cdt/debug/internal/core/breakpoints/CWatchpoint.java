/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.ICWatchpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 4, 2002
 */
public class CWatchpoint extends CBreakpoint implements ICWatchpoint
{
	private static final String C_WATCHPOINT = "org.eclipse.cdt.debug.core.cWatchpointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CWatchpoint.
	 */
	public CWatchpoint()
	{
	}

	/**
	 * Constructor for CWatchpoint.
	 * @param resource
	 * @param markerType
	 * @param attributes
	 * @param add
	 * @throws DebugException
	 */
	public CWatchpoint( IResource resource, Map attributes, boolean add ) throws DebugException
	{
		super( resource, getMarkerType(), attributes, add );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICWatchpoint#isWriteType()
	 */
	public boolean isWriteType() throws CoreException
	{
		return ensureMarker().getAttribute( WRITE, true );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICWatchpoint#isReadType()
	 */
	public boolean isReadType() throws CoreException
	{
		return ensureMarker().getAttribute( READ, false );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICWatchpoint#getExpression()
	 */
	public String getExpression() throws CoreException
	{
		return ensureMarker().getAttribute( EXPRESSION, "" );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType()
	{
		return C_WATCHPOINT;
	}
}
