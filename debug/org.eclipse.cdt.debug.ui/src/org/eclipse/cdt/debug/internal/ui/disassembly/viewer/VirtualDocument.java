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

import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;

/**
 * Converts the model elements into the text content
 */
public class VirtualDocument extends Document {

    private Object fRoot;
    private int fCurrentOffset = 0;

    private IDocumentPresentation fPresentationContext;
    private IAnnotationModel fAnnotationModel;
    private DocumentContentProvider fContentProvider;
    
    public VirtualDocument( IAnnotationModel annotationModel, IDocumentPresentation presentationContext, Object root ) {
        super();
        fRoot = root;
        fPresentationContext = presentationContext;
        fAnnotationModel = annotationModel;
        fContentProvider = new DocumentContentProvider( this );
    }

    public void dispose() {
        getContentProvider().dispose();
        fRoot = null;
    }

    public IDocumentPresentation getPresentationContext() {
        return fPresentationContext;
    }

    public IAnnotationModel getAnnotationModel() {
        return fAnnotationModel;
    }

    public DocumentContentProvider getContentProvider() {
        return fContentProvider;
    }

    public int getCurrentOffset() {
        return fCurrentOffset;
    }

    public void setCurrentOffset( int offset ) {
        fCurrentOffset = offset;
    }

    public void updateContent( int lineCount, int offset, boolean revealInput ) {
    }

    protected void updateElement( Object input, int index, Object element ) {
    }

    final void labelDone( Object element, int lineNumber, Properties labels ) {
    }

    protected void removeLine( int lineNumber ) {
    }

    protected void updateAnnotations( int lineNumber, Annotation[] annotations ) {
    }
}
