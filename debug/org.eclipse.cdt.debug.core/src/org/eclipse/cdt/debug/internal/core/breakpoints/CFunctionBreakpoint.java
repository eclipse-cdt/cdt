/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint;
import org.eclipse.core.resources.IFile;
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
public class CFunctionBreakpoint extends CBreakpoint implements ICFunctionBreakpoint
{
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
	public CFunctionBreakpoint()
	{
	}

	/**
	 * Constructor for CFunctionBreakpoint.
	 */
	public CFunctionBreakpoint( IResource resource, Map attributes, boolean add ) throws DebugException
	{
		super( resource, getMarkerType(), attributes, add );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICFunctionBreakpoint#getFunction()
	 */
	public String getFunction() throws CoreException
	{
		return ensureMarker().getAttribute( FUNCTION, null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICFunctionBreakpoint#setFunction(String)
	 */
	public void setFunction( String function ) throws CoreException
	{
		setAttribute( FUNCTION, function );
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
		return C_FUNCTION_BREAKPOINT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.model.ICFunctionBreakpoint#getFileName()
	 */
	public String getFileName() throws CoreException
	{
		IResource resource = ensureMarker().getResource();
		if ( resource instanceof IFile )
		{
			return ((IFile)resource).getLocation().lastSegment();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException
	{
		StringBuffer sb = new StringBuffer( "Function breakpoint:" );
		String name = ensureMarker().getResource().getName();
		if ( name != null && name.length() > 0 )
		{
			sb.append( ' ' );
			sb.append( name );
		}
		String function = getFunction();
		if ( function != null && function.trim().length() > 0 )
		{
			sb.append( " [" );
			sb.append( "function:" );
			sb.append( ' ' );
			sb.append( function.trim() );
			sb.append( ']' );
		}
		sb.append( getConditionText() );
		return sb.toString();
	}
}
