/*
 *(c) Copyright QNX Software Systems Ltd. 2002.
 * All Rights Reserved.
 * 
 */
package org.eclipse.cdt.debug.internal.ui.views.sharedlibs;

import org.eclipse.cdt.debug.core.ICSharedLibraryManager;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.internal.core.CDebugUtils;
import org.eclipse.cdt.debug.internal.ui.CDebugImages;
import org.eclipse.cdt.debug.internal.ui.CImageDescriptor;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
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
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Enter type comment.
 * 
 * @since: Jan 16, 2003
 */
public class SharedLibrariesView extends AbstractDebugEventHandlerView
								 implements ISelectionListener, 
								 			IPropertyChangeListener, 
								 			IDebugExceptionHandler
{
	/**
	 * Enter type comment.
	 * 
	 * @since: Jan 16, 2003
	 */
	public class SharedLibrariesViewLabelProvider extends LabelProvider
												  implements ITableLabelProvider
	{
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object, int)
		 */
		public Image getColumnImage( Object element, int columnIndex )
		{
			if ( columnIndex == 0 && element instanceof ICSharedLibrary )
			{
				if ( ((ICSharedLibrary)element).areSymbolsLoaded() )
				{
					return CDebugUIPlugin.getImageDescriptorRegistry().get( new CImageDescriptor( CDebugImages.DESC_OBJS_LOADED_SHARED_LIBRARY,  0 ) );
				}
				else
				{
					return CDebugUIPlugin.getImageDescriptorRegistry().get( new CImageDescriptor( CDebugImages.DESC_OBJS_SHARED_LIBRARY,  0 ) );
				}
			}
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object, int)
		 */
		public String getColumnText( Object element, int columnIndex )
		{
			if ( element instanceof ICSharedLibrary )
			{
				switch( columnIndex )
				{
					case 0:
						return ((ICSharedLibrary)element).getFileName();
					case 1:
						return CDebugUtils.toHexAddressString( ((ICSharedLibrary)element).getStartAddress() );
					case 2:
						return CDebugUtils.toHexAddressString( ((ICSharedLibrary)element).getEndAddress() );
					case 3:
						return ( ((ICSharedLibrary)element).areSymbolsLoaded() ) ? "Yes" : "No";
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
		TableViewer viewer = new TableViewer( parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );

		Table table = viewer.getTable();
		table.setHeaderVisible( true );
		table.setLinesVisible( true );
		table.setLayoutData( new GridData( GridData.FILL_BOTH ) );

		// Create the table columns
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		TableColumn[] columns = table.getColumns();
		columns[0].setText( "Name" );
		columns[1].setText( "Start Address" );
		columns[2].setText( "End Address" );
		columns[3].setText( "Symbols" );

		viewer.setContentProvider( new SharedLibrariesViewContentProvider() );
		viewer.setLabelProvider( new SharedLibrariesViewLabelProvider() );

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
