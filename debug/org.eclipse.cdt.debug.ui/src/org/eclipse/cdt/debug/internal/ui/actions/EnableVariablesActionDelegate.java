/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.actions;

import java.util.Iterator;

import org.eclipse.cdt.debug.core.model.ICVariable;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;

/**
 * Enter type comment.
 * 
 * @since Jun 19, 2003
 */
public class EnableVariablesActionDelegate implements IViewActionDelegate
{
	private IViewPart fView;

	private IAction fAction;

	public EnableVariablesActionDelegate()
	{
	}

	protected IViewPart getView()
	{
		return fView;
	}

	protected void setView( IViewPart view )
	{
		fView = view;
	}

	protected IAction getAction()
	{
		return fAction;
	}

	protected void setAction( IAction action )
	{
		fAction = action;
	}

	/**
	 * This action enables variables.
	 */
	protected boolean isEnableAction() 
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(org.eclipse.ui.IViewPart)
	 */
	public void init( IViewPart view )
	{
		setView(view);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
	 */
	public void run( IAction action )
	{
		IStructuredSelection selection = getSelection();
		final int size = selection.size();
		if ( size == 0 )
			return;

		final Iterator enum = selection.iterator();
		final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(), DebugException.REQUEST_FAILED, "Enable variable(s) failed.", null );
		Runnable runnable = new Runnable()
								{
									public void run()
									{
										while( enum.hasNext() )
										{
											ICVariable var = (ICVariable)enum.next();
											try
											{
												if ( size > 1 )
												{
													if ( isEnableAction() )
														var.setEnabled( true );
													else
														var.setEnabled( false );
												}
												else
													var.setEnabled( !var.isEnabled() );
											}
											catch( DebugException e )
											{
												ms.merge( e.getStatus() );
											}
										}
										update();
									}
								};

		final Display display = CDebugUIPlugin.getStandardDisplay();
		if ( display.isDisposed() )
			return;
		display.asyncExec( runnable );

		if ( !ms.isOK() )
		{
			CDebugUIPlugin.errorDialog( "Exceptions occurred enabling the variable(s).", ms );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(org.eclipse.jface.action.IAction, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		setAction( action );
		if ( !( selection instanceof IStructuredSelection ) )
			return;
		IStructuredSelection sel = (IStructuredSelection)selection;
		Object o = sel.getFirstElement();
		if ( !( o instanceof ICVariable ) )
			return;

		Iterator enum = sel.iterator();
		boolean allEnabled = true;
		boolean allDisabled = true;
		while( enum.hasNext() )
		{
			ICVariable var = (ICVariable)enum.next();
			if ( !var.canEnableDisable() )
				continue;
			if ( var.isEnabled() )
				allDisabled = false;
			else
				allEnabled = false;
		}

		if ( isEnableAction() )
			action.setEnabled( !allEnabled );
		else
			action.setEnabled( !allDisabled );
	}

	private IStructuredSelection getSelection()
	{
		return (IStructuredSelection)getView().getViewSite().getSelectionProvider().getSelection();
	}

	protected void update() 
	{
		getView().getViewSite().getSelectionProvider().setSelection( getView().getViewSite().getSelectionProvider().getSelection() );
	}
}
