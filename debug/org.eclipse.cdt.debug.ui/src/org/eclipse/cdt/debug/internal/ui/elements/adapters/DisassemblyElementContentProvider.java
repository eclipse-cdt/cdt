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

import org.eclipse.cdt.debug.core.model.IDisassemblyLine;
import org.eclipse.cdt.debug.internal.core.model.DisassemblyRetrieval;
import org.eclipse.cdt.debug.internal.ui.disassembly.editor.DisassemblyEditorPresentation;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentBaseChangeUpdate;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentUpdate;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;

public class DisassemblyElementContentProvider implements IDocumentElementContentProvider {

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentProvider#updateContent(org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentUpdate)
     */
    @Override
	public void updateContent( final IDocumentElementContentUpdate update ) {
        Job job = new Job( "Source content update" ) { //$NON-NLS-1$

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
            protected IStatus run( IProgressMonitor monitor ) {
                if ( !update.isCanceled() ) {
                    retrieveDisassembly( update );
                }
                update.done();
                return Status.OK_STATUS;
            }
        };
        job.setSystem( true );
        job.schedule();
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentProvider#updateInput(org.eclipse.cdt.debug.ui.disassembly.IDocumentBaseChangeUpdate)
     */
    @Override
	public void updateInput( final IDocumentBaseChangeUpdate update ) {
        Job job = new Job( "Input update" ) { //$NON-NLS-1$

            /* (non-Javadoc)
             * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
             */
            @Override
            protected IStatus run( IProgressMonitor monitor ) {
                if ( !update.isCanceled() ) {
                    changeBaseElement( update );
                }
                update.done();
                return Status.OK_STATUS;
            }
        };
        job.setSystem( true );
        job.schedule();
    }

    protected void changeBaseElement( IDocumentBaseChangeUpdate update ) {
        Object root = update.getRootElement();
        if ( root instanceof DisassemblyRetrieval ) {
            DisassemblyRetrieval retrieval = (DisassemblyRetrieval)root;
            try {
                retrieval.changeBase( update.getElement(), update.getOriginalOffset(), getPresentationFlags( (IDocumentPresentation)update.getPresentationContext() ) );
                update.setBaseElement( retrieval.getBaseElement() );
                update.setOffset( retrieval.getCurrentOffset() );
            }
            catch( DebugException e ) {
                update.setStatus( e.getStatus() );
            }
        }
    }

    protected void retrieveDisassembly( IDocumentElementContentUpdate update ) {
        Object root = update.getRootElement();
        if ( root instanceof DisassemblyRetrieval ) {
            DisassemblyRetrieval retrieval = (DisassemblyRetrieval)root;
            try {
                retrieval.retrieveDisassembly( 
                        update.getElement(),
                        update.getBaseElement(),
                        update.getOriginalOffset(), 
                        update.getRequestedLineCount(),
                        update.reveal(),
                        getPresentationFlags( (IDocumentPresentation)update.getPresentationContext() ) );
                IDisassemblyLine[] lines = retrieval.getLines();
                update.setOffset( retrieval.getCurrentOffset() );
                update.setLineCount( lines.length );
                for( int i = 0; i < lines.length; ++i ) {
                    update.addElement( i, lines[i] );
                }
            }
            catch( DebugException e ) {
                update.setStatus( e.getStatus() );
            }
        }
    }

    private int getPresentationFlags( IDocumentPresentation presentation ) {
        int flags = 0;
        if ( presentation instanceof DisassemblyEditorPresentation ) {
            DisassemblyEditorPresentation dep = (DisassemblyEditorPresentation)presentation;
            if ( dep.showIntstructions() )
                flags |= DisassemblyRetrieval.FLAGS_SHOW_INSTRUCTIONS;
            if ( dep.showSource() )
                flags |= DisassemblyRetrieval.FLAGS_SHOW_SOURCE;
        }
        return flags;
    }
}
