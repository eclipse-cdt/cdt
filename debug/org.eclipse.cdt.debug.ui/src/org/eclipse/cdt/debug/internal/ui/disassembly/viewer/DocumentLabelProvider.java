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

package org.eclipse.cdt.debug.internal.ui.disassembly.viewer;

import java.util.Properties;

import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementLabelProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.BaseLabelProvider;
import org.eclipse.ui.progress.UIJob;

public class DocumentLabelProvider extends BaseLabelProvider {

    private VirtualDocument fDocument;

    public DocumentLabelProvider( VirtualDocument document ) {
        super();
        fDocument = document;
    }
//
//    public void update( Object parent, Object[] elements, IDocumentPresentation context ) {
//        IDocumentElementLabelProvider labelProvider = getLabelAdapter( parent );
//        if ( labelProvider != null ) {
//            Object root = getDocument().getContentProvider().getRoot();
//            Object base = getDocument().getContentProvider().getBase();
//            DocumentLabelUpdate[] updates = new DocumentLabelUpdate[elements.length];
//            for ( int i = 0; i < elements.length; ++i ) {
//                updates[i] = new DocumentLabelUpdate( this, context, root, base, elements[i], i );
//            }
//            labelProvider.update( updates );
//        }
//    }

    public void update( Object parent, Object element, int index, IDocumentPresentation context ) {
        IDocumentElementLabelProvider labelProvider = getLabelAdapter( element );
        if ( labelProvider != null ) {
            Object root = getDocument().getContentProvider().getRoot();
            Object base = getDocument().getContentProvider().getBase();
            labelProvider.update( new DocumentLabelUpdate[] { new DocumentLabelUpdate( this, context, root, base, element, index ) } );
        }
    }

    public void completed( DocumentLabelUpdate update ) {
        if ( update.isCanceled() )
            return;
        
        UIJob uiJob = null;
        final int index = update.getIndex();
        if ( update.getElement() != null ) {
            final Object element = update.getElement();
            final Properties labels = update.getLabels();

            uiJob = new UIJob( "Replace line" ) { //$NON-NLS-1$
                
                /* (non-Javadoc)
                 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
                 */
                @Override
                public IStatus runInUIThread( IProgressMonitor monitor ) {
                    getDocument().labelDone( element, index, labels );
                    return Status.OK_STATUS;
                }
            };
        }
        else {
            uiJob = new UIJob( "Remove line" ) { //$NON-NLS-1$
                
                /* (non-Javadoc)
                 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
                 */
                @Override
                public IStatus runInUIThread( IProgressMonitor monitor ) {
                    getDocument().removeLine( index );
                    return Status.OK_STATUS;
                }
            };
        }
        uiJob.setSystem( true );
        uiJob.schedule();
    }

    protected VirtualDocument getDocument() {
        return fDocument;
    }

    protected IDocumentElementLabelProvider getLabelAdapter( Object element ) {
        IDocumentElementLabelProvider adapter = null;
        if ( element instanceof IDocumentElementLabelProvider ) {
            adapter = (IDocumentElementLabelProvider)element;
        }
        else if ( element instanceof IAdaptable ) {
            IAdaptable adaptable = (IAdaptable)element;
            adapter = adaptable.getAdapter( IDocumentElementLabelProvider.class );
        }
        return adapter;
    }
}
