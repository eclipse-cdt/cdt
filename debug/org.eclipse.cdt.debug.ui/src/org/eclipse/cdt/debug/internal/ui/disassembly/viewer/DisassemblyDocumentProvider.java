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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.internal.ui.disassembly.editor.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.disassembly.editor.DisassemblyEditorPresentation;
import org.eclipse.cdt.debug.ui.disassembly.IDocumentPresentation;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.AnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

/**
 * org.eclipse.cdt.debug.internal.ui.disassembly.viewer.DisassemblyDocumentProvider: 
 * //TODO Add description.
 */
public class DisassemblyDocumentProvider implements IDocumentProvider {

    class DocumentInfo {
        
        private VirtualDocument fDocument;
        private IAnnotationModel fAnnotationModel;
        private IDocumentPresentation fPresentation;

        DocumentInfo( VirtualDocument document, IAnnotationModel annotationModel, IDocumentPresentation presentation ) {
            fDocument = document;
            fAnnotationModel = annotationModel;
            fPresentation = presentation;
        }

        VirtualDocument getDocument() {
            return fDocument;
        }

        IAnnotationModel getAnnotationModel() {
            return fAnnotationModel;
        }

        IDocumentPresentation getPresentation() {
            return fPresentation;
        }
        
        void dispose() {
            fPresentation.dispose();
            fPresentation = null;
            fAnnotationModel = null;
            fDocument.dispose();
            fDocument = null;
        }
    }

    private Map<Object, DocumentInfo> fDocumentInfos;

    public DisassemblyDocumentProvider() {
        fDocumentInfos = new HashMap<Object, DocumentInfo>();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#aboutToChange(java.lang.Object)
     */
    @Override
	public void aboutToChange( Object element ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#addElementStateListener(org.eclipse.ui.texteditor.IElementStateListener)
     */
    @Override
	public void addElementStateListener( IElementStateListener listener ) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#canSaveDocument(java.lang.Object)
     */
    @Override
	public boolean canSaveDocument( Object element ) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#changed(java.lang.Object)
     */
    @Override
	public void changed( Object element ) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#connect(java.lang.Object)
     */
    @Override
	public synchronized void connect( Object element ) throws CoreException {
        Object disassemblyContext = ((DisassemblyEditorInput)element).getDisassemblyContext();
        if ( fDocumentInfos.get( disassemblyContext ) == null ) {
            IDocumentPresentation presentation = createDocumentPresentation( disassemblyContext );
            AnnotationModel annotationModel = createAnnotationModel();
            VirtualDocument document = createDocument( disassemblyContext, presentation, annotationModel );
            fDocumentInfos.put( disassemblyContext, new DocumentInfo( document, annotationModel, presentation ) );
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#disconnect(java.lang.Object)
     */
    @Override
	public synchronized void disconnect( Object element ) {
        Object disassemblyContext = ((DisassemblyEditorInput)element).getDisassemblyContext();
        DocumentInfo info = fDocumentInfos.remove( disassemblyContext );
        if ( info != null ) {
            info.dispose();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#getAnnotationModel(java.lang.Object)
     */
    @Override
	public IAnnotationModel getAnnotationModel( Object element ) {
        Object disassemblyContext = ((DisassemblyEditorInput)element).getDisassemblyContext();
        DocumentInfo info = fDocumentInfos.get( disassemblyContext );
        return ( info != null ) ? info.getAnnotationModel() : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#getDocument(java.lang.Object)
     */
    @Override
	public IDocument getDocument( Object element ) {
        Object disassemblyContext = ((DisassemblyEditorInput)element).getDisassemblyContext();        
        DocumentInfo info = fDocumentInfos.get( disassemblyContext );
        return ( info != null ) ? info.getDocument() : null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#getModificationStamp(java.lang.Object)
     */
    @Override
	public long getModificationStamp( Object element ) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#getSynchronizationStamp(java.lang.Object)
     */
    @Override
	public long getSynchronizationStamp( Object element ) {
        // TODO Auto-generated method stub
        return 0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#isDeleted(java.lang.Object)
     */
    @Override
	public boolean isDeleted( Object element ) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#mustSaveDocument(java.lang.Object)
     */
    @Override
	public boolean mustSaveDocument( Object element ) {
        return false;
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#removeElementStateListener(org.eclipse.ui.texteditor.IElementStateListener)
     */
    @Override
	public void removeElementStateListener( IElementStateListener listener ) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#resetDocument(java.lang.Object)
     */
    @Override
	public void resetDocument( Object element ) throws CoreException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.IDocumentProvider#saveDocument(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
     */
    @Override
	public void saveDocument( IProgressMonitor monitor, Object element, IDocument document, boolean overwrite ) throws CoreException {
        // TODO Auto-generated method stub

    }

    public void dispose() {
        for( DocumentInfo info : fDocumentInfos.values() ) {
            info.dispose();
        }
        fDocumentInfos.clear();
    }

    public IDocumentPresentation getDocumentPresentation( Object element ) {
        Object disassemblyContext = ((DisassemblyEditorInput)element).getDisassemblyContext();
        DocumentInfo info = fDocumentInfos.get( disassemblyContext );
        return ( info != null ) ? info.getPresentation() : null;
    }

    private AnnotationModel createAnnotationModel() {
        return new AnnotationModel();
    }

    private VirtualDocument createDocument( Object disassemblyContext, IDocumentPresentation presentationContext, AnnotationModel annotationModel ) {
        return new VirtualDocument( annotationModel, presentationContext, disassemblyContext );
    }

    private IDocumentPresentation createDocumentPresentation( Object context ) {
        return new DisassemblyEditorPresentation();
    }
}
