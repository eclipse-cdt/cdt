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

package org.eclipse.cdt.debug.ui.disassembly;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * An adapter to support breakpoint creation/deletion for the disassembly editor.
 * 
 * This interface is experimental.
 */
public interface IElementToggleBreakpointAdapter {

    /**
     * Creates new line breakpoints or removes existing breakpoints for the given element.
     */
    public void toggleLineBreakpoints( IPresentationContext presentationContext, Object element ) throws CoreException;

    /**
     * Returns whether line breakpoints can be toggled on the given element.
     */
    public boolean canToggleLineBreakpoints( IPresentationContext presentationContext, Object element );
}
