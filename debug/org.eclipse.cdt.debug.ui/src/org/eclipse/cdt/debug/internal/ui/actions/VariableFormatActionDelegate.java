/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.core.cdi.ICDIFormat;
import org.eclipse.cdt.debug.core.model.ICVariable;
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
 * 
 * Enter type comment.
 * 
 * @since Dec 16, 2002
 */
public class VariableFormatActionDelegate implements IObjectActionDelegate
{
	private int fFormat = ICDIFormat.DECIMAL;
	private ICVariable fVariable = null;

	/**
	 * Constructor for VariableFormatActionDelegate.
	 */
	public VariableFormatActionDelegate( int format )
	{
		fFormat = format;
	}

	/**
	 * @see org.eclipse.ui.IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
	 */
	public void setActivePart( IAction action, IWorkbenchPart targetPart )
	{
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		if ( getVariable() != null )
		{
			final MultiStatus ms = new MultiStatus( CDebugUIPlugin.getUniqueIdentifier(), 
													DebugException.REQUEST_FAILED, "", null ); 
			BusyIndicator.showWhile( Display.getCurrent(), 
									new Runnable()
										{
											public void run()
											{
												try
												{
													doAction( getVariable() );
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
					CDebugUIPlugin.errorDialog( "Unable to set format of variable.", ms );
				}
				else
				{
					CDebugUIPlugin.log( ms );
				}
			}

		}
	}

	/**
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if ( selection instanceof IStructuredSelection )
		{
			Object element = ((IStructuredSelection)selection).getFirstElement();
			if ( element instanceof ICVariable )
			{
				boolean enabled = enablesFor( (ICVariable)element );
				action.setEnabled( enabled );
				if ( enabled )
				{
					action.setChecked( ( ((ICVariable)element).getFormat() == fFormat ) );
					setVariable( (ICVariable)element );
					return;
				}
			}
		}
		action.setChecked( false );
		action.setEnabled( false );
		setVariable( null );
	}
	
	private boolean enablesFor( ICVariable var )
	{
		return var.isEditable();
	}
	
	private void setVariable( ICVariable var )
	{
		fVariable = var;
	}
	
	protected ICVariable getVariable()
	{
		return fVariable;
	}

	protected void doAction( ICVariable var ) throws DebugException
	{
		var.setFormat( fFormat );
		var.refresh();
	}
}
