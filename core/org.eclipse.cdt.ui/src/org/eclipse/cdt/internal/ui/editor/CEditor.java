package org.eclipse.cdt.internal.ui.editor;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
 
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ISourceRange;
import org.eclipse.cdt.core.model.ISourceReference;
import org.eclipse.cdt.internal.ui.IContextMenuConstants;
import org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.internal.ui.text.CTextTools;
import org.eclipse.cdt.internal.ui.text.IColorManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextOperationTarget;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITextViewerExtension;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.AnnotationRulerColumn;
import org.eclipse.jface.text.source.CompositeRuler;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.IVerticalRulerColumn;
import org.eclipse.jface.text.source.LineNumberRulerColumn;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.EditorActionBarContributor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ContentAssistAction;
import org.eclipse.ui.texteditor.DefaultRangeIndicator;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.MarkerAnnotation;
import org.eclipse.ui.texteditor.MarkerUtilities;
import org.eclipse.ui.texteditor.TextOperationAction;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.eclipse.ui.views.tasklist.TaskList;
/**
 * C specific text editor.
 */
public class CEditor extends TextEditor implements ISelectionChangedListener {


	/** The outline page */
	protected CContentOutlinePage fOutlinePage;
	
	private SearchForReferencesAction fSearchForReferencesAction;
	
	/** Status bar fields -- @@@ gone with Eclipse 2.0 */
	private Map fStatusFields;
	private boolean fInserting= true;
	
	/** The editor's foreground color -- gone in 2.0 */
	private Color fForegroundColor;
	/** The editor's background color */
	private Color fBackgroundColor;
	
	
	protected ISelectionChangedListener fStatusLineClearer;
	
	/** The editor's paint manager */
	private PaintManager fPaintManager;
	/** The editor's bracket painter */
	private BracketPainter fBracketPainter;
	/** The editor's line painter */
	private LinePainter fLinePainter;
	/** The editor's problem painter */
	private ProblemPainter fProblemPainter;
	/** The editor's print margin ruler painter */
	private PrintMarginPainter fPrintMarginPainter;
	/** The editor's tab converter */
	private TabConverter fTabConverter;
	
	/** Listener to annotation model changes that updates the error tick in the tab image */
	private CEditorErrorTickUpdater fCEditorErrorTickUpdater;
	
	/** The line number ruler column */
	private LineNumberRulerColumn fLineNumberRulerColumn;


	/* Preference key line color shading */
	public final static String CURRENT_LINE=  "CEditor.currentLine";	
	/* Preference key for color of shading */
	public final static String CURRENT_LINE_COLOR=  "CEditor.currentLineColor";	
	/* Preference key for matching brackets */
	public final static String MATCHING_BRACKETS=  "matchingBrackets";
	/* Preference key for matching brackets color */
	public final static String MATCHING_BRACKETS_COLOR=  "matchingBracketsColor";
	/* Preference key for matching brackets color */
	public final static String MATCHING_BRACKETS_NOBOX=  "matchingBracketsNobox";
	/* Preference key for matching brackets color */
	public final static String PREFERENCE_COLOR_BACKGROUND =  "CEditor.preferenceColorBackground";
	/* Preference key for matching brackets color */
	public final static String PREFERENCE_COLOR_FOREGROUND =  "CEditor.preferenceColorForeground";
	/* Preference key for matching brackets color */
	public final static String PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT =  "CEditor.preferenceColorBackgroundDefault";
	/* Preference key for matching brackets color */
	public final static String PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT =  "CEditor.preferenceColorForegroundDefault";
	/** Preference key for problem indication */
	public final static String PROBLEM_INDICATION= "problemIndication";
	/** Preference key for problem highlight color */
	public final static String PROBLEM_INDICATION_COLOR= "problemIndicationColor";
		/** Preference key for showing print marging ruler */
	public final static String PRINT_MARGIN= "printMargin";
	/** Preference key for print margin ruler color */
	public final static String PRINT_MARGIN_COLOR= "printMarginColor";
	/** Preference key for print margin ruler column */
	public final static String PRINT_MARGIN_COLUMN= "printMarginColumn";
	/** Preference key for inserting spaces rather than tabs */
	public final static String SPACES_FOR_TABS= "spacesForTabs";
	/** Preference key for linked position color */
	public final static String LINKED_POSITION_COLOR= "linkedPositionColor"; //$NON-NLS-1$
	/** Preference key for shwoing the overview ruler */
	public final static String OVERVIEW_RULER= "overviewRuler"; //$NON-NLS-1$
	
	/** Preference key for showing the line number ruler */
	public final static String LINE_NUMBER_RULER= "lineNumberRuler"; //$NON-NLS-1$
	/** Preference key for the foreground color of the line numbers */
	public final static String LINE_NUMBER_COLOR= "lineNumberColor"; //$NON-NLS-1$
	

	/**
	 * Default constructor.
	 */
	public CEditor() {
		super();
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		setSourceViewerConfiguration(new CSourceViewerConfiguration(textTools, this));
		setDocumentProvider(CUIPlugin.getDefault().getDocumentProvider());
		setRangeIndicator(new DefaultRangeIndicator());
		setPreferenceStore(CUIPlugin.getDefault().getPreferenceStore());
		
		setEditorContextMenuId("#CEditorContext"); //$NON-NLS-1$
		setRulerContextMenuId("#CEditorRulerContext"); //$NON-NLS-1$
		setOutlinerContextMenuId("#CEditorOutlinerContext"); //$NON-NLS-1$
		
		fCEditorErrorTickUpdater= new CEditorErrorTickUpdater(this);
	}
	
	/**
	 * @see AbstractTextEditor#doSetInput(IEditorInput)
	 */
	protected void doSetInput(IEditorInput input) throws CoreException {
		super.doSetInput(input);
		fCEditorErrorTickUpdater.setAnnotationModel(getDocumentProvider().getAnnotationModel(input));
	}
	
	/**
	 * Update the title image
	 */
	public void updatedTitleImage(Image image) {
		setTitleImage(image);
	}


	/**
	 * @see IEditorPart#init(IWorkbenchPartSite, Object)
	 */
	public void init(IEditorSite site, IEditorInput input) throws PartInitException {
		//if (!(input instanceof IFileEditorInput)) {
		//	throw new PartInitException(CUIPlugin.getResourceString("Editor.error.invalid_input"));
		//}
		super.init(site, input);
	}


	/**
	 * Gets the current input
	 */	
	public IFile getInputFile() {
		//IFileEditorInput editorInput = (IFileEditorInput)getEditorInput();
		IEditorInput editorInput= (IEditorInput)getEditorInput();
		if (editorInput != null) {
			if ((editorInput instanceof IFileEditorInput)) {
				return ((IFileEditorInput)editorInput).getFile();
			}
		}
		return null;
	}


	public boolean isSaveAsAllowed() {
		return true;
	}
	/*
	 * 1GEUSSR: ITPUI:ALL - User should never loose changes made in the editors.
	 * Changed behavior to make sure that if called inside a regular save (because
	 * of deletion of input element) there is a way to report back to the caller.
	 */	
	protected void performSaveAs(IProgressMonitor progressMonitor) {
		
		Shell shell= getSite().getShell();
		
		SaveAsDialog dialog= new SaveAsDialog(shell);
		if (dialog.open() == Dialog.CANCEL) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}
			
		IPath filePath= dialog.getResult();
		if (filePath == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);
			return;
		}
			
		filePath= filePath.removeTrailingSeparator();
		IPath folderPath= filePath.removeLastSegments(1);
		if (folderPath == null) {
			if (progressMonitor != null)
				progressMonitor.setCanceled(true);			
			return;
		}
		
		IWorkspaceRoot root= ResourcesPlugin.getWorkspace().getRoot();
		
		/*
		 * 1GF7WG9: ITPJUI:ALL - EXCEPTION: "Save As..." always fails
		 */
		
		IFile file= root.getFile(filePath);
		final FileEditorInput newInput= new FileEditorInput(file);
		
		WorkspaceModifyOperation op= new WorkspaceModifyOperation() {
			public void execute(final IProgressMonitor monitor) throws CoreException {
				
				/* 
				 * 1GF5YOX: ITPJUI:ALL - Save of delete file claims it's still there
				 * Changed false to true.
				 */
				getDocumentProvider().saveDocument(monitor, newInput, getDocumentProvider().getDocument(getEditorInput()), true);
			}
		};
		
		boolean success= false;
		try {
			
			getDocumentProvider().aboutToChange(newInput);
			
			new ProgressMonitorDialog(shell).run(false, true, op);
			setInput(newInput);
			success= true;
			
		} catch (InterruptedException x) {
		} catch (InvocationTargetException x) {
			
			/* 
			 * 1GF5YOX: ITPJUI:ALL - Save of delete file claims it's still there
			 * Missing resources.
			 */						
			Throwable t= x.getTargetException();
			if (t instanceof CoreException) {
				CoreException cx= (CoreException) t;
				ErrorDialog.openError(shell, CEditorMessages.getString("CEditor.error.saving.title2"), CEditorMessages.getString("CEditor.error.saving.message2"), cx.getStatus()); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				MessageDialog.openError(shell, CEditorMessages.getString("CEditor.error.saving.title3"), CEditorMessages.getString("CEditor.error.saving.message3") + t.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		} finally {
				
			if (progressMonitor != null)
				progressMonitor.setCanceled(!success);
		}
	}
		
	/**
	 * Gets the outline page of the c-editor
	 */
	public CContentOutlinePage getOutlinePage() {
		if (fOutlinePage == null) {
			fOutlinePage= new CContentOutlinePage(this);
			fOutlinePage.addSelectionChangedListener(this);
		}
		return fOutlinePage;
	}
		
	/**
	 * @see AbstractTextEditor#getAdapter(Class)
	 */
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			return getOutlinePage();
		}
		return super.getAdapter(required);
	}
	/**
	 * Handles a property change event describing a change
	 * of the editor's preference store and updates the preference
	 * related editor properties.
	 * 
	 * @param event the property change event
	 */
	protected void handlePreferenceStoreChanged(PropertyChangeEvent event) {
		AdaptedSourceViewer asv= (AdaptedSourceViewer) getSourceViewer();
		
		try {
			if(asv != null) {
				
				String property= event.getProperty();
				if (PREFERENCE_COLOR_FOREGROUND.equals(property) || PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT.equals(property) ||
					PREFERENCE_COLOR_BACKGROUND.equals(property) ||	PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property))
				{
					initializeViewerColors(getSourceViewer());
					getSourceViewer().getTextWidget().redraw();
					return;
				}
				
				if (CURRENT_LINE.equals(property)) {
					if (isLineHighlightingEnabled())
						startLineHighlighting();
					else
						stopLineHighlighting();
					return;
				}
						
				if (CURRENT_LINE_COLOR.equals(property)) {
					if (fLinePainter != null)
						fLinePainter.setHighlightColor(getColor(CURRENT_LINE_COLOR));
					return;
				}
				
				if (PROBLEM_INDICATION.equals(property)) {
					if (isProblemIndicationEnabled())
						startProblemIndication();
					else
						stopProblemIndication();
					return;
				}
					
				if (PROBLEM_INDICATION_COLOR.equals(property)) {
					if (fProblemPainter != null)
						fProblemPainter.setHighlightColor(getColor(PROBLEM_INDICATION_COLOR));
					return;
				}
				
				if (PRINT_MARGIN.equals(property)) {
					if (isShowingPrintMarginEnabled())
						startShowingPrintMargin();
					else
						stopShowingPrintMargin();
					return;
				}
						
				if (PRINT_MARGIN_COLOR.equals(property)) {
					if (fPrintMarginPainter != null)
						fPrintMarginPainter.setMarginRulerColor(getColor(PRINT_MARGIN_COLOR));
					return;
				}
						
				if (PRINT_MARGIN_COLUMN.equals(property)) {
					if (fPrintMarginPainter != null)
						fPrintMarginPainter.setMarginRulerColumn(getPreferenceStore().getInt(PRINT_MARGIN_COLUMN));
					return;
				}
				
				if (CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH.equals(property)) {
				    SourceViewerConfiguration configuration= getSourceViewerConfiguration();
					String[] types= configuration.getConfiguredContentTypes(asv);					
					for (int i= 0; i < types.length; i++)
					    asv.setIndentPrefixes(configuration.getIndentPrefixes(asv, types[i]), types[i]);
							    
					if (fTabConverter != null)
						fTabConverter.setNumberOfSpacesPerTab(getPreferenceStore().getInt(CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH));
						
					Object value= event.getNewValue();
				
					if (value instanceof Integer) {
						asv.getTextWidget().setTabs(((Integer) value).intValue());
						
					} else if (value instanceof String) {
						asv.getTextWidget().setTabs(Integer.parseInt((String) value));
					}
				}
						
				if (SPACES_FOR_TABS.equals(property)) {
					if (isTabConversionEnabled())
						startTabConversion();
					else
						stopTabConversion();
					return;
				}
					
				if (MATCHING_BRACKETS.equals(property)) {
					if (isBracketHighlightingEnabled())
						startBracketHighlighting();
					else
						stopBracketHighlighting();
					return;
				}
						
				if (MATCHING_BRACKETS_COLOR.equals(property)) {
					if (fBracketPainter != null)
						fBracketPainter.setHighlightColor(getColor(MATCHING_BRACKETS_COLOR));
					return;
				}
				if (MATCHING_BRACKETS_NOBOX.equals(property)) {
					if (isBracketHighlightingEnabled())
						setBracketHighlightingStyle();
					return;
				}
				if (LINE_NUMBER_RULER.equals(property)) {
					if (isLineNumberRulerVisible())
						showLineNumberRuler();
					else
						hideLineNumberRuler();
					return;
				}
			
				if (fLineNumberRulerColumn != null &&
						(LINE_NUMBER_COLOR.equals(property) || 
						PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT.equals(property)  ||
						PREFERENCE_COLOR_BACKGROUND.equals(property))) {
					
					initializeLineNumberRulerColumn(fLineNumberRulerColumn);
				}
				
				if (OVERVIEW_RULER.equals(property))  {
					if (isOverviewRulerVisible())
						showOverviewRuler();
					else
						hideOverviewRuler();
					return;
				}
			}
		} finally {

			super.handlePreferenceStoreChanged(event);
			
			if (asv != null && affectsTextPresentation(event))
				asv.invalidateTextPresentation();
		}
	}
	
	/**
	 * Initializes the given viewer's colors.
	 * 
	 * @param viewer the viewer to be initialized
	 */
	private void initializeViewerColors(ISourceViewer viewer) {
		
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {
			
			StyledText styledText= viewer.getTextWidget();
			
			// ----------- foreground color --------------------
			Color color= store.getBoolean(PREFERENCE_COLOR_FOREGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, PREFERENCE_COLOR_FOREGROUND, styledText.getDisplay());
			styledText.setForeground(color);
				
			if (fForegroundColor != null)
				fForegroundColor.dispose();
			
			fForegroundColor= color;
			
			// ---------- background color ----------------------
			color= store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)
				? null
				: createColor(store, PREFERENCE_COLOR_BACKGROUND, styledText.getDisplay());
			styledText.setBackground(color);
				
			if (fBackgroundColor != null)
				fBackgroundColor.dispose();
				
			fBackgroundColor= color;
		}
	}
	
	/**
	 * Creates a color from the information stored in the given preference store.
	 * Returns <code>null</code> if there is no such information available.
	 */
	private Color createColor(IPreferenceStore store, String key, Display display) {
	
		RGB rgb= null;		
		
		if (store.contains(key)) {
			
			if (store.isDefault(key))
				rgb= PreferenceConverter.getDefaultColor(store, key);
			else
				rgb= PreferenceConverter.getColor(store, key);
		
			if (rgb != null)
				return new Color(display, rgb);
		}
		
		return null;
	}
	
	/**
	 * @see ISelectionChangedListener#selectionChanged
	 */
	public void selectionChanged(SelectionChangedEvent event) {
		ISelection sel= event.getSelection();
		if (sel instanceof IStructuredSelection) {
			IStructuredSelection selection= (IStructuredSelection)sel;
			Object obj= selection.getFirstElement();
			if (obj instanceof ISourceReference) {
				try {
					ISourceRange range = ((ISourceReference)obj).getSourceRange();
					if (range != null) {
						setSelection(range, !isActivePart());
					}
				} catch (CModelException e) {
				}
			}
		}
	}
	
	/**
	 * Sets the current editor selection to the source range. Optionally
	 * sets the current editor position.
	 *
	 * @param element the source range to be shown in the editor, can be null.
	 * @param moveCursor if true the editor is scrolled to show the range.
	 */
	public void setSelection(ISourceRange element, boolean moveCursor) {
		if (element != null) {
			try {
				IRegion alternateRegion = null;
				int start= element.getStartPos();
				int length= element.getLength();

				// Sanity check sometimes the parser may throw wrong numbers.
				if (start < 0 || length < 0) {
					start = 0;                              
					length = 0;
				}

				// 0 length and start and non-zero start line says we know
				// the line for some reason, but not the offset.
				if (length == 0 && start == 0 && element.getStartLine() != 0) {
					alternateRegion = 
						getDocumentProvider().getDocument(getEditorInput()).getLineInformation(element.getStartLine());
					if (alternateRegion != null) {
						start = alternateRegion.getOffset();
						length = alternateRegion.getLength();
					}
				}
				setHighlightRange(start, length, moveCursor);
				
				if (moveCursor) {
					start= element.getIdStartPos();
					length= element.getIdLength();
					if (start == 0 && length == 0 && alternateRegion != null) {
						start = alternateRegion.getOffset();
						length = alternateRegion.getLength();
					}
					if (start > -1 && getSourceViewer() != null) {
						getSourceViewer().revealRange(start, length);
						getSourceViewer().setSelectedRange(start, length);
					}
					updateStatusField(CTextEditorActionConstants.STATUS_CURSOR_POS);
				}
				return;
				
			} catch (IllegalArgumentException x) {
			} catch (BadLocationException e ) {
			}
		}
		
		if (moveCursor)
			resetHighlightRange();
	}	
	
	private boolean isActivePart() {
		IWorkbenchWindow window= getSite().getWorkbenchWindow();
		IPartService service= window.getPartService();
		return (this == service.getActivePart());
	}
	
	public void dispose() {
		
		stopBracketHighlighting();
		stopLineHighlighting();
		
		if (fPaintManager != null) {
			fPaintManager.dispose();
			fPaintManager= null;
		}
		if (fCEditorErrorTickUpdater != null) {
			fCEditorErrorTickUpdater.setAnnotationModel(null);
			fCEditorErrorTickUpdater= null;
		}
		
		super.dispose();
	}
	
	protected void createActions() 
	{
		super.createActions();
			
		// Default text editing menu items

		IAction action= new TextOperationAction(CEditorMessages.getResourceBundle(), "Comment.", this, ITextOperationTarget.PREFIX); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.COMMENT);		
		setAction("Comment", action); //$NON-NLS-1$
		markAsStateDependentAction("Comment", true); //$NON-NLS-1$

		action= new TextOperationAction(CEditorMessages.getResourceBundle(), "Uncomment.", this, ITextOperationTarget.STRIP_PREFIX); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.UNCOMMENT);		
		setAction("Uncomment", action); //$NON-NLS-1$
		markAsStateDependentAction("Uncomment", true); //$NON-NLS-1$
	
		action= new TextOperationAction(CEditorMessages.getResourceBundle(), "Format.", this, ISourceViewer.FORMAT); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.FORMAT);		
		setAction("Format", action); //$NON-NLS-1$
		markAsStateDependentAction("Format", true); //$NON-NLS-1$

		action = new ContentAssistAction(CEditorMessages.getResourceBundle(), "ContentAssistProposal.", this); //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.CONTENT_ASSIST_PROPOSALS);
		setAction("ContentAssistProposal", action); 
		markAsStateDependentAction("ContentAssistProposal", true); //$NON-NLS-1$
                 
		action = new TextOperationAction(CEditorMessages.getResourceBundle(), "ContentAssistTip.", this, ISourceViewer.CONTENTASSIST_CONTEXT_INFORMATION);  //$NON-NLS-1$
		action.setActionDefinitionId(ICEditorActionDefinitionIds.CONTENT_ASSIST_CONTEXT_INFORMATION);
		setAction("ContentAssistTip", action); 

		
		setAction("AddIncludeOnSelection", new AddIncludeOnSelectionAction(this));		 //$NON-NLS-1$
		setAction("OpenOnSelection", new OpenOnSelectionAction(this));
		
		fSearchForReferencesAction= new SearchForReferencesAction(getSelectionProvider());
	}

	public void editorContextMenuAboutToShow( IMenuManager menu ) 
	{	
		super.editorContextMenuAboutToShow( menu );
		
		addGroup(menu, ITextEditorActionConstants.GROUP_EDIT, IContextMenuConstants.GROUP_REORGANIZE);
		addGroup(menu, ITextEditorActionConstants.GROUP_EDIT, IContextMenuConstants.GROUP_GENERATE);
		addGroup(menu, ITextEditorActionConstants.GROUP_EDIT, IContextMenuConstants.GROUP_NEW);

		// Code formatting menu items -- only show in C perspective
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Comment"); //$NON-NLS-1$
		addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Uncomment"); //$NON-NLS-1$
		// @@@ disabled for now until we get it to do something...
		//addAction(menu, ITextEditorActionConstants.GROUP_EDIT, "Format"); //$NON-NLS-1$

		MenuManager search= new MenuManager("Search", IContextMenuConstants.GROUP_SEARCH); //$NON-NLS-1$
		menu.appendToGroup(ITextEditorActionConstants.GROUP_FIND, search);
		if(SearchForReferencesAction.canActionBeAdded(getSelectionProvider().getSelection())) {
			search.add(fSearchForReferencesAction);
		}
		
		addAction(menu, IContextMenuConstants.GROUP_GENERATE, "ContentAssistProposal"); //$NON-NLS-1$
		addAction(menu, IContextMenuConstants.GROUP_GENERATE, "AddIncludeOnSelection"); //$NON-NLS-1$
		addAction(menu, IContextMenuConstants.GROUP_GENERATE, "OpenOnSelection"); //$NON-NLS-1$
	}

	/**
	 * Internal interface for a cursor listener. I.e. aggregation 
	 * of mouse and key listener.
	 */
	interface ICursorListener extends MouseListener, KeyListener {
	};
		
	/**
	 * The <code>AbstractTextEditor</code> implementation of this 
	 * <code>IWorkbenchPart</code> method creates the vertical ruler and
	 * source viewer. Subclasses may extend.
	 * 
	 * We attach our own mouseDown listener on the menu bar, 
	 * and our own listener for cursor/key/selection events to update cursor position in
	 * status bar.
	 */
	public void createPartControl(Composite parent) {
		super.createPartControl(parent);
		fPaintManager= new PaintManager(getSourceViewer());
		ISelectionChangedListener sListener = new ISelectionChangedListener() {
				private Runnable fRunnable= new Runnable() {
					public void run() {
						updateStatusField(CTextEditorActionConstants.STATUS_CURSOR_POS);
					}
				};
				
				private Display fDisplay;
				
				public void selectionChanged(SelectionChangedEvent event) {
					if (fDisplay == null)
						fDisplay= getSite().getShell().getDisplay();
					fDisplay.asyncExec(fRunnable);	
				}
		};
		
		getSelectionProvider().addSelectionChangedListener(sListener);
		
		
		initializeViewerColors(getSourceViewer());
		
		if (isLineHighlightingEnabled())
			startLineHighlighting();
		if (isProblemIndicationEnabled())
			startProblemIndication();
		if (isShowingPrintMarginEnabled())
			startShowingPrintMargin();
		if (isTabConversionEnabled())
			startTabConversion();
		if (isBracketHighlightingEnabled())
			startBracketHighlighting();
		if (isOverviewRulerVisible())
			showOverviewRuler();


	}
	
	private Color getColor(String key) {
		RGB rgb= PreferenceConverter.getColor(getPreferenceStore(), key);
		return getColor(rgb);
	}
	
	private Color getColor(RGB rgb) {
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		return textTools.getColorManager().getColor(rgb);
	}
	
	private IMarker getNextError(int offset, boolean forward) {
		
		IMarker nextError= null;
		
		IDocument document= getDocumentProvider().getDocument(getEditorInput());
		int endOfDocument= document.getLength(); 
		int distance= 0;
		
		IAnnotationModel model= getDocumentProvider().getAnnotationModel(getEditorInput());
		Iterator e= model.getAnnotationIterator();
		while (e.hasNext()) {
			Annotation a= (Annotation) e.next();
			if (a instanceof CMarkerAnnotation) {
				MarkerAnnotation ma= (MarkerAnnotation) a;
				IMarker marker= ma.getMarker();
		
				if (MarkerUtilities.isMarkerType(marker, IMarker.PROBLEM)) {
					Position p= model.getPosition(a);
					if (!p.includes(offset)) {
						
						int currentDistance= 0;
						
						if (forward) {
							currentDistance= p.getOffset() - offset;
							if (currentDistance < 0)
								currentDistance= endOfDocument - offset + p.getOffset();
						} else {
							currentDistance= offset - p.getOffset();
							if (currentDistance < 0)
								currentDistance= offset + endOfDocument - p.getOffset();
						}						
												
						if (nextError == null || (currentDistance < distance && currentDistance != 0)) {
							distance= currentDistance;
							if(distance == 0) distance = endOfDocument;
							nextError= marker;
						}


					}
				}
		
			}
		}
		
		return nextError;
	}
	
	public void gotoError(boolean forward) {
		
		ISelectionProvider provider= getSelectionProvider();
		
		if (fStatusLineClearer != null) {
			provider.removeSelectionChangedListener(fStatusLineClearer);
			fStatusLineClearer= null;
		}
		
		ITextSelection s= (ITextSelection) provider.getSelection();
		IMarker nextError= getNextError(s.getOffset(), forward);
		
		if (nextError != null) {
			
			gotoMarker(nextError);
			
			IWorkbenchPage page= getSite().getPage();
			
			IViewPart view= view= page.findView("org.eclipse.ui.views.TaskList"); //$NON-NLS-1$
			if (view instanceof TaskList) {
				StructuredSelection ss= new StructuredSelection(nextError);
				((TaskList) view).setSelection(ss, true);
			}
			
			getStatusLineManager().setErrorMessage(nextError.getAttribute(IMarker.MESSAGE, "")); //$NON-NLS-1$
			fStatusLineClearer= new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					getSelectionProvider().removeSelectionChangedListener(fStatusLineClearer);
					fStatusLineClearer= null;
					getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$
				}
			};
			provider.addSelectionChangedListener(fStatusLineClearer);
			
		} else {
			
			getStatusLineManager().setErrorMessage(""); //$NON-NLS-1$
			
		}
	}
	/*
	 * Get the dektop's StatusLineManager
	 */
	protected IStatusLineManager getStatusLineManager() {
		IEditorActionBarContributor contributor= getEditorSite().getActionBarContributor();
		if (contributor instanceof EditorActionBarContributor) {
			return ((EditorActionBarContributor) contributor).getActionBars().getStatusLineManager();
		}
		return null;
	}
	
	/**
	 * LIneHighlighting manager
	 */
	private void startLineHighlighting() {
		if (fLinePainter == null) {
			ISourceViewer sourceViewer= getSourceViewer();
			fLinePainter= new LinePainter(sourceViewer);
			fLinePainter.setHighlightColor(getColor(CURRENT_LINE_COLOR));
			fPaintManager.addPainter(fLinePainter);
		}
	}
	
	private void stopLineHighlighting() {
		if (fLinePainter != null) {
			fPaintManager.removePainter(fLinePainter);
			fLinePainter.deactivate(true);
			fLinePainter.dispose();
			fLinePainter= null;
		}
	}
	
	private boolean isLineHighlightingEnabled() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(CURRENT_LINE);
	}
	
	private void startProblemIndication() {
		if (fProblemPainter == null) {
			fProblemPainter= new ProblemPainter(this, getSourceViewer());
			fProblemPainter.setHighlightColor(getColor(PROBLEM_INDICATION_COLOR));
			fPaintManager.addPainter(fProblemPainter);
		}
	}
	
	private void stopProblemIndication() {
		if (fProblemPainter != null) {
			fPaintManager.removePainter(fProblemPainter);
			fProblemPainter.deactivate(true);
			fProblemPainter.dispose();
			fProblemPainter= null;
		}
	}
		
	private boolean isProblemIndicationEnabled() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(PROBLEM_INDICATION);
	}
	
	private void startShowingPrintMargin() {
		if (fPrintMarginPainter == null) {
			fPrintMarginPainter= new PrintMarginPainter(getSourceViewer());
			fPrintMarginPainter.setMarginRulerColor(getColor(PRINT_MARGIN_COLOR));
			fPrintMarginPainter.setMarginRulerColumn(getPreferenceStore().getInt(PRINT_MARGIN_COLUMN));
			fPaintManager.addPainter(fPrintMarginPainter);
		}
	}
	
	private void stopShowingPrintMargin() {
		if (fPrintMarginPainter != null) {
			fPaintManager.removePainter(fPrintMarginPainter);
			fPrintMarginPainter.deactivate(true);
			fPrintMarginPainter.dispose();
			fPrintMarginPainter= null;
		}
	}
	
	private boolean isShowingPrintMarginEnabled() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(PRINT_MARGIN);
	}
	
	private void startTabConversion() {
		if (fTabConverter == null) {
			fTabConverter= new TabConverter();
			fTabConverter.setNumberOfSpacesPerTab(getPreferenceStore().getInt(CSourceViewerConfiguration.PREFERENCE_TAB_WIDTH));
			AdaptedSourceViewer asv= (AdaptedSourceViewer) getSourceViewer();
			asv.addTextConverter(fTabConverter);
		}
	}
	
	private void stopTabConversion() {
		if (fTabConverter != null) {
			AdaptedSourceViewer asv= (AdaptedSourceViewer) getSourceViewer();
			asv.removeTextConverter(fTabConverter);
			fTabConverter= null;
		}
	}
	
	private boolean isTabConversionEnabled() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(SPACES_FOR_TABS);
	}
	
	private void startBracketHighlighting() {
		if (fBracketPainter == null) {
			ISourceViewer sourceViewer= getSourceViewer();
			fBracketPainter= new BracketPainter(sourceViewer);
			fBracketPainter.setHighlightColor(getColor(MATCHING_BRACKETS_COLOR));
			fPaintManager.addPainter(fBracketPainter);
			IPreferenceStore store= getPreferenceStore();
			fBracketPainter.setHighlightStyle(store.getBoolean(MATCHING_BRACKETS_NOBOX));
		}
	}
	
	private void stopBracketHighlighting() {
		if (fBracketPainter != null) {
			fPaintManager.removePainter(fBracketPainter);
			fBracketPainter.deactivate(true);
			fBracketPainter.dispose();
			fBracketPainter= null;
		}
	}
	
	private boolean isBracketHighlightingEnabled() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(MATCHING_BRACKETS);
	}
	
	private void setBracketHighlightingStyle() {
		IPreferenceStore store= getPreferenceStore();
		if(fBracketPainter != null) {
			fBracketPainter.setHighlightStyle(store.getBoolean(MATCHING_BRACKETS_NOBOX));
		}
	}

	interface ITextConverter {
		void customizeDocumentCommand(IDocument document, DocumentCommand command);
	};
		
	static class TabConverter implements ITextConverter {
		
		private String fTabString= "";
		private int tabRatio = 0;
		
		public void setNumberOfSpacesPerTab(int ratio) {
			tabRatio = ratio;
			StringBuffer buffer= new StringBuffer();
			for (int i= 0; i < ratio; i++)
				buffer.append(' ');
			fTabString= buffer.toString();
		}		
		
		public void customizeDocumentCommand(IDocument document, DocumentCommand command) {
			String text= command.text;
			StringBuffer buffer= new StringBuffer();
			final String TAB = "\t";
			// create tokens including the tabs
			StringTokenizer tokens = new StringTokenizer(text, TAB, true);
			
			int charCount = 0;
			try{
				// get offset of insertion less start of line
				// buffer to determine how many characters
				// are already on this line and adjust tabs accordingly
				charCount = command.offset -  (document.getLineInformationOfOffset(command.offset).getOffset());
			} catch (Exception ex){
				
			}

			String nextToken = null;
			int spaces = 0;
			while (tokens.hasMoreTokens()){
				nextToken = tokens.nextToken();
				if (TAB.equals(nextToken)){
					spaces = tabRatio - (charCount % tabRatio);
					
					for (int i= 0; i < spaces; i++){
						buffer.append(' ');
					}						
					
					charCount += spaces;
				} else {
					buffer.append(nextToken);
					charCount += nextToken.length();	
				}
			}
			command.text= buffer.toString();			
		}
	};
	
	/* Source code language to display */
	public final static String LANGUAGE_CPP=  "CEditor.language.cpp";	
	public final static String LANGUAGE_C=  "CEditor.language.c";	
	
	
	class AdaptedRulerLayout extends Layout {
		
		protected int fGap;
		protected AdaptedSourceViewer fAdaptedSourceViewer;
		
		
		protected AdaptedRulerLayout(int gap, AdaptedSourceViewer asv) {
			fGap= gap;
			fAdaptedSourceViewer= asv;
		}
		
		protected Point computeSize(Composite composite, int wHint, int hHint, boolean flushCache) {
			Control[] children= composite.getChildren();
			Point s= children[children.length - 1].computeSize(SWT.DEFAULT, SWT.DEFAULT, flushCache);
			if (fAdaptedSourceViewer.isVerticalRulerVisible())
				s.x += fAdaptedSourceViewer.getVerticalRuler().getWidth() + fGap;
			return s;
		}
		
		protected void layout(Composite composite, boolean flushCache) {
			Rectangle clArea= composite.getClientArea();
			if (fAdaptedSourceViewer.isVerticalRulerVisible()) {
				
				StyledText textWidget= fAdaptedSourceViewer.getTextWidget();
				Rectangle trim= textWidget.computeTrim(0, 0, 0, 0);
				int scrollbarHeight= trim.height;
				
				IVerticalRuler vr= fAdaptedSourceViewer.getVerticalRuler();
				int vrWidth=vr.getWidth();
				
				int orWidth= 0;
				if (fAdaptedSourceViewer.isOverviewRulerVisible()) {
					OverviewRuler or= fAdaptedSourceViewer.getOverviewRuler();
					orWidth= or.getWidth();
					or.getControl().setBounds(clArea.width - orWidth, scrollbarHeight, orWidth, clArea.height - 3*scrollbarHeight);
				}
				
				textWidget.setBounds(vrWidth + fGap, 0, clArea.width - vrWidth - orWidth - 2*fGap, clArea.height);
				vr.getControl().setBounds(0, 0, vrWidth, clArea.height - scrollbarHeight);
				
			} else {
				StyledText textWidget= fAdaptedSourceViewer.getTextWidget();
				textWidget.setBounds(0, 0, clArea.width, clArea.height);
			}
		}
	};
	
	/**
	 * Adapted source viewer for CEditor
	 */
	
	public class AdaptedSourceViewer extends SourceViewer implements ITextViewerExtension {
		
		private List fTextConverters;
		private String fDisplayLanguage;
		private OverviewRuler fOverviewRuler;
		private boolean fIsOverviewRulerVisible;
		
		private IVerticalRuler fCachedVerticalRuler;
		private boolean fCachedIsVerticalRulerVisible;
		
		
		public AdaptedSourceViewer(Composite parent, IVerticalRuler ruler, int styles, String language) {
			super(parent, ruler, styles);
			
			fDisplayLanguage = language;
			fCachedVerticalRuler= ruler;
			fCachedIsVerticalRulerVisible= (ruler != null);
			fOverviewRuler= new OverviewRuler(VERTICAL_RULER_WIDTH);
			
			delayedCreateControl(parent, styles);
		}

		/*
		 * @see ISourceViewer#showAnnotations(boolean)
		 */
		public void showAnnotations(boolean show) {
			fCachedIsVerticalRulerVisible= (show && fCachedVerticalRuler != null);
			super.showAnnotations(show);
		}
		/*
		public IContentAssistant getContentAssistant() {
			return fContentAssistant;
		} */
		
		/*
		 * @see ITextOperationTarget#doOperation(int)
		 */
		public void doOperation(int operation) {
		
			if (getTextWidget() == null) {
				return;
			}
			
			super.doOperation(operation);
		}
		
		/*
		 * @see ITextOperationTarget#canDoOperation(int)
		 *
		public boolean canDoOperation(int operation) {
			
			if (getTextWidget() == null)
				return false;
		
			switch (operation) {
				case SHIFT_RIGHT:
				case SHIFT_LEFT:
					return isEditable() && fIndentChars != null && isBlockSelected();
			}
			
			return super.canDoOperation(operation);
		}*/
		
		public void insertTextConverter(ITextConverter textConverter, int index) {
			throw new UnsupportedOperationException();
		}
		
		public void addTextConverter(ITextConverter textConverter) {
			if (fTextConverters == null) {
				fTextConverters= new ArrayList(1);
				fTextConverters.add(textConverter);
			} else if (!fTextConverters.contains(textConverter))
				fTextConverters.add(textConverter);
		}
		
		public void removeTextConverter(ITextConverter textConverter) {
			if (fTextConverters != null) {
				fTextConverters.remove(textConverter);
				if (fTextConverters.size() == 0)
					fTextConverters= null;
			}
		}
		
		/*
		 * @see TextViewer#customizeDocumentCommand(DocumentCommand)
		 */
		protected void customizeDocumentCommand(DocumentCommand command) {
			super.customizeDocumentCommand(command);
			if (fTextConverters != null) {
				for (Iterator e = fTextConverters.iterator(); e.hasNext();)
					((ITextConverter) e.next()).customizeDocumentCommand(getDocument(), command);
			}
		}
		
		public IVerticalRuler getVerticalRuler() {
			return fCachedVerticalRuler;
		}
		
		public boolean isVerticalRulerVisible() {
			return fCachedIsVerticalRulerVisible;
		}
		
		public OverviewRuler getOverviewRuler() {
			return fOverviewRuler;
		}
		
		/*
		 * @see TextViewer#createControl(Composite, int)
		 */
		protected void createControl(Composite parent, int styles) {
			// do nothing here
		}
		
		protected void delayedCreateControl(Composite parent, int styles) {
			//create the viewer
			super.createControl(parent, styles);
			
			Control control= getControl();
			if (control instanceof Composite) {
				Composite composite= (Composite) control;
				composite.setLayout(new AdaptedRulerLayout(GAP_SIZE, this));
				fOverviewRuler.createControl(composite, this);
			}
		}
		
		public void hideOverviewRuler() {
			fIsOverviewRulerVisible= false;
			Control control= getControl();
			if (control instanceof Composite) {
				Composite composite= (Composite) control;
				composite.layout();
			}
		}
		
		public void showOverviewRuler() {
			fIsOverviewRulerVisible= true;
			Control control= getControl();
			if (control instanceof Composite) {
				Composite composite= (Composite) control;
				composite.layout();
			}
		}
		
		public boolean isOverviewRulerVisible() {
			return fIsOverviewRulerVisible;
		}
		
		/*
		 * @see ISourceViewer#setDocument(IDocument, IAnnotationModel, int, int)
		 */
		public void setDocument(IDocument document, IAnnotationModel annotationModel, int visibleRegionOffset, int visibleRegionLength) {
			super.setDocument(document, annotationModel, visibleRegionOffset, visibleRegionLength);
			fOverviewRuler.setModel(annotationModel);
		}
		
 		/**
		 * Invalidates the current presentation by sending an initialization
		 * event to all text listener.
		 */
/*
		public final void invalidateTextPresentation() {
			IDocument doc = getDocument();
			if(doc != null) {
				fireInputDocumentChanged(doc, doc);
			}
		}
*/		
		public void setDisplayLanguage(String language) {
			fDisplayLanguage = language;
		}
		
		public String getDisplayLanguage() {
			return fDisplayLanguage;
		}
		/**
		 * Internal verify listener.
		 */
		class TextVerifyListener implements VerifyListener {
			
			private boolean fForward= true;
			
			/**
			 * Tells the listener to forward received events.
			 */
			public void forward(boolean forward) {
				fForward= forward;
			}
			
			/*
			 * @see VerifyListener#verifyText(VerifyEvent)
			 */
			public void verifyText(VerifyEvent e) {
				if (fForward)
					handleVerifyEvent(e);
			}	
		};
		
		/**
		 * The viewer's manager of registered verify key listeners.
		 * Uses batches rather than robust iterators because of
		 * performance issues.
		 */
		class VerifyKeyListenersManager implements VerifyKeyListener {
			
			class Batch {
				int index;
				VerifyKeyListener listener;
				
				public Batch(VerifyKeyListener l, int i) {
					listener= l;
					index= i;
				}
			};
			
			private List fListeners= new ArrayList();
			private List fBatched= new ArrayList();
			private Iterator fIterator;
			
			/*
			 * @see VerifyKeyListener#verifyKey(VerifyEvent)
			 */
			public void verifyKey(VerifyEvent event) {
				if (fListeners.isEmpty())
					return;
					
				fIterator= fListeners.iterator();
				while (fIterator.hasNext() && event.doit) {
					VerifyKeyListener listener= (VerifyKeyListener) fIterator.next();
					listener.verifyKey(event);
				}
				fIterator= null;
				
				processBatchedRequests();
			}
			
			private void processBatchedRequests() {
				if (!fBatched.isEmpty()) {
					Iterator e= fBatched.iterator();
					while (e.hasNext()) {
						Batch batch= (Batch) e.next();
						insertListener(batch.listener, batch.index);
					}
					fBatched.clear();
				}
			}
			
			/**
			 * Returns the number of registered verify key listeners.
			 */
			public int numberOfListeners() {
				return fListeners.size();
			}
			
			/**
			 * Inserts the given listener at the given index or moves it
			 * to that index.
			 * 
			 * @param listener the listener to be inserted
			 * @param index the index of the listener or -1 for remove
			 */
			public void insertListener(VerifyKeyListener listener, int index) {
				
				if (index == -1) {
					removeListener(listener);
				} else if (listener != null) {
					
					if (fIterator != null) {
						
						fBatched.add(new Batch(listener, index));
					
					} else {
						
						int idx= fListeners.indexOf(listener);
						if (idx != index) {
							
							if (idx != -1)
								fListeners.remove(idx);
								
							if (index > fListeners.size())
								fListeners.add(listener);
							else
								fListeners.add(index, listener);
						}
						
						if (fListeners.size() == 1)
							install();
					}
				}
			}
			
			/**
			 * Removes the given listener.
			 * 
			 * @param listener the listener to be removed
			 */
			public void removeListener(VerifyKeyListener listener) {
				if (listener == null)
					return;
				
				if (fIterator != null) {
					
					fBatched.add(new Batch(listener, -1));
				
				} else {
					
					fListeners.remove(listener);
					if (fListeners.isEmpty())
						uninstall();
				
				}
			}
			
			/**
			 * Installs this manager.
			 */
			private void install() {
				getTextWidget().addVerifyKeyListener(this);
			}
			
			/**
			 * Uninstalls this manager.
			 */
			private void uninstall() {
				StyledText textWidget = getTextWidget();
				if (textWidget != null && !textWidget.isDisposed()) {
					textWidget.removeVerifyKeyListener(this);
				}
			}
		};
		
		/** The viewer's manager of verify key listeners */
		private VerifyKeyListenersManager fVerifyKeyListenersManager= new VerifyKeyListenersManager();
		
		/*
		 * @see ITextViewerExtension#appendVerifyKeyListener(VerifyKeyListener)
		 */
		public void appendVerifyKeyListener(VerifyKeyListener listener) {
			int index= fVerifyKeyListenersManager.numberOfListeners();
			fVerifyKeyListenersManager.insertListener(listener, index);
		}
		
		/*
		 * @see ITextViewerExtension#prependVerifyKeyListener(VerifyKeyListener)
		 */
		public void prependVerifyKeyListener(VerifyKeyListener listener) {
			fVerifyKeyListenersManager.insertListener(listener, 0);
			
		}
		
		/*
		 * @see ITextViewerExtension#removeVerifyKeyListener(VerifyKeyListener)
		 */
		public void removeVerifyKeyListener(VerifyKeyListener listener) {
			fVerifyKeyListenersManager.removeListener(listener);
		}

	};

	
	/*
	 * @see AbstractTextEditor#createSourceViewer(Composite, IVerticalRuler, int)
	 */
	protected ISourceViewer createSourceViewer(Composite parent, IVerticalRuler ruler, int styles) {
		// Figure out if this is a C or C++ source file
		String filename = getEditorInput().getName();
		boolean c_file = filename.endsWith(".c");
		
		if (!c_file && filename.endsWith(".h")){
			// ensure that this .h file is part of a C project & not a CPP project
		
			IFile file = getInputFile();
			if (file != null) {
				IProject project = file.getProject();
				c_file = !CoreModel.getDefault().hasCCNature(project);
			}
		}

		return new AdaptedSourceViewer(parent, ruler, styles, c_file ? LANGUAGE_C : LANGUAGE_CPP);
	}
	
	/*
	 * @see AbstractTextEditor#affectsTextPresentation(PropertyChangeEvent)
	 * Pulled in from 2.0
	 */
	protected boolean affectsTextPresentation(PropertyChangeEvent event) {
		String p= event.getProperty();
		
		boolean affects=MATCHING_BRACKETS_COLOR.equals(p) || 
									CURRENT_LINE_COLOR.equals(p) ||
									PROBLEM_INDICATION_COLOR.equals(p);
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		affects |= textTools.affectsBehavior(event);
									
		return affects ? affects : super.affectsTextPresentation(event);
	}

	/**
	 * Creates a new line number ruler column that is appropriately initialized.
	 */
	protected IVerticalRulerColumn createLineNumberRulerColumn() {
		fLineNumberRulerColumn= new LineNumberRulerColumn();
		initializeLineNumberRulerColumn(fLineNumberRulerColumn);
		return fLineNumberRulerColumn;
	}
	
	/*
	 * @see AbstractTextEditor#createVerticalRuler()
	 */
	protected IVerticalRuler createVerticalRuler() {
		CompositeRuler ruler= new CompositeRuler();
		ruler.addDecorator(0, new AnnotationRulerColumn(VERTICAL_RULER_WIDTH));
		if (isLineNumberRulerVisible())
			ruler.addDecorator(1, createLineNumberRulerColumn());
		return ruler;
	}
	
	/**
	 * Initializes the given line number ruler column from the preference store.
	 * @param rulerColumn the ruler column to be initialized
	 */
	protected void initializeLineNumberRulerColumn(LineNumberRulerColumn rulerColumn) {
		CTextTools textTools= CUIPlugin.getDefault().getTextTools();
		IColorManager manager= textTools.getColorManager();	
		
		IPreferenceStore store= getPreferenceStore();
		if (store != null) {	
		
			RGB rgb=  null;
			// foreground color
			if (store.contains(LINE_NUMBER_COLOR)) {
				if (store.isDefault(LINE_NUMBER_COLOR))
					rgb= PreferenceConverter.getDefaultColor(store, LINE_NUMBER_COLOR);
				else
					rgb= PreferenceConverter.getColor(store, LINE_NUMBER_COLOR);
			}
			rulerColumn.setForeground(manager.getColor(rgb));
			
			
			rgb= null;
			// background color
			if (!store.getBoolean(PREFERENCE_COLOR_BACKGROUND_SYSTEM_DEFAULT)) {
				if (store.contains(PREFERENCE_COLOR_BACKGROUND)) {
					if (store.isDefault(PREFERENCE_COLOR_BACKGROUND))
						rgb= PreferenceConverter.getDefaultColor(store, PREFERENCE_COLOR_BACKGROUND);
					else
						rgb= PreferenceConverter.getColor(store, PREFERENCE_COLOR_BACKGROUND);
				}
			}
			rulerColumn.setBackground(manager.getColor(rgb));
		}
	}
	
	/**
	 * Shows the line number ruler column.
	 */
	private void showLineNumberRuler() {
		IVerticalRuler v= getVerticalRuler();
		if (v instanceof CompositeRuler) {
			CompositeRuler c= (CompositeRuler) v;
			c.addDecorator(1, createLineNumberRulerColumn());
		}
	}
	
	/**
	 * Hides the line number ruler column.
	 */
	private void hideLineNumberRuler() {
		IVerticalRuler v= getVerticalRuler();
		if (v instanceof CompositeRuler) {
			CompositeRuler c= (CompositeRuler) v;
			c.removeDecorator(1);
		}
	}
	
	/**
	 * Return whether the line number ruler column should be 
	 * visible according to the preference store settings.
	 * @return <code>true</code> if the line numbers should be visible
	 */
	private boolean isLineNumberRulerVisible() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(LINE_NUMBER_RULER);
	}
	
		private void showOverviewRuler() {
		AdaptedSourceViewer asv= (AdaptedSourceViewer) getSourceViewer();
		asv.showOverviewRuler();
	}
	
	private void hideOverviewRuler() {
		AdaptedSourceViewer asv= (AdaptedSourceViewer) getSourceViewer();
		asv.hideOverviewRuler();
	}
	
	protected boolean isOverviewRulerVisible() {
		IPreferenceStore store= getPreferenceStore();
		return store.getBoolean(OVERVIEW_RULER);
	}
	
	/** Outliner context menu Id */
	protected String fOutlinerContextMenuId;
	
	/**
	 * Sets the outliner's context menu ID.
	 */
	protected void setOutlinerContextMenuId(String menuId) {
		fOutlinerContextMenuId= menuId;
	}
}
