/*******************************************************************************
 * Copyright (c) 2008 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Jiju George T- bug 118100
 *******************************************************************************/
package org.eclipse.cdt.debug.core.model;

import org.eclipse.core.runtime.CoreException;

public interface ICBreakpointTyped {
	/**
	 * Breakpoint attribute storing the type this breakpoint
	 * is set in (value <code>"org.eclipse.cdt.debug.core.type"</code>). 
	 * This attribute is a <code>int</code>.
	 * Possible values are 
	 * 	<code>ICDIBreakpoint.REGULAR</code> 
	 *  <code>ICDIBreakpoint.HARDWARE</code>
	 *  <code>ICDIBreakpoint.TEMPORARY</code>
	 * 
	 * @since 5.0
	 */
	public static final String TYPE = "org.eclipse.cdt.debug.core.breakpointType"; //$NON-NLS-1$
	
	
	/**
	 * Returns the type of this breakpoint
	 * 
	 * @return type of breakpoint. Defaults to REGULAR if property does not exists in
	 * the underlying marker.
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 * 
	 * @since 5.0
	 */
	public int getType() throws CoreException;
	
	/**
	 * Sets the type of this breakpoint.
	 * 
	 * @param type breakpoint type
	 * @exception CoreException if unable to access the property on this breakpoint's
	 *  underlying marker
	 *  
	 *  @since 5.0
	 */
	public void setType( int type ) throws CoreException;
}
