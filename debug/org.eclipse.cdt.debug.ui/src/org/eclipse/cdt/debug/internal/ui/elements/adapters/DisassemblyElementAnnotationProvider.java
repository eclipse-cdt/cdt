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
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassemblyInstruction;
import org.eclipse.cdt.debug.internal.core.model.CDebugTarget;
import org.eclipse.cdt.debug.internal.core.model.DisassemblyRetrieval;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyInstructionPointerAnnotation;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementAnnotationProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementAnnotationUpdate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.ui.texteditor.MarkerAnnotation;

public class DisassemblyElementAnnotationProvider implements IDocumentElementAnnotationProvider {

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentElementAnnotationProvider#update(com.arm.eclipse.rvd.ui.disassembly.IDocumentElementAnnotationUpdate[])
     */
    public void update( final IDocumentElementAnnotationUpdate[] updates ) {
        Job job = new Job( "Annotations update" ) { //$NON-NLS-1$

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
            protected IStatus run( IProgressMonitor monitor ) {
                for( int i = 0; i < updates.length; i++ ) {
                    IDocumentElementAnnotationUpdate update = updates[i];
                    if ( !update.isCanceled() ) {
                        retrieveAnnotations( update );
                    }
                    update.done();
                }
                return Status.OK_STATUS;
            }
        };
        job.setSystem( true );
        job.schedule();
    }

    protected void retrieveAnnotations( IDocumentElementAnnotationUpdate update ) {
        retrieveInstructionPointers( update );
        retrieveBreakpoints( update );
    }

    private void retrieveInstructionPointers( IDocumentElementAnnotationUpdate update ) {
        Object root = update.getRootElement();
        if ( root instanceof DisassemblyRetrieval ) {
            DisassemblyRetrieval retrieval = (DisassemblyRetrieval)root;
            Object input = retrieval.getInput();
            if ( input instanceof ICStackFrame ) {
                Object element = update.getElement();
                if ( element instanceof IDisassemblyInstruction ) {
                    BigInteger address = ((IDisassemblyInstruction)element).getAdress().getValue();
                    ICStackFrame frame = (ICStackFrame)input;
                    IAddress frameAddress = frame.getAddress();	// will return null if frame has been disposed
                    if ( (frameAddress != null) && address.equals( frameAddress.getValue() ) ) {
                        IThread thread = frame.getThread();
                        boolean topFrame;
                        try {
                            topFrame = ( frame == thread.getTopStackFrame() );
                            update.addAnnotation( new DisassemblyInstructionPointerAnnotation( frame, topFrame ) );
                        }
                        catch( DebugException e ) {
                        } 
                    }
                }
            }
        }
    }

    private void retrieveBreakpoints( IDocumentElementAnnotationUpdate update ) {
        IBreakpoint[] breakpoints = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints( CDebugCorePlugin.getUniqueIdentifier() );
        for ( IBreakpoint b : breakpoints ) {
            if ( b instanceof ICLineBreakpoint ) {
                try {
                    Object root = update.getRootElement();
                    if ( root instanceof DisassemblyRetrieval ) {
                        Object element = update.getElement();
                        if ( element instanceof IDisassemblyInstruction ) {
                            IDisassemblyInstruction instruction = (IDisassemblyInstruction)element;
                            BigInteger address = ((CDebugTarget)((DisassemblyRetrieval)root).getDebugTarget()).getBreakpointAddress( (ICLineBreakpoint)b ).getValue();
                            BigInteger instrAddress = instruction.getAdress().getValue();
                            if ( address != null && instrAddress != null && instrAddress.compareTo( address ) == 0 ) {
                                update.addAnnotation( new MarkerAnnotation( b.getMarker() ) );
                            }
                        }
                    }
                }
                catch( CoreException e ) {
                }
                catch( NumberFormatException e ) {
                }
            }
        }
    }
}
