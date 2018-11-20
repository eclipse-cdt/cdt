/*******************************************************************************
 * Copyright (c) 2008 Symbian Software Systems and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 * Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.cdt.ui.text.doctools;

import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.cdt.ui.text.ICTokenScanner;
import org.eclipse.cdt.ui.text.ITokenStoreFactory;
import org.eclipse.cdt.ui.text.contentassist.ICompletionProposalComputer;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITextDoubleClickStrategy;

/**
 * An IDocCommentViewerConfiguration aggregates a collection of editor tools that can be contributed to
 * the CDT editor. The tools will be active for CEditor partitions of type {@link ICPartitions#C_MULTI_LINE_DOC_COMMENT} or
 * {@link ICPartitions#C_SINGLE_LINE_DOC_COMMENT} when the {@link IDocCommentOwner} this instance originated from is active.
 *
 * @see org.eclipse.jface.text.source.SourceViewerConfiguration (in analogy to)
 * @since 5.0
 */
public interface IDocCommentViewerConfiguration {
	/**
	 * @param doc the document to examine
	 * @param offset the offset of the start of the region (inclusive)
	 * @param length the length of the region to examine
	 * @return whether the region specified is a documentation comment handled by this viewer configuration
	 */
	boolean isDocumentationComment(IDocument doc, int offset, int length);

	/**
	 * @return a ICTokenScanner for tokenising/coloring the appropriate comment region. May return null.
	 */
	ICTokenScanner createCommentScanner(ITokenStoreFactory tokenStoreFactory);

	/**
	 * @return an auto edit strategy suitable for the appropriate comment region. May return null
	 * in the case where no auto-edit-strategy is required.
	 */
	IAutoEditStrategy createAutoEditStrategy();

	/**
	 * @return a double click strategy suitable for the associated comment-region. May return null in
	 * the case where no double-click-strategy is required.
	 */
	ITextDoubleClickStrategy createDoubleClickStrategy();

	/**
	 * @return a completion proposal computer suitable for the associated comment-region. May return null in
	 * the case where no proposal-computer is required.
	 */
	ICompletionProposalComputer createProposalComputer();

	/**
	 * @return a {@link IDocCommentDictionary} suitable for spell-checking. May return null
	 * in the case where no additional dictionary is required.
	 */
	IDocCommentDictionary getSpellingDictionary();
}
