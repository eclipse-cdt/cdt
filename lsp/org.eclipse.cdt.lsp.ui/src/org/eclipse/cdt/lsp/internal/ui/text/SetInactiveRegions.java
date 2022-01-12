/*******************************************************************************
 * Copyright (c) 2018, 2020 Manish Khurana, Nathan Ridge and others.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * https://www.eclipse.org/legal/epl-2.0/.
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.cdt.lsp.internal.ui.text;

import java.net.URI;
import java.util.Collection;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

import org.eclipse.cdt.lsp.internal.core.workspace.ResolveDocumentUri;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.lsp4j.Range;

//FIXME: needs more work
public final class SetInactiveRegions implements BiConsumer<Supplier<URI>, Supplier<Collection<Range>>> {

	private final ResolveDocumentUri resolve;

	public SetInactiveRegions() {
		this.resolve = new ResolveDocumentUri();
	}

	@Override
	public void accept(Supplier<URI> uri, Supplier<Collection<Range>> ranges) {
		URI uriReceived = uri.get();
		Collection<Range> inactiveRegions = ranges.get();
		//FIXME: AF: extract the retrieval of this document to a separate method
		IDocument doc = null;
		// To get the document for the received URI.
		for (PresentationReconcilerCPP eachReconciler : PresentationReconcilerCPP.presentationReconcilers) {
			IDocument currentReconcilerDoc = eachReconciler.getTextViewer().getDocument();
			Optional<URI> currentReconcilerUri = resolve.apply(currentReconcilerDoc);
			if (!currentReconcilerUri.isPresent()) {
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
			Platform.getLog(getClass()).error(e.getMessage(), e);
		}
		// Again add Inactive Code Position Category to the document.
		doc.addPositionCategory(PresentationReconcilerCPP.INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY);

		for (Range region : inactiveRegions) {
			int offset = 0, length = 0;
			try {
				offset = doc.getLineOffset(region.getStart().getLine());
				length = doc.getLineOffset(region.getEnd().getLine()) - offset;
			} catch (BadLocationException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}

			Position inactivePosition = new Position(offset, length);
			try {
				doc.addPosition(PresentationReconcilerCPP.INACTIVE_CODE_HIGHLIGHTING_POSITION_CATEGORY,
						inactivePosition);
			} catch (BadLocationException | BadPositionCategoryException e) {
				Platform.getLog(getClass()).error(e.getMessage(), e);
			}
		}
	}

}
