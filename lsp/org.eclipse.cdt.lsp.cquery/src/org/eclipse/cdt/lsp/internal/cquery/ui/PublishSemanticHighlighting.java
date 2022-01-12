/*******************************************************************************
 * Copyright (c) 2018, 2020 Manish Khurana, Nathan Ridge and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.cquery.ui;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightedPosition;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightingStyle;
import org.eclipse.cdt.lsp.internal.core.workspace.ResolveDocumentUri;
import org.eclipse.cdt.lsp.internal.cquery.CquerySemanticHighlights;
import org.eclipse.cdt.lsp.internal.cquery.HighlightSymbol;
import org.eclipse.cdt.lsp.internal.ui.text.PresentationReconcilerCPP;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.lsp4j.Range;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

//FIXME: needs more work
@SuppressWarnings("restriction")
public class PublishSemanticHighlighting implements Consumer<CquerySemanticHighlights> {

	private final ResolveDocumentUri uri;

	public PublishSemanticHighlighting() {
		this.uri = new ResolveDocumentUri();
	}

	@Override
	public void accept(CquerySemanticHighlights highlights) {
		URI uriReceived = highlights.getUri();
		// List of PresentationReconcilerCPP objects attached with same C++ source file.
		//FIXME: AF: extract the retrieval of this list to a separate method
		List<PresentationReconcilerCPP> matchingReconcilers = new ArrayList<>();

		for (PresentationReconcilerCPP eachReconciler : PresentationReconcilerCPP.presentationReconcilers) {
			IDocument currentReconcilerDoc = eachReconciler.getTextViewer().getDocument();
			Optional<URI> currentReconcilerUri = uri.apply(currentReconcilerDoc);
			if (!currentReconcilerUri.isPresent()) {
				continue;
			}

			if (uriReceived.equals(currentReconcilerUri.get())) {
				matchingReconcilers.add(eachReconciler);
			}
		}

		if (matchingReconcilers.size() == 0) {
			return;
		}

		// Using only first object of matchingReconcilers because all reconciler objects share same document object.
		IDocument doc = matchingReconcilers.get(0).getTextViewer().getDocument();
		IPreferenceStore store = CUIPlugin.getDefault().getPreferenceStore();

		// Removing semantic highlighting position category and old positions from document.
		try {
			doc.removePositionCategory(PresentationReconcilerCPP.SEMANTIC_HIGHLIGHTING_POSITION_CATEGORY);
		} catch (BadPositionCategoryException e) {
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
		// Again add Semantic Highlighting Position Category to the document.
		doc.addPositionCategory(PresentationReconcilerCPP.SEMANTIC_HIGHLIGHTING_POSITION_CATEGORY);

		for (HighlightSymbol highlight : highlights.getSymbols()) {

			String highlightingName = HighlightingNames.getHighlightingName(highlight.getKind(),
					highlight.getParentKind(), highlight.getStorage(), highlight.getRole());
			String colorKey = PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + highlightingName
					+ PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_COLOR_SUFFIX;

			boolean isEnabled = store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX
					+ highlightingName + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ENABLED_SUFFIX);
			boolean isBold = store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX + highlightingName
					+ PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_BOLD_SUFFIX);
			boolean isItalic = store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX
					+ highlightingName + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_ITALIC_SUFFIX);
			boolean isUnderline = store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX
					+ highlightingName + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_UNDERLINE_SUFFIX);
			boolean isStrikethrough = store.getBoolean(PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_PREFIX
					+ highlightingName + PreferenceConstants.EDITOR_SEMANTIC_HIGHLIGHTING_STRIKETHROUGH_SUFFIX);

			// TODO: Use IColorManager to cache Color objects so that only one object per color is created.
			Color color = new Color(Display.getCurrent(),
					PreferenceConverter.getColor(CUIPlugin.getDefault().getPreferenceStore(), colorKey));

			List<Range> ranges = highlight.getRanges();
			for (Range range : ranges) {

				int offset = 0, length = 0;
				try {
					offset = doc.getLineOffset(range.getStart().getLine()) + range.getStart().getCharacter();
					length = doc.getLineOffset(range.getEnd().getLine()) + range.getEnd().getCharacter() - offset;
				} catch (BadLocationException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}

				int textStyle = SWT.NORMAL;

				if (isBold) {
					textStyle = SWT.BOLD;
				}
				if (isItalic) {
					textStyle |= SWT.ITALIC;
				}
				if (isUnderline) {
					textStyle |= TextAttribute.UNDERLINE;
				}
				if (isStrikethrough) {
					textStyle |= TextAttribute.STRIKETHROUGH;
				}

				TextAttribute textAttribute = new TextAttribute(color, null, textStyle);
				HighlightingStyle highlightingStyle = new HighlightingStyle(textAttribute, isEnabled);
				HighlightedPosition highlightedPosition = new HighlightedPosition(offset, length, highlightingStyle,
						matchingReconcilers.get(0).getSemanticHighlightingPositionUpdater());
				try {
					doc.addPosition(PresentationReconcilerCPP.SEMANTIC_HIGHLIGHTING_POSITION_CATEGORY,
							highlightedPosition);
				} catch (BadLocationException | BadPositionCategoryException e) {
					Platform.getLog(getClass()).error(e.getMessage(), e);
				}
			}
		}

		Display.getDefault().asyncExec(() -> {
			for (PresentationReconcilerCPP eachReconciler : matchingReconcilers) {
				TextPresentation presentation = eachReconciler.createPresentation(new Region(0, doc.getLength()), doc);
				eachReconciler.getTextViewer().changeTextPresentation(presentation, false);
			}
		});
	}

}
