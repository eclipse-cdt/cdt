/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.actions.ActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Action for adding/removing watchpoints at a selection in the active 
 * C/C++ or assembly editor.
 * 
 * @since Sep 4, 2002
 */
public class AddWatchpointActionDelegate extends ActionDelegate
										 implements IWorkbenchWindowActionDelegate,
													IPartListener
{
	private boolean fInitialized = false;
	private IAction fAction = null;
	private ITextEditor fTextEditor = null;
	private IWorkbenchWindow fWorkbenchWindow = null;
	private IProject fProject = null;

	/**
	 * Constructor for AddWatchpointActionDelegate.
	 */
	public AddWatchpointActionDelegate()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#init(IWorkbenchWindow)
	 */
	public void init( IWorkbenchWindow window )
	{
		setWorkbenchWindow( window );
		window.getPartService().addPartListener( this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		String expression = getSelectedExpression();
		AddWatchpointDialog dlg = new AddWatchpointDialog( CDebugUIPlugin.getActiveWorkbenchShell(),
														   true, 
											  			   false,
														   expression );
		if ( dlg.open() != Dialog.OK )
			return;
		if ( getTextEditor() != null )
		{
			update();
			addWatchpoint( getTextEditor().getEditorInput(), 
						   dlg.getWriteAccess(),
						   dlg.getReadAccess(),
						   dlg.getExpression()  );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if ( !fInitialized )
		{
			setAction( action );
			if ( getWorkbenchWindow() != null )
			{
				IWorkbenchPage page = getWorkbenchWindow().getActivePage();
				if ( page != null )
				{
					IEditorPart part = page.getActiveEditor();
					if ( part instanceof ITextEditor )
					{
						setTextEditor( (ITextEditor)part );
					}
				}
			}
			fInitialized = true;
		}
	}

	protected IAction getAction()
	{
		return fAction;
	}

	protected void setAction( IAction action )
	{
		fAction = action;
	}

	protected IWorkbenchWindow getWorkbenchWindow()
	{
		return fWorkbenchWindow;
	}

	protected void setWorkbenchWindow( IWorkbenchWindow workbenchWindow )
	{
		fWorkbenchWindow = workbenchWindow;
	}

	protected ITextEditor getTextEditor()
	{
		return fTextEditor;
	}

	protected void setTextEditor( ITextEditor editor )
	{
		fTextEditor = editor;
		if ( fTextEditor != null )
		{
			IEditorInput input = fTextEditor.getEditorInput();
			IFile file = ( input != null && input instanceof IFileEditorInput ) ? ((IFileEditorInput)input).getFile() : null;
			setProject( ( file != null ) ? file.getProject() : null );
		}
		setEnabledState( editor );
	}
	
	protected String getSelectedExpression()
	{
		if ( getTextEditor() != null )
		{
			ISelectionProvider sp = getTextEditor().getSelectionProvider();
			if ( sp != null )
			{
				ISelection s = sp.getSelection();
				if ( s instanceof ITextSelection )
				{
					return ((ITextSelection)s).getText().trim();
				}
			}
		}
		return "";
	}

	protected void update( ISelection selection )
	{
		setEnabledState( getTextEditor() );
	}

	protected void update()
	{
		IAction action = getAction();
		if ( action != null )
		{
			action.setEnabled( getTextEditor() != null );
		}
	}

	protected void setEnabledState( ITextEditor editor )
	{
		if ( getAction() != null )
		{
			getAction().setEnabled( editor != null );
		}
	}

	protected IProject getProject()
	{
		return fProject;
	}

	protected void setProject( IProject project ) 
	{
		fProject = project;
	}
	
	protected void addWatchpoint( IEditorInput editorInput, boolean write, boolean read, String expression )
	{
		if ( getProject() == null )
			return;
		IDocument document = getTextEditor().getDocumentProvider().getDocument( editorInput );
		WatchpointExpressionVerifier wev = new WatchpointExpressionVerifier();
		if ( wev.isValidExpression( document, expression ) )
		{
			try
			{
				CDebugModel.createWatchpoint( getProject(),
											  write,
											  read,
											  expression,
											  true,
											  0,
											  "",
											  true );
			}
			catch( CoreException ce )
			{
				CDebugUIPlugin.errorDialog( "Cannot add watchpoint", ce );
			}
		}
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part )
	{
		if ( part instanceof ITextEditor )
		{
			setTextEditor( (ITextEditor)part );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop( IWorkbenchPart part )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed( IWorkbenchPart part )
	{
		if ( part == getTextEditor() )
		{
			setTextEditor( null );
			if ( getAction() != null )
			{
				getAction().setEnabled( false );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened( IWorkbenchPart part )
	{
		if ( part instanceof ITextEditor )
		{
			if ( getTextEditor() == null )
			{
				setTextEditor( (ITextEditor)part );
			}
		}
	}
}
