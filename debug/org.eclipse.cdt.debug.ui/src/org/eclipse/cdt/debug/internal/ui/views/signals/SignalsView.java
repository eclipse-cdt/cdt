/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.core.ICSignalManager;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.CImageDescriptor;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Enter type comment.
 * 
 * @since: Jan 30, 2003
 */
public class SignalsView extends AbstractDebugEventHandlerView
						 implements ISelectionListener, 
						 			IPropertyChangeListener, 
						 			IDebugExceptionHandler
{
	/**
	 * Enter type comment.
	 * 
	 * @since: Jan 30, 2003
	 */
	public class SignalsViewLabelProvider extends LabelProvider implements ITableLabelProvider
	{
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object, int)
		 */
		public Image getColumnImage( Object element, int columnIndex )
		{
			if ( columnIndex == 0 )
				return CDebugUIPlugin.getImageDescriptorRegistry().get( new CImageDescriptor( CDebugImages.DESC_OBJS_SIGNAL,  0 ) );
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText( Object element, int columnIndex )
		{
			if ( element instanceof ICSignal )
			{
				switch( columnIndex )
				{
					case 0:
						return ((ICSignal)element).getName();
					case 1:
						return ( ((ICSignal)element).isPassEnabled() ) ? 
											SignalsViewer.YES_VALUE : SignalsViewer.NO_VALUE;
					case 2:
						return ( ((ICSignal)element).isStopEnabled() ) ? 
											SignalsViewer.YES_VALUE : SignalsViewer.NO_VALUE;
					case 3:
						return ((ICSignal)element).getDescription();
				}
			}
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer( Composite parent )
	{
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		
		// add tree viewer
		final SignalsViewer vv = new SignalsViewer( parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
		vv.setContentProvider( new SignalsViewContentProvider() );
		vv.setLabelProvider( new SignalsViewLabelProvider() );
		vv.setUseHashlookup( true );

		// listen to selection in debug view
		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( new SignalsViewEventHandler( this ) );

		return vv;
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
		return ICDebugHelpContextIds.SIGNALS_VIEW;
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
		ICSignalManager sm = null;
		if ( ssel != null && ssel.size() == 1 )
		{
			Object input = ssel.getFirstElement();
			if ( input instanceof IDebugElement )
			{
				sm = (ICSignalManager)((IDebugElement)input).getDebugTarget().getAdapter( ICSignalManager.class );
			}
		}

		Object current = getViewer().getInput();
		if ( current != null && current.equals( sm ) )
		{
			return;
		}
		showViewer();
		getViewer().setInput( sm );
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

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose()
	{
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
		SignalsViewContentProvider cp = new SignalsViewContentProvider();
		cp.setExceptionHandler( this );
		return cp;
	}
}
