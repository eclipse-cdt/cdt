/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */

package org.eclipse.cdt.debug.internal.ui.views.registers;

import java.util.HashMap;

import org.eclipse.cdt.debug.core.ICRegisterManager;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.actions.AutoRefreshAction;
import org.eclipse.cdt.debug.internal.ui.actions.ChangeRegisterValueAction;
import org.eclipse.cdt.debug.internal.ui.actions.RefreshAction;
import org.eclipse.cdt.debug.internal.ui.actions.ShowRegisterTypesAction;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.internal.ui.views.ViewerState;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegister;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.help.WorkbenchHelp;

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
	 * A label provider that delegates to a debug model
	 * presentation and adds coloring to registers to
	 * reflect their changed state
	 */
	class VariablesViewLabelProvider implements ILabelProvider, IColorProvider
	{
		private IDebugModelPresentation fPresentation;

		public VariablesViewLabelProvider( IDebugModelPresentation presentation )
		{
			fPresentation = presentation;
		}

		public IDebugModelPresentation getPresentation()
		{
			return fPresentation;
		}

		public Image getImage( Object element )
		{
			return fPresentation.getImage( element );
		}
		public String getText( Object element )
		{
			return fPresentation.getText( element );
		}
		public void addListener( ILabelProviderListener listener )
		{
			fPresentation.addListener( listener );
		}
		public void dispose()
		{
			fPresentation.dispose();
		}
		public boolean isLabelProperty( Object element, String property )
		{
			return fPresentation.isLabelProperty( element, property );
		}
		public void removeListener( ILabelProviderListener listener )
		{
			fPresentation.removeListener( listener );
		}

		public Color getForeground( Object element )
		{
			if ( element instanceof IRegister )
			{
				IRegister register = (IRegister)element;
				try
				{
					if ( register.hasValueChanged() )
					{
						return CDebugUIPlugin.getPreferenceColor( ICDebugPreferenceConstants.CHANGED_REGISTER_RGB );
					}
				}
				catch( DebugException e )
				{
					CDebugUIPlugin.log( e );
				}
			}
			return null;
		}

		public Color getBackground( Object element )
		{
			return null;
		}
	}

	/**
	 * The model presentation used as the label provider for the tree viewer.
	 */
	private IDebugModelPresentation fModelPresentation;

	protected static final String VARIABLES_SELECT_ALL_ACTION = SELECT_ALL_ACTION + ".Registers"; //$NON-NLS-1$

	/**
	 * A map of register managers to <code>ViewerState</code>s.
	 * Used to restore the expanded state of the registers view on
	 * re-selection of the register manager. The cache is cleared on
	 * a frame by frame basis when a thread/target is terminated.
	 */
	private HashMap fExpandedRegisters = new HashMap( 10 );

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer( Composite parent )
	{
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		
		// add tree viewer
		final TreeViewer vv = new RegistersViewer( parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
		vv.setContentProvider( createContentProvider() );
		vv.setLabelProvider( new VariablesViewLabelProvider( getModelPresentation() ) );
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
		setAction( "ShowTypeNames", action ); //$NON-NLS-1$

		action = new ChangeRegisterValueAction( getViewer() );
		action.setEnabled( false );
		setAction( "ChangeRegisterValue", action ); //$NON-NLS-1$
		setAction( DOUBLE_CLICK_ACTION, action );

		action = new AutoRefreshAction( getViewer(), "Auto-Refresh" );
		CDebugImages.setLocalImageDescriptors( action, CDebugImages.IMG_LCL_AUTO_REFRESH );
		action.setDescription( "Automatically Refresh Registers View" );
		action.setToolTipText( "Auto-Refresh" );
		WorkbenchHelp.setHelp( action, ICDebugHelpContextIds.AUTO_REFRESH_REGISTERS_ACTION );
		action.setEnabled( false );
		setAction( "AutoRefresh", action ); //$NON-NLS-1$
		add( (AutoRefreshAction)action );

		action = new RefreshAction( getViewer(), "Refresh" );
		CDebugImages.setLocalImageDescriptors( action, CDebugImages.IMG_LCL_REFRESH );
		action.setDescription( "Refresh Registers View" );
		action.setToolTipText( "Refresh" );
		WorkbenchHelp.setHelp( action, ICDebugHelpContextIds.REFRESH_REGISTERS_ACTION );
		action.setEnabled( false );
		setAction( "Refresh", action ); //$NON-NLS-1$
		add( (RefreshAction)action );

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

		menu.add( new Separator( IDebugUIConstants.EMPTY_RENDER_GROUP ) );
		menu.add( new Separator( IDebugUIConstants.RENDER_GROUP ) );

		menu.add( new Separator( ICDebugUIConstants.EMPTY_REFRESH_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.REFRESH_GROUP ) );

		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );

		menu.appendToGroup( ICDebugUIConstants.REGISTER_GROUP, getAction( "ChangeRegisterValue" ) ); //$NON-NLS-1$
		menu.appendToGroup( IDebugUIConstants.RENDER_GROUP, getAction( "ShowTypeNames" ) ); //$NON-NLS-1$
		menu.appendToGroup( ICDebugUIConstants.REFRESH_GROUP, getAction( "AutoRefresh" ) ); //$NON-NLS-1$
		menu.appendToGroup( ICDebugUIConstants.REFRESH_GROUP, getAction( "Refresh" ) ); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar( IToolBarManager tbm )
	{
		tbm.add( new Separator( this.getClass().getName() ) );

		tbm.add( new Separator( ICDebugUIConstants.REFRESH_GROUP ) );
		tbm.add( getAction( "AutoRefresh" ) ); //$NON-NLS-1$
		tbm.add( getAction( "Refresh" ) ); //$NON-NLS-1$

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
		showMessage( e.getMessage() );
	}

	/**
	 * Remove myself as a selection listener
	 * and preference change listener.
	 *
	 * @see IWorkbenchPart#dispose()
	 */
	public void dispose()
	{
		fModelPresentation.dispose();
		getSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
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
			fModelPresentation = DebugUITools.newDebugModelPresentation();
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
		ICRegisterManager rm = null;
		if ( ssel.size() == 1 && ssel.getFirstElement() instanceof IStackFrame )
		{
			rm = (ICRegisterManager)((IStackFrame)ssel.getFirstElement()).getDebugTarget().getAdapter( ICRegisterManager.class );
		}

		if ( getViewer() == null )
		{
			return;
		}

		Object current = getViewer().getInput();
		if ( current == null && rm == null )
		{
			return;
		}

		if ( current != null && current.equals( rm ) )
		{
			return;
		}

		if ( current != null )
		{
			// save state
			ViewerState state = new ViewerState( getRegistersViewer() );
			fExpandedRegisters.put( current, state );
		}

		showViewer();
		getViewer().setInput( rm );

		// restore state
		if ( rm != null ) 
		{
			ViewerState state = (ViewerState)fExpandedRegisters.get( rm );
			if ( state != null ) 
			{
				state.restoreState( getRegistersViewer() );
			}
		}

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
	}
	
	protected RegistersViewer getRegistersViewer()
	{
		return (RegistersViewer)getViewer();
	}

	protected void clearExpandedRegisters( ICRegisterManager rm ) 
	{
		fExpandedRegisters.remove( rm );
	}
}
