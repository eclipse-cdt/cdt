/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.dd.examples.pda.service.breakpoints;

import org.eclipse.dd.dsf.concurrent.Immutable;
import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IBreakpoints.IBreakpointDMContext;
import org.eclipse.dd.examples.pda.service.command.PDACommandControlDMContext;

/**
 * Context representing a PDA line breakpoint.  In PDA debugger, since there is only 
 * one file being debugged at a time, a breakpoint is uniquely identified using the 
 * line number only.
 */
@Immutable
class BreakpointDMContext extends AbstractDMContext implements IBreakpointDMContext {

	final Integer fLine;

	public BreakpointDMContext(String sessionId, PDACommandControlDMContext commandControlCtx, Integer line) {
        super(sessionId, new IDMContext[] { commandControlCtx });
        fLine = line;
    }

    @Override
    public boolean equals(Object obj) {
        return baseEquals(obj) && (fLine.equals(((BreakpointDMContext) obj).fLine));
    }
    
    @Override
    public int hashCode() {
        return baseHashCode() + fLine.hashCode();
    }

    @Override
    public String toString() {
        return baseToString() + ".breakpoint(" + fLine + ")";  //$NON-NLS-1$//$NON-NLS-2$*/
    }
}