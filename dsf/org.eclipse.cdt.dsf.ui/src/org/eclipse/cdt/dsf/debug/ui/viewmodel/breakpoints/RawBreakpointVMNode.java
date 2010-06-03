/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.ui.viewmodel.breakpoints;

import org.eclipse.debug.core.model.IBreakpoint;

/**
 * Breakpoint node which uses raw breakpiont objects (without a wrapper) as 
 * elements which are populated into the view.  The breakpoint objects are 
 * responsible for supplying their own label and memento providers, as well
 * as content provider for any children.
 * 
 * @since 2.1
 */
public class RawBreakpointVMNode extends AbstractBreakpointVMNode {

    public RawBreakpointVMNode(BreakpointVMProvider provider) {
        super(provider);
    }

    @Override
    protected Object createBreakpiontElement(IBreakpoint bp) {
        return bp;
    }
}
