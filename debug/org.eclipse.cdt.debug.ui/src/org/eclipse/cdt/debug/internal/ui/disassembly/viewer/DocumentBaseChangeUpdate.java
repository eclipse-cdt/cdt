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

import org.eclipse.cdt.debug.ui.disassembly.IDocumentBaseChangeUpdate;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementContentProvider;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;

public class DocumentBaseChangeUpdate extends DocumentUpdate implements IDocumentBaseChangeUpdate {

    private DocumentContentProvider fContentProvider;
    private IDocumentElementContentProvider fElementContentProvider;
    private int fOriginalOffset = 0;
    private int fOffset = 0;

    public DocumentBaseChangeUpdate( DocumentContentProvider contentProvider, IDocumentElementContentProvider elementContentProvider, IDocumentPresentation presentationContext, Object root, Object base, Object input, int offset ) {
        super( presentationContext, root, base, input );
        fContentProvider = contentProvider;
        fElementContentProvider = elementContentProvider;
        fOriginalOffset = offset;
        fOffset = offset;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentBaseChangeUpdate#setBaseElement(java.lang.Object)
     */
    @Override
    public void setBaseElement( Object base ) {
        super.setBaseElement( base );
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentBaseChangeUpdate#setOffset(int)
     */
    @Override
	public void setOffset( int offset ) {
        fOffset = offset;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentBaseChangeUpdate#getOriginalOffset()
     */
    @Override
	public int getOriginalOffset() {
        return fOriginalOffset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.debug.internal.core.commands.Request#done()
     */
    @Override
    public void done() {
        super.done();
        getContentProvider().inputChanged( this );
    }

    public int getOffset() {
        return fOffset;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.internal.ui.disassembly.DocumentUpdate#startRequest()
     */
    @Override
    void startRequest() {
        getElementContentProvider().updateInput( this );
    }

    protected DocumentContentProvider getContentProvider() {
        return fContentProvider;
    }

    protected IDocumentElementContentProvider getElementContentProvider() {
        return fElementContentProvider;
    }
}
