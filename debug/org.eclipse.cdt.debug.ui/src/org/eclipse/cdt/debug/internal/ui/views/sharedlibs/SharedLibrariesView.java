/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.sharedlibs;

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICSharedLibrary;
import org.eclipse.cdt.debug.internal.ui.CDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.CDebugUIUtils;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.PixelConverter;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableTreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Displays shared libraries.
 */
public class SharedLibrariesView extends AbstractDebugEventHandlerView
								 implements ISelectionListener, 
								 			INullSelectionListener,
								 			IPropertyChangeListener, 
								 			IDebugExceptionHandler {

	public class SharedLibrariesLabelProvider extends CDebugModelPresentation implements ITableLabelProvider {
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage( Object element, int columnIndex ) {
			if ( element instanceof ICSharedLibrary && columnIndex == 1 )
				return getImage( element );
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText( Object element, int columnIndex ) {
			if ( element instanceof ICSharedLibrary ) {
				ICSharedLibrary library = (ICSharedLibrary)element;
				switch( columnIndex ) {
					case 0:
						return ""; //$NON-NLS-1$
					case 1:
						return getText( element );
					case 2:
						return ( library.areSymbolsLoaded() ) ? SharedLibrariesMessages.getString( "SharedLibrariesView.Loaded_1" ) : SharedLibrariesMessages.getString( "SharedLibrariesView.Not_loaded_1" ); //$NON-NLS-1$ //$NON-NLS-2$
					case 3:
						return ( library.getStartAddress() > 0 ) ? 
									CDebugUIUtils.toHexAddressString( library.getStartAddress() ) : ""; //$NON-NLS-1$
					case 4:
						return ( library.getEndAddress() > 0 ) ? 
									CDebugUIUtils.toHexAddressString( library.getEndAddress() ) : ""; //$NON-NLS-1$
				}
			}
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(Composite)
	 */
	protected Viewer createViewer( Composite parent ) {
		TableTreeViewer viewer = new TableTreeViewer( parent, SWT.FULL_SELECTION | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
		Table table = viewer.getTableTree().getTable();
		table.setLinesVisible( true );
		table.setHeaderVisible( true );		

		// Create the table columns
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		new TableColumn( table, SWT.NULL );
		TableColumn[] columns = table.getColumns();
		columns[1].setResizable( true );
		columns[2].setResizable( true );
		columns[3].setResizable( true );
		columns[4].setResizable( true );

		columns[0].setText( "" ); //$NON-NLS-1$
		columns[1].setText( SharedLibrariesMessages.getString( "SharedLibrariesView.Name_1" ) ); //$NON-NLS-1$
		columns[2].setText( SharedLibrariesMessages.getString( "SharedLibrariesView.Symbols_1" ) ); //$NON-NLS-1$
		columns[3].setText( SharedLibrariesMessages.getString( "SharedLibrariesView.Start_Address_1" ) ); //$NON-NLS-1$
		columns[4].setText( SharedLibrariesMessages.getString( "SharedLibrariesView.End_Address_1" ) ); //$NON-NLS-1$

		PixelConverter pc = new PixelConverter( parent );
		columns[0].setWidth( pc.convertWidthInCharsToPixels( 3 ) );
		columns[1].setWidth( pc.convertWidthInCharsToPixels( 50 ) );
		columns[2].setWidth( pc.convertWidthInCharsToPixels( 20 ) );
		columns[3].setWidth( pc.convertWidthInCharsToPixels( 20 ) );
		columns[4].setWidth( pc.convertWidthInCharsToPixels( 20 ) );

		viewer.setContentProvider( createContentProvider() );
		viewer.setLabelProvider( new SharedLibrariesLabelProvider() );

		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );

		// listen to selection in debug view
		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( createEventHandler( viewer ) );
		
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return ICDebugHelpContextIds.SHARED_LIBRARIES_VIEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(IMenuManager)
	 */
	protected void fillContextMenu( IMenuManager menu ) {
		menu.add( new Separator( ICDebugUIConstants.EMPTY_SHARED_LIBRARIES_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.SHARED_LIBRARIES_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.EMPTY_REFRESH_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.REFRESH_GROUP ) );
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(IToolBarManager)
	 */
	protected void configureToolBar( IToolBarManager tbm ) {
		tbm.add( new Separator( ICDebugUIConstants.SHARED_LIBRARIES_GROUP ) );
		tbm.add( new Separator( ICDebugUIConstants.REFRESH_GROUP ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(IWorkbenchPart, ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		if ( !isAvailable() || !isVisible() )
			return;
		if ( selection == null )
			setViewerInput( new StructuredSelection() );
		else if ( selection instanceof IStructuredSelection )
			setViewerInput( (IStructuredSelection)selection );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler#handleException(DebugException)
	 */
	public void handleException( DebugException e ) {
		showMessage( e.getMessage() );
	}

	protected void setViewerInput( IStructuredSelection ssel ) {
		ICDebugTarget target = null;
		if ( ssel != null && ssel.size() == 1 ) {
			Object input = ssel.getFirstElement();
			if ( input instanceof IDebugElement && ((IDebugElement)input).getDebugTarget() instanceof ICDebugTarget )
				target = (ICDebugTarget)((IDebugElement)input).getDebugTarget();
		}

		if ( getViewer() == null )
			return;

		Object current = getViewer().getInput();
		if ( current != null && current.equals( target ) ) {
			updateObjects();
			return;
		}
		
		showViewer();
		getViewer().setInput( target );
		updateObjects();
	}

	/**
	 * Creates this view's event handler.
	 * 
	 * @param viewer the viewer associated with this view
	 * @return an event handler
	 */
	protected AbstractDebugEventHandler createEventHandler( Viewer viewer ) {
		return new SharedLibrariesViewEventHandler( this );
	}	

	/**
	 * Creates this view's content provider.
	 * 
	 * @return a content provider
	 */
	protected IContentProvider createContentProvider() {
		SharedLibrariesViewContentProvider cp = new SharedLibrariesViewContentProvider();
		cp.setExceptionHandler( this );
		return cp;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesHidden()
	 */
	protected void becomesHidden() {
		setViewerInput( new StructuredSelection() );
		super.becomesHidden();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesVisible()
	 */
	protected void becomesVisible() {
		super.becomesVisible();
		IViewPart part = getSite().getPage().findView( IDebugUIConstants.ID_DEBUG_VIEW );
		if ( part != null ) {
			ISelection selection = getSite().getPage().getSelection( IDebugUIConstants.ID_DEBUG_VIEW );
			selectionChanged( part, selection );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
		super.dispose();
	}
}
