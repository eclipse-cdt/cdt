/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 21, 2002
 */
public class CLineBreakpoint extends CBreakpoint implements ICLineBreakpoint
{
	private static final String C_LINE_BREAKPOINT = "org.eclipse.cdt.debug.core.cLineBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CLineBreakpoint.
	 */
	public CLineBreakpoint()
	{
	}

	/**
	 * Constructor for CLineBreakpoint.
	 */
	public CLineBreakpoint( IResource resource, Map attributes, boolean add ) throws DebugException
	{
		super( resource, getMarkerType(), attributes, add );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getLineNumber()
	 */
	public int getLineNumber() throws CoreException
	{
		return ensureMarker().getAttribute( IMarker.LINE_NUMBER, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharStart()
	 */
	public int getCharStart() throws CoreException
	{
		return ensureMarker().getAttribute( IMarker.CHAR_START, -1 );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILineBreakpoint#getCharEnd()
	 */
	public int getCharEnd() throws CoreException
	{
		return ensureMarker().getAttribute( IMarker.CHAR_END, -1 );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType()
	{
		return C_LINE_BREAKPOINT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException
	{
		StringBuffer sb = new StringBuffer( "Line breakpoint:" );
		String fileName = ensureMarker().getResource().getName();
		if ( fileName != null && fileName.length() > 0 )
		{
			sb.append( ' ' );
			sb.append( fileName );
		}
		int lineNumber = getLineNumber();
		if ( lineNumber > 0 )
		{
			sb.append( " [" );
			sb.append( "line:" );
			sb.append( ' ' );
			sb.append( lineNumber );
			sb.append( ']' );
		}
		sb.append( getConditionText() );
		return sb.toString();
	}
}
