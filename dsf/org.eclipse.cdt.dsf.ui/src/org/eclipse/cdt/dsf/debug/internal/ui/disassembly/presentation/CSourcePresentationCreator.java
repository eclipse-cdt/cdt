/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation;

import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IAsmLanguage;
import org.eclipse.cdt.core.model.ICModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.ui.CDTUITools;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.text.AsmSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.CSourceViewerConfiguration;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.IColorManager;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFileState;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.formatter.IContentFormatter;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.IAnnotationHover;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;

/**
 * A presentation creator based on CDT syntax highlighting.
 */
public class CSourcePresentationCreator extends PresentationReconciler implements ISourcePresentationCreator, IPropertyChangeListener {

	/**
	 *
	 */
	private final static class CustomCSourceViewerConfiguration extends CSourceViewerConfiguration {
		/**
		 * Comment for <code>fLanguage</code>
		 */
		private final ILanguage fLanguage;
		private AsmSourceViewerConfiguration fAsmConfig;

		/**
		 * @param colorManager
		 * @param preferenceStore
		 * @param language
		 */
		private CustomCSourceViewerConfiguration(
				IColorManager colorManager, IPreferenceStore preferenceStore,
				ILanguage language) {
			super(colorManager, preferenceStore, null, ICPartitions.C_PARTITIONING);
			fLanguage = language;
			if (language instanceof IAsmLanguage) {
				fAsmConfig= new AsmSourceViewerConfiguration(colorManager, preferenceStore, null, ICPartitions.C_PARTITIONING);
			}
		}

		public void dispose() {
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration#getLanguage()
		 */
		@Override
		protected ILanguage getLanguage() {
			return fLanguage;
		}

		/**
		 * @param contentType
		 * @return
		 */
		public ITokenScanner getScannerForContentType(String contentType) {
			if (IDocument.DEFAULT_CONTENT_TYPE.equals(contentType)) {
				return getLanguage() != null ? getCodeScanner(getLanguage()) : null;
			} else if (ICPartitions.C_CHARACTER.equals(contentType)) {
				return getStringScanner();
			} else if (ICPartitions.C_STRING.equals(contentType)) {
				return getStringScanner();
			} else if (ICPartitions.C_SINGLE_LINE_COMMENT.equals(contentType)) {
				return getSinglelineCommentScanner();
			} else if (ICPartitions.C_SINGLE_LINE_DOC_COMMENT.equals(contentType)) {
				return getSinglelineDocCommentScanner(getProject());
			} else if (ICPartitions.C_MULTI_LINE_COMMENT.equals(contentType)) {
				return getMultilineCommentScanner();
			} else if (ICPartitions.C_MULTI_LINE_DOC_COMMENT.equals(contentType)) {
				return getMultilineDocCommentScanner(getProject());
			} else if (ICPartitions.C_PREPROCESSOR.equals(contentType)) {
				return getPreprocessorScanner(getLanguage());
			}
			return null;
		}

		/*
		 * @see org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration#getCodeScanner(org.eclipse.cdt.core.model.ILanguage)
		 */
		@Override
		protected RuleBasedScanner getCodeScanner(ILanguage language) {
			if (language instanceof IAsmLanguage) {
				return fAsmConfig.getCodeScanner(language);
			}
			return super.getCodeScanner(language);
		}
		
		/*
		 * @see org.eclipse.cdt.internal.ui.text.CSourceViewerConfiguration#getPreprocessorScanner(org.eclipse.cdt.core.model.ILanguage)
		 */
		@Override
		protected RuleBasedScanner getPreprocessorScanner(ILanguage language) {
			if (language instanceof IAsmLanguage) {
				return fAsmConfig.getPreprocessorScanner(language);
			}
			return super.getPreprocessorScanner(language);
		}

		/*
		 * @see SourceViewerConfiguration#getAutoEditStrategies(ISourceViewer, String)
		 */
		@Override
		public IAutoEditStrategy[] getAutoEditStrategies(ISourceViewer sourceViewer, String contentType) {
			return null;
		}

		/*
		 * @see SourceViewerConfiguration#getAnnotationHover(ISourceViewer)
		 */
		@Override
		public IAnnotationHover getAnnotationHover(ISourceViewer sourceViewer) {
			return null;
		}

		/*
		 * @see SourceViewerConfiguration#getOverviewRulerAnnotationHover(ISourceViewer)
		 */
		@Override
		public IAnnotationHover getOverviewRulerAnnotationHover(ISourceViewer sourceViewer) {
			return null;
		}

		/*
		 * @see SourceViewerConfiguration#getConfiguredTextHoverStateMasks(ISourceViewer, String)
		 */
		@Override
		public int[] getConfiguredTextHoverStateMasks(ISourceViewer sourceViewer, String contentType) {
			return null;
		}

		/*
		 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String, int)
		 */
		@Override
		public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType, int stateMask) {
			return null;
		}

		/*
		 * @see SourceViewerConfiguration#getTextHover(ISourceViewer, String)
		 */
		@Override
		public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
			return null;
		}

		/*
		 * @see SourceViewerConfiguration#getContentFormatter(ISourceViewer)
		 */
		@Override
		public IContentFormatter getContentFormatter(ISourceViewer sourceViewer) {
			return null;
		}

		/*
		 * @see SourceViewerConfiguration#getInformationControlCreator(ISourceViewer)
		 */
		@Override
		public IInformationControlCreator getInformationControlCreator(ISourceViewer sourceViewer) {
			return null;
		}

		/*
		 * @see SourceViewerConfiguration#getInformationPresenter(ISourceViewer)
		 */
		@Override
		public IInformationPresenter getInformationPresenter(ISourceViewer sourceViewer) {
			return null;
		}

		/*
		 * @see SourceViewerConfiguration#getHyperlinkDetectors(ISourceViewer)
		 */
		@Override
		public IHyperlinkDetector[] getHyperlinkDetectors(ISourceViewer sourceViewer) {
			return null;
		}
		
		/*
		 * @see CSourceViewerConfiguration#getOutlinePresenter(ISourceViewer)
		 */
		@Override
		public IInformationPresenter getOutlinePresenter(ISourceViewer sourceViewer) {
			return null;
		}
}

	private ITextViewer fViewer;
	private ISourceTagProvider fSourceTagProvider;
	private SourceTagDamagerRepairer fDamagerRepairer;
	private ISourceTagListener fSourceTagListener;
	private TextPresentation fPresentation;
	private CustomCSourceViewerConfiguration fSourceViewerConfiguration;
	private IPreferenceStore fPreferenceStore;

	/**
	 * @param language
	 * @param storage
	 * @param textViewer
	 */
	public CSourcePresentationCreator(ILanguage language, IStorage storage, ITextViewer textViewer) {
		if (language != null) {
			fViewer= textViewer;
			fPreferenceStore= CUIPlugin.getDefault().getCombinedPreferenceStore();
			final IColorManager colorManager= CDTUITools.getColorManager();
			fSourceViewerConfiguration= new CustomCSourceViewerConfiguration(colorManager, fPreferenceStore, language);
			setDocumentPartitioning(fSourceViewerConfiguration.getConfiguredDocumentPartitioning(null));
			initializeDamagerRepairer(storage, colorManager, fPreferenceStore);
			fPreferenceStore.addPropertyChangeListener(this);
		}
	}

	private void initializeDamagerRepairer(IStorage storage, IColorManager colorManager, IPreferenceStore store) {
		String[] contentTypes= fSourceViewerConfiguration.getConfiguredContentTypes(null);
		for (int i = 0; i < contentTypes.length; ++i) {
			String contentType = contentTypes[i];
			ITokenScanner scanner;
			scanner = fSourceViewerConfiguration.getScannerForContentType(contentType);
			if (scanner != null) {
				if (fDamagerRepairer == null) {
					fSourceTagProvider = createSourceTagProvider(storage);
					fDamagerRepairer= new SourceTagDamagerRepairer(scanner, fSourceTagProvider, colorManager, store);
					if (fSourceTagProvider != null) {
						if (fSourceTagListener == null) {
							fSourceTagListener= new ISourceTagListener() {
								@Override
								public void sourceTagsChanged(ISourceTagProvider provider) {
									handleSourceTagsChanged();
								}};
						}
						fSourceTagProvider.addSourceTagListener(fSourceTagListener);
					}
				}
				fDamagerRepairer.setScanner(contentType, scanner);
				setDamager(fDamagerRepairer, contentType);
				setRepairer(fDamagerRepairer, contentType);
			}
		}
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation.ISourcePresentationCreator#dispose()
	 */
	@Override
	public void dispose() {
		fViewer= null;
		fPresentation= null;
		if (fPreferenceStore != null) {
			fPreferenceStore.removePropertyChangeListener(this);
			fPreferenceStore= null;
		}
		if (fSourceViewerConfiguration != null) {
			fSourceViewerConfiguration.dispose();
			fSourceViewerConfiguration= null;
		}
		if (fSourceTagProvider != null) {
			if (fSourceTagListener != null) {
				fSourceTagProvider.removeSourceTagListener(fSourceTagListener);
				fSourceTagListener= null;
			}
			fSourceTagProvider= null;
		}
	}

	/*
	 * @see org.eclipse.cdt.dsf.debug.internal.ui.disassembly.presentation.ISourcePresentationCreator#getPresentation(org.eclipse.jface.text.IRegion, org.eclipse.jface.text.IDocument)
	 */
	@Override
	public TextPresentation getPresentation(IRegion region, IDocument document) {
		assert fViewer != null;
		if (fViewer == null) {
			return null;
		}
		if (fPresentation == null) {
			setDocumentToDamagers(document);
			setDocumentToRepairers(document);
			int docLength= document.getLength();
			if (docLength <= 128*1024) {
				IRegion all= new Region(0, docLength);
				fPresentation= createPresentation(all, document);
			} else {
				return createPresentation(region, document);
			}
		}
		fPresentation.setResultWindow(region);
		return fPresentation;
	}

	protected void handleSourceTagsChanged() {
		invalidateTextPresentation();
	}

	private void invalidateTextPresentation() {
		if (fPresentation != null) {
			fPresentation= null;
			if (fViewer != null) {
				Display display= fViewer.getTextWidget().getDisplay();
				if (display.getThread() != Thread.currentThread()) {
					display.asyncExec(new Runnable() {
						@Override
						public void run() {
							if (fViewer != null) {
								fViewer.invalidateTextPresentation();
							}
						}});
				} else {
					fViewer.invalidateTextPresentation();
				}
			}
		}
	}

	private ISourceTagProvider createSourceTagProvider(IStorage storage) {
		ITranslationUnit tUnit= null;
		if (storage instanceof IFile) {
			tUnit= (ITranslationUnit) CoreModel.getDefault().create((IFile)storage);
		} else if (storage instanceof IFileState) {
			ICModel cModel= CoreModel.getDefault().getCModel();
			ICProject[] cProjects;
			try {
				cProjects = cModel.getCProjects();
				if (cProjects.length > 0) {
					tUnit= CoreModel.getDefault().createTranslationUnitFrom(cProjects[0], storage.getFullPath());
				}
			} catch (CModelException e) {
			}
		} else {
			IEditorInput input= CDTUITools.getEditorInputForLocation(storage.getFullPath(), null);
			if (input != null) {
				tUnit= (ITranslationUnit) input.getAdapter(ITranslationUnit.class);
			}
		}
		if (tUnit != null) {
			return new CSourceTagProvider(tUnit);
		}
		return null;
	}

	/*
	 * @see org.eclipse.jface.util.IPropertyChangeListener#propertyChange(org.eclipse.jface.util.PropertyChangeEvent)
	 */
	@Override
	public void propertyChange(PropertyChangeEvent event) {
		if (fSourceViewerConfiguration.affectsBehavior(event)) {
			fSourceViewerConfiguration.handlePropertyChangeEvent(event);
			invalidateTextPresentation();
		} else if (fDamagerRepairer.affectsBahvior(event)) {
			fDamagerRepairer.handlePropertyChangeEvent(event);
			invalidateTextPresentation();
		}
	}

}
