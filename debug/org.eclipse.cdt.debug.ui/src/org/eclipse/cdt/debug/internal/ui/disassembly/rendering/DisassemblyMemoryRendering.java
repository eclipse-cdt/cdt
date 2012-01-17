/*******************************************************************************
 * Copyright (c) 2008, 2010 ARM Limited and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * ARM Limited - Initial API and implementation
 *******************************************************************************/

package org.eclipse.cdt.debug.internal.ui.disassembly.rendering;

import org.eclipse.cdt.debug.core.disassembly.IDisassemblyContextProvider;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DisassemblyPane;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.VirtualDocument;
import org.eclipse.cdt.debug.internal.ui.disassembly.viewer.VirtualSourceViewer;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.ui.memory.AbstractMemoryRendering;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

public class DisassemblyMemoryRendering extends AbstractMemoryRendering {

    protected SashForm fSashForm;
    protected DisassemblyPane fDisassemblyPane;

    public DisassemblyMemoryRendering( String renderingId ) {
        super( renderingId );
        fDisassemblyPane = new DisassemblyPane( "#DisassemblyRenderingContext", "#DisassemblyRenderingRulerContext" );  //$NON-NLS-1$//$NON-NLS-2$
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.memory.IMemoryRendering#createControl(org.eclipse.swt.widgets.Composite)
     */
    @Override
	public Control createControl( Composite parent ) {
        Composite composite = new Composite( parent, SWT.BORDER );
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.verticalSpacing = 0;
        composite.setLayout( layout );
        composite.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        createViewer( composite );
        return composite;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.memory.IMemoryRendering#getControl()
     */
    @Override
	public Control getControl() {
        return fDisassemblyPane.getControl();
    }

    protected VirtualSourceViewer getViewer() {
        return fDisassemblyPane.getViewer();
    }

    protected void createViewer( final Composite parent ) {
        fSashForm = new SashForm( parent, SWT.VERTICAL );
        fSashForm.setLayoutData( new GridData( GridData.FILL_BOTH  ) );
        
        fDisassemblyPane.create( fSashForm );

//      createGoToAddressComposite( fSashForm );
//      hideGotoAddressComposite();

        IMemoryBlock memoryBlock = getMemoryBlock();
        IDisassemblyContextProvider contextProvider = getDisassemblyContextProvider( memoryBlock );
        Object disassemblyContext = null;
        if ( contextProvider != null ) {
            disassemblyContext = contextProvider.getDisassemblyContext( memoryBlock );
        }
        DisassemblyAnnotationModel annotationModel = new DisassemblyAnnotationModel();
        VirtualDocument document = new VirtualDocument( annotationModel, getDocumentPresentationContext(), disassemblyContext );
        getViewer().setDocument( document );
        document.getContentProvider().changeInput( getViewer(), document.getPresentationContext(), null, getMemoryBlock(), document.getCurrentOffset() );
    }

    private IDocumentPresentation getDocumentPresentationContext() {
        return null;
    }

    private IDisassemblyContextProvider getDisassemblyContextProvider( Object element ) {
        IDisassemblyContextProvider adapter = null;
        if ( element instanceof IDisassemblyContextProvider ) {
            adapter = (IDisassemblyContextProvider)element;
        }
        else if ( element instanceof IAdaptable ) {
            IAdaptable adaptable = (IAdaptable)element;
            adapter = (IDisassemblyContextProvider)adaptable.getAdapter( IDisassemblyContextProvider.class );
        }
        return adapter;
    }
}
