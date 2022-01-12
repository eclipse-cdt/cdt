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
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * Action that removes the enclosing comment marks from a C block comment.
 *
 * @since 3.0
 */
public class RemoveBlockCommentAction extends BlockCommentAction {

	/**
	 * Creates a new instance.
	 *
	 * @param bundle the resource bundle
	 * @param prefix a prefix to be prepended to the various resource keys
	 *   (described in <code>ResourceAction</code> constructor), or
	 *   <code>null</code> if none
	 * @param editor the text editor
	 */
	public RemoveBlockCommentAction(ResourceBundle bundle, String prefix, ITextEditor editor) {
		super(bundle, prefix, editor);
	}

	@Override
	protected void runInternal(ITextSelection selection, IDocumentExtension3 docExtension, Edit.EditFactory factory)
			throws BadPartitioningException, BadLocationException {
		if (!(docExtension instanceof IDocument))
			return;

		List<Edit> edits = new LinkedList<>();

		int partitionStart = -1;
		int partitionEnd = selection.getOffset();

		do {
			ITypedRegion partition = docExtension.getPartition(ICPartitions.C_PARTITIONING, partitionEnd, false);
			if (partition.getOffset() <= partitionStart) {
				// If we did not advance break the loop
				break;
			}
			partitionStart = partition.getOffset();
			partitionEnd = partitionStart + partition.getLength();
			if (partition.getType() == ICPartitions.C_MULTI_LINE_COMMENT
					|| partition.getType() == ICPartitions.C_MULTI_LINE_DOC_COMMENT) {
				uncommentPartition((IDocument) docExtension, factory, edits, partitionStart, partitionEnd);
			}
		} while (partitionEnd < selection.getOffset() + selection.getLength());

		executeEdits(edits);
	}

	private void uncommentPartition(IDocument doc, Edit.EditFactory factory, List<Edit> edits, int partitionStart,
			int partitionEnd) throws BadLocationException {

		int startCommentTokenLength = getCommentStart().length();
		int endCommentTokenLength = getCommentEnd().length();

		// Remove whole line (with EOL) if it contains start or end comment tag
		// and nothing else
		if (partitionStart >= 0) {
			IRegion lineRegion = doc.getLineInformationOfOffset(partitionStart);
			String lineContent = doc.get(lineRegion.getOffset(), lineRegion.getLength());
			// start comment tag '/*'
			if (lineContent.equals(getCommentStart())) {
				String eol = doc.getLineDelimiter(doc.getLineOfOffset(partitionStart));
				if (eol != null) {
					startCommentTokenLength = startCommentTokenLength + eol.length();
				}
			}
		}

		int commentContentEnd = partitionEnd - endCommentTokenLength;
		if (partitionEnd < doc.getLength()) {
			IRegion lineRegion = doc.getLineInformationOfOffset(partitionEnd);
			String lineContent = doc.get(lineRegion.getOffset(), lineRegion.getLength());
			// end comment tag '*/'
			if (lineContent.equals(getCommentEnd())) {
				String eol = doc.getLineDelimiter(doc.getLineOfOffset(partitionEnd));
				if (eol != null) {
					endCommentTokenLength = endCommentTokenLength + eol.length();
				}
			}
		}

		edits.add(factory.createEdit(partitionStart, startCommentTokenLength, "")); //$NON-NLS-1$
		edits.add(factory.createEdit(commentContentEnd, endCommentTokenLength, "")); //$NON-NLS-1$
	}

	@Override
	protected boolean isValidSelection(ITextSelection selection) {
		return selection != null && !selection.isEmpty() && selection.getLength() > 0;
	}

}
