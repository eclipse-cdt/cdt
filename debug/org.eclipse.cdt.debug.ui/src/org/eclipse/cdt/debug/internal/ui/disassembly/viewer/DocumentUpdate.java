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

import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentUpdate;
import org.eclipse.debug.internal.core.commands.Request;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.jface.viewers.TreePath;

public abstract class DocumentUpdate extends Request implements IDocumentUpdate {

    private Object fRootElement;
    private Object fBaseElement;
    private Object fElement;
    private IDocumentPresentation fPresentationContext;

    private boolean fDone = false;
    private boolean fStarted = false;
    
    public DocumentUpdate( IDocumentPresentation presentationContext, Object rootElement, Object baseElement, Object element ) {
        super();
        fRootElement = rootElement;
        fBaseElement = baseElement;
        fElement = element;
        fPresentationContext = presentationContext;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentUpdate#getBaseElement()
     */
    @Override
	public Object getBaseElement() {
        return fBaseElement;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentUpdate#getRootElement()
     */
    @Override
	public Object getRootElement() {
        return fRootElement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElement()
     */
    @Override
	public Object getElement() {
        return fElement;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getElementPath()
     */
    @Override
	public TreePath getElementPath() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getPresentationContext()
     */
    @Override
	public IPresentationContext getPresentationContext() {
        return fPresentationContext;
    }

    /**
     * Starts this request. Subclasses must override startRequest().
     */
    final void start() {
        synchronized( this ) {
            if ( fStarted ) {
                return;
            }
            fStarted = true;
        }
        if ( !isCanceled() ) {
            startRequest();
        }
        else {
            done();
        }
    }   

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.core.commands.Request#done()
     */
    @Override
    public void done() {
        synchronized( this ) {
            if ( isDone() ) {
                return;
            }
            fDone = true;
        }
    }

    void setRootElement( Object rootElement ) {
        fRootElement = rootElement;
    }

    void setBaseElement( Object baseElement ) {
        fBaseElement = baseElement;
    }

    protected synchronized boolean isDone() {
        return fDone;
    }
    
    /**
     * Subclasses must override to initiate specific request types.
     */
    abstract void startRequest();

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate#getViewerInput()
     */
    @Override
	public Object getViewerInput() {
        return null;
    }
}
