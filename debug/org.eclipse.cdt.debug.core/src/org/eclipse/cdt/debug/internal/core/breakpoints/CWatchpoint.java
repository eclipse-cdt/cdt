/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICWatchpoint;
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

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException
	{
		StringBuffer sb = new StringBuffer();
		if ( isWriteType() && !isReadType() )
			sb.append( "Write " );
		else if ( !isWriteType() && isReadType() )
			sb.append( "Read " );
		else if ( isWriteType() && isReadType() )
			sb.append( "Access " );
		sb.append( "watchpoint: " );
		String fileName = ensureMarker().getResource().getName();
		if ( fileName != null && fileName.length() > 0 )
		{
			sb.append( ' ' );
			sb.append( fileName );
		}
		String expression = getExpression();
		if ( expression != null && expression.length() > 0 )
		{
			sb.append( " at \'" ); 
			sb.append( expression );
			sb.append( '\'' );
		}
		sb.append( getConditionText() );
		return sb.toString();
	}
}
