/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointsListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

/**
 * Document provider for disassembly view.
 */
public class DisassemblyDocumentProvider implements IDocumentProvider, IBreakpointsListener {

	private IDocument fDocument;
	private DisassemblyAnnotationModel fAnnotationModel;
	
	/**
	 * Constructor for DisassemblyDocumentProvider.
	 */
	public DisassemblyDocumentProvider() {
		fDocument = new Document();
		fAnnotationModel = new DisassemblyAnnotationModel();
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#connect(java.lang.Object)
	 */
	public void connect( Object element ) throws CoreException {
		if ( element instanceof DisassemblyEditorInput ) {
			String contents = ((DisassemblyEditorInput)element).getContents();
			fDocument.set( contents );
		}
		else {
			fDocument.set( "" ); //$NON-NLS-1$
		}
		fAnnotationModel.setInput( ( element instanceof DisassemblyEditorInput ) ? (DisassemblyEditorInput)element : null, fDocument );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#disconnect(java.lang.Object)
	 */
	public void disconnect( Object element ) {
		fDocument.set( "" ); //$NON-NLS-1$
		fAnnotationModel.setInput( null, fDocument );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getDocument(java.lang.Object)
	 */
	public IDocument getDocument( Object element ) {
		return fDocument;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#resetDocument(java.lang.Object)
	 */
	public void resetDocument( Object element ) throws CoreException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#saveDocument(org.eclipse.core.runtime.IProgressMonitor, java.lang.Object, org.eclipse.jface.text.IDocument, boolean)
	 */
	public void saveDocument( IProgressMonitor monitor, Object element, IDocument document, boolean overwrite ) throws CoreException {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getModificationStamp(java.lang.Object)
	 */
	public long getModificationStamp( Object element ) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getSynchronizationStamp(java.lang.Object)
	 */
	public long getSynchronizationStamp( Object element ) {
		// TODO Auto-generated method stub
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#isDeleted(java.lang.Object)
	 */
	public boolean isDeleted( Object element ) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#mustSaveDocument(java.lang.Object)
	 */
	public boolean mustSaveDocument( Object element ) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#canSaveDocument(java.lang.Object)
	 */
	public boolean canSaveDocument( Object element ) {
		// TODO Auto-generated method stub
		return false;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#getAnnotationModel(java.lang.Object)
	 */
	public IAnnotationModel getAnnotationModel( Object element ) {
		return fAnnotationModel;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#aboutToChange(java.lang.Object)
	 */
	public void aboutToChange( Object element ) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#changed(java.lang.Object)
	 */
	public void changed( Object element ) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#addElementStateListener(org.eclipse.ui.texteditor.IElementStateListener)
	 */
	public void addElementStateListener( IElementStateListener listener ) {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IDocumentProvider#removeElementStateListener(org.eclipse.ui.texteditor.IElementStateListener)
	 */
	public void removeElementStateListener( IElementStateListener listener ) {
		// TODO Auto-generated method stub
	}

	protected void dispose() {
		fDocument = null;
		if ( fAnnotationModel != null ) {
			fAnnotationModel.dispose();
			fAnnotationModel = null;
		}
		DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsAdded(org.eclipse.debug.core.model.IBreakpoint[])
	 */
	public void breakpointsAdded( IBreakpoint[] breakpoints ) {
		fAnnotationModel.breakpointsAdded( breakpoints, fDocument );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsChanged(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsChanged( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		fAnnotationModel.breakpointsChanged( breakpoints, fDocument );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.IBreakpointsListener#breakpointsRemoved(org.eclipse.debug.core.model.IBreakpoint[], org.eclipse.core.resources.IMarkerDelta[])
	 */
	public void breakpointsRemoved( IBreakpoint[] breakpoints, IMarkerDelta[] deltas ) {
		fAnnotationModel.breakpointsRemoved( breakpoints, fDocument );
	}
}
