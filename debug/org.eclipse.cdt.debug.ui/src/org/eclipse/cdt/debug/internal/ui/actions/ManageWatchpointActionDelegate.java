/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

/**
 * 
 * Action for adding/removing watchpoints at a selection in the active 
 * C/C++ or assembly editor.
 * 
 * @since Sep 4, 2002
 */
public class ManageWatchpointActionDelegate implements IWorkbenchWindowActionDelegate, 
													   IPartListener
{
	/**
	 * Constructor for ManageWatchpointActionDelegate.
	 */
	public ManageWatchpointActionDelegate()
	{
		super();
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part )
	{
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
	}
}
