/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Anton Leherbauer (Wind River Systems)
 *     Andrew Gvozdev - http://bugs.eclipse.org/236160
 *******************************************************************************/
package org.eclipse.cdt.internal.ui.actions;

import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;

import org.eclipse.cdt.ui.text.ICPartitions;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPartitioningException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that encloses the editor's current selection with C block comment terminators
 * (<code>&#47;&#42;</code> and <code>&#42;&#47;</code>).
 *
 * @since 3.0
 */
public class AddBlockCommentAction extends BlockCommentAction {
	/**
	 * Creates a new instance.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 */
	public AddBlockCommentAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	@Override
	protected void runInternal(ITextSelection selection, IDocumentExtension3 docExtension, Edit.EditFactory factory)
			throws BadLocationException, BadPartitioningException {

		if (!(docExtension instanceof IDocument))
			return;

		List<Edit> edits = new LinkedList<>();

		ITypedRegion firstPartition = docExtension.getPartition(ICPartitions.C_PARTITIONING, selection.getOffset(),
				false);
		ITypedRegion lastPartition = docExtension.getPartition(ICPartitions.C_PARTITIONING,
				selection.getOffset() + selection.getLength() - 1, false);

		int commentAreaStart = selection.getOffset();
		int commentAreaEnd = selection.getOffset() + selection.getLength();
		// Include special partitions fully in the comment area
		if (isSpecialPartition(firstPartition.getType())) {
			commentAreaStart = firstPartition.getOffset();
		}
		if (isSpecialPartition(lastPartition.getType())) {
			commentAreaEnd = lastPartition.getOffset() + lastPartition.getLength();
		}
		Region estimatedCommentArea = new Region(commentAreaStart, commentAreaEnd - commentAreaStart);

		Region commentArea = handleEnclosingPartitions(estimatedCommentArea, lastPartition, (IDocument) docExtension,
				factory, edits);

		handleInteriorPartition(commentArea, firstPartition, docExtension, factory, edits);

		executeEdits(edits);
	}

	/**
	 * Add enclosing comment tags for the whole area to be commented
	 *
	 * @param commentArea initial comment area which can be adjusted
	 * @param lastPartition last partition
	 * @param doc document
	 * @param factory Edit factory
	 * @param edits List of edits to update the document
	 * @return new possibly adjusted comment area
	 * @throws BadLocationException
	 */
	private Region handleEnclosingPartitions(Region commentArea, ITypedRegion lastPartition, IDocument doc,
			Edit.EditFactory factory, List<Edit> edits) throws BadLocationException {

		int commentAreaStart = commentArea.getOffset();
		int commentAreaEnd = commentArea.getOffset() + commentArea.getLength();

		String commentStartTag = getCommentStart(); // "/*"
		String commentEndTag = getCommentEnd(); // "*/"

		String startLineEOL = doc.getLineDelimiter(doc.getLineOfOffset(commentAreaStart));
		if (startLineEOL == null)
			startLineEOL = ""; //$NON-NLS-1$
		String endLineEOL = doc.getLineDelimiter(doc.getLineOfOffset(commentAreaEnd - 1));
		if (endLineEOL == null)
			endLineEOL = ""; //$NON-NLS-1$

		boolean isLeftEol = commentAreaStart < startLineEOL.length()
				|| doc.get(commentAreaStart - startLineEOL.length(), startLineEOL.length()).equals(startLineEOL);
		boolean isRightEol = doc.get(commentAreaEnd - endLineEOL.length(), endLineEOL.length()).equals(endLineEOL);

		if (isLeftEol && isRightEol) {
			// Block of full lines found
			int areaStartLine = doc.getLineOfOffset(commentAreaStart + startLineEOL.length());
			int areaEndLine = doc.getLineOfOffset(commentAreaEnd - endLineEOL.length());
			if (areaStartLine != areaEndLine) {
				// If multiple full lines arrange inserting comment tags on their own lines
				commentStartTag = getCommentStart() + startLineEOL;
				commentEndTag = getCommentEnd() + endLineEOL;
			} else {
				// If one full line insert end comment tag on the same line (before the EOL)
				commentAreaEnd = commentAreaEnd - endLineEOL.length();
			}
		} else {
			if (lastPartition.getType() == ICPartitions.C_SINGLE_LINE_COMMENT
					|| lastPartition.getType() == ICPartitions.C_SINGLE_LINE_DOC_COMMENT) {
				// C++ comments "//" partition ends with EOL, insert end comment tag before it
				// on the same line, so we get something like /*// text*/
				commentAreaEnd = commentAreaEnd - endLineEOL.length();
			}
		}

		edits.add(factory.createEdit(commentAreaStart, 0, commentStartTag));
		edits.add(factory.createEdit(commentAreaEnd, 0, commentEndTag));

		return new Region(commentAreaStart, commentAreaEnd - commentAreaStart);
	}

	/**
	 * Make all inside partitions join in one comment, in particular remove
	 * all enclosing comment tokens of the inside partitions.
	 *
	 * @param commentArea comment area region
	 * @param partition first partition
	 * @param docExtension document
	 * @param factory EditFactory
	 * @param List of edits to update the document
	 * @throws BadLocationException
	 * @throws BadPartitioningException
	 */
	private void handleInteriorPartition(IRegion commentArea, ITypedRegion partition, IDocumentExtension3 docExtension,
			Edit.EditFactory factory, List<Edit> edits) throws BadLocationException, BadPartitioningException {

		int commentAreaEnd = commentArea.getOffset() + commentArea.getLength();
		int prevPartitionEnd = -1;
		int partitionEnd = partition.getOffset() + partition.getLength();

		final int startCommentTokenLength = getCommentStart().length();
		final int endCommentTokenLength = getCommentEnd().length();

		while (partitionEnd <= commentAreaEnd) {
			if (partition.getType() == ICPartitions.C_MULTI_LINE_COMMENT
					|| partition.getType() == ICPartitions.C_MULTI_LINE_DOC_COMMENT) {
				// already in a comment - remove start/end tokens
				edits.add(factory.createEdit(partition.getOffset(), startCommentTokenLength, "")); //$NON-NLS-1$
				edits.add(factory.createEdit(partitionEnd - endCommentTokenLength, endCommentTokenLength, "")); //$NON-NLS-1$
			}
			// advance to next partition
			prevPartitionEnd = partitionEnd;
			partition = docExtension.getPartition(ICPartitions.C_PARTITIONING, partitionEnd, false);
			partitionEnd = partition.getOffset() + partition.getLength();

			// break the loop if we get stuck and no advance was made
			if (partitionEnd <= prevPartitionEnd)
				break;
		}
	}

	/**
	 * Returns whether <code>partType</code> is special, i.e. a <code>String</code>,
	 * <code>Character</code>, or <code>Comment</code> partition.
	 *
	 * @param partType the partition type to check
	 * @return <code>true</code> if <code>partType</code> is special, <code>false</code> otherwise
	 */
	private boolean isSpecialPartition(String partType) {
		return partType != IDocument.DEFAULT_CONTENT_TYPE;
	}

	@Override
	protected boolean isValidSelection(ITextSelection selection) {
		return selection != null && !selection.isEmpty() && selection.getLength() > 0;
	}

}
