/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.core.breakpoints;

import org.eclipse.cdt.debug.core.ICAddressBreakpoint;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * 
 * Enter type comment.
 * 
 * @since Aug 21, 2002
 */
public class CAddressBreakpoint extends CBreakpoint	implements ICAddressBreakpoint
{
	/**
	 * Breakpoint attribute storing the address this breakpoint suspends 
	 * execution at (value <code>"org.eclipse.cdt.debug.core.address"</code>).
	 * This attribute is a <code>String</code>.
	 */
	protected static final String ADDRESS = "org.eclipse.cdt.debug.core.address"; //$NON-NLS-1$	

	/**
	 * Constructor for CAddressBreakpoint.
	 */
	public CAddressBreakpoint()
	{
		super();
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
}
