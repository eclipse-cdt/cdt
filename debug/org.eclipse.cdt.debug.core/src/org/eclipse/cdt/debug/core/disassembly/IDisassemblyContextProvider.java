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

package org.eclipse.cdt.debug.core.disassembly;

/**
 * Provides disassembly context for given element.
 * <p>
 * Clients must implements this interface to plug into
 * the diassembly framework.
 * </p>
 * This interface is experimental.
 */
public interface IDisassemblyContextProvider {

    /**
     * Returns the disassembly context object for <code>element</code>
     * 
     * @param element the element being queried for disassembly context
     * @return an object that represents the disassembly context
     *      for the given element, or <code>null</code> if the given element
     *      doesn't provide a disassembly context
     */
    public Object getDisassemblyContext( Object element );
}
