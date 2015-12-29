/*******************************************************************************
 * Copyright (c) 2004, 2012 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.mi.internal.ui; 

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.ui.IActionFilter;
 
/**
 * The UI adapter factory for GDB/MI Debug extensions
 */
public class ActionFilterAdapterFactory implements IAdapterFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter( Object adaptableObject, Class adapterType ) {
		if ( adapterType.isInstance( adaptableObject ) ) {
			return adaptableObject;
		}
		if ( adapterType == IActionFilter.class ) {
			if ( adaptableObject instanceof ICDebugTarget ) {
				return new GDBTargetActionFilter();
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapterList()
	 */
	@Override
	public Class[] getAdapterList() {
		return new Class[] {
				IActionFilter.class 
			};
	}
}
