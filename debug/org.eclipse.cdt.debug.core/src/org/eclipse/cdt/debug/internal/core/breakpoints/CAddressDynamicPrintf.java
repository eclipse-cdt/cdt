/*******************************************************************************
 * Copyright (c) 2014 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Marc Khouzam (Ericsson) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.core.breakpoints;

import java.util.Map;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.ICAddressBreakpoint;
import org.eclipse.cdt.debug.core.model.ICDynamicPrintf;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;

import com.ibm.icu.text.MessageFormat;

/**
 * A DynamicPrintf that prints a message when a particular address is reached.
 *
 * @since 7.5
 */
public class CAddressDynamicPrintf extends AbstractDynamicPrintf implements ICAddressBreakpoint, ICDynamicPrintf {

	public CAddressDynamicPrintf() {
	}

	public CAddressDynamicPrintf( IResource resource, Map<String, Object> attributes, boolean add ) throws CoreException {
		super( resource, attributes, add );
	}

	/**
	 * Returns the type of marker associated with this type of breakpoints
	 */
	public String getMarkerType() {
		return C_ADDRESS_DYNAMICPRINTF_MARKER;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.debug.internal.core.breakpoints.CBreakpoint#getMarkerMessage()
	 */
	@Override
	protected String getMarkerMessage() throws CoreException {
		return MessageFormat.format( BreakpointMessages.getString( "CAddressDynamicPrintf.0" ), (Object[])new String[] { CDebugUtils.getBreakpointText( this, false ) } ); //$NON-NLS-1$
	}
}
