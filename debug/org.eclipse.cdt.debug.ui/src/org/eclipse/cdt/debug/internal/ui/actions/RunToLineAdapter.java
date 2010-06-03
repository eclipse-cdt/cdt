/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 * Freescale - https://bugs.eclipse.org/bugs/show_bug.cgi?id=186929
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.core.model.CDebugElement;
import org.eclipse.cdt.debug.internal.core.sourcelookup.CSourceLookupDirector;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Run to line target adapter for the CDI debugger
 */
public class RunToLineAdapter implements IRunToLineTarget {

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#runToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public void runToLine( IWorkbenchPart part, ISelection selection, ISuspendResume target ) throws CoreException {
		String errorMessage = null;
		if ( part instanceof ITextEditor ) {
			ITextEditor textEditor = (ITextEditor)part;
			IEditorInput input = textEditor.getEditorInput();
			if ( input == null ) {
				errorMessage = ActionMessages.getString( "RunToLineAdapter.Empty_editor_1" ); //$NON-NLS-1$
			}
			else {
				IDocument document = textEditor.getDocumentProvider().getDocument( input );
				if ( document == null ) {
					errorMessage = ActionMessages.getString( "RunToLineAdapter.Missing_document_1" ); //$NON-NLS-1$
				}
				else {
					final String fileName = getFileName( input ); // actually, absolute path, not just file name
					IDebugTarget debugTarget = null;
					if (target instanceof CDebugElement) { // should always be, but just in case
						debugTarget = ((CDebugElement)target).getDebugTarget();
					}
					final IPath path = convertPath( fileName, debugTarget );					
					ITextSelection textSelection = (ITextSelection)selection;
					final int lineNumber = textSelection.getStartLine() + 1;
					if ( target instanceof IAdaptable ) {
						final IRunToLine runToLine = (IRunToLine)((IAdaptable)target).getAdapter( IRunToLine.class );
						if ( runToLine != null && runToLine.canRunToLine( path.toPortableString(), lineNumber ) ) {
							Runnable r = new Runnable() {
								
								public void run() {
									try {
										runToLine.runToLine( path.toPortableString(), lineNumber, DebugUITools.getPreferenceStore().getBoolean( IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE ) );
									}
									catch( DebugException e ) {
										failed( e );
									}								
								}
							};
							runInBackground( r );
						}
					}
					return;
				}
			}
		}
		else {
			errorMessage = ActionMessages.getString( "RunToLineAdapter.Operation_is_not_supported_1" ); //$NON-NLS-1$
		}
		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), IInternalCDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#canRunToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public boolean canRunToLine( IWorkbenchPart part, ISelection selection, ISuspendResume target ) {
		if ( target instanceof IAdaptable ) {			
			if ( part instanceof IEditorPart ) {
				IRunToLine runToLine = (IRunToLine)((IAdaptable)target).getAdapter( IRunToLine.class );
				if ( runToLine == null )
					return false;
				IEditorPart editorPart = (IEditorPart)part;
				IEditorInput input = editorPart.getEditorInput();
				if ( input == null ) {
					return false;
				}
				if ( !(editorPart instanceof ITextEditor) ) {
					return false;
				}
				ITextEditor textEditor = (ITextEditor)editorPart;
				IDocument document = textEditor.getDocumentProvider().getDocument( input );
				if ( document == null ) {
					return false;
				}
				String fileName = null; // actually, absolute path, not just file name
				try {
					fileName = getFileName( input );
				}
				catch( CoreException e ) {
				}
				if (fileName == null) {
					return false;
				}
				IDebugTarget debugTarget = null;
				if (target instanceof CDebugElement) { // should always be, but just in case
					debugTarget = ((CDebugElement)target).getDebugTarget();
				}
				final IPath path = convertPath( fileName, debugTarget );					
				
				ITextSelection textSelection = (ITextSelection)selection;
				int lineNumber = textSelection.getStartLine() + 1;
				return runToLine.canRunToLine( path.toPortableString(), lineNumber );
			}
		}
		return false;
	}

	private String getFileName( IEditorInput input ) throws CoreException {
		return CDebugUIUtils.getEditorFilePath(input);
	}

	private void runInBackground( Runnable r ) {
		DebugPlugin.getDefault().asyncExec( r );
	}

	protected void failed( Throwable e ) {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, ActionMessages.getString( "RunToLineAdapter.0" ), null ); //$NON-NLS-1$
		ms.add( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e ) );
		CDebugUtils.error( ms, this );
	}
	
	private IPath convertPath( String sourceHandle, IDebugTarget debugTarget ) {
		IPath path = null;
		if ( Path.EMPTY.isValidPath( sourceHandle ) ) {
			if ( debugTarget != null ) {
				ISourceLocator sl = debugTarget.getLaunch().getSourceLocator();
				if ( sl instanceof CSourceLookupDirector ) {
					path = ((CSourceLookupDirector)sl).getCompilationPath( sourceHandle );
				}
			}
			if ( path == null ) {
				path = new Path( sourceHandle );
			}
		}
		return path;
	}
}
