/*******************************************************************************
 * Copyright (c) 2004, 2007 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.modules; 

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.actions.ConfigureColumnsAction;
import org.eclipse.cdt.debug.internal.ui.actions.ToggleDetailPaneAction;
import org.eclipse.cdt.debug.internal.ui.actions.ToggleShowColumnsAction;
import org.eclipse.cdt.debug.internal.ui.preferences.ICDebugPreferenceConstants;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.cdt.debug.ui.ICDebugUIConstants;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelChangedListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelProxy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdateListener;
import org.eclipse.debug.internal.ui.viewers.model.provisional.PresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.ui.AbstractDebugView;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.contexts.DebugContextEvent;
import org.eclipse.debug.ui.contexts.IDebugContextListener;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.console.actions.TextViewerAction;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.IWorkbenchActionDefinitionIds;
 
/**
 * Displays the modules currently loaded by the process being debugged.
 */
public class ModulesView extends AbstractDebugView implements IDebugContextListener, IDebugExceptionHandler, IPropertyChangeListener, IPerspectiveListener, IModelChangedListener, IViewerUpdateListener {

	public class ModulesViewPresentationContext extends PresentationContext {

		private IDebugModelPresentation fModelPresentation;

		public ModulesViewPresentationContext( IDebugModelPresentation modelPresentation ) {
			super( ICDebugUIConstants.ID_MODULES_VIEW );
			fModelPresentation = modelPresentation;
		}

		public IDebugModelPresentation getModelPresentation() {
			return fModelPresentation;
		}
	}

	/**
	 * Internal interface for a cursor listener. I.e. aggregation 
	 * of mouse and key listener.
	 */
	interface ICursorListener extends MouseListener, KeyListener {
	}

    protected String PREF_STATE_MEMENTO = "pref_state_memento."; //$NON-NLS-1$
	
	/**
	 * the preference name for the view part of the sash form
	 */
	protected static final String SASH_VIEW_PART = CDebugUIPlugin.getUniqueIdentifier() + ".SASH_VIEW_PART"; //$NON-NLS-1$

	/**
	 * the preference name for the details part of the sash form
	 */
	protected static final String SASH_DETAILS_PART = CDebugUIPlugin.getUniqueIdentifier() + ".SASH_DETAILS_PART"; //$NON-NLS-1$

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
	 * Stores whether the tree viewer was the last control to have focus in the
	 * view. Used to give focus to the correct component if the user leaves the view.    
	 */
	private boolean fTreeHasFocus = true;

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
    protected ConfigureColumnsAction fConfigureColumnsAction;
	private String fCurrentDetailPaneOrientation = ICDebugPreferenceConstants.MODULES_DETAIL_PANE_HIDDEN;
	protected static final String SASH_WEIGHTS = CDebugUIPlugin.getUniqueIdentifier() + ".modulesView.SASH_WEIGHTS"; //$NON-NLS-1$
	
	private ICursorListener fCursorListener;

	private HashMap fSelectionStates = new HashMap( 10 );

	private HashMap fImageCache = new HashMap( 10 );

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer( Composite parent ) {

		// create the sash form that will contain the tree viewer & text viewer
		setSashForm( new SashForm( parent, SWT.NONE ) );
		
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		JFaceResources.getFontRegistry().addListener( this );

		TreeModelViewer viewer = createTreeViewer( getSashForm() );

		createDetailsViewer();
		getSashForm().setMaximizedControl( viewer.getControl() );

		createOrientationActions( viewer );
		IPreferenceStore prefStore = CDebugUIPlugin.getDefault().getPreferenceStore();
		String orientation = prefStore.getString( getDetailPanePreferenceKey() );
		for( int i = 0; i < fToggleDetailPaneActions.length; i++ ) {
			fToggleDetailPaneActions[i].setChecked( fToggleDetailPaneActions[i].getOrientation().equals( orientation ) );
		}
		setDetailPaneOrientation( orientation );

		viewer.addModelChangedListener( this );
		viewer.addViewerUpdateListener( this );

		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		TextViewerAction textAction = new TextViewerAction( getDetailViewer(), ITextOperationTarget.SELECT_ALL );
		textAction.configureAction( ModulesMessages.getString( "ModulesView.13" ), "", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		textAction.setActionDefinitionId( IWorkbenchActionDefinitionIds.SELECT_ALL );
		setAction( ActionFactory.SELECT_ALL.getId(), textAction );
		textAction = new TextViewerAction( getDetailViewer(), ITextOperationTarget.COPY );
		textAction.configureAction( ModulesMessages.getString( "ModulesView.16" ), "", "" ); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		textAction.setActionDefinitionId( IWorkbenchActionDefinitionIds.COPY );
		setAction( ActionFactory.COPY.getId(), textAction );
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

	protected void setViewerInput( Object context ) {

		if ( context == null ) {
			clearDetails();
		}
		Object current = getViewer().getInput();
		if ( current == null && context == null ) {
			return;
		}
		if ( current != null && current.equals( context ) ) {
			return;
		}

		showViewer();
		getViewer().setInput( context );
	}

	protected TreeModelViewer createTreeViewer( Composite parent ) {
		// add tree viewer
		final TreeModelViewer modulesViewer = new ModulesViewTreeViewer( parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL | SWT.VIRTUAL | SWT.FULL_SELECTION, getPresentationContext() );
		modulesViewer.getControl().addFocusListener( new FocusAdapter() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusListener#focusGained(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusGained( FocusEvent e ) {
				fTreeHasFocus = true;
				getSite().setSelectionProvider( modulesViewer );
			}
			
			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.FocusAdapter#focusLost(org.eclipse.swt.events.FocusEvent)
			 */
			public void focusLost( FocusEvent e ) {
				getSite().setSelectionProvider( null );
			}
		} );

		getSite().setSelectionProvider( modulesViewer );
		modulesViewer.addPostSelectionChangedListener( getTreeSelectionChangedListener() );

		// listen to debug context
		DebugUITools.getDebugContextManager().getContextService( getSite().getWorkbenchWindow() ).addDebugContextListener( this );
		return modulesViewer;
	}

	/**
	 * Create the widgetry for the details viewer.
	 */
	protected void createDetailsViewer() {
		// Create & configure a SourceViewer
		SourceViewer detailsViewer = new SourceViewer( getSashForm(), null, SWT.V_SCROLL | SWT.H_SCROLL );
		Listener activateListener = new Listener() {

			public void handleEvent( Event event ) {
				fTreeHasFocus = false;
			}
		};
		detailsViewer.getControl().addListener( SWT.Activate, activateListener );
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

	protected TreeModelViewer getModulesViewer() {
		return (TreeModelViewer)getViewer();
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
						clearStatusLine();
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

	protected void updateSelectionDependentActions() {
	}

	protected void updateAction( String actionId ) {
		IAction action = getAction( actionId );
		if ( action instanceof IUpdate ) {
			((IUpdate)action).update();
		}
	}

	protected void createDetailContextMenu( Control menuControl ) {
		MenuManager menuMgr = new MenuManager();
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
		menu.add( getAction( ActionFactory.COPY.getId() ) );
		menu.add( getAction( ActionFactory.SELECT_ALL.getId() ) );
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
		updateObjects();
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

	private void createOrientationActions( TreeModelViewer viewer ) {
		IActionBars actionBars = getViewSite().getActionBars();
		final IMenuManager viewMenu = actionBars.getMenuManager();
		fToggleDetailPaneActions = new ToggleDetailPaneAction[3];
		fToggleDetailPaneActions[0] = new ToggleDetailPaneAction( this, ICDebugPreferenceConstants.MODULES_DETAIL_PANE_UNDERNEATH, null );
		fToggleDetailPaneActions[1] = new ToggleDetailPaneAction( this, ICDebugPreferenceConstants.MODULES_DETAIL_PANE_RIGHT, null );
		fToggleDetailPaneActions[2] = new ToggleDetailPaneAction( this, ICDebugPreferenceConstants.MODULES_DETAIL_PANE_HIDDEN, getToggleActionLabel() );
		viewMenu.add( new Separator() );
		viewMenu.add( fToggleDetailPaneActions[0] );
		viewMenu.add( fToggleDetailPaneActions[1] );
		viewMenu.add( fToggleDetailPaneActions[2] );
		viewMenu.add( new Separator() );		

		fConfigureColumnsAction = new ConfigureColumnsAction( viewer );
        setAction( "ToggleColumns", new ToggleShowColumnsAction( viewer ) ); //$NON-NLS-1$

        viewMenu.addMenuListener( new IMenuListener() {
            public void menuAboutToShow( IMenuManager manager ) {
                IAction action = getAction( "ToggleColumns" ); //$NON-NLS-1$
                ((IUpdate)action).update();
                if ( action.isEnabled() ) {
                    if  ( viewMenu.find( action.getId() ) == null )
                        viewMenu.add( action );
                }
                else {
                    viewMenu.remove( action.getId() );
                }
                fConfigureColumnsAction.update();
                if ( fConfigureColumnsAction.isEnabled() ) {
                    if ( viewMenu.find( fConfigureColumnsAction.getId() ) == null )
                        viewMenu.add( fConfigureColumnsAction );
                }
                else {
                    viewMenu.remove( fConfigureColumnsAction.getId() );
                }
            }
        } );
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
		StructuredViewer viewer = (StructuredViewer)getViewer();
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

	/**
	 * Saves the current state of the viewer
	 * @param memento the memento to write the viewer state into
	 */
	public void saveViewerState( IMemento memento ) {
		if ( fSashForm != null && !fSashForm.isDisposed() ) {
			int[] weights = fSashForm.getWeights();
			memento.putInteger( SASH_VIEW_PART, weights[0] );
			memento.putInteger( SASH_DETAILS_PART, weights[1] );
		}
		getModulesViewer().saveState( memento );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IViewPart#init(org.eclipse.ui.IViewSite, org.eclipse.ui.IMemento)
	 */
	public void init( IViewSite site, IMemento memento ) throws PartInitException {
		super.init( site, memento );

		PREF_STATE_MEMENTO = PREF_STATE_MEMENTO + site.getId();
		IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
		String string = store.getString( PREF_STATE_MEMENTO );
		if ( string.length() > 0 ) {
			ByteArrayInputStream bin = new ByteArrayInputStream( string.getBytes() );
			InputStreamReader reader = new InputStreamReader( bin );
			try {
				XMLMemento stateMemento = XMLMemento.createReadRoot( reader );
				setMemento( stateMemento );
			}
			catch( WorkbenchException e ) {
			}
			finally {
				try {
					reader.close();
					bin.close();
				}
				catch( IOException e ) {
				}
			}
		}

        IMemento mem = getMemento();
        setLastSashWeights( DEFAULT_SASH_WEIGHTS );
        setLastSashWeights( DEFAULT_SASH_WEIGHTS );
		if ( mem != null ) {
			Integer sw = mem.getInteger( SASH_VIEW_PART );
			if ( sw != null ) {
				int view = sw.intValue();
				sw = mem.getInteger( SASH_DETAILS_PART );
				if ( sw != null ) {
					int details = sw.intValue();
					if ( view > -1 & details > -1 ) {
						setLastSashWeights( new int[] { view, details } );
					}
				}
			}
		}
		site.getWorkbenchWindow().addPerspectiveListener( this );
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
		ISelection selection = DebugUITools.getDebugContextManager().getContextService( getSite().getWorkbenchWindow() ).getActiveContext();
		contextActivated( selection );
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
		DebugUITools.getDebugContextManager().getContextService( getSite().getWorkbenchWindow() ).removeDebugContextListener( this );
		getSite().getWorkbenchWindow().removePerspectiveListener( this );
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
		JFaceResources.getFontRegistry().removeListener( this );
		TreeModelViewer viewer = getModulesViewer();
		if ( viewer != null ) {
			viewer.removeModelChangedListener( this );
			viewer.removeViewerUpdateListener( this );
		}
		if ( viewer != null ) {
			getDetailDocument().removeDocumentListener( getDetailDocumentListener() );
		}
		disposeImageCache();
		super.dispose();
	}

	protected Image getImage( ImageDescriptor desc ) {
		Image image = (Image)fImageCache.get( desc );
		if ( image == null ) {
			image = desc.createImage();
			fImageCache.put( desc, image );
		}
		return image;
	}

	private void disposeImageCache() {
		Iterator it = fImageCache.values().iterator();
		while( it.hasNext() ) {
			((Image)it.next()).dispose();
		}
		fImageCache.clear();
	}

	public void contextActivated( ISelection selection ) {
		if ( !isAvailable() || !isVisible() ) {
			return;
		}
		if ( selection instanceof IStructuredSelection ) {
			setViewerInput( ((IStructuredSelection)selection).getFirstElement() );
		}
		showViewer();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.contexts.IDebugContextListener#debugContextChanged(org.eclipse.debug.ui.contexts.DebugContextEvent)
	 */
	public void debugContextChanged( DebugContextEvent event ) {
		if ( (event.getFlags() & DebugContextEvent.ACTIVATED) > 0 ) {
			contextActivated( event.getContext() );
		}
	}

	private IPresentationContext getPresentationContext() {
		return new ModulesViewPresentationContext( CDebugUIPlugin.getDebugModelPresentation() );
	}

	private void clearDetails() {
		getDetailDocument().set( "" ); //$NON-NLS-1$
	}

	public void perspectiveActivated( IWorkbenchPage page, IPerspectiveDescriptor perspective ) {
	}

	public void perspectiveChanged( IWorkbenchPage page, IPerspectiveDescriptor perspective, String changeId ) {
		if ( changeId.equals( IWorkbenchPage.CHANGE_RESET ) ) {
			setLastSashWeights( DEFAULT_SASH_WEIGHTS );
			fSashForm.setWeights( DEFAULT_SASH_WEIGHTS );
		}
	}

	public void modelChanged( IModelDelta delta, IModelProxy proxy ) {
	}

	public void updateComplete( IViewerUpdate update ) {
		IStatus status = update.getStatus();
		if ( !update.isCanceled() ) {
			if ( status != null && status.getCode() != IStatus.OK ) {
				showMessage( status.getMessage() );
			}
			else {
				showViewer();
			}
		}
	}

	public void updateStarted( IViewerUpdate update ) {
	}

	public void viewerUpdatesBegin() {
	}

	public void viewerUpdatesComplete() {
	}
	
	protected void clearStatusLine() {
		IStatusLineManager manager = getViewSite().getActionBars().getStatusLineManager();
		manager.setErrorMessage( null );
		manager.setMessage( null );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part ) {
		String id = part.getSite().getId();
		if ( id.equals( getSite().getId() ) ) {
			ByteArrayOutputStream bout = new ByteArrayOutputStream();
			OutputStreamWriter writer = new OutputStreamWriter( bout );

			try {
				XMLMemento memento = XMLMemento.createWriteRoot( "ModulesViewMemento" ); //$NON-NLS-1$
				saveViewerState( memento );
				memento.save( writer );

				IPreferenceStore store = CDebugUIPlugin.getDefault().getPreferenceStore();
				String xmlString = bout.toString();
				store.putValue( PREF_STATE_MEMENTO, xmlString );
			}
			catch( IOException e ) {
			}
			finally {
				try {
					writer.close();
					bout.close();
				}
				catch( IOException e ) {
				}
			}
		}
		super.partDeactivated( part );
	}
}
