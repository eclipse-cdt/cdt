/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.resources.FileStorage;
import org.eclipse.cdt.debug.core.model.IJumpToAddress;
import org.eclipse.cdt.debug.core.model.IJumpToLine;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
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
import org.eclipse.ui.IStorageEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Enter type comment.
 * 
 * @since: Feb 5, 2003
 */
public class JumpToLineActionDelegate extends AbstractEditorActionDelegate
{
	/**
	 * Constructor for JumpToLineActionDelegate.
	 */
	public JumpToLineActionDelegate()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection )
	{
		IDebugTarget target = null;
		if ( part != null && part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) )
		{
			if ( selection instanceof IStructuredSelection )
			{
				Object element = ((IStructuredSelection)selection).getFirstElement();
				if ( element != null && element instanceof IDebugElement )
				{
					IDebugTarget target1 = ((IDebugElement)element).getDebugTarget();
					if ( target1 != null && 
						 ( target1 instanceof IJumpToLine || target1 instanceof IJumpToAddress ) )
					{
						target = target1;
					}
				}
			}
			setDebugTarget( target );
			update();
		}
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
					ITextSelection selection = (ITextSelection)((ITextEditor)getTargetPart()).getSelectionProvider().getSelection();
					int lineNumber = selection.getStartLine() + 1;
					jumpToLine( file, lineNumber );
				}
			}
			else if ( input != null && input instanceof IStorageEditorInput )
			{
				try
				{
					IStorage storage = ((IStorageEditorInput)input).getStorage();
					if ( storage instanceof FileStorage )
					{
						IPath path = storage.getFullPath();
						if ( path != null )
						{
							ITextSelection selection = (ITextSelection)((ITextEditor)getTargetPart()).getSelectionProvider().getSelection();
							int lineNumber = selection.getStartLine() + 1;
							jumpToLine( path.lastSegment(), lineNumber );
						}
					}
				}
				catch( CoreException e )
				{
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
			if ( target != null && 
				 ( target instanceof IJumpToLine || target instanceof IJumpToAddress ) )
			{
				setDebugTarget( target );
			}			
		}
	}

	protected void jumpToLine( IFile file, int lineNumber )
	{
		IJumpToLine target = (IJumpToLine)getDebugTarget().getAdapter( IJumpToLine.class );
		if ( target != null )
		{
			if ( !target.canJumpToLine( file, lineNumber ) )
			{
				getTargetPart().getSite().getShell().getDisplay().beep();
				return;
			}
			try
			{
				target.jumpToLine( file, lineNumber );
			}
			catch( DebugException e )
			{
				CDebugUIPlugin.errorDialog( e.getMessage(), e );
			}
		}
	}

	protected void jumpToLine( String fileName, int lineNumber )
	{
		IJumpToLine target = (IJumpToLine)getDebugTarget().getAdapter( IJumpToLine.class );
		if ( target != null )
		{
			if ( !target.canJumpToLine( fileName, lineNumber ) )
			{
				getTargetPart().getSite().getShell().getDisplay().beep();
				return;
			}
			try
			{
				target.jumpToLine( fileName, lineNumber );
			}
			catch( DebugException e )
			{
				CDebugUIPlugin.errorDialog( e.getMessage(), e );
			}
		}
	}

	protected void jumpToAddress( IAddress address )
	{
		IJumpToAddress target = (IJumpToAddress)getDebugTarget().getAdapter( IJumpToAddress.class );
		if ( target != null )
		{
			if ( !target.canJumpToAddress( address ) )
			{
				getTargetPart().getSite().getShell().getDisplay().beep();
				return;
			}
			try
			{
				target.jumpToAddress( address );
			}
			catch( DebugException e )
			{
				CDebugUIPlugin.errorDialog( e.getMessage(), e );
			}
		}
	}
}
