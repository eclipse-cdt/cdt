/**********************************************************************
 * Copyright (c) 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/ 
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import java.util.HashMap;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.ui.CDebugModelPresentation;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.actions.ToggleDetailPaneAction;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.AbstractViewerState;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IValueDetailListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.ListenerList;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.texteditor.IUpdate;
 
/**
 * Displays the modules currently loaded by the process being debugged.
 */
public class ModulesView extends AbstractDebugEventHandlerView implements IDebugExceptionHandler, IPropertyChangeListener, ISelectionListener, INullSelectionListener {


	class ModulesViewModelPresentation implements IDebugModelPresentation {

		private CDebugModelPresentation fDelegate;

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.IDebugModelPresentation#setAttribute(java.lang.String, java.lang.Object)
		 */
		public void setAttribute( String attribute, Object value ) {
			getModelPresentation().setAttribute( attribute, value );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getImage(java.lang.Object)
		 */
		public Image getImage( Object element ) {
			return getModelPresentation().getImage( element );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ILabelProvider#getText(java.lang.Object)
		 */
		public String getText( Object element ) {
			String text = getModelPresentation().getText( element );
			if ( element instanceof ICModule ) {
				ICModule module = (ICModule)element;
				text += ( module.areSymbolsLoaded() ) ? ModulesMessages.getString( "ModulesView.11" ) : ModulesMessages.getString( "ModulesView.12" ); //$NON-NLS-1$ //$NON-NLS-2$
			}
			return text;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.IDebugModelPresentation#computeDetail(org.eclipse.debug.core.model.IValue, org.eclipse.debug.ui.IValueDetailListener)
		 */
		public void computeDetail( IValue value, IValueDetailListener listener ) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorInput(java.lang.Object)
		 */
		public IEditorInput getEditorInput( Object element ) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.debug.ui.ISourcePresentation#getEditorId(org.eclipse.ui.IEditorInput, java.lang.Object)
		 */
		public String getEditorId( IEditorInput input, Object element ) {
			return null;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#addListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void addListener( ILabelProviderListener listener ) {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#isLabelProperty(java.lang.Object, java.lang.String)
		 */
		public boolean isLabelProperty( Object element, String property ) {
			return false;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IBaseLabelProvider#removeListener(org.eclipse.jface.viewers.ILabelProviderListener)
		 */
		public void removeListener( ILabelProviderListener listener ) {
		}

		private CDebugModelPresentation getModelPresentation() {
			if ( fDelegate == null ) {
				fDelegate = CDebugModelPresentation.getDefault();
			}
			return fDelegate;
		}
	}

	/**
	 * Internal interface for a cursor listener. I.e. aggregation 
	 * of mouse and key listener.
	 */
	interface ICursorListener extends MouseListener, KeyListener {
	}

	/**
	 * The selection provider for the modules view changes depending on whether
	 * the variables viewer or detail pane source viewer have focus. This "super" 
	 * provider ensures the correct selection is sent to all listeners.
	 */
	public class ModulesViewSelectionProvider implements ISelectionProvider {

		private ListenerList fListeners= new ListenerList();
		
		private ISelectionProvider fUnderlyingSelectionProvider;

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void addSelectionChangedListener( ISelectionChangedListener listener ) {
			fListeners.add( listener );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
		 */
		public ISelection getSelection() {
			return getUnderlyingSelectionProvider().getSelection();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionProvider#removeSelectionChangedListener(org.eclipse.jface.viewers.ISelectionChangedListener)
		 */
		public void removeSelectionChangedListener( ISelectionChangedListener listener ) {
			fListeners.remove( listener );
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
		 */
		public void setSelection( ISelection selection ) {
			getUnderlyingSelectionProvider().setSelection( selection );
		}

		protected ISelectionProvider getUnderlyingSelectionProvider() {
			return fUnderlyingSelectionProvider;
		}

		protected void setUnderlyingSelectionProvider( ISelectionProvider underlyingSelectionProvider ) {
			fUnderlyingSelectionProvider = underlyingSelectionProvider;
		}

		protected void fireSelectionChanged( SelectionChangedEvent event ) {
			Object[] listeners = fListeners.getListeners();
			for( int i = 0; i < listeners.length; i++ ) {
				ISelectionChangedListener listener = (ISelectionChangedListener)listeners[i];
				listener.selectionChanged( event );
			}
		}
	}

	/**
	 * The UI construct that provides a sliding sash between the modules tree
	 * and the detail pane.
	 */
	private SashForm fSashForm;
	
	/**
	 * The detail pane viewer.
	 */
	private ISourceViewer fDetailViewer;

	/**
	 * The document associated with the detail pane viewer.
	 */
	private IDocument fDetailDocument;
	
	/**
	 * Selection provider for this view.
	 */
	private ModulesViewSelectionProvider fSelectionProvider = new ModulesViewSelectionProvider();

	/**
	 * The model presentation used as the label provider for the tree viewer,
	 * and also as the detail information provider for the detail pane.
	 */
	private IDebugModelPresentation fModelPresentation;

	/**
	 * Remembers which viewer (tree viewer or details viewer) had focus, so we
	 * can reset the focus properly when re-activated.
	 */
	private Viewer fFocusViewer = null;

	/**
	 * Various listeners used to update the enabled state of actions and also to
	 * populate the detail pane.
	 */
	private ISelectionChangedListener fTreeSelectionChangedListener;
	private ISelectionChangedListener fDetailSelectionChangedListener;
	private IDocumentListener fDetailDocumentListener;

	/**
	 * These are used to initialize and persist the position of the sash that
	 * separates the tree viewer from the detail pane.
	 */
	private static final int[] DEFAULT_SASH_WEIGHTS = { 13, 6 };
	private int[] fLastSashWeights;
	private boolean fToggledDetailOnce;
	private ToggleDetailPaneAction[] fToggleDetailPaneActions;
	private String fCurrentDetailPaneOrientation = ICDebugPreferenceConstants.MODULES_DETAIL_PANE_HIDDEN;
	protected static final String SASH_WEIGHTS = CDebugUIPlugin.getUniqueIdentifier() + ".modulesView.SASH_WEIGHTS"; //$NON-NLS-1$
	
	private ICursorListener fCursorListener;

	private HashMap fSelectionStates = new HashMap( 10 );

	private AbstractViewerState fLastState = null;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer( Composite parent ) {
		TreeViewer viewer = createTreeViewer( parent );
		createDetailsViewer();
		getSashForm().setMaximizedControl( viewer.getControl() );

		createOrientationActions();
		IPreferenceStore prefStore = CDebugUIPlugin.getDefault().getPreferenceStore();
		String orientation = prefStore.getString( getDetailPanePreferenceKey() );
		for( int i = 0; i < fToggleDetailPaneActions.length; i++ ) {
			fToggleDetailPaneActions[i].setChecked( fToggleDetailPaneActions[i].getOrientation().equals( orientation ) );
		}
		setDetailPaneOrientation( orientation );

		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return ICDebugHelpContextIds.MODULES_VIEW;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#fillContextMenu(org.eclipse.jface.action.IMenuManager)
	 */
	protected void fillContextMenu( IMenuManager menu ) {
		menu.add( new Separator( ICDebugUIConstants.EMPTY_MODULES_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.MODULES_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.EMPTY_REFRESH_GROUP ) );
		menu.add( new Separator( ICDebugUIConstants.REFRESH_GROUP ) );
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#configureToolBar(org.eclipse.jface.action.IToolBarManager)
	 */
	protected void configureToolBar( IToolBarManager tbm ) {
		tbm.add( new Separator( ICDebugUIConstants.MODULES_GROUP ) );
		tbm.add( new Separator( ICDebugUIConstants.REFRESH_GROUP ) );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler#handleException(org.eclipse.debug.core.DebugException)
	 */
	public void handleException( DebugException e ) {
		showMessage( e.getMessage() );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
		String propertyName = event.getProperty();
		if ( propertyName.equals( IInternalCDebugUIConstants.DETAIL_PANE_FONT ) ) {
			getDetailViewer().getTextWidget().setFont( JFaceResources.getFont( IInternalCDebugUIConstants.DETAIL_PANE_FONT ) );
		}
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

	protected void setViewerInput( IStructuredSelection ssel ) {
		ICDebugTarget target = null;
		if ( ssel.size() == 1 ) {
			Object input = ssel.getFirstElement();
			if ( input instanceof ICDebugElement ) {
				target = (ICDebugTarget)((ICDebugElement)input).getDebugTarget();
			}
		}

		Object current = getViewer().getInput();
		if ( current == null && target == null ) {
			return;
		}
		if ( current != null && current.equals( target ) ) {
			return;
		}

		if ( current != null ) {
			// save state
			fLastState = getViewerState();
			fSelectionStates.put( current, fLastState );
		}		

		showViewer();
		getViewer().setInput( target );

		// restore state
		if ( target != null ) {
			AbstractViewerState state = (AbstractViewerState)fSelectionStates.get( target );
			if ( state == null ) {
				// attempt to restore selection/expansion based on last target
				state = fLastState;
			}
			if ( state != null ) {
				state.restoreState( getModulesViewer() );
			}
		}
	}

	protected TreeViewer createTreeViewer( Composite parent ) {
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		JFaceResources.getFontRegistry().addListener( this );
		// create the sash form that will contain the tree viewer & text viewer
		setSashForm( new SashForm( parent, SWT.NONE ) );
		// add tree viewer
		final TreeViewer modulesViewer = new ModulesViewer( getSashForm(), SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL );
		modulesViewer.setContentProvider( createContentProvider() );
		modulesViewer.setLabelProvider( createLabelProvider( modulesViewer ) );
		modulesViewer.setUseHashlookup( true );
		modulesViewer.getControl().addFocusListener( new FocusAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusGained( FocusEvent e ) {
				getModulesViewSelectionProvider().setUnderlyingSelectionProvider( modulesViewer );
				setFocusViewer( getModulesViewer() );
			}
		} );
		modulesViewer.addPostSelectionChangedListener( getTreeSelectionChangedListener() );
		getModulesViewSelectionProvider().setUnderlyingSelectionProvider( modulesViewer );
		getSite().setSelectionProvider( getModulesViewSelectionProvider() );
		// listen to selection in debug view
		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( createEventHandler() );
		return modulesViewer;
	}

	/**
	 * Create the widgetry for the details viewer.
	 */
	protected void createDetailsViewer() {
		// Create & configure a SourceViewer
		SourceViewer detailsViewer = new SourceViewer( getSashForm(), null, SWT.V_SCROLL | SWT.H_SCROLL );
		setDetailViewer( detailsViewer );
		detailsViewer.setDocument( getDetailDocument() );
		detailsViewer.getTextWidget().setFont( JFaceResources.getFont( IInternalCDebugUIConstants.DETAIL_PANE_FONT ) );
		getDetailDocument().addDocumentListener( getDetailDocumentListener() );
		detailsViewer.configure( new SourceViewerConfiguration() );
		detailsViewer.setEditable( false );
		Control control = detailsViewer.getControl();
		GridData gd = new GridData( GridData.FILL_BOTH );
		control.setLayoutData( gd );
		detailsViewer.getSelectionProvider().addSelectionChangedListener( getDetailSelectionChangedListener() );
		detailsViewer.getControl().addFocusListener( new FocusAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusGained( FocusEvent e ) {
				getModulesViewSelectionProvider().setUnderlyingSelectionProvider( getDetailViewer().getSelectionProvider() );
				setFocusViewer( (Viewer)getDetailViewer() );
			}
		} );
		// add a context menu to the detail area
		createDetailContextMenu( detailsViewer.getTextWidget() );
		detailsViewer.getTextWidget().addMouseListener( getCursorListener() );
		detailsViewer.getTextWidget().addKeyListener( getCursorListener() );
	}

	private void setDetailViewer( ISourceViewer viewer ) {
		fDetailViewer = viewer;
	}

	protected ISourceViewer getDetailViewer() {
		return fDetailViewer;
	}

	protected SashForm getSashForm() {
		return fSashForm;
	}

	private void setSashForm( SashForm sashForm ) {
		fSashForm = sashForm;
	}

	protected IContentProvider createContentProvider() {
		ModulesViewContentProvider cp = new ModulesViewContentProvider();
		cp.setExceptionHandler( this );
		return cp;
	}

	protected IBaseLabelProvider createLabelProvider( StructuredViewer viewer ) {
//		return new DebugViewDecoratingLabelProvider( viewer, new DebugViewInterimLabelProvider( getModelPresentation() ), new DebugViewLabelDecorator( getModelPresentation() ) );
		return getModelPresentation();
	}

	protected IDebugModelPresentation getModelPresentation() {
		if ( fModelPresentation == null ) {
			fModelPresentation = new ModulesViewModelPresentation();
		}
		return fModelPresentation;
	}

	protected ModulesViewSelectionProvider getModulesViewSelectionProvider() {
		return fSelectionProvider;
	}

	protected ModulesViewer getModulesViewer() {
		return (ModulesViewer)getViewer();
	}

	protected void setFocusViewer( Viewer viewer ) {
		fFocusViewer = viewer;
	}

	protected Viewer getFocusViewer() {
		return fFocusViewer;
	}

	/**
	 * Lazily instantiate and return a selection listener that populates the detail pane,
	 * but only if the detail is currently visible. 
	 */
	protected ISelectionChangedListener getTreeSelectionChangedListener() {
		if ( fTreeSelectionChangedListener == null ) {
			fTreeSelectionChangedListener = new ISelectionChangedListener() {

				public void selectionChanged( SelectionChangedEvent event ) {
					if ( event.getSelectionProvider().equals( getModulesViewer() ) ) {
						getModulesViewSelectionProvider().fireSelectionChanged( event );
						// if the detail pane is not visible, don't waste time retrieving details
						if ( getSashForm().getMaximizedControl() == getViewer().getControl() ) {
							return;
						}
						IStructuredSelection selection = (IStructuredSelection)event.getSelection();
						populateDetailPaneFromSelection( selection );
						treeSelectionChanged( event );
					}
				}
			};
		}
		return fTreeSelectionChangedListener;
	}

	protected void treeSelectionChanged( SelectionChangedEvent event ) {
	}

	/**
	 * Ask the modules tree for its current selection, and use this to populate
	 * the detail pane.
	 */
	public void populateDetailPane() {
		if ( isDetailPaneVisible() ) {
			Viewer viewer = getViewer();
			if ( viewer != null ) {
				IStructuredSelection selection = (IStructuredSelection)viewer.getSelection();
				populateDetailPaneFromSelection( selection );
			}
		}
	}

	/**
	 * Show the details associated with the first of the selected elements in the 
	 * detail pane.
	 */
	protected void populateDetailPaneFromSelection( IStructuredSelection selection ) {
		getDetailDocument().set( "" ); //$NON-NLS-1$
		if ( !selection.isEmpty() ) {
			computeDetail( selection.getFirstElement() );
		}
	}

	/**
	 * Lazily instantiate and return a selection listener that updates the enabled
	 * state of the selection oriented actions in this view.
	 */
	protected ISelectionChangedListener getDetailSelectionChangedListener() {
		if ( fDetailSelectionChangedListener == null ) {
			fDetailSelectionChangedListener = new ISelectionChangedListener() {

				public void selectionChanged( SelectionChangedEvent event ) {
					if ( event.getSelectionProvider().equals( getModulesViewSelectionProvider().getUnderlyingSelectionProvider() ) ) {
						getModulesViewSelectionProvider().fireSelectionChanged( event );
						updateSelectionDependentActions();
					}
				}
			};
		}
		return fDetailSelectionChangedListener;
	}

	/**
	 * Lazily instantiate and return a document listener that updates the enabled state
	 * of the 'Find/Replace' action.
	 */
	protected IDocumentListener getDetailDocumentListener() {
		if ( fDetailDocumentListener == null ) {
			fDetailDocumentListener = new IDocumentListener() {

				public void documentAboutToBeChanged( DocumentEvent event ) {
				}

				public void documentChanged( DocumentEvent event ) {
				}
			};
		}
		return fDetailDocumentListener;
	}

	/**
	 * Lazily instantiate and return a Document for the detail pane text viewer.
	 */
	protected IDocument getDetailDocument() {
		if ( fDetailDocument == null ) {
			fDetailDocument = new Document();
		}
		return fDetailDocument;
	}

	protected AbstractDebugEventHandler createEventHandler() {
		return new ModulesViewEventHandler( this );
	}

	protected void updateSelectionDependentActions() {
	}

	protected void updateAction( String actionId ) {
		IAction action = getAction( actionId );
		if ( action instanceof IUpdate ) {
			((IUpdate)action).update();
		}
	}

	protected void createDetailContextMenu( Control menuControl ) {
		MenuManager menuMgr = new MenuManager(); //$NON-NLS-1$
		menuMgr.setRemoveAllWhenShown( true );
		menuMgr.addMenuListener( new IMenuListener() {

			public void menuAboutToShow( IMenuManager mgr ) {
				fillDetailContextMenu( mgr );
			}
		} );
		Menu menu = menuMgr.createContextMenu( menuControl );
		menuControl.setMenu( menu );
		// register the context menu such that other plugins may contribute to it
		getSite().registerContextMenu( ICDebugUIConstants.MODULES_VIEW_DETAIL_ID, menuMgr, getDetailViewer().getSelectionProvider() );
		addContextMenuManager( menuMgr );
	}

	protected void fillDetailContextMenu( IMenuManager menu ) {
		menu.add( new Separator( ICDebugUIConstants.MODULES_GROUP ) );
		menu.add( new Separator() );
		menu.add( getAction( ActionFactory.CUT.getId() ) );
		menu.add( getAction( ActionFactory.COPY.getId() + ".Detail" ) ); //$NON-NLS-1$
		menu.add( getAction( ActionFactory.PASTE.getId() ) );
		menu.add( new Separator( "FIND" ) ); //$NON-NLS-1$
		menu.add( getAction( ActionFactory.FIND.getId() ) );
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
	}

	private ICursorListener getCursorListener() {
		if ( fCursorListener == null ) {
			fCursorListener = new ICursorListener() {

				public void keyPressed( KeyEvent e ) {
				}

				public void keyReleased( KeyEvent e ) {
				}

				public void mouseDoubleClick( MouseEvent e ) {
				}

				public void mouseDown( MouseEvent e ) {
				}

				public void mouseUp( MouseEvent e ) {
				}
			};
		}
		return fCursorListener;
	}

	public void setDetailPaneOrientation( String orientation ) {
		if ( orientation.equals( fCurrentDetailPaneOrientation ) ) {
			return;
		}
		if ( orientation.equals( ICDebugPreferenceConstants.MODULES_DETAIL_PANE_HIDDEN ) ) {
			hideDetailPane();
		}
		else {
			int vertOrHoriz = orientation.equals( ICDebugPreferenceConstants.MODULES_DETAIL_PANE_UNDERNEATH ) ? SWT.VERTICAL : SWT.HORIZONTAL;
			getSashForm().setOrientation( vertOrHoriz );
			if ( ICDebugPreferenceConstants.MODULES_DETAIL_PANE_HIDDEN.equals( fCurrentDetailPaneOrientation ) ) {
				showDetailPane();
			}
		}
		fCurrentDetailPaneOrientation = orientation;
		CDebugUIPlugin.getDefault().getPreferenceStore().setValue( getDetailPanePreferenceKey(), orientation );
	}
	
	private void hideDetailPane() {
		if ( fToggledDetailOnce ) {
			setLastSashWeights( getSashForm().getWeights() );
		}
		getSashForm().setMaximizedControl( getViewer().getControl() );		
	}
	
	private void showDetailPane() {
		getSashForm().setMaximizedControl( null );
		getSashForm().setWeights( getLastSashWeights() );
		populateDetailPane();
		revealTreeSelection();
		fToggledDetailOnce = true;		
	}

	protected String getDetailPanePreferenceKey() {
		return ICDebugPreferenceConstants.MODULES_DETAIL_PANE_ORIENTATION;
	}

	protected int[] getLastSashWeights() {
		if ( fLastSashWeights == null ) {
			fLastSashWeights = DEFAULT_SASH_WEIGHTS;
		}
		return fLastSashWeights;
	}

	protected void setLastSashWeights( int[] weights ) {
		fLastSashWeights = weights;
	}

	private void createOrientationActions() {
		IActionBars actionBars = getViewSite().getActionBars();
		IMenuManager viewMenu = actionBars.getMenuManager();
		fToggleDetailPaneActions = new ToggleDetailPaneAction[3];
		fToggleDetailPaneActions[0] = new ToggleDetailPaneAction( this, ICDebugPreferenceConstants.MODULES_DETAIL_PANE_UNDERNEATH, null );
		fToggleDetailPaneActions[1] = new ToggleDetailPaneAction( this, ICDebugPreferenceConstants.MODULES_DETAIL_PANE_RIGHT, null );
		fToggleDetailPaneActions[2] = new ToggleDetailPaneAction( this, ICDebugPreferenceConstants.MODULES_DETAIL_PANE_HIDDEN, getToggleActionLabel() );
		viewMenu.add( new Separator() );
		viewMenu.add( fToggleDetailPaneActions[0] );
		viewMenu.add( fToggleDetailPaneActions[1] );
		viewMenu.add( fToggleDetailPaneActions[2] );
		viewMenu.add( new Separator() );		
	}

	protected String getToggleActionLabel() {
		return ModulesMessages.getString( "ModulesView.0" ); //$NON-NLS-1$
	}

	protected boolean isDetailPaneVisible() {
		return !fToggleDetailPaneActions[2].isChecked();
	}

	/**
	 * Make sure the currently selected item in the tree is visible.
	 */
	protected void revealTreeSelection() {
		ModulesViewer viewer = getModulesViewer();
		if ( viewer != null ) {
			ISelection selection = viewer.getSelection();
			if ( selection instanceof IStructuredSelection ) {
				Object selected = ((IStructuredSelection)selection).getFirstElement();
				if ( selected != null ) {
					viewer.reveal( selected );
				}
			}
		}
	}

	/**
	 * Set on or off the word wrap flag for the detail pane.
	 */
	public void toggleDetailPaneWordWrap( boolean on ) {
		fDetailViewer.getTextWidget().setWordWrap( on );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#saveState(org.eclipse.ui.IMemento)
	 */
	public void saveState( IMemento memento ) {
		super.saveState( memento );
		SashForm sashForm = getSashForm();
		if ( sashForm != null ) {
			int[] weights = sashForm.getWeights();
			memento.putInteger( SASH_WEIGHTS + "-Length", weights.length ); //$NON-NLS-1$
			for( int i = 0; i < weights.length; i++ ) {
				memento.putInteger( SASH_WEIGHTS + "-" + i, weights[i] ); //$NON-NLS-1$
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init( IViewSite site, IMemento memento ) throws PartInitException {
		super.init( site, memento );
		if ( memento != null ) {
			Integer bigI = memento.getInteger( SASH_WEIGHTS + "-Length" ); //$NON-NLS-1$
			if ( bigI == null ) {
				return;
			}
			int numWeights = bigI.intValue();
			int[] weights = new int[numWeights];
			for( int i = 0; i < numWeights; i++ ) {
				bigI = memento.getInteger( SASH_WEIGHTS + "-" + i ); //$NON-NLS-1$
				if ( bigI == null ) {
					return;
				}
				weights[i] = bigI.intValue();
			}
			if ( weights.length > 0 ) {
				setLastSashWeights( weights );
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getDefaultControl()
	 */
	protected Control getDefaultControl() {
		return getSashForm();
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

	private void computeDetail( final Object element ) {
		if ( element != null ) {
			DebugPlugin.getDefault().asyncExec( new Runnable() {

				public void run() {
					detailComputed( element, doComputeDetail( element ) );
				}
			} );
		}
	}

	protected String doComputeDetail( Object element ) {
		if ( element instanceof ICModule ) {
			return getModuleDetail( ((ICModule)element) );
		}
		if ( element instanceof ICElement ) {
			return element.toString();
		}
		return ""; //$NON-NLS-1$
	}

	private String getModuleDetail( ICModule module ) {
		StringBuffer sb = new StringBuffer();
		
		// Type
		String type = null;
		switch( module.getType() ) {
			case ICModule.EXECUTABLE:
				type = ModulesMessages.getString( "ModulesView.1" ); //$NON-NLS-1$
				break;
			case ICModule.SHARED_LIBRARY:
				type = ModulesMessages.getString( "ModulesView.2" ); //$NON-NLS-1$
				break;
		}
		if ( type != null ) {
			sb.append( ModulesMessages.getString( "ModulesView.3" ) ); //$NON-NLS-1$
			sb.append( type );
			sb.append( '\n' );
		}
		
		// Symbols flag
		sb.append( ModulesMessages.getString( "ModulesView.4" ) ); //$NON-NLS-1$
		sb.append( ( module.areSymbolsLoaded() ) ? ModulesMessages.getString( "ModulesView.5" ) : ModulesMessages.getString( "ModulesView.6" ) ); //$NON-NLS-1$ //$NON-NLS-2$
		sb.append( '\n' );

		// Symbols file
		sb.append( ModulesMessages.getString( "ModulesView.7" ) ); //$NON-NLS-1$
		sb.append( module.getSymbolsFileName().toOSString() );
		sb.append( '\n' );

		// CPU
		String cpu = module.getCPU();
		if ( cpu != null ) {
			sb.append( ModulesMessages.getString( "ModulesView.8" ) ); //$NON-NLS-1$
			sb.append( cpu );
			sb.append( '\n' );
		}

		// Base address
		IAddress baseAddress = module.getBaseAddress();
		if ( !baseAddress.isZero() ) {
			sb.append( ModulesMessages.getString( "ModulesView.9" ) ); //$NON-NLS-1$
			sb.append( baseAddress.toHexAddressString() );
			sb.append( '\n' );
		}
		
		// Size
		long size = module.getSize();
		if ( size > 0 ) { 
			sb.append( ModulesMessages.getString( "ModulesView.10" ) ); //$NON-NLS-1$
			sb.append( size );
			sb.append( '\n' );
		}

		return sb.toString();
	}

	protected void detailComputed( Object element, final String result ) {
		Runnable runnable = new Runnable() {

			public void run() {
				if ( isAvailable() ) {
					getDetailDocument().set( result );	
				}
			}
		};
		asyncExec( runnable );		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
		JFaceResources.getFontRegistry().removeListener( this );
		Viewer viewer = getViewer();
		if ( viewer != null ) {
			getDetailDocument().removeDocumentListener( getDetailDocumentListener() );
		}
		super.dispose();
	}

	private AbstractViewerState getViewerState() {
		return new ModulesViewerState( getModulesViewer() );
	}
}
