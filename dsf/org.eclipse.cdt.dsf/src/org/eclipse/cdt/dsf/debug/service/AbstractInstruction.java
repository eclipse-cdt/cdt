/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.service;

/**
 * Implementers of {@link IInstruction} should extend this abstract class
 * instead of implementing the interface directly.
 *
 * @since 2.2
 */
public abstract class AbstractInstruction implements IInstructionWithSize {
    /*
     * @see org.eclipse.cdt.dsf.debug.service.IInstructionWithSize#getSize()
     */
    public Integer getSize() {
        // unkown size
        return null;
    }
}
