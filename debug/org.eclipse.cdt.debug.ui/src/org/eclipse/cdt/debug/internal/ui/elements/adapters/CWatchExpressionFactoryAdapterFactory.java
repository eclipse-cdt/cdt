/*******************************************************************************
 * Copyright (c) 2007 ARM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.elements.adapters; 

import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter;
 
public class CWatchExpressionFactoryAdapterFactory implements IAdapterFactory {

    private static IWatchExpressionFactoryAdapter fgWatchExpressionFactoryAdapter = new CWatchExpressionFactoryAdapter();

    /* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
	 */
	@Override
	public Object getAdapter( Object adaptableObject, Class adapterType ) {
        if ( adapterType.equals( IWatchExpressionFactoryAdapter.class ) ) {
			if ( adaptableObject instanceof ICVariable ) {
				return fgWatchExpressionFactoryAdapter;
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
        		IWatchExpressionFactoryAdapter.class,
			};
	}
}
