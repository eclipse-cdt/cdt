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

package org.eclipse.cdt.debug.internal.ui.elements.adapters;

import java.math.BigInteger;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.IDisassemblyInstruction;
import org.eclipse.cdt.debug.ui.disassembly.IElementToggleBreakpointAdapter;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;

/**
 * org.eclipse.cdt.debug.internal.ui.elements.adapters.DisassemblyToggleBreakpointAdapter: 
 * //TODO Add description.
 */
public class DisassemblyToggleBreakpointAdapter implements IElementToggleBreakpointAdapter {


    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IElementToggleBreakpointAdapter#canToggleLineBreakpoints(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    @Override
	public boolean canToggleLineBreakpoints( IPresentationContext presentationContext, Object element ) {
        if ( element instanceof IDisassemblyInstruction ) {
            return true;
        }
        return false;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IElementToggleBreakpointAdapter#toggleLineBreakpoints(org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext, java.lang.Object)
     */
    @Override
	public void toggleLineBreakpoints( IPresentationContext presentationContext, Object element ) throws CoreException {
        if ( element instanceof IDisassemblyInstruction ) {
            IDisassemblyInstruction instruction = (IDisassemblyInstruction)element;
            IBreakpoint breakpoint = findBreakpoint( instruction );
            if ( breakpoint != null ) {
                DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
            }
            else {
                IAddress address = instruction.getAdress();
                CDIDebugModel.createAddressBreakpoint( 
                                        null,
                                        "",  //$NON-NLS-1$
                                        ResourcesPlugin.getWorkspace().getRoot(),
                                        ICBreakpointType.REGULAR,
                                        -1,
                                        address, 
                                        true, 
                                        0, 
                                        "", //$NON-NLS-1$
                                        true );
            }
        }        
    }

    private IBreakpoint findBreakpoint( IDisassemblyInstruction instruction ) {
        BigInteger address = instruction.getAdress().getValue();
        IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints();
        for ( IBreakpoint bp : breakpoints ) {
            if ( bp instanceof ICLineBreakpoint ) {
                try {
                    IAddress bpAddress = ((ICDebugTarget)instruction.getDebugTarget()).getBreakpointAddress( (ICLineBreakpoint)bp );
                    if ( bpAddress != null && address.compareTo( bpAddress.getValue() ) == 0 )
                        return bp;
                }
                catch( DebugException e ) {
                }
            }
        }
        return null;
    }
}
