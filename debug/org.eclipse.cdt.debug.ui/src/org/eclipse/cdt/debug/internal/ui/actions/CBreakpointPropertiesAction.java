/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * Presents a custom properties dialog to configure the attibutes of a C/C++ breakpoint.
 * 
 * @since Sep 3, 2002
 */
public class CBreakpointPropertiesAction implements IObjectActionDelegate
{
	private IWorkbenchPart fPart;
	private ICBreakpoint fBreakpoint;

	/**
	 * Constructor for CBreakpointPropertiesAction.
	 */
	public CBreakpointPropertiesAction()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
		fPart = targetPart;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		Dialog d = new CBreakpointPropertiesDialog( getActivePart().getSite().getShell(), getBreakpoint() );
		d.open();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if ( selection instanceof IStructuredSelection )
		{
			IStructuredSelection ss = (IStructuredSelection)selection;
			if ( ss.isEmpty() || ss.size() > 1 )
			{
				return;
			}
			Object element = ss.getFirstElement();
			if ( element instanceof ICBreakpoint )
			{
				setBreakpoint( (ICBreakpoint)element );
			}
		}
	}

	protected IWorkbenchPart getActivePart()
	{
		return fPart;
	}

	protected void setActivePart( IWorkbenchPart part )
	{
		fPart = part;
	}

	protected ICBreakpoint getBreakpoint()
	{
		return fBreakpoint;
	}

	protected void setBreakpoint( ICBreakpoint breakpoint )
	{
		fBreakpoint = breakpoint;
	}
}
