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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension;
 
public class CWatchExpressionFactoryAdapter implements IWatchExpressionFactoryAdapterExtension {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapterExtension#canCreateWatchExpression(org.eclipse.debug.core.model.IVariable)
	 */
	@Override
	public boolean canCreateWatchExpression( IVariable variable ) {
		return ( variable instanceof ICVariable );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IWatchExpressionFactoryAdapter#createWatchExpression(org.eclipse.debug.core.model.IVariable)
	 */
	@Override
	public String createWatchExpression( IVariable variable ) throws CoreException {
		return ( variable instanceof ICVariable ) ? ((ICVariable)variable).getExpressionString() : null; 
	}
}
