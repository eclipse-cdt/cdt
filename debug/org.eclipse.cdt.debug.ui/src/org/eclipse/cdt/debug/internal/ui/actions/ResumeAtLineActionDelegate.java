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
import org.eclipse.cdt.debug.core.model.IJumpToAddress;
import org.eclipse.cdt.debug.core.model.IJumpToLine;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyEditorInput;
import org.eclipse.cdt.debug.internal.ui.views.disassembly.DisassemblyView;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

/**
 * Global retargettable "Resume at Line" action.
 */
public class ResumeAtLineActionDelegate implements IWorkbenchWindowActionDelegate, IPartListener, IUpdate {

	protected IWorkbenchWindow fWindow = null;
	private IWorkbenchPart fActivePart = null;
	private IAction fAction = null;
	private IDebugElement fTargetElement = null;

	private static final ISelection EMPTY_SELECTION = new EmptySelection();  
	
	static class EmptySelection implements ISelection {
		public boolean isEmpty() {
			return true;
		}		
	}

	private ISelectionListener fSelectionListener = new DebugSelectionListener();

	class DebugSelectionListener implements ISelectionListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
		 */
		public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
			setTargetElement( null );
			if ( selection instanceof IStructuredSelection ) {
				IStructuredSelection ss = (IStructuredSelection)selection;
				if ( ss.size() == 1 ) {
					Object object = ss.getFirstElement();
					if ( object instanceof IDebugElement ) {
						setTargetElement( (IDebugElement)object );
					}
				}
			}
			update();
		}
	}

	/**
	 * Returns the current selection in the active part, possibly
	 * and empty selection, but never <code>null</code>.
	 * 
	 * @return the selection in the active part, possibly empty
	 */
	private ISelection getTargetSelection() {
		if ( fActivePart != null ) {
			ISelectionProvider selectionProvider = fActivePart.getSite().getSelectionProvider();
			if ( selectionProvider != null ) {
				return selectionProvider.getSelection();
			}
		}
		return EMPTY_SELECTION;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
		fWindow.getSelectionService().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener );
		fWindow.getPartService().removePartListener( this );
		fActivePart = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(org.eclipse.ui.IWorkbenchWindow)
	 */
	public void init( IWorkbenchWindow window ) {
		this.fWindow = window;
		IPartService partService = window.getPartService();
		partService.addPartListener( this );
		fWindow.getSelectionService().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, fSelectionListener );
		IWorkbenchPart part = partService.getActivePart();
		if ( part != null ) {
			partActivated( part );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part ) {
		fActivePart = part;
		update();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partBroughtToTop( IWorkbenchPart part ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partClosed( IWorkbenchPart part ) {
		clearPart(part);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part ) {
		clearPart(part);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partOpened( IWorkbenchPart part ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.IUpdate#update()
	 */
	public void update() {
		if ( fAction == null ) {
			return;
		}
		fAction.setEnabled( canPerformAction( getTargetSelection() ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action ) {
		if ( fTargetElement != null ) {
			try {
				performAction( getTargetSelection() );
			} catch( CoreException e ) {
				DebugUIPlugin.errorDialog( fWindow.getShell(), ActionMessages.getString( "ResumeAtLineActionDelegate.Error_1" ), ActionMessages.getString( "ResumeAtLineActionDelegate.Operation_failed_1" ), e.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		this.fAction = action;
		update();
	}

	/**
	 * Clears reference to active part and adapter when a relevant part
	 * is closed or deactivated.
	 * 
	 * @param part workbench part that has been closed or deactivated
	 */
	protected void clearPart( IWorkbenchPart part ) {
		if ( part.equals( fActivePart ) ) {
			fActivePart = null;
		}
	}

	protected IDebugElement getTargetElement() {
		return this.fTargetElement;
	}
	
	protected void setTargetElement( IDebugElement targetElement ) {
		this.fTargetElement = targetElement;
	}

	private boolean canPerformAction( ISelection selection ) {
		if ( fTargetElement == null || 
			 !fTargetElement.getModelIdentifier().equals( CDIDebugModel.getPluginIdentifier() ) )
			return false;
		IDebugTarget debugTarget = fTargetElement.getDebugTarget();
		if ( fActivePart instanceof IEditorPart ) {
			IJumpToLine jumpToLine = (IJumpToLine)debugTarget.getAdapter( IJumpToLine.class );
			if ( jumpToLine == null )
				return false;
			IEditorPart editorPart = (IEditorPart)fActivePart;
			IEditorInput input = editorPart.getEditorInput();
			if ( input == null ) {
				return false;
			}
			ITextEditor textEditor = (ITextEditor)editorPart;
			IDocument document = textEditor.getDocumentProvider().getDocument( input );
			if ( document == null ) {
				return false;
			}
			String fileName;
			try {
				fileName = getFileName( input );
			}
			catch( CoreException e ) {
				return false;
			}
			ITextSelection textSelection = (ITextSelection)selection;
			int lineNumber = textSelection.getStartLine() + 1;
			return jumpToLine.canJumpToLine( fileName, lineNumber );
		}
		if ( fActivePart instanceof DisassemblyView ) {
			IJumpToAddress jumpToAddress = (IJumpToAddress)debugTarget.getAdapter( IJumpToAddress.class );
			if ( jumpToAddress == null )
				return false;
			IEditorInput input = ((DisassemblyView)fActivePart).getInput();
			if ( !(input instanceof DisassemblyEditorInput) ) {
				return false;
			}
			ITextSelection textSelection = (ITextSelection)selection;
			int lineNumber = textSelection.getStartLine() + 1;
			IAddress address = ((DisassemblyEditorInput)input).getAddress( lineNumber );
			return jumpToAddress.canJumpToAddress( address );
		}
		return false;
	}

	private void performAction( ISelection selection ) throws CoreException {
		IDebugTarget debugTarget = fTargetElement.getDebugTarget();
		String errorMessage = null;
		if ( fActivePart instanceof IEditorPart ) {
			IEditorPart editorPart = (IEditorPart)fActivePart;
			IEditorInput input = editorPart.getEditorInput();
			if ( input == null ) {
				errorMessage = ActionMessages.getString( "ResumeAtLineActionDelegate.Empty_editor_1" ); //$NON-NLS-1$
			}
			else {
				ITextEditor textEditor = (ITextEditor)editorPart;
				IDocument document = textEditor.getDocumentProvider().getDocument( input );
				if ( document == null ) {
					errorMessage = ActionMessages.getString( "ResumeAtLineActionDelegate.Missing_document" ); //$NON-NLS-1$
				}
				else {
					String fileName = getFileName( input );
					ITextSelection textSelection = (ITextSelection)selection;
					int lineNumber = textSelection.getStartLine() + 1;
					IJumpToLine jumpToLine = (IJumpToLine)((IAdaptable)debugTarget).getAdapter( IJumpToLine.class );
					if ( jumpToLine != null )
						jumpToLine.jumpToLine( fileName, lineNumber );
					return;
				}
			}
		}
		else if ( fActivePart instanceof DisassemblyView ) {
			IEditorInput input = ((DisassemblyView)fActivePart).getInput();
			if ( !(input instanceof DisassemblyEditorInput) ) {
				errorMessage = ActionMessages.getString( "ResumeAtLineActionDelegate.Empty_editor_1" ); //$NON-NLS-1$
			}
			else {
				ITextSelection textSelection = (ITextSelection)selection;
				int lineNumber = textSelection.getStartLine() + 1;
				IAddress address = ((DisassemblyEditorInput)input).getAddress( lineNumber );
				IJumpToAddress jumpToAddress = (IJumpToAddress)((IAdaptable)debugTarget).getAdapter( IJumpToAddress.class );
				if ( jumpToAddress != null )
					jumpToAddress.jumpToAddress( address );
				return;
			}
		}
		else {
			errorMessage = ActionMessages.getString( "ResumeAtLineActionDelegate.Operation_is_not_supported_1" ); //$NON-NLS-1$
		}
		throw new CoreException( new Status( IStatus.ERROR, CDebugUIPlugin.getUniqueIdentifier(), ICDebugUIConstants.INTERNAL_ERROR, errorMessage, null ) );
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
}
