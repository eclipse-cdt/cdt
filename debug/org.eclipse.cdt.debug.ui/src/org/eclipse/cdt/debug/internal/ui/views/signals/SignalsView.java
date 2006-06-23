/*******************************************************************************
 * Copyright (c) 2004, 2006 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.signals;

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.ui.IDebugModelPresentation;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;


/**
 * Displays signals.
 *
 * @since: Mar 8, 2004
 */
public class SignalsView extends AbstractDebugEventHandlerView 
						 implements ISelectionListener, 
						 			INullSelectionListener, 
									IPropertyChangeListener, 
									IDebugExceptionHandler {

	public class SignalsViewLabelProvider extends LabelProvider implements ITableLabelProvider {

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage( Object element, int columnIndex ) {
			if ( columnIndex == 0 )
				return getModelPresentation().getImage( element );
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText( Object element, int columnIndex ) {
			if ( element instanceof ICSignal ) {
				try {
					switch( columnIndex ) {
						case 0:
							return ((ICSignal)element).getName();
						case 1:
							return (((ICSignal)element).isPassEnabled()) ? SignalsViewer.YES_VALUE : SignalsViewer.NO_VALUE;
						case 2:
							return (((ICSignal)element).isStopEnabled()) ? SignalsViewer.YES_VALUE : SignalsViewer.NO_VALUE;
						case 3:
							return ((ICSignal)element).getDescription();
					}
				} catch( DebugException e ) {
				}
			}
			return null;
		}
		
		private IDebugModelPresentation getModelPresentation() {
			return CDebugUIPlugin.getDebugModelPresentation();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer( Composite parent ) {
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		
		// add tree viewer
		final SignalsViewer vv = new SignalsViewer( parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
		vv.setContentProvider( createContentProvider() );
		vv.setLabelProvider( new SignalsViewLabelProvider() );
		vv.setUseHashlookup( true );

		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );

		// listen to selection in debug view
		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( new SignalsViewEventHandler( this ) );

		return vv;
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
		return ICDebugHelpContextIds.SIGNALS_VIEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu( IMenuManager menu ) {
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar( IToolBarManager tbm ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
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
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler#handleException(org.eclipse.debug.core.DebugException)
	 */
	public void handleException( DebugException e ) {
		showMessage( e.getMessage() );
	}

	/**
	 * Creates this view's content provider.
	 * 
	 * @return a content provider
	 */
	private IContentProvider createContentProvider() {
		SignalsViewContentProvider cp = new SignalsViewContentProvider();
		cp.setExceptionHandler( this );
		return cp;
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
