/*******************************************************************************
 *  Copyright (c) 2005, 2017 IBM Corporation and others.
 *
 *  This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License 2.0
 *  which accompanies this distribution, and is available at
 *  https://www.eclipse.org/legal/epl-2.0/
 *
 *  SPDX-License-Identifier: EPL-2.0
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

package org.eclipse.cdt.lsp.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ICLanguageKeywords;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.internal.ui.editor.CEditor.BracketInserter;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightedPosition;
import org.eclipse.cdt.internal.ui.text.CCodeScanner;
import org.eclipse.cdt.internal.ui.text.CCommentScanner;
import org.eclipse.cdt.internal.ui.text.CPreprocessorScanner;
import org.eclipse.cdt.internal.ui.text.CPresentationReconciler;
import org.eclipse.cdt.internal.ui.text.PartitionDamager;
import org.eclipse.cdt.internal.ui.text.SingleTokenCScanner;
import org.eclipse.cdt.internal.ui.text.TokenStore;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.ILanguageUI;
import org.eclipse.cdt.ui.text.AbstractCScanner;
import org.eclipse.cdt.ui.text.ICColorConstants;
import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.ICTokenScanner;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DefaultPositionUpdater;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextInputListener;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.RuleBasedScanner;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;

/**
 * Hack-ish reconciler to get some colors in the generic editor using the C/C++
 * Server.
 */
public class PresentationReconcilerCPP extends CPresentationReconciler {

	private CCommentScanner fSinglelineCommentScanner;
	private CCommentScanner fMultilineCommentScanner;
	private SingleTokenCScanner fStringScanner;
	private AbstractCScanner fCodeScanner;
	private LineBackgroundListenerCPP fLineBackgroundListener = new LineBackgroundListenerCPP();
	private ITextViewer textViewer;
	private TextInputListenerCPP textInputListener;
	private BracketInserter fBracketInserter;
	private DefaultPositionUpdater semanticHighlightingPositionUpdater;

	public static final String SEMANTIC_HIGHLIGHTING_POSITION_CATEGORY = "org.eclipse.cdt.lsp.core.semanticHighlight"; //$NON-NLS-1$
	public static final String INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY = "org.eclipse.cdt.lsp.core.inactiveCodeHighlight"; //$NON-NLS-1$

	// A set containing all active objects of PresentationReconcilerCPP.
	public static Set<PresentationReconcilerCPP> presentationReconcilers = ConcurrentHashMap.newKeySet();

	protected ITokenStoreFactory getTokenStoreFactory() {
		return propertyColorNames -> new TokenStore(CUIPlugin.getDefault().getTextTools().getColorManager(),
				CUIPlugin.getDefault().getCombinedPreferenceStore(), propertyColorNames);
	}

	protected ILanguage getLanguage() {
		// fallback
		return GPPLanguage.getDefault();
	}

	protected RuleBasedScanner getCodeScanner(ILanguage language) {
		if (fCodeScanner != null) {
			return fCodeScanner;
		}
		RuleBasedScanner scanner = null;

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
			fCodeScanner = (AbstractCScanner) scanner;
		}
		return scanner;
	}

	public PresentationReconcilerCPP() {
		fStringScanner = new SingleTokenCScanner(getTokenStoreFactory(), ICColorConstants.C_STRING);
		fMultilineCommentScanner = new CCommentScanner(getTokenStoreFactory(), ICColorConstants.C_MULTI_LINE_COMMENT);
		fSinglelineCommentScanner = new CCommentScanner(getTokenStoreFactory(), ICColorConstants.C_SINGLE_LINE_COMMENT);

		setDocumentPartitioning(CUIPlugin.getDefault().getTextTools().getDocumentPartitioning());

		ILanguage language = getLanguage();
		RuleBasedScanner scanner = getCodeScanner(language);

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(scanner);

		setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(fSinglelineCommentScanner);
		setDamager(dr, ICPartitions.C_SINGLE_LINE_COMMENT);
		setRepairer(dr, ICPartitions.C_SINGLE_LINE_COMMENT);

		dr = new DefaultDamagerRepairer(fMultilineCommentScanner);
		setDamager(dr, ICPartitions.C_MULTI_LINE_COMMENT);
		setRepairer(dr, ICPartitions.C_MULTI_LINE_COMMENT);

		ICTokenScanner docCommentSingleScanner = getSinglelineDocCommentScanner(null);
		if (docCommentSingleScanner != null) {
			dr = new DefaultDamagerRepairer(docCommentSingleScanner);
			setDamager(dr, ICPartitions.C_SINGLE_LINE_DOC_COMMENT);
			setRepairer(dr, ICPartitions.C_SINGLE_LINE_DOC_COMMENT);
		}

		ICTokenScanner docCommentMultiScanner = getMultilineDocCommentScanner(null);
		if (docCommentMultiScanner != null) {
			dr = new DefaultDamagerRepairer(docCommentMultiScanner);
			setDamager(dr, ICPartitions.C_MULTI_LINE_DOC_COMMENT);
			setRepairer(dr, ICPartitions.C_MULTI_LINE_DOC_COMMENT);
		}

		dr = new DefaultDamagerRepairer(fStringScanner);
		setDamager(dr, ICPartitions.C_STRING);
		setRepairer(dr, ICPartitions.C_STRING);

		dr = new DefaultDamagerRepairer(fStringScanner);
		setDamager(dr, ICPartitions.C_CHARACTER);
		setRepairer(dr, ICPartitions.C_CHARACTER);

		dr = new DefaultDamagerRepairer(getPreprocessorScanner(language));
		setDamager(new PartitionDamager(), ICPartitions.C_PREPROCESSOR);
		setRepairer(dr, ICPartitions.C_PREPROCESSOR);
	}

	@Override
	protected TextPresentation createPresentation(IRegion damage, IDocument document) {
		TextPresentation presentation = super.createPresentation(damage, document);

		IDocument doc = textViewer.getDocument();
		URI uri = Server2ClientProtocolExtension.getUri(doc);

		if (uri == null) {
			return presentation;
		}

		Position[] returnedPositions = null;
		List<StyleRange> styleRanges = new ArrayList<>();
		HighlightedPosition[] highlightedPositions;

		/*
		 * Adding Semantic Highlighting Position Category to so that we don't get a
		 * BadPositionCategoryException if this method is called before setupDocument()
		 * could the add new position category.
		 */
		addSemanticHighlightPositionCategory(doc);

		try {
			returnedPositions = doc.getPositions(SEMANTIC_HIGHLIGHTING_POSITION_CATEGORY);
		} catch (BadPositionCategoryException e) {
			Activator.log(e);
		}

		if (returnedPositions == null) {
			return presentation;
		}

		highlightedPositions = Arrays.copyOf(returnedPositions, returnedPositions.length, HighlightedPosition[].class);
		int damageStartOffset = damage.getOffset();
		int damageEndOffset = damageStartOffset + damage.getLength();

		for (HighlightedPosition eachPosition : highlightedPositions) {
			// Find each position that resides in or overlaps the damage region and create StyleRange for it.
			if ((eachPosition.getOffset() + eachPosition.getLength()) >= damageStartOffset
					&& eachPosition.getOffset() < damageEndOffset) {
				StyleRange range = eachPosition.createStyleRange();
				styleRanges.add(range);
			}
		}

		StyleRange[] styleRangesArray = new StyleRange[styleRanges.size()];
		styleRangesArray = styleRanges.toArray(styleRangesArray);
		presentation.replaceStyleRanges(styleRangesArray);
		return presentation;
	}

	/**
	 * Returns the C multi-line doc comment scanner for this configuration.
	 *
	 * @return the C multi-line doc comment scanner
	 */
	protected ICTokenScanner getMultilineDocCommentScanner(IResource resource) {
		if (fMultilineDocCommentScanner == null) {
			if (resource == null) {
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
			// IDocCommentViewerConfiguration owner= DocCommentOwnerManager.getInstance().getCommentOwner(resource).getMultilineConfiguration();
			// fMultilineDocCommentScanner= owner.createCommentScanner(getTokenStoreFactory());
			if (fMultilineDocCommentScanner == null) {
				// fallback: normal comment highlighting
				fMultilineDocCommentScanner = fMultilineCommentScanner;
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
				resource = ResourcesPlugin.getWorkspace().getRoot();
			}
			// IDocCommentViewerConfiguration owner= DocCommentOwnerManager.getInstance().getCommentOwner(resource).getSinglelineConfiguration();
			// fSinglelineDocCommentScanner= owner.createCommentScanner(getTokenStoreFactory());
			if (fSinglelineDocCommentScanner == null) {
				// fallback: normal comment highlighting
				fSinglelineDocCommentScanner = fSinglelineCommentScanner;
			}
		}
		return fSinglelineDocCommentScanner;
	}

	protected AbstractCScanner fPreprocessorScanner;

	protected RuleBasedScanner getPreprocessorScanner(ILanguage language) {
		if (fPreprocessorScanner != null) {
			return fPreprocessorScanner;
		}
		AbstractCScanner scanner = null;
		ICLanguageKeywords keywords = language == null ? null
				: (ICLanguageKeywords) language.getAdapter(ICLanguageKeywords.class);
		if (keywords != null) {
			scanner = new CPreprocessorScanner(getTokenStoreFactory(), keywords);
		}
		if (scanner == null) {
			keywords = GPPLanguage.getDefault().getAdapter(ICLanguageKeywords.class);
			scanner = new CPreprocessorScanner(getTokenStoreFactory(), keywords);
		}
		fPreprocessorScanner = scanner;
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

	private void addSemanticHighlightPositionCategory(IDocument document) {
		if (!document.containsPositionCategory(SEMANTIC_HIGHLIGHTING_POSITION_CATEGORY)) {
			document.addPositionCategory(SEMANTIC_HIGHLIGHTING_POSITION_CATEGORY);
			semanticHighlightingPositionUpdater = new DefaultPositionUpdater(SEMANTIC_HIGHLIGHTING_POSITION_CATEGORY);
			document.addPositionUpdater(semanticHighlightingPositionUpdater);
		}
	}

	private void addInactiveCodeHighlightingCategory(IDocument document) {
		if (!document.containsPositionCategory(INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY)) {
			document.addPositionCategory(INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY);
			DefaultPositionUpdater inactiveCodeHighlightingPositionUpdater = new DefaultPositionUpdater(
					INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY);
			document.addPositionUpdater(inactiveCodeHighlightingPositionUpdater);
		}
	}

	public void setupDocument(IDocument newDocument) {
		if (newDocument != null) {
			fLineBackgroundListener.setCurrentDocument(newDocument);

			// Adding Semantic Highlighting Position Category and a DefaultPositionUpdater to the document.
			addSemanticHighlightPositionCategory(newDocument);

			// Adding Inactive Code Highlighting Position Category and a DefaultPositionUpdater to the document.
			addInactiveCodeHighlightingCategory(newDocument);
		}
	}

	public ITextViewer getTextViewer() {
		return textViewer;
	}

	public DefaultPositionUpdater getSemanticHighlightingPositionUpdater() {
		return semanticHighlightingPositionUpdater;
	}

	@Override
	public void install(ITextViewer viewer) {
		super.install(viewer);
		this.textViewer = viewer;
		textInputListener = new TextInputListenerCPP();
		viewer.addTextInputListener(textInputListener);
		IDocument document = viewer.getDocument();
		if (document != null) {
			textInputListener.inputDocumentChanged(null, document);
		}
		StyledText textWidget = textViewer.getTextWidget();
		textWidget.addLineBackgroundListener(fLineBackgroundListener);
		presentationReconcilers.add(this);

		// Using asyncExec() to make sure that by the time Runnable runs,
		// the Editor is active and we don't get a NPE.
		Display.getDefault().asyncExec(() -> {
			// To provide bracket auto-completion support of CEditor in Generic Editor of LSP4E-CPP.
			TextEditor editor = (TextEditor) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.getActiveEditor();
			fBracketInserter = new BracketInserter(editor, true);
			fBracketInserter.setSourceViewer((SourceViewer) textViewer);
			((TextViewer) textViewer).prependVerifyKeyListener(fBracketInserter);
		});
	}

	@Override
	public void uninstall() {
		super.uninstall();
		textViewer.getTextWidget().removeLineBackgroundListener(fLineBackgroundListener);
		textViewer.removeTextInputListener(textInputListener);
		((TextViewer) textViewer).removeVerifyKeyListener(fBracketInserter);
		presentationReconcilers.remove(this);
	}
}
