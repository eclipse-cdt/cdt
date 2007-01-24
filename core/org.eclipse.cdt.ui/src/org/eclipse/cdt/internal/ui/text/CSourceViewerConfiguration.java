/*******************************************************************************
 * Copyright (c) 2005, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin, Google
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.text;

import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewerExtension2;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.AbstractDecoratedTextEditorPreferenceConstants;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ILanguageUI;
import org.eclipse.cdt.ui.text.ICPartitions;

import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;

import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.internal.ui.editor.CElementHyperlinkDetector;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverDescriptor;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverProxy;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistProcessor;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.internal.ui.util.ExternalEditorInput;


/**
 * Configuration for an <code>SourceViewer</code> which shows C/C++ code.
 */
public class CSourceViewerConfiguration extends TextSourceViewerConfiguration {
	
    private CTextTools fTextTools;
	private ITextEditor fTextEditor;
	/**
	 * The document partitioning.
	 */
	private String fDocumentPartitioning;
	/**
	 * The C++ source code scanner.
	 */
	private AbstractCScanner fCppCodeScanner;
	/**
	 * The C source code scanner.
	 */
	private AbstractCScanner fCCodeScanner;
	/**
	 * The C multi-line comment scanner.
	 */
	private AbstractCScanner fMultilineCommentScanner;
	/**
	 * The C single-line comment scanner.
	 */
	private AbstractCScanner fSinglelineCommentScanner;
	/**
	 * The C string scanner.
	 */
	private AbstractCScanner fStringScanner;
	/**
	 * The color manager.
	 */
	private IColorManager fColorManager;
	/**
	 * The C preprocessor scanner.
	 */
	private AbstractCScanner fCPreprocessorScanner;
	/**
	 * The C++ preprocessor scanner.
	 */
	private AbstractCScanner fCppPreprocessorScanner;
	
	/**
	 * Creates a new C source viewer configuration for viewers in the given editor
	 * using the given preference store, the color manager and the specified document partitioning.
	 * <p>
	 * Creates a C source viewer configuration in the new setup without text tools. Clients are
	 * allowed to call {@link CSourceViewerConfiguration#handlePropertyChangeEvent(PropertyChangeEvent)}
	 * and disallowed to call {@link CSourceViewerConfiguration#getPreferenceStore()} on the resulting
	 * C source viewer configuration.
	 * </p>
	 *
	 * @param colorManager the color manager
	 * @param preferenceStore the preference store, can be read-only
	 * @param editor the editor in which the configured viewer(s) will reside, or <code>null</code> if none
	 * @param partitioning the document partitioning for this configuration, or <code>null</code> for the default partitioning
	 */
	public CSourceViewerConfiguration(IColorManager colorManager, IPreferenceStore preferenceStore, ITextEditor editor, String partitioning) {
		super(preferenceStore);
		fColorManager= colorManager;
		fTextEditor= editor;
		fDocumentPartitioning= partitioning;
		initializeScanners();
	}

	/**
	 * Creates a new C source viewer configuration for viewers in the given editor using
	 * the given C tools collection.
	 *
	 * @param tools the C text tools collection to be used
	 * @param editor the editor in which the configured viewer will reside
	 */
	public CSourceViewerConfiguration(CTextTools tools, ITextEditor editor) {
		super(CUIPlugin.getDefault().getCombinedPreferenceStore());
		fTextTools= tools;
		fColorManager= tools.getColorManager();
		fTextEditor= editor;
		fDocumentPartitioning= fTextTools.getDocumentPartitioning();
		fCppCodeScanner= (AbstractCScanner) fTextTools.getCppCodeScanner();
		fCCodeScanner= (AbstractCScanner) fTextTools.getCCodeScanner();
		fMultilineCommentScanner= (AbstractCScanner) fTextTools.getMultilineCommentScanner();
		fSinglelineCommentScanner= (AbstractCScanner) fTextTools.getSinglelineCommentScanner();
		fStringScanner= (AbstractCScanner) fTextTools.getStringScanner();
		fCPreprocessorScanner= (AbstractCScanner) fTextTools.getCPreprocessorScanner();
		fCppPreprocessorScanner= (AbstractCScanner) fTextTools.getCppPreprocessorScanner();
	}

	/**
	 * Returns the C multi-line comment scanner for this configuration.
	 *
	 * @return the C multi-line comment scanner
	 */
	protected RuleBasedScanner getMultilineCommentScanner() {
		return fMultilineCommentScanner;
	}

	/**
	 * Returns the C single-line comment scanner for this configuration.
	 *
	 * @return the C single-line comment scanner
	 */
	protected RuleBasedScanner getSinglelineCommentScanner() {
		return fSinglelineCommentScanner;
	}

	/**
	 * Returns the C string scanner for this configuration.
	 *
	 * @return the C string scanner
	 */
	protected RuleBasedScanner getStringScanner() {
		return fStringScanner;
	}

	/**
	 * Returns the C preprocessor scanner for this configuration.
	 *
	 * @return the C preprocessor scanner
	 */
	protected RuleBasedScanner getCPreprocessorScanner() {
		return fCPreprocessorScanner;
	}	
	
	/**
	 * Returns the C++ preprocessor scanner for this configuration.
	 *
	 * @return the C++ preprocessor scanner
	 */
	protected RuleBasedScanner getCppPreprocessorScanner() {
		return fCppPreprocessorScanner;
	}	
	
	/**
	 * Returns the color manager for this configuration.
	 *
	 * @return the color manager
	 */
	protected IColorManager getColorManager() {
		return fColorManager;
	}

	/**
	 * Returns the editor in which the configured viewer(s) will reside.
	 *
	 * @return the enclosing editor
	 */
	public ITextEditor getEditor() {
		return fTextEditor;
	}

    /**
     * Creates outline presenter. 
     * @return Presenter with outline view.
     */
    public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer) {
        final IInformationControlCreator outlineControlCreator = getOutlineControlCreator();
        final InformationPresenter presenter = new InformationPresenter(outlineControlCreator);
        presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
		final IInformationProvider provider = new CElementContentProvider(getEditor());
        presenter.setInformationProvider(provider, IDocument.DEFAULT_CONTENT_TYPE);
        presenter.setInformationProvider(provider, ICPartitions.C_MULTI_LINE_COMMENT);
        presenter.setInformationProvider(provider, ICPartitions.C_SINGLE_LINE_COMMENT);
        presenter.setInformationProvider(provider, ICPartitions.C_STRING);
        presenter.setInformationProvider(provider, ICPartitions.C_CHARACTER);
        presenter.setInformationProvider(provider, ICPartitions.C_PREPROCESSOR);
        presenter.setSizeConstraints(50, 20, true, false);
        return presenter;
    }

	/**
	 * Initializes the scanners.
	 */
	private void initializeScanners() {
		Assert.isTrue(isNewSetup());
		fCppCodeScanner= new CppCodeScanner(getColorManager(), fPreferenceStore);
		fCCodeScanner= new CCodeScanner(getColorManager(), fPreferenceStore);
		fMultilineCommentScanner= new CCommentScanner(getColorManager(), fPreferenceStore, ICColorConstants.C_MULTI_LINE_COMMENT);
		fSinglelineCommentScanner= new CCommentScanner(getColorManager(), fPreferenceStore, ICColorConstants.C_SINGLE_LINE_COMMENT);
		fStringScanner= new SingleTokenCScanner(getColorManager(), fPreferenceStore, ICColorConstants.C_STRING);
		fCppPreprocessorScanner= new CPreprocessorScanner(fColorManager, fPreferenceStore, true);
		fCPreprocessorScanner= new CPreprocessorScanner(fColorManager, fPreferenceStore, false);
	}

    /**
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
    public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		CPresentationReconciler reconciler= new CPresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		RuleBasedScanner scanner = null;
		ILanguage language = getLanguage();
		if (language instanceof GPPLanguage) {
			scanner = fCppCodeScanner;
		} else if (language instanceof GCCLanguage) {
			scanner = fCCodeScanner;
		} else if (language != null) {
			ILanguageUI languageUI = (ILanguageUI)language.getAdapter(ILanguageUI.class);
			if (languageUI != null)
				scanner = languageUI.getCodeScanner();
		}
		if (scanner == null) {
			scanner= fCppCodeScanner;
		}

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);

		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		//TextAttribute attr = new TextAttribute(manager.getColor(ICColorConstants.C_DEFAULT));
		
		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());		
		reconciler.setDamager(dr, ICPartitions.C_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_SINGLE_LINE_COMMENT);
		
		dr= new DefaultDamagerRepairer(getMultilineCommentScanner());		
		reconciler.setDamager(dr, ICPartitions.C_MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_MULTI_LINE_COMMENT);

		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, ICPartitions.C_STRING);
		reconciler.setRepairer(dr, ICPartitions.C_STRING);
		
		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, ICPartitions.C_CHARACTER);
		reconciler.setRepairer(dr, ICPartitions.C_CHARACTER);
		
		if (language instanceof GPPLanguage) {
			dr= new DefaultDamagerRepairer(getCppPreprocessorScanner());
		} else if (language instanceof GCCLanguage) {
			dr= new DefaultDamagerRepairer(getCPreprocessorScanner());
		} else {
			dr= new DefaultDamagerRepairer(getCppPreprocessorScanner());
		}
		if (dr != null) {
			reconciler.setDamager(new PartitionDamager(), ICPartitions.C_PREPROCESSOR);
			reconciler.setRepairer(dr, ICPartitions.C_PREPROCESSOR);
		}
		
		return reconciler;
	}

	/*
	 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (getEditor() == null) {
			return null;
		}

		ContentAssistant assistant = new ContentAssistant();
		assistant.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		assistant.setRestoreCompletionProposalSize(getSettings("completion_proposal_size")); //$NON-NLS-1$
		
		IContentAssistProcessor processor = new CContentAssistProcessor(getEditor(), assistant, IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);

		processor = new CContentAssistProcessor(getEditor(), assistant, ICPartitions.C_MULTI_LINE_COMMENT);
		assistant.setContentAssistProcessor(processor, ICPartitions.C_MULTI_LINE_COMMENT);

		processor = new CContentAssistProcessor(getEditor(), assistant, ICPartitions.C_SINGLE_LINE_COMMENT);
		assistant.setContentAssistProcessor(processor, ICPartitions.C_SINGLE_LINE_COMMENT);

		processor = new CContentAssistProcessor(getEditor(), assistant, ICPartitions.C_STRING);
		assistant.setContentAssistProcessor(processor, ICPartitions.C_STRING);

		processor = new CContentAssistProcessor(getEditor(), assistant, ICPartitions.C_PREPROCESSOR);
		assistant.setContentAssistProcessor(processor, ICPartitions.C_PREPROCESSOR);

		ContentAssistPreference.configure(assistant, fPreferenceStore);
		
		assistant.setProposalPopupOrientation(IContentAssistant.PROPOSAL_OVERLAY);		
		assistant.setContextInformationPopupOrientation(IContentAssistant.CONTEXT_INFO_ABOVE);
		assistant.setInformationControlCreator(getInformationControlCreator(sourceViewer));

		return assistant;
	}

	/**
	 * Returns the settings for the given section.
	 *
	 * @param sectionName the section name
	 * @return the settings
	 * @since 4.0
	 */
	private IDialogSettings getSettings(String sectionName) {
		IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings().getSection(sectionName);
		if (settings == null)
			settings= CUIPlugin.getDefault().getDialogSettings().addNewSection(sectionName);

		return settings;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fTextEditor != null && (fTextEditor.isEditable() || fTextEditor.getEditorInput() instanceof ExternalEditorInput)) {
			//Delay changed and non-incremental reconciler used due to 
			//PR 130089
			MonoReconciler reconciler= new CReconciler(new CReconcilingStrategy(fTextEditor));
			reconciler.setIsIncrementalReconciler(false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);
			return reconciler;
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		String partitioning= getConfiguredDocumentPartitioning(sourceViewer);
		if (ICPartitions.C_MULTI_LINE_COMMENT.equals(contentType))
			return new IAutoEditStrategy[] { new CCommentAutoIndentStrategy() };
		else if (ICPartitions.C_STRING.equals(contentType))
			return new IAutoEditStrategy[] { /*new SmartSemicolonAutoEditStrategy(partitioning),*/ new CStringAutoIndentStrategy(partitioning, getProject()) };
		else
			return new IAutoEditStrategy[] { new CAutoIndentStrategy(partitioning, getProject()) };
	}

	/**
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
	 */
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		return new CDoubleClickSelector();
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDefaultPrefixes(ISourceViewer, String)
	 */
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "//", "" }; //$NON-NLS-1$ //$NON-NLS-2$
	}

	/*
	 * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
	 */
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {

		Vector vector= new Vector();

		// prefix[0] is either '\t' or ' ' x tabWidth, depending on useSpaces

		ICProject project= getProject();
		final int tabWidth= CodeFormatterUtil.getTabWidth(project);
		final int indentWidth= CodeFormatterUtil.getIndentWidth(project);
		int spaceEquivalents= Math.min(tabWidth, indentWidth);
		boolean useSpaces;
		if (project == null)
			useSpaces= CCorePlugin.SPACE.equals(CCorePlugin.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR)) || tabWidth > indentWidth;
		else
			useSpaces= CCorePlugin.SPACE.equals(project.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, true)) || tabWidth > indentWidth;

		for (int i= 0; i <= spaceEquivalents; i++) {
		    StringBuffer prefix= new StringBuffer();

			if (useSpaces) {
			    for (int j= 0; j + i < spaceEquivalents; j++)
			    	prefix.append(' ');

				if (i != 0)
		    		prefix.append('\t');
			} else {
			    for (int j= 0; j < i; j++)
			    	prefix.append(' ');

				if (i != spaceEquivalents)
		    		prefix.append('\t');
			}

			vector.add(prefix.toString());
		}

		vector.add(""); //$NON-NLS-1$

		return (String[]) vector.toArray(new String[vector.size()]);
	}

	private ICProject getProject() {
		ITextEditor editor= getEditor();
		if (editor == null)
			return null;

		ICElement element= null;
		IEditorInput input= editor.getEditorInput();
		IDocumentProvider provider= editor.getDocumentProvider();
		if (provider instanceof CDocumentProvider) {
			CDocumentProvider cudp= (CDocumentProvider) provider;
			element= cudp.getWorkingCopy(input);
		}

		if (element == null)
			return null;

		return element.getCProject();
	}

	/*
	 * @see SourceViewerConfiguration#getTabWidth(ISourceViewer)
	 */
	public int getTabWidth(ISourceViewer sourceViewer) {
		return CodeFormatterUtil.getTabWidth(getProject());
	}

	/**
	 * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
	 */
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new CAnnotationHover();
	}

	/*
	 * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer, String)
	 * @since 2.1
	 */
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
		CEditorTextHoverDescriptor[] hoverDescs= CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
		int stateMasks[]= new int[hoverDescs.length];
		int stateMasksLength= 0;		
		for (int i= 0; i < hoverDescs.length; i++) {
			if (hoverDescs[i].isEnabled()) {
				int j= 0;
				int stateMask= hoverDescs[i].getStateMask();
				while (j < stateMasksLength) {
					if (stateMasks[j] == stateMask)
						break;
					j++;
				}
				if (j == stateMasksLength)
					stateMasks[stateMasksLength++]= stateMask;
			}
		}
		if (stateMasksLength == hoverDescs.length)
			return stateMasks;
		
		int[] shortenedStateMasks= new int[stateMasksLength];
		System.arraycopy(stateMasks, 0, shortenedStateMasks, 0, stateMasksLength);
		return shortenedStateMasks;
	}
	
	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String, int)
	 * @since 2.1
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
		CEditorTextHoverDescriptor[] hoverDescs= CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
		int i= 0;
		while (i < hoverDescs.length) {
			if (hoverDescs[i].isEnabled() &&  hoverDescs[i].getStateMask() == stateMask)
				return new CEditorTextHoverProxy(hoverDescs[i], getEditor());
			i++;
		}

		return null;
	}

	/*
	 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
	 */
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return getTextHover(sourceViewer, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
	}

	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { 	
				IDocument.DEFAULT_CONTENT_TYPE, 
				ICPartitions.C_MULTI_LINE_COMMENT,
				ICPartitions.C_SINGLE_LINE_COMMENT,
				ICPartitions.C_STRING,
				ICPartitions.C_CHARACTER,
				ICPartitions.C_PREPROCESSOR};
	}
	
	/**
	 * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
	 */
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		
		final MultiPassContentFormatter formatter = 
			new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer), 
				IDocument.DEFAULT_CONTENT_TYPE);
		
		formatter.setMasterStrategy(new CFormattingStrategy());
		return formatter;
	}
	
	public boolean affectsBehavior(PropertyChangeEvent event) {
		return  fCppCodeScanner.affectsBehavior(event)
			|| fMultilineCommentScanner.affectsBehavior(event)
			|| fSinglelineCommentScanner.affectsBehavior(event)
			|| fStringScanner.affectsBehavior(event)
			|| fCppPreprocessorScanner.affectsBehavior(event);
	}

	/**
	 * Adapts the behavior of the contained components to the change
	 * encoded in the given event.
	 * 
	 * @param event the event to whch to adapt
	 */
	public void adaptToPreferenceChange(PropertyChangeEvent event) {
		fTextTools.adaptToPreferenceChange(event);
	}

	protected IPreferenceStore getPreferenceStore() {
		Assert.isTrue(!isNewSetup());
		return fPreferenceStore;
	}
	
	/**
	 * @return <code>true</code> iff the new setup without text tools is in use.
	 */
	private boolean isNewSetup() {
		return fTextTools == null;
	}

	/*
	 * @see SourceViewerConfiguration#getHoverControlCreator(ISourceViewer)
	 * @since 2.0
	 */
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return getInformationControlCreator(sourceViewer, true);
	}
	
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer, final boolean cutDown) {
			return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				int style = cutDown ? SWT.NONE : (SWT.V_SCROLL | SWT.H_SCROLL);
				return new DefaultInformationControl(parent, style, new HTMLTextPresenter(cutDown));
				// return new HoverBrowserControl(parent);
			}
		};
	}

	/*
	 * @see SourceViewerConfiguration#getInformationPresenter(ISourceViewer)
	 * @since 2.0
	 */
	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		return super.getInformationPresenter(sourceViewer);
	}
    
	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one of its contained components.
	 *
	 * @param event the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 */
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		return fCppCodeScanner.affectsBehavior(event)
		    || fCCodeScanner.affectsBehavior(event)
			|| fMultilineCommentScanner.affectsBehavior(event)
			|| fSinglelineCommentScanner.affectsBehavior(event)
			|| fStringScanner.affectsBehavior(event)
		    || fCppPreprocessorScanner.affectsBehavior(event);
	}

	/**
	 * Adapts the behavior of the contained components to the change
	 * encoded in the given event.
	 * <p>
	 * Clients are not allowed to call this method if the old setup with
	 * text tools is in use.
	 * </p>
	 *
	 * @param event the event to which to adapt
	 * @see CSourceViewerConfiguration#CSourceViewerConfiguration(IColorManager, IPreferenceStore, ITextEditor, String)
	 */
	public void handlePropertyChangeEvent(PropertyChangeEvent event) {
		Assert.isTrue(isNewSetup());
		if (fCppCodeScanner.affectsBehavior(event))
			fCppCodeScanner.adaptToPreferenceChange(event);
		if (fCCodeScanner.affectsBehavior(event))
			fCCodeScanner.adaptToPreferenceChange(event);
		if (fMultilineCommentScanner.affectsBehavior(event))
			fMultilineCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		if (fStringScanner.affectsBehavior(event))
			fStringScanner.adaptToPreferenceChange(event);
		if (fCppPreprocessorScanner.affectsBehavior(event))
			fCppPreprocessorScanner.adaptToPreferenceChange(event);
		if (fCPreprocessorScanner.affectsBehavior(event))
			fCPreprocessorScanner.adaptToPreferenceChange(event);
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectors(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 3.1
	 */
	public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
		if (!fPreferenceStore.getBoolean(AbstractDecoratedTextEditorPreferenceConstants.EDITOR_HYPERLINKS_ENABLED))
			return null;
		
		IHyperlinkDetector[] inheritedDetectors= super.getHyperlinkDetectors(sourceViewer);
		
		if (fTextEditor == null)
			return inheritedDetectors;
		
		int inheritedDetectorsLength= inheritedDetectors != null ? inheritedDetectors.length : 0;
		IHyperlinkDetector[] detectors= new IHyperlinkDetector[inheritedDetectorsLength + 1];
		detectors[0]= new CElementHyperlinkDetector(fTextEditor); 
		for (int i= 0; i < inheritedDetectorsLength; i++) {
			detectors[i+1]= inheritedDetectors[i];
		}
		
		return detectors;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
	 */
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		if (fDocumentPartitioning != null)
			return fDocumentPartitioning;
		return super.getConfiguredDocumentPartitioning(sourceViewer);
	}

	/**
     * Creates control for outline presentation in editor.
     * @return Control.
     */
    private IInformationControlCreator getOutlineControlCreator() {
        final IInformationControlCreator conrolCreator = new IInformationControlCreator() {
            /**
             * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
             */
            public IInformationControl createInformationControl(Shell parent) {
                int shellStyle= SWT.RESIZE;
                int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
                return new COutlineInformationControl(parent, shellStyle, treeStyle);   
            }
        };
        return conrolCreator;
    }
    
	protected ILanguage getLanguage() {
		if (fTextEditor == null) {
			return null;
		}
		ICElement element = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fTextEditor.getEditorInput());
		if (element instanceof ITranslationUnit) {
			try {
				return ((ITranslationUnit)element).getLanguage();
			} catch (CoreException e) {
				CUIPlugin.getDefault().log(e);
			}
		} else {
			// compute the language from the plain editor input
			IContentType contentType = null;
			IEditorInput input = fTextEditor.getEditorInput();
			IFile file = ResourceUtil.getFile(input);
			if (file != null) {
				contentType = CCorePlugin.getContentType(file.getProject(), file.getName());
			} else if (input instanceof IPathEditorInput) {
				IPath path = ((IPathEditorInput)input).getPath();
				contentType = CCorePlugin.getContentType(path.lastSegment());
			} else {
				ILocationProvider locationProvider = (ILocationProvider)input.getAdapter(ILocationProvider.class);
				if (locationProvider != null) {
					IPath path = locationProvider.getPath(input);
					contentType = CCorePlugin.getContentType(path.lastSegment());
				}
			}
			if (contentType != null) {
				return LanguageManager.getInstance().getLanguage(contentType);
			}
		}
		return null;
	}
}
