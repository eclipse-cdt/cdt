/*******************************************************************************
 * Copyright (c) 2006, 2007, 2009 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat Incorporated - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.autotools.ui.editors;

import java.util.ResourceBundle;

import org.eclipse.cdt.autotools.core.AutotoolsPlugin;
import org.eclipse.cdt.autotools.ui.AutotoolsUIPlugin;
import org.eclipse.cdt.autotools.ui.editors.outline.AutoconfContentOutlinePage;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfElement;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfMacroDetector;
import org.eclipse.cdt.autotools.ui.editors.parser.AutoconfParser;
import org.eclipse.cdt.autotools.ui.editors.parser.IAutoconfMacroValidator;
import org.eclipse.cdt.internal.autotools.core.AutotoolsPropertyConstants;
import org.eclipse.cdt.internal.autotools.ui.editors.autoconf.ProjectionFileUpdater;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.IReconcilingParticipant;
import org.eclipse.cdt.internal.autotools.ui.editors.automake.MakefileEditorPreferenceConstants;
import org.eclipse.cdt.internal.autotools.ui.preferences.AutoconfEditorPreferencePage;
import org.eclipse.cdt.internal.autotools.ui.preferences.AutotoolsEditorPreferenceConstants;
import org.eclipse.cdt.internal.autotools.ui.preferences.ColorManager;
import org.eclipse.cdt.internal.autotools.ui.properties.AutotoolsPropertyManager;
import org.eclipse.cdt.internal.autotools.ui.properties.IProjectPropertyListener;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.ITextViewerExtension4;
import org.eclipse.jface.text.ITextViewerExtension5;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextUtilities;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.IAnnotationHoverExtension;
import org.eclipse.jface.text.source.ILineRange;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.ISourceViewerExtension3;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel;
import org.eclipse.jface.text.source.projection.ProjectionSupport;
import org.eclipse.jface.text.source.projection.ProjectionViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.EditorsUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.ChainedPreferenceStore;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.ITextEditorActionDefinitionIds;
import org.eclipse.ui.texteditor.ResourceAction;
import org.eclipse.ui.texteditor.TextEditorAction;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;


public class AutoconfEditor extends TextEditor implements IAutotoolsEditor, IProjectPropertyListener {

	public final static String AUTOCONF_PARTITIONING= "autoconf_partitioning";  //$NON-NLS-1$


    private AutoconfPartitionScanner fPartitionScanner;
    private RuleBasedScanner fCodeScanner;
    private RuleBasedScanner fMacroCodeScanner;
    private static volatile AutoconfDocumentProvider fDocumentProvider;
    private AutoconfElement rootElement;
    private AutoconfContentOutlinePage outlinePage;
    private AutoconfParser fParser;
    private IEditorInput input;
    private IProject fProject;
	
    /** The information provider used to present focusable information shells. */
    private InformationPresenter fInformationPresenter;
	/** 
	 * This editor's projection support 
	 */
    
	ProjectionSupport fProjectionSupport;
	ProjectionFileUpdater fProjectionFileUpdater;

	/**
	 * Reconciling listeners
	 */
	private ListenerList fReconcilingListeners= new ListenerList(ListenerList.IDENTITY);


    public AutoconfEditor() {
    	super();
    }

    protected void initializeEditor() {
    	super.initializeEditor();
    	setDocumentProvider(getAutoconfDocumentProvider());
		IPreferenceStore[] stores = new IPreferenceStore[2];
		stores[0] = AutotoolsPlugin.getDefault().getPreferenceStore();
		stores[1] = EditorsUI.getPreferenceStore();
		ChainedPreferenceStore chainedStore = new ChainedPreferenceStore(stores);
		setPreferenceStore(chainedStore);    	
		setSourceViewerConfiguration(new AutoconfSourceViewerConfiguration(chainedStore, this));
		AutotoolsEditorPreferenceConstants.initializeDefaultValues(stores[0]);
    	AutoconfEditorPreferencePage.initDefaults(stores[0]);
    }

    public static AutoconfDocumentProvider getAutoconfDocumentProvider() {
    	if (fDocumentProvider == null)
    		fDocumentProvider= new AutoconfDocumentProvider();
    	return fDocumentProvider;
    }

    public AutoconfElement getRootElement() {
    	return rootElement;
    }
    
    public void setRootElement(AutoconfElement element) {
    	rootElement = element;
    }

    public ISourceViewer getViewer() {
    	return getSourceViewer();
    }
    
    protected IDocument getInputDocument()
	{
		IDocument document = getDocumentProvider().getDocument(input);
		return document;
	}

    protected void doSetInput(IEditorInput newInput) throws CoreException
	{
    	// If this editor is for a project file, remove this editor as a property
    	// change listener.
    	if (fProject != null)
    		AutotoolsPropertyManager.getDefault().removeProjectPropertyListener(fProject, this);
    	this.fProject = null;
		super.doSetInput(newInput);
		this.input = newInput;

		if (input instanceof IFileEditorInput) {
			IFile f = ((IFileEditorInput)input).getFile();
			fProject = f.getProject();
			// This is a project file.  We want to be notified if the Autoconf editor
			// properties are changed such that the macro versions are changed.
			AutotoolsPropertyManager.getDefault().addProjectPropertyListener(fProject, this);
		}
		getOutlinePage().setInput(input);
		try
		{
			IDocument document = getInputDocument();

			setRootElement(reparseDocument(document, newInput));
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

    @SuppressWarnings({ "unchecked" })
	public Object getAdapter(Class required) {
		if (ProjectionAnnotationModel.class.equals(required)) {
			if (fProjectionSupport != null) {
				Object result = fProjectionSupport.getAdapter(getSourceViewer(), required);
				if (result != null) {
					return result;
				}
			}
		} else if (IContentOutlinePage.class.equals(required)) {
			return getOutlinePage();
		}
		return super.getAdapter(required);
	}
	

    public AutoconfContentOutlinePage getOutlinePage() {
		if (outlinePage == null) {
			outlinePage= new AutoconfContentOutlinePage(this);
			if (getEditorInput() != null)
				outlinePage.setInput(getEditorInput());
		}
		return outlinePage;
	}

    /**
     * Return a scanner for creating Autoconf partitions.
     *
     * @return a scanner for creating Autoconf partitions
     */
    public AutoconfParser getAutoconfParser() {
    	if (fParser == null) {
    		AutoconfErrorHandler errorHandler = new AutoconfErrorHandler(input);
    		IAutoconfMacroValidator macroValidator = new AutoconfEditorMacroValidator(this);
    		fParser = new AutoconfParser(errorHandler, new AutoconfMacroDetector(), macroValidator);
    	}
    	return fParser;
    }
 
	/**
     * Return a scanner for creating Autoconf partitions.
     *
     * @return a scanner for creating Autoconf partitions
     */
    public AutoconfPartitionScanner getAutoconfPartitionScanner() {
    	if (fPartitionScanner == null)
    		fPartitionScanner= new AutoconfPartitionScanner();
    	return fPartitionScanner;
    }
    
    /**
     * Returns the Autoconf code scanner.
     *
     * @return the Autoconf code scanner
     */
    public RuleBasedScanner getAutoconfCodeScanner() {
    	if (fCodeScanner == null)
    		fCodeScanner= new AutoconfCodeScanner();
    	return fCodeScanner;
    }
    
    /**
     * Returns the Autoconf code scanner.
     *
     * @return the Autoconf code scanner
     */
    public RuleBasedScanner getAutoconfMacroCodeScanner() {
    	if (fMacroCodeScanner == null)
    		fMacroCodeScanner= new AutoconfMacroCodeScanner();
    	return fMacroCodeScanner;
    }
    
	/**
	 * Returns the preference color, identified by the given preference.
	 */
	public static Color getPreferenceColor(String key) {
		return ColorManager.getDefault().getColor(PreferenceConverter.getColor(AutotoolsPlugin.getDefault().getPreferenceStore(), key));
	}

	public void handleProjectPropertyChanged(IProject project, String property) {
		if (property.equals(AutotoolsPropertyConstants.AUTOCONF_MACRO_VERSIONING)) {
			ISourceViewer sourceViewer= getSourceViewer();
			if (sourceViewer == null)
				return;
			handleVersionChange(sourceViewer);
		}
	}

	/**
	 * Handle the case whereby the Autoconf or Automake macro versions to use
	 * for this project are changed in which case we want to invalidate and reparse 
	 * the document.
	 * 
	 * @param sourceViewer
	 */
	protected void handleVersionChange(ISourceViewer sourceViewer) {
		sourceViewer.invalidateTextPresentation();
		try {
			IDocument document = getInputDocument();

			setRootElement(reparseDocument(document, getEditorInput()));
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private AutoconfElement reparseDocument(IDocument document,
			IEditorInput editorInput) {
		AutoconfParser parser = getAutoconfParser();
		((AutoconfErrorHandler)parser.getErrorHandler()).removeAllExistingMarkers();
		
		return parser.parse(document);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#handlePreferenceStoreChanged(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		ISourceViewer sourceViewer= getSourceViewer();
		if (sourceViewer == null)
			return;

        String property = event.getProperty();

        AutoconfCodeScanner scanner = (AutoconfCodeScanner)getAutoconfCodeScanner();
        if (scanner != null) {
        	if (scanner.affectsBehavior(event)) {
        		scanner.adaptToPreferenceChange(event);
        		sourceViewer.invalidateTextPresentation();
        	}
        }

        if (AutotoolsEditorPreferenceConstants.AUTOCONF_VERSION.equals(property) ||
        		AutotoolsEditorPreferenceConstants.AUTOMAKE_VERSION.equals(property)) {
        	handleVersionChange(sourceViewer);
        } else if (AutotoolsEditorPreferenceConstants.EDITOR_FOLDING_ENABLED.equals(property)) {
			if (sourceViewer instanceof ProjectionViewer) {
				ProjectionViewer projectionViewer= (ProjectionViewer) sourceViewer;
				if (fProjectionFileUpdater != null)
					fProjectionFileUpdater.uninstall();
				// either freshly enabled or provider changed
				fProjectionFileUpdater= new ProjectionFileUpdater();
				if (fProjectionFileUpdater != null) {
					fProjectionFileUpdater.install(this, projectionViewer);
				}
			}
			return;
		}

		super.handlePreferenceStoreChanged(event);
	}

    /**
     * Information provider used to present focusable information shells.
     *
     * @since 3.1.1
     */
    private static final class InformationProvider implements IInformationProvider, IInformationProviderExtension, IInformationProviderExtension2 {

    	private IRegion fHoverRegion;
    	private Object fHoverInfo;
    	private IInformationControlCreator fControlCreator;

    	InformationProvider(IRegion hoverRegion, Object hoverInfo, IInformationControlCreator controlCreator) {
    		fHoverRegion= hoverRegion;
    		fHoverInfo= hoverInfo;
    		fControlCreator= controlCreator;
    	}
    	/*
    	 * @see org.eclipse.jface.text.information.IInformationProvider#getSubject(org.eclipse.jface.text.ITextViewer, int)
    	 */
    	public IRegion getSubject(ITextViewer textViewer, int invocationOffset) {
    		return fHoverRegion;
    	}
    	/*
    	 * @see org.eclipse.jface.text.information.IInformationProvider#getInformation(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
    	 */
    	public String getInformation(ITextViewer textViewer, IRegion subject) {
    		return fHoverInfo.toString();
    	}
    	
    	/*
    	 * @see org.eclipse.jface.text.information.IInformationProviderExtension#getInformation2(org.eclipse.jface.text.ITextViewer, org.eclipse.jface.text.IRegion)
    	 * @since 3.2
    	 */
    	public Object getInformation2(ITextViewer textViewer, IRegion subject) {
    		return fHoverInfo;
    	}
    	/*
    	 * @see org.eclipse.jface.text.information.IInformationProviderExtension2#getInformationPresenterControlCreator()
    	 */
    	public IInformationControlCreator getInformationPresenterControlCreator() {
    		return fControlCreator;
    	}
    }

    /**
     * This action behaves in two different ways: If there is no current text
     * hover, the tooltip is displayed using information presenter. If there is
     * a current text hover, it is converted into a information presenter in
     * order to make it sticky.
     * @since 3.1.1
     */
    class InformationDispatchAction extends TextEditorAction {

    	/** The wrapped text operation action. */
    	private final TextOperationAction fTextOperationAction;

    	/**
    	 * Creates a dispatch action.
    	 *
    	 * @param resourceBundle the resource bundle
    	 * @param prefix the prefix
    	 * @param textOperationAction the text operation action
    	 */
    	public InformationDispatchAction(ResourceBundle resourceBundle, String prefix, final TextOperationAction textOperationAction) {
    		super(resourceBundle, prefix, AutoconfEditor.this);
    		if (textOperationAction == null)
    			throw new IllegalArgumentException();
    		fTextOperationAction= textOperationAction;
    	}

    	/*
    	 * @see org.eclipse.jface.action.IAction#run()
    	 */
    	public void run() {

    		ISourceViewer sourceViewer= getSourceViewer();
    		if (sourceViewer == null) {
    			fTextOperationAction.run();
    			return;
    		}

    		if (sourceViewer instanceof ITextViewerExtension4)  {
    			ITextViewerExtension4 extension4= (ITextViewerExtension4) sourceViewer;
    			if (extension4.moveFocusToWidgetToken())
    				return;
    		}

    		if (sourceViewer instanceof ITextViewerExtension2) {
    			// does a text hover exist?
    			ITextHover textHover= ((ITextViewerExtension2) sourceViewer).getCurrentTextHover();
    			if (textHover != null && makeTextHoverFocusable(sourceViewer, textHover))
    				return;
    		}

    		if (sourceViewer instanceof ISourceViewerExtension3) {
    			// does an annotation hover exist?
    			IAnnotationHover annotationHover= ((ISourceViewerExtension3) sourceViewer).getCurrentAnnotationHover();
    			if (annotationHover != null && makeAnnotationHoverFocusable(sourceViewer, annotationHover))
    				return;
    		}

    		// otherwise, just display the tooltip
    		//fTextOperationAction.run();
    	}

    	/**
    	 * Tries to make a text hover focusable (or "sticky").
    	 * 
    	 * @param sourceViewer the source viewer to display the hover over
    	 * @param textHover the hover to make focusable
    	 * @return <code>true</code> if successful, <code>false</code> otherwise
    	 */
    	@SuppressWarnings("deprecation")
		private boolean makeTextHoverFocusable(ISourceViewer sourceViewer, ITextHover textHover) {
    		Point hoverEventLocation= ((ITextViewerExtension2) sourceViewer).getHoverEventLocation();
    		int offset= computeOffsetAtLocation(sourceViewer, hoverEventLocation.x, hoverEventLocation.y);
    		if (offset == -1)
    			return false;

    		try {
    			IRegion hoverRegion= textHover.getHoverRegion(sourceViewer, offset);
    			if (hoverRegion == null)
    				return false;

    			String hoverInfo= textHover.getHoverInfo(sourceViewer, hoverRegion);

    			IInformationControlCreator controlCreator= null;
    			if (textHover instanceof IInformationProviderExtension2)
    				controlCreator= ((IInformationProviderExtension2)textHover).getInformationPresenterControlCreator();

    			IInformationProvider informationProvider= new InformationProvider(hoverRegion, hoverInfo, controlCreator);

    			fInformationPresenter.setOffset(offset);
    			fInformationPresenter.setAnchor(AbstractInformationControlManager.ANCHOR_BOTTOM);
    			fInformationPresenter.setMargins(6, 6); // default values from AbstractInformationControlManager
    			String contentType= TextUtilities.getContentType(sourceViewer.getDocument(), AutoconfPartitionScanner.AUTOCONF_MACRO, offset, true);
    			fInformationPresenter.setInformationProvider(informationProvider, contentType);
    			fInformationPresenter.showInformation();

    			return true;

    		} catch (BadLocationException e) {
    			return false;
    		}
    	}

    	/**
    	 * Tries to make an annotation hover focusable (or "sticky").
    	 * 
    	 * @param sourceViewer the source viewer to display the hover over
    	 * @param annotationHover the hover to make focusable
    	 * @return <code>true</code> if successful, <code>false</code> otherwise
    	 */
    	private boolean makeAnnotationHoverFocusable(ISourceViewer sourceViewer, IAnnotationHover annotationHover) {
    		IVerticalRulerInfo info= getVerticalRuler();
    		int line= info.getLineOfLastMouseButtonActivity();
    		if (line == -1)
    			return false;

    		try {

    			// compute the hover information
    			Object hoverInfo;
    			if (annotationHover instanceof IAnnotationHoverExtension) {
    				IAnnotationHoverExtension extension= (IAnnotationHoverExtension) annotationHover;
    				ILineRange hoverLineRange= extension.getHoverLineRange(sourceViewer, line);
    				if (hoverLineRange == null)
    					return false;
    				final int maxVisibleLines= Integer.MAX_VALUE; // allow any number of lines being displayed, as we support scrolling
    				hoverInfo= extension.getHoverInfo(sourceViewer, hoverLineRange, maxVisibleLines);
    			} else {
    				hoverInfo= annotationHover.getHoverInfo(sourceViewer, line);
    			}

    			// hover region: the beginning of the concerned line to place the control right over the line
    			IDocument document= sourceViewer.getDocument();
    			int offset= document.getLineOffset(line);
    			String contentType= TextUtilities.getContentType(document, AutoconfPartitionScanner.AUTOCONF_MACRO, offset, true);

    			IInformationControlCreator controlCreator= null;

//    			/* 
//    			 * XXX: This is a hack to avoid API changes at the end of 3.2,
//    			 * and should be fixed for 3.3, see: https://bugs.eclipse.org/bugs/show_bug.cgi?id=137967
//    			 */
//    			if ("org.eclipse.jface.text.source.projection.ProjectionAnnotationHover".equals(annotationHover.getClass().getName())) { //$NON-NLS-1$
//    				controlCreator= new IInformationControlCreator() {
//    					public IInformationControl createInformationControl(Shell shell) {
//    						int shellStyle= SWT.RESIZE | SWT.TOOL | getOrientation();
//    						int style= SWT.V_SCROLL | SWT.H_SCROLL;
//    						return new SourceViewerInformationControl(shell, shellStyle, style);
//    					}
//    				};
//
//    			} else {
    			if (annotationHover instanceof IInformationProviderExtension2)
    				controlCreator= ((IInformationProviderExtension2) annotationHover).getInformationPresenterControlCreator();
    			else if (annotationHover instanceof IAnnotationHoverExtension)
    				controlCreator= ((IAnnotationHoverExtension) annotationHover).getHoverControlCreator();
//    			}

    			IInformationProvider informationProvider= new InformationProvider(new Region(offset, 0), hoverInfo, controlCreator);

    			fInformationPresenter.setOffset(offset);
    			fInformationPresenter.setAnchor(AbstractInformationControlManager.ANCHOR_RIGHT);
    			fInformationPresenter.setMargins(4, 0); // AnnotationBarHoverManager sets (5,0), minus SourceViewer.GAP_SIZE_1
    			fInformationPresenter.setInformationProvider(informationProvider, contentType);
    			fInformationPresenter.showInformation();

    			return true;

    		} catch (BadLocationException e) {
    			return false;
    		}
    	}

    	// modified version from TextViewer
    	private int computeOffsetAtLocation(ITextViewer textViewer, int x, int y) {

    		StyledText styledText= textViewer.getTextWidget();
    		IDocument document= textViewer.getDocument();

    		if (document == null)
    			return -1;

    		try {
    			int widgetOffset= styledText.getOffsetAtLocation(new Point(x, y));
    			Point p= styledText.getLocationAtOffset(widgetOffset);
    			if (p.x > x)
    				widgetOffset--;

    			if (textViewer instanceof ITextViewerExtension5) {
    				ITextViewerExtension5 extension= (ITextViewerExtension5) textViewer;
    				return extension.widgetOffset2ModelOffset(widgetOffset);
    			} else {
    				IRegion visibleRegion= textViewer.getVisibleRegion();
    				return widgetOffset + visibleRegion.getOffset();
    			}
    		} catch (IllegalArgumentException e) {
    			return -1;
    		}

    	}
    }

	/**
	 * Adds the given listener.
	 * Has no effect if an identical listener was not already registered.
	 * 
	 * @param listener	The reconcile listener to be added
	 */
	public final void addReconcilingParticipant(IReconcilingParticipant listener) {
		synchronized (fReconcilingListeners) {
			fReconcilingListeners.add(listener);
		}
	}
	
	/**
	 * Removes the given listener.
	 * Has no effect if an identical listener was not already registered.
	 * 
	 * @param listener	the reconcile listener to be removed
	 */
	final void removeReconcilingParticipant(IReconcilingParticipant listener) {
		synchronized (fReconcilingListeners) {
			fReconcilingListeners.remove(listener);
		}
	}
	
	/*
	 */
	public void reconciled() {
		// Notify listeners
		Object[] listeners = fReconcilingListeners.getListeners();
		for (int i = 0, length= listeners.length; i < length; ++i) {
			((IReconcilingParticipant)listeners[i]).reconciled();
		}
	}

    /**
     * Determines is folding enabled.
     * @return <code>true</code> if folding is enabled, <code>false</code> otherwise.
     */
	boolean isFoldingEnabled() {
		return AutotoolsPlugin.getDefault().getPreferenceStore().getBoolean(MakefileEditorPreferenceConstants.EDITOR_FOLDING_ENABLED);
	}

    /* (non-Javadoc)
	 * @see org.eclipse.ui.editors.text.TextEditor#initializeKeyBindingScopes()
	 */
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String [] { AutotoolsUIPlugin.getUniqueIdentifier() + ".editor.scope" } ); //$NON-NLS-1$
	}


    /**
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#createActions()
     */
    protected void createActions() {
    	super.createActions();
    	// TODO: Figure out how to do this later. 		
//  	fFoldingGroup= new FoldingActionGroup(this, getSourceViewer());

    	// Sticky hover support
    	ResourceAction resAction= new TextOperationAction(AutoconfEditorMessages.getResourceBundle(), "ShowToolTip.", this, ISourceViewer.INFORMATION, true); //$NON-NLS-1$
    	resAction= new InformationDispatchAction(AutoconfEditorMessages.getResourceBundle(), "ShowToolTip.", (TextOperationAction) resAction); //$NON-NLS-1$
    	resAction.setActionDefinitionId(IAutotoolEditorActionDefinitionIds.SHOW_TOOLTIP);
    	setAction("ShowToolTip", resAction); //$NON-NLS-1$
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(resAction, IAutotoolHelpContextIds.SHOW_TOOLTIP_ACTION);

    	// Content assist
    	Action action = new ContentAssistAction(AutoconfEditorMessages.getResourceBundle(), "ContentAssistProposal.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ITextEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); //$NON-NLS-1$
		markAsStateDependentAction("ContentAssistProposal", true); //$NON-NLS-1$
    	PlatformUI.getWorkbench().getHelpSystem().setHelp(action, IAutotoolHelpContextIds.CONTENT_ASSIST);
   }

    /**
     * The <code>AbstractTextEditor</code> implementation of this 
     * <code>IWorkbenchPart</code> method creates the vertical ruler and
     * source viewer. Subclasses may extend.
     * 
     * We attach our own mouseDown listener on the menu bar, 
     * and our own listener for cursor/key/selection events to update cursor position in
     * status bar.

     * @param parent Parent composite of the control.
     */
    public void createPartControl(Composite parent) {
    	super.createPartControl(parent);

    	// Sticky hover support
    	IInformationControlCreator informationControlCreator= new IInformationControlCreator() {
    		public IInformationControl createInformationControl(Shell shell) {
       			return new DefaultInformationControl(shell, true);
    		}
    	};

    	fInformationPresenter= new InformationPresenter(informationControlCreator);
    	fInformationPresenter.setSizeConstraints(60, 10, true, true);
    	fInformationPresenter.install(getSourceViewer());
    	fInformationPresenter.setDocumentPartitioning(AutoconfPartitionScanner.AUTOCONF_MACRO);

    	ProjectionViewer projectionViewer= (ProjectionViewer) getSourceViewer();

    	fProjectionSupport= new ProjectionSupport(projectionViewer, getAnnotationAccess(), getSharedColors());
    	fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.error"); //$NON-NLS-1$
    	fProjectionSupport.addSummarizableAnnotationType("org.eclipse.ui.workbench.texteditor.warning"); //$NON-NLS-1$
    	fProjectionSupport.install();

    	if (isFoldingEnabled())
    		projectionViewer.doOperation(ProjectionViewer.TOGGLE);
    	
		fProjectionFileUpdater= new ProjectionFileUpdater();
		if (fProjectionFileUpdater != null) {
			fProjectionFileUpdater.install(this, projectionViewer);
			fProjectionFileUpdater.initialize();
		}

    	PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, IAutotoolHelpContextIds.AC_EDITOR_VIEW);

    	// TODO: Do we need the following two lines?
//  	fEditorSelectionChangedListener= new EditorSelectionChangedListener();
//  	fEditorSelectionChangedListener.install(getSelectionProvider());
    }

    protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
    	ISourceViewer viewer = new ProjectionViewer(parent, ruler, getOverviewRuler(), isOverviewRulerVisible(), styles);

    	// ensure decoration support has been created and configured.
    	getSourceViewerDecorationSupport(viewer);

    	return viewer;
    }
    
    
	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchPart#dispose()
	 */
	public void dispose() {
		if (fProjectionFileUpdater != null) {
			fProjectionFileUpdater.uninstall();
			fProjectionFileUpdater= null;
		}
		if (fProject != null) {
			AutotoolsPropertyManager.getDefault().removeProjectPropertyListener(fProject, this);
		}
		super.dispose();
	}

	/*
	 * @see org.eclipse.ui.texteditor.AbstractTextEditor#performRevert()
	 */
	protected void performRevert() {
		ProjectionViewer projectionViewer= (ProjectionViewer) getSourceViewer();
		projectionViewer.setRedraw(false);
		try {

			boolean projectionMode= projectionViewer.isProjectionMode();
			if (projectionMode) {
				projectionViewer.disableProjection();
				if (fProjectionFileUpdater != null)
					fProjectionFileUpdater.uninstall();
			}

			super.performRevert();

			if (projectionMode) {
				if (fProjectionFileUpdater != null)
					fProjectionFileUpdater.install(this, projectionViewer);
				projectionViewer.enableProjection();
			}

		} finally {
			projectionViewer.setRedraw(true);
		}
	}

	public  IProject getProject() {
		return this.fProject;
	}

}
