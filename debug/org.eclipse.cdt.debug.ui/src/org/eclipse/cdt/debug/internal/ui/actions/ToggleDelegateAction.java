/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.actions;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * A generic Toggle view action delegate, meant to be subclassed to provide
 * a specific filter.
 * 
 * @since Oct 4, 2002
 */
public abstract class ToggleDelegateAction implements IViewActionDelegate, 
													  IPropertyChangeListener, 
													  IPartListener
{
	/**
	 * The viewer that this action works for
	 */
	private StructuredViewer fViewer;

	private IViewPart fView;

	protected String fId = ""; //$NON-NLS-1$

	private IAction fAction;
	private boolean fNeedsInitialization = true;

	protected void dispose()
	{
		if ( fView != null )
		{
			fView.getViewSite().getPage().removePartListener( this );
		}
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
	}

	/**
	 * @see IViewActionDelegate#init(IViewPart)
	 */
	public void init( IViewPart view )
	{
		setView( view );
		initActionId();
		IDebugView adapter = (IDebugView)view.getAdapter( IDebugView.class );
		if ( adapter != null && adapter.getViewer() instanceof StructuredViewer )
		{
			setViewer( (StructuredViewer)adapter.getViewer() );
		}
		view.getViewSite().getPage().addPartListener( this );
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
	}

	protected abstract void initActionId();

	/**
	 * @see IActionDelegate#run(IAction)
	 */
	public void run( IAction action )
	{
		//do nothing.."run" will occur from the property change
		//this allows for setting the checked state of the IAction
		//to drive the execution of this delegate.
		//see propertyChange(PropertyChangeEvent)
	}

	protected abstract void valueChanged( boolean on );

	protected String getActionId()
	{
		return fId;
	}

	protected StructuredViewer getViewer()
	{
		return fViewer;
	}

	protected void setViewer( StructuredViewer viewer )
	{
		fViewer = viewer;
	}

	/**
	 * @see IActionDelegate#selectionChanged(IAction, ISelection)
	 */
	public void selectionChanged( IAction action, ISelection selection )
	{
		if ( fNeedsInitialization )
		{
			setAction( action );
			action.setId( getActionId() );
			fNeedsInitialization = false;
		}
	}

	protected IAction getAction()
	{
		return fAction;
	}

	protected void setAction( IAction action )
	{
		fAction = action;
		action.addPropertyChangeListener( this );
	}

	/**
	 * @see IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event )
	{
		if ( event.getProperty().equals( getActionId() ) )
		{
			getAction().setChecked( CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( getActionId() ) );
		}
		else if ( event.getProperty().equals( IAction.CHECKED ) )
		{
			CDebugUIPlugin.getDefault().getPreferenceStore().setValue( getActionId(), getAction().isChecked() );
			valueChanged( getAction().isChecked() );
		}
	}
	
	/**
	 * @see IPartListener#partActivated(IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part )
	{
	}

	/**
	 * @see IPartListener#partBroughtToTop(IWorkbenchPart)
	 */
	public void partBroughtToTop( IWorkbenchPart part )
	{
	}

	/**
	 * @see IPartListener#partClosed(IWorkbenchPart)
	 */
	public void partClosed( IWorkbenchPart part )
	{
		if ( part.equals( getView() ) )
		{
			dispose();
		}
	}

	/**
	 * @see IPartListener#partDeactivated(IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part )
	{
	}

	/**
	 * @see IPartListener#partOpened(IWorkbenchPart)
	 */
	public void partOpened( IWorkbenchPart part )
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
}
