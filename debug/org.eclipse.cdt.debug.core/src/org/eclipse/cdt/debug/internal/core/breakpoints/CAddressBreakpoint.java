/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 21, 2002
 */
public class CAddressBreakpoint extends CBreakpoint	implements ICAddressBreakpoint
{
	private static final String C_ADDRESS_BREAKPOINT = "org.eclipse.cdt.debug.core.cAddressBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CAddressBreakpoint.
	 */
	public CAddressBreakpoint()
	{
	}

	/**
	 * Constructor for CAddressBreakpoint.
	 */
	public CAddressBreakpoint( IResource resource, Map attributes, boolean add ) throws DebugException
	{
		super( resource, getMarkerType(), attributes, add );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICAddressBreakpoint#getAddress()
	 */
	public String getAddress() throws CoreException
	{
		return ensureMarker().getAttribute( ADDRESS, null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.ICAddressBreakpoint#setAddress(long)
	 */
	public void setAddress( String address ) throws CoreException
	{
		setAttribute( ADDRESS, address );
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
		return C_ADDRESS_BREAKPOINT;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException
	{
		StringBuffer sb = new StringBuffer( CDebugCorePlugin.getResourceString("internal.core.breakpoints.CAddressBreakpoint.Address_breakpoint") ); //$NON-NLS-1$
		String name = ensureMarker().getResource().getName();
		if ( name != null && name.length() > 0 )
		{
			sb.append( ' ' );
			sb.append( name );
		}
		try
		{
			long address = Long.parseLong( getAddress() );
			sb.append( CDebugCorePlugin.getResourceString("internal.core.breakpoints.CAddressBreakpoint.address") ); //$NON-NLS-1$
			sb.append( CDebugUtils.toHexAddressString( address ) );
			sb.append( ']' );
		}
		catch( NumberFormatException e )
		{
		}
		sb.append( getConditionText() );
		return sb.toString();
	}
}
