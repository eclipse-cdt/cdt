/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.views.registers;

import org.eclipse.cdt.debug.internal.ui.CDTDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.actions.ChangeRegisterValueAction;
import org.eclipse.cdt.debug.internal.ui.actions.ShowRegisterTypesAction;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IStackFrame;
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
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * 
 * This view shows registers and their values for a particular stack frame.
 * 
 * @since Jul 23, 2002
 */
public class RegistersView extends AbstractDebugEventHandlerView
						   implements ISelectionListener, 
									  IPropertyChangeListener, 
									  IDebugExceptionHandler
{
	/**
	 * The model presentation used as the label provider for the tree viewer.
	 */
	private CDTDebugModelPresentation fModelPresentation;

	protected static final String VARIABLES_SELECT_ALL_ACTION = SELECT_ALL_ACTION + ".Registers"; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer( Composite parent )
	{
		fModelPresentation = new CDTDebugModelPresentation();
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		
		// add tree viewer
		final TreeViewer vv = new RegistersViewer( parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
		vv.setContentProvider( createContentProvider() );
		vv.setLabelProvider( getModelPresentation() );
		vv.setUseHashlookup( true );
		setAction( SELECT_ALL_ACTION, getAction( VARIABLES_SELECT_ALL_ACTION ) );
		getViewSite().getActionBars().updateActionBars();

		// listen to selection in debug view
		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( createEventHandler( vv ) );

		return vv;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions()
	{
		IAction action = new ShowRegisterTypesAction( getStructuredViewer() );
		action.setChecked( CDebugUIPlugin.getDefault().getPreferenceStore().getBoolean( IDebugUIConstants.PREF_SHOW_TYPE_NAMES ) );
		setAction( "ShowTypeNames", action ); //$NON-NLS-1$

		action = new ChangeRegisterValueAction( getViewer() );
		action.setEnabled( false );
		setAction( "ChangeRegisterValue", action ); //$NON-NLS-1$
		setAction( DOUBLE_CLICK_ACTION, action );

		// set initial content here, as viewer has to be set
		setInitialContent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId()
	{
		return ICDebugHelpContextIds.REGISTERS_VIEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu( IMenuManager menu )
	{
		menu.add( new Separator( ICDebugUIConstants.EMPTY_REGISTER_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.REGISTER_GROUP ) );
		menu.add( getAction( "ChangeRegisterValue" ) ); //$NON-NLS-1$
		menu.add( new Separator( IDebugUIConstants.EMPTY_RENDER_GROUP ) );
		menu.add( new Separator( IDebugUIConstants.RENDER_GROUP ) );
		menu.add( getAction( "ShowTypeNames" ) ); //$NON-NLS-1$

		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar( IToolBarManager tbm )
	{
		tbm.add( new Separator( this.getClass().getName() ) );
		tbm.add( new Separator( IDebugUIConstants.RENDER_GROUP ) );
		tbm.add( getAction( "ShowTypeNames" ) ); //$NON-NLS-1$
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
		String propertyName= event.getProperty();
		if ( propertyName.equals( ICDebugPreferenceConstants.CHANGED_REGISTER_RGB ) ) 
		{
			getEventHandler().refresh();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler#handleException(DebugException)
	 */
	public void handleException( DebugException e )
	{
	}

	/**
	 * Remove myself as a selection listener
	 * and preference change listener.
	 *
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose()
	{
		getSite().getPage().removeSelectionListener( ICDebugUIConstants.ID_REGISTERS_VIEW, this );
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
		super.dispose();
	}

	/**
	 * Creates this view's content provider.
	 * 
	 * @return a content provider
	 */
	protected IContentProvider createContentProvider() 
	{
		RegistersViewContentProvider cp = new RegistersViewContentProvider();
		cp.setExceptionHandler( this );
		return cp;
	}

	protected IDebugModelPresentation getModelPresentation() 
	{
		if ( fModelPresentation == null ) 
		{
			fModelPresentation = new CDTDebugModelPresentation();
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
		return new RegistersViewEventHandler( this );
	}	

	protected void setViewerInput( IStructuredSelection ssel )
	{
		IStackFrame frame = null;
		if ( ssel.size() == 1 )
		{
			Object input = ssel.getFirstElement();
			if ( input instanceof IStackFrame )
			{
				frame = (IStackFrame)input;
			}
		}

		Object current = getViewer().getInput();
		if ( current == null && frame == null )
		{
			return;
		}

		if ( current != null && current.equals( frame ) )
		{
			return;
		}

		showViewer();
		getViewer().setInput( frame );
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
	}
}
