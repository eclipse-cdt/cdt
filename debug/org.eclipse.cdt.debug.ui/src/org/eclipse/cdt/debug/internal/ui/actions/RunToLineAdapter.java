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

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.model.IRunToAddress;
import org.eclipse.cdt.debug.core.model.IRunToLine;
import org.eclipse.cdt.debug.internal.core.ICDebugInternalConstants;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyView;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.ISuspendResume;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.actions.IRunToLineTarget;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IStorageEditorInput;
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
					final String fileName = getFileName( input );
					ITextSelection textSelection = (ITextSelection)selection;
					final int lineNumber = textSelection.getStartLine() + 1;
					if ( target instanceof IAdaptable ) {
						final IRunToLine runToLine = (IRunToLine)((IAdaptable)target).getAdapter( IRunToLine.class );
						if ( runToLine != null && runToLine.canRunToLine( fileName, lineNumber ) ) {
							Runnable r = new Runnable() {
								
								public void run() {
									try {
										runToLine.runToLine( fileName, lineNumber, DebugUIPlugin.getDefault().getPluginPreferences().getBoolean( IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE ) );
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
		else if ( part instanceof DisassemblyView ) {
			IEditorInput input = ((DisassemblyView)part).getInput();
			if ( !(input instanceof DisassemblyEditorInput) ) {
				errorMessage = ActionMessages.getString( "RunToLineAdapter.Empty_editor_1" ); //$NON-NLS-1$
			}
			else {
				ITextSelection textSelection = (ITextSelection)selection;
				int lineNumber = textSelection.getStartLine() + 1;
				final IAddress address = ((DisassemblyEditorInput)input).getAddress( lineNumber );
				if ( target instanceof IAdaptable ) {
					final IRunToAddress runToAddress = (IRunToAddress)((IAdaptable)target).getAdapter( IRunToAddress.class );
					if ( runToAddress != null && runToAddress.canRunToAddress( address ) ) {
						Runnable r = new Runnable() {
							
							public void run() {
								try {
									runToAddress.runToAddress( address, DebugUIPlugin.getDefault().getPluginPreferences().getBoolean( IDebugUIConstants.PREF_SKIP_BREAKPOINTS_DURING_RUN_TO_LINE ) );
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
		else {
			errorMessage = ActionMessages.getString( "RunToLineAdapter.Operation_is_not_supported_1" ); //$NON-NLS-1$
		}
		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), IInternalCDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.actions.IRunToLineTarget#canRunToLine(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection, org.eclipse.debug.core.model.ISuspendResume)
	 */
	public boolean canRunToLine( IWorkbenchPart part, ISelection selection, ISuspendResume target ) {
		if ( part instanceof DisassemblyView || part instanceof ITextEditor )
			return target instanceof IDebugElement && ((IDebugElement)target).getModelIdentifier().equals( CDIDebugModel.getPluginIdentifier() );
		return false;
	}

	private String getFileName( IEditorInput input ) throws CoreException {
		if ( input instanceof IFileEditorInput ) {
			return ((IFileEditorInput)input).getFile().getName();
		}
		if ( input instanceof IStorageEditorInput ) {
			return ((IStorageEditorInput)input).getStorage().getName();
		}
		return null;
	}

	private void runInBackground( Runnable r ) {
		DebugPlugin.getDefault().asyncExec( r );
	}

	protected void failed( Throwable e ) {
		MultiStatus ms = new MultiStatus( CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, ActionMessages.getString( "RunToLineAdapter.0" ), null ); //$NON-NLS-1$
		ms.add( new Status( IStatus.ERROR, CDIDebugModel.getPluginIdentifier(), ICDebugInternalConstants.STATUS_CODE_ERROR, e.getMessage(), e ) );
		CDebugUtils.error( ms, this );
	}
}