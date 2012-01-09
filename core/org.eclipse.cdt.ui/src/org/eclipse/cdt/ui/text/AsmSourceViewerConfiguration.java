/*******************************************************************************
 * Copyright (c) 2000, 2009 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *******************************************************************************/
package org.eclipse.cdt.ui.text;


import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.editors.text.ILocationProvider;
import org.eclipse.ui.editors.text.TextSourceViewerConfiguration;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.texteditor.ITextEditor;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.AssemblyLanguage;
import org.eclipse.cdt.core.model.IAsmLanguage;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.core.model.LanguageManager;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ILanguageUI;

import org.eclipse.cdt.internal.ui.editor.asm.AsmCodeScanner;
import org.eclipse.cdt.internal.ui.editor.asm.AsmPreprocessorScanner;
import org.eclipse.cdt.internal.ui.editor.asm.AsmReconcilingStrategy;
import org.eclipse.cdt.internal.ui.text.CCommentScanner;
import org.eclipse.cdt.internal.ui.text.PartitionDamager;
import org.eclipse.cdt.internal.ui.text.SingleTokenCScanner;
import org.eclipse.cdt.internal.ui.text.TokenStore;

/**
 * Configuration for a source viewer which shows Assembly code.
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 *
 * @noextend This class is not intended to be subclassed by clients.
 * 
 * @since 5.1
 */
public class AsmSourceViewerConfiguration extends TextSourceViewerConfiguration {

	private ITextEditor fTextEditor;
	/**
	 * The code scanner.
	 */
	private AbstractCScanner fCodeScanner;
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
	 * The preprocessor scanner.
	 */
	private AbstractCScanner fPreprocessorScanner;
	/**
	 * The color manager.
	 */
	private IColorManager fColorManager;
	/**
	 * The document partitioning.
	 */
	private String fDocumentPartitioning;
	
	/**
	 * Creates a new assembly source viewer configuration for viewers in the given editor
	 * using the given preference store, the color manager and the specified document partitioning.
	 *
	 * @param colorManager the color manager
	 * @param preferenceStore the preference store, can be read-only
	 * @param editor the editor in which the configured viewer(s) will reside, or <code>null</code> if none
	 * @param partitioning the document partitioning for this configuration, or <code>null</code> for the default partitioning
	 */
	public AsmSourceViewerConfiguration(IColorManager colorManager, IPreferenceStore preferenceStore, ITextEditor editor, String partitioning) {
		super(preferenceStore);
		fColorManager= colorManager;
		fTextEditor= editor;
		fDocumentPartitioning= partitioning;
		initializeScanners();
	}

	/**
	 * Initializes the scanners.
	 */
	private void initializeScanners() {
		fMultilineCommentScanner= new CCommentScanner(getTokenStoreFactory(), ICColorConstants.C_MULTI_LINE_COMMENT);
		fSinglelineCommentScanner= new CCommentScanner(getTokenStoreFactory(), ICColorConstants.C_SINGLE_LINE_COMMENT);
		fStringScanner= new SingleTokenCScanner(getTokenStoreFactory(), ICColorConstants.C_STRING);
	}

	/**
	 * Returns the ASM multiline comment scanner for this configuration.
	 *
	 * @return the ASM multiline comment scanner
	 */
	public RuleBasedScanner getMultilineCommentScanner() {
		return fMultilineCommentScanner;
	}
	
	/**
	 * Returns the ASM singleline comment scanner for this configuration.
	 *
	 * @return the ASM singleline comment scanner
	 */
	public RuleBasedScanner getSinglelineCommentScanner() {
		return fSinglelineCommentScanner;
	}
	
	/**
	 * Returns the ASM string scanner for this configuration.
	 *
	 * @return the ASM string scanner
	 */
	public RuleBasedScanner getStringScanner() {
		return fStringScanner;
	}

	/**
	 * Returns the assembly preprocessor scanner for this configuration.
	 * @param language
	 *
	 * @return the assembly preprocessor scanner
	 */
	public RuleBasedScanner getPreprocessorScanner(ILanguage language) {
		if (fPreprocessorScanner != null) {
			return fPreprocessorScanner;
		}
		AbstractCScanner scanner= null;
		if (language instanceof IAsmLanguage) {
			scanner= new AsmPreprocessorScanner(getTokenStoreFactory(), (IAsmLanguage)language);
		}
		if (scanner == null) {
			scanner= new AsmPreprocessorScanner(getTokenStoreFactory(), AssemblyLanguage.getDefault());
		}
		fPreprocessorScanner= scanner;
		return fPreprocessorScanner;
	}

	/**
	 * @param language
	 * @return the assembly code scanner for the given language
	 */
	public RuleBasedScanner getCodeScanner(ILanguage language) {
		if (fCodeScanner != null) {
			return fCodeScanner;
		}
		RuleBasedScanner scanner= null;
		if (language instanceof IAsmLanguage) {
			IAsmLanguage asmLang= (IAsmLanguage)language;
			scanner = new AsmCodeScanner(getTokenStoreFactory(), asmLang);
		} else if (language != null) {
			ILanguageUI languageUI = (ILanguageUI)language.getAdapter(ILanguageUI.class);
			if (languageUI != null)
				scanner = languageUI.getCodeScanner();
		}
		if (scanner == null) {
			scanner = new AsmCodeScanner(getTokenStoreFactory(), AssemblyLanguage.getDefault());
		}
		if (scanner instanceof AbstractCScanner) {
			fCodeScanner= (AbstractCScanner)scanner;
		}
		return scanner;
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

	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getPresentationReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {

		PresentationReconciler reconciler= new PresentationReconciler();

		ILanguage language= getLanguage();
		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(getCodeScanner(language));
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr= new DefaultDamagerRepairer(getMultilineCommentScanner());		
		reconciler.setDamager(dr, ICPartitions.C_MULTI_LINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_MULTI_LINE_COMMENT);
		
		dr= new DefaultDamagerRepairer(getSinglelineCommentScanner());		
		reconciler.setDamager(dr, ICPartitions.C_SINGLE_LINE_COMMENT);
		reconciler.setRepairer(dr, ICPartitions.C_SINGLE_LINE_COMMENT);

		dr= new DefaultDamagerRepairer(getStringScanner());		
		reconciler.setDamager(dr, ICPartitions.C_STRING);
		reconciler.setRepairer(dr, ICPartitions.C_STRING);

		dr= new DefaultDamagerRepairer(getStringScanner());		
		reconciler.setDamager(dr, ICPartitions.C_CHARACTER);
		reconciler.setRepairer(dr, ICPartitions.C_CHARACTER);

		dr= new DefaultDamagerRepairer(getPreprocessorScanner(language));		
		reconciler.setDamager(new PartitionDamager(), ICPartitions.C_PREPROCESSOR);
		reconciler.setRepairer(dr, ICPartitions.C_PREPROCESSOR);

		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));
		return reconciler;
	}
	
	/*
	 * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getConfiguredContentTypes(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		return new String[] { 	
				IDocument.DEFAULT_CONTENT_TYPE, 
				ICPartitions.C_MULTI_LINE_COMMENT,
				ICPartitions.C_SINGLE_LINE_COMMENT,
				ICPartitions.C_STRING,
				ICPartitions.C_CHARACTER,
				ICPartitions.C_PREPROCESSOR};
	}

	/*
	 * @see org.eclipse.ui.editors.text.TextSourceViewerConfiguration#getReconciler(org.eclipse.jface.text.source.ISourceViewer)
	 */
	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		if (fTextEditor != null) {
			MonoReconciler reconciler= new MonoReconciler(new AsmReconcilingStrategy(fTextEditor), false);
			reconciler.setIsIncrementalReconciler(false);
			reconciler.setProgressMonitor(new NullProgressMonitor());
			reconciler.setDelay(500);
			return reconciler;
		}
		return super.getReconciler(sourceViewer);
	}

	/**
	 * Determines whether the preference change encoded by the given event
	 * changes the behavior of one of its contained components.
	 *
	 * @param event the event to be investigated
	 * @return <code>true</code> if event causes a behavioral change
	 */
	public boolean affectsTextPresentation(PropertyChangeEvent event) {
		if (fMultilineCommentScanner.affectsBehavior(event)
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
		if (fMultilineCommentScanner.affectsBehavior(event))
			fMultilineCommentScanner.adaptToPreferenceChange(event);
		if (fSinglelineCommentScanner.affectsBehavior(event))
			fSinglelineCommentScanner.adaptToPreferenceChange(event);
		if (fStringScanner.affectsBehavior(event))
			fStringScanner.adaptToPreferenceChange(event);
		if (fPreprocessorScanner != null && fPreprocessorScanner.affectsBehavior(event))
			fPreprocessorScanner.adaptToPreferenceChange(event);
	}

	/**
	 * Returns the color manager for this configuration.
	 *
	 * @return the color manager
	 */
	public IColorManager getColorManager() {
		return fColorManager;
	}

	public ILanguage getLanguage() {
		if (fTextEditor == null) {
			return AssemblyLanguage.getDefault();
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
					contentType = CCorePlugin.getContentType(path.lastSegment());
				}
			}
			if (contentType != null) {
				return LanguageManager.getInstance().getLanguage(contentType);
			}
		}
		// fallback
		return AssemblyLanguage.getDefault();
	}

	/**
	 * Reset cached language dependent scanners.
	 */
	public void resetScanners() {
		fCodeScanner= null;
		fPreprocessorScanner= null;
	}

	private ITokenStoreFactory getTokenStoreFactory() {
		return new ITokenStoreFactory() {
			@Override
			public ITokenStore createTokenStore(String[] propertyColorNames) {
				return new TokenStore(getColorManager(), fPreferenceStore, propertyColorNames);
			}
		};
	}
}


