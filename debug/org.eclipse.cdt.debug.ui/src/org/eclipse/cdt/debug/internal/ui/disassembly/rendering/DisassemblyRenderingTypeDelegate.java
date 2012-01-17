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

package org.eclipse.cdt.debug.internal.ui.disassembly.rendering;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.memory.IMemoryRendering;
import org.eclipse.debug.ui.memory.IMemoryRenderingTypeDelegate;

public class DisassemblyRenderingTypeDelegate implements IMemoryRenderingTypeDelegate {

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.memory.IMemoryRenderingTypeDelegate#createRendering(java.lang.String)
     */
    @Override
	public IMemoryRendering createRendering( String id ) throws CoreException {
        return new DisassemblyMemoryRendering( id );
    }
}
