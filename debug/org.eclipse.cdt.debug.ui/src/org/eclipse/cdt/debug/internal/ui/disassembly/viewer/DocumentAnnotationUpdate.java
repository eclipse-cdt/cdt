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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.ui.disassembly.IDocumentElementAnnotationUpdate;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.jface.text.source.Annotation;

/**
 * org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DocumentAnnotationUpdate: 
 * //TODO Add description.
 */
public class DocumentAnnotationUpdate extends DocumentUpdate implements IDocumentElementAnnotationUpdate {
    private DocumentAnnotationProvider fAnnotationProvider;
    private int fIndex = 0;
    private List<Annotation> fAnnotations;

    public DocumentAnnotationUpdate( DocumentAnnotationProvider annotationProvider, IDocumentPresentation presentationContext, Object root, Object base, Object element, int index ) {
        super( presentationContext, root, base, element );
        fAnnotationProvider = annotationProvider;
        fIndex = index;
        fAnnotations = new ArrayList<Annotation>();
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.ui.disassembly.IDocumentElementAnnotationUpdate#addAnnotation(org.eclipse.jface.text.source.Annotation)
     */
    @Override
	public void addAnnotation( Annotation annotation ) {
        fAnnotations.add( annotation );
    }

    public int getIndex() {
        return fIndex;
    }

    public Annotation[] getAnnotations() {
        return fAnnotations.toArray( new Annotation[fAnnotations.size()] );
    }

    protected DocumentAnnotationProvider getAnnotationProvider() {
        return fAnnotationProvider;
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.internal.ui.disassembly.DocumentUpdate#done()
     */
    @Override
    public void done() {
        super.done();
        getAnnotationProvider().completed( this );
    }

    /* (non-Javadoc)
     * @see com.arm.eclipse.rvd.internal.ui.disassembly.DocumentUpdate#startRequest()
     */
    @Override
    void startRequest() {
    }
}
