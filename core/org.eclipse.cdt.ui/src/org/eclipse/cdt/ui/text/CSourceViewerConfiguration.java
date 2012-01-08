/*******************************************************************************
 *  Copyright (c) 2005, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 * 
 *  Contributors:
 *     IBM Corporation - initial API and implementation
 *     QNX Software System
 *     Anton Leherbauer (Wind River Systems)
 *     Sergey Prigogin (Google)
 *     Markus Schorn (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.text;

import java.util.Arrays;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.AbstractInformationControlManager;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.DefaultTextDoubleClickStrategy;
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
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.quickassist.IQuickAssistAssistant;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.Annotation;
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
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.ui.CElementContentProvider;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ILanguageUI;
import org.eclipse.cdt.ui.text.doctools.DefaultMultilineCommentAutoEditStrategy;
import org.eclipse.cdt.ui.text.doctools.IDocCommentOwner;
import org.eclipse.cdt.ui.text.doctools.IDocCommentViewerConfiguration;

import org.eclipse.cdt.internal.core.model.ProgressMonitorAndCanceler;
import org.eclipse.cdt.internal.corext.util.CodeFormatterUtil;

import org.eclipse.cdt.internal.ui.editor.CDocumentProvider;
import org.eclipse.cdt.internal.ui.text.CAutoIndentStrategy;
import org.eclipse.cdt.internal.ui.text.CCodeScanner;
import org.eclipse.cdt.internal.ui.text.CCommentScanner;
import org.eclipse.cdt.internal.ui.text.CCompositeReconcilingStrategy;
import org.eclipse.cdt.internal.ui.text.CDoubleClickSelector;
import org.eclipse.cdt.internal.ui.text.CFormattingStrategy;
import org.eclipse.cdt.internal.ui.text.COutlineInformationControl;
import org.eclipse.cdt.internal.ui.text.CPreprocessorScanner;
import org.eclipse.cdt.internal.ui.text.CPresentationReconciler;
import org.eclipse.cdt.internal.ui.text.CReconciler;
import org.eclipse.cdt.internal.ui.text.CStringAutoIndentStrategy;
import org.eclipse.cdt.internal.ui.text.CStringDoubleClickSelector;
import org.eclipse.cdt.internal.ui.text.HTMLAnnotationHover;
import org.eclipse.cdt.internal.ui.text.PartitionDamager;
import org.eclipse.cdt.internal.ui.text.SingleTokenCScanner;
import org.eclipse.cdt.internal.ui.text.TokenStore;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverDescriptor;
import org.eclipse.cdt.internal.ui.text.c.hover.CEditorTextHoverProxy;
import org.eclipse.cdt.internal.ui.text.c.hover.CInformationProvider;
import org.eclipse.cdt.internal.ui.text.c.hover.CMacroExpansionExplorationControl;
import org.eclipse.cdt.internal.ui.text.c.hover.CMacroExpansionInformationProvider;
import org.eclipse.cdt.internal.ui.text.contentassist.CContentAssistProcessor;
import org.eclipse.cdt.internal.ui.text.contentassist.ContentAssistPreference;
import org.eclipse.cdt.internal.ui.text.correction.CCorrectionAssistant;
import org.eclipse.cdt.internal.ui.text.doctools.DocCommentOwnerManager;
import org.eclipse.cdt.internal.ui.typehierarchy.THInformationControl;
import org.eclipse.cdt.internal.ui.typehierarchy.THInformationProvider;


/**
 * Configuration for a <code>SourceViewer</code> which shows C/C++ code.
 * <p>
 * This class may be instantiated and subclassed by clients.
 * </p>
 * 
 * @since 5.1
 */
public class CSourceViewerConfiguration extends TextSourceViewerConfiguration {
	
	protected ITextEditor fTextEditor;
	/**
	 * The document partitioning.
	 */
	protected String fDocumentPartitioning;
	/**
	 * The code scanner.
	 */
	protected AbstractCScanner fCodeScanner;
	/**
	 * The C multi-line comment scanner.
	 */
	protected ICTokenScanner fMultilineCommentScanner;
	/**
	 * The C single-line comment scanner.
	 */
	protected ICTokenScanner fSinglelineCommentScanner;
	/**
	 * The C multi-line doc comment scanner.
	 */
	protected ICTokenScanner fMultilineDocCommentScanner;
	/**
	 * The C single-line doc comment scanner.
	 */
	protected ICTokenScanner fSinglelineDocCommentScanner;
	/**
	 * The C string scanner.
	 */
	protected AbstractCScanner fStringScanner;
	/**
	 * The preprocessor scanner.
	 */
	protected AbstractCScanner fPreprocessorScanner;
	/**
	 * The color manager.
	 */
	protected IColorManager fColorManager;
	
	/**
	 * Creates a new C source viewer configuration for viewers in the given editor
	 * using the given preference store, the color manager and the specified document partitioning.
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
	 * Returns the C string scanner for this configuration.
	 *
	 * @return the C string scanner
	 */
	protected RuleBasedScanner getStringScanner() {
		return fStringScanner;
	}

	/**
	 * Returns the preprocessor scanner for this configuration.
	 *
	 * @return the preprocessor scanner
	 */
	protected RuleBasedScanner getPreprocessorScanner(ILanguage language) {
		if (fPreprocessorScanner != null) {
			return fPreprocessorScanner;
		}
		AbstractCScanner scanner= null;
		ICLanguageKeywords keywords = language == null ? null : (ICLanguageKeywords) language.getAdapter(ICLanguageKeywords.class);
		if (keywords != null) {
			scanner = new CPreprocessorScanner(getTokenStoreFactory(), keywords);
		}
		if (scanner == null) {
			keywords = (ICLanguageKeywords) GPPLanguage.getDefault().getAdapter(ICLanguageKeywords.class);
			scanner= new CPreprocessorScanner(getTokenStoreFactory(), keywords);
		}
		fPreprocessorScanner= scanner;
		return fPreprocessorScanner;
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
		String[] contentTypes= getConfiguredContentTypes(sourceViewer);
		for (int i= 0; i < contentTypes.length; i++)
			presenter.setInformationProvider(provider, contentTypes[i]);
        presenter.setSizeConstraints(50, 20, true, false);
        return presenter;
    }

    /**
     * Creates type hierarchy presenter.
     * @return Presenter with type hierarchy view.
     */
    public IInformationPresenter getHierarchyPresenter(ISourceViewer sourceViewer) {
        final IInformationControlCreator hierarchyControlCreator = getHierarchyControlCreator();
        final InformationPresenter presenter = new InformationPresenter(hierarchyControlCreator);
        presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
		final IInformationProvider provider = new THInformationProvider(getEditor());
		String[] contentTypes= getConfiguredContentTypes(sourceViewer);
		for (int i= 0; i < contentTypes.length; i++)
			presenter.setInformationProvider(provider, contentTypes[i]);
        presenter.setSizeConstraints(50, 20, true, false);
        return presenter;
    }
    
	/**
	 * Initializes language independent scanners.
	 */
	protected void initializeScanners() {
		fStringScanner= new SingleTokenCScanner(getTokenStoreFactory(), ICColorConstants.C_STRING);
		fMultilineCommentScanner= new CCommentScanner(getTokenStoreFactory(),  ICColorConstants.C_MULTI_LINE_COMMENT);
		fSinglelineCommentScanner= new CCommentScanner(getTokenStoreFactory(),  ICColorConstants.C_SINGLE_LINE_COMMENT);
	}

    /**
     * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
    @Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		CPresentationReconciler reconciler= new CPresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		ILanguage language= getLanguage();
		RuleBasedScanner scanner = getCodeScanner(language);

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);

		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);
		
		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());
		reconciler.setDamager(dr, ICPartitions.C_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_SINGLE_LINE_COMMENT);

		dr= new DefaultDamagerRepairer(getMultilineCommentScanner());
		reconciler.setDamager(dr, ICPartitions.C_MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_MULTI_LINE_COMMENT);
		
		ICTokenScanner docCommentSingleScanner= getSinglelineDocCommentScanner(getProject());
		if(docCommentSingleScanner!=null) {
			dr= new DefaultDamagerRepairer(docCommentSingleScanner);
			reconciler.setDamager(dr, ICPartitions.C_SINGLE_LINE_DOC_COMMENT);
			reconciler.setRepairer(dr, ICPartitions.C_SINGLE_LINE_DOC_COMMENT);
		}
	
		ICTokenScanner docCommentMultiScanner= getMultilineDocCommentScanner(getProject());
		if(docCommentMultiScanner!=null) {
			dr= new DefaultDamagerRepairer(docCommentMultiScanner);
			reconciler.setDamager(dr, ICPartitions.C_MULTI_LINE_DOC_COMMENT);
			reconciler.setRepairer(dr, ICPartitions.C_MULTI_LINE_DOC_COMMENT);
		}
		
		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, ICPartitions.C_STRING);
		reconciler.setRepairer(dr, ICPartitions.C_STRING);
		
		dr= new DefaultDamagerRepairer(getStringScanner());
		reconciler.setDamager(dr, ICPartitions.C_CHARACTER);
		reconciler.setRepairer(dr, ICPartitions.C_CHARACTER);
		
		dr= new DefaultDamagerRepairer(getPreprocessorScanner(language));
		reconciler.setDamager(new PartitionDamager(), ICPartitions.C_PREPROCESSOR);
		reconciler.setRepairer(dr, ICPartitions.C_PREPROCESSOR);
		
		return reconciler;
	}

	/**
	 * Returns the C multi-line comment scanner for this configuration.
	 *
	 * @return the C multi-line comment scanner
	 */
	protected ICTokenScanner getMultilineCommentScanner() {
		return fMultilineCommentScanner;
	}

	/**
	 * Returns the C single-line comment scanner for this configuration.
	 *
	 * @return the C single-line comment scanner
	 */
	protected ICTokenScanner getSinglelineCommentScanner() {
		return fSinglelineCommentScanner;
	}

	/**
	 * Returns the C multi-line doc comment scanner for this configuration.
	 *
	 * @return the C multi-line doc comment scanner
	 */
	protected ICTokenScanner getMultilineDocCommentScanner(IResource resource) {
		if (fMultilineDocCommentScanner == null) {
			if (resource == null) {
				resource= ResourcesPlugin.getWorkspace().getRoot();
			}
			IDocCommentViewerConfiguration owner= DocCommentOwnerManager.getInstance().getCommentOwner(resource).getMultilineConfiguration();
			fMultilineDocCommentScanner= owner.createCommentScanner(getTokenStoreFactory());
			if (fMultilineDocCommentScanner == null) {
				// fallback: normal comment highlighting
				fMultilineDocCommentScanner= fMultilineCommentScanner;
			}
		}
		return fMultilineDocCommentScanner;
	}

	/**
	 * Returns the C single-line doc comment scanner for this configuration.
	 *
	 * @return the C single-line doc comment scanner
	 */
	protected ICTokenScanner getSinglelineDocCommentScanner(IResource resource) {
		if (fSinglelineDocCommentScanner == null) {
			if (resource == null) {
				resource= ResourcesPlugin.getWorkspace().getRoot();
			}
			IDocCommentViewerConfiguration owner= DocCommentOwnerManager.getInstance().getCommentOwner(resource).getSinglelineConfiguration();
			fSinglelineDocCommentScanner= owner.createCommentScanner(getTokenStoreFactory());
			if (fSinglelineDocCommentScanner == null) {
				// fallback: normal comment highlighting
				fSinglelineDocCommentScanner= fSinglelineCommentScanner;
			}
		}
		return fSinglelineDocCommentScanner;
	}
    
	/**
	 * @return the code scanner for the given language
	 */
	protected RuleBasedScanner getCodeScanner(ILanguage language) {
		if (fCodeScanner != null) {
			return fCodeScanner;
		}
		RuleBasedScanner scanner= null;
		
		if(language != null) {
			ICLanguageKeywords keywords = (ICLanguageKeywords) language.getAdapter(ICLanguageKeywords.class);
			if(keywords != null) {
				scanner = new CCodeScanner(getTokenStoreFactory(), keywords);
			}
			else {
				ILanguageUI languageUI = (ILanguageUI)language.getAdapter(ILanguageUI.class);
				if (languageUI != null)
					scanner = languageUI.getCodeScanner();
			}
		}
		
		if (scanner == null) {
			scanner = new CCodeScanner(getTokenStoreFactory(), GPPLanguage.getDefault());
		}
		if (scanner instanceof AbstractCScanner) {
			fCodeScanner= (AbstractCScanner)scanner;
		}
		return scanner;
	}

	/*
	 * @see SourceViewerConfiguration#getContentAssistant(ISourceViewer)
	 */
	@Override
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

		processor = new CContentAssistProcessor(getEditor(), assistant, ICPartitions.C_MULTI_LINE_DOC_COMMENT);
		assistant.setContentAssistProcessor(processor, ICPartitions.C_MULTI_LINE_DOC_COMMENT);

		processor = new CContentAssistProcessor(getEditor(), assistant, ICPartitions.C_SINGLE_LINE_DOC_COMMENT);
		assistant.setContentAssistProcessor(processor, ICPartitions.C_SINGLE_LINE_DOC_COMMENT);

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
	protected IDialogSettings getSettings(String sectionName) {
		IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings().getSection(sectionName);
		if (settings == null)
			settings= CUIPlugin.getDefault().getDialogSettings().addNewSection(sectionName);

		return settings;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getQuickAssistAssistant(org.eclipse.jface.text.source.ISourceViewer)
	 * @since 5.0
	 */
	@Override
	public IQuickAssistAssistant getQuickAssistAssistant(ISourceViewer sourceViewer) {
		if (getEditor() != null)
			return new CCorrectionAssistant(getEditor());
		return null;
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fTextEditor != null) {
			//Delay changed and non-incremental reconciler used due to
			//PR 130089
			CCompositeReconcilingStrategy strategy=
				new CCompositeReconcilingStrategy(sourceViewer, fTextEditor, getConfiguredDocumentPartitioning(sourceViewer));
			MonoReconciler reconciler= new CReconciler(fTextEditor, strategy);
			reconciler.setIsIncrementalReconciler(false);
			reconciler.setProgressMonitor(new ProgressMonitorAndCanceler());
			reconciler.setDelay(500);
			return reconciler;
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getAutoEditStrategies(org.eclipse.jface.text.source.ISourceViewer, java.lang.String)
	 */
	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
		String partitioning= getConfiguredDocumentPartitioning(sourceViewer);
		
		IDocCommentOwner owner= DocCommentOwnerManager.getInstance().getCommentOwner(getProject());
		IAutoEditStrategy single= owner.getSinglelineConfiguration().createAutoEditStrategy();
		IAutoEditStrategy multi= owner.getMultilineConfiguration().createAutoEditStrategy();
		
		IAutoEditStrategy[] NONE= new IAutoEditStrategy[0];
		
		if (ICPartitions.C_MULTI_LINE_COMMENT.equals(contentType))
			return new IAutoEditStrategy[] { new DefaultMultilineCommentAutoEditStrategy() };
		if (ICPartitions.C_SINGLE_LINE_DOC_COMMENT.equals(contentType))
			return single!=null ? new IAutoEditStrategy[] {single} : NONE;
		else if (ICPartitions.C_MULTI_LINE_DOC_COMMENT.equals(contentType))
			return multi!=null? new IAutoEditStrategy[] {multi} : NONE;
		else if (ICPartitions.C_STRING.equals(contentType))
			return new IAutoEditStrategy[] { /*new SmartSemicolonAutoEditStrategy(partitioning),*/ new CStringAutoIndentStrategy(partitioning, getCProject()) };
		else
			return new IAutoEditStrategy[] { new CAutoIndentStrategy(partitioning, getCProject()) };
	}
	
	/**
	 * @see SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
	 */
	@Override
	public ITextDoubleClickStrategy getDoubleClickStrategy(ISourceViewer sourceViewer, String contentType) {
		if (ICPartitions.C_MULTI_LINE_COMMENT.equals(contentType) ||
				ICPartitions.C_SINGLE_LINE_COMMENT.equals(contentType)) {
			return new DefaultTextDoubleClickStrategy();
		} else if (ICPartitions.C_SINGLE_LINE_DOC_COMMENT.equals(contentType)) {
			IDocCommentOwner owner= DocCommentOwnerManager.getInstance().getCommentOwner(getProject());
			ITextDoubleClickStrategy single= owner.getSinglelineConfiguration().createDoubleClickStrategy();
			return single != null ? single : new DefaultTextDoubleClickStrategy();
		} else if(ICPartitions.C_MULTI_LINE_DOC_COMMENT.equals(contentType)) {
			IDocCommentOwner owner= DocCommentOwnerManager.getInstance().getCommentOwner(getProject());
			ITextDoubleClickStrategy multi= owner.getMultilineConfiguration().createDoubleClickStrategy();
			return multi!=null ? multi : new DefaultTextDoubleClickStrategy();
		} else if (ICPartitions.C_STRING.equals(contentType) ||
				ICPartitions.C_CHARACTER.equals(contentType)) {
			return new CStringDoubleClickSelector(getConfiguredDocumentPartitioning(sourceViewer));
		} else if (ICPartitions.C_PREPROCESSOR.equals(contentType)) {
			return new CStringDoubleClickSelector(getConfiguredDocumentPartitioning(sourceViewer), new CDoubleClickSelector());
		}
		return new CDoubleClickSelector();
	}

	/**
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDefaultPrefixes(ISourceViewer, String)
	 */
	@Override
	public String[] getDefaultPrefixes(ISourceViewer sourceViewer, String contentType) {
		return new String[] { "//", "//!", "" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

	/*
	 * @see SourceViewerConfiguration#getIndentPrefixes(ISourceViewer, String)
	 */
	@Override
	public String[] getIndentPrefixes(ISourceViewer sourceViewer, String contentType) {
		ICProject project= getCProject();
		final int tabWidth= CodeFormatterUtil.getTabWidth(project);
		final int indentWidth= CodeFormatterUtil.getIndentWidth(project);
		boolean allowTabs= tabWidth <= indentWidth;
		
		String indentMode;
		if (project == null)
			indentMode= CCorePlugin.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		else
			indentMode= project.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, true);

		boolean useSpaces= CCorePlugin.SPACE.equals(indentMode) || DefaultCodeFormatterConstants.MIXED.equals(indentMode);
		
		// assert allowTabs || useSpaces;
		
		if (!allowTabs)
			return new String[] { getStringWithSpaces(indentWidth), "" }; //$NON-NLS-1$
		else if  (!useSpaces)
			return getIndentPrefixesForTab(tabWidth);
		else
			return getIndentPrefixesForSpaces(tabWidth);
	}

	/**
	 * Computes and returns the indent prefixes for space indentation
	 * and the given <code>tabWidth</code>.
	 * 
	 * @param tabWidth the display tab width
	 * @return the indent prefixes
	 * @see #getIndentPrefixes(ISourceViewer, String)
	 */
	protected String[] getIndentPrefixesForSpaces(int tabWidth) {
		String[] indentPrefixes= new String[tabWidth + 2];
		indentPrefixes[0]= getStringWithSpaces(tabWidth);
		
		for (int i= 0; i < tabWidth; i++) {
			String spaces= getStringWithSpaces(i);
			if (i < tabWidth)
				indentPrefixes[i+1]= spaces + '\t';
			else
				indentPrefixes[i+1]= new String(spaces);
		}
		
		indentPrefixes[tabWidth + 1]= ""; //$NON-NLS-1$

		return indentPrefixes;
	}

	/**
	 * Creates and returns a String with <code>count</code> spaces.
	 * 
	 * @param count	the space count
	 * @return the string with the spaces
	 */
	protected static String getStringWithSpaces(int count) {
		char[] spaceChars= new char[count];
		Arrays.fill(spaceChars, ' ');
		return new String(spaceChars);
	}

	/**
	 * Returns the ICProject associated with this CSourceViewerConfiguration, or null if
	 * no ICProject could be determined
	 * @return the ICProject or <code>null</code>
	 */
	protected ICProject getCProject() {
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
	@Override
	public int getTabWidth(ISourceViewer sourceViewer) {
		return CodeFormatterUtil.getTabWidth(getCProject());
	}

	/**
	 * Returns the configured indent width for this viewer.
	 * @param sourceViewer
	 * @return the indent width
	 */
	public int getIndentWidth(ISourceViewer sourceViewer) {
		return CodeFormatterUtil.getIndentWidth(getCProject());
	}

	/**
	 * Returns whether spaces should be used exclusively for indentation.
	 * 
	 * @param sourceViewer
	 * @return <code>true</code> if spaces should be used for indentation
	 */
	public boolean useSpacesOnly(ISourceViewer sourceViewer) {
		ICProject project= getCProject();
		String option;
		if (project == null)
			option= CCorePlugin.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
		else
			option= project.getOption(DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR, true);
		return CCorePlugin.SPACE.equals(option);
	}
	
	/**
	 * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
	 */
	@Override
	public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
		return new HTMLAnnotationHover() {
			@Override
			protected boolean isIncluded(Annotation annotation) {
				return isShowInVerticalRuler(annotation);
			}
		};
	}

	/*
	 * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer, String)
	 * @since 2.1
	 */
	@Override
	public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
		CEditorTextHoverDescriptor[] hoverDescs= CUIPlugin.getDefault().getCEditorTextHoverDescriptors();
		int stateMasks[]= new int[hoverDescs.length];
		int stateMasksLength= 0;
		for (CEditorTextHoverDescriptor hoverDesc : hoverDescs) {
			if (hoverDesc.isEnabled()) {
				int j= 0;
				int stateMask= hoverDesc.getStateMask();
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
	@Override
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
	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		return getTextHover(sourceViewer, contentType, ITextViewerExtension2.DEFAULT_HOVER_STATE_MASK);
	}

	/**
	 * @see SourceViewerConfiguration#getConfiguredContentTypes(ISourceViewer)
	 */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] {
				IDocument.DEFAULT_CONTENT_TYPE,
				ICPartitions.C_MULTI_LINE_COMMENT,
				ICPartitions.C_SINGLE_LINE_COMMENT,
				ICPartitions.C_STRING,
				ICPartitions.C_CHARACTER,
				ICPartitions.C_PREPROCESSOR,
				ICPartitions.C_SINGLE_LINE_DOC_COMMENT,
				ICPartitions.C_MULTI_LINE_DOC_COMMENT
		};
	}
	
	/**
	 * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
	 */
	@Override
	public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
		
		final MultiPassContentFormatter formatter =
			new MultiPassContentFormatter(getConfiguredDocumentPartitioning(sourceViewer),
				IDocument.DEFAULT_CONTENT_TYPE);
		
		formatter.setMasterStrategy(new CFormattingStrategy());
		return formatter;
	}
	
	public boolean affectsBehavior(PropertyChangeEvent event) {
		if ((fMultilineDocCommentScanner != null && fMultilineDocCommentScanner.affectsBehavior(event))
			|| (fSinglelineDocCommentScanner != null && fSinglelineDocCommentScanner.affectsBehavior(event))
			|| fMultilineCommentScanner.affectsBehavior(event)
			|| fSinglelineCommentScanner.affectsBehavior(event)
			|| fStringScanner.affectsBehavior(event)) {
			return true;
		}
		if (fCodeScanner != null && fCodeScanner.affectsBehavior(event)) {
			return true;
		}
		if (fPreprocessorScanner != null && fPreprocessorScanner.affectsBehavior(event)) {
			return true;
		}
		return false;
	}

	/*
	 * @see SourceViewerConfiguration#getHoverControlCreator(ISourceViewer)
	 * @since 2.0
	 */
	@Override
	public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, false);
			}
		};
	}

	/**
	 * Returns the information presenter control creator. The creator is a factory creating the
	 * presenter controls for the given source viewer. This implementation always returns a creator
	 * for <code>DefaultInformationControl</code> instances.
	 *
	 * @param sourceViewer the source viewer to be configured by this configuration
	 * @return an information control creator
	 * @since 5.0
	 */
	protected IInformationControlCreator getInformationPresenterControlCreator(ISourceViewer sourceViewer) {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(Shell parent) {
				return new DefaultInformationControl(parent, true);
			}
		};
	}

	/*
	 * @see SourceViewerConfiguration#getInformationPresenter(ISourceViewer)
	 * @since 2.0
	 */
	@Override
	public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
		InformationPresenter presenter= new InformationPresenter(getInformationPresenterControlCreator(sourceViewer));
		presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		
		// Register information provider
		IInformationProvider provider= new CInformationProvider(getEditor());
		String[] contentTypes= getConfiguredContentTypes(sourceViewer);
		for (String contentType : contentTypes)
			presenter.setInformationProvider(provider, contentType);
		
		presenter.setSizeConstraints(60, 10, true, true);
		return presenter;
	}
    
	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one of its contained components.
	 *
	 * @param event the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 */
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		return affectsBehavior(event);
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
		if (fCodeScanner != null && fCodeScanner.affectsBehavior(event))
			fCodeScanner.adaptToPreferenceChange(event);
		if (fMultilineDocCommentScanner!=null && fMultilineDocCommentScanner.affectsBehavior(event))
			fMultilineDocCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineDocCommentScanner!=null && fSinglelineDocCommentScanner.affectsBehavior(event))
			fSinglelineDocCommentScanner.adaptToPreferenceChange(event);
		if (fMultilineCommentScanner.affectsBehavior(event))
			fMultilineCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		if (fStringScanner.affectsBehavior(event))
			fStringScanner.adaptToPreferenceChange(event);
		if (fPreprocessorScanner != null && fPreprocessorScanner.affectsBehavior(event))
			fPreprocessorScanner.adaptToPreferenceChange(event);
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredDocumentPartitioning(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public String getConfiguredDocumentPartitioning(ISourceViewer sourceViewer) {
		if (fDocumentPartitioning != null)
			return fDocumentPartitioning;
		return super.getConfiguredDocumentPartitioning(sourceViewer);
	}

	/**
     * Creates control for outline presentation in editor.
     * @return Control.
     */
    protected IInformationControlCreator getOutlineControlCreator() {
        final IInformationControlCreator conrolCreator = new IInformationControlCreator() {
            /**
             * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
             */
            @Override
			public IInformationControl createInformationControl(Shell parent) {
                int shellStyle= SWT.RESIZE;
                int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
                return new COutlineInformationControl(parent, shellStyle, treeStyle);
            }
        };
        return conrolCreator;
    }

	/**
     * Creates control for outline presentation in editor.
     * @return Control.
     */
    protected IInformationControlCreator getHierarchyControlCreator() {
        final IInformationControlCreator conrolCreator = new IInformationControlCreator() {
            /**
             * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(org.eclipse.swt.widgets.Shell)
             */
            @Override
			public IInformationControl createInformationControl(Shell parent) {
                int shellStyle= SWT.RESIZE;
                int treeStyle= SWT.V_SCROLL | SWT.H_SCROLL;
                return new THInformationControl(parent, shellStyle, treeStyle);
            }
        };
        return conrolCreator;
    }

	protected ILanguage getLanguage() {
		if (fTextEditor == null) {
			return GPPLanguage.getDefault();
		}
		ICElement element = CUIPlugin.getDefault().getWorkingCopyManager().getWorkingCopy(fTextEditor.getEditorInput());
		if (element instanceof ITranslationUnit) {
			try {
				return ((ITranslationUnit)element).getLanguage();
			} catch (CoreException e) {
				CUIPlugin.log(e);
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
					if (path != null) {
						contentType = CCorePlugin.getContentType(path.lastSegment());
					}
				}
			}
			if (contentType != null) {
				return LanguageManager.getInstance().getLanguage(contentType);
			}
		}
		// fallback
		return GPPLanguage.getDefault();
	}
	
	/**
	 * Reset cached language dependent scanners.
	 */
	public void resetScanners() {
		fCodeScanner= null;
		fMultilineDocCommentScanner= null;
		fSinglelineDocCommentScanner= null;
		fPreprocessorScanner= null;
	}
	
	/**
	 * Creates macro exploration presenter.
	 * @param sourceViewer
	 * @return Presenter with macro exploration view.
	 * 
	 * @since 5.0
	 */
	public IInformationPresenter getMacroExplorationPresenter(ISourceViewer sourceViewer) {
        final IInformationControlCreator controlCreator= getMacroExplorationControlCreator();
        final InformationPresenter presenter = new InformationPresenter(controlCreator);
        presenter.setRestoreInformationControlBounds(getDialogSettings(CMacroExpansionExplorationControl.KEY_CONTROL_BOUNDS), true, true);
        presenter.setSizeConstraints(320, 120, true, false);
        presenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		presenter.setAnchor(AbstractInformationControlManager.ANCHOR_GLOBAL);
		final IInformationProvider provider = new CMacroExpansionInformationProvider(getEditor());
		String[] contentTypes= getConfiguredContentTypes(sourceViewer);
		for (int i= 0; i < contentTypes.length; i++)
			presenter.setInformationProvider(provider, contentTypes[i]);
        presenter.setSizeConstraints(50, 20, true, false);
        return presenter;
	}

	protected IDialogSettings getDialogSettings(String sectionName) {
		if (sectionName == null) {
			return null;
		}
		IDialogSettings settings= CUIPlugin.getDefault().getDialogSettings().getSection(sectionName);
		if (settings == null) {
			settings= CUIPlugin.getDefault().getDialogSettings().addNewSection(sectionName);
		}
		return settings;
	}
	
	/**
     * Creates control for macro exploration in editor.
     * @return Control.
     */
    protected IInformationControlCreator getMacroExplorationControlCreator() {
        final IInformationControlCreator conrolCreator = new IInformationControlCreator() {
            @Override
			public IInformationControl createInformationControl(Shell parent) {
                return new CMacroExpansionExplorationControl(parent);
            }
        };
        return conrolCreator;
    }
	
    /**
	 * @return the IProject associated with this CSourceViewerConfiguration, or null if
	 * no IProject could be determined
	 */
	protected IProject getProject() {
		ICProject cproject= getCProject();
		return cproject!=null ? cproject.getProject() :null;
	}

	protected ITokenStoreFactory getTokenStoreFactory() {
		return new ITokenStoreFactory() {
			@Override
			public ITokenStore createTokenStore(String[] propertyColorNames) {
				return new TokenStore(getColorManager(), fPreferenceStore, propertyColorNames);
			}
		};
	}

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getHyperlinkDetectorTargets(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	protected Map<String, IAdaptable> getHyperlinkDetectorTargets(ISourceViewer sourceViewer) {
		@SuppressWarnings("unchecked")
		Map<String, IAdaptable> targets= super.getHyperlinkDetectorTargets(sourceViewer);
		targets.put("org.eclipse.cdt.ui.cCode", fTextEditor); //$NON-NLS-1$
		return targets;
	}

}
