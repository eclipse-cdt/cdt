/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.sharedlibs;

import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
import org.eclipse.cdt.debug.internal.ui.CDTDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Enter type comment.
 * 
 * @since: Jan 21, 2003
 */
public class SharedLibrariesView extends AbstractDebugEventHandlerView
								 implements ISelectionListener, 
								 			IPropertyChangeListener, 
								 			IDebugExceptionHandler
{
	public class SharedLibrariesLabelProvider extends CDTDebugModelPresentation implements ITableLabelProvider
	{
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage( Object element, int columnIndex )
		{
			if ( element instanceof ICSharedLibrary && columnIndex == 1 )
			{
				return getImage( element );
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText( Object element, int columnIndex )
		{
			if ( element instanceof ICSharedLibrary )
			{
				ICSharedLibrary library = (ICSharedLibrary)element;
				switch( columnIndex )
				{
					case 0:
						return "";
					case 1:
						return getText( element );
					case 2:
						return ( library.getStartAddress() > 0 ) ? 
									CDebugUtils.toHexAddressString( library.getStartAddress() ) : "";
					case 3:
						return ( library.getEndAddress() > 0 ) ? 
									CDebugUtils.toHexAddressString( library.getEndAddress() ) : "";
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
		TableTreeViewer viewer = new TableTreeViewer( parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
		Table table = viewer.getTableTree().getTable();
		table.setLinesVisible( true );
		table.setHeaderVisible( true );		

		// Create the table columns
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		TableColumn[] columns = table.getColumns();
		columns[1].setResizable( true );
		columns[2].setResizable( true );
		columns[3].setResizable( true );

		columns[0].setText( "" );
		columns[1].setText( "Name" );
		columns[2].setText( "Start Address" );
		columns[3].setText( "End Address" );

		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 3 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 50 ) );
		columns[2].setWidth( pc.convertWidthInCharsToPixels( 20 ) );
		columns[3].setWidth( pc.convertWidthInCharsToPixels( 20 ) );

		viewer.setContentProvider( new SharedLibrariesViewContentProvider() );
		viewer.setLabelProvider( new SharedLibrariesLabelProvider() );

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
	protected AbstractDebugEventHandler createEventHandler( Viewer viewer ) 
	{
		return new SharedLibrariesViewEventHandler( this );
	}	
}
