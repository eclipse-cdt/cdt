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
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action for adding a watchpoint at a selection in the active 
 * C/C++ or assembly editor.
 */
public class AddWatchpointActionDelegate extends ActionDelegate implements IWorkbenchWindowActionDelegate, IPartListener {

	private boolean fInitialized = false;

	private IAction fAction = null;

	private ITextEditor fTextEditor = null;

	private IWorkbenchWindow fWorkbenchWindow = null;

	private IResource fResource = null;

	private String fSourceHandle = ""; //$NON-NLS-1$

	/**
	 * Constructor for AddWatchpointActionDelegate.
	 */
	public AddWatchpointActionDelegate() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init( IWorkbenchWindow window ) {
		setWorkbenchWindow( window );
		window.getPartService().addPartListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action ) {
		String expression = getSelectedExpression();
		AddWatchpointDialog dlg = new AddWatchpointDialog( CDebugUIPlugin.getActiveWorkbenchShell(), true, false, expression, true );
		if ( dlg.open() != Window.OK )
			return;
		if ( getTextEditor() != null ) {
			update();
			addWatchpoint( getTextEditor().getEditorInput(), dlg.getWriteAccess(), dlg.getReadAccess(), dlg.getExpression() );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection ) {
		if ( !fInitialized ) {
			setAction( action );
			if ( getWorkbenchWindow() != null ) {
				IWorkbenchPage page = getWorkbenchWindow().getActivePage();
				if ( page != null ) {
					IEditorPart part = page.getActiveEditor();
					if ( part instanceof ITextEditor ) {
						setTextEditor( (ITextEditor)part );
					}
				}
			}
			fInitialized = true;
		}
	}

	protected IAction getAction() {
		return fAction;
	}

	protected void setAction( IAction action ) {
		fAction = action;
	}

	protected IWorkbenchWindow getWorkbenchWindow() {
		return fWorkbenchWindow;
	}

	protected void setWorkbenchWindow( IWorkbenchWindow workbenchWindow ) {
		fWorkbenchWindow = workbenchWindow;
	}

	protected ITextEditor getTextEditor() {
		return fTextEditor;
	}

	protected void setTextEditor( ITextEditor editor ) {
		fTextEditor = editor;
		if ( fTextEditor != null ) {
			IEditorInput input = fTextEditor.getEditorInput();
			setSourceHandle( input );
			setResource( input );
		}
		setEnabledState( editor );
	}

	protected String getSelectedExpression() {
		if ( getTextEditor() != null ) {
			ISelectionProvider sp = getTextEditor().getSelectionProvider();
			if ( sp != null ) {
				ISelection s = sp.getSelection();
				if ( s instanceof ITextSelection ) {
					return ((ITextSelection)s).getText().trim();
				}
			}
		}
		return ""; //$NON-NLS-1$
	}

	protected void update( ISelection selection ) {
		setEnabledState( getTextEditor() );
	}

	protected void update() {
		IAction action = getAction();
		if ( action != null ) {
			action.setEnabled( getTextEditor() != null );
		}
	}

	protected void setEnabledState( ITextEditor editor ) {
		if ( getAction() != null ) {
			getAction().setEnabled( editor != null );
		}
	}

	protected IResource getResource() {
		return fResource;
	}

	protected void setResource( IEditorInput input ) {
		if ( input instanceof IFileEditorInput ) {
			fResource = ((IFileEditorInput)input).getFile().getProject();
		}
		else {
			fResource = ResourcesPlugin.getWorkspace().getRoot();
		}
	}

	protected void addWatchpoint( IEditorInput editorInput, boolean write, boolean read, String expression ) {
		if ( getResource() == null )
			return;
		IDocument document = getTextEditor().getDocumentProvider().getDocument( editorInput );
		WatchpointExpressionVerifier wev = new WatchpointExpressionVerifier();
		if ( wev.isValidExpression( document, expression ) ) {
			try {
				CDIDebugModel.createWatchpoint( getSourceHandle(), getResource(), write, read, expression, true, 0, "", true ); //$NON-NLS-1$
			}
			catch( CoreException ce ) {
				CDebugUIPlugin.errorDialog( ActionMessages.getString( "AddWatchpointActionDelegate.0" ), ce ); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part ) {
		if ( part instanceof ITextEditor ) {
			setTextEditor( (ITextEditor)part );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop( IWorkbenchPart part ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed( IWorkbenchPart part ) {
		if ( part == getTextEditor() ) {
			setTextEditor( null );
			if ( getAction() != null ) {
				getAction().setEnabled( false );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened( IWorkbenchPart part ) {
		if ( part instanceof ITextEditor ) {
			if ( getTextEditor() == null ) {
				setTextEditor( (ITextEditor)part );
			}
		}
	}

	private String getSourceHandle() {
		return fSourceHandle;
	}
	
	private void setSourceHandle( IEditorInput input ) {
		fSourceHandle = ""; //$NON-NLS-1$
		if ( input instanceof IFileEditorInput ) {
			fSourceHandle = ((IFileEditorInput)input).getFile().getFullPath().toOSString();
		}
		else if ( input instanceof IStorageEditorInput ) {
			try {
				IPath path = ((IStorageEditorInput)input).getStorage().getFullPath();
				if ( path != null )
					fSourceHandle = path.toOSString();
			}
			catch( CoreException e ) {
			}
		}
	}
}