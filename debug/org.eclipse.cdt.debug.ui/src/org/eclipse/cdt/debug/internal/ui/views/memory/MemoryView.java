/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.memory;

import org.eclipse.cdt.debug.core.ICMemoryManager;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.actions.AutoRefreshMemoryAction;
import org.eclipse.cdt.debug.internal.ui.actions.ClearMemoryAction;
import org.eclipse.cdt.debug.internal.ui.actions.RefreshMemoryAction;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * This view shows the content of the memory blocks associated 
 * with the selected debug target.
 * 
 * @since Jul 24, 2002
 */
public class MemoryView extends AbstractDebugEventHandlerView
						implements ISelectionListener, 
								   IPropertyChangeListener, 
								   IDebugExceptionHandler
{
	private IDebugModelPresentation fModelPresentation = null;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer( Composite parent )
	{
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		final MemoryViewer viewer = new MemoryViewer( parent, this );
		viewer.setContentProvider( createContentProvider() );
		viewer.setLabelProvider( getModelPresentation() );

		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( createEventHandler( viewer ) );

		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions()
	{
		IAction action = new RefreshMemoryAction( (MemoryViewer)getViewer() );
		action.setEnabled( false );
		setAction( "RefreshMemory", action ); //$NON-NLS-1$
		add( (RefreshMemoryAction)action );

		action = new AutoRefreshMemoryAction( (MemoryViewer)getViewer() );
		action.setChecked( false );
		setAction( "AutoRefreshMemory", action ); //$NON-NLS-1$
		add( (AutoRefreshMemoryAction)action );

		action = new ClearMemoryAction( (MemoryViewer)getViewer() );
		action.setEnabled( false );
		setAction( "ClearMemory", action ); //$NON-NLS-1$
		add( (ClearMemoryAction)action );

		// set initial content here, as viewer has to be set
		setInitialContent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId()
	{
		return ICDebugHelpContextIds.MEMORY_VIEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu( IMenuManager menu )
	{
		menu.add( new Separator( ICDebugUIConstants.EMPTY_MEMORY_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.MEMORY_GROUP ) );
		menu.add( getAction( "AutoRefreshMemory" ) ); //$NON-NLS-1$
		menu.add( getAction( "RefreshMemory" ) ); //$NON-NLS-1$
		menu.add( getAction( "ClearMemory" ) ); //$NON-NLS-1$

		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar( IToolBarManager tbm )
	{
		tbm.add( new Separator( this.getClass().getName() ) );
		tbm.add( new Separator( ICDebugUIConstants.MEMORY_GROUP ) );
		tbm.add( getAction( "AutoRefreshMemory" ) ); //$NON-NLS-1$
		tbm.add( getAction( "RefreshMemory" ) ); //$NON-NLS-1$
		tbm.add( getAction( "ClearMemory" ) ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection )
	{
		if ( selection instanceof IStructuredSelection ) 
		{
			setViewerInput( (IStructuredSelection)selection );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event )
	{
		((MemoryViewer)getViewer()).propertyChange( event ); 
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler#handleException(DebugException)
	 */
	public void handleException( DebugException e )
	{
	}

	/**
	 * Remove myself as a selection listener and preference change 
	 * listener.
	 *
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose()
	{
		remove( (ClearMemoryAction)getAction( "ClearMemory" ) );
		remove( (RefreshMemoryAction)getAction( "RefreshMemory" ) );
		remove( (AutoRefreshMemoryAction)getAction( "AutoRefreshMemory" ) );
		getSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
		super.dispose();
	}

	protected void setViewerInput( IStructuredSelection ssel )
	{
		ICMemoryManager mm = null;
		if ( ssel != null && ssel.size() == 1 )
		{
			Object input = ssel.getFirstElement();
			if ( input instanceof IDebugElement )
			{
				mm = (ICMemoryManager)((IDebugElement)input).getDebugTarget().getAdapter( ICMemoryManager.class );
			}
		}

		Object current = getViewer().getInput();
		if ( current != null && current.equals( mm ) )
		{
			return;
		}
		showViewer();
		getViewer().setInput( mm );
		updateObjects();
	}
	
	private IContentProvider createContentProvider()
	{
		return new MemoryViewContentProvider();
	}

	private IDebugModelPresentation getModelPresentation()
	{
		if ( fModelPresentation == null )
		{
			fModelPresentation = CDebugUIPlugin.getDebugModelPresentation();
		}
		return fModelPresentation;
	}

	/**
	 * Creates this view's event handler.
	 * 
	 * @param viewer the viewer associated with this view
	 * @return an event handler
	 */
	protected AbstractDebugEventHandler createEventHandler( Viewer viewer ) 
	{
		return new MemoryViewEventHandler( this );
	}	

	/**
	 * Initializes the viewer input on creation
	 */
	protected void setInitialContent()
	{
		ISelection selection =
			getSite().getPage().getSelection( IDebugUIConstants.ID_DEBUG_VIEW );
		if ( selection instanceof IStructuredSelection && !selection.isEmpty() )
		{
			setViewerInput( (IStructuredSelection)selection );
		}
		else
		{
			setViewerInput( null );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createContextMenu(Control)
	 */
	protected void createContextMenu( Control menuControl )
	{
		CTabItem[] items = ((MemoryViewer)getViewer()).getTabFolder().getItems();
		for ( int i = 0; i < items.length; ++i )
		{
			super.createContextMenu( ((MemoryControlArea)items[i].getControl()).getMemoryText().getControl() );
		}
	}
}
