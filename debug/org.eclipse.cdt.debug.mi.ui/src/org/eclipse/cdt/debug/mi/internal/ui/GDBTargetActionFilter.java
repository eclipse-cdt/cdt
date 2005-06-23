/*******************************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui; 

import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.mi.core.cdi.Session;
import org.eclipse.ui.IActionFilter;
 
/**
 * Comment for .
 */
public class GDBTargetActionFilter implements IActionFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionFilter#testAttribute(java.lang.Object, java.lang.String, java.lang.String)
	 */
	public boolean testAttribute( Object target, String name, String value ) {
		if ( target instanceof ICDebugTarget ) {
			if ( name.equals( "GDBTargetActionFilter" ) && value.equals( "isGDBTarget" ) ) {  //$NON-NLS-1$//$NON-NLS-2$
				return ( ((ICDebugTarget)target).getAdapter( ICDISession.class ) instanceof Session );
			}
		}
		return false;
	}
}
