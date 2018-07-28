/*******************************************************************************
 *  Copyright (c) 2005, 2017 IBM Corporation and others.
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
 *     Marc-Andre Laperle (Ericsson) - Mostly copied from CSourceViewerConfiguration
 *     Nathan Ridge
 *     Manish Khurana
 *******************************************************************************/

package org.eclipse.lsp4e.cpp.language;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightings;
import org.eclipse.cdt.internal.ui.text.CCodeScanner;
import org.eclipse.cdt.internal.ui.text.CCommentScanner;
import org.eclipse.cdt.internal.ui.text.CPreprocessorScanner;
import org.eclipse.cdt.internal.ui.text.CPresentationReconciler;
import org.eclipse.cdt.internal.ui.text.FastCPartitioner;
import org.eclipse.cdt.internal.ui.text.PartitionDamager;
import org.eclipse.cdt.internal.ui.text.SingleTokenCScanner;
import org.eclipse.cdt.internal.ui.text.TokenStore;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ILanguageUI;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.cdt.ui.text.AbstractCScanner;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.ICTokenScanner;
import org.eclipse.cdt.ui.text.ITokenStore;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.core.internal.resources.File;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.lsp4e.LSPEclipseUtils;
import org.eclipse.lsp4e.cpp.language.cquery.CquerySemanticHighlights;
import org.eclipse.lsp4e.cpp.language.cquery.HighlightSymbol;
import org.eclipse.lsp4e.cpp.language.cquery.StorageClass;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.SymbolKind;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * Hack-ish reconciler to get some colors in the generic editor using the C/C++
 * Server.
 */
@SuppressWarnings("restriction")
public class PresentationReconcilerCPP extends CPresentationReconciler {

	private CCommentScanner fSinglelineCommentScanner;
	private CCommentScanner fMultilineCommentScanner;
	private SingleTokenCScanner fStringScanner;
	private AbstractCScanner fCodeScanner;
	private boolean fSettingPartitioner = false;
	private CqueryLineBackgroundListener fLineBackgroundListener = new CqueryLineBackgroundListener();
	private ITextViewer textViewer;
	private TextInputListenerCPP textInputListener;
	protected ITokenStoreFactory getTokenStoreFactory() {
		return new ITokenStoreFactory() {
			@Override
			public ITokenStore createTokenStore(String[] propertyColorNames) {
				return new TokenStore(CUIPlugin.getDefault().getTextTools().getColorManager(), CUIPlugin.getDefault().getCombinedPreferenceStore(), propertyColorNames);
			}
		};
	}

	protected ILanguage getLanguage() {
		// fallback
		return GPPLanguage.getDefault();
	}

	protected RuleBasedScanner getCodeScanner(ILanguage language) {
		if (fCodeScanner != null) {
			return fCodeScanner;
		}
		RuleBasedScanner scanner= null;

		if (language != null) {
			ICLanguageKeywords keywords = language.getAdapter(ICLanguageKeywords.class);
			if (keywords != null) {
				scanner = new CCodeScanner(getTokenStoreFactory(), keywords);
			} else {
				ILanguageUI languageUI = language.getAdapter(ILanguageUI.class);
				if (languageUI != null) {
					scanner = languageUI.getCodeScanner();
				}
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

	public PresentationReconcilerCPP() {
		fStringScanner= new SingleTokenCScanner(getTokenStoreFactory(), ICColorConstants.C_STRING);
		fMultilineCommentScanner= new CCommentScanner(getTokenStoreFactory(),  ICColorConstants.C_MULTI_LINE_COMMENT);
		fSinglelineCommentScanner= new CCommentScanner(getTokenStoreFactory(),  ICColorConstants.C_SINGLE_LINE_COMMENT);

		setDocumentPartitioning(CUIPlugin.getDefault().getTextTools().getDocumentPartitioning());

		ILanguage language= getLanguage();
		RuleBasedScanner scanner = getCodeScanner(language);

		DefaultDamagerRepairer dr= new DefaultDamagerRepairer(scanner);

		setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr= new DefaultDamagerRepairer(fSinglelineCommentScanner);
		setDamager(dr, ICPartitions.C_SINGLE_LINE_COMMENT);
		setRepairer(dr, ICPartitions.C_SINGLE_LINE_COMMENT);

		dr= new DefaultDamagerRepairer(fMultilineCommentScanner);
		setDamager(dr, ICPartitions.C_MULTI_LINE_COMMENT);
		setRepairer(dr, ICPartitions.C_MULTI_LINE_COMMENT);

		ICTokenScanner docCommentSingleScanner= getSinglelineDocCommentScanner(null);
		if (docCommentSingleScanner!=null) {
			dr= new DefaultDamagerRepairer(docCommentSingleScanner);
			setDamager(dr, ICPartitions.C_SINGLE_LINE_DOC_COMMENT);
			setRepairer(dr, ICPartitions.C_SINGLE_LINE_DOC_COMMENT);
		}

		ICTokenScanner docCommentMultiScanner= getMultilineDocCommentScanner(null);
		if (docCommentMultiScanner!=null) {
			dr= new DefaultDamagerRepairer(docCommentMultiScanner);
			setDamager(dr, ICPartitions.C_MULTI_LINE_DOC_COMMENT);
			setRepairer(dr, ICPartitions.C_MULTI_LINE_DOC_COMMENT);
		}

		dr= new DefaultDamagerRepairer(fStringScanner);
		setDamager(dr, ICPartitions.C_STRING);
		setRepairer(dr, ICPartitions.C_STRING);

		dr= new DefaultDamagerRepairer(fStringScanner);
		setDamager(dr, ICPartitions.C_CHARACTER);
		setRepairer(dr, ICPartitions.C_CHARACTER);

		dr= new DefaultDamagerRepairer(getPreprocessorScanner(language));
		setDamager(new PartitionDamager(), ICPartitions.C_PREPROCESSOR);
		setRepairer(dr, ICPartitions.C_PREPROCESSOR);
	}

	@Override
	protected TextPresentation createPresentation(IRegion damage, IDocument document) {
		if (fSettingPartitioner) {
			return null;
		}
		//FIXME: Need a better place to set the partitioner
		if (!(document.getDocumentPartitioner() instanceof FastCPartitioner)) {
			// Prevent infinite recursion
			fSettingPartitioner = true;
			CUIPlugin.getDefault().getTextTools().setupCDocument(document);
			fSettingPartitioner = false;
		}
		TextPresentation createPresentation = super.createPresentation(damage, document);

		IDocument doc = textViewer.getDocument();
		File file = (File) LSPEclipseUtils.getFile(doc);

		if (file != null) {
			URI uri = LSPEclipseUtils.toUri(file);

			if (uri != null) {
				Server2ClientProtocolExtension.uriToPresentationReconcilerMapping.put(uri, this);
				StyleRange[] styleRangesArray;

				IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();

				List<StyleRange> styleRanges = new ArrayList<>();
				List<HighlightSymbol> symbols = CquerySemanticHighlights.semanticHighlightingsMap.get(uri);

				if (symbols != null) {

					for (HighlightSymbol symbol : symbols) {
						String symbolName = HighlightSymbol.semanticHighlightSymbolsMap.get(symbol.getKind().getValue());
						if (symbolName == null) {
							if (symbol.getKind().getValue() == SymbolKind.Variable.getValue()) {
								if (symbol.getParentKind().getValue() == SymbolKind.Function.getValue()
										|| symbol.getParentKind().getValue() == SymbolKind.Method.getValue()
										|| symbol.getParentKind().getValue() == SymbolKind.Constructor.getValue()) {

									symbolName = SemanticHighlightings.LOCAL_VARIABLE;
								} else {
									symbolName = SemanticHighlightings.GLOBAL_VARIABLE;
							}
							} else if (symbol.getKind().getValue() == SymbolKind.Field.getValue()) {
								if (symbol.getStorage() == StorageClass.Static) {
									symbolName = SemanticHighlightings.STATIC_FIELD;
								} else {
								symbolName = SemanticHighlightings.FIELD;
								}
							}
						}

						String colorKey = PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + symbolName + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

						boolean isBold = store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + symbolName + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX);
						boolean isItalic = store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + symbolName + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX);
						boolean isUnderline = store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + symbolName + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX);
						boolean isStrikethrough = store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + symbolName + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX);

						Color color = new Color(Display.getCurrent(), PreferenceConverter.getColor(CUIPlugin.getDefault().getPreferenceStore(), colorKey));

						int damageStartOffset = damage.getOffset();
						int damageEndOffset = damageStartOffset + damage.getLength();

						List<Range> ranges = symbol.getRanges();
						for (Range range : ranges) {
							int offset = 0, length = 0;
							try {
								offset = doc.getLineOffset(range.getStart().getLine()) + range.getStart().getCharacter();
								length = doc.getLineOffset(range.getEnd().getLine()) + range.getEnd().getCharacter() - offset;
							} catch (BadLocationException e) {
								Activator.log(e);
							}
							if ((offset + length) >= damageStartOffset && offset < damageEndOffset) {

								StyleRange styleRange = new StyleRange(offset, length, color, null);

								if(isBold) {
									styleRange.fontStyle = SWT.BOLD;
								}
								if(isItalic) {
									styleRange.fontStyle = SWT.ITALIC;
								}
								if(isUnderline) {
									styleRange.underline = true;
								}
								if(isStrikethrough) {
									styleRange.strikeout = true;
								}

								styleRanges.add(styleRange);
							}
						}
					}

					styleRangesArray = new StyleRange[styleRanges.size()];
					styleRangesArray = styleRanges.toArray(styleRangesArray);
					createPresentation.mergeStyleRanges(styleRangesArray);
				}
			}
		}
		return createPresentation;
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
//			IDocCommentViewerConfiguration owner= DocCommentOwnerManager.getInstance().getCommentOwner(resource).getMultilineConfiguration();
//			fMultilineDocCommentScanner= owner.createCommentScanner(getTokenStoreFactory());
			if (fMultilineDocCommentScanner == null) {
				// fallback: normal comment highlighting
				fMultilineDocCommentScanner= fMultilineCommentScanner;
			}
		}
		return fMultilineDocCommentScanner;
	}

	protected ICTokenScanner fMultilineDocCommentScanner;
	/**
	 * The C single-line doc comment scanner.
	 */
	protected ICTokenScanner fSinglelineDocCommentScanner;

	protected ICTokenScanner getSinglelineDocCommentScanner(IResource resource) {
		if (fSinglelineDocCommentScanner == null) {
			if (resource == null) {
				resource= ResourcesPlugin.getWorkspace().getRoot();
			}
//			IDocCommentViewerConfiguration owner= DocCommentOwnerManager.getInstance().getCommentOwner(resource).getSinglelineConfiguration();
//			fSinglelineDocCommentScanner= owner.createCommentScanner(getTokenStoreFactory());
			if (fSinglelineDocCommentScanner == null) {
				// fallback: normal comment highlighting
				fSinglelineDocCommentScanner= fSinglelineCommentScanner;
			}
		}
		return fSinglelineDocCommentScanner;
	}

	protected AbstractCScanner fPreprocessorScanner;

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
			keywords = GPPLanguage.getDefault().getAdapter(ICLanguageKeywords.class);
			scanner= new CPreprocessorScanner(getTokenStoreFactory(), keywords);
		}
		fPreprocessorScanner= scanner;
		return fPreprocessorScanner;
	}

	public class TextInputListenerCPP implements ITextInputListener {

		@Override
		public void inputDocumentChanged(IDocument oldInput, IDocument newInput) {
			setupDocument(newInput);
		}

		@Override
		public void inputDocumentAboutToBeChanged(IDocument oldInput, IDocument newInput) {
		}
	}

	public void setupDocument(IDocument newDocument) {
		if (newDocument != null) {
			fLineBackgroundListener.setCurrentDocument(newDocument);
		}
	}

	@Override
	public void install(ITextViewer viewer) {
		super.install(viewer);
		this.textViewer = viewer;
		textInputListener = new TextInputListenerCPP();
		viewer.addTextInputListener(textInputListener);
		IDocument document= viewer.getDocument();
		if (document != null) {
		    textInputListener.inputDocumentChanged(null, document);
		}
		StyledText textWidget = textViewer.getTextWidget();
		textWidget.addLineBackgroundListener(fLineBackgroundListener);
	}

	public ITextViewer getTextViewer() {
		return textViewer;
	}

	@Override
	public void uninstall() {
		super.uninstall();
		textViewer.getTextWidget().removeLineBackgroundListener(fLineBackgroundListener);
		textViewer.removeTextInputListener(textInputListener);
		Server2ClientProtocolExtension.uriToPresentationReconcilerMapping.remove(LSPEclipseUtils.toUri(LSPEclipseUtils.getFile(textViewer.getDocument())));
	}

}
