/*******************************************************************************
 * Copyright (c) 2009 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Ericsson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICTracepoint;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.icu.text.MessageFormat;

/**
 * A tracepoint that collects data when a particular address is reached.
 *
 * @since 6.1
 */
public class CAddressTracepoint extends AbstractTracepoint implements ICAddressBreakpoint, ICTracepoint {

	private static final String C_ADDRESS_TRACEPOINT_MARKER = "org.eclipse.cdt.debug.core.cAddressTracepointMarker"; //$NON-NLS-1$

	/**
	 * Constructor for CAddressTracepoint.
	 */
	public CAddressTracepoint() {
	}

	/**
	 * Constructor for CAddressTracepoint.
	 */
	public CAddressTracepoint( IResource resource, Map<String, Object> attributes, boolean add ) throws CoreException {
		super( resource, getMarkerType(), attributes, add );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public static String getMarkerType() {
		return C_ADDRESS_TRACEPOINT_MARKER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	@Override
	protected String getMarkerMessage() throws CoreException {
		return MessageFormat.format( BreakpointMessages.getString( "CAddressTracepoint.0" ), (Object[])new String[] { CDebugUtils.getBreakpointText( this, false ) } ); //$NON-NLS-1$
	}
}
