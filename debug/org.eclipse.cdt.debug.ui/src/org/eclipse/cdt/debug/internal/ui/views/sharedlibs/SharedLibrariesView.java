/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.sharedlibs;

import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Enter type comment.
 * 
 * @since: Jan 16, 2003
 */
public class SharedLibrariesView extends AbstractDebugView
								 implements ISelectionListener, 
								 			IPropertyChangeListener, 
								 			IDebugExceptionHandler
{
	/**
	 * Event handler for this view
	 */
	private SharedLibrariesViewEventHandler fEventHandler;

	/**
	 * The model presentation used as the label provider for the tree viewer.
	 */
	private IDebugModelPresentation fModelPresentation;

	/**
	 * Sets the event handler for this view
	 * 
	 * @param eventHandler event handler
	 */
	protected void setEventHandler( SharedLibrariesViewEventHandler eventHandler )
	{
		fEventHandler = eventHandler;
	}

	/**
	 * Returns the event handler for this view
	 * 
	 * @return The event handler for this view
	 */
	protected SharedLibrariesViewEventHandler getEventHandler()
	{
		return fEventHandler;
	}

	/**
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose()
	{
		super.dispose();
		if ( getEventHandler() != null )
		{
			getEventHandler().dispose();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer( Composite parent )
	{
		TableViewer viewer = new TableViewer( parent, SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL );
		viewer.setContentProvider( new SharedLibrariesViewContentProvider() );
		viewer.setLabelProvider( getModelPresentation() );

		// listen to selection in debug view
		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( createEventHandler( viewer ) );
		
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions()
	{
		// set initial content here, as viewer has to be set
		setInitialContent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId()
	{
		return ICDebugHelpContextIds.SHARED_LIBRARIES_VIEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu( IMenuManager menu )
	{
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar( IToolBarManager tbm )
	{
		tbm.add( new Separator( this.getClass().getName() ) );
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
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler#handleException(DebugException)
	 */
	public void handleException( DebugException e )
	{
	}

	protected void setViewerInput( IStructuredSelection ssel )
	{
		ICSharedLibraryManager slm = null;
		if ( ssel != null && ssel.size() == 1 )
		{
			Object input = ssel.getFirstElement();
			if ( input instanceof IDebugElement )
			{
				slm = (ICSharedLibraryManager)((IDebugElement)input).getDebugTarget().getAdapter( ICSharedLibraryManager.class );
			}
		}

		Object current = getViewer().getInput();
		if ( current != null && current.equals( slm ) )
		{
			return;
		}
		showViewer();
		getViewer().setInput( slm );
		updateObjects();
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

	/**
	 * Creates this view's event handler.
	 * 
	 * @param viewer the viewer associated with this view
	 * @return an event handler
	 */
	protected SharedLibrariesViewEventHandler createEventHandler( Viewer viewer ) 
	{
		return new SharedLibrariesViewEventHandler( this );
	}	

	protected IDebugModelPresentation getModelPresentation() 
	{
		if ( fModelPresentation == null ) 
		{
			fModelPresentation = DebugUITools.newDebugModelPresentation();
		}
		return fModelPresentation;
	}
}
