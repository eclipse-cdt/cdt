/*******************************************************************************
 * Copyright (c) 2018-2019 Manish Khurana, Nathan Ridge and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Manish Khurana <mkmanishkhurana98@gmail.com> - initial API and implementation
 *     Nathan Ridge <zeratul976@hotmail.com> - initial API and implementation
 *     Alexander Fedorov <alexander.fedorov@arsysop.ru> - Bug 558516
 *******************************************************************************/

package org.eclipse.cdt.lsp.core;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.cdt.cquery.CqueryInactiveRegions;
import org.eclipse.cdt.cquery.CquerySemanticHighlights;
import org.eclipse.cdt.cquery.HighlightSymbol;
import org.eclipse.cdt.cquery.IndexingProgressStats;
import org.eclipse.cdt.internal.cquery.CqueryMessages;
import org.eclipse.cdt.internal.cquery.ui.HighlightingNames;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightedPosition;
import org.eclipse.cdt.internal.ui.editor.SemanticHighlightingManager.HighlightingStyle;
import org.eclipse.cdt.lsp.internal.core.ShowStatus;
import org.eclipse.cdt.lsp.internal.text.ResolveDocumentUri;
import org.eclipse.cdt.lsp.internal.ui.StatusLineMessage;
import org.eclipse.cdt.ui.CUIPlugin;
import org.eclipse.cdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.lsp4e.LanguageClientImpl;
import org.eclipse.lsp4j.Range;
import org.eclipse.lsp4j.jsonrpc.services.JsonNotification;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

//FIXME: AF: currently this extension is cquery-specific and it should be contributed from cquery-specific part
@SuppressWarnings("restriction")
public class Server2ClientProtocolExtension extends LanguageClientImpl {

	private final ResolveDocumentUri uri;
	private final ShowStatus progress;

	public Server2ClientProtocolExtension() {
		this.uri = new ResolveDocumentUri();
		this.progress = new ShowStatus(() -> CqueryMessages.Server2ClientProtocolExtension_cquery_name,
				new StatusLineMessage());
	}

	@JsonNotification("$cquery/progress")
	public final void indexingProgress(IndexingProgressStats stats) {
		progress.accept(stats::getTotalJobs);
	}

	@JsonNotification("$cquery/setInactiveRegions")
	public final void setInactiveRegions(CqueryInactiveRegions regions) {
		URI uriReceived = regions.getUri();
		List<Range> inactiveRegions = regions.getInactiveRegions();
		//FIXME: AF: extract the retrieval of this document to a separate method
		IDocument doc = null;
		// To get the document for the received URI.
		for (PresentationReconcilerCPP eachReconciler : PresentationReconcilerCPP.presentationReconcilers) {
			IDocument currentReconcilerDoc = eachReconciler.getTextViewer().getDocument();
			Optional<URI> currentReconcilerUri = uri.apply(currentReconcilerDoc);

			if (currentReconcilerUri.isEmpty()) {
				continue;
			}

			if (uriReceived.equals(currentReconcilerUri.get())) {
				doc = currentReconcilerDoc;
				break;
			}
		}

		if (doc == null) {
			return;
		}

		// Removing inactive code highlighting position category and old positions from document.
		try {
			doc.removePositionCategory(PresentationReconcilerCPP.INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY);
		} catch (BadPositionCategoryException e) {
			Activator.log(e);
		}
		// Again add Inactive Code Position Category to the document.
		doc.addPositionCategory(PresentationReconcilerCPP.INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY);

		for (Range region : inactiveRegions) {
			int offset = 0, length = 0;
			try {
				offset = doc.getLineOffset(region.getStart().getLine());
				length = doc.getLineOffset(region.getEnd().getLine()) - offset;
			} catch (BadLocationException e) {
				Activator.log(e);
			}

			Position inactivePosition = new Position(offset, length);
			try {
				doc.addPosition(PresentationReconcilerCPP.INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY,
						inactivePosition);
			} catch (BadLocationException | BadPositionCategoryException e) {
				Activator.log(e);
			}
		}
	}

	@JsonNotification("$cquery/publishSemanticHighlighting")
	public final void semanticHighlights(CquerySemanticHighlights highlights) {
		URI uriReceived = highlights.getUri();

		// List of PresentationReconcilerCPP objects attached with same C++ source file.
		//FIXME: AF: extract the retrieval of this list to a separate method
		List<PresentationReconcilerCPP> matchingReconcilers = new ArrayList<>();

		for (PresentationReconcilerCPP eachReconciler : PresentationReconcilerCPP.presentationReconcilers) {
			IDocument currentReconcilerDoc = eachReconciler.getTextViewer().getDocument();
			Optional<URI> currentReconcilerUri = uri.apply(currentReconcilerDoc);
			if (currentReconcilerUri.isEmpty()) {
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
			Activator.log(e);
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
					Activator.log(e);
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
					Activator.log(e);
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
