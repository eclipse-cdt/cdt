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

import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentUpdate;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;

public class DocumentContentUpdate extends DocumentUpdate implements IDocumentElementContentUpdate {

    private DocumentContentProvider fContentProvider;
    private IDocumentElementContentProvider fElementContentProvider;
    private int fLineCount = 0;
    private int fOriginalOffset = 0;
    private int fOffset = 0;
    private Object[] fElements;
    boolean fReveal = true;

    public DocumentContentUpdate( DocumentContentProvider contentProvider, IDocumentElementContentProvider elementContentProvider, IDocumentPresentation presentationContext, Object rootElement, Object baseElement, Object input, int lineCount, int offset, boolean reveal ) {
        super( presentationContext, rootElement, baseElement, input );
        fContentProvider = contentProvider;
        fElementContentProvider = elementContentProvider;
        fLineCount = lineCount;
        fOriginalOffset = offset;
        fOffset = offset;
        fElements = new Object[lineCount];
        fReveal = reveal;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentElementContentUpdate#setLineCount(int)
     */
    @Override
	public void setLineCount( int lineCount ) {
        fElements = new Object[lineCount];
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentElementContentUpdate#addElement(int, java.lang.Object)
     */
    @Override
	public void addElement( int line, Object element ) throws IndexOutOfBoundsException {
        if ( line < 0 || line >= fElements.length )
            throw new IndexOutOfBoundsException( Integer.toString( line ) );
        fElements[line] = element;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentElementContentUpdate#getOriginalOffset()
     */
    @Override
	public int getOriginalOffset() {
        return fOriginalOffset;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentElementContentUpdate#getRequestedLineCount()
     */
    @Override
	public int getRequestedLineCount() {
        return fLineCount;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentElementContentUpdate#setOffset(int)
     */
    @Override
	public void setOffset( int offset ) {
        fOffset = offset;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentElementContentUpdate#reveal()
     */
    @Override
	public boolean reveal() {
        return fReveal;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.core.commands.Request#done()
     */
    @Override
    public void done() {
        super.done();
        getContentProvider().updateCompleted( this );
    }

    protected Object[] getElements() {
        return fElements;
    }

    protected int getLineCount() {
        return fElements.length;
    }

    protected int getOffset() {
        return fOffset;
    }

    protected DocumentContentProvider getContentProvider() {
        return fContentProvider;
    }

    protected IDocumentElementContentProvider getElementContentProvider() {
        return fElementContentProvider;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.internal.ui.disassembly.DocumentUpdate#startRequest()
     */
    @Override
    void startRequest() {
        getElementContentProvider().updateContent( this );
    }
}
