/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Run to line target adapter for the CDI debugger
 */
public class RunToLineAdapter implements IRunToLineTarget {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#runToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection,
	 *      org.eclipse.debug.core.model.ISuspendResume)
	 */
	public void runToLine( IWorkbenchPart part, ISelection selection, ISuspendResume target ) throws CoreException {
		IEditorPart editorPart = (IEditorPart)part;
		IEditorInput input = editorPart.getEditorInput();
		String errorMessage = null;
		if ( input == null ) {
			errorMessage = "Empty editor";
		}
		else {
			final ITextEditor textEditor = (ITextEditor)editorPart;
			final IDocument document = textEditor.getDocumentProvider().getDocument( input );
			if ( document == null ) {
				errorMessage = "Missing document";
			}
			else {
				IFile file = getFile( input );
				ITextSelection textSelection = (ITextSelection)selection;
				int lineNumber = textSelection.getStartLine() + 1;
			}
		}
		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#canRunToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection,
	 *      org.eclipse.debug.core.model.ISuspendResume)
	 */
	public boolean canRunToLine( IWorkbenchPart part, ISelection selection, ISuspendResume target ) {
		return target instanceof IDebugElement && ((IDebugElement)target).getModelIdentifier().equals( CDIDebugModel.getPluginIdentifier() );
	}

	private IFile getFile( IEditorInput input ) {
		if ( input instanceof IFileEditorInput ) {
			return ((IFileEditorInput)input).getFile();
		}
		return null;
	}
}