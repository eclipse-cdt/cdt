/*******************************************************************************
 * Copyright (c) 2004, 2005 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.ICDebugConstants;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.IInternalCDebugUIConstants;
import org.eclipse.cdt.debug.internal.ui.actions.CBreakpointPropertiesRulerAction;
import org.eclipse.cdt.debug.internal.ui.actions.EnableDisableBreakpointRulerAction;
import org.eclipse.cdt.debug.internal.ui.actions.ToggleBreakpointRulerAction;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.OverviewRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.jface.util.Assert;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.IUpdate;
import org.eclipse.ui.texteditor.MarkerAnnotationPreferences;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

/**
 * This view shows disassembly for a particular stack frame.
 */
public class DisassemblyView extends AbstractDebugEventHandlerView 
							 implements ISelectionListener, 
							 			INullSelectionListener, 
										IPropertyChangeListener, 
										IDebugExceptionHandler,
										IDisassemblyListener {

	/**
	 * Creates and returns the listener on this view's context menus.
	 *
	 * @return the menu listener
	 */
	protected final IMenuListener getContextMenuListener() {
		if ( fMenuListener == null ) {
			fMenuListener = new IMenuListener() {

				public void menuAboutToShow( IMenuManager menu ) {
					String id = menu.getId();
					if ( getRulerContextMenuId().equals( id ) ) {
						setFocus();
						rulerContextMenuAboutToShow( menu );
					}
					else if ( getViewContextMenuId().equals( id ) ) {
						setFocus();
						viewContextMenuAboutToShow( menu );
					}
				}
			};
		}
		return fMenuListener;
	}

	/**
	 * Creates and returns the listener on this editor's vertical ruler.
	 *
	 * @return the mouse listener
	 */
	protected final MouseListener getRulerMouseListener() {
		if ( fMouseListener == null ) {
			fMouseListener = new MouseListener() {

				private boolean fDoubleClicked = false;

				private void triggerAction( String actionID ) {
					IAction action = getAction( actionID );
					if ( action != null ) {
						if ( action instanceof IUpdate )
							((IUpdate)action).update();
						if ( action.isEnabled() )
							action.run();
					}
				}

				public void mouseUp( MouseEvent e ) {
					setFocus();
					if ( 1 == e.button && !fDoubleClicked )
						triggerAction( ITextEditorActionConstants.RULER_CLICK );
					fDoubleClicked = false;
				}

				public void mouseDoubleClick( MouseEvent e ) {
					if ( 1 == e.button ) {
						fDoubleClicked = true;
						triggerAction( IInternalCDebugUIConstants.ACTION_TOGGLE_BREAKPOINT );
					}
				}

				public void mouseDown( MouseEvent e ) {
					StyledText text = getSourceViewer().getTextWidget();
					if ( text != null && !text.isDisposed() ) {
						Display display = text.getDisplay();
						Point location = display.getCursorLocation();
						getRulerContextMenu().setLocation( location.x, location.y );
					}
				}
			};
		}
		return fMouseListener;
	}

	/**
	 * The width of the vertical ruler.
	 */
	private final static int VERTICAL_RULER_WIDTH = 12;

	/**
	 * Preference key for highlighting current line.
	 */
	private final static String CURRENT_LINE = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;

	/**
	 * Preference key for highlight color of current line.
	 */
	private final static String CURRENT_LINE_COLOR = AbstractDecoratedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;

	/** 
	 * The view's context menu id. 
	 */
	private String fViewContextMenuId;

	/** 
	 * The ruler's context menu id. 
	 */
	private String fRulerContextMenuId;

	/**
	 * The vertical ruler.
	 */
	private IVerticalRuler fVerticalRuler;
	
	/**
	 * The overview ruler.
	 */
	private IOverviewRuler fOverviewRuler;

	/**
	 * The last stack frame for which the disassembly storage has 
	 * been requested.
	 */
	protected ICStackFrame fLastStackFrame = null;

	/**
	 * Helper for managing the decoration support of this view's viewer.
	 */
	private SourceViewerDecorationSupport fSourceViewerDecorationSupport;

	/**
	 * Helper for accessing annotation from the perspective of this view.
	 */
	private IAnnotationAccess fAnnotationAccess;

	/**
	 * The annotation preferences.
	 */
	private MarkerAnnotationPreferences fAnnotationPreferences;

	/**
	 * Disassembly document provider.
	 */
	private DisassemblyDocumentProvider fDocumentProvider;

	/**
	 * Current instruction pointer nnotation.
	 */
	private DisassemblyInstructionPointerAnnotation fInstrPointerAnnotation;

	/** 
	 * Context menu listener. 
	 */
	private IMenuListener fMenuListener;

	/** 
	 * Vertical ruler mouse listener. 
	 */
	private MouseListener fMouseListener;

	/** 
	 * The ruler context menu to be disposed. 
	 */
	private Menu fRulerContextMenu;

	/** 
	 * The text context menu to be disposed. 
	 */
	private Menu fTextContextMenu;

	/** 
	 * The actions registered with the view. 
	 */	
	private Map fActions = new HashMap( 10 );

	/**
	 * Constructor for DisassemblyView.
	 */
	public DisassemblyView() {
		super();
		setViewContextMenuId( "#DisassemblyViewContext" ); //$NON-NLS-1$
		setRulerContextMenuId( "#DisassemblyEditorRulerContext" ); //$NON-NLS-1$
		fAnnotationPreferences = new MarkerAnnotationPreferences();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer( Composite parent ) {
		fVerticalRuler = createVerticalRuler();
		fOverviewRuler = createOverviewRuler( getSharedColors() );
		
		SourceViewer viewer = createSourceViewer( parent, fVerticalRuler, getOverviewRuler() );
		viewer.configure( new DisassemblyViewerConfiguration() );
		getSourceViewerDecorationSupport( viewer );
		
		getEditorPreferenceStore().addPropertyChangeListener( this );
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		getSite().setSelectionProvider( viewer.getSelectionProvider() );
		setEventHandler( createEventHandler() );
		
		viewer.setDocument( getDocumentProvider().getDocument( DisassemblyEditorInput.EMPTY_EDITOR_INPUT ), getDocumentProvider().getAnnotationModel( null ) );

		resetViewerInput();

		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createActions()
	 */
	protected void createActions() {
		IAction action;
		IVerticalRuler ruler = getVerticalRuler();
		action= new ToggleBreakpointRulerAction( this, ruler );
		setAction( IInternalCDebugUIConstants.ACTION_TOGGLE_BREAKPOINT, action );
		action= new EnableDisableBreakpointRulerAction( this, ruler );
		setAction( IInternalCDebugUIConstants.ACTION_ENABLE_DISABLE_BREAKPOINT, action );
		action= new CBreakpointPropertiesRulerAction( this, ruler );
		setAction( IInternalCDebugUIConstants.ACTION_BREAKPOINT_PROPERTIES, action );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#getHelpContextId()
	 */
	protected String getHelpContextId() {
		return ICDebugHelpContextIds.DISASSEMBLY_VIEW;		
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
		// TODO Auto-generated method stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.ISelectionListener#selectionChanged(org.eclipse.ui.IWorkbenchPart, org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged( IWorkbenchPart part, ISelection selection ) {
		if ( !isAvailable() || !isVisible() )
			return;
		if ( selection == null )
			resetViewerInput();
		else if ( selection instanceof IStructuredSelection )
			computeInput( (IStructuredSelection)selection );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	public void propertyChange( PropertyChangeEvent event ) {
		String propertyName = event.getProperty();
		if ( IInternalCDebugUIConstants.DISASSEMBLY_SOURCE_LINE_COLOR.equals( propertyName ) ) {
			IEditorInput input = getInput();
			if ( input instanceof DisassemblyEditorInput )
				getSourceViewer().changeTextPresentation( createTextPresentation( ((DisassemblyEditorInput)input).getSourceRegions() ), true );
		}
		else if ( IInternalCDebugUIConstants.DISASSEMBLY_FONT.equals( propertyName ) ) {
			getSourceViewer().getTextWidget().setFont( JFaceResources.getFont( IInternalCDebugUIConstants.DISASSEMBLY_FONT ) );			
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler#handleException(org.eclipse.debug.core.DebugException)
	 */
	public void handleException( DebugException e ) {
		showMessage( e.getMessage() );
	}

	/**
	 * Creates the vertical ruler to be used by this view.
	 *
	 * @return the vertical ruler
	 */
	protected IVerticalRuler createVerticalRuler() {
		IVerticalRuler ruler = new VerticalRuler( VERTICAL_RULER_WIDTH, getAnnotationAccess() );
		return ruler;
	}

	private IOverviewRuler createOverviewRuler( ISharedTextColors sharedColors ) {
		IOverviewRuler ruler = new OverviewRuler( getAnnotationAccess(), VERTICAL_RULER_WIDTH, sharedColors );
		Iterator e = fAnnotationPreferences.getAnnotationPreferences().iterator();
		while( e.hasNext() ) {
			AnnotationPreference preference = (AnnotationPreference)e.next();
			if ( preference.contributesToHeader() )
				ruler.addHeaderAnnotationType( preference.getAnnotationType() );
		}
		return ruler;
	}

	/**
	 * Creates the source viewer to be used by this view.
	 *
	 * @param parent the parent control
	 * @param ruler the vertical ruler
	 * @param styles style bits
	 * @return the source viewer
	 */
	private SourceViewer createSourceViewer( Composite parent, IVerticalRuler vertRuler, IOverviewRuler ovRuler ) {
		DisassemblyViewer viewer = new DisassemblyViewer( parent, vertRuler, ovRuler );
		viewer.setRangeIndicator( new DefaultRangeIndicator() );
		JFaceResources.getFontRegistry().addListener( this );
		JFaceResources.getColorRegistry().addListener( this );
		return viewer;
	}

	protected SourceViewer getSourceViewer() {
		return (SourceViewer)getViewer();
	}

	/**
	 * Creates this view's event handler.
	 * 
	 * @return an event handler
	 */
	protected AbstractDebugEventHandler createEventHandler() {
		return new DisassemblyViewEventHandler( this );
	}	

	protected void computeInput( IStructuredSelection ssel ) {
		SourceViewer viewer = getSourceViewer();
		if ( viewer == null )
			return;

		fLastStackFrame = null;
		if ( ssel != null && ssel.size() == 1 ) {
			Object element = ssel.getFirstElement();
			if ( element instanceof ICStackFrame ) {
				fLastStackFrame = (ICStackFrame)element;
				IEditorInput input = getInput();
				if ( input instanceof DisassemblyEditorInput && 
					 !((DisassemblyEditorInput)input).contains( (ICStackFrame)element ) )
					setViewerInput( DisassemblyEditorInput.PENDING_EDITOR_INPUT );
				computeInput( input, (ICStackFrame)element, this );
				return;
			}
		}
		resetViewerInput();
	}

	protected void setViewerInput( IEditorInput input ) {
		SourceViewer viewer = getSourceViewer();
		if ( viewer == null )
			return;

		if ( input == null )
			input = DisassemblyEditorInput.EMPTY_EDITOR_INPUT;

		IEditorInput current = getInput();
		if ( current != null && current.equals( input ) ) {
			updateObjects();
			return;
		}

		setInput( input );
		showViewer();
		try {
			getDocumentProvider().connect( input );
		}
		catch( CoreException e ) {
			// never happens
		}
		getSourceViewer().setDocument( getDocumentProvider().getDocument( input ), 
									   getDocumentProvider().getAnnotationModel( input ) );
		if ( input instanceof DisassemblyEditorInput ) {
			// Workaround for bug #69728
			IRegion[] sourceRegions = ((DisassemblyEditorInput)input).getSourceRegions();
			if ( sourceRegions.length > 0 ) {
				getSourceViewer().changeTextPresentation( createTextPresentation( sourceRegions ), true );
			}
		}
		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		JFaceResources.getFontRegistry().removeListener( this );
		JFaceResources.getColorRegistry().removeListener( this );
		getEditorPreferenceStore().removePropertyChangeListener( this );
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );

		if ( fSourceViewerDecorationSupport != null ) {
			fSourceViewerDecorationSupport.dispose();
			fSourceViewerDecorationSupport = null;
		}

		if ( fDocumentProvider != null ) {
			fDocumentProvider.dispose();
			fDocumentProvider = null;
		}

		if ( fActions != null ) {
			fActions.clear();
			fActions = null;
		}

		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.internal.ui.views.disassembly.IDisassemblyListener#inputComputed(org.eclipse.cdt.debug.core.model.ICStackFrame, org.eclipse.core.runtime.IStatus, org.eclipse.ui.IEditorInput)
	 */
	public void inputComputed( final ICStackFrame frame, final IStatus status, final IEditorInput input ) {
		Runnable runnable = new Runnable() {
			public void run() {
				if ( isAvailable() ) {
					if ( fLastStackFrame != null && fLastStackFrame.equals( frame ) ) {
						fLastStackFrame = null;
						if ( !status.isOK() ) {
							setInput( null );
							getViewer().setInput( null );
							showMessage( status.getMessage() );
							return;
						}
					}
					if ( input != null ) {
						setViewerInput( input );
						selectAndReveal( frame, input );
					}
					else {
						resetViewerInput();
					}
				}
			}
		};
		asyncExec( runnable );		
	}

	/**
	 * Asynchronousy computes the editor input for the given stack frame.
	 * 
	 * @param current the current editor input
	 * @param frame the stack frame for which the input is required
	 * @param listener the listener to be notified when the computation is completed
	 */
	public void computeInput( final Object current, 
							  final ICStackFrame frame, 
							  final IDisassemblyListener listener ) {
		Runnable runnable = new Runnable() {
			public void run() {
				IStatus status = Status.OK_STATUS;
				IEditorInput input = null;
				if ( current instanceof DisassemblyEditorInput && 
					 ((DisassemblyEditorInput)current).contains( frame ) ) {
					input = (IEditorInput)current;
				}
				else {
					try {
						input = DisassemblyEditorInput.create( frame );
					}
					catch( DebugException e ) {
						status = new Status( IStatus.ERROR, 
											 CDebugUIPlugin.getUniqueIdentifier(),
											 0,
											 e.getMessage(),
											 null );
					}
					
				}
				listener.inputComputed( frame, status, input );
			}
		};
		DebugPlugin.getDefault().asyncExec( runnable );
	}

	protected void selectAndReveal( ICStackFrame frame, IEditorInput input ) {
		IRegion region = getLineInformation( frame, input );
		if ( region != null ) {
			int start = region.getOffset();
			int length = region.getLength();			
			StyledText widget = getSourceViewer().getTextWidget();
			widget.setRedraw( false );
			{
				getSourceViewer().revealRange( start, length );
				getSourceViewer().setSelectedRange( start, 0 );
			}
			widget.setRedraw( true );
			setInstructionPointer( frame, start, length, getDocumentProvider().getAnnotationModel( input ) );
		}
	}

	/**
	 * Returns the line information for the given line in the given editor
	 */
	private IRegion getLineInformation( ICStackFrame frame, IEditorInput input ) {
		if ( input instanceof DisassemblyEditorInput ) {
			int line = ((DisassemblyEditorInput)input).getInstructionLine( frame.getAddress() );
			if ( line > 0 ) {
				try {
					return getSourceViewer().getDocument().getLineInformation( --line );
				}
				catch( BadLocationException e1 ) {
				}
			}
		}
		return null;
	}

	public IEditorInput getInput() {
		if ( getSourceViewer() != null ) {
			Object input = getSourceViewer().getInput();
			if ( input instanceof IEditorInput )
				return (IEditorInput)input;
		}
		return null;
	}

	protected void setInput( IEditorInput input ) {
		getSourceViewer().setInput( input );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createPartControl( Composite parent ) {
		super.createPartControl( parent );
		createViewContextMenu();
		createRulerContextMenu();
		if ( fSourceViewerDecorationSupport != null )
			fSourceViewerDecorationSupport.install( getEditorPreferenceStore() );
		}

	/**
	 * Returns the source viewer decoration support.
	 * 
	 * @return the source viewer decoration support
	 */
	protected SourceViewerDecorationSupport getSourceViewerDecorationSupport( ISourceViewer viewer ) {
		if ( fSourceViewerDecorationSupport == null ) {
			fSourceViewerDecorationSupport = new SourceViewerDecorationSupport( viewer, getOverviewRuler(), getAnnotationAccess(), getSharedColors() );
			configureSourceViewerDecorationSupport( fSourceViewerDecorationSupport );
		}
		return fSourceViewerDecorationSupport;
	}

	/**
	 * Creates the annotation access for this view.
	 * 
	 * @return the created annotation access
	 */
	private IAnnotationAccess createAnnotationAccess() {
		return new DefaultMarkerAnnotationAccess();
	}

	/**
	 * Configures the decoration support for this view's the source viewer.
	 */
	private void configureSourceViewerDecorationSupport( SourceViewerDecorationSupport support ) {
		Iterator e = fAnnotationPreferences.getAnnotationPreferences().iterator();
		while( e.hasNext() )
			support.setAnnotationPreference( (AnnotationPreference)e.next() );
		support.setCursorLinePainterPreferenceKeys( CURRENT_LINE, CURRENT_LINE_COLOR );
	}

	/**
	 * Returns the annotation access. 
	 * 
	 * @return the annotation access
	 */
	private IAnnotationAccess getAnnotationAccess() {
		if ( fAnnotationAccess == null )
			fAnnotationAccess = createAnnotationAccess();
		return fAnnotationAccess;
	}

	private ISharedTextColors getSharedColors() {
		ISharedTextColors sharedColors= EditorsPlugin.getDefault().getSharedTextColors();
		return sharedColors;
	}

	private IPreferenceStore getEditorPreferenceStore() {
		return EditorsUI.getPreferenceStore();
	}

	public DisassemblyDocumentProvider getDocumentProvider() {
		if ( this.fDocumentProvider == null )
			this.fDocumentProvider = new DisassemblyDocumentProvider();
		return this.fDocumentProvider;
	}

	protected void setInstructionPointer( ICStackFrame frame, int start, int length, IAnnotationModel model ) {
		Assert.isNotNull( model );
		boolean tos = isTopStackFrame( frame );
		DisassemblyInstructionPointerAnnotation instPtrAnnotation = new DisassemblyInstructionPointerAnnotation( frame, tos );
		Position position = new Position( start, length );		
		DisassemblyInstructionPointerAnnotation oldPointer = getCurrentInstructionPointer();
		if ( oldPointer != null )
			model.removeAnnotation( oldPointer );
		model.addAnnotation( instPtrAnnotation, position );
		setCurrentInstructionPointer( instPtrAnnotation );
	}

	private boolean isTopStackFrame( ICStackFrame stackFrame ) {
		IThread thread = stackFrame.getThread();
		boolean tos = false;
		try {
			tos = stackFrame.equals( thread.getTopStackFrame() );
		}
		catch( DebugException e ) {
		}
		return tos;
	}

	private DisassemblyInstructionPointerAnnotation getCurrentInstructionPointer() {
		return fInstrPointerAnnotation;
	}

	private void setCurrentInstructionPointer( DisassemblyInstructionPointerAnnotation instrPointer ) {
		fInstrPointerAnnotation = instrPointer;
	}

	protected void removeCurrentInstructionPointer( IAnnotationModel model ) {
		Assert.isNotNull( model );
		DisassemblyInstructionPointerAnnotation instrPointer = getCurrentInstructionPointer();
		if ( instrPointer != null ) {
			model.removeAnnotation( instrPointer );
			setCurrentInstructionPointer( null );
		}
	}

	protected void resetViewerInput() {
		SourceViewer viewer = getSourceViewer();
		if ( viewer == null )
			return;

		IEditorInput input = DisassemblyEditorInput.EMPTY_EDITOR_INPUT; 
		setInput( input );
		showViewer();
		try {
			getDocumentProvider().connect( input );
		}
		catch( CoreException e ) {
			// never happens
		}
		IAnnotationModel model = getDocumentProvider().getAnnotationModel( input );
		getSourceViewer().setDocument( getDocumentProvider().getDocument( input ), model );
		removeCurrentInstructionPointer( model );

		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#becomesHidden()
	 */
	protected void becomesHidden() {
		selectionChanged( null, new StructuredSelection() );
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

	/**
	 * Returns the overview ruler.
	 * 
	 * @return the overview ruler
	 */
	private IOverviewRuler getOverviewRuler() {
		if ( fOverviewRuler == null )
			fOverviewRuler = createOverviewRuler( getSharedColors() );
		return fOverviewRuler;
	}

	protected String getRulerContextMenuId() {
		return this.fRulerContextMenuId;
	}

	private void setRulerContextMenuId( String rulerContextMenuId ) {
		Assert.isNotNull( rulerContextMenuId );
		this.fRulerContextMenuId = rulerContextMenuId;
	}

	protected String getViewContextMenuId() {
		return this.fViewContextMenuId;
	}

	private void setViewContextMenuId( String viewContextMenuId ) {
		Assert.isNotNull( viewContextMenuId );
		this.fViewContextMenuId = viewContextMenuId;
	}

	/**
	 * Sets up the ruler context menu before it is made visible.
	 * 
	 * @param menu the menu
	 */
	protected void rulerContextMenuAboutToShow( IMenuManager menu ) {
		menu.add( new Separator( ITextEditorActionConstants.GROUP_REST ) );
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
		addAction( menu, IInternalCDebugUIConstants.ACTION_TOGGLE_BREAKPOINT );
		addAction( menu, IInternalCDebugUIConstants.ACTION_ENABLE_DISABLE_BREAKPOINT );
		addAction( menu, IInternalCDebugUIConstants.ACTION_BREAKPOINT_PROPERTIES );
	}

	/**
	 * Sets up the view context menu before it is made visible.
	 * 
	 * @param menu the menu
	 */
	protected void viewContextMenuAboutToShow( IMenuManager menu ) {
		menu.add( new Separator( ITextEditorActionConstants.GROUP_REST ) );
		menu.add( new Separator( IWorkbenchActionConstants.MB_ADDITIONS ) );
	}

	/**
	 * Convenience method to add the action installed under the given action id to the specified group of the menu.
	 * @param menu the menu to add the action to
	 * @param group the group in the menu
	 * @param actionId the id of the action to add
	 */
	protected final void addAction( IMenuManager menu, String group, String actionId ) {
		IAction action = getAction( actionId );
		if ( action != null ) {
			if ( action instanceof IUpdate )
				((IUpdate)action).update();
			IMenuManager subMenu = menu.findMenuUsingPath( group );
			if ( subMenu != null )
				subMenu.add( action );
			else
				menu.appendToGroup( group, action );
		}
	}

	/**
	 * Convenience method to add the action installed under the given action id to the given menu.
	 * @param menu the menu to add the action to
	 * @param actionId the id of the action to be added
	 */
	protected final void addAction( IMenuManager menu, String actionId ) {
		IAction action = getAction( actionId );
		if ( action != null ) {
			if ( action instanceof IUpdate )
				((IUpdate)action).update();
			menu.add( action );
		}
	}

	protected Menu getRulerContextMenu() {
		return this.fRulerContextMenu;
	}

	private void setRulerContextMenu( Menu rulerContextMenu ) {
		this.fRulerContextMenu = rulerContextMenu;
	}

	private void createViewContextMenu() {
		String id = getViewContextMenuId();
		MenuManager manager = new MenuManager( id, id );
		manager.setRemoveAllWhenShown( true );
		manager.addMenuListener( getContextMenuListener() );
		StyledText styledText = getSourceViewer().getTextWidget();
		setTextContextMenu( manager.createContextMenu( styledText ) );
		styledText.setMenu( getTextContextMenu() );

		// register the context menu such that other plugins may contribute to it
		if ( getSite() != null ) {
			getSite().registerContextMenu( getViewContextMenuId(), manager, getSourceViewer() );
		}
	}

	private void createRulerContextMenu() {
		String id = getRulerContextMenuId();
		MenuManager manager = new MenuManager( id, id );
		manager.setRemoveAllWhenShown( true );
		manager.addMenuListener( getContextMenuListener() );
		Control rulerControl = fVerticalRuler.getControl();
		setRulerContextMenu( manager.createContextMenu( rulerControl ) );
		rulerControl.setMenu( getRulerContextMenu() );
		rulerControl.addMouseListener( getRulerMouseListener() );
	}

	private Menu getTextContextMenu() {
		return this.fTextContextMenu;
	}

	private void setTextContextMenu( Menu textContextMenu ) {
		this.fTextContextMenu = textContextMenu;
	}

	public void setAction( String actionID, IAction action ) {
		Assert.isNotNull( actionID );
		if ( action == null ) {
			action = (IAction)fActions.remove( actionID );
		}
		else {
			fActions.put( actionID, action );
		}
	}

	public IAction getAction( String actionID ) {
		Assert.isNotNull( actionID );
		return (IAction)fActions.get( actionID );
	}

	private IVerticalRuler getVerticalRuler() {
		return this.fVerticalRuler;
	}

	private TextPresentation createTextPresentation( IRegion[] regions ) {
		TextPresentation p = new TextPresentation();
		for ( int i = 0; i < regions.length; ++i ) {
			p.addStyleRange( new StyleRange( regions[i].getOffset(), 
											 regions[i].getLength(), 
											 CDebugUIPlugin.getPreferenceColor( IInternalCDebugUIConstants.DISASSEMBLY_SOURCE_LINE_COLOR ),
											 null ) );
		}
		return p;
	}

	protected void refresh( IDisassembly disassembly ) {
		if ( !(getInput() instanceof DisassemblyEditorInput) || !disassembly.equals( ((DisassemblyEditorInput)getInput()).getDisassembly() ) )
			return;
		resetViewerInput();
		if ( !isAvailable() || !isVisible() )
			return;
		IAdaptable context = DebugUITools.getDebugContext();
		if ( context instanceof ICStackFrame ) {
			fLastStackFrame = (ICStackFrame)context;
			IEditorInput input = getInput();
			if ( input instanceof DisassemblyEditorInput )
				setViewerInput( DisassemblyEditorInput.PENDING_EDITOR_INPUT );
			computeInput( input, (ICStackFrame)context, this );
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#partActivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partActivated( IWorkbenchPart part ) {
		if ( this.equals( part ) ) {
			CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON, true );
		}
		super.partActivated( part );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.part.PageBookView#partDeactivated(org.eclipse.ui.IWorkbenchPart)
	 */
	public void partDeactivated( IWorkbenchPart part ) {
		if ( this.equals( part ) ) {
			CDebugCorePlugin.getDefault().getPluginPreferences().setValue( ICDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON, false );
		}
		super.partDeactivated( part );
	}
}
