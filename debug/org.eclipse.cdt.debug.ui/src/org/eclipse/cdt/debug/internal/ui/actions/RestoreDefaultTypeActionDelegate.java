/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.ICastToType;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.actions.ActionDelegate;

/**
 * Enter type comment.
 * 
 * @since Mar 9, 2003
 */
public class RestoreDefaultTypeActionDelegate extends ActionDelegate
											  implements IObjectActionDelegate
{
	private ICastToType fCastToType = null;
	private IStatus fStatus = null;

	/**
	 * 
	 */
	public RestoreDefaultTypeActionDelegate()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(org.eclipse.jface.action.IAction, org.eclipse.ui.IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
	}

	protected ICastToType getCastToType()
	{
		return fCastToType;
	}

	protected void setCastToType( ICastToType castToType )
	{
		fCastToType = castToType;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action )
	{
		if ( getCastToType() == null )
			return;
		BusyIndicator.showWhile( Display.getCurrent(), 
								 new Runnable() 
									 {
										 public void run() 
										 {
											 try 
											 {
												 doAction( getCastToType() );
												 setStatus( null );
											 } 
											 catch( DebugException e ) 
											 {
												setStatus( e.getStatus() );
											 }
										 }
									 } );
		if ( getStatus() != null && !getStatus().isOK() ) 
		{
			IWorkbenchWindow window= CDebugUIPlugin.getActiveWorkbenchWindow();
			if ( window != null ) 
			{
				CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString("internal.ui.actions.RestoreDefaultTypeActionDelegate.Unable_to_cast_type"), getStatus() ); //$NON-NLS-1$
			} 
			else 
			{
				CDebugUIPlugin.log( getStatus() );
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if ( selection instanceof IStructuredSelection )
		{
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof ICastToType )
			{
				boolean enabled = ((ICastToType)element).isCasted();
				action.setEnabled( enabled );
				if ( enabled )
				{
					setCastToType( (ICastToType)element );
					return;
				}
			}
		}
		action.setEnabled( false );
		setCastToType( null );
	}

	public IStatus getStatus()
	{
		return fStatus;
	}

	public void setStatus( IStatus status )
	{
		fStatus = status;
	}

	protected void doAction( ICastToType castToType ) throws DebugException
	{
		castToType.restoreDefault();
	}
}
