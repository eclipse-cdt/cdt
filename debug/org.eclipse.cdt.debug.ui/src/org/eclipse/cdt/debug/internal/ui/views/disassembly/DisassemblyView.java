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
package org.eclipse.cdt.debug.internal.ui.views.disassembly;

import java.util.Iterator;

import org.eclipse.cdt.debug.core.model.IAsmInstruction;
import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICStackFrame;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.internal.ui.ICDebugHelpContextIds;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandler;
import org.eclipse.cdt.debug.internal.ui.views.AbstractDebugEventHandlerView;
import org.eclipse.cdt.debug.internal.ui.views.IDebugExceptionHandler;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.IAnnotationAccess;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.INullSelectionListener;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;
import org.eclipse.ui.texteditor.AnnotationPreference;
import org.eclipse.ui.texteditor.DefaultMarkerAnnotationAccess;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.ExtendedTextEditorPreferenceConstants;
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
	 * The width of the vertical ruler.
	 */
	private final static int VERTICAL_RULER_WIDTH = 12;

	/**
	 * Preference key for highlighting current line.
	 */
	private final static String CURRENT_LINE = ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE;

	/**
	 * Preference key for highlight color of current line.
	 */
	private final static String CURRENT_LINE_COLOR = ExtendedTextEditorPreferenceConstants.EDITOR_CURRENT_LINE_COLOR;

	/**
	 * The vertical ruler.
	 */
	private IVerticalRuler fVerticalRuler;

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
	 * Constructor for DisassemblyView.
	 */
	public DisassemblyView() {
		super();
		fAnnotationPreferences = new MarkerAnnotationPreferences();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractDebugView#createViewer(org.eclipse.swt.widgets.Composite)
	 */
	protected Viewer createViewer( Composite parent ) {
		fVerticalRuler = createVerticalRuler();
		
		SourceViewer viewer = createSourceViewer( parent, fVerticalRuler );
		getSourceViewerDecorationSupport( viewer );
		
		EditorsPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );
		CDebugUIPlugin.getDefault().getPreferenceStore().addPropertyChangeListener( this );

		getSite().getPage().addSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		setEventHandler( createEventHandler() );
		
		viewer.setDocument( getDocumentProvider().getDocument( DisassemblyEditorInput.EMPTY_EDITOR_INPUT ), getDocumentProvider().getAnnotationModel( null ) );

		resetViewerInput();

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
		// TODO Auto-generated method stub
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
	private IVerticalRuler createVerticalRuler() {
		return new VerticalRuler( VERTICAL_RULER_WIDTH, getAnnotationAccess() );
	}

	/**
	 * Creates the source viewer to be used by this view.
	 *
	 * @param parent the parent control
	 * @param ruler the vertical ruler
	 * @param styles style bits
	 * @return the source viewer
	 */
	private SourceViewer createSourceViewer( Composite parent, IVerticalRuler ruler ) {
		DisassemblyViewer viewer = new DisassemblyViewer( parent, ruler );
		viewer.setRangeIndicator( new DefaultRangeIndicator() );
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
		IDocument document = viewer.getDocument();
		String contents = ((DisassemblyEditorInput)input).getContents();
		document.set( contents );

		updateObjects();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		getSite().getPage().removeSelectionListener( IDebugUIConstants.ID_DEBUG_VIEW, this );
		EditorsPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );
		CDebugUIPlugin.getDefault().getPreferenceStore().removePropertyChangeListener( this );

		if ( fSourceViewerDecorationSupport != null ) {
			fSourceViewerDecorationSupport.dispose();
			fSourceViewerDecorationSupport = null;
		}

		if ( fDocumentProvider != null ) {
			fDocumentProvider.dispose();
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
					IAsmInstruction[] instructions = new IAsmInstruction[0];
					try {
						IDisassembly disassembly = ((ICDebugTarget)frame.getDebugTarget()).getDisassembly();
						if ( disassembly != null ) {
							instructions = disassembly.getInstructions( frame );
							input = new DisassemblyEditorInput( disassembly, instructions );
						}
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
		long address = frame.getAddress();
		IRegion region = getLineInformation( address, input );
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
			setInstructionPointer( frame, start, length );
		}
	}

	/**
	 * Returns the line information for the given line in the given editor
	 */
	private IRegion getLineInformation( long address, IEditorInput input ) {
		if ( input instanceof DisassemblyEditorInput ) {
			int line = ((DisassemblyEditorInput)input).getLineNumber( address );
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

	protected IEditorInput getInput() {
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
			fSourceViewerDecorationSupport = new SourceViewerDecorationSupport( viewer, null, getAnnotationAccess(), getSharedColors() );
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
		return EditorsPlugin.getDefault().getPreferenceStore();
	}

	protected DisassemblyDocumentProvider getDocumentProvider() {
		if ( this.fDocumentProvider == null )
			this.fDocumentProvider = new DisassemblyDocumentProvider();
		return this.fDocumentProvider;
	}

	protected void setInstructionPointer( ICStackFrame frame, int start, int length ) {
		boolean tos = isTopStackFrame( frame );
		DisassemblyInstructionPointerAnnotation instPtrAnnotation = new DisassemblyInstructionPointerAnnotation( frame, tos );
		
		Position position = new Position( start, length );
		
		// Add the annotation at the position to the editor's annotation model.
		// If there is no annotation model, there's nothing more to do
		IAnnotationModel annModel = getDocumentProvider().getAnnotationModel( null );
		if ( annModel == null ) {
			return;
		}
		DisassemblyInstructionPointerAnnotation currentPointer = getCurrentInstructionPointer();
		if ( currentPointer != null )
			removeCurrentInstructionPointer();
		annModel.addAnnotation( instPtrAnnotation, position );
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

	protected void removeCurrentInstructionPointer() {
		DisassemblyInstructionPointerAnnotation instrPointer = getCurrentInstructionPointer();
		if ( instrPointer != null ) {
			IAnnotationModel annModel = getDocumentProvider().getAnnotationModel( null );
			if ( annModel != null ) {
				annModel.removeAnnotation( instrPointer );
				setCurrentInstructionPointer( null );
			}
		}
	}

	protected void resetViewerInput() {
		SourceViewer viewer = getSourceViewer();
		if ( viewer == null )
			return;

		IEditorInput input = DisassemblyEditorInput.EMPTY_EDITOR_INPUT;
		setInput( input );

		showViewer();
		IDocument document = getSourceViewer().getDocument();
		String contents = ((DisassemblyEditorInput)input).getContents();
		document.set( contents );
		removeCurrentInstructionPointer();

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
}
