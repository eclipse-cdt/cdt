/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.IRunToLine;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * 
 * Enter type comment.
 * 
 * @since Sep 19, 2002
 */
public class RunToLineActionDelegate extends AbstractEditorActionDelegate
{
	/**
	 * Constructor for RunToLineActionDelegate.
	 */
	public RunToLineActionDelegate()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		if ( getTargetPart() != null && getTargetPart() instanceof ITextEditor )
		{
			IEditorInput input = ((ITextEditor)getTargetPart()).getEditorInput();
			if ( input != null && input instanceof IFileEditorInput )
			{
				IFile file = ((IFileEditorInput)input).getFile();
				if ( file != null )
				{
					ITextSelection selection= (ITextSelection)((ITextEditor)getTargetPart()).getSelectionProvider().getSelection();
					int lineNumber = selection.getStartLine() + 1;
					runToLine( file, lineNumber );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.actions.AbstractEditorActionDelegate#initializeDebugTarget()
	 */
	protected void initializeDebugTarget()
	{
		setDebugTarget( null );
		IAdaptable context = DebugUITools.getDebugContext();
		if ( context != null && context instanceof IDebugElement )
		{
			IDebugTarget target = ((IDebugElement)context).getDebugTarget();
			if ( target != null && target instanceof IRunToLine )
			{
				setDebugTarget( target );
			}			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection )
	{
		IDebugTarget target = null;
		if ( part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) )
		{
			if ( selection != null && selection instanceof IStructuredSelection )
			{
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if ( element != null && element instanceof IDebugElement )
				{
					IDebugTarget target1 = ((IDebugElement)element).getDebugTarget();
					if ( target1 != null && target1 instanceof IRunToLine )
					{
						target = target1;
					}
				}
			}
			setDebugTarget( target );
			update();
		}
	}

	protected void runToLine( IResource resource, int lineNumber )
	{
		IRunToLine target = (IRunToLine)getDebugTarget().getAdapter( IRunToLine.class );
		if ( target != null )
		{
			if ( !target.canRunToLine( resource, lineNumber ) )
			{
				getTargetPart().getSite().getShell().getDisplay().beep();
				return;
			}
			try
			{
				target.runToLine( resource, lineNumber );
			}
			catch( DebugException e )
			{
				CDebugUIPlugin.errorDialog( e.getMessage(), e );
			}
		}
	}
}
