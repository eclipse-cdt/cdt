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

import java.util.Map;

import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.framework.msg.MessageFormat;

/**
 * A breakpoint that suspend the execution when a particular address is reached.
 */
public class CAddressBreakpoint extends AbstractLineBreakpoint implements ICAddressBreakpoint {

	private static final String C_ADDRESS_BREAKPOINT = "org.eclipse.cdt.debug.core.cAddressBreakpointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CAddressBreakpoint.
	 */
	public CAddressBreakpoint() {
	}

	/**
	 * Constructor for CAddressBreakpoint.
	 */
	public CAddressBreakpoint( IResource resource, Map attributes, boolean add ) throws CoreException {
		super( resource, getMarkerType(), attributes, add );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType() {
		return C_ADDRESS_BREAKPOINT;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	protected String getMarkerMessage() throws CoreException {
		StringBuffer sb = new StringBuffer( BreakpointMessages.getString( "CAddressBreakpoint.1" ) ); //$NON-NLS-1$
		String name = ensureMarker().getResource().getName();
		if ( name != null && name.length() > 0 ) {
			sb.append( ' ' );
			sb.append( name );
		}
		try {
			sb.append( MessageFormat.format( BreakpointMessages.getString( "CAddressBreakpoint.2" ), new String[] { getAddress() } ) ); //$NON-NLS-1$
		}
		catch( NumberFormatException e ) {
		}
		sb.append( getConditionText() );
		return sb.toString();
	}
}