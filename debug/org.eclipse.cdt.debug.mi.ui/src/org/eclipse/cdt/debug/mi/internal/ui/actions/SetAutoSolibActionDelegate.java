/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.mi.internal.ui.actions;

import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * Enter type comment.
 * 
 * @since: Feb 11, 2003
 */
public class SetAutoSolibActionDelegate implements IViewActionDelegate, 
												   ISelectionListener, 
												   IPartListener
{
	private IViewPart fView = null;
	private IAction fAction;
	private IStructuredSelection fSelection;
	private IStatus fStatus = null;

	/**
	 * Constructor for SetAutoSolibActionDelegate.
	 */
	public SetAutoSolibActionDelegate()
	{
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewActionDelegate#init(IViewPart)
	 */
	public void init( IViewPart view )
	{
		fView = view;
		view.getSite().getPage().addPartListener( this );
		view.getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection )
	{
		if ( part.getSite().getId().equals( IDebugUIConstants.ID_DEBUG_VIEW ) )
		{
			if ( selection instanceof IStructuredSelection )
			{
				setSelection( (IStructuredSelection)selection );
			}
			else
			{
				setSelection( null );
			}
			update( getAction() );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		final IStructuredSelection selection = getSelection();
		if ( selection != null && selection.size() != 1 )
			return;
		BusyIndicator.showWhile( Display.getCurrent(), 
								 new Runnable() 
									 {
										 public void run() 
										 {
											 try 
											 {
												 doAction( selection.getFirstElement() );
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
				CDebugUIPlugin.errorDialog( getErrorDialogMessage(), getStatus() );
			} 
			else 
			{
				CDebugUIPlugin.log( getStatus() );
			}
		}		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		setAction( action );
		if ( getView() != null )
		{
			update( action );
		}
	}

	protected void update( IAction action )
	{
		if ( action != null )
		{
			action.setEnabled( getEnableStateForSelection( getSelection() ) );
			action.setChecked( getCheckStateForSelection( getSelection() ) );
		}
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
		if ( part.equals( getView() ) )
		{
			dispose();
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
	}
	
	protected IViewPart getView()
	{
		return fView;
	}

	protected void setView( IViewPart viewPart )
	{
		fView = viewPart;
	}
	
	protected void setAction( IAction action )
	{
		fAction = action;
	}

	protected IAction getAction()
	{
		return fAction;
	}

	private void setSelection( IStructuredSelection selection )
	{
		fSelection = selection;
	}

	private IStructuredSelection getSelection()
	{
		return fSelection;
	}

	protected void dispose()
	{
		if ( getView() != null ) 
		{
			getView().getViewSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
			getView().getViewSite().getPage().removePartListener( this );
		}	
	}

	protected boolean getCheckStateForSelection( IStructuredSelection selection )
	{
		if ( selection == null || selection.size() != 1 )
		{
			return false;
		}
		Object element = selection.getFirstElement();
		if ( element instanceof IDebugElement )
		{
			ICSharedLibraryManager slm = (ICSharedLibraryManager)((IDebugElement)element).getDebugTarget().getAdapter( ICSharedLibraryManager.class );
			if ( slm != null )
			{
				return slm.getAutoLoadSymbols();
			}
		}
		return false;
	}

	protected boolean getEnableStateForSelection( IStructuredSelection selection )
	{
		if ( selection == null || selection.size() != 1 )
		{
			return false;
		}
		Object element = selection.getFirstElement();
		return ( element instanceof IDebugElement && 
				 ((IDebugElement)element).getDebugTarget().isSuspended() &&
				 ((IDebugElement)element).getDebugTarget().getAdapter( ICSharedLibraryManager.class ) != null );
	}

	protected String getStatusMessage()
	{
		return "Exceptions occurred attempting to set 'Automaticaly Load Symbols' mode.";
	}

	/**
	 * @see AbstractDebugActionDelegate#getErrorDialogMessage()
	 */
	protected String getErrorDialogMessage()
	{
		return "Set 'Automatically Load Symbols' mode failed.";
	}
	
	protected void setStatus( IStatus status )
	{
		fStatus = status;
	}
	
	protected IStatus getStatus()
	{
		return fStatus;
	}

	protected void doAction( Object element ) throws DebugException
	{
		if ( getView() == null )
			return;
		if ( element instanceof IDebugElement )
		{
			ICSharedLibraryManager slm = (ICSharedLibraryManager)((IDebugElement)element).getDebugTarget().getAdapter( ICSharedLibraryManager.class );
			if ( slm != null && getAction() != null )
			{
				try
				{
					slm.setAutoLoadSymbols( getAction().isChecked() );
				}
				catch( DebugException e )
				{
					getAction().setChecked( slm.getAutoLoadSymbols() );
					throw e;
				}
			}
		}
	}
}
