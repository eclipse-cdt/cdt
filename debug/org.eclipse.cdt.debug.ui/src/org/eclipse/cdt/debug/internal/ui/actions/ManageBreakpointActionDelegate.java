/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.CDebugModel;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.jface.action.IAction;
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
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Action for adding/removing breakpoints at a line in the source file 
 * shown in the active C/C++ Editor.
 * 
 * @since Sep 3, 2002
 */
public class ManageBreakpointActionDelegate implements IWorkbenchWindowActionDelegate, 
													   IPartListener
{
	private boolean fInitialized = false;
	private IAction fAction = null;
	private int fLineNumber;
	private ITextEditor fTextEditor = null;
	private IWorkbenchWindow fWorkbenchWindow = null;
	private IFile fFile = null;

	/**
	 * Constructor for ManageBreakpointActionDelegate.
	 */
	public ManageBreakpointActionDelegate()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWindowActionDelegate#dispose()
	 */
	public void dispose()
	{
		getWorkbenchWindow().getPartService().removePartListener( this );
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		if ( getTextEditor() != null )
		{
			update();
			manageBreakpoint( getTextEditor().getEditorInput() );
		}
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
			if ( getTextEditor() != null )
			{
				breakpointExists( getTextEditor().getEditorInput() );
			}
			action.setEnabled( getTextEditor() != null && getFile() != null );
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
						update( getTextEditor().getSelectionProvider().getSelection() );
					}
				}
			}
			fInitialized = true;
		}
		update( selection );
	}

	/**
	 * Manages a breakpoint.
	 */
	protected void manageBreakpoint( IEditorInput editorInput )
	{
		ISelectionProvider sp = getTextEditor().getSelectionProvider();
		if ( sp == null || getFile() == null )
		{
			beep();
			return;
		}
		ISelection selection = sp.getSelection();
		if ( selection instanceof ITextSelection )
		{
			if ( getFile() == null )
				return;
			IDocument document = getTextEditor().getDocumentProvider().getDocument( editorInput );
			BreakpointLocationVerifier bv = new BreakpointLocationVerifier();
			int lineNumber = bv.getValidBreakpointLocation( document, ((ITextSelection)selection).getStartLine());
			if ( lineNumber > -1 )
			{
				try
				{
					ICLineBreakpoint breakpoint = CDebugModel.lineBreakpointExists( getFile().getLocation().toOSString(), lineNumber );
					if ( breakpoint != null )
					{
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint( breakpoint, true );
					}
					else
					{
						CDebugModel.createLineBreakpoint( getFile(),
														  lineNumber,
														  true,
														  0,
														  "",
														  true );
					}
				}
				catch( CoreException ce )
				{
					CDebugUIPlugin.errorDialog( "Cannot add breakpoint", ce );
				}
			}
		}
	}

	/**
	 * Determines if a breakpoint exists on the line of the current selection.
	 */
	protected boolean breakpointExists(IEditorInput editorInput)
	{
		if ( getFile() != null )
		{
			try
			{
				return CDebugModel.lineBreakpointExists( getFile().getLocation().toOSString(), getLineNumber() ) == null;
			}
			catch (CoreException ce)
			{
				CDebugUIPlugin.log( ce );
			}
		}

		return false;
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
			setFile( ( input != null && input instanceof IFileEditorInput ) ? ((IFileEditorInput)input).getFile() : null );
		}
		setEnabledState( editor );
	}

	protected void setEnabledState( ITextEditor editor )
	{
		if ( getAction() != null )
		{
			getAction().setEnabled( editor != null );
		}
	}

	protected void beep()
	{
		if ( CDebugUIPlugin.getActiveWorkbenchShell() != null )
		{
			CDebugUIPlugin.getActiveWorkbenchShell().getDisplay().beep();
		}
	}

	protected int getLineNumber()
	{
		return fLineNumber;
	}

	protected void setLineNumber( int lineNumber )
	{
		fLineNumber = lineNumber;
	}

	protected IAction getAction()
	{
		return fAction;
	}

	protected void setAction( IAction action )
	{
		fAction = action;
	}

	protected IFile getFile()
	{
		return fFile;
	}

	protected void setFile( IFile file ) 
	{
		fFile = file;
	}
}
