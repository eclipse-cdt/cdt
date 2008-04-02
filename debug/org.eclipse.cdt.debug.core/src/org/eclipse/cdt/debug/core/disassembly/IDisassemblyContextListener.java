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
 * The instances of this interface are notified when 
 * a disassembly context is registered or unregistered 
 * with <code>IDisassemblyContextService</code>.
 * <p>
 * This interface is used by the disassembly UI components.
 * </p>
 * <p>
 * The clients may implement this interface.
 * </p>
 * This interface is experimental.
 */
public interface IDisassemblyContextListener {
    
    /**
     * Indicates that <code>context</code> has been registered
     * with <code>IDisassemblyContextService</code>.
     * 
     * @param context the disassembly context that is registered
     */
    public void contextAdded( Object context );

    /**
     * Indicates that <code>context</code> has been unregistered
     * with <code>IDisassemblyContextService</code>.
     * 
     * @param context the disassembly context that is unregistered
     */
    public void contextRemoved( Object context );
}
