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
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
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
	
	protected void runToLine( IResource resource, int lineNumber )
	{
		if ( !((IRunToLine)getDebugTarget()).canRunToLine( resource, lineNumber ) )
		{
			getTargetPart().getSite().getShell().getDisplay().beep();
			return;
		}
		try
		{
			((IRunToLine)getDebugTarget()).runToLine( resource, lineNumber );
		}
		catch( DebugException e )
		{
			CDebugUIPlugin.errorDialog( e.getMessage(), e );
		}
	}
}
