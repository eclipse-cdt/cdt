/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.model.IResumeWithoutSignal;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Enter type comment.
 * 
 * @since: Feb 4, 2003
 */
public class SignalZeroObjectActionDelegate implements IObjectActionDelegate
{
	private IResumeWithoutSignal fTarget = null;

	/**
	 * Constructor for SignalZeroObjectActionDelegate.
	 */
	public SignalZeroObjectActionDelegate()
	{
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		if ( getTarget() != null )
		{
			final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(), 
													DebugException.REQUEST_FAILED, 
													CDebugUIPlugin.getResourceString("internal.ui.actions.SignalZeroObjectActionDelegate.Unable_to_resume_ignoring_signal"),  //$NON-NLS-1$
													null ); 
			BusyIndicator.showWhile( Display.getCurrent(), 
									new Runnable()
										{
											public void run()
											{
												try
												{
													doAction( getTarget() );
												}
												catch( DebugException e )
												{
													ms.merge( e.getStatus() );
												}
											}
										} );
			if ( !ms.isOK() )
			{
				IWorkbenchWindow window = CDebugUIPlugin.getActiveWorkbenchWindow();
				if ( window != null )
				{
					CDebugUIPlugin.errorDialog( CDebugUIPlugin.getResourceString("internal.ui.actions.SignalZeroObjectActionDelegate.Operation_failed"), ms ); //$NON-NLS-1$
				}
				else
				{
					CDebugUIPlugin.log( ms );
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if ( selection instanceof IStructuredSelection )
		{
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof IResumeWithoutSignal )
			{
				boolean enabled = ((IResumeWithoutSignal)element).canResumeWithoutSignal();
				action.setEnabled( enabled );
				if ( enabled )
				{
					setTarget( (IResumeWithoutSignal)element );
					return;
				}
			}
		}
		action.setEnabled( false );
		setTarget( null );
	}

	protected void doAction( IResumeWithoutSignal target ) throws DebugException
	{
		target.resumeWithoutSignal();
	}

	protected IResumeWithoutSignal getTarget()
	{
		return fTarget;
	}

	protected void setTarget( IResumeWithoutSignal target )
	{
		fTarget = target;
	}
}
