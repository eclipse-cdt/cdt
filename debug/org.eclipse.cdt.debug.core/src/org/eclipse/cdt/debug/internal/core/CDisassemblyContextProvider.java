/*******************************************************************************
 * Copyright (c) 2008 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.core;

import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextProvider;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.debug.core.model.IDebugTarget;

/**
 * org.eclipse.cdt.debug.internal.core.CDisassemblyContextProvider: 
 * //TODO Add description.
 */
public class CDisassemblyContextProvider implements IDisassemblyContextProvider {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextProvider#getDisassemblyContext(java.lang.Object)
     */
    @Override
	public Object getDisassemblyContext( Object element ) {
        if ( element instanceof ICDebugElement ) {
            IDebugTarget target = ((ICDebugElement)element).getDebugTarget();
            return ((CDebugTarget)target).getDisassemblyRetrieval();
        }
        return null;
    }
}
